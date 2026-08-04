<template>
  <div class="chat-box" :class="{ 'is-page-mode': isPageMode }">
    <!-- 独立路由页面时显示统一顶部导航 -->
    <AppHeader v-if="isPageMode" />

    <!-- 聊天头部 -->
    <div class="chat-header">
      <div class="header-info">
        <i class="el-icon-chat-dot-round"></i>
        <span>{{ chatPartnerName || '聊天' }}</span>
        <el-tag size="mini" :type="connectionStatus === 'connected' ? 'success' : 'warning'">
          {{ connectionStatus === 'connected' ? '已连接' : '连接中' }}
        </el-tag>
      </div>
      <div class="header-actions">
        <el-button size="mini" icon="el-icon-close" @click="closeChat"></el-button>
      </div>
    </div>

    <!-- 消息列表 -->
    <div class="message-list" ref="messageList">
      <div v-if="messages.length === 0" class="empty-message">
        <i class="el-icon-chat-line-square"></i>
        <p>开始聊天吧~</p>
      </div>
      
      <div 
        v-for="(msg, index) in messages" 
        :key="index" 
        :class="['message-item', msg.isSelf ? 'self' : 'other']">
        <div class="message-avatar">
          <i class="el-icon-user"></i>
        </div>
        <div class="message-content">
          <div class="message-bubble">
            {{ msg.content }}
          </div>
          <div class="message-time">{{ msg.pending ? '发送中...' : formatTime(msg.sendTime) }}</div>
        </div>
      </div>
    </div>

    <!-- 输入框 -->
    <div class="chat-input">
      <el-input
        v-model="inputMessage"
        type="textarea"
        :rows="3"
        placeholder="请输入消息..."
        @keyup.enter.native="sendMessage"
      ></el-input>
      <div class="input-actions">
        <span class="tip-text">按 Enter 发送</span>
        <el-button 
          type="primary" 
          size="small" 
          :loading="sending"
          @click="sendMessage">
          发送
        </el-button>
      </div>
    </div>
  </div>
</template>

<script>
import AppHeader from '@/components/common/AppHeader.vue'
import chatWebSocket from '@/utils/websocket';

