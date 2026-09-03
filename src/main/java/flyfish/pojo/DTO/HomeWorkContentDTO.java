package flyfish.pojo.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HomeWorkContentDTO {
    private String content;
    private String classNumber;
    private String subject;
    private String type;
    private String supplementary;
    private List<String> imageurl;
    private String school;
    private Integer year;
}
