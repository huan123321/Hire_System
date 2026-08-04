<template>
  <div class="resume-detail">
    <!-- 顶部导航 -->
    <AppHeader />

    <div class="container">
      <div class="main-content" v-loading="loading">
        <!-- 基本信息卡片 -->
        <div class="resume-card">
          <div class="card-header">
            <h2>{{ resumeData.name || '候选人简历' }}</h2>
          </div>

          <!-- 基本信息 -->
          <div class="section">
            <h3><i class="fas fa-user"></i> 基本信息</h3>
            <div class="info-grid">
              <div class="info-item">
                <span class="label">姓名：</span>
                <span class="value">{{ basicInfo.name }}</span>
              </div>
              <div class="info-item">
                <span class="label">性别：</span>
                <span class="value">{{ basicInfo.gender }}</span>
              </div>
              <div class="info-item">
                <span class="label">年龄：</span>
                <span class="value">{{ basicInfo.age }}岁</span>
              </div>
              <div class="info-item">
                <span class="label">所在地：</span>
                <span class="value">{{ basicInfo.location }}</span>
              </div>
              <div class="info-item">
                <span class="label">手机号：</span>
                <span class="value">{{ basicInfo.phone }}</span>
              </div>
              <div class="info-item">
                <span class="label">邮箱：</span>
                <span class="value">{{ basicInfo.email }}</span>
              </div>
            </div>
          </div>

          <!-- 教育背景 -->
          <div class="section" v-if="resumeData.education && resumeData.education.length > 0">
            <h3><i class="fas fa-graduation-cap"></i> 教育背景</h3>
            <div v-for="(edu, index) in resumeData.education" :key="index" class="timeline-item">
              <div class="timeline-content">
                <div class="timeline-header">
                  <h4>{{ edu.school }}</h4>
                  <span class="time">{{ formatDate(edu.startDate) }} - {{ formatDate(edu.endDate) }}</span>
                </div>
                <div class="timeline-body">
                  <p><strong>专业：</strong>{{ edu.major }}</p>
                  <p><strong>学历：</strong>{{ edu.degree }}</p>
                  <p v-if="edu.description"><strong>描述：</strong>{{ edu.description }}</p>
                </div>
              </div>
            </div>
          </div>

          <!-- 工作经历 -->
          <div class="section" v-if="resumeData.experience && resumeData.experience.length > 0">
            <h3><i class="fas fa-briefcase"></i> 工作经历</h3>
            <div v-for="(exp, index) in resumeData.experience" :key="index" class="timeline-item">
              <div class="timeline-content">
                <div class="timeline-header">
                  <h4>{{ exp.company }} - {{ exp.position }}</h4>
                  <span class="time">{{ formatDate(exp.startDate) }} - {{ formatDate(exp.endDate) }}</span>
                </div>
                <div class="timeline-body">
                  <p v-if="exp.description">{{ exp.description }}</p>
                </div>
              </div>
            </div>
          </div>

          <!-- 项目经验 -->
          <div class="section" v-if="resumeData.projects && resumeData.projects.length > 0">
            <h3><i class="fas fa-project-diagram"></i> 项目经验</h3>
            <div v-for="(project, index) in resumeData.projects" :key="index" class="timeline-item">
              <div class="timeline-content">
                <div class="timeline-header">
                  <h4>{{ project.name }}</h4>
                  <span class="time">{{ project.time }}</span>
                </div>
                <div class="timeline-body">
                  <p><strong>角色：</strong>{{ project.role }}</p>
                  <p v-if="project.description">{{ project.description }}</p>
                </div>
              </div>
            </div>
          </div>

          <!-- 空状态提示 -->
          <el-empty v-if="!hasContent" description="该候选人尚未填写简历信息"></el-empty>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import AppHeader from '@/components/common/AppHeader.vue'

export default {
  name: 'ResumeDetail',
  components: { AppHeader },
  data() {
    return {
      loading: false,
      resumeData: {},
      basicInfo: {}
    };
  },
  computed: {
    hasContent() {
      return this.resumeData.education?.length || 
             this.resumeData.experience?.length || 
             this.resumeData.projects?.length;
    }
  },
  mounted() {
    this.loadResume();
  },
  methods: {
    /**
     * 加载候选人简历数据
     */
    async loadResume() {
      try {
        this.loading = true;
        const userId = this.$route.params.userId;
        
        if (!userId) {
          this.$message.error('未找到候选人信息');
          return;
        }

        // 获取候选人的默认简历
        const response = await this.$axios.get(
          `${this.$apiBaseUrl}/resume/default/${userId}`
        );
        
        if (response.data.code === 200 && response.data.data) {
          const resume = response.data.data;
          
          // 解析简历数据
          this.resumeData = {
            id: resume.id,
            name: resume.name || '未命名简历',
            education: resume.education ? JSON.parse(resume.education) : [],
            experience: resume.experience ? JSON.parse(resume.experience) : [],
            projects: resume.projects ? JSON.parse(resume.projects) : []
          };
          
          // 解析基本信息
          if (resume.basicInfo) {
            this.basicInfo = JSON.parse(resume.basicInfo);
          }
        } else {
          this.$message.warning('候选人尚未创建简历');
        }
      } catch (error) {
        console.error('加载简历失败:', error);
        this.$message.error('加载简历失败：' + (error.response?.data?.msg || error.message));
      } finally {
        this.loading = false;
      }
    },

    /**
     * 格式化日期
     */
    formatDate(date) {
      if (!date) return '';
      if (date.includes('T')) {
        return date.split('T')[0];
      }
      return date;
    }
  }
}
</script>

<style scoped>
.resume-detail {
  min-height: 100vh;
  background: #f5f7fa;
}

.main-content {
  max-width: 1000px;
  margin: 30px auto;
  padding: 0 20px;
}

.resume-card {
  background: #fff;
  border-radius: 8px;
  padding: 40px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.card-header {
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 2px solid #409EFF;
}

.card-header h2 {
  margin: 0;
  color: #303133;
  font-size: 28px;
}

.section {
  margin-bottom: 40px;
}

.section h3 {
  margin-bottom: 20px;
  color: #303133;
  font-size: 20px;
  display: flex;
  align-items: center;
}

.section h3 i {
  margin-right: 10px;
  color: #409EFF;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 15px;
}

.info-item {
  display: flex;
  padding: 10px;
  background: #f5f7fa;
  border-radius: 4px;
}

.info-item .label {
  color: #909399;
  min-width: 80px;
  font-weight: 500;
}

.info-item .value {
  color: #303133;
  font-weight: 500;
}

.timeline-item {
  margin-bottom: 20px;
  padding-left: 20px;
  border-left: 3px solid #409EFF;
}

.timeline-item:last-child {
  margin-bottom: 0;
}

.timeline-content {
  background: #f5f7fa;
  padding: 15px;
  border-radius: 4px;
}

.timeline-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.timeline-header h4 {
  margin: 0;
  color: #303133;
  font-size: 16px;
}

.timeline-header .time {
  color: #909399;
  font-size: 14px;
}

.timeline-body {
  color: #606266;
  line-height: 1.6;
}

.timeline-body p {
  margin: 5px 0;
}
</style>
