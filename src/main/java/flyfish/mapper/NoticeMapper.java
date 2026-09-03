package flyfish.mapper;

import flyfish.pojo.Notice;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface NoticeMapper {


    /**
     * 更新通知信息
     * @param n
     */
    @Update("      UPDATE homework.notificationInfo\n" +
            "            SET name = #{n.name},\n" +
            "            mail = #{n.mail},\n" +
            "            phone = #{n.phone},\n" +
            "            check_mail = #{n.checkMail},\n" +
            "            check_phone = #{n.checkPhone}\n" +
            "            WHERE subject = #{n.subject}\n" +
            "            AND class_number = #{classNumber} " +
            "and school = #{school}")
    void updateNotice(Notice n, String classNumber, String school);

    /**
     * 是否存在3个全部的老师信息
     * @param classNumber
     * @return
     */
    @Select("select count(*) from homework.notificationInfo where class_number = #{classNumber} and school = #{school}")
    Integer existNotice(String classNumber,String school);

    @Delete("delete  from homework.notificationInfo where class_number =#{classNumber} and school #{school} ;")
    void deleteNotice(String classNumber,String school);

    /**
     * 批量新增数据
     * @param noticeList
     * @param classNumber
     */
    void addNotice(List<Notice> noticeList, String classNumber, String school);

    /**
     * 根据班级查询信息
     * @param username
     * @return
     */
    @Select("select * from homework.notificationInfo where class_number = #{usernmae } and school = #{school}")
    List<Notice> queryNoticeByClass(String username,String school);
}
