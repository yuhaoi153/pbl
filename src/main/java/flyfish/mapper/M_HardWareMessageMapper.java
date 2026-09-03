package flyfish.mapper;

import flyfish.pojo.M_HardWareMessage;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Optional;

@Mapper
public interface M_HardWareMessageMapper {
    @Insert("""
            INSERT INTO miniprograme.hardwareMessage
                (superVisor, deviceName, messageType, messageContent,
                 messageRead, sentTime, supplementary, direction)
            VALUES
                (#{superVisor}, #{deviceName}, #{messageType}, #{messageContent},
                 #{messageRead}, #{sentTime}, #{supplementary}, #{direction})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insert(M_HardWareMessage message);

    @Update("""
            UPDATE miniprograme.hardwareMessage
            SET messageRead = 1
            WHERE id = #{messageId}
              AND deviceName = #{deviceName}
              AND direction = 'toHardware'
            """)
    int markRead(@Param("messageId") Integer messageId,
                 @Param("deviceName") String deviceName);

    @Select("""
            SELECT id, superVisor, deviceName, messageType, messageContent,
                   messageRead, sentTime, supplementary, direction
            FROM miniprograme.hardwareMessage
            WHERE deviceName = #{deviceName}
              AND direction = 'toHardware'
              AND id < #{currentMessageId}
            ORDER BY id DESC
            LIMIT 1
            """)
    Optional<M_HardWareMessage> findPreviousHardwareMessage(
            @Param("deviceName") String deviceName,
            @Param("currentMessageId") Integer currentMessageId);

    @Select("""
            SELECT id, superVisor, deviceName, messageType, messageContent,
                   messageRead, sentTime, supplementary, direction
            FROM miniprograme.hardwareMessage
            WHERE deviceName = #{deviceName}
              AND direction = 'toHardware'
              AND id > #{currentMessageId}
            ORDER BY id ASC
            LIMIT 1
            """)
    Optional<M_HardWareMessage> findNextHardwareMessage(
            @Param("deviceName") String deviceName,
            @Param("currentMessageId") Integer currentMessageId);

    @Select("""
            SELECT id, superVisor, deviceName, messageType, messageContent,
                   messageRead, sentTime, supplementary, direction
            FROM miniprograme.hardwareMessage
            WHERE deviceName = #{deviceName}
              AND direction = 'toHardware'
            ORDER BY id DESC
            LIMIT 1
            """)
    Optional<M_HardWareMessage> findLatestHardwareMessage(
            @Param("deviceName") String deviceName);

    @Select("""
            SELECT id, superVisor, deviceName, messageType, messageContent,
                   messageRead, sentTime, supplementary, direction
            FROM miniprograme.hardwareMessage
            WHERE deviceName = #{deviceName}
            ORDER BY id DESC
            LIMIT #{limit}
            """)
    List<M_HardWareMessage> findRecentDeviceMessages(
            @Param("deviceName") String deviceName,
            @Param("limit") Integer limit);

    @Select("""
            SELECT COUNT(*)
            FROM miniprograme.hardwareMessage
            WHERE deviceName = #{deviceName}
              AND direction = 'toSoftware'
              AND supplementary = #{supplementary}
            """)
    int countReplyByEvent(
            @Param("deviceName") String deviceName,
            @Param("supplementary") String supplementary);
}
