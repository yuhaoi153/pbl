package flyfish.utils;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.UploadPartRequest;
import com.aliyun.oss.model.UploadPartResult;
import flyfish.properties.AliOSSProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.UUID;


import com.aliyun.oss.model.*;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 阿里云 OSS 工具类
 */
@Component
public class AliOSSUtils {

    @Autowired
    private AliOSSProperties aliOSSProperties;

    /**
     * 实现上传图片到OSS
     */
    public String upload(MultipartFile file) throws IOException {
        System.out.println(aliOSSProperties.getAccessKeyId());
        //获取阿里云OSS参数
        String endpoint = aliOSSProperties.getEndpoint();
        String accessKeyId = aliOSSProperties.getAccessKeyId();
        String accessKeySecret = aliOSSProperties.getAccessKeySecret();
        String bucketName = aliOSSProperties.getBucketName();

        // 获取上传的文件的输入流
        InputStream inputStream = file.getInputStream();

        // 避免文件覆盖
        String originalFilename = file.getOriginalFilename();
        String fileName = "miniprograme/"+UUID.randomUUID().toString() + originalFilename.substring(originalFilename.lastIndexOf("."));

        //上传文件到 OSS
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        ossClient.putObject(bucketName, fileName, inputStream);

        //文件访问路径
        String url = "https://"  + bucketName + "." + endpoint + "/" + fileName;
        // 关闭ossClient
        ossClient.shutdown();
        return url;// 把上传到oss的路径返回
    }
    public String uploadByFilePathAddOriginName(MultipartFile file, String filePath) throws IOException {
        System.out.println(aliOSSProperties.getAccessKeyId());

        // 1. 获取阿里云OSS参数
        String endpoint = aliOSSProperties.getEndpoint(); // 依然是 oss-cn-shenzhen.aliyuncs.com
        String customDomain = aliOSSProperties.getCustomDomain(); // 新增：oss.eduyuyue.com
        String accessKeyId = aliOSSProperties.getAccessKeyId();
        String accessKeySecret = aliOSSProperties.getAccessKeySecret();
        String bucketName = aliOSSProperties.getBucketName();

        // 获取上传文件的输入流
        InputStream inputStream = file.getInputStream();

        // 处理文件名
        String originalFilename = file.getOriginalFilename();
        String baseName = originalFilename;
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            int dotIndex = originalFilename.lastIndexOf(".");
            baseName = originalFilename.substring(0, dotIndex);
            extension = originalFilename.substring(dotIndex);
        }
        String fileName = filePath + baseName + "_" + UUID.randomUUID().toString() + extension;

        // 2. 创建OSS客户端 (使用官方 endpoint 进行底层通信，就不会报 UnknownHost 了)
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        // 设置对象元数据，实现预览优先 (这部分你写的完全正确，保持不变！)
        ObjectMetadata metadata = new ObjectMetadata();
        String contentType = file.getContentType();
        if (contentType == null || contentType.isEmpty()) {
            if (extension.equalsIgnoreCase(".pdf")) {
                contentType = "application/pdf";
            } else if (extension.equalsIgnoreCase(".jpg") || extension.equalsIgnoreCase(".jpeg")) {
                contentType = "image/jpeg";
            } else if (extension.equalsIgnoreCase(".png")) {
                contentType = "image/png";
            } else {
                contentType = "application/octet-stream";
            }
        }
        metadata.setContentType(contentType);
        // 关键：设置为 inline，使浏览器预览而非下载
        metadata.setContentDisposition("inline; filename=\"" + originalFilename + "\"");

        // 上传文件
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, fileName, inputStream, metadata);
        ossClient.putObject(putObjectRequest);

        // 3. 重点在这里！拼接返回给前端的 URL 时，抛弃 bucketName 和 endpoint，直接使用你的自定义域名
        // 生成的 URL 格式为：https://oss.eduyuyue.com/filePath/xxx.pdf
        String url = "https://" + customDomain + "/" + fileName;

        // 关闭OSSClient
        ossClient.shutdown();
        return url;
    }


    /**
     * 实现上传图片到OSS
     */
    public String uploadByFilePath(MultipartFile file,String filePath) throws IOException {
        System.out.println(aliOSSProperties.getAccessKeyId());
        //获取阿里云OSS参数
        String endpoint = aliOSSProperties.getEndpoint();
        String accessKeyId = aliOSSProperties.getAccessKeyId();
        String accessKeySecret = aliOSSProperties.getAccessKeySecret();
        String bucketName = aliOSSProperties.getBucketName();

        // 获取上传的文件的输入流
        InputStream inputStream = file.getInputStream();

        // 避免文件覆盖
        String originalFilename = file.getOriginalFilename();
        String fileName = filePath+UUID.randomUUID().toString() + originalFilename.substring(originalFilename.lastIndexOf("."));

        //上传文件到 OSS
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        ossClient.putObject(bucketName, fileName, inputStream);

        //文件访问路径
        String url = "https://"  + bucketName + "." + endpoint + "/" + fileName;
        // 关闭ossClient
        ossClient.shutdown();
        return url;// 把上传到oss的路径返回
    }

