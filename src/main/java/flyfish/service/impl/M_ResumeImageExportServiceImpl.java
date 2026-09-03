package flyfish.service.impl;

import flyfish.pojo.M_Certification;
import flyfish.service.M_ResumeImageExportService;
import flyfish.utils.AliOSSUtils;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@Slf4j
public class M_ResumeImageExportServiceImpl implements M_ResumeImageExportService {

    @Autowired
    private AliOSSUtils aliOSSUtils;

    // 线程池配置
    private static final int THREAD_POOL_SIZE = 6; // 可根据实际情况调整
    private final ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);


    /**
     * Spring 容器销毁 Bean 前调用，优雅关闭线程池
     */
    @PreDestroy
    public void destroy() {
        log.info("开始关闭线程池...");
        // 1. 停止接收新任务，并等待已提交任务执行完成
        executorService.shutdown();
        try {
            // 2. 等待所有任务结束，最多等待 30 秒（可根据需要调整）
            if (!executorService.awaitTermination(30, TimeUnit.SECONDS)) {
                // 3. 若超时仍有任务未完成，则强制停止
                executorService.shutdownNow();
                if (!executorService.awaitTermination(30, TimeUnit.SECONDS)) {
                    log.error("线程池未能正常关闭");
                }
            }
        } catch (InterruptedException e) {
            // 4. 若当前线程被中断，也强制停止
            executorService.shutdownNow();
            Thread.currentThread().interrupt(); // 保留中断状态
        }
        log.info("线程池已关闭");
    }


    /**
     * 内部类：下载结果
     */
    private static class DownloadResult {
        String fileName;
        byte[] data;

        DownloadResult(String fileName, byte[] data) {
            this.fileName = fileName;
            this.data = data;
        }
    }

    public byte[] generateImageZip(List<M_Certification> certifications) {
        if (certifications == null || certifications.isEmpty()) {
            return new byte[0];
        }

        // 1. 收集所有需要下载的图片任务
        List<DownloadTask> tasks = new ArrayList<>();
        for (M_Certification cert : certifications) {
            if (cert.getImageUrl() != null && !cert.getImageUrl().isEmpty()) {
                String[] imageUrls = cert.getImageUrl().split(";");
                for (int i = 0; i < imageUrls.length; i++) {
                    String imageUrl = imageUrls[i].trim();
                    if (!imageUrl.isEmpty()) {
                        // 基础URL有效性检查
                        if (imageUrl.length() > 2000 || imageUrl.contains("\n") || imageUrl.contains("Error")) {
                            log.warn("图片URL可能无效，跳过: {}", imageUrl);
                            continue;
                        }
                        tasks.add(new DownloadTask(imageUrl, cert, i));
                    }
                }
            }
        }

        // 2. 并发执行下载任务
        List<Future<DownloadResult>> futures;
        try {
            futures = executorService.invokeAll(tasks);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("下载任务被中断", e);
        }

        // 3. 收集成功下载的结果
        List<DownloadResult> results = new ArrayList<>();
        for (Future<DownloadResult> future : futures) {
            try {
                DownloadResult result = future.get();
                if (result != null) {
                    results.add(result);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("下载任务中断", e);
            } catch (ExecutionException e) {
                log.warn("下载任务执行异常", e.getCause());
            }
        }

        // 4. 顺序写入ZIP
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(baos)) {

            for (DownloadResult result : results) {
                try {
                    ZipEntry entry = new ZipEntry("images/" + result.fileName);
                    zos.putNextEntry(entry);
                    zos.write(result.data);
                    zos.closeEntry();
                } catch (Exception e) {
                    log.warn("添加图片到压缩包失败: {}", result.fileName, e);
                }
            }

            zos.finish();
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("生成图片压缩包失败", e);
            throw new RuntimeException("生成图片包失败: " + e.getMessage());
        }
    }

    /**
     * 下载任务（Callable）
     */
    private class DownloadTask implements Callable<DownloadResult> {
        private final String imageUrl;
        private final M_Certification cert;
        private final int index;

        DownloadTask(String imageUrl, M_Certification cert, int index) {
            this.imageUrl = imageUrl;
            this.cert = cert;
            this.index = index;
        }

        @Override
        public DownloadResult call() throws Exception {
            byte[] imageData = downloadImage(imageUrl);
            if (imageData == null) {
                return null;
            }
            String fileName = generateImageFileName(cert, index, imageUrl);
            return new DownloadResult(fileName, imageData);
        }
    }

    /**
     * 生成文件名（与原逻辑一致）
     */
    private String generateImageFileName(M_Certification cert, int index, String imageUrl) {
        String awardName = cert.getAwardName() != null ?
                cert.getAwardName().replaceAll("[^\\w\\u4e00-\\u9fa5]", "_") : "未知奖项";
        awardName = awardName + "_" + (cert.getCreateTime() != null ? cert.getCreateTime().toLocalDate().toString() : "未知日期");
        awardName = awardName.replaceAll("[:]", "-");
        awardName = awardName + "_" + cert.getId();

        String extension = getFileExtension(imageUrl);
        return String.format("%s_%d%s", awardName, index + 1, extension);
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String url) {
        String cleanUrl = url.split("\\?")[0];
        int dotIndex = cleanUrl.lastIndexOf(".");
        if (dotIndex > 0 && dotIndex < cleanUrl.length() - 1) {
            String ext = cleanUrl.substring(dotIndex);
            if (ext.length() <= 5) {
                return ext;
            }
        }
        return ".jpg";
    }

    /**
     * 下载图片（内部调用 AliOSSUtils）
     */
    private byte[] downloadImage(String imageUrl) {
        try {
            return aliOSSUtils.downloadImage(imageUrl);
        } catch (Exception e) {
            log.error("下载图片失败: {}", imageUrl, e);
            return null;
        }
    }
}