package flyfish.service.impl;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.UUID;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import flyfish.service.DirectMailService;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

@Service
public class DirectMailServiceImpl implements DirectMailService {

    @Value("${aliyun.directmail.accessKeyId}")
    private String accessKeyId;

    @Value("${aliyun.directmail.accessKeySecret}")
    private String accessKeySecret;

    @Value("${aliyun.directmail.accountName}")
    private String accountName;

    @Autowired
    private RestTemplate restTemplate;

    private String generateSignature(String data, String key) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(new SecretKeySpec(key.getBytes("UTF-8"), "HmacSHA1"));
        byte[] rawHmac = mac.doFinal(data.getBytes("UTF-8"));
        return Base64.getEncoder().encodeToString(rawHmac);
    }

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
    public void sendMail(String toEmail, String subject, String content) throws Exception {
        String apiUrl = "https://dm.aliyuncs.com/";
        String timestamp = ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"));



        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(apiUrl)
                .queryParam("Action", "SingleSendMail")
                .queryParam("Version", "2015-11-23")
                .queryParam("Format", "JSON")
                .queryParam("AccountName", accountName)
                .queryParam("ReplyToAddress", "true")
                .queryParam("AddressType", "1")
                .queryParam("ToAddress", toEmail)
                .queryParam("Subject", subject)
                .queryParam("HtmlBody", content)
                .queryParam("AccessKeyId", accessKeyId)
                .queryParam("SignatureMethod", "HMAC-SHA1")
                .queryParam("Timestamp", timestamp)
                .queryParam("SignatureVersion", "1.0")
                .queryParam("SignatureNonce", UUID.randomUUID().toString());

        // Prepare string to sign
        String queryString = builder.build().encode().toUriString().split("\\?")[1];
        String stringToSign = "POST&" + URLEncoder.encode("/", StandardCharsets.UTF_8) + "&" + URLEncoder.encode(queryString, StandardCharsets.UTF_8);

        // Generate signature
        String signature = generateSignature(stringToSign, accessKeySecret + "&");
        builder.queryParam("Signature", URLEncoder.encode(signature, StandardCharsets.UTF_8));

        String finalUrl = builder.build().encode().toUri().toString();
        restTemplate.postForObject(finalUrl, null, String.class);
        System.out.println("Constructed URL: " + finalUrl);
    }
}