//    public String uploadCertification(MultipartFile file) throws IOException {
//        System.out.println(aliOSSProperties.getAccessKeyId());
//        String endpoint = aliOSSProperties.getEndpoint();
//        String accessKeyId = aliOSSProperties.getAccessKeyId();
//        String accessKeySecret = aliOSSProperties.getAccessKeySecret();
//        String bucketName = aliOSSProperties.getBucketName();
//
//        InputStream inputStream = file.getInputStream();
//        String originalFilename = file.getOriginalFilename();
//        String fileName = "miniprograme/certification/" + UUID.randomUUID() + originalFilename.substring(originalFilename.lastIndexOf("."));
//
//        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
//        ossClient.putObject(bucketName, fileName, inputStream);
//
//        String url = "https://" + bucketName + "." + endpoint + "/" + fileName;
//        ossClient.shutdown();
//        return url;
//    }

    public String uploadCertification(MultipartFile file) throws IOException {
        String endpoint = aliOSSProperties.getEndpoint();
        String accessKeyId = aliOSSProperties.getAccessKeyId();
        String accessKeySecret = aliOSSProperties.getAccessKeySecret();
        String bucketName = aliOSSProperties.getBucketName();

        // 考虑使用加速域名（如果开启）
        // String endpoint = "oss-accelerate.aliyuncs.com";

        InputStream inputStream = file.getInputStream();
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String uuid = UUID.randomUUID().toString();

        // 添加哈希前缀避免热点
        String hashPrefix = Integer.toHexString(uuid.hashCode() & 0xFFFF);
        String fileName = "miniprograme/certification/" + hashPrefix + "/" + uuid + extension;

        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        // 大文件使用分片上传
        if (file.getSize() > 100 * 1024 * 1024) { // 大于100MB
            // 执行分片上传逻辑
            uploadByMultipart(ossClient, bucketName, fileName, inputStream, file.getSize());
        } else {
            // 小文件使用简单上传
            ossClient.putObject(bucketName, fileName, inputStream);
        }

        String url = "https://" + bucketName + "." + endpoint + "/" + fileName;
        ossClient.shutdown();
        return url;
    }


    /**
     * 使用阿里云OSS分片上传大文件
     * @param ossClient OSS客户端
     * @param bucketName 存储空间名称
     * @param objectName 对象名称（包含路径）
     * @param inputStream 文件输入流（来自MultipartFile）
     * @param fileSize 文件大小（字节）
     * @throws IOException 文件读写异常
     */
    private void uploadByMultipart(OSS ossClient, String bucketName, String objectName,
                                   InputStream inputStream, long fileSize) throws IOException {
        // 1. 将输入流保存到临时文件（因为分片上传需要多次读取文件）
        File tempFile = File.createTempFile("oss_upload_", ".tmp");
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            byte[] buffer = new byte[8192];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                fos.write(buffer, 0, len);
            }
            fos.flush();
        } finally {
            // 关闭输入流（外部传入，但此处可以关闭，因为后续不再使用）
            try { inputStream.close(); } catch (IOException ignored) {}
        }

        // 2. 初始化分片上传
        InitiateMultipartUploadRequest initiateRequest = new InitiateMultipartUploadRequest(bucketName, objectName);
        InitiateMultipartUploadResult initiateResult = ossClient.initiateMultipartUpload(initiateRequest);
        String uploadId = initiateResult.getUploadId();
        List<PartETag> partETags = Collections.synchronizedList(new ArrayList<>());

        // 3. 计算分片信息（每个分片5MB，最后一个分片可能较小）
        long partSize = 5 * 1024 * 1024L; // 5MB
        long fileLength = tempFile.length();
        int partCount = (int) (fileLength / partSize);
        if (fileLength % partSize != 0) {
            partCount++;
        }

        // 4. 顺序上传分片（也可改为并行上传）
        try (RandomAccessFile raf = new RandomAccessFile(tempFile, "r")) {
            for (int i = 0; i < partCount; i++) {
                long startPos = i * partSize;
                long currentPartSize = Math.min(partSize, fileLength - startPos);
                byte[] data = new byte[(int) currentPartSize];
                raf.seek(startPos);
                raf.readFully(data);

                // 创建UploadPartRequest，传入数据
                UploadPartRequest uploadPartRequest = new UploadPartRequest();
                uploadPartRequest.setBucketName(bucketName);
                uploadPartRequest.setKey(objectName);
                uploadPartRequest.setUploadId(uploadId);
                uploadPartRequest.setPartNumber(i + 1); // 分片编号从1开始
                uploadPartRequest.setInputStream(new ByteArrayInputStream(data));
                uploadPartRequest.setPartSize(currentPartSize);

                UploadPartResult uploadPartResult = ossClient.uploadPart(uploadPartRequest);
                partETags.add(uploadPartResult.getPartETag());
            }
        } catch (Exception e) {
            // 上传失败时中止分片上传
            try {
                AbortMultipartUploadRequest abortRequest = new AbortMultipartUploadRequest(bucketName, objectName, uploadId);
                ossClient.abortMultipartUpload(abortRequest);
            } catch (Exception ignored) {}
            throw new IOException("分片上传失败", e);
        } finally {
            // 5. 删除临时文件
            if (tempFile.exists()) {
                tempFile.delete();
            }
        }

        // 6. 完成分片上传
        CompleteMultipartUploadRequest completeRequest = new CompleteMultipartUploadRequest(bucketName, objectName, uploadId, partETags);
        ossClient.completeMultipartUpload(completeRequest);
    }

