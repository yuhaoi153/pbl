package flyfish.pojo.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NoticeDTO {
    private String chineseEmail;
    @JsonProperty("CES")
    private boolean CES;
    private String chinesePhone;
    @JsonProperty("CPS")
    private boolean CPS;
    private String mathEmail;
    @JsonProperty("MES")
    private boolean MES;
    private String mathPhone;
    @JsonProperty("MPS")
    private boolean MPS;
    private String englishEmail;
    @JsonProperty("EES")
    private boolean EES;
    private String englishPhone;
    @JsonProperty("EPS")
    private boolean EPS;
    private String username;
    private String school;

}
