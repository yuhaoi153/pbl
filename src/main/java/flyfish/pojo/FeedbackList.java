package flyfish.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackList {

    private List<String> classNames;
    private List<String> teacherNames;
    private LocalDate checkdate;
    private  String censor;
    private String timeZone;
}
