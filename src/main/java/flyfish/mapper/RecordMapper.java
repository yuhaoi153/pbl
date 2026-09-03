package flyfish.mapper;

import flyfish.pojo.AccumulateScore;
import flyfish.pojo.DTO.RecordQueryDTO;
import flyfish.pojo.Record;
import flyfish.pojo.VO.RecordVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


@Mapper
public interface RecordMapper {

        /**
         * 初始化判断是否存在
         *
         * @param classNumber
         * @param content
         * @param checkdate
         * @return
         */
        @Select("select * from homework.record where class_number =#{classNumber} and content = #{content} and checkdate = #{checkdate} and subject =#{subject} and school = #{school}")
        List<Record> isexist(String classNumber, String content, LocalDate checkdate, String subject,String school);

        /**
         * 批量上传数据
         * @param originrecordList
         */
        void batchupload(List<Record> originrecordList);

        /**
         * 批量更新完成状态
         * @param completed
         * @param nowtime
         * @param content
         * @param classNumber
         * @param checkdate
         * @param studentNumberList
         * @param nameList
         */
        void updatecompleted
        (Integer completed, LocalDateTime nowtime, String content, String classNumber, LocalDate checkdate, List<String> studentNumberList, List<String> nameList, String subject, Integer level,String school);


        /**
         * 查询指定班级指定作业未完成的同学
         * @param completed
         * @param content
         * @param classNumber
         * @param checkdate
         * @return
         */
        @Select("select name from homework.record where class_number = #{classNumber} and school = #{school} and content = #{content} and checkdate = #{checkdate} and subject = #{subject} and completed = 0")
        List<String> getuncompleted(Integer completed, String content, String classNumber, LocalDate checkdate, String subject, String school);


        /**
         * 获取当前班级今天所有未完成作业的同学
         * @param uncompleted
         * @param classNumber
         * @param checkdate
         * @param subject
         * @return
         */
        List<Record> getalluncompleted(Integer uncompleted,String classNumber, LocalDate checkdate, String subject, String school);

        /**
         * 改造已经完成初始化的数据
         * @param changecompleted
         * @param nowtime
         * @param content
         * @param classNumber
         * @param checkdate
         */
        void updatenull(Integer changecompleted, LocalDateTime nowtime, String content, String classNumber, LocalDate checkdate,String subject, String school);

        /**
         * 更新作业等级
         * @param completed
         * @param nowtime
         * @param content
         * @param classNumber
         * @param checkdate
         * @param level1
         * @param studentNumberList
         * @param nameList
         */
        void updatelevel(Integer completed, LocalDateTime nowtime, String content, String classNumber, LocalDate checkdate, Integer level1, List<String> studentNumberList, List<String> nameList,boolean revision, String subject,String school);

        /**
         * 获得等级作业反馈
         *
         * @param level
         * @param content
         * @param classNumber
         * @param checkdate
         * @return
         */

        List<String> getlevelfeedback(Integer level, String content, String classNumber, LocalDate checkdate, String subject, String school);


        /**
         * 查询班级反馈数据
         * @param startdate
         * @param enddate
         * @param subject
         * @param content
         * @param classNumber
         * @return
         */

        List<Record> pageQueryClass(LocalDate startdate,LocalDate enddate, String subject, String content, String classNumber, String school);

        /**
         * 查询个人反馈数据
         * @param startdate
         * @param enddate
         * @param classNumber
         * @param content
         * @param name
         * @param subject
         * @return
         */
        List<Record> getByNameDateClass(LocalDate startdate, LocalDate enddate, String classNumber, String content, String name, String subject, String school);

        /**
         * 根据条件，完成查询所有的记录
         * @param recordQueryDTO
         * @return
         */
        List<RecordVO> getAllRecord(RecordQueryDTO recordQueryDTO);

        void editRecord(RecordVO recordVO);

        /**
         *
         * @param recordVO
         * @return
         */
        Integer addRecord(RecordVO recordVO);

        void deleteRecord(List<Integer> ids);

        List<Record> findAllById(List<Integer> ids);

        void updatecompletedLevel1(Integer completed, LocalDateTime nowtime, String content, String classNumber, LocalDate checkdate, List<String> studentNumberList, List<String> nameList, String subject,String school);

//        List<FeedBackLineChartVO> feedbackUnsubmit(FeedBackDTO feedBackDTO);




    @Select("select * from homework.record where class_number = #{classNumber} and checkdate between #{startDate} and #{endDate} and subject = #{subject} and name = #{username}")
    List<Record> getRecordBySchoolClassName(String classNumber, String username, LocalDate startDate, LocalDate endDate, String subject);

    void updatecompletedLevelminus1(Integer completed, LocalDateTime nowtime, String content, String classNumber, LocalDate checkdate, List<String> studentNumberList, List<String> nameList, String subject, String school);

    void updateUncompleteForLevel1andminus1(LocalDateTime nowtime, String content, String classNumber, LocalDate checkdate, List<String> studentNumberList, List<String> nameList, String subject, String school);

        List<Record> getByNameListDateClass(LocalDate checkdate, LocalDate checkdate1, String classNumber, String content, List<String> nameList, String subject, String school);

        void updateRevisionNoSubmit(LocalDate checkdate, String content, String classNumber, String subject, String school, List<String> noSubmitNameList);

        void updateRevisionSubmitLevel0(LocalDate checkdate, String content, String classNumber, String subject, String school, List<String> submitLevel0NameList);

        void updateRevisionSubmitLevelMinus1(LocalDate checkdate, String content, String classNumber, String subject, String school, List<String> submitLevelMinus1NameList);

        void updateRevisionSubmitLevel1(LocalDate checkdate, String content, String classNumber, String subject, String school, List<String> submitLevel1NameList);

    List<Record> pageQueryStudent(LocalDate startdate, LocalDate enddate, String subject, String content, String classNumber, String school, String name);

    @Select("select id from homework.record where school = #{school} and class_number = #{className} and subject = #{subject} and name = #{studentName} and checkdate = #{checkDate} and content = #{content} limit 1")
    Integer getRecordId(String school, String className, String subject, String studentName, LocalDate checkDate,String content);

    void updateSupplementary(String supplementary, LocalDateTime nowtime, String content, String classNumber, LocalDate checkdate, String subject, String school);
}


