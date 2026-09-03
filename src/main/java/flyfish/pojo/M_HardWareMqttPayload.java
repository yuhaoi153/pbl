package flyfish.pojo;

public record M_HardWareMqttPayload(
        Integer messageId,
        Integer version,
        String deviceName,
        String school,
        String grade,
        Integer className,
        String userName,
        String superVisor,
        String messageType,
        String messageContent,
        String supplementary,
        String sentTime) {
}
