package flyfish.mapper;

import flyfish.pojo.ChatMessage;
import flyfish.pojo.VO.PrivateMessageVO;
import org.apache.ibatis.annotations.*;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface PBL_ChatMessageMapper {
    @Select("select * from pbl.messages where sender_id = #{userId} or receiver_id = #{userId} " +
            "order by sent_at ASC ")
    List<PrivateMessageVO> getPrivateMassage(Integer userId);


    void storePrivateMessage(ChatMessage message);

    @Select("select * from pbl.messages where receiver_id = #{userId} and messageRead = 0")
    List<PrivateMessageVO> getAllUnreadMessage(Integer userId);

    @Update({"update pbl.messages set messageRead = 1 where sender_id = #{senderId} and receiver_id = #{receiverId} and messageRead = 0"})
    void setMessageRead(Integer senderId, Integer receiverId);

    @Select("select * from pbl.messages where call_Id = #{callId}")
    ChatMessage selectByCallId(String callId);

    @Update("update pbl.messages set supplementary = #{supplementary}, duration = #{duration} where message_id = #{messageId}")
    void updateById(ChatMessage record);

    @Delete("delete from pbl.messages where message_id = #{id}")
    void deleteById(Integer id);

    @Select("select * from pbl.messages where (sender_id = #{userId} or receiver_id = #{userId}) and checkDate= #{checkDate} order by sent_at ASC")
    List<PrivateMessageVO> getPrivateMassageByIdCheckDate(Integer userId, LocalDate checkDate);

    @Delete("delete from pbl.messages where checkDate = #{checkDate}")
    void resetAllByCheckDate(LocalDate checkDate);
}
