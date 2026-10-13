<template>
  <div class="header">
    <div class="container">
      <div class="header-content">
        <div class="logo" @click="goToHome">
          <i class="fas fa-briefcase"></i>
          <span>Hire<span class="logo-dot">•</span></span>
        </div>
        <div class="nav-menu">
          <el-button type="text" @click="goToHome" :class="{ 'nav-active': isActive('/HomePage') || isActive('/HrHomePage') }">首页</el-button>
          <el-button type="text" @click="goToPosition" :class="{ 'nav-active': isActive('/Position') || isActive('/HrPosition') }">职位</el-button>
          <el-button type="text" @click="goToCompany" :class="{ 'nav-active': isActive('/ComPany') || isActive('/HrCompany') }">公司</el-button>
          <el-button type="text" @click="goToInterviewManagement" :class="{ 'nav-active': isActive('/InterviewManagement') }">面试安排</el-button>
          <el-button type="text" @click="goToMessageCenter" :class="{ 'nav-active': isActive('/MessageCenter') }">消息</el-button>
        </div>
        <div class="user-area">
          <el-button type="text" class="user-link" @click="goToPerson">
            <el-avatar :size="28" :src="displayAvatar" class="header-avatar">
              <i class="el-icon-user"></i>
            </el-avatar>
            {{ nickname }}
          </el-button>
          <el-button type="text" class="logout-link" @click="handleLogout">退出登录</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { defaultAvatar } from '@/utils/avatar';

export default {
  name: 'AppHeader',
  data() {
    return {
      nickname: '',
      avatar: '',
      gender: ''
    };
  },
  created() {
    const userStr = sessionStorage.getItem('user');
    if (userStr) {
      try {
        const user = JSON.parse(userStr);
        this.nickname = user.nickname || user.realName || user.username || '用户';
        this.avatar = user.avatar || '';
        this.gender = user.gender;
      } catch (e) {
        this.nickname = '用户';
      }
    }
  },
  computed: {
    // 无自定义头像时按性别显示默认头像
    displayAvatar() {
      return this.avatar || defaultAvatar(this.gender);
    }
  },
  methods: {
    isActive(path) {
      return this.$route.path === path || this.$route.path.startsWith(path);
    },
    goToHome() {
      const userStr = sessionStorage.getItem('user')
      const user = userStr ? JSON.parse(userStr) : null
      const targetPath = (user && user.role === 1) ? '/HrHomePage' : '/HomePage'
      if (this.$route.path !== targetPath) {
        this.$router.push(targetPath)
      }
    },
    goToPosition() {
      // HR 进职位管理页，求职者进职位列表页
      const userStr = sessionStorage.getItem('user');
      const user = userStr ? JSON.parse(userStr) : null;
      const target = (user && user.role === 1) ? '/HrPosition' : '/Position';
      if (this.$route.path !== target) {
        this.$router.push(target);
      }
    },
    goToCompany() {
      // HR 进公司管理页，求职者进公司列表页
      const userStr = sessionStorage.getItem('user');
      const user = userStr ? JSON.parse(userStr) : null;
      const target = (user && user.role === 1) ? '/HrCompany' : '/ComPany';
      if (this.$route.path !== target) {
        this.$router.push(target);
      }
    },
    goToInterviewManagement() {
      if (this.$route.path !== '/InterviewManagement') {
        this.$router.push('/InterviewManagement');
      }
    },
    goToMessageCenter() {
      if (this.$route.path !== '/MessageCenter') {
        this.$router.push({ name: 'MessageCenter' });
      }
    },
    goToPerson() {
      const userStr = sessionStorage.getItem('user');
      const user = userStr ? JSON.parse(userStr) : null;
      const target = (user && user.role === 1) ? '/HrPerson' : '/PerSon';
      if (this.$route.path !== target) {
        this.$router.push(target);
      }
    },
    handleLogout() {
      this.$confirm('确定要退出登录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        sessionStorage.removeItem('user');
        this.$message.success('已退出登录');
        this.$router.push('/');
      }).catch(() => {});
    }
  }
}
</script>

<style scoped lang="scss">
.header {
  background: #fff;
  color: #333;
  box-shadow: 0 1px 6px rgba(0, 0, 0, 0.06);
  position: sticky;
  top: 0;
  z-index: 1000;
  padding: 10px 0;

  .container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 20px;
  }

  .header-content {
    display: flex;
    justify-content: space-between;
    align-items: center;
    height: 40px;
  }

  .logo {
    display: flex;
    align-items: center;
    gap: 10px;
    font-size: 24px;
    font-weight: 800;
    color: #00C2A1;
    cursor: pointer;
    margin-right: 40px;

    i { font-size: 26px; }
    .logo-dot { color: #FF8A00; }
  }

  .nav-menu {
    display: flex;
    align-items: center;
    gap: 4px;
    flex: 1;

    .el-button {
      padding: 8px 14px;
      font-size: 15px;
      color: #333;
      font-weight: 400;

      &:hover { color: #00C2A1; }
      &.nav-active { color: #00C2A1; font-weight: 600; }
    }
  }

  .user-area {
    display: flex;
    align-items: center;
    gap: 4px;

    .user-link {
      color: #00C2A1;
      font-weight: 500;
      display: flex;
      align-items: center;
      gap: 4px;
    }
    .header-avatar {
      border: 2px solid #00C2A1;
      flex-shrink: 0;
    }
    .logout-link { color: #999; }
    .logout-link:hover { color: #FF5252; }
  }
}
</style>

