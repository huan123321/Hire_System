<template>
  <div class="app-wrapper">
    <AppHeader />
<div class="company-detail-container">
    <!-- �������� -->

    <!-- ��Ҫ���� -->
    <div class="main-content">
      <div class="container">
        <!-- ���ذ�ť -->
        <div class="back-btn">
          <el-button icon="el-icon-arrow-left" @click="goBack">���ع�˾�б�</el-button>
        </div>

        <div v-loading="loading" class="company-detail">
          <!-- ��˾��Ϣ��Ƭ -->
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
                    {{ viewCount }} �����
                  </span>
                </div>
              </div>
            </div>

            <div class="company-detail-grid">
              <div class="detail-item">
                <div class="label">
                  <i class="fas fa-map-marker-alt"></i>
                  <span>��˾��ַ</span>
                </div>
                <div class="value">{{ company.address }}</div>
              </div>

              <div class="detail-item">
                <div class="label">
                  <i class="fas fa-phone"></i>
                  <span>��ϵ�绰</span>
                </div>
                <div class="value">{{ company.phone }}</div>
              </div>

              <div class="detail-item">
                <div class="label">
                  <i class="fas fa-users"></i>
                  <span>��˾��ģ</span>
                </div>
                <div class="value">{{ company.scale }}</div>
              </div>

              <div class="detail-item">
                <div class="label">
                  <i class="fas fa-industry"></i>
                  <span>������ҵ</span>
                </div>
                <div class="value">{{ company.industry }}</div>
              </div>
            </div>

            <div class="company-description">
              <h3 class="section-title">
                <i class="fas fa-align-left"></i>
                ��˾���
              </h3>
              <p class="description-content">{{ company.description || '���޹�˾���' }}</p>
            </div>

            <div class="company-actions">
              <el-button type="primary" size="large" @click="viewJobs">�鿴����ְλ</el-button>
              <el-button type="success" size="large" icon="el-icon-chat-dot-round" @click="openChat">������ͨ</el-button>
              <el-button size="large" @click="followCompany">
                <i class="fas fa-heart" :class="{active: isFollowed}"></i>
                {{ isFollowed ? '�ѹ�ע' : '��ע��˾' }}
              </el-button>
            </div>
          </div>

          <!-- ����ְλ -->
          <div class="jobs-section" v-if="jobs.length > 0">
            <h3 class="section-title">
              <i class="fas fa-briefcase"></i>
              ����ְλ
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
                <el-button type="text" @click="applyJob(job.id)">��������</el-button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- ����Ի��� -->
    <el-dialog 
      title="��HR��ͨ" 
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
    <SimpleFooter />
  </div>


</template>

<script>
import ChatBox from '@/components/ChatBox.vue';
import chatWebSocket from '@/utils/websocket';

