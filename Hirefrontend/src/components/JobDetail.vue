<template>
  <div class="app-wrapper">
    <AppHeader />
<div class="job-detail-container">
    <!-- �������� -->

    <!-- ��Ҫ���� -->
    <div class="main-content">
      <div class="container">
        <!-- ���ذ�ť -->
        <div class="back-btn">
          <el-button icon="el-icon-arrow-left" @click="goBack">����ְλ�б�</el-button>
        </div>

        <div v-loading="loading" class="job-detail">
          <!-- ְλ��Ϣ��Ƭ -->
          <div class="job-info-card">
            <div class="job-header">
              <div class="job-title-section">
                <h1 class="job-title">{{ job.title }}</h1>
                <div class="job-tags">
                  <span class="tag" v-if="job.salary">{{ job.salary }}</span>
                  <span class="tag" v-if="job.city">{{ job.city }}</span>
                  <span class="tag" v-if="job.experience">{{ job.experience }}</span>
                  <span class="tag" v-if="job.education">{{ job.education }}</span>
                </div>
              </div>
              <div class="job-salary">{{ job.salary }}</div>
            </div>

            <div class="company-info-section">
              <div class="company-logo" @click="goToCompanyDetail">
                <i class="fas fa-building" v-if="!job.companyLogo"></i>
                <img :src="job.companyLogo" :alt="job.companyName" v-if="job.companyLogo" />
              </div>
              <div class="company-detail">
                <div class="company-name" @click="goToCompanyDetail">{{ job.companyName }}</div>
                <div class="company-desc">{{ job.industry || '��ҵ����' }} �� {{ job.companySize || '��ģ����' }}</div>
              </div>
              <el-button type="primary" size="large" @click="applyJob" :loading="applying">
                {{ hasApplied ? '������' : '����ְλ' }}
              </el-button>
              <el-button size="large" 
                :type="isFav ? 'warning' : 'default'"
                :icon="isFav ? 'el-icon-star-on' : 'el-icon-star-off'"
                @click="toggleFavorite">
                {{ isFav ? '���ղ�' : '�ղ�' }}
              </el-button>
            </div>
          </div>

          <!-- ְλ���� -->
          <div class="job-description-section">
            <h3 class="section-title">
              <i class="fas fa-align-left"></i>
              ְλ����
            </h3>
            <div class="description-content">
              <p>{{ job.description || '����ְλ����' }}</p>
            </div>
          </div>

          <!-- ְλҪ�� -->
          <div class="job-requirements-section">
            <h3 class="section-title">
              <i class="fas fa-list-ul"></i>
              ְλҪ��
            </h3>
            <div class="requirements-content">
              <div class="requirement-item" v-if="job.experience">
                <div class="requirement-label">
                  <i class="fas fa-user-tie"></i>
                  ����Ҫ��
                </div>
                <div class="requirement-value">{{ job.experience }}</div>
              </div>
              <div class="requirement-item" v-if="job.education">
                <div class="requirement-label">
                  <i class="fas fa-graduation-cap"></i>
                  ѧ��Ҫ��
                </div>
                <div class="requirement-value">{{ job.education }}</div>
              </div>
              <div class="requirement-item" v-if="job.city">
                <div class="requirement-label">
                  <i class="fas fa-map-marker-alt"></i>
                  �����ص�
                </div>
                <div class="requirement-value">{{ job.city }}</div>
              </div>
              <div class="requirement-item" v-if="job.tags">
                <div class="requirement-label">
                  <i class="fas fa-tags"></i>
                  ְλ��ǩ
                </div>
                <div class="requirement-value">
                  <span class="tag" v-for="(tag, index) in job.tags.split(',')" :key="index">{{ tag.trim() }}</span>
                </div>
              </div>
            </div>
          </div>

          <!-- ������Ϣ -->
          <div class="additional-info-section">
            <h3 class="section-title">
              <i class="fas fa-info-circle"></i>
              ������Ϣ
            </h3>
            <div class="info-grid">
              <div class="info-item">
                <div class="label">����ʱ��</div>
                <div class="value">{{ formatTime(job.createTime) }}</div>
              </div>
              <div class="info-item">
                <div class="label">����ʱ��</div>
                <div class="value">{{ formatTime(job.updateTime) }}</div>
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
  name: 'JobDetail',
  components: {},
  data() {
    return {
      jobId: null,
      job: {},
      loading: true,
      applying: false,
      hasApplied: false,
      currentUser: null,
      isFav: false
    }
  },
  created() {
    this.jobId = this.$route.params.id;
    // ��ȡ��ǰ�û�
    const userStr = sessionStorage.getItem('user');
    if (userStr) {
      this.currentUser = JSON.parse(userStr);
    }
    // ����ְλ����
    this.loadJobDetail();
    // ����Ƿ�������
    if (this.currentUser) {
      this.checkApplicationStatus();
      this.checkFavorite();
    }
  },
  methods: {
    async loadJobDetail() {
      try {
        this.loading = true;
        const response = await this.$axios.get(this.$apiBaseUrl + '/job/' + this.jobId);
        
        if (response.data.code === 200) {
          this.job = response.data.data;
        } else {
          this.$message.error(response.data.msg || '����ְλ����ʧ��');
        }
      } catch (error) {
        console.error('����ְλ����ʧ��:', error);
        this.$message.error('����ְλ����ʧ��');
      } finally {
        this.loading = false;
      }
    },

    async checkApplicationStatus() {
      try {
        const res = await this.$axios.get(this.$apiBaseUrl + '/job-application/user/' + this.currentUser.id);
        if (res.data.code === 200) {
          const applications = res.data.data || [];
          this.hasApplied = applications.some(app => app.jobId === this.jobId);
        }
      } catch (error) {
        console.error('�������״̬ʧ��:', error);
      }
    },
    async checkFavorite() {
      if (!this.currentUser) return;
      try {
        const res = await this.$axios.get(`${this.$apiBaseUrl}/favorite/check`, {
          params: { userId: this.currentUser.id, jobId: this.jobId }
        });
        if (res.data.code === 200) this.isFav = res.data.data;
      } catch (e) { /* ignore */ }
    },
    async toggleFavorite() {
      if (!this.currentUser) { this.$message.warning('���ȵ�¼'); return; }
      const url = this.isFav ? '/favorite/remove' : '/favorite/add';
      try {
        const res = await this.$axios.post(`${this.$apiBaseUrl}${url}`, {
          userId: this.currentUser.id, jobId: Number(this.jobId)
        });
        if (res.data.code === 200) {
          this.isFav = !this.isFav;
          this.$message.success(this.isFav ? '�ղسɹ�' : '��ȡ���ղ�');
        }
      } catch (e) { /* ignore */ }
    },

    async applyJob() {
      if (!this.currentUser) {
        this.$message.warning('���ȵ�¼');
        this.$router.push('/');
        return;
      }

      if (this.hasApplied) {
        this.$message.info('���Ѿ��������ְλ');
        return;
      }

      try {
        this.applying = true;
        
        // ��ȡĬ�ϼ���
        const resumeResponse = await this.$axios.get(this.$apiBaseUrl + '/resume/default/' + this.currentUser.id);
        
        if (resumeResponse.data.code !== 200 || !resumeResponse.data.data) {
          this.$message.warning('�������Ƽ���');
          this.$router.push('/EditResume');
          return;
        }

        const resumeId = resumeResponse.data.data.id;

        // ����ְλ - ʹ����ȷ�Ľӿ�
        const applyResponse = await this.$axios.post(
          this.$apiBaseUrl + '/job-application/apply',
          null,
          {
            params: {
              jobId: this.jobId,
              userId: this.currentUser.id,
              resumeId: resumeId
            }
          }
        );

        if (applyResponse.data.code === 200) {
          this.$message.success('����ɹ�');
          this.hasApplied = true;
        } else {
          this.$message.error(applyResponse.data.msg || '����ʧ��');
        }
      } catch (error) {
        console.error('����ְλʧ��:', error);
        this.$message.error('����ʧ�ܣ�' + (error.response?.data?.msg || error.message));
      } finally {
        this.applying = false;
      }
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

    goToCompanyDetail() {
      if (this.job.companyId) {
        this.$router.push('/CompanyDetail/' + this.job.companyId);
      } else {
        this.$message.info('��˾��Ϣ��������');
      }
    },

    goBack() {
      this.$router.push('/Position');
    },

    logout() {
      sessionStorage.removeItem('user');
      this.$router.push('/');
      this.$message.success('���˳���¼');
    },

    formatTime(time) {
      if (!time) return 'δ֪';
      const date = new Date(time);
      return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      });
    },

    /**
     * ��ת����Ϣ����
     */
    goToMessageCenter() {
      this.$router.push({
        name: 'MessageCenter'
      });
    }
  }
}
</script>

