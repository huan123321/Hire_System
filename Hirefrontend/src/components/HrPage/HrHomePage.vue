<template>
  <div class="app-wrapper">
    <AppHeader />
<div class="hr-home-container">
    <!-- �������� -->


    <!-- Banner ���� -->
    <div class="banner">
      <div class="banner-content">
        <h1 class="banner-title">HR ����̨</h1>
        <p class="banner-subtitle">��Ч��Ƹ���ҵ����ʵ��˲�</p>
      </div>
    </div>

    <div class="container">
      <!-- ����ͳ�ƿ�Ƭ -->
      <div class="stats-cards">
        <div class="stat-card">
          <div class="stat-icon" style="background: #00C2A1;">
            <i class="fas fa-file-alt"></i>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.positionCount }}</div>
            <div class="stat-label">����ְλ</div>
          </div>
        </div>

        <div class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);">
            <i class="fas fa-users"></i>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.resumeCount }}</div>
            <div class="stat-label">�յ�����</div>
          </div>
        </div>

        <div class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);">
            <i class="fas fa-handshake"></i>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.interviewCount }}</div>
            <div class="stat-label">���԰���</div>
          </div>
        </div>

        <div class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);">
            <i class="fas fa-building"></i>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.companyCount }}</div>
            <div class="stat-label">�����˾</div>
          </div>
        </div>
      </div>

      <!-- ��Ҫ������ -->
      <div class="main-content">
        <!-- ������� -->
        <div class="content-left">
          <!-- ������ְλ -->
          <div class="section">
            <div class="section-header">
              <h2 class="section-title">
                <i class="fas fa-briefcase"></i>
                ����ְλ
              </h2>
              <el-button type="primary" @click="createPosition">
                <i class="fas fa-plus"></i>
                ������ְλ
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
                      {{ job.status === 'active' ? '��Ƹ��' : '�ѹر�' }}
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
                    {{ job.resumeCount || 0 }} �ݼ���
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- ���¼��� -->
          <div class="section">
            <div class="section-header">
              <h2 class="section-title">
                <i class="fas fa-file-alt"></i>
                ���¼���
              </h2>
              <el-button type="text" @click="viewAllResumes">�鿴ȫ�� ></el-button>
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
                  <span><i class="fas fa-map-marker-alt"></i> {{ resume.location }}</span>
                </div>
                <div class="resume-skills">
                  <el-tag
                      v-for="(skill, index) in resume.skills ? resume.skills.split(',') : []"
                      :key="index"
                      size="mini"
                      class="skill-tag">
                    {{ skill.trim() }}
                  </el-tag>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- �Ҳ����� -->
        <div class="sidebar">
          <!-- ������� -->
          <div class="widget quick-actions">
            <h3 class="widget-title">
              <i class="fas fa-thumbs-up"></i>
              �������
            </h3>
            <div class="action-list">
              <el-button type="primary" plain @click="createPosition">
                <i class="fas fa-plus"></i>
                ����ְλ
              </el-button>
              <el-button type="success" plain @click="viewResumes">
                <i class="fas fa-file-alt"></i>
                �鿴����
              </el-button>
              <el-button type="warning" plain @click="viewCandidates">
                <i class="fas fa-users"></i>
                ��ѡ�˹���
              </el-button>
              <el-button type="info" plain @click="viewInterviews">
                <i class="fas fa-handshake"></i>
                ���԰���
              </el-button>
            </div>
          </div>

          <!-- ��ƸС��ʿ -->
          <div class="widget recruitment-tips">
            <h3 class="widget-title">
              <i class="fas fa-lightbulb"></i>
              ��ƸС��ʿ
            </h3>
            <div class="tips-content">
              <ul class="tips-list">
                <li>����ְλ������������������ʵĺ�ѡ��</li>
                <li>��ʱ�ظ�����������߹�˾����</li>
                <li>��ȷн�ʷ�Χ�������Ͷ����</li>
                <li>���ڸ���ְλ��Ϣ���ֻ�Ծ��</li>
              </ul>
            </div>
          </div>

          <!-- ��˾��̬ -->
          <div class="widget company-news">
            <h3 class="widget-title">
              <i class="fas fa-newspaper"></i>
              ��˾��̬
            </h3>
            <div class="news-list">
              <div class="news-item">
                <div class="news-title">��˾��֤���ͨ��</div>
                <div class="news-time">2024-01-15</div>
              </div>
              <div class="news-item">
                <div class="news-title">����ְλ 3 ��</div>
                <div class="news-time">2024-01-14</div>
              </div>
              <div class="news-item">
                <div class="news-title">�յ����� 15 ��</div>
                <div class="news-time">2024-01-13</div>
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

