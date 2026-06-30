package Hiresys.mapper;

import Hiresys.entity.ChatMessage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * 聊天消息表 Mapper 接口
 * </p>
 *
 * @author author
 * @since 2025-04-06
 */
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {

    /**
     * 查询两个用户之间的聊天记录
     * @param userId1 用户ID1
     * @param userId2 用户ID2
     * @return 聊天记录列表
     */
    @Select("SELECT * FROM chat_message WHERE " +
            "(sender_id = #{userId1} AND receiver_id = #{userId2}) OR " +
            "(sender_id = #{userId2} AND receiver_id = #{userId1}) " +
            "ORDER BY send_time ASC")
    List<ChatMessage> getChatHistory(@Param("userId1") Integer userId1, 
                                     @Param("userId2") Long userId2);

    /**
     * 获取用户的未读消息数量
     * @param receiverId 接收者ID
     * @param receiverType 接收者类型
     * @return 未读消息数量
     */
    @Select("SELECT COUNT(*) FROM chat_message WHERE receiver_id = #{receiverId} " +
            "AND receiver_type = #{receiverType} AND is_read = 0")
    Integer getUnreadCount(@Param("receiverId") Long receiverId, 
                          @Param("receiverType") String receiverType);

    /**
     * 标记消息为已读
     * @param receiverId 接收者ID
     * @param receiverType 接收者类型
     */
    @Select("UPDATE chat_message SET is_read = 1, read_time = NOW() " +
            "WHERE receiver_id = #{receiverId} AND receiver_type = #{receiverType} " +
            "AND is_read = 0")
    void markMessagesAsRead(@Param("receiverId") Long receiverId, 
                           @Param("receiverType") String receiverType);

    /**
     * 获取用户的会话列表（每个聊天对象的最后一条消息）
     * @param userId 用户ID
     * @return 会话列表
     */
    @Select("SELECT cm.* FROM chat_message cm " +
            "INNER JOIN (" +
            "  SELECT " +
            "    CASE " +
            "      WHEN sender_id = #{userId} THEN receiver_id " +
            "      ELSE sender_id " +
            "    END as partner_id, " +
            "    MAX(id) as max_id " +
            "  FROM chat_message " +
            "  WHERE sender_id = #{userId} OR receiver_id = #{userId} " +
            "  GROUP BY partner_id" +
            ") latest ON cm.id = latest.max_id " +
            "ORDER BY cm.send_time DESC")
    List<ChatMessage> getConversations(@Param("userId") Integer userId);
}
