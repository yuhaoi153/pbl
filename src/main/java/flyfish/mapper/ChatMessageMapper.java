package flyfish.mapper;

import flyfish.pojo.ChatMessage;
import flyfish.pojo.PBLpojo.PBL_NameScoreDTO;
import flyfish.pojo.VO.PrivateMessageVO;
import org.apache.ibatis.annotations.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface ChatMessageMapper {
    @Select("select * from chat.messages where (sender_id =#{sender} and receiver_id = #{receiver}) or (sender_id =#{receiver} and receiver_id = #{sender}) " +
            "order by sent_at ASC ")
    List<PrivateMessageVO> getPrivateMassage(Integer sender, Integer receiver);

    @Insert("insert into chat.messages (sender_id, receiver_id, chat_type, message_type, message_content, sent_at,duration,fileSize,supplementary,call_Id) " +
            "values (#{senderId},#{receiverId},#{chatType},#{messageType},#{messageContent},#{sentAt},#{duration},#{fileSize},#{supplementary},#{callId})")
    void storePrivateMessage(ChatMessage message);

    @Select("select * from chat.messages where receiver_id = #{userId} and messageRead = 0")
    List<PrivateMessageVO> getAllUnreadMessage(Integer userId);

    @Update({"update chat.messages set messageRead = 1 where sender_id = #{senderId} and receiver_id = #{receiverId} and messageRead = 0"})
    void setMessageRead(Integer senderId, Integer receiverId);

    @Select("select * from chat.messages where call_Id = #{callId}")
    ChatMessage selectByCallId(String callId);

    @Update("update chat.messages set supplementary = #{supplementary}, duration = #{duration} where message_id = #{messageId}")
    void updateById(ChatMessage record);

    @Select("select  senderName as username, count(*) as sumScore from pbl.messages where checkDate = #{checkDate} group by senderName")
    List<PBL_NameScoreDTO> getNameScore(LocalDate checkDate);

    @Select("SELECT * FROM chat.messages " +
            "WHERE ((sender_id = #{senderId} AND receiver_id = #{receiverId}) " +
            "    OR (sender_id = #{receiverId} AND receiver_id = #{senderId})) " +
            "  AND sent_at BETWEEN #{startDate} AND #{endDate} " +
            "ORDER BY sent_at ASC")
    List<PrivateMessageVO> getPrivateMassageByDate(Integer senderId, Integer receiverId, LocalDateTime startDate, LocalDateTime endDate);

    @Delete("delete from chat.messages where message_id = #{messageId}")
    void deleteById(Integer messageId);
}