export default {
  name: 'ChatBox',
  components: { AppHeader },
  props: {
    // 聊天对象ID (公司ID或用户ID)
    partnerId: {
      type: [Number, String],
      default: null
    },
    // 聊天对象类型: 'user' 或 'company'
    partnerType: {
      type: String,
      default: ''
    },
    // 聊天对象名称
    partnerName: {
      type: String,
      default: ''
    }
  },
  data() {
    return {
      currentUser: null,
      messages: [],
      inputMessage: '',
      sending: false,
      chatPartnerName: '',
      internalPartnerId: null,
      internalPartnerType: '',
      connectionStatus: chatWebSocket.isConnected() ? 'connected' : 'disconnected',
      connectionTimeout: null
    };
  },
  computed: {
    isPageMode() {
      return this.$route.name === 'Chat';
    }
  },
  created() {
    // 获取当前用户信息
    const userStr = sessionStorage.getItem('user');
    if (userStr) {
      this.currentUser = JSON.parse(userStr);
    }
    
    // 从路由参数获取聊天对象信息
    if (this.$route.query.partnerId) {
      this.chatPartnerName = this.$route.query.partnerName || '聊天';
      this.internalPartnerId = this.$route.query.partnerId;
      this.internalPartnerType = this.$route.query.partnerType || 'user';
    } else if (this.partnerId) {
      // 使用props传入的参数
      this.chatPartnerName = this.partnerName || '聊天';
      this.internalPartnerId = this.partnerId;
      this.internalPartnerType = this.partnerType;
    } else {
      // 缺少聊天对象信息，显示提示并返回
      console.warn('ChatBox: 缺少聊天对象信息');
      this.$message.warning('缺少聊天对象信息，请从消息中心或候选人列表进入');
      this.$nextTick(() => {
        this.$router.back();
      });
      return;
    }
    
    // 连接 WebSocket（如果未连接）
    if (this.currentUser) {
      const userType = this.currentUser.role === 0 ? 'user' : 'company';
      console.log('ChatBox: 准备连接 WebSocket，userId:', this.currentUser.id, ', userType:', userType);
      
      // 设置连接超时提示
      this.connectionTimeout = setTimeout(() => {
        if (!chatWebSocket.isConnected()) {
          this.$message.warning('聊天服务连接超时，请检查后端服务是否启动');
          console.warn('WebSocket 连接超时，请检查：');
          console.warn('1. 后端服务是否在 http://localhost:8094 运行');
          console.warn('2. WebSocket 端点 /ws/chat 是否可访问');
        }
      }, 5000); // 5秒超时
      
      chatWebSocket.connect(this.currentUser.id, userType, {
        companyId: this.currentUser.companyId
      });
    } else {
      this.$message.error('用户信息不存在，请重新登录');
    }
    
    // 加载历史消息
    this.loadHistory();
    
    // 注册消息处理器
    chatWebSocket.onMessage(this.handleIncomingMessage);
    chatWebSocket.onStatusChange(this.handleSocketStatus);
  },
  mounted() {
    // 滚动到底部
    this.scrollToBottom();
  },
  beforeDestroy() {
    // 清除连接超时定时器
    if (this.connectionTimeout) {
      clearTimeout(this.connectionTimeout);
      this.connectionTimeout = null;
    }
    
    // 移除消息处理器
    chatWebSocket.offMessage(this.handleIncomingMessage);
    chatWebSocket.offStatusChange(this.handleSocketStatus);
  },
  methods: {
    /**
     * 加载历史消息
     */
    async loadHistory() {
      try {
        let userId1, userId2;
        
        // 确定聊天对象ID
        const partnerId = this.internalPartnerId || this.partnerId;
        const partnerType = this.internalPartnerType || this.partnerType;
        
        // 根据聊天对象类型确定查询参数
        if (partnerType === 'company') {
          // 求职者与公司聊天
          if (this.currentUser.role === 0) {
            // 求职者：userId1=求职者ID, userId2=公司ID
            userId1 = this.currentUser.id;
            userId2 = partnerId;
          } else {
            // HR：userId1=公司ID, userId2=求职者ID
            userId1 = this.currentUser.companyId || this.currentUser.id;
            userId2 = partnerId;
          }
        } else {
          // 用户与用户聊天（HR与候选人）
          userId1 = this.currentUser.id;
          userId2 = partnerId;
        }
        
        console.log('加载历史消息，参数:', { userId1, userId2, partnerType });
        
        const response = await this.$axios.get(this.$apiBaseUrl + '/chat/history', {
          params: {
            userId1: userId1,
            userId2: userId2
          }
        });
        
        if (response.data.code === 200) {
          this.messages = response.data.data.map(msg => ({
            ...msg,
            isSelf: msg.senderId === this.currentUser.id
          }));
          
          console.log('历史消息加载成功，共', this.messages.length, '条');
          
          this.$nextTick(() => {
            this.scrollToBottom();
          });
        } else {
          console.error('加载历史消息失败:', response.data.msg);
          this.$message.error(response.data.msg || '加载历史消息失败');
        }
      } catch (error) {
        console.error('加载历史消息异常:', error);
        this.$message.error('加载历史消息失败');
      }
    },

    /**
     * 发送消息
     */
    sendMessage() {
      if (!this.inputMessage.trim()) {
        this.$message.warning('请输入消息内容');
        return;
      }

      this.sending = true;
      const clientMsgId = `${this.currentUser.id}_${Date.now()}_${Math.random().toString(36).slice(2, 8)}`;

      // 构建消息对象
      const message = {
        type: 'chat',
        clientMsgId,
        senderId: this.currentUser.id,
        receiverId: this.internalPartnerId || this.partnerId,
        receiverType: this.internalPartnerType || this.partnerType,
        content: this.inputMessage,
        messageType: 'text'
      };

      // 通过WebSocket发送
      const success = chatWebSocket.sendMessage(message);
      
      if (success) {
        // 添加到本地消息列表
        this.messages.push({
          ...message,
          sendTime: new Date().toISOString(),
          isSelf: true,
          pending: true
        });
        
        // 清空输入框
        this.inputMessage = '';
        
        // 滚动到底部
        this.$nextTick(() => {
          this.scrollToBottom();
        });
      } else {
        this.$message.error('发送失败');
      }
      
      this.sending = false;
    },

    /**
     * 处理接收到的消息
     */
    handleIncomingMessage(response) {
      if (response.type === 'sent') {
        const msgData = response.data || {};
        if (msgData.clientMsgId) {
          const localMessage = this.messages.find(m => m.clientMsgId === msgData.clientMsgId);
          if (localMessage) {
            localMessage.pending = false;
            localMessage.id = msgData.id;
            localMessage.sendTime = msgData.sendTime || localMessage.sendTime;
          }
        }
        return;
      }
      if (response.type === 'chat') {
        // 收到新消息
        const msgData = response.data;
        if (!msgData) {
          return;
        }
        if (msgData.clientMsgId && this.messages.some(m => m.clientMsgId === msgData.clientMsgId)) {
          return;
        }
        
        // 判断是否是当前聊天的消息
        const partnerIdNum = Number(this.internalPartnerId || this.partnerId);
        const currentUserIdNum = Number(this.currentUser.id);
        const msgSenderIdNum = Number(msgData.senderId);
        const msgReceiverIdNum = Number(msgData.receiverId);
        const isCurrentChat = 
          (msgSenderIdNum === partnerIdNum && msgReceiverIdNum === currentUserIdNum) ||
          (msgSenderIdNum === currentUserIdNum && msgReceiverIdNum === partnerIdNum);
        
        if (isCurrentChat) {
          this.messages.push({
            ...msgData,
            isSelf: msgData.senderId === this.currentUser.id
          });
          
          this.$nextTick(() => {
            this.scrollToBottom();
          });
        } else {
          // 其他聊天的消息,可以显示通知
          this.$notify({
            title: '新消息',
            message: response.message,
            type: 'info'
          });
        }
      }
    },

    handleSocketStatus(status) {
      this.connectionStatus = status;
    },

    /**
     * 滚动到底部
     */
    scrollToBottom() {
      const container = this.$refs.messageList;
      if (container) {
        container.scrollTop = container.scrollHeight;
      }
    },

    /**
     * 格式化时间
     */
    formatTime(timeStr) {
      if (!timeStr) return '';
      
      const date = new Date(timeStr);
      const now = new Date();
      const diff = now - date;
      
      const minutes = Math.floor(diff / 60000);
      const hours = Math.floor(diff / 3600000);
      const days = Math.floor(diff / 86400000);
      
      if (minutes < 1) {
        return '刚刚';
      } else if (minutes < 60) {
        return `${minutes}分钟前`;
      } else if (hours < 24) {
        return `${hours}小时前`;
      } else if (days < 7) {
        return `${days}天前`;
      } else {
        return date.toLocaleDateString('zh-CN', {
          month: '2-digit',
          day: '2-digit',
          hour: '2-digit',
          minute: '2-digit'
        });
      }
    },

    /**
     * 关闭聊天
     */
    closeChat() {
      // 先触发 close 事件（兼容嵌入使用的情况）
      this.$emit('close');
      
      // 如果是独立路由页面，返回上一页
      if (this.$route.name === 'Chat') {
        this.$router.back();
      }
    }
  }
};
</script>

