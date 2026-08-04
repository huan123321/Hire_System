<template>
  <div class="simple-footer">
    <div class="container">
      <div class="footer-content">
        <div class="footer-left">
          <h3 class="footer-title">
            <span class="brand-main">Hire</span><span class="brand-dot">•</span>
          </h3>
          <p class="footer-desc">找工作，直接谈，找得快</p>
        </div>

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

      <div class="footer-bottom">
        <div class="copyright">
          © 2026 Hire System 网上招聘系统. All rights reserved.
        </div>
        <div class="tech-info">
          Powered by Vue.js & Spring Boot
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'SimpleFooter',
  methods: {
    goToPosition() { this.$router.push('/Position'); },
    goToCompany() { this.$router.push('/ComPany'); },
    goToMessageCenter() { this.$router.push({ name: 'MessageCenter' }); },
    goToPerson() { this.$router.push('/PerSon'); },
    goToResume() { this.$router.push('/EditResume'); },
    goToApplications() { this.$router.push({ path: '/PerSon', query: { tab: 'applications' } }); },
    goToInterviews() { this.$router.push({ path: '/PerSon', query: { tab: 'interviews' } }); }
  }
}
</script>

<style scoped>
.simple-footer {
  background: #2C3238;
  color: #AEB4BE;
  padding: 40px 0 0;
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
  padding-bottom: 30px;
}

.footer-left {
  flex: 1;
  max-width: 300px;
}

.footer-title {
  font-size: 24px;
  font-weight: 800;
  margin-bottom: 12px;
}
.brand-main { color: #00C2A1; }
.brand-dot { color: #FF8A00; }

.footer-desc {
  font-size: 13px;
  color: #AEB4BE;
  line-height: 1.6;
}

.footer-right {
  display: flex;
  gap: 80px;
  flex: 2;
  justify-content: flex-end;
}

.footer-nav { min-width: 120px; }

.nav-title {
  font-size: 15px;
  font-weight: 600;
  color: #fff;
  margin-bottom: 16px;
}

.nav-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.nav-list li { margin-bottom: 10px; }

.nav-list a {
  color: #AEB4BE;
  text-decoration: none;
  font-size: 13px;
  transition: color 0.2s;
}

.nav-list a:hover { color: #00C2A1; }

.footer-bottom {
  border-top: 1px solid rgba(255, 255, 255, 0.08);
  padding: 16px 0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: #6B7280;
}

@media (max-width: 768px) {
  .footer-content { flex-direction: column; gap: 30px; }
  .footer-left { max-width: 100%; }
  .footer-right { flex-direction: column; gap: 24px; }
  .footer-bottom { flex-direction: column; gap: 8px; text-align: center; }
}
</style>