//
//    // 新增：最灵活的 byte[] 方法，重载 uploadCertification，目的是接受文件字节数组上传，并指定文件扩展名和内容类型
//    public String uploadCertification(byte[] fileBytes, String fileExtension, String contentType) throws IOException {
//        String endpoint = aliOSSProperties.getEndpoint();
//        String accessKeyId = aliOSSProperties.getAccessKeyId();
//        String accessKeySecret = aliOSSProperties.getAccessKeySecret();
//        String bucketName = aliOSSProperties.getBucketName();
//
//        // 生成文件名
//        String fileName = "miniprograme/certification/" + UUID.randomUUID() +
//                (fileExtension.startsWith(".") ? fileExtension : "." + fileExtension);
//
//        InputStream inputStream = new ByteArrayInputStream(fileBytes);
//
//        // 创建 ObjectMetadata 设置内容类型
//        ObjectMetadata metadata = new ObjectMetadata();
//        metadata.setContentLength(fileBytes.length);
//        metadata.setContentType(contentType);
//
//        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
//        ossClient.putObject(bucketName, fileName, inputStream, metadata);
//
//        String url = "https://" + bucketName + "." + endpoint + "/" + fileName;
//        ossClient.shutdown();
//        inputStream.close();
//
//        return url;
//    }

    /**
     * 上传字节数组到阿里云OSS，自动选择简单上传或分片上传
     * @param fileBytes     文件字节数组
     * @param fileExtension 文件扩展名（如 "pdf" 或 ".pdf"）
     * @param contentType   文件MIME类型（如 "application/pdf"）
     * @return 可访问的文件URL
     * @throws IOException 上传失败时抛出
     */
    public String uploadCertification(byte[] fileBytes, String fileExtension, String contentType) throws IOException {
        // 1. 准备OSS客户端
        String endpoint = aliOSSProperties.getEndpoint();
        String accessKeyId = aliOSSProperties.getAccessKeyId();
        String accessKeySecret = aliOSSProperties.getAccessKeySecret();
        String bucketName = aliOSSProperties.getBucketName();

        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        // 2. 生成对象键（添加随机前缀避免热点）
        String uuid = UUID.randomUUID().toString();
        String hashPrefix = Integer.toHexString(uuid.hashCode() & 0xFFFF); // 4位十六进制
        String finalExtension = fileExtension.startsWith(".") ? fileExtension : "." + fileExtension;
        String objectName = "miniprograme/certification/" + hashPrefix + "/" + uuid + finalExtension;

        try {
            // 3. 根据文件大小选择上传方式
            long fileSize = fileBytes.length;
            if (fileSize < 100 * 1024 * 1024) { // 小于100MB使用简单上传
                simpleUpload(ossClient, bucketName, objectName, fileBytes, contentType);
            } else { // 大于等于100MB使用分片上传
                multipartUpload(ossClient, bucketName, objectName, fileBytes, contentType);
            }

            // 4. 构建并返回URL
            return "https://" + bucketName + "." + endpoint + "/" + objectName;
        } finally {
            ossClient.shutdown();
        }
    }

    /**
     * 简单上传（小文件）
     */
    private void simpleUpload(OSS ossClient, String bucketName, String objectName,
                              byte[] fileBytes, String contentType) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(fileBytes.length);
        metadata.setContentType(contentType);

        try (InputStream inputStream = new ByteArrayInputStream(fileBytes)) {
            ossClient.putObject(bucketName, objectName, inputStream, metadata);
        } catch (IOException e) {
            throw new RuntimeException("简单上传失败", e);
        }
    }

    /**
     * 分片上传（大文件）
     */
    private void multipartUpload(OSS ossClient, String bucketName, String objectName,
                                 byte[] fileBytes, String contentType) throws IOException {
        // 初始化分片上传
        InitiateMultipartUploadRequest initiateRequest = new InitiateMultipartUploadRequest(bucketName, objectName);
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(contentType);
        initiateRequest.setObjectMetadata(metadata);
        InitiateMultipartUploadResult initiateResult = ossClient.initiateMultipartUpload(initiateRequest);
        String uploadId = initiateResult.getUploadId();

        // 分片参数
        long partSize = 5 * 1024 * 1024L; // 5MB
        long fileLength = fileBytes.length;
        int partCount = (int) (fileLength / partSize);
        if (fileLength % partSize != 0) {
            partCount++;
        }

        List<PartETag> partETags = new ArrayList<>(partCount);

        try {
            // 按顺序上传分片（可优化为并行，但此处保持简单）
            for (int i = 0; i < partCount; i++) {
                long start = i * partSize;
                long currentPartSize = Math.min(partSize, fileLength - start);
                byte[] partData = new byte[(int) currentPartSize];
                System.arraycopy(fileBytes, (int) start, partData, 0, (int) currentPartSize);

                UploadPartRequest uploadPartRequest = new UploadPartRequest();
                uploadPartRequest.setBucketName(bucketName);
                uploadPartRequest.setKey(objectName);
                uploadPartRequest.setUploadId(uploadId);
                uploadPartRequest.setPartNumber(i + 1);
                uploadPartRequest.setInputStream(new ByteArrayInputStream(partData));
                uploadPartRequest.setPartSize(currentPartSize);

                UploadPartResult uploadPartResult = ossClient.uploadPart(uploadPartRequest);
                partETags.add(uploadPartResult.getPartETag());
            }

            // 完成分片上传
            CompleteMultipartUploadRequest completeRequest = new CompleteMultipartUploadRequest(
                    bucketName, objectName, uploadId, partETags);
            ossClient.completeMultipartUpload(completeRequest);
        } catch (Exception e) {
            // 上传失败时中止分片上传，清理已上传的分片
            try {
                AbortMultipartUploadRequest abortRequest = new AbortMultipartUploadRequest(bucketName, objectName, uploadId);
                ossClient.abortMultipartUpload(abortRequest);
            } catch (Exception ignored) {
            }
            throw new IOException("分片上传失败", e);
        }
    }




    // 新增：便捷方法，专门用于图片上传
    public String uploadImage(byte[] imageBytes) throws IOException {
        return uploadCertification(imageBytes, "png", "image/png");
    }



    /**
     * 根据图片URL下载图片到字节数组
     * @param imageUrl 图片URL
     * @return 图片字节数组
     */
    public byte[] downloadImage(String imageUrl) throws IOException {
        // 从URL中提取对象key
        String objectKey = extractObjectKeyFromUrl(imageUrl);

        if (objectKey == null) {
            throw new IOException("无法从URL中提取对象key: " + imageUrl);
        }

        // 获取阿里云OSS参数
        String endpoint = aliOSSProperties.getEndpoint();
        String accessKeyId = aliOSSProperties.getAccessKeyId();
        String accessKeySecret = aliOSSProperties.getAccessKeySecret();
        String bucketName = aliOSSProperties.getBucketName();

        // 创建OSS客户端
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        try (InputStream inputStream = ossClient.getObject(bucketName, objectKey).getObjectContent();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            // 读取数据
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, length);
            }

            return outputStream.toByteArray();

        } finally {
            // 关闭OSS客户端
            ossClient.shutdown();
        }
    }


    /**
     * 从URL中提取对象key
     * URL格式: https://bucket-name.endpoint/object-key
     * 例如: https://webtry.oss-cn-shenzhen.aliyuncs.com/miniprograme/certification/xxx.png
     */
    private String extractObjectKeyFromUrl(String imageUrl) {
        try {
            // 移除协议部分
            String urlWithoutProtocol = imageUrl.replace("https://", "");

            // 找到第一个斜杠，获取bucket和对象key的分界
            int firstSlashIndex = urlWithoutProtocol.indexOf("/");
            if (firstSlashIndex == -1) {
                return null;
            }

            // 提取对象key（第一个斜杠后面的部分）
            return urlWithoutProtocol.substring(firstSlashIndex + 1);

        } catch (Exception e) {
            return null;
        }
    }



}
