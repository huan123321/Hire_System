package Hiresys.controller;

import Hiresys.dto.Result;
import Hiresys.entity.ChatMessage;
import Hiresys.entity.User;
import Hiresys.mapper.UserMapper;
import Hiresys.service.IChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Tag(name = "即时聊天", description = "聊天记录查询、未读消息、标记已读、会话列表")
@RestController
@RequestMapping("/chat")
public class ChatController {

    @Autowired
    private IChatService chatService;

    @Autowired
    private UserMapper userMapper;

    @Operation(summary = "获取聊天记录")
    @GetMapping("/history")
    public Result<List<ChatMessage>> getChatHistory(@RequestParam Integer userId1,
                                                     @RequestParam Long userId2) {
        List<ChatMessage> messages = chatService.getChatHistory(userId1, userId2);
        return Result.ok("获取成功", messages);
    }

    @Operation(summary = "获取未读消息数量")
    @GetMapping("/unread")
    public Result<Integer> getUnreadCount(@RequestParam Long receiverId,
                                           @RequestParam String receiverType) {
        Integer count = chatService.getUnreadCount(receiverId, receiverType);
        return Result.ok("获取成功", count);
    }

    @Operation(summary = "标记消息为已读")
    @PostMapping("/mark-read")
    public Result<Void> markMessagesAsRead(@RequestParam Long receiverId,
                                            @RequestParam String receiverType) {
        chatService.markMessagesAsRead(receiverId, receiverType);
        return Result.ok("标记成功", null);
    }

    @Operation(summary = "获取会话列表")
    @GetMapping("/conversations")
    public Result<List<Map<String, Object>>> getConversations(@RequestParam Integer userId) {
        List<ChatMessage> conversations = chatService.getConversations(userId);

        List<Map<String, Object>> conversationList = new ArrayList<>();
        for (ChatMessage msg : conversations) {
            // 解析对方用户：统一按用户维度（历史 company 消息转该公司 HR 用户）
            User partner = null;
            if (msg.getSenderId() != null && msg.getSenderId().equals(userId)) {
                // 我发送的：对方是 receiver
                if ("company".equals(msg.getReceiverType())) {
                    partner = userMapper.selectOne(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<User>()
                            .eq("role", 1)
                            .eq("company_id", msg.getReceiverId())
                            .last("LIMIT 1"));
                } else {
                    partner = userMapper.selectById(msg.getReceiverId().intValue());
                }
            } else {
                partner = userMapper.selectById(msg.getSenderId());
            }

            if (partner == null) {
                continue; // 无有效对方的会话跳过
            }

            Map<String, Object> conv = new HashMap<>();
            conv.put("partnerId", partner.getId());
            conv.put("partnerName",
                    partner.getNickname() != null && !partner.getNickname().isEmpty()
                            ? partner.getNickname()
                            : (partner.getRealName() != null && !partner.getRealName().isEmpty()
                                    ? partner.getRealName() : partner.getUsername()));
            conv.put("partnerType", "user");
            conv.put("lastMessage", msg.getContent());
            conv.put("lastMessageTime", msg.getSendTime());
            // 会话级未读：该会话对方发给我、我未读的消息数
            Integer unreadCount = chatService.getUnreadCountWithSender(userId.longValue(), partner.getId());
            conv.put("unreadCount", unreadCount != null ? unreadCount : 0);

            conversationList.add(conv);
        }

        return Result.ok("获取成功", conversationList);
    }
}
