<template>
  <div class="job-detail-container">
    <!-- 顶部导航 -->
    <AppHeader />

    <!-- 主要内容 -->
    <div class="main-content">
      <div class="container">
        <!-- 返回按钮 -->
        <div class="back-btn">
          <el-button icon="el-icon-arrow-left" @click="goBack">返回职位列表</el-button>
        </div>

        <div v-loading="loading" class="job-detail">
          <!-- 职位信息卡片 -->
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
                <div class="company-desc">{{ job.industry || '行业不限' }} · {{ job.companySize || '规模不限' }}</div>
              </div>
              <el-button type="primary" size="large" @click="applyJob" :loading="applying">
                {{ hasApplied ? '已申请' : '申请职位' }}
              </el-button>
              <el-button size="large"
                :type="isFav ? 'warning' : 'default'"
                :icon="isFav ? 'el-icon-star-on' : 'el-icon-star-off'"
                @click="toggleFavorite">
                {{ isFav ? '已收藏' : '收藏' }}
              </el-button>
            </div>
          </div>

          <!-- 职位详情 -->
          <div class="job-description-section">
            <h3 class="section-title">
              <i class="fas fa-align-left"></i>
              职位描述
            </h3>
            <div class="description-content">
              <p>{{ job.description || '暂无职位描述' }}</p>
            </div>
          </div>

          <!-- 职位要求 -->
          <div class="job-requirements-section">
            <h3 class="section-title">
              <i class="fas fa-list-ul"></i>
              职位要求
            </h3>
            <div class="requirements-content">
              <div class="requirement-item" v-if="job.experience">
                <div class="requirement-label">
                  <i class="fas fa-user-tie"></i>
                  经验要求
                </div>
                <div class="requirement-value">{{ job.experience }}</div>
              </div>
              <div class="requirement-item" v-if="job.education">
                <div class="requirement-label">
                  <i class="fas fa-graduation-cap"></i>
                  学历要求
                </div>
                <div class="requirement-value">{{ job.education }}</div>
              </div>
              <div class="requirement-item" v-if="job.city">
                <div class="requirement-label">
                  <i class="fas fa-map-marker-alt"></i>
                  工作地点
                </div>
                <div class="requirement-value">{{ job.city }}</div>
              </div>
              <div class="requirement-item" v-if="job.tags">
                <div class="requirement-label">
                  <i class="fas fa-tags"></i>
                  职位标签
                </div>
                <div class="requirement-value">
                  <span class="tag" v-for="(tag, index) in job.tags.split(',')" :key="index">{{ tag.trim() }}</span>
                </div>
              </div>
            </div>
          </div>

          <!-- 其他信息 -->
          <div class="additional-info-section">
            <h3 class="section-title">
              <i class="fas fa-info-circle"></i>
              其他信息
            </h3>
            <div class="info-grid">
              <div class="info-item">
                <div class="label">发布时间</div>
                <div class="value">{{ formatTime(job.createTime) }}</div>
              </div>
              <div class="info-item">
                <div class="label">更新时间</div>
                <div class="value">{{ formatTime(job.updateTime) }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

  </div>
</template>

<script>
import AppHeader from '@/components/common/AppHeader.vue'

export default {
  name: 'JobDetail',
  components: {
    AppHeader
  },
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
    // 获取职位 ID
    this.jobId = this.$route.params.id;
    // 获取当前用户
    const userStr = sessionStorage.getItem('user');
    if (userStr) {
      this.currentUser = JSON.parse(userStr);
    }
    // 加载职位详情
    this.loadJobDetail();
    // 检查是否已申请
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
          this.$message.error(response.data.msg || '加载职位详情失败');
        }
      } catch (error) {
        console.error('加载职位详情失败:', error);
        this.$message.error('加载职位详情失败');
      } finally {
        this.loading = false;
      }
    },

    async checkApplicationStatus() {
      try {
        const response = await this.$axios.get(this.$apiBaseUrl + '/job-application/check', {
          params: {
            jobId: this.jobId,
            userId: this.currentUser.id
          }
        });
        if (response.data.code === 200) {
          this.hasApplied = response.data.data;
        }
      } catch (error) {
        console.error('检查申请状态失败:', error);
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
      if (!this.currentUser) { this.$message.warning('请先登录'); return; }
      const url = this.isFav ? '/favorite/remove' : '/favorite/add';
      try {
        const res = await this.$axios.post(`${this.$apiBaseUrl}${url}`, {
          userId: this.currentUser.id, jobId: Number(this.jobId)
        });
        if (res.data.code === 200) {
          this.isFav = !this.isFav;
          this.$message.success(this.isFav ? '收藏成功' : '已取消收藏');
        }
      } catch (e) { /* ignore */ }
    },

    async applyJob() {
      if (!this.currentUser) {
        this.$message.warning('请先登录');
        this.$router.push('/');
        return;
      }

      if (this.hasApplied) {
        this.$message.info('您已经申请过该职位');
        return;
      }

      try {
        this.applying = true;
        
        // 获取默认简历
        const resumeResponse = await this.$axios.get(this.$apiBaseUrl + '/resume/default/' + this.currentUser.id);
        
        if (resumeResponse.data.code !== 200 || !resumeResponse.data.data) {
          this.$message.warning('请先完善简历');
          this.$router.push('/EditResume');
          return;
        }

        const resumeId = resumeResponse.data.data.id;

        // 申请职位 - 使用正确的接口
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
          this.$message.success('申请成功');
          this.hasApplied = true;
        } else {
          this.$message.error(applyResponse.data.msg || '申请失败');
        }
      } catch (error) {
        console.error('申请职位失败:', error);
        this.$message.error('申请失败：' + (error.response?.data?.msg || error.message));
      } finally {
        this.applying = false;
      }
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

    goToCompanyDetail() {
      if (this.job.companyId) {
        this.$router.push('/CompanyDetail/' + this.job.companyId);
      } else {
        this.$message.info('公司信息暂无详情');
      }
    },

    goBack() {
      this.$router.push('/Position');
    },

    logout() {
      sessionStorage.removeItem('user');
      this.$router.push('/');
      this.$message.success('已退出登录');
    },

    formatTime(time) {
      if (!time) return '未知';
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
     * 跳转到消息中心
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

</style>
