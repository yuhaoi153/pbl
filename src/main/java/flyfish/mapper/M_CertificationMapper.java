package flyfish.mapper;

import flyfish.pojo.M_Certification;
import flyfish.pojo.M_ExportRequestDTO;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface M_CertificationMapper {

    void addSingleRecord(M_Certification certification);


    //根据ID删除证书记录
    @Delete("DELETE FROM miniprograme.certification WHERE id = #{id}")
    void deleteById(Integer id);

    void updateSingleRecord(M_Certification mCertification);

    @Select("SELECT id , imageUrl, createTime,type,awardLevel,content,regionLevel,userName as teacherName,awardName,personal,supplement,awardTime,organization,school,judgeRepeat ,repeatId FROM miniprograme.certification WHERE userName = #{userName} and school = #{school} ORDER BY createTime DESC")
    List<M_Certification> getCertificationsByUserName(String userName, String school);

    @Select("SELECT imageUrl FROM miniprograme.certification WHERE id = #{id}")
    String getImageUrlById(Integer id);

    @Update("UPDATE miniprograme.certification SET imageUrl = #{imageNew} WHERE id = #{id}")
    void updateImageUrlById(Integer id, String imageNew);

    @Update( "UPDATE miniprograme.certification SET judgeRepeat = #{judgeRepeat}, repeatId = #{repeatId} WHERE id = #{id}")
    void markDuplicateById(Integer id, String judgeRepeat, Integer repeatId);

    @Select("select * from miniprograme.certification where repeatId = #{repeatId}")
    List<M_Certification> getCertificationsByRepeatId(Integer repeatId);

    @Select("select * from miniprograme.certification where type =#{type} and awardLevel = #{awardLevel} and regionLevel = #{regionLevel} and awardName = #{awardName} and personal = #{personal} and awardTime = #{awardTime} and organization = #{organization} and school = #{school}")
    List<M_Certification> getRepeatList(M_Certification certification);


    @Select("select repeatId from miniprograme.certification where imageUrl = #{imageUrl} and type =#{type} and awardLevel = #{awardLevel} and regionLevel = #{regionLevel} and awardName = #{awardName} and personal = #{personal} and awardTime = #{awardTime} and organization = #{organization} and school = #{school} limit 1")
    Integer getIdByAll(M_Certification cert);
    @Select("SELECT id , imageUrl, createTime,type,awardLevel,content,regionLevel,userName as teacherName,awardName,personal,supplement,awardTime,organization,school,judgeRepeat ,repeatId FROM miniprograme.certification WHERE  school = #{school} ORDER BY createTime DESC")
    List<M_Certification> getAllCertifications(String school);

    @Select("SELECT userName FROM miniprograme.certification WHERE repeatId = #{repeatId}")
    List<String> getTeacherNameListByRepeatId(Integer repeatId);

    List<M_Certification> getAllCertificationsByItems(M_ExportRequestDTO request);
}
