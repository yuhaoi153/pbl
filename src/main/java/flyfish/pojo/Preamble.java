package flyfish.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Preamble {
    private String clarification;
    private String teacherGuide;
    private String selfGuide;
    private String preschoolInspection;
}
