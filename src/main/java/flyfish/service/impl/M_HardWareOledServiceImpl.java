package flyfish.service.impl;

import flyfish.exception.M_HardWareOledApiException;
import flyfish.pojo.DTO.M_HardWareOledUploadDTO;
import flyfish.pojo.M_HardWareOledNotification;
import flyfish.pojo.VO.M_HardWareOledNotificationVO;
import flyfish.pojo.VO.M_HardWareOledUploadResultVO;
import flyfish.properties.M_HardWareOledProperties;
import flyfish.service.M_HardWareOledService;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class M_HardWareOledServiceImpl implements M_HardWareOledService {
    private final M_HardWareOledProperties properties;
    private final ConcurrentMap<String, M_HardWareOledNotification> latestByDevice =
            new ConcurrentHashMap<>();
    private final AtomicLong versionSequence = new AtomicLong(System.currentTimeMillis());

    public M_HardWareOledServiceImpl(M_HardWareOledProperties properties) {
        this.properties = properties;
    }

    @Override
    public M_HardWareOledUploadResultVO uploadNotification(
            M_HardWareOledUploadDTO request) {
        validateMessage(request.getMessage());

        String requestId = normalizeOptional(request.getRequestId());
        AtomicReference<M_HardWareOledUploadResultVO> result = new AtomicReference<>();

        latestByDevice.compute(request.getDeviceId(), (deviceId, current) -> {
            if (requestId != null && current != null && requestId.equals(current.getRequestId())) {
                if (!request.getMessage().equals(current.getMessage())) {
                    throw M_HardWareOledApiException.conflict(
                            "requestId 已用于不同的消息；请为新消息生成新的 requestId");
                }
                result.set(toUploadResult(current, true));
                return current;
            }

            M_HardWareOledNotification next = new M_HardWareOledNotification(
                    deviceId,
                    nextVersion(),
                    request.getMessage(),
                    requestId,
                    Instant.now());
            result.set(toUploadResult(next, false));
            return next;
        });

        return result.get();
    }

    @Override
    public Optional<M_HardWareOledNotificationVO> getLatestNotification(
            String deviceId,
            Long knownVersion) {
        M_HardWareOledNotification latest = latestByDevice.get(deviceId);
        if (latest == null ||
                (knownVersion != null && knownVersion.longValue() >= latest.getVersion())) {
            return Optional.empty();
        }

        return Optional.of(new M_HardWareOledNotificationVO(
                latest.getDeviceId(),
                latest.getVersion(),
                latest.getMessage(),
                latest.getCreatedAt()));
    }

    private M_HardWareOledUploadResultVO toUploadResult(
            M_HardWareOledNotification notification,
            boolean duplicate) {
        return new M_HardWareOledUploadResultVO(
                notification.getDeviceId(),
                notification.getVersion(),
                notification.getMessage(),
                notification.getRequestId(),
                notification.getCreatedAt(),
                duplicate);
    }

    private void validateMessage(String message) {
        int codePoints = message.codePointCount(0, message.length());
        if (codePoints > properties.getMaxMessageCodePoints()) {
            throw M_HardWareOledApiException.badRequest(
                    "message 最多允许 " + properties.getMaxMessageCodePoints() +
                            " 个 Unicode 字符");
        }

        int utf8Bytes = message.getBytes(StandardCharsets.UTF_8).length;
        if (utf8Bytes > properties.getMaxMessageUtf8Bytes()) {
            throw M_HardWareOledApiException.badRequest(
                    "message 的 UTF-8 编码最多允许 " +
                            properties.getMaxMessageUtf8Bytes() + " 字节");
        }

        message.codePoints().forEach(codePoint -> {
            boolean permittedWhitespace =
                    codePoint == '\n' || codePoint == '\r' || codePoint == '\t';
            if (Character.isISOControl(codePoint) && !permittedWhitespace) {
                throw M_HardWareOledApiException.badRequest(
                        "message 含有不支持的控制字符");
            }
        });
    }

    private String normalizeOptional(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private long nextVersion() {
        return versionSequence.updateAndGet(previous ->
                Math.max(previous + 1, System.currentTimeMillis()));
    }
}
