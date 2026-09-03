package flyfish.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ImageUrl {
    private MultipartFile image;
    private Integer id;
    private String url;
    private LocalDate checkDate;
    private LocalDateTime createTime;
}
