<template>
  <div class="app-wrapper">
    <AppHeader />
<div class="hr-company-container">
    <!-- 公司信息管理 -->


    <div class="container">
      <!-- 页面头部 -->
      <div class="page-header">
        <h1 class="page-title">
          <i class="fas fa-building"></i>
          公司信息管理
        </h1>
        <p class="page-subtitle">查看并维护公司信息</p>
      </div>

      <!-- 加载状态 -->
      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="10" animated />
      </div>

      <!-- 公司信息展示 -->
      <div v-else-if="companyInfo" class="company-management">
        <!-- 公司基本信息卡片 -->
        <div class="company-card">
          <div class="card-header">
            <h2 class="card-title">
              <i class="fas fa-info-circle"></i>
              公司基本信息
            </h2>
            <el-button 
              type="primary" 
              size="small"
              @click="toggleEdit"
              :icon="isEditing ? 'el-icon-close' : 'el-icon-edit'">
              {{ isEditing ? '取消编辑' : '编辑信息' }}
            </el-button>
          </div>

          <div class="card-body">
            <!-- 查看模式 -->
            <div v-if="!isEditing" class="info-display">
              <div class="info-row">
                <div class="info-item">
                  <label>公司名称：</label>
                  <span>{{ companyInfo.name }}</span>
                </div>
                <div class="info-item">
                  <label>所属行业：</label>
                  <span>{{ companyInfo.industry }}</span>
                </div>
              </div>
              <div class="info-row">
                <div class="info-item">
                  <label>公司规模：</label>
                  <span>{{ companyInfo.scale }}</span>
                </div>
                <div class="info-item">
                  <label>联系电话：</label>
                  <span>{{ companyInfo.phone }}</span>
                </div>
              </div>
              <div class="info-row">
                <div class="info-item full-width">
                  <label>公司地址：</label>
                  <span>{{ companyInfo.address }}</span>
                </div>
              </div>
              <div class="info-row">
                <div class="info-item full-width">
                  <label>公司简介：</label>
                  <div class="description-text">{{ companyInfo.description || '暂无简介' }}</div>
                </div>
              </div>
            </div>

            <!-- 编辑模式 -->
            <el-form v-else :model="editForm" :rules="rules" ref="editForm" label-width="100px" class="edit-form">
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="公司名称" prop="name">
                    <el-input v-model="editForm.name" placeholder="请输入公司名称"></el-input>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="所属行业" prop="industry">
                    <el-select v-model="editForm.industry" placeholder="请选择所属行业" style="width: 100%">
                      <el-option label="互联网" value="互联网"></el-option>
                      <el-option label="电子商务" value="电子商务"></el-option>
                      <el-option label="金融" value="金融"></el-option>
                      <el-option label="教育" value="教育"></el-option>
                      <el-option label="房地产" value="房地产"></el-option>
                      <el-option label="通信技术" value="通信技术"></el-option>
                      <el-option label="制造业" value="制造业"></el-option>
                      <el-option label="服务业" value="服务业"></el-option>
                    </el-select>
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="公司规模" prop="scale">
                    <el-select v-model="editForm.scale" placeholder="请选择规模" style="width: 100%">
                      <el-option label="少于50人" value="少于50人"></el-option>
                      <el-option label="50-200人" value="50-200人"></el-option>
                      <el-option label="200-500人" value="200-500人"></el-option>
                      <el-option label="500人以上" value="500人以上"></el-option>
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="联系电话" prop="phone">
                    <el-input v-model="editForm.phone" placeholder="请输入联系电话"></el-input>
                  </el-form-item>
                </el-col>
              </el-row>
              <el-form-item label="公司地址" prop="address">
                <el-input v-model="editForm.address" placeholder="请输入公司地址"></el-input>
              </el-form-item>
              <el-form-item label="公司简介" prop="description">
                <el-input 
                  v-model="editForm.description" 
                  type="textarea" 
                  :rows="4"
                  placeholder="请输入公司简介">
                </el-input>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="saveCompany" :loading="saving">保存修改</el-button>
                <el-button @click="cancelEdit">取消</el-button>
              </el-form-item>
            </el-form>
          </div>
        </div>

        <!-- 统计数据卡片 -->
        <div class="stats-section">
          <div class="stat-card" @click="viewJobs">
            <div class="stat-icon" style="background: #00C2A1;">
              <i class="fas fa-briefcase"></i>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ jobCount }}</div>
              <div class="stat-label">发布职位</div>
            </div>
          </div>
          <div class="stat-card" @click="viewApplicants">
            <div class="stat-icon" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);">
              <i class="fas fa-users"></i>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ applicantCount }}</div>
              <div class="stat-label">收到申请</div>
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-icon" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);">
              <i class="fas fa-eye"></i>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ viewCount }}</div>
              <div class="stat-label">公司浏览量</div>
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-icon" style="background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);">
              <i class="fas fa-heart"></i>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ followCount }}</div>
              <div class="stat-label">关注人数</div>
            </div>
          </div>
        </div>

        <!-- 快捷操作 -->
        <div class="quick-actions">
          <h3 class="section-title">
            <i class="fas fa-thumbs-up"></i>
            快捷操作
          </h3>
          <div class="action-buttons">
            <el-button type="primary" @click="createPosition">
              <i class="fas fa-plus"></i>
              发布新职位
            </el-button>
            <el-button type="success" @click="viewJobs">
              <i class="fas fa-list"></i>
              查看职位
            </el-button>
            <el-button type="warning" @click="viewApplicants">
              <i class="fas fa-users"></i>
              查看候选人
            </el-button>
            <el-button type="info" @click="viewCompanyDetail">
              <i class="fas fa-eye"></i>
              预览公司页
            </el-button>
          </div>
        </div>

        <!-- 最新申请人 -->
        <div class="applicants-section" v-if="recentApplicants.length > 0">
          <h3 class="section-title">
            <i class="fas fa-user-clock"></i>
            最新申请
          </h3>
          <div class="applicant-list">
            <div 
              v-for="applicant in recentApplicants" 
              :key="applicant.id"
              class="applicant-item">
              <div class="applicant-avatar">
                <i class="fas fa-user-circle"></i>
              </div>
              <div class="applicant-info">
                <div class="applicant-name">{{ applicant.userName }}</div>
                <div class="applicant-position">
                  申请职位：{{ applicant.jobTitle }}
                  <el-tag 
                    :type="getStatusType(applicant.status)" 
                    size="mini" 
                    style="margin-left: 8px;">
                    {{ getStatusText(applicant.status) }}
                  </el-tag>
                </div>
                <div class="applicant-time">{{ formatTime(applicant.applyTime) }}</div>
              </div>
              <div class="applicant-actions">
                <el-button 
                  type="primary" 
                  size="small" 
                  @click="viewApplicantDetail(applicant)">
                  查看详情
                </el-button>
              </div>
            </div>
          </div>
        </div>

        <!-- 公司职位列表 -->
        <div class="jobs-section" v-if="companyJobs.length > 0">
          <h3 class="section-title">
            <i class="fas fa-briefcase"></i>
            公司职位
          </h3>
          <div class="job-list">
            <div 
              v-for="job in companyJobs" 
              :key="job.id"
              class="job-item"
              @click="viewJobDetail(job)">
              <div class="job-title-row">
                <div class="job-title">{{ job.title }}</div>
                <el-tag :type="job.status === 'active' ? 'success' : 'info'" size="mini">
                  {{ job.status === 'active' ? '招聘中' : '已关闭' }}
                </el-tag>
              </div>
              <div class="job-info-row">
                <span><i class="fas fa-money-bill-wave"></i> {{ job.salary }}</span>
                <span><i class="fas fa-map-marker-alt"></i> {{ job.city }}</span>
                <span><i class="fas fa-graduation-cap"></i> {{ job.education }}</span>
                <span><i class="fas fa-clock"></i> {{ job.experience }}</span>
              </div>
              <div class="job-footer">
                <span class="apply-count">
                  <i class="fas fa-user-friends"></i>
                  {{ job.applyCount || 0 }} 份申请
                </span>
                <span class="publish-time">{{ formatTime(job.createTime) }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 无公司信息时提示 -->
      <div v-else class="empty-state">
        <i class="fas fa-building empty-icon"></i>
        <h3>暂无公司信息</h3>
        <p>请联系管理员为你创建公司信息</p>
        <el-button type="primary" @click="goToHome">返回首页</el-button>
      </div>
    </div>
  </div>
  </div>

</template>

<script>
import AppHeader from '@/components/common/AppHeader.vue';

export default {
  name: 'HrCompany',
  components: { AppHeader },
  data() {
    return {
      currentUser: null,
      companyInfo: null,
      loading: true,
      isEditing: false,
      saving: false,
      editForm: {
        name: '',
        industry: '',
        scale: '',
        phone: '',
        address: '',
        description: ''
      },
      rules: {
        name: [
          { required: true, message: '请输入公司名称', trigger: 'blur' }
        ],
        industry: [
          { required: true, message: '请选择所属行业', trigger: 'change' }
        ],
        scale: [
          { required: true, message: '请选择公司规模', trigger: 'change' }
        ],
        phone: [
          { required: true, message: '请输入联系电话', trigger: 'blur' },
          { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
        ],
        address: [
          { required: true, message: '请输入公司地址', trigger: 'blur' }
        ]
      },
      // 统计数据
      jobCount: 0,
      applicantCount: 0,
      viewCount: 0,
      followCount: 0,
      // 最新申请人
      recentApplicants: [],
      // 公司职位列表
      companyJobs: [],
    }
  },
  created() {
    // 获取当前用户信息
    const userStr = sessionStorage.getItem('user');
    if (userStr) {
      this.currentUser = JSON.parse(userStr);
    }
    
    // 加载公司信息
    this.loadCompanyInfo();
  },
  methods: {
    async loadCompanyInfo() {
      try {
        this.loading = true;
        
        // 从用户信息中获取 companyId
        if (!this.currentUser || !this.currentUser.companyId) {
          this.$message.warning('未找到你的公司信息');
          this.loading = false;
          return;
        }
        
        // 直接通过 companyId 获取公司信息
        const response = await this.$axios.get(
          this.$apiBaseUrl + '/company/detail/' + this.currentUser.companyId
        );
        
        if (response.data.code === 200 && response.data.data) {
          // 找到对应的公司
          this.companyInfo = response.data.data;
          
          // 初始化编辑表单
          this.editForm = {
            name: this.companyInfo.name,
            industry: this.companyInfo.industry,
            scale: this.companyInfo.scale,
            phone: this.companyInfo.phone,
            address: this.companyInfo.address,
            description: this.companyInfo.description
          };
          
          // 加载统计数据
          this.loadStats();
        } else {
          this.$message.warning('未找到公司信息，请联系管理员');
        }
      } catch (error) {
        console.error('加载公司信息失败:', error);
        this.$message.error('加载公司信息失败');
      } finally {
        this.loading = false;
      }
    },
    
    async loadStats() {
      try {
        // 获取职位数量和职位列表
        if (this.currentUser && this.currentUser.companyId && this.companyInfo) {
          const jobResponse = await this.$axios.get(
            this.$apiBaseUrl + '/job/company/' + encodeURIComponent(this.companyInfo.name)
          );
          
          if (jobResponse.data.code === 200) {
            const jobs = jobResponse.data.data || [];
            this.jobCount = jobs.length;
            this.companyJobs = jobs;
            
            // 统计每个职位的申请数量
            let totalApplicants = 0;
            for (const job of jobs) {
              try {
                const countResponse = await this.$axios.get(
                  this.$apiBaseUrl + '/job-application/job/' + job.id + '/count'
                );
                if (countResponse.data.code === 200) {
                  job.applyCount = countResponse.data.data || 0;
                  totalApplicants += job.applyCount;
                }
              } catch (error) {
                console.error('获取职位申请数量失败:', error);
              }
            }
            this.applicantCount = totalApplicants;
          }
          
          // 加载关注数量
          try {
            const followResponse = await this.$axios.get(
              this.$apiBaseUrl + '/user-follow-company/companies/' + this.currentUser.id
            );
            if (followResponse.data.code === 200) {
              const followedCompanies = followResponse.data.data || [];
              // 筛选出当前公司
              const currentCompanyFollows = followedCompanies.filter(
                c => c.id === this.companyInfo.id
              );
              this.followCount = currentCompanyFollows.length;
            }
          } catch (error) {
            console.error('获取关注数量失败:', error);
            this.followCount = 0;
          }
          
          // 加载最新申请人
          await this.loadRecentApplicants();
        }
        
        // 获取公司的真实浏览量
        this.viewCount = await this.getViewCount();
        
        // 记录浏览
        await this.recordView();
      } catch (error) {
        console.error('加载统计数据失败:', error);
      }
    },
    
    async loadRecentApplicants() {
      try {
        // 获取公司所有职位的申请人
        const allApplicants = [];
        
        for (const job of this.companyJobs) {
          try {
            const response = await this.$axios.get(
              this.$apiBaseUrl + '/job-application/job/' + job.id + '/applicants'
            );
            if (response.data.code === 200 && response.data.data) {
              response.data.data.forEach(applicant => {
                allApplicants.push({
                  ...applicant,
                  jobTitle: job.title,
                  jobId: job.id
                });
              });
            }
          } catch (error) {
            console.error('获取职位申请人失败:', error);
          }
        }
        
        // 按时间排序并取前10条
        this.recentApplicants = allApplicants
          .sort((a, b) => new Date(b.applyTime) - new Date(a.applyTime))
          .slice(0, 10);
      } catch (error) {
        console.error('加载最新申请人失败:', error);
      }
    },
    
    toggleEdit() {
      this.isEditing = !this.isEditing;
      if (!this.isEditing) {
        // 取消编辑时恢复原值
        this.editForm = {
          name: this.companyInfo.name,
          industry: this.companyInfo.industry,
          scale: this.companyInfo.scale,
          phone: this.companyInfo.phone,
          address: this.companyInfo.address,
          description: this.companyInfo.description
        };
      }
    },
    
    cancelEdit() {
      this.isEditing = false;
      this.editForm = {
        name: this.companyInfo.name,
        industry: this.companyInfo.industry,
        scale: this.companyInfo.scale,
        phone: this.companyInfo.phone,
        address: this.companyInfo.address,
        description: this.companyInfo.description
      };
    },
    
    saveCompany() {
      this.$refs.editForm.validate(async (valid) => {
        if (valid) {
          try {
            this.saving = true;
            
            // 调用更新接口
            const response = await this.$axios.put(
              this.$apiBaseUrl + '/company/update/' + this.companyInfo.id,
              this.editForm
            );
            
            if (response.data.code === 200) {
              this.$message.success('公司信息更新成功');
              
              // 更新显示的信息
              this.companyInfo = { ...this.companyInfo, ...this.editForm };
              this.isEditing = false;
            } else {
              this.$message.error(response.data.msg || '保存失败');
            }
          } catch (error) {
            console.error('更新公司信息失败:', error);
            this.$message.error('更新公司信息失败');
          } finally {
            this.saving = false;
          }
        } else {
          this.$message.warning('请填写完整信息');
          return false;
        }
      });
    },
    
    createPosition() {
      this.$router.push('/HrPosition');
    },
    
    viewJobs() {
      this.$router.push('/HrPosition');
    },
    
    viewApplicants() {
      // 跳转到 HR 个人中心 - 候选人管理
      this.$router.push({
        path: '/HrPerson',
        query: { tab: 'candidates' }
      });
    },
    
    viewApplicantDetail(applicant) {
      // 跳转到 HR 个人中心查看候选人详情
      this.$router.push({
        path: '/HrPerson',
        query: { 
          tab: 'candidates',
          applicantId: applicant.id
        }
      });
    },
    
    viewJobDetail(job) {
      this.$router.push('/JobDetail/' + job.id);
    },
    
    getStatusType(status) {
      const typeMap = {
        'new': '',
        'viewed': 'info',
        'interview': 'warning',
        'hired': 'success',
        'rejected': 'danger'
      };
      return typeMap[status] || '';
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
    },
    
    /**
     * 获取公司浏览量
     */
    async getViewCount() {
      try {
        if (this.currentUser && this.currentUser.companyId) {
          const response = await this.$axios.get(
            this.$apiBaseUrl + '/company/' + this.currentUser.companyId + '/view-count'
          );
          if (response.data.code === 200) {
            return response.data.data || 0;
          }
        }
        return 0;
      } catch (error) {
        console.error('获取浏览量失败:', error);
        return 0;
      }
    },
    
    /**
     * 记录浏览
     */
    async recordView() {
      try {
        if (this.companyInfo && this.companyInfo.id) {
          await this.$axios.post(
            this.$apiBaseUrl + '/company/' + this.companyInfo.id + '/view',
            {},
            {
              params: {
                userId: this.currentUser ? this.currentUser.id : null,
                ipAddress: null // 后续可以从请求中获取IP
              }
            }
          );
          console.log('浏览记录已被记录');
        }
      } catch (error) {
        console.error('记录浏览失败:', error);
        // 记录失败不影响页面显示
      }
    },
    
    viewCompanyDetail() {
      if (this.companyInfo && this.companyInfo.id) {
        this.$router.push('/CompanyDetail/' + this.companyInfo.id);
      }
    },
    
    goToHome() {
      this.$router.push('/HrHomePage');
    },
    
    position() {
      this.$router.push('/HrPosition');
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
    
    logout() {
      sessionStorage.removeItem('user');
      this.$router.push('/');
      this.$message.success('已退出登录');
    }
  }
}
</script>

<style scoped>
.hr-company-container {
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

.page-header {
  background: white;
  padding: 30px;
  margin-top: 30px;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
  text-align: center;
}

.page-title {
  font-size: 32px;
  font-weight: bold;
  color: #2c3e50;
  margin-bottom: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
}

.page-title i {
  color: #667eea;
}

.page-subtitle {
  font-size: 16px;
  color: #909399;
}

.loading-container {
  background: white;
  padding: 30px;
  margin-top: 30px;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
}

.company-management {
  margin-top: 30px;
}

.company-card {
  background: white;
  border-radius: 12px;
  padding: 30px;
  margin-bottom: 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 2px solid rgba(102, 126, 234, 0.1);
}

.card-title {
  font-size: 22px;
  font-weight: bold;
  color: #2c3e50;
  display: flex;
  align-items: center;
  gap: 10px;
}

.card-title i {
  color: #667eea;
}

.card-body {
  padding: 10px 0;
}

.info-display {
  padding: 10px 0;
}

.info-row {
  display: flex;
  gap: 30px;
  margin-bottom: 20px;
}

.info-item {
  flex: 1;
  display: flex;
  align-items: flex-start;
}

.info-item.full-width {
  flex-direction: column;
}

.info-item label {
  font-weight: 600;
  color: #606266;
  min-width: 100px;
  margin-right: 10px;
}

.info-item span {
  color: #303133;
  line-height: 1.6;
}

.description-text {
  margin-top: 8px;
  padding: 12px;
  background: #f8fafc;
  border-radius: 8px;
  line-height: 1.8;
  color: #606266;
}

.edit-form {
  padding: 10px 0;
}

.stats-section {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 24px;
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

.quick-actions {
  background: white;
  border-radius: 12px;
  padding: 30px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
}

.section-title {
  font-size: 20px;
  font-weight: bold;
  color: #2c3e50;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.section-title i {
  color: #667eea;
}

.action-buttons {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 15px;
}

.action-buttons .el-button {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 16px;
  font-size: 15px;
}

/* 最新申请人区块样式 */
.applicants-section {
  background: white;
  border-radius: 12px;
  padding: 30px;
  margin-bottom: 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
}

.applicant-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.applicant-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: #f8fafc;
  border-radius: 10px;
  transition: all 0.3s;
  border: 2px solid transparent;
}

.applicant-item:hover {
  background: #f0f9ff;
  border-color: #667eea;
  transform: translateX(4px);
}

.applicant-avatar {
  font-size: 48px;
  color: #667eea;
  flex-shrink: 0;
}

.applicant-info {
  flex: 1;
}

.applicant-name {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 6px;
}

.applicant-position {
  font-size: 14px;
  color: #606266;
  margin-bottom: 6px;
}

.applicant-time {
  font-size: 13px;
  color: #909399;
}

.applicant-actions {
  flex-shrink: 0;
}

/* 公司职位列表样式 */
.jobs-section {
  background: white;
  border-radius: 12px;
  padding: 30px;
  margin-bottom: 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
}

.job-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.job-item {
  padding: 20px;
  background: #f8fafc;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.3s;
  border: 2px solid transparent;
}

.job-item:hover {
  background: #f0f9ff;
  border-color: #667eea;
  
  box-shadow: 0 1px 3px rgba(102, 126, 234, 0.2);
}

.job-title-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.job-title {
  font-size: 18px;
  font-weight: 600;
  color: #2c3e50;
}

.job-info-row {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  margin-bottom: 12px;
  font-size: 14px;
  color: #606266;
}

.job-info-row span {
  display: flex;
  align-items: center;
  gap: 6px;
}

.job-info-row i {
  color: #667eea;
}

.job-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 13px;
  color: #909399;
  padding-top: 12px;
  border-top: 1px solid #e4e7ed;
}

.apply-count {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #667eea;
  font-weight: 500;
}

.apply-count i {
  font-size: 14px;
}

.empty-state {
  background: white;
  padding: 80px 30px;
  margin-top: 30px;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
  text-align: center;
}

.empty-icon {
  font-size: 80px;
  color: #dcdfe6;
  margin-bottom: 20px;
}

.empty-state h3 {
  font-size: 24px;
  color: #606266;
  margin-bottom: 10px;
}

.empty-state p {
  font-size: 16px;
  color: #909399;
  margin-bottom: 30px;
}

@media (max-width: 1024px) {
  .stats-section {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .action-buttons {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .stats-section {
    grid-template-columns: 1fr;
  }
  
  .info-row {
    flex-direction: column;
    gap: 15px;
  }
  
  .page-title {
    font-size: 24px;
  }
}
</style>
