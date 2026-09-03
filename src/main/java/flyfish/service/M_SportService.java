package flyfish.service;

import flyfish.pojo.DTO.M_ReadGradeFeedDTO;
import flyfish.pojo.DTO.M_SportFourSituationDTO;

import java.time.LocalDate;

public interface M_SportService {
    /**
     * 记录体育成绩
     * @param mReadGradeFeedDTO
     */
    String recordSport(M_ReadGradeFeedDTO mReadGradeFeedDTO);


    M_SportFourSituationDTO getSingleSportFeedback(String school, LocalDate checkDate);

    String getFeedbackReport(String school, LocalDate checkDate);
}