export default {
  name: 'HrHomepage',
  components: {},
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
      latestResumes: []
    }
  },
  created() {
    // ��ȡ��ǰ�û���Ϣ
    const userStr = sessionStorage.getItem('user');
    if (userStr) {
      this.currentUser = JSON.parse(userStr);
    }
    // ���� HR �������
    this.loadHrData();
  },
  methods: {
    async loadHrData() {
      try {
        // ���� HR ��ְλ�б�
        await this.loadMyPositions();
        // �������¼���
        await this.loadLatestResumes();
        // ����ͳ������
        this.loadStats();
      } catch (error) {
        console.error('���� HR ����ʧ��:', error);
      }
    },

    async loadMyPositions() {
      try {
        if (!this.currentUser || !this.currentUser.companyName) {
          return;
        }

        const response = await this.$axios.get(this.$apiBaseUrl + '/job/company/' + encodeURIComponent(this.currentUser.companyName));
        
        if (response.data.code === 200) {
          this.myPositions = response.data.data || [];
          this.stats.positionCount = this.myPositions.length;
        }
      } catch (error) {
        console.error('����ְλ�б�ʧ��:', error);
      }
    },

    async loadLatestResumes() {
      try {
        // ������Ե��ú�� API ��ȡ���¼���
        // ��ʱʹ��ģ������
        this.latestResumes = [
          {
            id: 1,
            name: '����',
            position: 'Java ����ʦ',
            education: '����',
            experience: '3 ��',
            location: '����',
            skills: 'Java, Spring Boot, MySQL'
          },
          {
            id: 2,
            name: '����',
            position: 'ǰ�˹���ʦ',
            education: '����',
            experience: '2 ��',
            location: '�Ϻ�',
            skills: 'Vue, React, JavaScript'
          },
          {
            id: 3,
            name: '����',
            position: '��Ʒ����',
            education: '˶ʿ',
            experience: '5 ��',
            location: '����',
            skills: '��Ʒ��ƣ���Ŀ��������ݷ���'
          }
        ];
        this.stats.resumeCount = this.latestResumes.length;
      } catch (error) {
        console.error('���ؼ���ʧ��:', error);
      }
    },

    loadStats() {
      // ����ͳ������
      this.stats.interviewCount = 5; // ʾ������
    },

    createPosition() {
      // ��ת������ְλҳ��
      this.$router.push('/HrPosition');
    },

    viewJobDetail(job) {
      this.$router.push('/JobDetail/' + job.id);
    },

    viewResume(resume) {
      this.$message(`�鿴 ${resume.name} �ļ���`);
      // ������ת����������ҳ
    },

    viewAllResumes() {
      this.$message('�鿴���м���');
    },

    viewResumes() {
      // ��ת�� HR ��������
      this.$router.push({
        path: '/HrPerson',
        query: { tab: 'resumes' }
      });
    },

    viewCandidates() {
      // ��ת�� HR �������� - ��ѡ�˹���
      this.$router.push({
        path: '/HrPerson',
        query: { tab: 'candidates' }
      });
    },

    viewInterviews() {
      // ��ת�� HR �������� - ���Թ���
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
     * ��ת����Ϣ����
     */
    goToMessageCenter() {
      this.$router.push({
        name: 'MessageCenter'
      });
    },

    /**
     * ��ת�����Թ���ҳ��
     */
    goToInterviewManagement() {
      this.$router.push('/InterviewManagement');
    },

    logout() {
      sessionStorage.removeItem('user');
      this.$router.push('/');
      this.$message.success('���˳���¼');
    },

    formatTime(time) {
      if (!time) return 'δ֪';
      const date = new Date(time);
      const now = new Date();
      const diff = now - date;
      
      const minutes = Math.floor(diff / 60000);
      const hours = Math.floor(diff / 3600000);
      const days = Math.floor(diff / 86400000);
      
      if (minutes < 60) {
        return minutes + '����ǰ';
      } else if (hours < 24) {
        return hours + 'Сʱǰ';
      } else if (days < 7) {
        return days + '��ǰ';
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
  content: '??';
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
