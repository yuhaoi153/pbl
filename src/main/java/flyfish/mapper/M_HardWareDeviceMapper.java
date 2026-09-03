package flyfish.mapper;

import flyfish.pojo.M_HardwareDevice;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Optional;

@Mapper
public interface M_HardWareDeviceMapper {
    @Select("""
            SELECT id, school, grade, className, deviceType, deviceNum,
                   userName, purpose, superVisor
            FROM miniprograme.hardwareDevice
            WHERE school = #{school}
              AND grade = #{grade}
              AND className = #{className}
              AND superVisor = #{teacherName}
            ORDER BY id
            """)
    List<M_HardwareDevice> findByClassAndSupervisor(
            @Param("school") String school,
            @Param("grade") String grade,
            @Param("className") Integer className,
            @Param("teacherName") String teacherName);

    @Select("""
            SELECT id, school, grade, className, deviceType, deviceNum,
                   userName, purpose, superVisor
            FROM miniprograme.hardwareDevice
            WHERE id = #{id}
            LIMIT 1
            """)
    Optional<M_HardwareDevice> findById(@Param("id") Integer id);

    @Select("""
            SELECT id, school, grade, className, deviceType, deviceNum,
                   userName, purpose, superVisor
            FROM miniprograme.hardwareDevice
            WHERE CONCAT(deviceType, deviceNum) = #{deviceName}
            LIMIT 1
            """)
    Optional<M_HardwareDevice> findByDeviceName(
            @Param("deviceName") String deviceName);
}
