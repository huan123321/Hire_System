<template>
  <div class="company-detail-container">
    <!-- 顶部导航 -->
    <AppHeader />

    <!-- 主要内容 -->
    <div class="main-content">
      <div class="container">
        <!-- 返回按钮 -->
        <div class="back-btn">
          <el-button icon="el-icon-arrow-left" @click="goBack">返回公司列表</el-button>
        </div>

        <div v-loading="loading" class="company-detail">
          <!-- 公司信息卡片 -->
          <div class="company-info-card">
            <div class="company-header">
              <div class="company-logo">
                <i class="fas fa-building" v-if="!company.logo"></i>
                <img :src="company.logo" :alt="company.name" v-if="company.logo">
              </div>
              <div class="company-name-section">
                <h1 class="company-name">{{ company.name }}</h1>
                <div class="company-tags">
                  <span class="tag" v-if="company.industry">{{ company.industry }}</span>
                  <span class="tag" v-if="company.scale">{{ company.scale }}</span>
                  <span class="tag" v-if="company.financeStage">{{ company.financeStage }}</span>
                </div>
                <div class="company-stats">
                  <span class="stat-item">
                    <i class="fas fa-eye"></i>
                    {{ viewCount }} 次浏览
                  </span>
                </div>
              </div>
            </div>

            <div class="company-detail-grid">
              <div class="detail-item">
                <div class="label">
                  <i class="fas fa-map-marker-alt"></i>
                  <span>公司地址</span>
                </div>
                <div class="value">{{ company.address }}</div>
              </div>

              <div class="detail-item">
                <div class="label">
                  <i class="fas fa-phone"></i>
                  <span>联系电话</span>
                </div>
                <div class="value">{{ company.phone }}</div>
              </div>

              <div class="detail-item">
                <div class="label">
                  <i class="fas fa-users"></i>
                  <span>公司规模</span>
                </div>
                <div class="value">{{ company.scale }}</div>
              </div>

              <div class="detail-item">
                <div class="label">
                  <i class="fas fa-industry"></i>
                  <span>所属行业</span>
                </div>
                <div class="value">{{ company.industry }}</div>
              </div>
            </div>

            <div class="company-description">
              <h3 class="section-title">
                <i class="fas fa-align-left"></i>
                公司简介
              </h3>
              <p class="description-content">{{ company.description || '暂无公司简介' }}</p>
            </div>

            <div class="company-actions">
              <el-button type="primary" size="large" @click="viewJobs">查看在招职位</el-button>
              <el-button type="success" size="large" icon="el-icon-chat-dot-round" @click="openChat">立即沟通</el-button>
              <el-button size="large" @click="followCompany">
                <i class="fas fa-heart" :class="{active: isFollowed}"></i>
                {{ isFollowed ? '已关注' : '关注公司' }}
              </el-button>
            </div>
          </div>

          <!-- 在招职位 -->
          <div class="jobs-section" v-if="jobs.length > 0">
            <h3 class="section-title">
              <i class="fas fa-briefcase"></i>
              在招职位
            </h3>
            <div class="job-list">
              <div class="job-card" v-for="job in jobs" :key="job.id">
                <div class="job-header">
                  <h4 class="job-title">{{ job.title }}</h4>
                  <span class="job-salary">{{ job.salary }}</span>
                </div>
                <div class="job-info">
                  <span><i class="fas fa-map-marker-alt"></i> {{ job.city }}</span>
                  <span><i class="fas fa-graduation-cap"></i> {{ job.education }}</span>
                  <span><i class="fas fa-clock"></i> {{ job.experience }}</span>
                </div>
                <el-button type="text" @click="applyJob(job.id)">立即申请</el-button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 聊天对话框 -->
    <el-dialog 
      title="与HR沟通" 
      :visible.sync="chatVisible"
      width="650px"
      :close-on-click-modal="false">
      <ChatBox 
        v-if="chatVisible"
        :partnerId="companyId"
        partnerType="company"
        :partnerName="company.name"
        @close="chatVisible = false"
      />
    </el-dialog>
  </div>
</template>

<script>
import AppHeader from '@/components/common/AppHeader.vue'
import ChatBox from '@/components/ChatBox.vue';
import chatWebSocket from '@/utils/websocket';

