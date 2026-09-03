package flyfish.pojo.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class M_HardWareSendMessageDTO {
    @NotNull
    @Positive
    private Integer deviceId;

    @NotBlank
    @Size(max = 100)
    private String superVisor;

    @NotBlank
    @Size(max = 100)
    private String messageType;

    @NotBlank
    @Size(max = 1024)
    private String messageContent;

    @Size(max = 500)
    private String supplementary;
}
