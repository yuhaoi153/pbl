package flyfish.pojo.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeleteContetnDTO {
    private String subject;
    private String content;
    private List<String> contentList;
    private String classNumber;
    private String school;
    private String type;

}
