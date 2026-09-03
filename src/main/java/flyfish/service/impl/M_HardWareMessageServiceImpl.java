package flyfish.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import flyfish.exception.M_HardWareOledApiException;
import flyfish.mapper.M_HardWareDeviceMapper;
import flyfish.mapper.M_HardWareMessageMapper;
import flyfish.pojo.DTO.M_HardWareSendMessageDTO;
import flyfish.pojo.M_HardWareMessage;
import flyfish.pojo.M_HardWareMqttPayload;
import flyfish.pojo.M_HardwareDevice;
import flyfish.pojo.VO.M_HardWareDeviceVO;
import flyfish.pojo.VO.M_HardWareSendMessageVO;
import flyfish.pojo.VO.M_HardWareMessagePollVO;
import flyfish.pojo.VO.M_HardWareMessageVO;
import flyfish.properties.M_HardWareMqttProperties;
import flyfish.service.M_HardWareMessageService;
import flyfish.service.M_HardWareMqttPublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

@Service
public class M_HardWareMessageServiceImpl implements M_HardWareMessageService {
    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final M_HardWareDeviceMapper deviceMapper;
    private final M_HardWareMessageMapper messageMapper;
    private final M_HardWareMqttPublisher mqttPublisher;
    private final M_HardWareMqttProperties mqttProperties;
    private final ObjectMapper objectMapper;

    public M_HardWareMessageServiceImpl(
            M_HardWareDeviceMapper deviceMapper,
            M_HardWareMessageMapper messageMapper,
            M_HardWareMqttPublisher mqttPublisher,
            M_HardWareMqttProperties mqttProperties,
            ObjectMapper objectMapper) {
        this.deviceMapper = deviceMapper;
        this.messageMapper = messageMapper;
        this.mqttPublisher = mqttPublisher;
        this.mqttProperties = mqttProperties;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<M_HardWareDeviceVO> findDevices(
            String school, String grade, Integer className, String teacherName) {
        return deviceMapper.findByClassAndSupervisor(
                        school.trim(), grade.trim(), className, teacherName.trim())
                .stream()
                .map(this::toDeviceVO)
                .toList();
    }

    @Override
    public M_HardWareSendMessageVO sendMessage(M_HardWareSendMessageDTO request) {
        M_HardwareDevice device = deviceMapper.findById(request.getDeviceId())
                .orElseThrow(() -> M_HardWareOledApiException.notFound(
                        "没有找到 deviceId=" + request.getDeviceId() + " 的硬件设备"));

        String deviceName = buildDeviceName(device);
        LocalDateTime sentTime = LocalDateTime.now();
        M_HardWareMessage storedMessage = new M_HardWareMessage(
                null,
                request.getSuperVisor().trim(),
                deviceName,
                request.getMessageType().trim(),
                request.getMessageContent().trim(),
                false,
                sentTime,
                normalizeOptional(request.getSupplementary()),
                "toHardware");

        int inserted = messageMapper.insert(storedMessage);
        if (inserted != 1 || storedMessage.getId() == null) {
            throw new M_HardWareOledApiException(
                    org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
                    "硬件消息保存失败");
        }

        String topic = buildTopic(deviceName);
        M_HardWareMqttPayload mqttPayload = new M_HardWareMqttPayload(
                storedMessage.getId(),
                storedMessage.getId(),
                deviceName,
                device.getSchool(),
                device.getGrade(),
                device.getClassName(),
                device.getUserName(),
                storedMessage.getSuperVisor(),
                storedMessage.getMessageType(),
                storedMessage.getMessageContent(),
                storedMessage.getSupplementary(),
                sentTime.format(TIME_FORMATTER));

        try {
            mqttPublisher.publish(topic, objectMapper.writeValueAsString(mqttPayload));
        } catch (JsonProcessingException exception) {
            throw new M_HardWareOledApiException(
                    org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
                    "硬件消息 JSON 生成失败");
        }

        return new M_HardWareSendMessageVO(
                storedMessage.getId(),
                deviceName,
                topic,
                storedMessage.getSuperVisor(),
                storedMessage.getMessageType(),
                storedMessage.getMessageContent(),
                sentTime.format(TIME_FORMATTER),
                true);
    }

    @Override
    public M_HardWareMessagePollVO pollMessages(
            Integer deviceId, Integer afterId, Integer limit) {
        M_HardwareDevice device = deviceMapper.findById(deviceId)
                .orElseThrow(() -> M_HardWareOledApiException.notFound(
                        "没有找到 deviceId=" + deviceId + " 的硬件设备"));
        String deviceName = buildDeviceName(device);
        List<M_HardWareMessage> recent = new ArrayList<>(
                messageMapper.findRecentDeviceMessages(deviceName, limit));
        Collections.reverse(recent);
        List<M_HardWareMessageVO> messages = recent
                .stream()
                .map(this::toMessageVO)
                .toList();
        int latestId = messages.isEmpty()
                ? afterId
                : messages.get(messages.size() - 1).getId();
        return new M_HardWareMessagePollVO(
                deviceId, deviceName, latestId, messages);
    }

    private M_HardWareMessageVO toMessageVO(M_HardWareMessage message) {
        return new M_HardWareMessageVO(
                message.getId(),
                message.getSuperVisor(),
                message.getDeviceName(),
                message.getMessageType(),
                message.getMessageContent(),
                Boolean.TRUE.equals(message.getMessageRead()),
                message.getSentTime().format(TIME_FORMATTER),
                message.getSupplementary(),
                message.getDirection());
    }

    private M_HardWareDeviceVO toDeviceVO(M_HardwareDevice device) {
        return new M_HardWareDeviceVO(
                device.getId(),
                device.getSchool(),
                device.getGrade(),
                device.getClassName(),
                device.getUserName(),
                device.getDeviceType(),
                device.getDeviceNum(),
                buildDeviceName(device),
                device.getPurpose(),
                device.getSuperVisor());
    }

    private String buildDeviceName(M_HardwareDevice device) {
        if (device.getDeviceType() == null ||
                !device.getDeviceType().matches("[A-Za-z0-9_-]+") ||
                device.getDeviceNum() == null || device.getDeviceNum() < 0) {
            throw M_HardWareOledApiException.badRequest(
                    "设备的 deviceType/deviceNum 配置不合法");
        }
        return device.getDeviceType() + device.getDeviceNum();
    }

    private String buildTopic(String deviceName) {
        String prefix = mqttProperties.getTopicPrefix();
        while (prefix.endsWith("/")) {
            prefix = prefix.substring(0, prefix.length() - 1);
        }
        return prefix + "/" + deviceName + "/notification";
    }

    private String normalizeOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
