package flyfish.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import flyfish.config.DeepSeekConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DeepSeek 大模型工具类。
 */
@Component
@Slf4j
public class DeepSeekUtills {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private DeepSeekConfig deepSeekConfig;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 提取作业相关信息（姓名、日期、状态）。
     */
    public String getJsonHomeWork(String message) {
        String systemPrompt = "请从用户提供的文本中提取信息，严格按照 JSON 格式输出，不要输出 Markdown 代码块或其他文字。\n" +
                "输出格式：\n" +
                "{\n" +
                "  \"namelistStr\": \"把所有姓名转化为拼音（不带声调），用‘、’拼接；若未找到则返回‘无’\",\n" +
                "  \"checkdateStr\": \"日期，格式为数字xx月xx日，若未找到则返回‘无’\",\n" +
                "  \"typeStr\": \"判断作业状态，从以下选择一个：完成作业、未提交作业、优秀作业、不达标作业、订正作业；若无法判断则返回‘无’\"\n" +
                "}\n\n" +
                "注意：\n" +
                "- 姓名可能以‘和’、‘与’、‘、’等分隔，需全部提取。\n" +
                "- 拼音使用标准普通话读音，如‘张三’转为‘zhangsan’。\n" +
                "- 如果有‘很好’、‘很不错’、‘好’、‘优秀’等字眼，判断为优秀作业。\n" +
                "- 如果有‘不好’、‘不太好’、‘差’、‘不达标’等字眼，判断为不达标作业。";

        String result = callDeepSeek(systemPrompt, message);
        log.info("DeepSeek getJsonHomeWork 响应:{}", result);
        return result;
    }

    /**
     * 从键盘输入中提取作业名称、学生姓名、日期和作业状态。
     */
    public String getJsonMessageHomeWork(String message) {
        String systemPrompt = "请从用户提供的文本中提取信息，严格按照 JSON 格式输出，不要输出 Markdown 代码块或其他文字。\n" +
                "输出格式：\n" +
                "{\n" +
                "  \"homeworkName\": \"作业名称，转化为拼音；若未找到则返回‘无’\",\n" +
                "  \"namelistStr\": \"把所有姓名转化为拼音（不带声调），用‘、’拼接；若未找到则返回‘无’\",\n" +
                "  \"checkdateStr\": \"日期，格式为数字xx月xx日，若未找到则返回‘无’\",\n" +
                "  \"typeStr\": \"判断作业状态，从以下选择一个：完成作业、未提交作业、优秀作业、不达标作业、订正作业；若无法判断则返回‘无’\"\n" +
                "}\n\n" +
                "注意：\n" +
                "- 姓名可能以‘和’、‘与’、‘、’等分隔，需全部提取。\n" +
                "- 拼音使用标准普通话读音，如‘张三’转为‘zhangsan’。\n" +
                "- 如果有‘很好’、‘很不错’、‘好’、‘优秀’等字眼，判断为优秀作业。\n" +
                "- 如果有‘不好’、‘不太好’、‘差’、‘不达标’等字眼，判断为不达标作业。";

        String result = callDeepSeek(systemPrompt, message);
        log.info("DeepSeek getJsonMessageHomeWork 响应:{}", result);
        return result;
    }

    private String callDeepSeek(String systemPrompt, String userContent) {
        validateConfig();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(deepSeekConfig.getApiKey());

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(message("system", systemPrompt));
        messages.add(message("user", userContent));

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", deepSeekConfig.getModel());
        requestBody.put("messages", messages);
        Map<String, String> thinking = new HashMap<>();
        thinking.put("type", deepSeekConfig.getThinkingType());
        if ("enabled".equals(deepSeekConfig.getThinkingType())) {
            thinking.put("reasoning_effort", deepSeekConfig.getReasoningEffort());
        }
        requestBody.put("thinking", thinking);
        requestBody.put("temperature", 0.2);
        requestBody.put("response_format", Map.of("type", "json_object"));
        requestBody.put("stream", false);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                    deepSeekConfig.getUrl(), request, String.class);
            if (response.getStatusCode() != HttpStatus.OK || !StringUtils.hasText(response.getBody())) {
                throw new IllegalStateException("DeepSeek API 返回异常，状态码: " + response.getStatusCode());
            }

            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode content = root.path("choices").path(0).path("message").path("content");
            if (!content.isTextual() || !StringUtils.hasText(content.asText())) {
                log.error("DeepSeek API 响应中缺少 choices[0].message.content: {}", response.getBody());
                throw new IllegalStateException("DeepSeek API 响应格式不符合预期");
            }
            return content.asText();
        } catch (Exception e) {
            log.error("调用 DeepSeek API 异常: {}", e.getMessage(), e);
            throw new RuntimeException("调用 DeepSeek API 失败", e);
        }
    }

    private Map<String, String> message(String role, String content) {
        Map<String, String> message = new HashMap<>();
        message.put("role", role);
        message.put("content", content);
        return message;
    }

    private void validateConfig() {
        if (!StringUtils.hasText(deepSeekConfig.getApiKey())) {
            throw new IllegalStateException("未配置 deepseek.api-key，请先在 application.yml 中填写 DeepSeek API Key");
        }
        if (!StringUtils.hasText(deepSeekConfig.getUrl()) || !StringUtils.hasText(deepSeekConfig.getModel())) {
            throw new IllegalStateException("DeepSeek 的 url 或 model 配置缺失");
        }
        if (!List.of("enabled", "disabled").contains(deepSeekConfig.getThinkingType())) {
            throw new IllegalStateException("deepseek.thinking-type 只能配置为 enabled 或 disabled");
        }
        if ("enabled".equals(deepSeekConfig.getThinkingType())
                && !List.of("low", "medium", "high", "max").contains(deepSeekConfig.getReasoningEffort())) {
            throw new IllegalStateException("deepseek.reasoning-effort 只能配置为 low、medium、high 或 max");
        }
    }
}