export default {
  name: 'CompanyDetail',
  components: {
    AppHeader,
    ChatBox
  },
  data() {
    return {
      companyId: null,
      company: {},
      jobs: [],
      loading: true,
      isFollowed: false,
      currentUser: null,
      chatVisible: false,
      viewCount: 0 // 公司浏览量
    }
  },
  created() {
    // 获取公司 ID
    this.companyId = this.$route.params.id;
    // 校验 ID 是否有效，无效则返回公司列表页
    if (!this.companyId || this.companyId === 'undefined' || isNaN(Number(this.companyId))) {
      this.$message.error('公司ID无效，请从公司列表进入');
      this.$router.replace('/ComPany');
      return;
    }
    // 获取当前用户
    const userStr = sessionStorage.getItem('user');
    if (userStr) {
      this.currentUser = JSON.parse(userStr);
      
      // 连接WebSocket
      const userType = 'user';
      chatWebSocket.connect(this.currentUser.id, userType, {
        companyId: this.currentUser.companyId
      });
    }
    // 加载公司详情
    this.loadCompanyDetail();
    // 加载公司职位
    this.loadCompanyJobs();
    // 检查是否已关注
    if (this.currentUser) {
      this.checkFollowStatus();
    }
    // 记录浏览
    this.recordView();
    // 加载浏览量
    this.loadViewCount();
  },
  beforeDestroy() {
    // 断开WebSocket连接
    chatWebSocket.disconnect();
  },
  methods: {
    async loadCompanyDetail() {
      try {
        this.loading = true;
        const response = await this.$axios.get(this.$apiBaseUrl + '/company/detail/' + this.companyId);
        
        if (response.data.code === 200) {
          this.company = response.data.data;
        } else {
          this.$message.error(response.data.msg || '加载公司详情失败');
        }
      } catch (error) {
        console.error('加载公司详情失败:', error);
        this.$message.error('加载公司详情失败');
      } finally {
        this.loading = false;
      }
    },

    async loadCompanyJobs() {
      try {
        const response = await this.$axios.get(this.$apiBaseUrl + '/job/company/' + encodeURIComponent(this.company.name));
        
        if (response.data.code === 200) {
          this.jobs = response.data.data || [];
        }
      } catch (error) {
        console.error('加载公司职位失败:', error);
      }
    },

    goBack() {
      this.$router.push('/ComPany');
    },

    goToHome() {
      const userStr = sessionStorage.getItem('user')
      const user = userStr ? JSON.parse(userStr) : null

      if (user && user.role === 1) {  // HR角色
        this.$router.push('/HrHomePage')
      } else {
        this.$router.push('/HomePage')
      }
    },

    goToPosition() {
      this.$router.push('/Position');
    },

    goToCompany() {
      this.$router.push('/ComPany');
    },

    goToCertification() {
      this.$router.push('/Certification');
    },

    goToPerson() {
      this.$router.push('/PerSon');
    },

    logout() {
      sessionStorage.removeItem('currentUser');
      this.$router.push('/');
    },

    viewJobs() {
      // 滚动到职位列表
      const jobsSection = document.querySelector('.jobs-section');
      if (jobsSection) {
        jobsSection.scrollIntoView({ behavior: 'smooth' });
      }
    },

    /**
     * 打开聊天窗口
     */
    openChat() {
      if (!this.currentUser) {
        this.$message.warning('请先登录');
        this.$router.push('/');
        return;
      }
      
      // 检查WebSocket连接状态
      if (!chatWebSocket.isConnected()) {
        this.$message.warning('正在连接聊天服务，请稍后...');
        return;
      }
      
      this.chatVisible = true;
    },

    followCompany() {
      if (!this.currentUser) {
        this.$message.warning('请先登录');
        this.$router.push('/');
        return;
      }

      const loading = this.$loading({
        lock: true,
        text: this.isFollowed ? '取消关注中...' : '关注中...',
        spinner: 'el-icon-loading',
        background: 'rgba(0, 0, 0, 0.7)'
      });

      // 调用后端 API 关注/取消关注
      const url = this.isFollowed 
        ? `${this.$apiBaseUrl}/user-follow-company/unfollow`
        : `${this.$apiBaseUrl}/user-follow-company/follow`;

      this.$axios.post(url, null, {
        params: {
          userId: this.currentUser.id,
          companyId: this.companyId
        }
      })
      .then(response => {
        loading.close();
        if (response.data.code === 200) {
          this.isFollowed = !this.isFollowed;
          const message = this.isFollowed ? '关注成功' : '已取消关注';
          this.$message.success(message);
          // 如果是关注操作，提示用户可以到个人中心查看
          if (this.isFollowed) {
            setTimeout(() => {
              this.$message({
                message: '您可以到个人中心查看已关注的公司',
                type: 'info',
                duration: 3000
              });
            }, 1000);
          }
        } else {
          this.$message.error(response.data.msg || '操作失败');
        }
      })
      .catch(error => {
        loading.close();
        console.error('关注公司失败:', error);
        this.$message.error('操作失败，请稍后重试');
      });
    },

    // 检查关注状态
    async checkFollowStatus() {
      if (!this.currentUser || !this.companyId) {
        return;
      }

      try {
        const response = await this.$axios.get(`${this.$apiBaseUrl}/user-follow-company/is-following`, {
          params: {
            userId: this.currentUser.id,
            companyId: this.companyId
          }
        });

        if (response.data.code === 200) {
          this.isFollowed = response.data.data;
        }
      } catch (error) {
        console.error('检查关注状态失败:', error);
      }
    },
    
    /**
     * 记录浏览
     */
    async recordView() {
      try {
        if (this.companyId) {
          // 获取当前用户 ID（如果已登录）
          const userId = this.currentUser ? this.currentUser.id : null;
          
          await this.$axios.post(
            this.$apiBaseUrl + '/company/' + this.companyId + '/view',
            {},
            {
              params: {
                userId: userId
              }
            }
          );
          console.log('公司浏览记录已保存');
        }
      } catch (error) {
        // 记录失败不影响页面显示
        console.error('记录浏览失败:', error);
      }
    },
    
    /**
     * 加载浏览量
     */
    async loadViewCount() {
      try {
        if (this.companyId) {
          const response = await this.$axios.get(
            this.$apiBaseUrl + '/company/' + this.companyId + '/view-count'
          );
          if (response.data.code === 200) {
            this.viewCount = response.data.data || 0;
          }
        }
      } catch (error) {
        console.error('加载浏览量失败:', error);
      }
    },
    
    /**
     * 申请职位
     */
    async applyJob(jobId) {
      // 检查是否登录
      if (!this.currentUser) {
        this.$message.warning('请先登录');
        this.$router.push('/');
        return;
      }
      
      // 确认申请
      this.$confirm('确定要申请这个职位吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const loading = this.$loading({
            lock: true,
            text: '正在提交申请...',
            spinner: 'el-icon-loading',
            background: 'rgba(0, 0, 0, 0.7)'
          });
          
          // 调用后端 API 申请职位
          const response = await this.$axios.post(
            this.$apiBaseUrl + '/job-application/apply',
            null,
            {
              params: {
                userId: this.currentUser.id,
                jobId: jobId
              }
            }
          );
          
          loading.close();
          
          if (response.data.code === 200) {
            this.$message.success('申请成功！HR 将尽快查看您的简历');
          } else {
            this.$message.error(response.data.msg || '申请失败，请稍后重试');
          }
        } catch (error) {
          console.error('申请职位失败:', error);
          this.$message.error('申请失败，请稍后重试');
        }
      }).catch(() => {
        this.$message.info('已取消申请');
      });
    },

    /**
     * 跳转到消息中心
     */
    goToMessageCenter() {
      this.$router.push({
        name: 'MessageCenter'
      });
    },
  }
}
</script>

