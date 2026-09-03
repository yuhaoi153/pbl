package flyfish.pojo.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageQueryClassDTO {
    private Integer currentPage;
    private Integer pageSize;
    private LocalDate startdate;
    private LocalDate enddate;
    private String subject;
    private String content;
    private String classNumber;
    private String school;
    private String studentName;
}
