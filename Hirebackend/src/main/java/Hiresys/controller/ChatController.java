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

        List<Map<String, Object>> conversationList = conversations.stream().map(msg -> {
            Map<String, Object> conv = new HashMap<>();

            Long partnerId;
            String partnerType;
            if (msg.getSenderId().equals(userId)) {
                partnerId = msg.getReceiverId();
                partnerType = msg.getReceiverType();
            } else {
                partnerId = msg.getSenderId().longValue();
                partnerType = "user";
            }

            String partnerName = "未知用户";
            if ("user".equals(partnerType)) {
                User partner = userMapper.selectById(partnerId.intValue());
                if (partner != null) {
                    partnerName = partner.getUsername();
                }
            } else if ("company".equals(partnerType)) {
                partnerName = "HR";
            }

            Integer unreadCount = chatService.getUnreadCount(userId.longValue(), "user");

            conv.put("partnerId", partnerId);
            conv.put("partnerName", partnerName);
            conv.put("partnerType", partnerType);
            conv.put("lastMessage", msg.getContent());
            conv.put("lastMessageTime", msg.getSendTime());
            conv.put("unreadCount", unreadCount != null ? unreadCount : 0);

            return conv;
        }).collect(Collectors.toList());

        return Result.ok("获取成功", conversationList);
    }
}
