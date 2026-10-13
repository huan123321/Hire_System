<template>
  <div id="app" class="boss-app">
    <router-view/>

    <!-- BOSS风格底部 -->
    <SimpleFooter v-if="showFooter" />

    <!-- AI 助手浮动按钮 -->
    <div v-if="currentUser" class="ai-fab" @click="$router.push('/AiChat')" title="AI 招聘助手">
      <i class="el-icon-cpu"></i>
    </div>
  </div>
</template>

<script>
import chatWebSocket from '@/utils/websocket';
import SimpleFooter from '@/components/common/SimpleFooter.vue';

export default {
  name: 'App',
  components: { SimpleFooter },
  data() {
    return {
      currentUser: null
    };
  },
  computed: {
    showFooter() {
      const hiddenRoutes = ['LoginView', 'RegisTer'];
      return !hiddenRoutes.includes(this.$route.name);
    }
  },
  created() {
    const userStr = sessionStorage.getItem('user');
    if (userStr) {
      this.currentUser = JSON.parse(userStr);
      this.connectWebSocket();
      this.setupNotificationListener();
    }
  },
  beforeDestroy() {
    chatWebSocket.disconnect();
  },
  methods: {
    connectWebSocket() {
      if (!this.currentUser || !this.currentUser.id) return;
      // 聊天统一按用户维度：求职者和 HR 都以用户身份连接
      const userType = 'user';
      const companyId = this.currentUser.companyId || null;
      chatWebSocket.connect(this.currentUser.id, userType, { companyId });
    },
    setupNotificationListener() {
      chatWebSocket.onMessage((message) => {
        if (message.type === 'notification') this.handleNotification(message);
        else if (message.type === 'chat') this.handleChatMessage(message);
      });
    },
    handleNotification(message) {
      this.$notify({
        title: '📢 状态更新通知',
        message: message.message || '您收到一条新通知',
        type: this.getNotificationType(message.data?.newStatus),
        duration: 8000,
        position: 'top-right',
        offset: 80,
        onClick: () => this.$router.push('/MessageCenter')
      });
    },
    handleChatMessage(message) {
      this.$notify({
        title: '💬 新消息',
        message: (message.data || {}).content || '您收到一条新消息',
        type: 'info',
        duration: 5000,
        position: 'top-right',
        offset: 80,
        onClick: () => this.$router.push('/MessageCenter')
      });
    },
    getNotificationType(status) {
      const map = { hired: 'success', interview: 'warning', rejected: 'error', viewed: 'info' };
      return map[status] || 'info';
    }
  }
};
</script>

<style lang="scss">
@import './assets/styles/variables.scss';
@import './assets/styles/global.scss';
@import './assets/styles/footer.scss';

.ai-fab {
  position: fixed;
  bottom: 30px;
  right: 30px;
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: linear-gradient(135deg, #00C2A1, #36cfc9);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 26px;
  cursor: pointer;
  box-shadow: 0 4px 14px rgba(0, 194, 161, 0.4);
  transition: transform 0.2s;
  z-index: 999;
  &:hover { transform: scale(1.1); }
}
</style>
