package flyfish.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import flyfish.config.QianfanConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 百度文心（千帆）大模型工具类
 * 使用 REST API 调用替代原 SDK 调用
 */
@Component
@Slf4j
public class BaiWenXinUtills {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private QianfanConfig qianfanConfig;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 通用请求发送与结果提取
     * @param systemPrompt 系统提示词
     * @param userContent 用户输入内容
     * @param model 使用的模型名称
     * @return 模型返回的文本结果
     */
    private String callQianfan(String systemPrompt, String userContent, String model) {
        // 构建请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", qianfanConfig.getAuthorization());
        headers.set("appid", qianfanConfig.getAppid());

        // 构建消息列表
        List<Map<String, String>> messages = new ArrayList<>();

        Map<String, String> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", systemPrompt);
        messages.add(systemMessage);

        Map<String, String> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", userContent);
        messages.add(userMessage);

        // 构建请求体
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("messages", messages);
        // 可根据需要添加 temperature 等参数
        requestBody.put("temperature", 0.7);
        requestBody.put("web_search", Map.of("enable", false));

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            // 发送请求
            ResponseEntity<String> response = restTemplate.postForEntity(
                    qianfanConfig.getUrl(), request, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                String body = response.getBody();
                // 解析返回 JSON，提取 result 字段（兼容 choices 结构）
                JsonNode root = objectMapper.readTree(body);
                if (root.has("result")) {
                    return root.get("result").asText();
                } else if (root.has("choices") && root.get("choices").isArray()
                        && root.get("choices").size() > 0) {
                    JsonNode messageNode = root.get("choices").get(0).get("message");
                    if (messageNode != null && messageNode.has("content")) {
                        return messageNode.get("content").asText();
                    }
                }
                // 未找到预期字段，返回原始响应体
                log.warn("未从响应中提取到 result 字段，返回原始响应体");
                return body;
            } else {
                log.error("千帆API调用失败，状态码：{}", response.getStatusCode());
                throw new RuntimeException("API调用失败，状态码: " + response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("调用千帆API异常", e);
            throw new RuntimeException("调用千帆API失败", e);
        }
    }

    /**
     * 提取反馈信息（姓名、评价、原因、惩罚措施）
     */
    public String getJsonFeedback(String message) {
        String systemPrompt = "你是一个专业的文本分析助手。请从用户提供的文本中提取以下信息，并以JSON格式返回。\n" +
                "第一部分是name，要把中文姓名转换为拼音不带声调；\n" +
                "第二部分是situation，内容为表扬或者批评二选一；\n" +
                "第三部分是reason，是原因；\n" +
                "第四部分是punishMeasures，是惩罚举措，如果没有则填“无”。\n" +
                "请确保返回格式为：{\"name\": \"\", \"situation\": \"\", \"reason\": \"\", \"punishMeasures\": \"\"}";

        String result = callQianfan(systemPrompt, message, "deepseek-v4-flash");
        log.info("getJsonFeedback 响应:{}", result);
        return result;
    }

    /**
     * 提取作业相关信息（姓名、日期、状态）
     */
    public String getJsonHomeWork(String message) {
        String systemPrompt = "请从用户提供的文本中提取信息，严格按照 JSON 格式输出。\n" +
                "输出格式：\n" +
                "{\n" +
                "  \"namelistStr\": \"把所有姓名转化为拼音（不带声调），用“、”拼接\",\n" +
                "  \"checkdateStr\": \"日期，格式为 数字xx月xx日，若未找到则返回“无”\",\n" +
                "  \"typeStr\": \"判断作业状态，从以下选择一个：完成作业、未提交作业、优秀作业、不达标作业、订正作业\"\n" +
                "}\n\n" +
                "注意：\n" +
                "- 姓名可能以“和”、“与”、“、”等分隔，需全部提取。\n" +
                "- 拼音使用标准普通话读音，如“张三” → “zhangsan”。\n" +
                "- 如果有很好，很不错，好，优秀这样的字眼判断为优秀作业，如果有不好，不太好，差，不达标这样的字眼判断为不达标作业。";

        String result = callQianfan(systemPrompt, message, "deepseek-v4-flash");
        log.info("getJsonHomeWork 响应:{}", result);
        return result;
    }

    /**
     * 键盘登记作业
     */
    public String getJsonMessageHomeWork(String message) {
        String systemPrompt = "请从用户提供的文本中提取信息，严格按照 JSON 格式输出。\n" +
                "输出格式：\n" +
                "{\n" +
                "  \"homeworkName\": \"作业名称，转化为拼音\",\n" +
                "  \"namelistStr\": \"把所有姓名转化为拼音（不带声调），用“、”拼接\",\n" +
                "  \"checkdateStr\": \"日期，格式为 数字xx月xx日，若未找到则返回“无”\",\n" +
                "  \"typeStr\": \"判断作业状态，从以下选择一个：完成作业、未提交作业、优秀作业、不达标作业、订正作业\"\n" +
                "}\n\n" +
                "注意：\n" +
                "- 姓名可能以“和”、“与”、“、”等分隔，需全部提取。\n" +
                "- 拼音使用标准普通话读音，如“张三” → “zhangsan”。\n" +
                "- 如果有很好，很不错，好，优秀这样的字眼判断为优秀作业，如果有不好，不太好，差，不达标这样的字眼判断为不达标作业。";

        String result = callQianfan(systemPrompt, message, "deepseek-v4-flash");
        log.info("getJsonMessageHomeWork 响应:{}", result);
        return result;
    }

    /**
     * PBL项目式学习评价打分
     */
    public String getJsonPBLFindAndRank(String audioText) {
        String systemPrompt = "你是一位耐心、善于鼓励的小学数学老师。请根据学生对“图形的密铺，与什么相关？为什么？”的回答，进行评分并给出评语。\n\n" +
                "评分标准（满分10分）：\n" +
                "- 学生回答与“角度之和是否为360度”相关，给满分10分。\n" +
                "- 若提到“角度”但未明确360度，或提到“边长”、“形状”、“拼接”等合理思考，可给8-9分。\n" +
                "- 若学生表达清晰、有条理、有独特见解，可在总分基础上酌情加0.5-1分，但总分不超过10分。\n" +
                "- 即使回答不完全准确，只要展现出思考过程，也应给予鼓励。\n\n" +
                "请严格按照以下JSON格式输出，不要包含其他文字：\n" +
                "{\n" +
                "  \"score\": 数字,\n" +
                "  \"comment\": \"评语，以肯定和鼓励为主，指出亮点，不要告诉角度和360度这个答案，后面还有探究环节\"\n" +
                "}\n\n" +
                "注意：\n" +
                "- 评语要积极正向，但是不能告诉角度和360度这个答案，后面还有探究环节\n" +
                "- 如果回答有误，委婉鼓励，不直接否定。\n" +
                "- 严格遵守JSON格式。";

        String result = callQianfan(systemPrompt, audioText, "deepseek-v4-flash");
        log.info("getJsonPBLFindAndRank 响应:{}", result);
        return result;
    }
}