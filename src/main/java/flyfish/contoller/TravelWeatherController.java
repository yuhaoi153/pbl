package flyfish.contoller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

@Slf4j
@RestController
public class TravelWeatherController {

    private final String caiyunKey;
    private final String caiyunSecret;
    private final String caiyunHost;
    private final HttpClient httpClient;

    public TravelWeatherController(
            @Value("${caiyun.key}") String caiyunKey,
            @Value("${caiyun.secret}") String caiyunSecret,
            @Value("${caiyun.host:https://api.caiyunapp.com}") String caiyunHost
    ) {
        this.caiyunKey = caiyunKey;
        this.caiyunSecret = caiyunSecret;
        this.caiyunHost = caiyunHost;
        this.httpClient = HttpClient.newHttpClient();
    }

    // 后端代理查询彩云天气
    @GetMapping(value = "/mpi/travel/getCaiyunWeather", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getCaiyunWeather(
            @RequestParam String lon,
            @RequestParam String lat
    ) {
        try {
            String location = lon + "," + lat;
            String pathname = "/v2.6/" + caiyunKey + "/" + location + "/weather";

            Map<String, String> query = new TreeMap<>();
            query.put("alert", "true");
            query.put("dailysteps", "1");
            query.put("hourlysteps", "24");

            String queryString = buildQueryString(query);
            String nonce = UUID.randomUUID().toString();
            String timestamp = String.valueOf(Instant.now().getEpochSecond());

            String stringToSign = String.join(
                    ":",
                    "GET",
                    pathname,
                    queryString,
                    caiyunKey,
                    nonce,
                    timestamp
            );

            String signature = hmacSha256Base64Url(caiyunSecret, stringToSign);

            String url = caiyunHost + pathname + "?" + queryString;

            log.info("请求彩云天气 url={}", url);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .header("x-cy-nonce", nonce)
                    .header("x-cy-timestamp", timestamp)
                    .header("x-cy-signature", signature)
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            log.info("彩云天气响应状态={}, body={}", response.statusCode(), response.body());

            return ResponseEntity
                    .status(response.statusCode())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(response.body());

        } catch (Exception e) {
            log.error("查询彩云天气失败 lon={}, lat={}", lon, lat, e);
            return ResponseEntity
                    .status(500)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("{\"status\":\"error\",\"message\":\"查询彩云天气失败\"}");
        }
    }

    private String buildQueryString(Map<String, String> query) {
        StringBuilder sb = new StringBuilder();

        for (Map.Entry<String, String> entry : query.entrySet()) {
            if (sb.length() > 0) {
                sb.append("&");
            }

            sb.append(urlEncode(entry.getKey()));
            sb.append("=");
            sb.append(urlEncode(entry.getValue()));
        }

        return sb.toString();
    }

    private String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8)
                .replace("%20", "+");
    }

    private String hmacSha256Base64Url(String secret, String data) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(
                secret.getBytes(StandardCharsets.UTF_8),
                "HmacSHA256"
        );
        mac.init(secretKeySpec);

        byte[] rawHmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));

        return Base64.getEncoder()
                .encodeToString(rawHmac)
                .replace("+", "-")
                .replace("/", "_");
    }
}
