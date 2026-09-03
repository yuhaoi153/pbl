package flyfish.contoller;

import flyfish.exception.M_HardWareOledApiException;
import flyfish.pojo.DTO.M_HardWareOledUploadDTO;
import flyfish.pojo.VO.M_HardWareOledNotificationVO;
import flyfish.pojo.VO.M_HardWareOledUploadResultVO;
import flyfish.service.M_HardWareOledService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@Validated
@RestController
@RequestMapping(path = "/mpi/oled", produces = MediaType.APPLICATION_JSON_VALUE)
public class M_HardWareOledController {
    public static final String VERSION_HEADER = "X-Notification-Version";

    private final M_HardWareOledService oledService;

    public M_HardWareOledController(M_HardWareOledService oledService) {
        this.oledService = oledService;
    }

    @PostMapping(
            path = {"/uploadNotificaiton", "/uploadNotification"},
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<M_HardWareOledUploadResultVO> uploadNotification(
            @Valid @RequestBody M_HardWareOledUploadDTO request) {
        M_HardWareOledUploadResultVO result =
                oledService.uploadNotification(request);

        HttpStatus status = result.isDuplicate() ? HttpStatus.OK : HttpStatus.CREATED;
        return ResponseEntity.status(status)
                .cacheControl(CacheControl.noStore())
                .header(VERSION_HEADER, Long.toString(result.getVersion()))
                .body(result);
    }

    @GetMapping({"/latest", "/latestNotification"})
    public ResponseEntity<M_HardWareOledNotificationVO> getLatestNotification(
            @RequestParam
            @Size(min = 1, max = 64)
            @Pattern(regexp = "[A-Za-z0-9._:-]+")
            String deviceId,
            @RequestParam(required = false) @PositiveOrZero Long lastVersion,
            @RequestParam(required = false) @PositiveOrZero Long afterVersion) {
        Long knownVersion = resolveKnownVersion(lastVersion, afterVersion);
        Optional<M_HardWareOledNotificationVO> latest =
                oledService.getLatestNotification(deviceId, knownVersion);

        if (latest.isEmpty()) {
            return ResponseEntity.noContent()
                    .cacheControl(CacheControl.noStore())
                    .build();
        }

        M_HardWareOledNotificationVO response = latest.get();
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .header(VERSION_HEADER, Long.toString(response.getVersion()))
                .body(response);
    }

    private Long resolveKnownVersion(Long lastVersion, Long afterVersion) {
        if (lastVersion != null && afterVersion != null &&
                !lastVersion.equals(afterVersion)) {
            throw M_HardWareOledApiException.badRequest(
                    "lastVersion 与兼容参数 afterVersion 不能同时提供不同的值");
        }
        return lastVersion != null ? lastVersion : afterVersion;
    }
}
