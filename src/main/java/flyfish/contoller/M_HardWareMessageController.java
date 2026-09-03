package flyfish.contoller;

import flyfish.pojo.DTO.M_HardWareSendMessageDTO;
import flyfish.pojo.VO.M_HardWareDeviceVO;
import flyfish.pojo.VO.M_HardWareSendMessageVO;
import flyfish.pojo.VO.M_HardWareMessagePollVO;
import flyfish.service.M_HardWareMessageService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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

import java.util.List;

@Validated
@RestController
@RequestMapping(path = "/mpi/hardware", produces = MediaType.APPLICATION_JSON_VALUE)
public class M_HardWareMessageController {
    private final M_HardWareMessageService messageService;

    public M_HardWareMessageController(M_HardWareMessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping("/devices")
    public List<M_HardWareDeviceVO> findDevices(
            @RequestParam @NotBlank @Size(max = 200) String school,
            @RequestParam @NotBlank @Size(max = 100) String grade,
            @RequestParam @Min(1) @Max(99) Integer className,
            @RequestParam @NotBlank @Size(max = 100) String teacherName) {
        return messageService.findDevices(school, grade, className, teacherName);
    }

    @PostMapping(path = "/messages", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<M_HardWareSendMessageVO> sendMessage(
            @Valid @RequestBody M_HardWareSendMessageDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(messageService.sendMessage(request));
    }

    @GetMapping("/messages/status")
    public M_HardWareMessagePollVO pollMessages(
            @RequestParam @Min(1) Integer deviceId,
            @RequestParam(defaultValue = "0") @Min(0) Integer afterId,
            @RequestParam(defaultValue = "50") @Min(1) @Max(100) Integer limit) {
        return messageService.pollMessages(deviceId, afterId, limit);
    }
}