<style scoped>
.company-detail-container {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.main-content {
  padding: 30px 0;
  max-width: 1200px;
  margin: 0 auto;
}

.container {
  padding: 0 20px;
}

.back-btn {
  margin-bottom: 20px;
}

.company-detail {
  background: white;
  border-radius: 8px;
  padding: 30px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.company-info-card {
  margin-bottom: 30px;
}

.company-header {
  display: flex;
  gap: 20px;
  margin-bottom: 30px;
  padding-bottom: 30px;
  border-bottom: 1px solid #e8e8e8;
}

.company-logo {
  width: 100px;
  height: 100px;
  border-radius: 8px;
  background-color: #f5f7fa;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #909399;
  font-size: 48px;
  flex-shrink: 0;
}

.company-logo img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 8px;
}

.company-name-section {
  flex: 1;
}

.company-name {
  font-size: 32px;
  color: #303133;
  margin: 0 0 15px 0;
}

.company-tags {
  display: flex;
  gap: 10px;
}

.tag {
  background-color: #f0f5ff;
  color: #3498db;
  padding: 5px 12px;
  border-radius: 4px;
  font-size: 14px;
}

.company-stats {
  margin-top: 15px;
  display: flex;
  gap: 20px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #909399;
  font-size: 14px;
}

.stat-item i {
  color: #67c23a;
}

.company-detail-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
  margin-bottom: 30px;
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.label {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #909399;
  font-size: 14px;
}

.label i {
  color: #3498db;
}

.value {
  color: #303133;
  font-size: 16px;
  font-weight: 500;
}

.company-description {
  margin-bottom: 30px;
}

.section-title {
  font-size: 20px;
  color: #303133;
  margin-bottom: 15px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.section-title i {
  color: #3498db;
}

.description-content {
  color: #606266;
  line-height: 1.8;
  white-space: pre-wrap;
}

.company-actions {
  display: flex;
  gap: 15px;
  padding-top: 20px;
  border-top: 1px solid #e8e8e8;
}

.jobs-section {
  margin-top: 30px;
}

.job-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.job-card {
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  padding: 20px;
  transition: all 0.3s;
}

.job-card:hover {
  border-color: #3498db;
  box-shadow: 0 2px 12px 0 rgba(52, 152, 219, 0.2);
}

.job-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.job-title {
  font-size: 18px;
  color: #303133;
  margin: 0;
}

.job-salary {
  color: #e74c3c;
  font-size: 18px;
  font-weight: bold;
}

.job-info {
  display: flex;
  gap: 20px;
  color: #909399;
  font-size: 14px;
  margin-bottom: 15px;
}

.job-info span {
  display: flex;
  align-items: center;
  gap: 5px;
}
</style>