<style scoped>
.chat-box {
  display: flex;
  flex-direction: column;
  height: 600px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.chat-box.is-page-mode {
  height: calc(100vh - 120px);
  max-width: 900px;
  margin: 20px auto;
  border-radius: 12px;
}

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  border-bottom: 1px solid #e4e7ed;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 8px 8px 0 0;
}

.chat-box.is-page-mode .chat-header {
  border-radius: 12px 12px 0 0;
}

.header-info {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 16px;
  font-weight: bold;
}

.header-info i {
  font-size: 20px;
}

.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  background: #f5f7fa;
}

.empty-message {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #909399;
}

.empty-message i {
  font-size: 64px;
  margin-bottom: 10px;
}

.message-item {
  display: flex;
  margin-bottom: 15px;
  gap: 10px;
}

.message-item.self {
  flex-direction: row-reverse;
}

.message-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #667eea;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 20px;
  flex-shrink: 0;
}

.message-content {
  max-width: 60%;
}

.message-bubble {
  padding: 10px 15px;
  border-radius: 8px;
  background: white;
  word-wrap: break-word;
  line-height: 1.6;
}

.message-item.self .message-bubble {
  background: #667eea;
  color: white;
}

.message-time {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
  text-align: right;
}

.chat-input {
  padding: 15px;
  border-top: 1px solid #e4e7ed;
  background: white;
  border-radius: 0 0 8px 8px;
}

.input-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 10px;
}

.tip-text {
  font-size: 12px;
  color: #909399;
}
</style>
