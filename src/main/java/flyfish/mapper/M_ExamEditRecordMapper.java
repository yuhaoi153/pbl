package flyfish.mapper;

import flyfish.pojo.M_ExamEditRecord;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface M_ExamEditRecordMapper {

    void insertSingleRecord(M_ExamEditRecord mExamEditRecord);
}
