package flyfish.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import flyfish.mapper.M_HardWareDeviceMapper;
import flyfish.mapper.M_HardWareMessageMapper;
import flyfish.pojo.M_HardWareMessage;
import flyfish.pojo.M_HardwareDevice;
import flyfish.properties.M_HardWareMqttProperties;
import flyfish.service.M_HardWareMqttPublisher;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class M_HardWareMqttEventListenerTest {
    private M_HardWareMqttEventListener listener;

    @AfterEach
    void closeListener() {
        if (listener != null) listener.close();
    }

    @Test
    void buttonReplyUsesSupervisor() throws Exception {
        M_HardWareDeviceMapper deviceMapper = mock(M_HardWareDeviceMapper.class);
        M_HardWareMessageMapper messageMapper = mock(M_HardWareMessageMapper.class);
        M_HardWareMqttProperties properties = new M_HardWareMqttProperties();
        properties.setBrokerUri("tcp://127.0.0.1:1883");

        M_HardwareDevice device = new M_HardwareDevice(
                1, "附小", "四年级", 9,
                "oledScreen", 1, "唐佳", "班级通知", "于文字");
        when(deviceMapper.findByDeviceName("oledScreen1"))
                .thenReturn(Optional.of(device));
        when(messageMapper.countReplyByEvent(anyString(), anyString()))
                .thenReturn(0);

        ObjectMapper objectMapper = new ObjectMapper();
        listener = new M_HardWareMqttEventListener(
                properties,
                deviceMapper,
                messageMapper,
                mock(M_HardWareMqttPublisher.class),
                objectMapper);

        listener.storeReply(
                "oledScreen1",
                42,
                objectMapper.readTree("{\"reply\":\"好的\",\"eventId\":\"evt-1\"}"));

        ArgumentCaptor<M_HardWareMessage> messageCaptor =
                ArgumentCaptor.forClass(M_HardWareMessage.class);
        verify(messageMapper).insert(messageCaptor.capture());
        assertThat(messageCaptor.getValue().getSuperVisor()).isEqualTo("于文字");
        assertThat(messageCaptor.getValue().getDirection()).isEqualTo("toSoftware");
    }
}
