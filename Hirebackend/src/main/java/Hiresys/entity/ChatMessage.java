package Hiresys.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

import java.time.LocalDateTime;

/**
 * <p>
 * 聊天消息�?
 * </p>
 *
 * @author author
 * @since 2025-04-06
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("chat_message")
public class ChatMessage implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Integer senderId;

    private Long receiverId;

    private String receiverType;

    private String content;

    private String messageType;

    private Boolean isRead;

    private LocalDateTime sendTime;

    private LocalDateTime readTime;
}
