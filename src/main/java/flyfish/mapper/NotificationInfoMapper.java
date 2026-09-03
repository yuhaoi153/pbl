package flyfish.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface NotificationInfoMapper {
    /**
     * 查看是否允许使用邮件通知
     * @param classNumber
     * @param subject
     * @return
     */
    @Select("select mail from homework.notificationInfo where class_number = #{classNumber} and subject = #{subject} and check_mail = #{checkMail} and school = #{school}")
    String getCheckMail(String classNumber, String subject, boolean checkMail,String school);

    /**
     * 检查是否允许使用手机短信
     * @param classNumber
     * @param subject
     * @param checkPhone
     * @return
     */
    @Select("select phone from homework.notificationInfo where class_number = #{classNumber} and subject = #{subject} and check_phone = #{checkPhone} and school = #{school}")
    String getCheckPhone(String classNumber, String subject, boolean checkPhone,String school);

    /**
     * 查询所有的邮件
     * @param classNumber
     * @param checkMail
     * @return
     */
    @Select("select mail from homework.notificationInfo where class_number = #{classNumber}  and check_mail = #{checkMail} and school = #{school}")
    List<String> getCheckMailList(String classNumber, boolean checkMail,String school);

    /**
     * 查询所有的电话
     * @param classNumber
     * @param checkPhone
     * @return
     */

    @Select("select phone from homework.notificationInfo where class_number = #{classNumber} and check_phone = #{checkPhone} and school = #{school}")
    List<String> getCheckPhoneList(String classNumber, boolean checkPhone,String school);

    /**
     * 查询开通了电话的学科列表
     * @param classNumber
     * @param checkPhone
     * @return
     */
    @Select("select subject from homework.notificationInfo where class_number = #{classNumber} and check_phone = #{checkPhone} and school = #{school}")
    List<String> getCheckPhoneSubject(String classNumber, boolean checkPhone,String school);

    /**
     * 查询班级和学科的邮箱地址，但是有可能是空的
     * @param classNumber
     * @param subject
     * @return
     */
    @Select("select mail from homework.notificationInfo where class_number = #{classNumber} and subject = #{subject} and school = #{school}")
    String getEmailAddress(String classNumber, String subject,String school);

    /**
     * 修改班级和学科的邮箱地址
     * @param classNumber
     * @param subject
     * @param email
     */
    @Select("update homework.notificationInfo set mail = #{email} where class_number = #{classNumber} and subject = #{subject} and school = #{school}")
    void setMail(String classNumber, String subject, String email,String school);

    @Select("select id from homework.notificationInfo where class_number = #{classNumber} and school = #{school}")
    List<Integer> getIdByClassNumber(String classNumber,String school);

    @Update("update homework.notificationInfo set check_mail = #{checkWarningMail} where class_number = #{classNumber} and subject = #{subject} and school = #{school}")
    void setCheckMail(String classNumber, String subject, boolean checkWarningMail,String school);
}
