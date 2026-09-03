package flyfish.service;

public interface M_HardWareMqttPublisher {
    void publish(String topic, String payload);

    void publish(String topic, String payload, boolean retained);
}
