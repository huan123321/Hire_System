<template>
  <div class="simple-footer">
    <div class="container">
      <div class="footer-content">
        <!-- 左侧品牌信息 -->
        <div class="footer-left">
          <h3 class="footer-title">网上招聘系统</h3>
          <p class="footer-desc">连接全球优秀企业与人才，让招聘更简单，让求职更高效</p>
          <div class="social-icons">
            <div class="social-icon"></div>
            <div class="social-icon"></div>
            <div class="social-icon"></div>
            <div class="social-icon"></div>
          </div>
        </div>

        <!-- 右侧导航链接 -->
        <div class="footer-right">
          <div class="footer-nav">
            <h4 class="nav-title">快速导航</h4>
            <ul class="nav-list">
              <li><a href="#" @click.prevent="goToPosition">职位</a></li>
              <li><a href="#" @click.prevent="goToCompany">公司</a></li>
              <li><a href="#" @click.prevent="goToMessageCenter">消息中心</a></li>
              <li><a href="#" @click.prevent="goToPerson">个人中心</a></li>
            </ul>
          </div>

          <div class="footer-nav">
            <h4 class="nav-title">求职服务</h4>
            <ul class="nav-list">
              <li><a href="#" @click.prevent="goToResume">创建简历</a></li>
              <li><a href="#" @click.prevent="goToResume">我的简历</a></li>
              <li><a href="#" @click.prevent="goToApplications">我的申请</a></li>
              <li><a href="#" @click.prevent="goToInterviews">面试邀请</a></li>
            </ul>
          </div>

          <div class="footer-nav">
            <h4 class="nav-title">帮助与支持</h4>
            <ul class="nav-list">
              <li><a href="#">帮助中心</a></li>
              <li><a href="#">隐私政策</a></li>
              <li><a href="#">服务条款</a></li>
              <li><a href="#">联系我们</a></li>
            </ul>
          </div>
        </div>
      </div>

      <!-- 底部版权 -->
      <div class="footer-bottom">
        <div class="copyright">
          2024-2025 网上招聘系统. All rights reserved.
        </div>
        <div class="tech-info">
          Powered by Vue.js & Spring Boot · Made with
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'SimpleFooter',
  methods: {
    goToPosition() {
      this.$router.push('/Position');
    },
    goToCompany() {
      this.$router.push('/ComPany');
    },
    goToMessageCenter() {
      this.$router.push({ name: 'MessageCenter' });
    },
    goToPerson() {
      this.$router.push('/PerSon');
    },
    goToResume() {
      this.$router.push('/EditResume');
    },
    goToApplications() {
      this.$router.push({ path: '/PerSon', query: { tab: 'applications' } });
    },
    goToInterviews() {
      this.$router.push({ path: '/PerSon', query: { tab: 'interviews' } });
    }
  }
}
</script>

<style scoped>
.simple-footer {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  padding: 60px 0 30px;
  margin-top: 60px;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.footer-content {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 40px;
}

.footer-left {
  flex: 1;
  max-width: 400px;
}

.footer-title {
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 16px;
}

.footer-desc {
  font-size: 14px;
  line-height: 1.6;
  color: rgba(255, 255, 255, 0.85);
  margin-bottom: 24px;
}

.social-icons {
  display: flex;
  gap: 12px;
}

.social-icon {
  width: 40px;
  height: 40px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  cursor: pointer;
  transition: all 0.3s ease;
}

.social-icon:hover {
  background: rgba(255, 255, 255, 0.3);
  transform: translateY(-2px);
}

.footer-right {
  display: flex;
  gap: 80px;
  flex: 2;
  justify-content: flex-end;
}

.footer-nav {
  min-width: 120px;
}

.nav-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 20px;
  position: relative;
  padding-bottom: 10px;
}

.nav-title::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 30px;
  height: 2px;
  background: rgba(255, 255, 255, 0.5);
}

.nav-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.nav-list li {
  margin-bottom: 12px;
}

.nav-list a {
  color: rgba(255, 255, 255, 0.85);
  text-decoration: none;
  font-size: 14px;
  transition: all 0.3s ease;
}

.nav-list a:hover {
  color: #fff;
  padding-left: 5px;
}

.footer-bottom {
  border-top: 1px solid rgba(255, 255, 255, 0.2);
  padding-top: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.8);
}

@media (max-width: 768px) {
  .footer-content {
    flex-direction: column;
    gap: 40px;
  }

  .footer-left {
    max-width: 100%;
  }

  .footer-right {
    flex-direction: column;
    gap: 30px;
  }

  .footer-bottom {
    flex-direction: column;
    gap: 10px;
    text-align: center;
  }
}
</style>
