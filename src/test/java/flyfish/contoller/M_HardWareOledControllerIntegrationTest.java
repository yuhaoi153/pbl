package flyfish.contoller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import flyfish.handler.M_HardWareOledApiExceptionHandler;
import flyfish.properties.M_HardWareOledProperties;
import flyfish.service.impl.M_HardWareOledServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.nio.charset.StandardCharsets;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class M_HardWareOledControllerIntegrationTest {
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        M_HardWareOledProperties properties = new M_HardWareOledProperties();
        properties.setMaxMessageCodePoints(200);
        properties.setMaxMessageUtf8Bytes(1024);

        M_HardWareOledServiceImpl service =
                new M_HardWareOledServiceImpl(properties);
        M_HardWareOledController controller =
                new M_HardWareOledController(service);

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new M_HardWareOledApiExceptionHandler())
                .setValidator(validator)
                .build();
        objectMapper = new ObjectMapper().findAndRegisterModules();
    }

    @Test
    void chineseUploadPollAndVersionDedupWork() throws Exception {
        String uploadJson = """
                {
                  "deviceId": "screen-001",
                  "message": "你好，唐佳同学",
                  "requestId": "test-001"
                }
                """;

        String responseBody = mockMvc.perform(post("/mpi/oled/uploadNotificaiton")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(uploadJson))
                .andExpect(status().isCreated())
                .andExpect(header().exists("X-Notification-Version"))
                .andExpect(jsonPath("$.message").value("你好，唐佳同学"))
                .andExpect(jsonPath("$.duplicate").value(false))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        JsonNode response = objectMapper.readTree(responseBody);
        long version = response.path("version").asLong();

        mockMvc.perform(get("/mpi/oled/latestNotification")
                        .param("deviceId", "screen-001")
                        .param("afterVersion", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.version").value(version))
                .andExpect(jsonPath("$.message").value("你好，唐佳同学"));

        mockMvc.perform(get("/mpi/oled/latest")
                        .param("deviceId", "screen-001")
                        .param("lastVersion", Long.toString(version)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/mpi/oled/latestNotification")
                        .param("deviceId", "screen-001")
                        .param("afterVersion", Long.toString(version + 1)))
                .andExpect(status().isNoContent());
    }

    @Test
    void uploadRetryIsIdempotentAndBothSpellingsWork() throws Exception {
        String uploadJson = """
                {
                  "deviceId": "screen-001",
                  "message": "请到办公室领取资料",
                  "requestId": "test-002"
                }
                """;

        mockMvc.perform(post("/mpi/oled/uploadNotification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(uploadJson))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/mpi/oled/uploadNotificaiton")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(uploadJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.duplicate").value(true));
    }

    @Test
    void requestsNeedNoTokenAndDevicesRemainIsolated() throws Exception {
        mockMvc.perform(post("/mpi/oled/uploadNotification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "deviceId": "screen-002",
                                  "message": "第二块屏幕的通知",
                                  "requestId": "test-003"
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/mpi/oled/latest")
                        .param("deviceId", "screen-002")
                        .param("lastVersion", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("第二块屏幕的通知"));

        mockMvc.perform(get("/mpi/oled/latest")
                        .param("deviceId", "screen-001")
                        .param("lastVersion", "0"))
                .andExpect(status().isNoContent());
    }
}
