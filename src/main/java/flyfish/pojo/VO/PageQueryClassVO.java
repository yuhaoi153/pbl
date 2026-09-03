package flyfish.pojo.VO;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageQueryClassVO {
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate checkdate;
    private String subject;
    private String content;
    private String pageAndSupplementary;
    private List<String> uncompletelist;
    private List<String> welllist;
    private List<String> badlist;
    private List<String> revisionListGeneral;
    private List<String> revisionListFail;
    private String supplementary;



}
