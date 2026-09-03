package flyfish.service;

import flyfish.pojo.M_WellBadHomeworkPerform;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;


public interface M_WellBadPerformService {
    List<M_WellBadHomeworkPerform> getShowImage( String school, String className, String content, String subject, String studentName, LocalDate startDate, LocalDate endDate) throws IOException;

    String uploadHomeWorkImage(MultipartFile file,String grade, String situation, String school, String className, String content, String subject, String studentName, LocalDate checkDate) throws IOException;

    String deleteShowImage(Integer id);

    List<M_WellBadHomeworkPerform> getPunishItemRecord(String school, String className, String subject, LocalDate startDate, LocalDate endDate,String showItem);

    String uploadpunishItemRecord(M_WellBadHomeworkPerform mWellBadHomeworkPerform);

    String cancelPunishItemRecord(Integer id);
}
