/**
 * WebSocket聊天工具类
 * 使用原生WebSocket API,无需额外依赖
 */
class ChatWebSocket {
  constructor() {
    this.ws = null;
    this.connected = false;
    this.messageHandlers = [];
    this.statusHandlers = [];
    this.reconnectTimer = null;
    this.reconnectAttempts = 0;
    this.maxReconnectDelay = 30000;
    this.userId = null;
    this.userType = null;
    this.companyId = null;
    this.heartbeatTimer = null;
    this.pendingMessages = [];
    this.maxPendingMessages = 100;
  }

  /**
   * 连接WebSocket
   * @param {Number} userId - 用户ID
   * @param {String} userType - 用户类型: 'user'或'company'
   */
  connect(userId, userType, options = {}) {
    if (this.connected && this.ws && this.ws.readyState === WebSocket.OPEN) {
      console.log('WebSocket 已连接');
      return;
    }

    this.userId = userId;
    this.userType = userType;
    this.companyId = options.companyId || null;

    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer);
      this.reconnectTimer = null;
    }

    try {
      const params = new URLSearchParams({
        userId: String(userId),
        type: String(userType)
      });
      if (this.companyId) {
        params.append('companyId', String(this.companyId));
      }
      const wsUrl = `ws://localhost:8094/ws/chat?${params.toString()}`;
      this.ws = new WebSocket(wsUrl);
      this.notifyStatus('connecting');

      this.ws.onopen = () => {
        console.log('WebSocket连接成功');
        this.connected = true;
        this.reconnectAttempts = 0;
        this.notifyStatus('connected');
        this.startHeartbeat();
        this.flushPendingMessages();
      };

      // 接收消息
      this.ws.onmessage = (event) => {
        try {
          const message = JSON.parse(event.data);
          console.log('收到消息:', message);
          this.notifyHandlers(message);
        } catch (error) {
          console.error('解析消息失败:', error);
        }
      };

      // 连接关闭
      this.ws.onclose = () => {
        console.log('WebSocket连接关闭');
        this.connected = false;
        this.stopHeartbeat();
        this.notifyStatus('disconnected');
        this.scheduleReconnect();
      };

      // 连接错误
      this.ws.onerror = (error) => {
        console.error('WebSocket错误:', error);
        this.connected = false;
        this.notifyStatus('error');
      };

    } catch (error) {
      console.error('创建WebSocket连接失败:', error);
      this.notifyStatus('error');
      this.scheduleReconnect();
    }
  }

  /**
   * 断开连接
   */
  disconnect() {
    this.notifyStatus('disconnecting');
    if (this.ws) {
      this.ws.close();
      this.ws = null;
      this.connected = false;
      this.stopHeartbeat();
      
      // 清除重连定时器
      if (this.reconnectTimer) {
        clearTimeout(this.reconnectTimer);
        this.reconnectTimer = null;
      }
    }
    this.notifyStatus('disconnected');
  }

  /**
   * 发送消息
   * @param {Object} message - 消息对象
   */
  sendMessage(message) {
    if (!message || typeof message !== 'object') {
      return false;
    }
    if (!this.connected || !this.ws || this.ws.readyState !== WebSocket.OPEN) {
      this.enqueueMessage(message);
      this.scheduleReconnect();
      return true;
    }
    return this.sendRaw(message);
  }

  sendRaw(message) {
    try {
      this.ws.send(JSON.stringify(message));
      return true;
    } catch (error) {
      console.error('发送消息失败:', error);
      return false;
    }
  }

  enqueueMessage(message) {
    if (this.pendingMessages.length >= this.maxPendingMessages) {
      this.pendingMessages.shift();
    }
    this.pendingMessages.push(message);
  }

  flushPendingMessages() {
    if (!this.connected || !this.ws || this.ws.readyState !== WebSocket.OPEN) {
      return;
    }
    if (this.pendingMessages.length === 0) {
      return;
    }
    const queued = [...this.pendingMessages];
    this.pendingMessages = [];
    queued.forEach(msg => {
      const ok = this.sendRaw(msg);
      if (!ok) {
        this.enqueueMessage(msg);
      }
    });
  }

  /**
   * 注册消息处理器
   * @param {Function} handler - 消息处理函数
   */
  onMessage(handler) {
    this.messageHandlers.push(handler);
  }

  /**
   * 移除消息处理器
   * @param {Function} handler - 要移除的处理函数
   */
  offMessage(handler) {
    const index = this.messageHandlers.indexOf(handler);
    if (index > -1) {
      this.messageHandlers.splice(index, 1);
    }
  }

  onStatusChange(handler) {
    this.statusHandlers.push(handler);
  }

  offStatusChange(handler) {
    const index = this.statusHandlers.indexOf(handler);
    if (index > -1) {
      this.statusHandlers.splice(index, 1);
    }
  }

  notifyStatus(status) {
    this.statusHandlers.forEach(handler => {
      try {
        handler(status);
      } catch (error) {
        console.error('状态处理器执行错误:', error);
      }
    });
  }

  /**
   * 通知所有消息处理器
   * @param {Object} message - 消息对象
   */
  notifyHandlers(message) {
    this.messageHandlers.forEach(handler => {
      try {
        handler(message);
      } catch (error) {
        console.error('消息处理器执行错误:', error);
      }
    });
  }

  /**
   * 开始心跳
   */
  startHeartbeat() {
    this.stopHeartbeat(); // 先停止已有的心跳
    
    this.heartbeatTimer = setInterval(() => {
      if (this.connected) {
        this.sendMessage({ type: 'ping' });
      }
    }, 30000); // 每30秒发送一次心跳
  }

  /**
   * 停止心跳
   */
  stopHeartbeat() {
    if (this.heartbeatTimer) {
      clearInterval(this.heartbeatTimer);
      this.heartbeatTimer = null;
    }
  }

  /**
   * 安排重连
   */
  scheduleReconnect() {
    if (this.reconnectTimer || !this.userId || !this.userType) {
      return;
    }

    const delay = Math.min(2000 * Math.pow(2, this.reconnectAttempts), this.maxReconnectDelay);
    this.reconnectAttempts += 1;
    console.log(`${delay / 1000}秒后尝试重连...`);
    this.reconnectTimer = setTimeout(() => {
      this.reconnectTimer = null;
      this.connect(this.userId, this.userType, { companyId: this.companyId });
    }, delay);
  }

  /**
   * 获取连接状态
   */
  isConnected() {
    return this.connected;
  }
}

// 创建单例实例
const chatWebSocket = new ChatWebSocket();

export default chatWebSocket;
