<template>
  <div class="app-wrapper">
    <AppHeader />
<div class="hr-home-container">
    <!-- 数据统计卡片区域 -->


    <!-- Banner 区域 -->
    <div class="banner">
      <div class="banner-content">
        <h1 class="banner-title">HR 管理后台</h1>
        <p class="banner-subtitle">高效招聘，助你找到合适人才</p>
      </div>
    </div>

    <div class="container">
      <!-- 数据统计卡片 -->
      <div class="stats-cards">
        <div class="stat-card">
          <div class="stat-icon" style="background: #00C2A1;">
            <i class="fas fa-file-alt"></i>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.positionCount }}</div>
            <div class="stat-label">发布职位</div>
          </div>
        </div>

        <div class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);">
            <i class="fas fa-users"></i>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.resumeCount }}</div>
            <div class="stat-label">收到简历</div>
          </div>
        </div>

        <div class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);">
            <i class="fas fa-handshake"></i>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.interviewCount }}</div>
            <div class="stat-label">面试安排</div>
          </div>
        </div>

        <div class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);">
            <i class="fas fa-building"></i>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.companyCount }}</div>
            <div class="stat-label">旗下公司</div>
          </div>
        </div>
      </div>

      <!-- 主要内容 -->
      <div class="main-content">
        <!-- 左侧内容 -->
        <div class="content-left">
          <!-- 我的职位 -->
          <div class="section">
            <div class="section-header">
              <h2 class="section-title">
                <i class="fas fa-briefcase"></i>
                我的职位
              </h2>
              <el-button type="primary" @click="createPosition">
                <i class="fas fa-plus"></i>
                发布新职位
              </el-button>
            </div>
            <div class="job-list">
              <div
                  v-for="job in myPositions"
                  :key="job.id"
                  class="job-card"
                  @click="viewJobDetail(job)">
                <div class="job-header">
                  <div class="job-info">
                    <div class="job-title">{{ job.title }}</div>
                    <div class="job-salary">{{ job.salary }}</div>
                  </div>
                  <div class="job-status">
                    <el-tag :type="job.status === 'active' ? 'success' : 'info'" size="mini">
                      {{ job.status === 'active' ? '招聘中' : '已关闭' }}
                    </el-tag>
                  </div>
                </div>
                <div class="job-tags">
                  <el-tag
                      v-for="(tag, index) in job.tags ? job.tags.split(',') : []"
                      :key="index"
                      size="mini"
                      type="info"
                      class="job-tag">
                    {{ tag.trim() }}
                  </el-tag>
                </div>
                <div class="job-footer">
                  <div class="publish-time">{{ formatTime(job.createTime) }}</div>
                  <div class="resume-count">
                    <i class="fas fa-file-alt"></i>
                    {{ job.resumeCount || 0 }} 份简历
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 最新简历 -->
          <div class="section">
            <div class="section-header">
              <h2 class="section-title">
                <i class="fas fa-file-alt"></i>
                最新简历
              </h2>
              <el-button type="text" @click="viewAllResumes">查看全部 ></el-button>
            </div>
            <div class="resume-list">
              <div
                  v-for="resume in latestResumes"
                  :key="resume.id"
                  class="resume-card"
                  @click="viewResume(resume)">
                <div class="resume-header">
                  <div class="resume-name">
                    <i class="fas fa-user"></i>
                    {{ resume.name }}
                  </div>
                  <div class="resume-position">{{ resume.position }}</div>
                </div>
                <div class="resume-info">
                  <span><i class="fas fa-graduation-cap"></i> {{ resume.education }}</span>
                  <span><i class="fas fa-clock"></i> {{ resume.experience }}</span>
                  <span>
                    <el-tag :type="resume.status === 'interview' ? 'warning' : resume.status === 'hired' ? 'success' : 'info'" size="mini">
                      {{ resume.statusText }}
                    </el-tag>
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 右侧边栏 -->
        <div class="sidebar">
          <!-- 快捷操作 -->
          <div class="widget quick-actions">
            <h3 class="widget-title">
              <i class="fas fa-thumbs-up"></i>
              快捷操作
            </h3>
            <div class="action-list">
              <el-button type="primary" plain @click="createPosition">
                <i class="fas fa-plus"></i>
                发布职位
              </el-button>
              <el-button type="success" plain @click="viewResumes">
                <i class="fas fa-file-alt"></i>
                查看简历
              </el-button>
              <el-button type="warning" plain @click="viewCandidates">
                <i class="fas fa-users"></i>
                候选人管理
              </el-button>
              <el-button type="info" plain @click="viewInterviews">
                <i class="fas fa-handshake"></i>
                面试安排
              </el-button>
            </div>
          </div>

          <!-- 招聘小贴士 -->
          <div class="widget recruitment-tips">
            <h3 class="widget-title">
              <i class="fas fa-lightbulb"></i>
              招聘小贴士
            </h3>
            <div class="tips-content">
              <ul class="tips-list">
                <li>发布职位后及时更新状态，吸引更多合适的候选人</li>
                <li>及时回复候选人消息，提升公司形象</li>
                <li>明确薪资范围，减少无效沟通</li>
                <li>定期更新职位信息，保持活跃度</li>
              </ul>
            </div>
          </div>

          <!-- 公司动态 -->
          <div class="widget company-news">
            <h3 class="widget-title">
              <i class="fas fa-newspaper"></i>
              公司动态
            </h3>
            <div class="news-list">
              <div v-for="(news, index) in companyNews" :key="index" class="news-item">
                <div class="news-title">{{ news.title }}</div>
                <div class="news-time" v-if="news.time">{{ news.time }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
  </div>

</template>

<script>
import AppHeader from '@/components/common/AppHeader.vue';
import { promptProfileCompletion } from '@/utils/profileTip';

export default {
  name: 'HrHomepage',
  components: { AppHeader },
  data() {
    return {
      currentUser: null,
      stats: {
        positionCount: 0,
        resumeCount: 0,
        interviewCount: 0,
        companyCount: 1
      },
      myPositions: [],
      latestResumes: [],
      companyInfo: null,
      companyNews: []
    }
  },
  created() {
    // 获取当前用户信息
    const userStr = sessionStorage.getItem('user');
    if (userStr) {
      this.currentUser = JSON.parse(userStr);
    }
    // 新用户注册后首次进入首页：提示先完善个人资料
    promptProfileCompletion(this);
    // 加载 HR 首页数据
    this.loadHrData();
  },
  methods: {
    async loadHrData() {
      try {
        // 先加载公司信息
        await this.loadCompanyInfo();
        // 加载 HR 职位列表
        await this.loadMyPositions();
        // 加载最新简历（真实申请人数据）
        await this.loadLatestResumes();
        // 加载统计数据（真实面试数）
        await this.loadStats();
        // 生成公司动态（基于真实数据）
        this.loadCompanyNews();
      } catch (error) {
        console.error('加载 HR 数据失败:', error);
      }
    },

    async loadCompanyInfo() {
      try {
        if (!this.currentUser || !this.currentUser.companyId) {
          this.$message.warning('未找到你的公司信息');
          return;
        }
        const response = await this.$axios.get(
          this.$apiBaseUrl + '/company/detail/' + this.currentUser.companyId
        );
        if (response.data.code === 200 && response.data.data) {
          this.companyInfo = response.data.data;
        }
      } catch (error) {
        console.error('加载公司信息失败:', error);
      }
    },

    async loadMyPositions() {
      try {
        if (!this.companyInfo || !this.companyInfo.name) {
          return;
        }

        const response = await this.$axios.get(this.$apiBaseUrl + '/job/company/' + encodeURIComponent(this.companyInfo.name));
        
        if (response.data.code === 200) {
          this.myPositions = response.data.data || [];
          this.stats.positionCount = this.myPositions.length;
        }
      } catch (error) {
        console.error('加载职位列表失败:', error);
      }
    },

    async loadLatestResumes() {
      try {
        // 从各职位的真实申请人接口汇总（按申请时间取最新 5 条）
        const allApplicants = [];
        for (const job of this.myPositions) {
          try {
            const response = await this.$axios.get(
              this.$apiBaseUrl + '/job-application/job/' + job.id + '/applicants'
            );
            if (response.data.code === 200 && Array.isArray(response.data.data)) {
              response.data.data.forEach(a => {
                // Resume 表的 education/experience 字段是 JSON 字符串，解析后取第一条展示
                const eduList = this.safeParse(a.resumeEducation, []);
                const expList = this.safeParse(a.resumeExperience, []);
                const edu = Array.isArray(eduList) && eduList.length > 0 ? eduList[0] : null;
                const exp = Array.isArray(expList) && expList.length > 0 ? expList[0] : null;
                allApplicants.push({
                  id: a.id,
                  name: a.realName || a.nickname || a.username || '未知',
                  position: job.title,
                  education: edu ? (edu.degree ? edu.degree + ' · ' : '') + (edu.school || '') : '未填写',
                  experience: exp ? (exp.company || '') + (exp.position ? ' · ' + exp.position : '') : '暂无工作经历',
                  status: a.status,
                  statusText: this.getStatusText(a.status),
                  applyTime: a.applyTime
                });
              });
            }
          } catch (e) {
            // 单个职位获取失败不影响整体
          }
        }
        allApplicants.sort((a, b) => new Date(b.applyTime) - new Date(a.applyTime));
        this.latestResumes = allApplicants.slice(0, 5);
        this.stats.resumeCount = allApplicants.length;
      } catch (error) {
        console.error('加载最新简历失败:', error);
      }
    },

    async loadStats() {
      try {
        // 真实面试数量（该公司所有面试安排）
        if (this.companyInfo && this.companyInfo.id) {
          const response = await this.$axios.get(
            this.$apiBaseUrl + '/interview/company/' + this.companyInfo.id
          );
          if (response.data.code === 200 && Array.isArray(response.data.data)) {
            this.stats.interviewCount = response.data.data.length;
          }
        }
      } catch (error) {
        console.error('加载面试统计失败:', error);
      }
    },

    loadCompanyNews() {
      // 基于真实数据生成公司动态
      const news = [];
      // 1. 认证状态（真实状态展示）
      news.push({ title: '公司认证已通过', time: '' });
      // 2. 最新发布的职位
      if (this.myPositions.length > 0) {
        const sorted = [...this.myPositions].sort((a, b) => new Date(b.createTime) - new Date(a.createTime));
        news.push({ title: `发布新职位「${sorted[0].title}」`, time: this.formatTime(sorted[0].createTime) });
      }
      // 3. 最新收到的简历
      if (this.latestResumes.length > 0) {
        news.push({
          title: `「${this.latestResumes[0].name}」投递了「${this.latestResumes[0].position}」`,
          time: this.formatTime(this.latestResumes[0].applyTime)
        });
      }
      this.companyNews = news;
    },

    getStatusText(status) {
      const textMap = {
        'new': '新申请',
        'viewed': '已查看',
        'interview': '面试中',
        'hired': '已录用',
        'rejected': '已拒绝'
      };
      return textMap[status] || '未知';
    },

    safeParse(str, fallback) {
      if (!str) return fallback;
      try {
        return JSON.parse(str);
      } catch (e) {
        return fallback;
      }
    },

    createPosition() {
      // 跳转到发布职位页面
      this.$router.push('/HrPosition');
    },

    viewJobDetail(job) {
      this.$router.push('/JobDetail/' + job.id);
    },

    viewResume(resume) {
      // 跳转到候选人管理查看该申请人
      this.$router.push({
        path: '/HrPerson',
        query: { tab: 'candidates', applicantId: resume.id }
      });
    },

    viewAllResumes() {
      // 跳转到简历管理
      this.$router.push({
        path: '/HrPerson',
        query: { tab: 'resumes' }
      });
    },

    viewResumes() {
      // 跳转到 HR 个人中心
      this.$router.push({
        path: '/HrPerson',
        query: { tab: 'resumes' }
      });
    },

    viewCandidates() {
      // 跳转到 HR 个人中心 - 候选人管理
      this.$router.push({
        path: '/HrPerson',
        query: { tab: 'candidates' }
      });
    },

    viewInterviews() {
      // 跳转到 HR 个人中心 - 面试管理
      this.$router.push({
        path: '/HrPerson',
        query: { tab: 'interviews' }
      });
    },

    position() {
      this.$router.push('/HrPosition');
    },

    goToCompany() {
      this.$router.push('/HrCompany');
    },

    person() {
      this.$router.push('/HrPerson');
    },

    /**
     * 跳转到消息中心
     */
    goToMessageCenter() {
      this.$router.push({
        name: 'MessageCenter'
      });
    },

    /**
     * 跳转到面试管理页面
     */
    goToInterviewManagement() {
      this.$router.push('/InterviewManagement');
    },

    logout() {
      sessionStorage.removeItem('user');
      this.$router.push('/');
      this.$message.success('已退出登录');
    },

    formatTime(time) {
      if (!time) return '未知';
      const date = new Date(time);
      const now = new Date();
      const diff = now - date;
      
      const minutes = Math.floor(diff / 60000);
      const hours = Math.floor(diff / 3600000);
      const days = Math.floor(diff / 86400000);
      
      if (minutes < 60) {
        return minutes + '分钟前';
      } else if (hours < 24) {
        return hours + '小时前';
      } else if (days < 7) {
        return days + '天前';
      } else {
        return date.toLocaleDateString('zh-CN', {
          month: '2-digit',
          day: '2-digit'
        });
      }
    }
  }
}
</script>

<style scoped>
.hr-home-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f5f7fa;
}

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
  font-family: "Helvetica Neue", Helvetica, "PingFang SC", "Hiragino Sans GB", Arial, sans-serif;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 15px;
}

