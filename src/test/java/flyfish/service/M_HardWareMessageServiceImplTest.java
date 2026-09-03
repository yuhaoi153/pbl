package flyfish.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import flyfish.mapper.M_HardWareDeviceMapper;
import flyfish.mapper.M_HardWareMessageMapper;
import flyfish.pojo.DTO.M_HardWareSendMessageDTO;
import flyfish.pojo.M_HardWareMessage;
import flyfish.pojo.M_HardwareDevice;
import flyfish.pojo.VO.M_HardWareDeviceVO;
import flyfish.pojo.VO.M_HardWareSendMessageVO;
import flyfish.pojo.VO.M_HardWareMessagePollVO;
import flyfish.properties.M_HardWareMqttProperties;
import flyfish.service.impl.M_HardWareMessageServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class M_HardWareMessageServiceImplTest {
    private M_HardWareDeviceMapper deviceMapper;
    private M_HardWareMessageMapper messageMapper;
    private M_HardWareMqttPublisher mqttPublisher;
    private M_HardWareMessageService service;

    @BeforeEach
    void setUp() {
        deviceMapper = mock(M_HardWareDeviceMapper.class);
        messageMapper = mock(M_HardWareMessageMapper.class);
        mqttPublisher = mock(M_HardWareMqttPublisher.class);

        M_HardWareMqttProperties properties = new M_HardWareMqttProperties();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        service = new M_HardWareMessageServiceImpl(
                deviceMapper, messageMapper, mqttPublisher, properties, objectMapper);
    }

    @Test
    void findsAllSupervisorDevicesAndBuildsDeviceNames() {
        M_HardwareDevice secondDevice = new M_HardwareDevice(
                2, "附小", "四年级", 9,
                "oledScreen", 2, "温馨", "班级通知", "于文字");
        when(deviceMapper.findByClassAndSupervisor("附小", "四年级", 9, "于文字"))
                .thenReturn(List.of(device(), secondDevice));

        List<M_HardWareDeviceVO> devices =
                service.findDevices("附小", "四年级", 9, "于文字");

        assertThat(devices).hasSize(2);
        assertThat(devices.get(0).getUserName()).isEqualTo("唐佳");
        assertThat(devices.get(0).getDeviceName()).isEqualTo("oledScreen1");
        assertThat(devices.get(0).getTeacherName()).isEqualTo("于文字");
        assertThat(devices.get(1).getUserName()).isEqualTo("温馨");
        assertThat(devices.get(1).getDeviceName()).isEqualTo("oledScreen2");
    }

    @Test
    void storesAndPublishesHardwareMessage() {
        when(deviceMapper.findById(1)).thenReturn(Optional.of(device()));
        when(messageMapper.insert(any(M_HardWareMessage.class))).thenAnswer(invocation -> {
            M_HardWareMessage message = invocation.getArgument(0);
            message.setId(42);
            return 1;
        });

        M_HardWareSendMessageDTO request = new M_HardWareSendMessageDTO();
        request.setDeviceId(1);
        request.setSuperVisor("李老师");
        request.setMessageType("notification");
        request.setMessageContent("请四年级九班到操场集合");

        M_HardWareSendMessageVO result = service.sendMessage(request);

        assertThat(result.getMessageId()).isEqualTo(42);
        assertThat(result.getDeviceName()).isEqualTo("oledScreen1");
        assertThat(result.getTopic())
                .isEqualTo("flyfish/hardware/oledScreen1/notification");

        ArgumentCaptor<String> topic = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> payload = ArgumentCaptor.forClass(String.class);
        verify(mqttPublisher).publish(topic.capture(), payload.capture());
        assertThat(topic.getValue())
                .isEqualTo("flyfish/hardware/oledScreen1/notification");
        assertThat(payload.getValue())
                .contains("\"version\":42")
                .contains("\"school\":\"附小\"")
                .contains("\"grade\":\"四年级\"")
                .contains("\"className\":9")
                .contains("\"userName\":\"唐佳\"")
                .contains("\"superVisor\":\"李老师\"")
                .doesNotContain("senderName")
                .contains("请四年级九班到操场集合");
    }

    @Test
    void pollsCurrentReadStateAndDeviceReplies() {
        when(deviceMapper.findById(1)).thenReturn(Optional.of(device()));
        M_HardWareMessage outgoing = new M_HardWareMessage(
                10, "于文字", "oledScreen1", "notification", "请确认",
                true, LocalDateTime.of(2026, 8, 13, 12, 0),
                null, "toHardware");
        M_HardWareMessage reply = new M_HardWareMessage(
                11, "于文字", "oledScreen1", "buttonReply", "好的",
                false, LocalDateTime.of(2026, 8, 13, 12, 1),
                "eventId=x;replyTo=10", "toSoftware");
        when(messageMapper.findRecentDeviceMessages("oledScreen1", 50))
                .thenReturn(List.of(reply, outgoing));

        M_HardWareMessagePollVO result = service.pollMessages(1, 0, 50);

        assertThat(result.getLatestId()).isEqualTo(11);
        assertThat(result.getMessages()).hasSize(2);
        assertThat(result.getMessages().get(0).getMessageContent()).isEqualTo("请确认");
        assertThat(result.getMessages().get(0).isMessageRead()).isTrue();
        assertThat(result.getMessages().get(1).getMessageContent()).isEqualTo("好的");
        assertThat(result.getMessages().get(1).getSuperVisor()).isEqualTo("于文字");
        assertThat(result.getMessages().get(1).getDirection()).isEqualTo("toSoftware");
    }

    private M_HardwareDevice device() {
        return new M_HardwareDevice(
                1, "附小", "四年级", 9,
                "oledScreen", 1, "唐佳", "班级通知", "于文字");
    }
}
