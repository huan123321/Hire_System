<template>
  <div class="header">
    <div class="container">
      <div class="header-content">
        <div class="logo" @click="goToHome">
          <i class="fas fa-briefcase"></i>
          <span>网上招聘系统</span>
        </div>
        <div class="nav-menu">
          <el-button type="primary" round @click="goToHome">首页</el-button>
          <el-button type="primary" round @click="goToPosition">职位</el-button>
          <el-button type="primary" round @click="goToCompany">公司</el-button>
          <el-button type="primary" round @click="goToInterviewManagement">
            <i class="fas fa-handshake"></i>
            面试安排
          </el-button>
          <el-button type="primary" round @click="goToMessageCenter">
            <i class="fas fa-envelope"></i>
            消息
          </el-button>
          <el-button type="primary" round @click="goToPerson">个人中心</el-button>
          <el-button type="text" @click="handleLogout">退出登录</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'AppHeader',
  methods: {
    goToHome() {
      const userStr = sessionStorage.getItem('user')
      const user = userStr ? JSON.parse(userStr) : null
      const targetPath = (user && user.role === 1) ? '/HrHomePage' : '/HomePage'

      if (this.$route.path !== targetPath) {
        this.$router.push(targetPath)
      }
    },

    goToPosition() {
      if (this.$route.path !== '/position') {
        const userStr = sessionStorage.getItem('user')
        const user = userStr ? JSON.parse(userStr) : null
        const targetPath = (user && user.role === 1) ? '/HrPosition' : '/Position'

        if (this.$route.path !== targetPath) {
          this.$router.push(targetPath)
        }
      }
    },
    goToCompany() {
      if (this.$route.path !== '/ComPany') {

        const userStr = sessionStorage.getItem('user')
        const user = userStr ? JSON.parse(userStr) : null
        const targetPath = (user && user.role === 1) ? '/HrCompany' : '/Company'

        if (this.$route.path !== targetPath) {
          this.$router.push(targetPath)
        }

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
      if (this.$route.path !== '/PerSon') {
        const userStr = sessionStorage.getItem('user')
        const user = userStr ? JSON.parse(userStr) : null
        const targetPath = (user && user.role === 1) ? '/HrPerson' : '/Person'

        if (this.$route.path !== targetPath) {
          this.$router.push(targetPath)
        }
      }
    },
    handleLogout() {
      this.$confirm('确定要退出登录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        // 清除用户信息
        sessionStorage.removeItem('user');
        
        this.$message.success('已退出登录');
        this.$router.push('/');
      }).catch(() => {
        // 用户取消
      });
    }
  }
}
</script>

<style scoped lang="scss">
.header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 1000;
  backdrop-filter: blur(10px);
  padding: 15px 0;

  .container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 20px;
  }

  .header-content {
    display: flex;
    justify-content: space-between;
    align-items: center;
    height: auto;
  }

  .logo {
    display: flex;
    align-items: center;
    gap: 10px;
    font-size: 24px;
    font-weight: bold;
    color: #fff;
    cursor: pointer;
    transition: opacity 0.3s;

    &:hover {
      opacity: 0.8;
    }

    i {
      font-size: 28px;
    }
  }

  .nav-menu {
    display: flex;
    align-items: center;
    gap: 15px;

    .el-button {
      transition: all 0.3s;
      
      &:hover {
        transform: translateY(-2px);
      }
    }
  }
}
</style>
