package flyfish.service;

import flyfish.pojo.AccumulateScore;
import flyfish.pojo.DTO.ScoreDTO;
import flyfish.pojo.VO.AccumulateScoreVO;
import flyfish.pojo.VO.M_StudentNamePerformByDateVO;

import java.time.LocalDate;
import java.util.List;

public interface AccumulateScoreService {
    /**
     * 检查是否存在这个分数表
     * @param classNumber
     * @param nameList
     */
    void getNameClass(String classNumber, List<String> nameList,String subject,String school);

    /**
     * 加分或者减分操作
     * @param addnumber
     * @param classNumber
     * @param nameList
     * @param subject
     */
    void updatescore(Integer addnumber, String classNumber, List<String> nameList, String subject,String school);

    /**
     * 查询分数
     *
     * @param name
     * @param classNumber
     * @param subject
     * @return
     */
    AccumulateScore getByNameClass(String name, String classNumber, String subject);

    /**
     * 三种方式更新分数
     * @param scoreDTO
     * @param nameList
     * @return
     */
    List<AccumulateScoreVO> threeTypescore(ScoreDTO scoreDTO, List<String> nameList);


    /**
     * 加分特殊操作
     * @param scoreDTO
     * @param nameList
     * @param addscoreList
     * @return
     */
    List<AccumulateScoreVO> addscorespecial(ScoreDTO scoreDTO, List<String> nameList, List<Integer> addscoreList);

    List<AccumulateScoreVO> queryAllScore(String className, String school, String subject);

    /**
     * 查询分榜榜单
     * @param className
     * @param school
     * @param subject
     * @param startDate
     * @param endDate
     * @param type
     * @return
     */
    List<AccumulateScoreVO> queryPartScore(String className, String school, String subject, LocalDate startDate, LocalDate endDate, String type);

    /**
     * 查询某个学生的表现情况
     * @param name
     * @param school
     * @param subject
     * @param startDate
     * @param endDate
     * @param className
     * @return
     */
    List<M_StudentNamePerformByDateVO> queryScoreByName(String name, String school, String subject, LocalDate startDate, LocalDate endDate, String className);

    /**
     * 新增积分数据通过扫码方式
     * @param scoreDTO
     * @return
     */
    String addScoreByScanner(ScoreDTO scoreDTO);
}
