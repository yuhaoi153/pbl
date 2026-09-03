package flyfish.service;

import java.time.LocalDate;

public interface M_NotificationService {
    String getNotificationBySchoolUserName(String school, String username, LocalDate startDate, LocalDate endDate);

    String getNotificationForStudentBySchoolUserName(String school, String username, LocalDate startDate, LocalDate endDate,String studentClassName);
}