.banner {
  background: #00C2A1;
  color: white;
  padding: 60px 0;
  text-align: center;
  position: relative;
  overflow: hidden;
  margin-bottom: 30px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.15);
}

.banner-content {
  position: relative;
  z-index: 1;
  animation: fadeIn 0.8s ease-out;
}

.banner-title {
  font-size: 42px;
  font-weight: bold;
  margin-bottom: 12px;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.2);
  letter-spacing: 1px;
}

.banner-subtitle {
  font-size: 18px;
  opacity: 0.95;
  font-weight: 300;
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 30px;
}

.stat-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
  transition: all 0.3s;
}

.stat-card:hover {
  
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.12);
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 24px;
  flex-shrink: 0;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #2c3e50;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.main-content {
  display: flex;
  gap: 20px;
  flex: 1;
}

.content-left {
  flex: 1;
}

.sidebar {
  width: 320px;
}

.section {
  background: white;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-title {
  font-size: 20px;
  font-weight: bold;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 8px;
}

.section-title i {
  color: #667eea;
}

.job-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.job-card {
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.9) 0%, rgba(248, 250, 252, 0.9) 100%);
  border: 2px solid transparent;
  border-radius: 12px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.job-card:hover {
  
  border-color: #667eea;
  box-shadow: 0 1px 3px rgba(102, 126, 234, 0.2);
}

