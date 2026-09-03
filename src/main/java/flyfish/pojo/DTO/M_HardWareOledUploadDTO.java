package flyfish.pojo.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class M_HardWareOledUploadDTO {
    @NotBlank
    @Size(max = 64)
    @Pattern(regexp = "[A-Za-z0-9._:-]+")
    private String deviceId;

    @NotBlank
    private String message;

    @Size(max = 64)
    @Pattern(regexp = "[A-Za-z0-9._:-]+")
    private String requestId;
}
