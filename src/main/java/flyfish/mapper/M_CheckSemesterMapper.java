package flyfish.mapper;


import flyfish.pojo.VO.M_UpdateTableVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface M_CheckSemesterMapper {
    @Select("select distinct  TABLE_SCHEMA as dataBaseName, TABLE_NAME as tableName from INFORMATION_SCHEMA.COLUMNS where TABLE_SCHEMA in('chat','homework','miniprograme') and COLUMN_NAME = 'year' ;")
    List<M_UpdateTableVO> getAllTable(String school);

    @Select("select updateCount from  miniprograme.checkSemester where dataBaseName = #{dataBaseName} and tableName = #{tableName} and school  = #{school} limit 1;")
    Integer getCountByTableName(String dataBaseName, String tableName,String school);

    @Select("select updateStatus from  miniprograme.checkSemester where dataBaseName = #{dataBaseName} and tableName = #{tableName}  and school = #{school} limit 1;")
    String getUpdateStatusByTableName(String dataBaseName, String tableName,String school);

    /**
     * 只查询一小批符合条件的主键，service 使用最后一个主键作为下一批游标。
     * 这样不需要把几十万条 id 加载到 JVM，也不会使用大 OFFSET。
     */
    @Select("""
            SELECT id
            FROM chat.homeworkMessage
            WHERE school = #{school}
              AND id > #{lastId}
              AND CHAR_LENGTH(TRIM(className)) >= 5
              AND LEFT(TRIM(className), 1) IN ('一','二','三','四','五','六','七','八')
              AND SUBSTRING(TRIM(className), 2, 1) IN ('(', '（')
              AND SUBSTRING(TRIM(className), CHAR_LENGTH(TRIM(className)) - 1, 1) IN (')', '）')
              AND RIGHT(TRIM(className), 1) = '班'
            ORDER BY id
            LIMIT #{batchSize}
            """)
    List<Integer> getHomeworkMessageBatchIds(@Param("school") String school,
                                             @Param("lastId") int lastId,
                                             @Param("batchSize") int batchSize);

    /**
     * 一至八年级在同一条 UPDATE 中同时转换，保证每行每次只升一级。
     * service 不开启外层事务，每个小批次执行完即提交并释放行锁。
     */
    @Update("""
            <script>
            UPDATE chat.homeworkMessage
            SET className = CONCAT(
                CASE LEFT(TRIM(className), 1)
                    WHEN '一' THEN '二'
                    WHEN '二' THEN '三'
                    WHEN '三' THEN '四'
                    WHEN '四' THEN '五'
                    WHEN '五' THEN '六'
                    WHEN '六' THEN '七'
                    WHEN '七' THEN '八'
                    WHEN '八' THEN '九'
                END,
                SUBSTRING(TRIM(className), 2)
            )
            WHERE school = #{school}
              AND id IN
              <foreach collection="ids" item="id" open="(" separator="," close=")">
                  #{id}
              </foreach>
              AND CHAR_LENGTH(TRIM(className)) >= 5
              AND LEFT(TRIM(className), 1) IN ('一','二','三','四','五','六','七','八')
              AND SUBSTRING(TRIM(className), 2, 1) IN ('(', '（')
              AND SUBSTRING(TRIM(className), CHAR_LENGTH(TRIM(className)) - 1, 1) IN (')', '）')
              AND RIGHT(TRIM(className), 1) = '班'
            </script>
            """)
    int updateHomeworkMessageClassNameBatch(@Param("school") String school,
                                            @Param("ids") List<Integer> ids);

    @Insert("""
            INSERT INTO miniprograme.checkSemester
                (school, createTime, updateTime, dataBaseName, updateCount, updateStatus, tableName)
            VALUES
                (#{school}, NOW(), NOW(), #{dataBaseName}, 1, '1', #{tableName})
            """)
    int insertSemesterUpdateSucceeded(@Param("dataBaseName") String dataBaseName,
                                      @Param("tableName") String tableName,
                                      @Param("school") String school);

    @Update("""
            UPDATE miniprograme.checkSemester
            SET updateStatus = '1',
                updateCount = COALESCE(updateCount, 0) + 1,
                updateTime = NOW()
            WHERE dataBaseName = #{dataBaseName}
              AND tableName = #{tableName}
              AND school = #{school}
            """)
    int markSemesterUpdateSucceeded(@Param("dataBaseName") String dataBaseName,
                                    @Param("tableName") String tableName,
                                    @Param("school") String school);

    @Select("select dataBaseType from miniprograme.checkUpdateType where dataBaseName = #{dataBaseName} and tableName = #{tableName} limit 1;")
    String getDataBaseTypeByTableName(@Param("dataBaseName") String dataBaseName,
                                      @Param("tableName") String tableName);

    /**
     * 第二种表同时包含 class_name 和 grade。库名、表名只能由 service 校验后传入。
     */
    @Select("""
            <script>
            SELECT id
            FROM `${dataBaseName}`.`${tableName}`
            WHERE school = #{school}
              AND id > #{lastId}
              AND (
                  (CHAR_LENGTH(TRIM(class_name)) >= 5
                   AND LEFT(TRIM(class_name), 1) IN ('一','二','三','四','五','六','七','八')
                   AND SUBSTRING(TRIM(class_name), 2, 1) IN ('(', '（')
                   AND SUBSTRING(TRIM(class_name), CHAR_LENGTH(TRIM(class_name)) - 1, 1) IN (')', '）')
                   AND RIGHT(TRIM(class_name), 1) = '班')
                  OR TRIM(grade) IN ('一年级','二年级','三年级','四年级','五年级','六年级','七年级','八年级')
              )
            ORDER BY id
            LIMIT #{batchSize}
            </script>
            """)
    List<Integer> getClassNameAndGradeBatchIds(@Param("dataBaseName") String dataBaseName,
                                               @Param("tableName") String tableName,
                                               @Param("school") String school,
                                               @Param("lastId") int lastId,
                                               @Param("batchSize") int batchSize);

    /**
     * class_name 和 grade 在同一条 UPDATE 中升级，避免同一行的两个字段更新不同步。
     */
    @Update("""
            <script>
            UPDATE `${dataBaseName}`.`${tableName}`
            SET class_name = CASE
                    WHEN CHAR_LENGTH(TRIM(class_name)) >= 5
                         AND LEFT(TRIM(class_name), 1) IN ('一','二','三','四','五','六','七','八')
                         AND SUBSTRING(TRIM(class_name), 2, 1) IN ('(', '（')
                         AND SUBSTRING(TRIM(class_name), CHAR_LENGTH(TRIM(class_name)) - 1, 1) IN (')', '）')
                         AND RIGHT(TRIM(class_name), 1) = '班'
                    THEN CONCAT(
                        CASE LEFT(TRIM(class_name), 1)
                            WHEN '一' THEN '二'
                            WHEN '二' THEN '三'
                            WHEN '三' THEN '四'
                            WHEN '四' THEN '五'
                            WHEN '五' THEN '六'
                            WHEN '六' THEN '七'
                            WHEN '七' THEN '八'
                            WHEN '八' THEN '九'
                        END,
                        SUBSTRING(TRIM(class_name), 2)
                    )
                    ELSE class_name
                END,
                grade = CASE TRIM(grade)
                    WHEN '一年级' THEN '二年级'
                    WHEN '二年级' THEN '三年级'
                    WHEN '三年级' THEN '四年级'
                    WHEN '四年级' THEN '五年级'
                    WHEN '五年级' THEN '六年级'
                    WHEN '六年级' THEN '七年级'
                    WHEN '七年级' THEN '八年级'
                    WHEN '八年级' THEN '九年级'
                    ELSE grade
                END
            WHERE school = #{school}
              AND id IN
              <foreach collection="ids" item="id" open="(" separator="," close=")">
                  #{id}
              </foreach>
              AND (
                  (CHAR_LENGTH(TRIM(class_name)) >= 5
                   AND LEFT(TRIM(class_name), 1) IN ('一','二','三','四','五','六','七','八')
                   AND SUBSTRING(TRIM(class_name), 2, 1) IN ('(', '（')
                   AND SUBSTRING(TRIM(class_name), CHAR_LENGTH(TRIM(class_name)) - 1, 1) IN (')', '）')
                   AND RIGHT(TRIM(class_name), 1) = '班')
                  OR TRIM(grade) IN ('一年级','二年级','三年级','四年级','五年级','六年级','七年级','八年级')
              )
            </script>
            """)
    int updateClassNameAndGradeBatch(@Param("dataBaseName") String dataBaseName,
                                     @Param("tableName") String tableName,
                                     @Param("school") String school,
                                     @Param("ids") List<Integer> ids);

    @Select("select count(*) from miniprograme.grade_year where year = #{currentYear}")
    int countGradeYearByYear(@Param("currentYear") int currentYear);

    /**
     * 根据年份直接重算年级，不依赖原 grade，因此重复执行也是幂等的。
     * 当前年份为一年级，向前逐年递增，九年前及更早统一为十年级。
     */
    @Update("""
            UPDATE miniprograme.grade_year
            SET grade = CASE
                WHEN year >= #{currentYear} THEN '一年级'
                WHEN year = #{currentYear} - 1 THEN '二年级'
                WHEN year = #{currentYear} - 2 THEN '三年级'
                WHEN year = #{currentYear} - 3 THEN '四年级'
                WHEN year = #{currentYear} - 4 THEN '五年级'
                WHEN year = #{currentYear} - 5 THEN '六年级'
                WHEN year = #{currentYear} - 6 THEN '七年级'
                WHEN year = #{currentYear} - 7 THEN '八年级'
                WHEN year = #{currentYear} - 8 THEN '九年级'
                ELSE '十年级'
            END
            WHERE year IS NOT NULL
            """)
    int refreshGradeYearGrades(@Param("currentYear") int currentYear);

    @Insert("""
            INSERT INTO miniprograme.grade_year (year, grade)
            VALUES (#{currentYear}, '一年级')
            """)
    int insertCurrentGradeYear(@Param("currentYear") int currentYear);

    /** 第三至第六种：查找两位纯数字班级编码，姓名等非数字内容不会入选。 */
    @Select("""
            <script>
            SELECT id
            FROM `${dataBaseName}`.`${tableName}`
            WHERE school = #{school}
              AND id > #{lastId}
              AND TRIM(`${columnName}`) REGEXP '^[1-8][0-9]$'
            ORDER BY id
            LIMIT #{batchSize}
            </script>
            """)
    List<Long> getNumericClassBatchIds(@Param("dataBaseName") String dataBaseName,
                                       @Param("tableName") String tableName,
                                       @Param("columnName") String columnName,
                                       @Param("school") String school,
                                       @Param("lastId") long lastId,
                                       @Param("batchSize") int batchSize);

    /** 只增加两位编码的第一位，第二位班号保持不变。 */
    @Update("""
            <script>
            UPDATE `${dataBaseName}`.`${tableName}`
            SET `${columnName}` = CONCAT(
                CAST(LEFT(TRIM(`${columnName}`), 1) AS UNSIGNED) + 1,
                SUBSTRING(TRIM(`${columnName}`), 2)
            )
            WHERE school = #{school}
              AND id IN
              <foreach collection="ids" item="id" open="(" separator="," close=")">
                  #{id}
              </foreach>
              AND TRIM(`${columnName}`) REGEXP '^[1-8][0-9]$'
            </script>
            """)
    int updateNumericClassBatch(@Param("dataBaseName") String dataBaseName,
                                @Param("tableName") String tableName,
                                @Param("columnName") String columnName,
                                @Param("school") String school,
                                @Param("ids") List<Long> ids);

    /** 第七种：grade 从一年级逐级更新到十年级。 */
    @Select("""
            <script>
            SELECT id
            FROM `${dataBaseName}`.`${tableName}`
            WHERE school = #{school}
              AND id > #{lastId}
              AND TRIM(grade) IN ('一年级','二年级','三年级','四年级','五年级','六年级','七年级','八年级','九年级')
            ORDER BY id
            LIMIT #{batchSize}
            </script>
            """)
    List<Long> getGradeBatchIds(@Param("dataBaseName") String dataBaseName,
                                @Param("tableName") String tableName,
                                @Param("school") String school,
                                @Param("lastId") long lastId,
                                @Param("batchSize") int batchSize);

    @Update("""
            <script>
            UPDATE `${dataBaseName}`.`${tableName}`
            SET grade = CASE TRIM(grade)
                WHEN '一年级' THEN '二年级'
                WHEN '二年级' THEN '三年级'
                WHEN '三年级' THEN '四年级'
                WHEN '四年级' THEN '五年级'
                WHEN '五年级' THEN '六年级'
                WHEN '六年级' THEN '七年级'
                WHEN '七年级' THEN '八年级'
                WHEN '八年级' THEN '九年级'
                WHEN '九年级' THEN '十年级'
                ELSE grade
            END
            WHERE school = #{school}
              AND id IN
              <foreach collection="ids" item="id" open="(" separator="," close=")">
                  #{id}
              </foreach>
              AND TRIM(grade) IN ('一年级','二年级','三年级','四年级','五年级','六年级','七年级','八年级','九年级')
            </script>
            """)
    int updateGradeBatch(@Param("dataBaseName") String dataBaseName,
                         @Param("tableName") String tableName,
                         @Param("school") String school,
                         @Param("ids") List<Long> ids);

    /** 第八种：grade 与 class_Name 同一批、同一条 SQL 更新。 */
    @Select("""
            <script>
            SELECT id
            FROM `${dataBaseName}`.`${tableName}`
            WHERE school = #{school}
              AND id > #{lastId}
              AND (
                  TRIM(grade) IN ('一年级','二年级','三年级','四年级','五年级','六年级','七年级','八年级','九年级')
                  OR (CHAR_LENGTH(TRIM(class_Name)) >= 5
                      AND LEFT(TRIM(class_Name), 1) IN ('一','二','三','四','五','六','七','八','九')
                      AND SUBSTRING(TRIM(class_Name), 2, 1) IN ('(', '（')
                      AND SUBSTRING(TRIM(class_Name), CHAR_LENGTH(TRIM(class_Name)) - 1, 1) IN (')', '）')
                      AND RIGHT(TRIM(class_Name), 1) = '班')
              )
            ORDER BY id
            LIMIT #{batchSize}
            </script>
            """)
    List<Long> getGradeAndClassNameBatchIds(@Param("dataBaseName") String dataBaseName,
                                            @Param("tableName") String tableName,
                                            @Param("school") String school,
                                            @Param("lastId") long lastId,
                                            @Param("batchSize") int batchSize);

    @Update("""
            <script>
            UPDATE `${dataBaseName}`.`${tableName}`
            SET grade = CASE TRIM(grade)
                    WHEN '一年级' THEN '二年级'
                    WHEN '二年级' THEN '三年级'
                    WHEN '三年级' THEN '四年级'
                    WHEN '四年级' THEN '五年级'
                    WHEN '五年级' THEN '六年级'
                    WHEN '六年级' THEN '七年级'
                    WHEN '七年级' THEN '八年级'
                    WHEN '八年级' THEN '九年级'
                    WHEN '九年级' THEN '十年级'
                    ELSE grade
                END,
                class_Name = CASE
                    WHEN CHAR_LENGTH(TRIM(class_Name)) >= 5
                         AND LEFT(TRIM(class_Name), 1) IN ('一','二','三','四','五','六','七','八','九')
                         AND SUBSTRING(TRIM(class_Name), 2, 1) IN ('(', '（')
                         AND SUBSTRING(TRIM(class_Name), CHAR_LENGTH(TRIM(class_Name)) - 1, 1) IN (')', '）')
                         AND RIGHT(TRIM(class_Name), 1) = '班'
                    THEN CONCAT(
                        CASE LEFT(TRIM(class_Name), 1)
                            WHEN '一' THEN '二'
                            WHEN '二' THEN '三'
                            WHEN '三' THEN '四'
                            WHEN '四' THEN '五'
                            WHEN '五' THEN '六'
                            WHEN '六' THEN '七'
                            WHEN '七' THEN '八'
                            WHEN '八' THEN '九'
                            WHEN '九' THEN '十'
                        END,
                        SUBSTRING(TRIM(class_Name), 2)
                    )
                    ELSE class_Name
                END
            WHERE school = #{school}
              AND id IN
              <foreach collection="ids" item="id" open="(" separator="," close=")">
                  #{id}
              </foreach>
              AND (
                  TRIM(grade) IN ('一年级','二年级','三年级','四年级','五年级','六年级','七年级','八年级','九年级')
                  OR (CHAR_LENGTH(TRIM(class_Name)) >= 5
                      AND LEFT(TRIM(class_Name), 1) IN ('一','二','三','四','五','六','七','八','九')
                      AND SUBSTRING(TRIM(class_Name), 2, 1) IN ('(', '（')
                      AND SUBSTRING(TRIM(class_Name), CHAR_LENGTH(TRIM(class_Name)) - 1, 1) IN (')', '）')
                      AND RIGHT(TRIM(class_Name), 1) = '班')
              )
            </script>
            """)
    int updateGradeAndClassNameBatch(@Param("dataBaseName") String dataBaseName,
                                     @Param("tableName") String tableName,
                                     @Param("school") String school,
                                     @Param("ids") List<Long> ids);
}
