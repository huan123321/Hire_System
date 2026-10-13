<template>
  <div class="message-center">
    <!-- 顶部导航 -->
    <AppHeader />

    <div class="container">
      <div class="main-content" v-loading="loading">
        <!-- 空状态 -->
        <el-empty v-if="conversations.length === 0 && !loading" description="暂无消息">
          <el-button type="primary" @click="goBack">返回</el-button>
        </el-empty>

        <!-- 会话列表 -->
        <div v-else class="conversation-list">
          <div 
            v-for="conv in conversations" 
            :key="conv.partnerId"
            class="conversation-item"
            @click="openChat(conv)">
            <div class="conversation-avatar">
              <img v-if="getPartnerAvatar(conv)" :src="getPartnerAvatar(conv)" class="partner-avatar" alt="头像" />
              <i v-else class="fas fa-user"></i>
              <el-badge 
                v-if="conv.unreadCount > 0" 
                :value="conv.unreadCount" 
                class="unread-badge">
              </el-badge>
            </div>
            <div class="conversation-info">
              <div class="conversation-header">
                <h3 class="partner-name">{{ conv.partnerName }}</h3>
                <span class="last-time">{{ formatTime(conv.lastMessageTime) }}</span>
              </div>
              <div class="conversation-body">
                <p class="last-message">{{ conv.lastMessage || '暂无消息' }}</p>
              </div>
            </div>
            <div class="conversation-action">
              <i class="el-icon-arrow-right"></i>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import AppHeader from '@/components/common/AppHeader.vue'
import { defaultAvatar } from '@/utils/avatar'

export default {
  name: 'MessageCenter',
  components: {
    AppHeader
  },
  data() {
    return {
      loading: false,
      currentUser: null,
      conversations: [],
      // partnerId -> 头像 URL 映射（会话列表显示对方头像）
      avatarMap: {}
    };
  },
  created() {
    // 获取当前用户信息
    const userStr = sessionStorage.getItem('user');
    if (userStr) {
      this.currentUser = JSON.parse(userStr);
    }
    
    // 加载会话列表
    this.loadConversations();

    // 定时刷新会话列表（新消息/简历投递实时可见）
    this.refreshTimer = setInterval(() => {
      this.loadConversations(true);
    }, 10000);
  },
  beforeDestroy() {
    // 清除定时器
    if (this.refreshTimer) {
      clearInterval(this.refreshTimer);
      this.refreshTimer = null;
    }
  },
  methods: {
    /**
     * 加载会话列表
     * @param {boolean} silent 静默刷新（不显示 loading、不弹错误）
     */
    async loadConversations(silent) {
      try {
        if (!silent) {
          this.loading = true;
        }
        
        if (!this.currentUser) {
          if (!silent) {
            this.$message.error('请先登录');
          }
          return;
        }

        // 调用后端接口获取会话列表
        const response = await this.$axios.get(
          this.$apiBaseUrl + '/chat/conversations',
          {
            params: {
              userId: this.currentUser.id
            }
          }
        );
        
        if (response.data.code === 200) {
          this.conversations = response.data.data || [];
          // 加载会话对方的头像
          this.loadPartnerAvatars();
        } else {
          if (!silent) {
            this.$message.error(response.data.msg || '加载会话列表失败');
          }
          this.conversations = [];
        }
      } catch (error) {
        console.error('加载会话列表失败:', error);
        if (!silent) {
          this.$message.error('加载会话列表失败，请稍后重试');
        }
        this.conversations = [];
      } finally {
        this.loading = false;
      }
    },

    /**
     * 加载会话对方的头像（按 partnerId 查询用户信息）
     */
    async loadPartnerAvatars() {
      const partnerIds = [...new Set(
        this.conversations
          .map(c => c.partnerId)
          .filter(id => id != null && !this.avatarMap[String(id)])
      )];
      
      await Promise.all(partnerIds.map(async (partnerId) => {
        try {
          const res = await this.$axios.get(this.$apiBaseUrl + '/user/' + partnerId);
          if (res.data.code === 200 && res.data.data) {
            this.$set(this.avatarMap, String(partnerId), {
              avatar: res.data.data.avatar || '',
              gender: res.data.data.gender
            });
          } else {
            this.$set(this.avatarMap, String(partnerId), { avatar: '', gender: undefined });
          }
        } catch (e) {
          this.$set(this.avatarMap, String(partnerId), { avatar: '', gender: undefined });
        }
      }));
    },

    /**
     * 获取会话对方头像（无自定义头像时按性别显示默认头像）
     */
    getPartnerAvatar(conv) {
      const info = this.avatarMap[String(conv.partnerId)];
      return (info && info.avatar) || defaultAvatar(info && info.gender);
    },

    /**
     * 打开聊天窗口
     */
    openChat(conv) {
      this.$router.push({
        name: 'Chat',
        query: {
          partnerId: conv.partnerId,
          partnerType: 'user',
          partnerName: conv.partnerName
        }
      });
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
     * 返回上一页
     */
    goBack() {
      this.$router.back();
    }
  }
}
</script>

<style scoped>
.message-center {
  min-height: 100vh;
  background: #f5f7fa;
}

.main-content {
  max-width: 800px;
  margin: 30px auto;
  padding: 0 20px;
}

.conversation-list {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.conversation-item {
  display: flex;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #e4e7ed;
  cursor: pointer;
  transition: background-color 0.3s;
}

.conversation-item:last-child {
  border-bottom: none;
}

.conversation-item:hover {
  background-color: #f5f7fa;
}

.conversation-avatar {
  position: relative;
  width: 50px;
  height: 50px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 24px;
  flex-shrink: 0;
  margin-right: 15px;
  overflow: hidden;
}

.partner-avatar {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.unread-badge {
  position: absolute;
  top: -5px;
  right: -5px;
}

.unread-badge >>> .el-badge__content {
  background-color: #f56c6c;
  border: 2px solid #fff;
}

.conversation-info {
  flex: 1;
  min-width: 0;
}

.conversation-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.partner-name {
  margin: 0;
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.last-time {
  font-size: 12px;
  color: #909399;
  flex-shrink: 0;
  margin-left: 10px;
}

.conversation-body {
  overflow: hidden;
}

.last-message {
  margin: 0;
  font-size: 14px;
  color: #606266;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.conversation-action {
  margin-left: 10px;
  color: #909399;
  font-size: 16px;
}
</style>
