package flyfish.mapper;

import flyfish.pojo.EditStudentForm;
import flyfish.pojo.ParentPassword;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ParentPasswordMapper {
    /**
     * 查看对应班级是否有文件
     * @param classNumber
     * @return
     */
    @Select("select name from homework.parentPassword where class_number = #{classNumber} and name = #{name}")
    String isexist(String classNumber , String name);

    /**
     * 根据班级删除表
     * @param classNumber
     */
    @Delete("delete from homework.parentPassword where class_number = #{classNumber}")
    void deleteByClass(String classNumber);

    /**
     * 新增文件列表
     * @param parentPasswords
     */
    void addNewFile(List<ParentPassword> parentPasswords);

    /**
     * 根据班级和学生姓名查询
     * @param classNumber
     * @param studentName
     * @return
     */
    List<ParentPassword> getAllContentByCLassName(String classNumber, String studentName);

    /**
     * 新增并返回主键
     * @param parentPassword
     * @return
     */

    Integer addStudentPassword(ParentPassword parentPassword);

    /**
     * 根据ID修改学生用户信息
     * @param parentPassword
     */
    @Update("update homework.parentPassword set class_number =#{classNumber} , name = #{name} , password =#{password} , query_time = #{queryTime} where id =#{id}")
    void editstudentPassword(ParentPassword parentPassword);

    @Delete("delete from homework.parentPassword where id =#{id}")
    void deleteById(Integer id);

    List<String> getRealPassword(String classNumber, String name);

    @Update("update homework.parentPassword set query_time = query_time + 1 where class_number = #{classNumber} and name = #{name}")
    void addOneLoginTime(String classNumber, String name);
}
