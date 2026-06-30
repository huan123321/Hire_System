package Hiresys.service;

import Hiresys.entity.ChatMessage;

import java.util.List;

/**
 * 聊天服务接口
 */
public interface IChatService {

    /**
     * 保存消息
     * @param message 消息对象
     * @return 保存后的消息
     */
    ChatMessage saveMessage(ChatMessage message);

    /**
     * 获取聊天记录
     * @param userId1 用户ID1
     * @param userId2 用户ID2
     * @return 聊天记录列表
     */
    List<ChatMessage> getChatHistory(Integer userId1, Long userId2);

    /**
     * 获取未读消息数量
     * @param receiverId 接收者ID
     * @param receiverType 接收者类型
     * @return 未读消息数量
     */
    Integer getUnreadCount(Long receiverId, String receiverType);

    /**
     * 标记消息为已读
     * @param receiverId 接收者ID
     * @param receiverType 接收者类型
     */
    void markMessagesAsRead(Long receiverId, String receiverType);

    /**
     * 获取用户的会话列表
     * @param userId 用户ID
     * @return 会话列表
     */
    List<ChatMessage> getConversations(Integer userId);
}
