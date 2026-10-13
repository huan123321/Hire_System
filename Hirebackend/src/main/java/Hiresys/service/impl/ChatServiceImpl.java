package Hiresys.service.impl;

import Hiresys.entity.ChatMessage;
import Hiresys.mapper.ChatMessageMapper;
import Hiresys.service.IChatService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 聊天服务实现类
 */
@Service
public class ChatServiceImpl extends ServiceImpl<ChatMessageMapper, ChatMessage> implements IChatService {

    @Autowired
    private ChatMessageMapper chatMessageMapper;

    @Override
    public ChatMessage saveMessage(ChatMessage message) {
        // 设置发送时间
        message.setSendTime(LocalDateTime.now());
        // 默认未读
        if (message.getIsRead() == null) {
            message.setIsRead(false);
        }
        // 默认消息类型为文本
        if (message.getMessageType() == null) {
            message.setMessageType("text");
        }
        
        // 保存到数据库
        chatMessageMapper.insert(message);
        return message;
    }

    @Override
    public List<ChatMessage> getChatHistory(Integer userId1, Long userId2) {
        return chatMessageMapper.getChatHistory(userId1, userId2);
    }

    @Override
    public Integer getUnreadCount(Long receiverId, String receiverType) {
        return chatMessageMapper.getUnreadCount(receiverId, receiverType);
    }

    @Override
    public Integer getUnreadCountWithSender(Long receiverId, Integer senderId) {
        return chatMessageMapper.getUnreadCountWithSender(receiverId, senderId);
    }

    @Override
    public void markMessagesAsRead(Long receiverId, String receiverType) {
        chatMessageMapper.markMessagesAsRead(receiverId, receiverType);
    }

    @Override
    public List<ChatMessage> getConversations(Integer userId) {
        return chatMessageMapper.getConversations(userId);
    }
}