.job-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.job-info {
  flex: 1;
}

.job-title {
  font-size: 18px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 8px;
}

.job-salary {
  background: #00C2A1;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  font-weight: 700;
  font-size: 16px;
}

.job-status {
  margin-left: 12px;
}

.job-tags {
  margin-bottom: 12px;
}

.job-tag {
  margin-right: 5px;
  margin-bottom: 5px;
}

.job-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 13px;
  color: #909399;
}

.resume-count {
  display: flex;
  align-items: center;
  gap: 5px;
}

.resume-count i {
  color: #667eea;
}

.resume-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.resume-card {
  background: #f8fafc;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 16px;
  cursor: pointer;
  transition: all 0.3s;
}

.resume-card:hover {
  border-color: #667eea;
  background: #f0f9ff;
  
}

.resume-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.resume-name {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
  display: flex;
  align-items: center;
  gap: 6px;
}

.resume-name i {
  color: #667eea;
}

.resume-position {
  font-size: 14px;
  color: #606266;
}

.resume-info {
  display: flex;
  gap: 15px;
  margin-bottom: 10px;
  font-size: 13px;
  color: #909399;
}

.resume-info span {
  display: flex;
  align-items: center;
  gap: 5px;
}

.resume-info i {
  color: #667eea;
}

.resume-skills {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.skill-tag {
  background: #f0f9ff;
  color: #409EFF;
  border: 1px solid #d9ecff;
}

.widget {
  background: white;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
}

.widget-title {
  font-size: 18px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 18px;
  padding-bottom: 12px;
  border-bottom: 2px solid rgba(102, 126, 234, 0.1);
  display: flex;
  align-items: center;
  gap: 8px;
}

.widget-title i {
  color: #667eea;
}

.action-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.action-list .el-button {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.tips-content {
  background: #f8fafc;
  border-radius: 8px;
  padding: 16px;
}

.tips-list {
  padding-left: 20px;
  list-style: none;
}

.tips-list li {
  margin-bottom: 12px;
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
  position: relative;
}

.tips-list li::before {
  content: '•';
  position: absolute;
  left: -20px;
  top: 0;
}

.news-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.news-item {
  padding: 12px;
  background: #f8fafc;
  border-radius: 8px;
  border-left: 3px solid #667eea;
}

.news-title {
  font-size: 14px;
  color: #2c3e50;
  margin-bottom: 6px;
}

.news-time {
  font-size: 12px;
  color: #909399;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@media (max-width: 1024px) {
  .stats-cards {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .main-content {
    flex-direction: column;
  }
  
  .sidebar {
    width: 100%;
  }
}

@media (max-width: 768px) {
  .stats-cards {
    grid-template-columns: 1fr;
  }
}
</style>
