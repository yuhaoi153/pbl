package flyfish.utils;

import com.alibaba.nls.client.AccessToken;
import com.alibaba.nls.client.protocol.InputFormatEnum;
import com.alibaba.nls.client.protocol.NlsClient;
import com.alibaba.nls.client.protocol.SampleRateEnum;
import com.alibaba.nls.client.protocol.asr.SpeechRecognizer;
import com.alibaba.nls.client.protocol.asr.SpeechRecognizerListener;
import com.alibaba.nls.client.protocol.asr.SpeechRecognizerResponse;
import flyfish.properties.AliAudioProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Component
public class AliyunAudioRecognitionUtil {

    private static final Logger logger = LoggerFactory.getLogger(AliyunAudioRecognitionUtil.class);

    private final AliAudioProperties properties;
    private volatile AccessToken cachedToken;
    private final Object tokenLock = new Object();

    public AliyunAudioRecognitionUtil(AliAudioProperties properties) {
        this.properties = properties;
    }

    /**
     * 识别 MultipartFile 音频文件（必须是 PCM 格式，16kHz，单声道）
     */
    public String recognize(MultipartFile file) throws Exception {
        return recognize(file.getBytes(), 16000);
    }

    /**
     * 识别 PCM 音频字节数组
     */
    public String recognize(byte[] audioBytes, int sampleRate) throws Exception {
        if (audioBytes == null || audioBytes.length == 0) {
            throw new IllegalArgumentException("音频数据为空");
        }

        String token = getValidAccessToken();
        NlsClient client = createNlsClient(token);
        CompletableFuture<String> future = new CompletableFuture<>();
        SpeechRecognizer recognizer = null;

        try {
            recognizer = createSpeechRecognizer(client, future, sampleRate);
            recognizer.start();

            // 一次性发送整个音频文件
            recognizer.send(new ByteArrayInputStream(audioBytes));

            recognizer.stop();
            return future.get(60, TimeUnit.SECONDS);
        } catch (Exception e) {
            logger.error("语音识别失败", e);
            throw e;
        } finally {
            if (recognizer != null) recognizer.close();
            if (client != null) client.shutdown();
        }
    }
    // ---------- 私有方法 ----------

    private String getValidAccessToken() throws IOException {
        if (cachedToken == null || cachedToken.getExpireTime() - System.currentTimeMillis() < 60000) {
            synchronized (tokenLock) {
                if (cachedToken == null || cachedToken.getExpireTime() - System.currentTimeMillis() < 60000) {
                    AccessToken token = new AccessToken(properties.getAccessKeyId(), properties.getAccessKeySecret());
                    token.apply();
                    logger.info("获取新AccessToken，过期时间：{}", token.getExpireTime());
                    cachedToken = token;
                }
            }
        }
        return cachedToken.getToken();
    }

    private NlsClient createNlsClient(String token) {
        return new NlsClient(properties.getUrl(), token);
    }

    private SpeechRecognizer createSpeechRecognizer(NlsClient client,
                                                    CompletableFuture<String> future,
                                                    int sampleRate) throws Exception {
        SpeechRecognizerListener listener = new SpeechRecognizerListener() {
            @Override
            public void onRecognitionResultChanged(SpeechRecognizerResponse response) {
                logger.debug("中间结果：{}", response.getRecognizedText());
            }

            @Override
            public void onRecognitionCompleted(SpeechRecognizerResponse response) {
                String text = response.getRecognizedText();
                logger.info("识别完成：{}", text);
                future.complete(text);
            }

            @Override
            public void onStarted(SpeechRecognizerResponse response) {
                logger.debug("识别开始，task_id: {}", response.getTaskId());
            }

            @Override
            public void onFail(SpeechRecognizerResponse response) {
                String errorMsg = String.format("识别失败，task_id: %s, status: %d, status_text: %s",
                        response.getTaskId(), response.getStatus(), response.getStatusText());
                logger.error(errorMsg);
                future.completeExceptionally(new RuntimeException(errorMsg));
            }
        };

        SpeechRecognizer recognizer = new SpeechRecognizer(client, listener);
        recognizer.setAppKey(properties.getAppKey());
        recognizer.setFormat(InputFormatEnum.PCM);
        if (sampleRate == 16000) {
            recognizer.setSampleRate(SampleRateEnum.SAMPLE_RATE_16K);
        } else if (sampleRate == 8000) {
            recognizer.setSampleRate(SampleRateEnum.SAMPLE_RATE_8K);
        } else {
            throw new IllegalArgumentException("不支持的采样率：" + sampleRate);
        }
        recognizer.setEnableIntermediateResult(false);
        recognizer.addCustomedParam("enable_voice_detection", true);
        return recognizer;
    }

    private int getSleepDelta(int dataSize, int sampleRate) {
        int bytesPerSample = 2; // 16bit
        int channels = 1;
        int bytesPerSecond = sampleRate * bytesPerSample * channels;
        return (int) ((double) dataSize / bytesPerSecond * 1000);
    }
}
