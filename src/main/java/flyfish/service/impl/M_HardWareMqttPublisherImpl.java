package flyfish.service.impl;

import flyfish.exception.M_HardWareOledApiException;
import flyfish.properties.M_HardWareMqttProperties;
import flyfish.service.M_HardWareMqttPublisher;
import jakarta.annotation.PreDestroy;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Service
public class M_HardWareMqttPublisherImpl implements M_HardWareMqttPublisher {
    private final M_HardWareMqttProperties properties;
    private final MqttAsyncClient client;

    public M_HardWareMqttPublisherImpl(M_HardWareMqttProperties properties)
            throws MqttException {
        this.properties = properties;
        String clientId = properties.getPublisherClientId() + "-" +
                UUID.randomUUID().toString().substring(0, 8);
        this.client = new MqttAsyncClient(
                properties.getBrokerUri(), clientId, new MemoryPersistence());
    }

    @Override
    public synchronized void publish(String topic, String payload) {
        publish(topic, payload, properties.isRetained());
    }

    @Override
    public synchronized void publish(String topic, String payload, boolean retained) {
        try {
            ensureConnected();
            MqttMessage mqttMessage = new MqttMessage(
                    payload.getBytes(StandardCharsets.UTF_8));
            mqttMessage.setQos(properties.getQos());
            mqttMessage.setRetained(retained);
            client.publish(topic, mqttMessage).waitForCompletion(5000);
        } catch (MqttException exception) {
            throw new M_HardWareOledApiException(
                    org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE,
                    "MQTT 消息发布失败，请稍后重试");
        }
    }

    private void ensureConnected() throws MqttException {
        if (client.isConnected()) {
            return;
        }

        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);
        options.setConnectionTimeout(10);
        options.setKeepAliveInterval(30);
        client.connect(options).waitForCompletion(12000);
    }

    @PreDestroy
    public void close() {
        try {
            if (client.isConnected()) {
                client.disconnect().waitForCompletion(3000);
            }
            client.close();
        } catch (MqttException ignored) {
            // Application shutdown must continue even when the broker is unavailable.
        }
    }
}