<style scoped>
.job-detail-container {
  min-height: 100vh;
  background: #f5f7fa;
}

.header {
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 100;

  .container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 20px;

    .header-content {
      display: flex;
      justify-content: space-between;
      align-items: center;
      height: 70px;

      .logo {
        display: flex;
        align-items: center;
        gap: 10px;
        font-size: 20px;
        font-weight: bold;
        color: #409EFF;

        i {
          font-size: 24px;
        }
      }

      .nav-menu {
        display: flex;
        gap: 10px;
        align-items: center;
      }
    }
  }
}

.main-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 30px 20px;

  .back-btn {
    margin-bottom: 20px;
  }

  .job-detail {
    background: #fff;
    border-radius: 8px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
    overflow: hidden;

    .job-info-card {
      padding: 30px;
      border-bottom: 1px solid #EBEEF5;

      .job-header {
        display: flex;
        justify-content: space-between;
        align-items: flex-start;
        margin-bottom: 25px;

        .job-title-section {
          flex: 1;

          .job-title {
            font-size: 28px;
            font-weight: bold;
            color: #303133;
            margin-bottom: 15px;
          }

          .job-tags {
            display: flex;
            gap: 10px;
            flex-wrap: wrap;

            .tag {
              display: inline-block;
              padding: 6px 12px;
              background: #f0f9ff;
              color: #409EFF;
              border-radius: 4px;
              font-size: 13px;
            }
          }
        }

        .job-salary {
          font-size: 24px;
          font-weight: bold;
          color: #f56c6c;
        }
      }

      .company-info-section {
        display: flex;
        align-items: center;
        gap: 20px;
        padding-top: 25px;
        border-top: 1px solid #EBEEF5;

        .company-logo {
          width: 80px;
          height: 80px;
          border-radius: 8px;
          background: #f5f7fa;
          display: flex;
          align-items: center;
          justify-content: center;
          color: #C0C4CC;
          font-size: 36px;
          cursor: pointer;
          transition: all 0.3s;

          &:hover {
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
          }

          img {
            width: 100%;
            height: 100%;
            object-fit: cover;
            border-radius: 8px;
          }
        }

        .company-detail {
          flex: 1;
          cursor: pointer;

          .company-name {
            font-size: 18px;
            font-weight: bold;
            color: #303133;
            margin-bottom: 8px;

            &:hover {
              color: #409EFF;
            }
          }

          .company-desc {
            font-size: 14px;
            color: #909399;
          }
        }
      }
    }

    .job-description-section,
    .job-requirements-section,
    .additional-info-section {
      padding: 30px;
      border-bottom: 1px solid #EBEEF5;

      &:last-child {
        border-bottom: none;
      }

      .section-title {
        font-size: 18px;
        font-weight: bold;
        color: #303133;
        margin-bottom: 20px;
        display: flex;
        align-items: center;
        gap: 8px;

        i {
          color: #409EFF;
        }
      }

      .description-content,
      .requirements-content {
        color: #606266;
        line-height: 1.8;
        font-size: 14px;
      }

      .description-content {
        p {
          margin: 0;
          white-space: pre-wrap;
        }
      }
    }

    .requirements-content {
      .requirement-item {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 15px 0;
        border-bottom: 1px solid #f0f0f0;

        &:last-child {
          border-bottom: none;
        }

        .requirement-label {
          display: flex;
          align-items: center;
          gap: 8px;
          color: #606266;
          font-weight: 500;

          i {
            color: #409EFF;
            width: 20px;
          }
        }

        .requirement-value {
          color: #303133;
          font-weight: 500;

          .tag {
            display: inline-block;
            padding: 4px 10px;
            background: #f0f9ff;
            color: #409EFF;
            border-radius: 4px;
            font-size: 13px;
            margin-right: 8px;
          }
        }
      }
    }

    .info-grid {
      display: grid;
      grid-template-columns: repeat(2, 1fr);
      gap: 20px;

      .info-item {
        display: flex;
        flex-direction: column;
        gap: 8px;

        .label {
          color: #909399;
          font-size: 13px;
        }

        .value {
          color: #606266;
          font-size: 14px;
          font-weight: 500;
        }
      }
    }
  }
}

.footer {
  background: #fff;
  padding: 30px 20px;
  margin-top: 50px;

  .container {
    max-width: 1200px;
    margin: 0 auto;

    .footer-links {
      text-align: center;
      color: #909399;
      font-size: 13px;
    }
  }
}
</style>
