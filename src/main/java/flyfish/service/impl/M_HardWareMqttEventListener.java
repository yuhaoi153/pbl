package flyfish.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import flyfish.mapper.M_HardWareDeviceMapper;
import flyfish.mapper.M_HardWareMessageMapper;
import flyfish.pojo.M_HardWareMessage;
import flyfish.pojo.M_HardWareMqttPayload;
import flyfish.pojo.M_HardwareDevice;
import flyfish.properties.M_HardWareMqttProperties;
import flyfish.service.M_HardWareMqttPublisher;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class M_HardWareMqttEventListener {
    private static final Logger log =
            LoggerFactory.getLogger(M_HardWareMqttEventListener.class);
    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final M_HardWareMqttProperties properties;
    private final M_HardWareDeviceMapper deviceMapper;
    private final M_HardWareMessageMapper messageMapper;
    private final M_HardWareMqttPublisher mqttPublisher;
    private final ObjectMapper objectMapper;
    private final ScheduledExecutorService reconnectExecutor =
            Executors.newSingleThreadScheduledExecutor(runnable -> {
                Thread thread = new Thread(runnable, "hardware-mqtt-listener");
                thread.setDaemon(true);
                return thread;
            });
    private MqttAsyncClient client;

    public M_HardWareMqttEventListener(
            M_HardWareMqttProperties properties,
            M_HardWareDeviceMapper deviceMapper,
            M_HardWareMessageMapper messageMapper,
            M_HardWareMqttPublisher mqttPublisher,
            ObjectMapper objectMapper) {
        this.properties = properties;
        this.deviceMapper = deviceMapper;
        this.messageMapper = messageMapper;
        this.mqttPublisher = mqttPublisher;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void start() {
        reconnectExecutor.scheduleWithFixedDelay(
                this::connectIfNecessary, 0, 10, TimeUnit.SECONDS);
    }

    private synchronized void connectIfNecessary() {
        try {
            if (client == null) {
                String id = properties.getPublisherClientId() + "-events-" +
                        UUID.randomUUID().toString().substring(0, 8);
                client = new MqttAsyncClient(
                        properties.getBrokerUri(), id, new MemoryPersistence());
                client.setCallback(new MqttCallbackExtended() {
                    @Override
                    public void connectComplete(boolean reconnect, String serverURI) {
                        subscribeToDeviceEvents();
                    }

                    @Override
                    public void connectionLost(Throwable cause) {
                        log.warn("硬件 MQTT 事件连接断开: {}",
                                cause == null ? "unknown" : cause.getMessage());
                    }

                    @Override
                    public void messageArrived(String topic, MqttMessage message) {
                        handleEvent(topic, new String(
                                message.getPayload(), StandardCharsets.UTF_8));
                    }

                    @Override
                    public void deliveryComplete(IMqttDeliveryToken token) {
                    }
                });
            }
            if (client.isConnected()) return;

            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);
            options.setConnectionTimeout(10);
            options.setKeepAliveInterval(30);
            client.connect(options).waitForCompletion(12000);
        } catch (Exception exception) {
            log.warn("连接硬件 MQTT 事件 Topic 失败，将自动重试: {}",
                    exception.getMessage());
        }
    }

    private void subscribeToDeviceEvents() {
        try {
            client.subscribe(normalizedPrefix() + "/+/event", 1)
                    .waitForCompletion(5000);
            log.info("已订阅硬件事件 Topic: {}/+/event", normalizedPrefix());
        } catch (MqttException exception) {
            log.error("订阅硬件事件 Topic 失败", exception);
        }
    }

    private void handleEvent(String topic, String json) {
        try {
            String deviceName = extractDeviceName(topic);
            JsonNode event = objectMapper.readTree(json);
            if (!deviceName.equals(event.path("deviceName").asText())) {
                log.warn("忽略 deviceName 与 Topic 不匹配的硬件事件");
                return;
            }

            String action = event.path("action").asText();
            int currentMessageId = event.path("messageId").asInt(0);
            switch (action) {
                case "read" -> markRead(deviceName, currentMessageId);
                case "reply" -> storeReply(deviceName, currentMessageId, event);
                case "previous", "next" ->
                        sendHistory(deviceName, currentMessageId, action);
                default -> log.warn("未知硬件事件 action={}", action);
            }
        } catch (Exception exception) {
            log.error("处理硬件 MQTT 事件失败: topic={}, payload={}",
                    topic, json, exception);
        }
    }

    private void markRead(String deviceName, int messageId) {
        if (messageId > 0) messageMapper.markRead(messageId, deviceName);
    }

    void storeReply(String deviceName, int messageId, JsonNode event) {
        String reply = event.path("reply").asText();
        if (!"好的".equals(reply) && !"哒咩".equals(reply)) {
            log.warn("忽略不允许的按键回复: {}", reply);
            return;
        }

        String eventId = event.path("eventId").asText();
        if (eventId.isBlank()) return;
        String supplementary = "eventId=" + eventId + ";replyTo=" + messageId;
        if (messageMapper.countReplyByEvent(deviceName, supplementary) > 0) return;

        M_HardwareDevice device = deviceMapper.findByDeviceName(deviceName)
                .orElseThrow(() -> new IllegalArgumentException("设备不存在"));
        String teacherName = device.getSuperVisor();
        if (teacherName == null || teacherName.isBlank()) {
            throw new IllegalStateException("设备未配置 superVisor/teacherName");
        }
        M_HardWareMessage response = new M_HardWareMessage(
                null,
                teacherName.trim(),
                deviceName,
                "buttonReply",
                reply,
                false,
                LocalDateTime.now(),
                supplementary,
                "toSoftware");
        messageMapper.insert(response);
        markRead(deviceName, messageId);
        log.info("已保存设备按键回复: device={}, reply={}, replyTo={}",
                deviceName, reply, messageId);
    }

    private void sendHistory(
            String deviceName, int currentMessageId, String action) throws Exception {
        Optional<M_HardWareMessage> target;
        if (currentMessageId <= 0) {
            target = messageMapper.findLatestHardwareMessage(deviceName);
        } else if ("previous".equals(action)) {
            target = messageMapper.findPreviousHardwareMessage(
                    deviceName, currentMessageId);
        } else {
            target = messageMapper.findNextHardwareMessage(
                    deviceName, currentMessageId);
        }

        String historyTopic = normalizedPrefix() + "/" +
                deviceName + "/history";
        if (target.isEmpty()) {
            String payload = objectMapper.writeValueAsString(
                    java.util.Map.of("found", false, "action", action));
            mqttPublisher.publish(historyTopic, payload, false);
            return;
        }

        M_HardWareMessage message = target.get();
        M_HardwareDevice device = deviceMapper.findByDeviceName(deviceName)
                .orElseThrow(() -> new IllegalArgumentException("设备不存在"));
        M_HardWareMqttPayload payload = new M_HardWareMqttPayload(
                message.getId(), message.getId(), deviceName,
                device.getSchool(), device.getGrade(), device.getClassName(),
                device.getUserName(), message.getSuperVisor(),
                message.getMessageType(), message.getMessageContent(),
                message.getSupplementary(),
                message.getSentTime().format(TIME_FORMATTER));
        messageMapper.markRead(message.getId(), deviceName);
        mqttPublisher.publish(
                historyTopic, objectMapper.writeValueAsString(payload), false);
    }

    private String extractDeviceName(String topic) {
        String prefix = normalizedPrefix() + "/";
        String suffix = "/event";
        if (!topic.startsWith(prefix) || !topic.endsWith(suffix)) {
            throw new IllegalArgumentException("非法硬件事件 Topic");
        }
        String deviceName = topic.substring(
                prefix.length(), topic.length() - suffix.length());
        if (!deviceName.matches("[A-Za-z0-9_-]+")) {
            throw new IllegalArgumentException("非法 deviceName");
        }
        return deviceName;
    }

    private String normalizedPrefix() {
        String prefix = properties.getTopicPrefix();
        while (prefix.endsWith("/")) {
            prefix = prefix.substring(0, prefix.length() - 1);
        }
        return prefix;
    }

    @PreDestroy
    public void close() {
        reconnectExecutor.shutdownNow();
        if (client == null) return;
        try {
            if (client.isConnected()) {
                client.disconnect().waitForCompletion(3000);
            }
            client.close();
        } catch (MqttException ignored) {
        }
    }
}