export default {
  name: 'CompanyDetail',
  components: {
    
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
      viewCount: 0 // ��˾�����
    }
  },
  created() {
    // ��ȡ��˾ ID
    this.companyId = this.$route.params.id;
    // ��ȡ��ǰ�û�
    const userStr = sessionStorage.getItem('user');
    if (userStr) {
      this.currentUser = JSON.parse(userStr);
      
      // ����WebSocket
      const userType = 'user';
      chatWebSocket.connect(this.currentUser.id, userType, {
        companyId: this.currentUser.companyId
      });
    }
    // ���ع�˾����
    this.loadCompanyDetail();
    // ���ع�˾ְλ
    this.loadCompanyJobs();
    // ����Ƿ��ѹ�ע
    if (this.currentUser) {
      this.checkFollowStatus();
    }
    // ��¼���
    this.recordView();
    // ���������
    this.loadViewCount();
  },
  beforeDestroy() {
    // �Ͽ�WebSocket����
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
          this.$message.error(response.data.msg || '���ع�˾����ʧ��');
        }
      } catch (error) {
        console.error('���ع�˾����ʧ��:', error);
        this.$message.error('���ع�˾����ʧ��');
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
        console.error('���ع�˾ְλʧ��:', error);
      }
    },

    goBack() {
      this.$router.push('/ComPany');
    },

    goToHome() {
      const userStr = sessionStorage.getItem('user')
      const user = userStr ? JSON.parse(userStr) : null

      if (user && user.role === 1) {  // HR��ɫ
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
      // ������ְλ�б�
      const jobsSection = document.querySelector('.jobs-section');
      if (jobsSection) {
        jobsSection.scrollIntoView({ behavior: 'smooth' });
      }
    },

    /**
     * �����촰��
     */
    openChat() {
      if (!this.currentUser) {
        this.$message.warning('���ȵ�¼');
        this.$router.push('/');
        return;
      }
      
      // ���WebSocket����״̬
      if (!chatWebSocket.isConnected()) {
        this.$message.warning('������������������Ժ�...');
        return;
      }
      
      this.chatVisible = true;
    },

    followCompany() {
      if (!this.currentUser) {
        this.$message.warning('���ȵ�¼');
        this.$router.push('/');
        return;
      }

      const loading = this.$loading({
        lock: true,
        text: this.isFollowed ? 'ȡ����ע��...' : '��ע��...',
        spinner: 'el-icon-loading',
        background: 'rgba(0, 0, 0, 0.7)'
      });

      // ���ú�� API ��ע/ȡ����ע
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
          const message = this.isFollowed ? '��ע�ɹ�' : '��ȡ����ע';
          this.$message.success(message);
          // ����ǹ�ע��������ʾ�û����Ե��������Ĳ鿴
          if (this.isFollowed) {
            setTimeout(() => {
              this.$message({
                message: '�����Ե��������Ĳ鿴�ѹ�ע�Ĺ�˾',
                type: 'info',
                duration: 3000
              });
            }, 1000);
          }
        } else {
          this.$message.error(response.data.msg || '����ʧ��');
        }
      })
      .catch(error => {
        loading.close();
        console.error('��ע��˾ʧ��:', error);
        this.$message.error('����ʧ�ܣ����Ժ�����');
      });
    },

    // ����ע״̬
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
        console.error('����ע״̬ʧ��:', error);
      }
    },
    
    /**
     * ��¼���
     */
    async recordView() {
      try {
        if (this.companyId) {
          // ��ȡ��ǰ�û� ID������ѵ�¼��
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
          console.log('��˾�����¼�ѱ���');
        }
      } catch (error) {
        // ��¼ʧ�ܲ�Ӱ��ҳ����ʾ
        console.error('��¼���ʧ��:', error);
      }
    },
    
    /**
     * ���������
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
        console.error('���������ʧ��:', error);
      }
    },
    
    /**
     * ����ְλ
     */
    async applyJob(jobId) {
      // ����Ƿ��¼
      if (!this.currentUser) {
        this.$message.warning('���ȵ�¼');
        this.$router.push('/');
        return;
      }
      
      // ȷ������
      this.$confirm('ȷ��Ҫ�������ְλ��', '��ʾ', {
        confirmButtonText: 'ȷ��',
        cancelButtonText: 'ȡ��',
        type: 'warning'
      }).then(async () => {
        try {
          const loading = this.$loading({
            lock: true,
            text: '�����ύ����...',
            spinner: 'el-icon-loading',
            background: 'rgba(0, 0, 0, 0.7)'
          });
          
          // ���ú�� API ����ְλ
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
            this.$message.success('����ɹ���HR ������鿴���ļ���');
          } else {
            this.$message.error(response.data.msg || '����ʧ�ܣ����Ժ�����');
          }
        } catch (error) {
          console.error('����ְλʧ��:', error);
          this.$message.error('����ʧ�ܣ����Ժ�����');
        }
      }).catch(() => {
        this.$message.info('��ȡ������');
      });
    },

    /**
     * ��ת����Ϣ����
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

.header {
  background: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 100;
}

.header .container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 70px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 24px;
  font-weight: bold;
  color: #3498db;
}

.logo i {
  font-size: 28px;
}

.nav-menu {
  display: flex;
  gap: 15px;
  align-items: center;
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

.footer {
  background: #2c3e50;
  color: white;
  padding: 30px 0;
  margin-top: 50px;
}

.footer .container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.footer-links {
  text-align: center;
}
</style>
