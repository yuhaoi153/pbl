package flyfish.mapper;

import flyfish.pojo.ImageUrl;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface ImageUrlMapper {

    @Insert("insert into homework.imageurl (url, check_date, create_time) VALUES (#{url},#{checkDate},#{createTime})")
    void add(ImageUrl imageUrl);

    /**
     * 查询url列表
     * @param checkdate
     * @return
     */
    @Select("select url from homework.imageurl where check_date = #{checkdate}")
    List<String> getByDate(LocalDate checkdate);
}
