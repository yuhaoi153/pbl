package flyfish.utils;

import okhttp3.*;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

/**
 * 百度语音识别工具类
 * 依赖：
 * - okhttp:4.12.0
 * - org.json
 *
 * 使用前请确保音频格式为 PCM、16000Hz、单声道。
 * 将识别结果以字符串形式返回。
 */
@Component
public class BaiduAudioUtils {

    private final String apiKey;
    private final String secretKey;
    private final String cuid;

    // 全局 HTTP 客户端，设置较长的读超时以等待识别结果
    private static final OkHttpClient HTTP_CLIENT = new OkHttpClient.Builder()
            .readTimeout(300, TimeUnit.SECONDS)
            .build();

    public BaiduAudioUtils(
            @Value("${baidu.audio.api-key}") String apiKey,
            @Value("${baidu.audio.secret-key}") String secretKey,
            @Value("${baidu.audio.cuid}") String cuid
    ) {
        this.apiKey = apiKey;
        this.secretKey = secretKey;
        this.cuid = cuid;
    }

    /**
     * 识别语音文件，返回文本结果
     *
     * @param  （PCM格式，16kHz，单声道）
     * @return 识别出的文本
     * @throws IOException 如果文件读取失败、网络异常或API返回错误
     */
    public String recognize(byte[] audioBytes, String format, int rate, int channel) throws IOException {
        // 1. 获取 Access Token（可缓存）
        String accessToken = getAccessToken();

        // 2. 对音频字节数组进行 Base64 编码
        String audioBase64 = Base64.getEncoder().encodeToString(audioBytes);

        // 3. 构造请求体（与之前类似）
        JSONObject speech = new JSONObject();
        speech.put("audio", audioBase64);
        speech.put("audio_len", audioBytes.length);

        JSONObject requestBody = new JSONObject();
        requestBody.put("format", format);
        requestBody.put("rate", rate);
        requestBody.put("channel", channel);
        requestBody.put("cuid", cuid);
        requestBody.put("token", accessToken);
        requestBody.put("speech", speech);

        // 4. 发送 POST 请求
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, requestBody.toString());
        Request request = new Request.Builder()
                .url("https://vop.baidu.com/pro_api")
                .post(body)
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = HTTP_CLIENT.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected HTTP response: " + response);
            }

            String responseBody = response.body().string();
            JSONObject jsonResponse = new JSONObject(responseBody);

            // 5. 检查 API 返回的错误码
            if (jsonResponse.has("err_no") && jsonResponse.getInt("err_no") != 0) {
                String errMsg = jsonResponse.optString("err_msg", "Unknown error");
                throw new IOException("API error: " + errMsg);
            }

            // 6. 提取识别结果（result 为数组，取第一个）
            if (jsonResponse.has("result")) {
                return jsonResponse.getJSONArray("result").getString(0);
            } else {
                return null; // 无识别结果
            }
        }
    }


    public String recognize(MultipartFile file) throws IOException {
        // 可根据文件扩展名或传入参数决定 format/rate/channel，此处假设固定 PCM/16000/单声道
        return recognize(file.getBytes(), "pcm", 16000, 1);
    }
    /**
     * 获取百度 API 的 Access Token
     *
     * @return access_token 字符串
     * @throws IOException 如果网络请求失败或返回错误
     */
    private String getAccessToken() throws IOException {
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        String params = "grant_type=client_credentials&client_id=" + apiKey
                + "&client_secret=" + secretKey;
        RequestBody body = RequestBody.create(mediaType, params);
        Request request = new Request.Builder()
                .url("https://aip.baidubce.com/oauth/2.0/token")
                .post(body)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();

        try (Response response = HTTP_CLIENT.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Failed to get access token: " + response);
            }

            String responseBody = response.body().string();
            JSONObject json = new JSONObject(responseBody);

            if (json.has("error")) {
                String errorDesc = json.optString("error_description", "No description");
                throw new IOException("Error fetching token: " + errorDesc);
            }

            return json.getString("access_token");
        }
    }

//    // 可选：提供一个带格式参数的重载方法，以便支持不同的音频格式
//    public static String recognize(File audioFile, String format, int rate, int channel) throws IOException {
//        // 类似上面的实现，但使用传入的 format/rate/channel 覆盖默认值
//        // 此处省略，可根据需要自行扩展
//        return recognize(audioFile);
//    }
}
