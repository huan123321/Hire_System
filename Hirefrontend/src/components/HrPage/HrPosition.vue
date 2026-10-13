<template>
  <div class="app-wrapper">
    <AppHeader />
<div class="hr-position-container">


    <div class="container">
      <!-- 页面标题和操作按钮 -->
      <div class="page-header">
        <h1 class="page-title">
          <i class="fas fa-list-alt"></i>
          职位管理
        </h1>
        <p class="page-subtitle">管理你公司发布的职位信息</p>
        <el-button type="primary" size="large" @click="showCreateDialog" icon="el-icon-plus">
          发布新职位
        </el-button>
      </div>

      <!-- 加载状态 -->
      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="8" animated />
      </div>

      <!-- 职位列表 -->
      <div v-else-if="jobList.length > 0" class="job-management">
        <div v-for="job in pagedJobs" :key="job.id" class="job-card">
          <div class="job-header">
            <div class="job-info">
              <h3 class="job-title">{{ job.title }}</h3>
              <div class="job-meta">
                <span class="meta-item">
                  <i class="fas fa-money-bill-wave"></i>
                  {{ job.salary }}
                </span>
                <span class="meta-item">
                  <i class="fas fa-map-marker-alt"></i>
                  {{ job.city }}
                </span>
                <span class="meta-item">
                  <i class="fas fa-user-tie"></i>
                  {{ job.experience }}
                </span>
                <span class="meta-item">
                  <i class="fas fa-graduation-cap"></i>
                  {{ job.education }}
                </span>
              </div>
            </div>
            <div class="job-status">
              <el-tag :type="job.status === 'active' ? 'success' : 'info'" size="medium">
                {{ job.status === 'active' ? '招聘中' : '已下线' }}
              </el-tag>
            </div>
          </div>

          <div class="job-tags">
            <el-tag 
              v-for="(tag, index) in job.tags ? job.tags.split(',') : []" 
              :key="index" 
              size="small"
              effect="plain">
              {{ tag }}
            </el-tag>
          </div>

          <div class="job-description">
            {{ job.description || '暂无职位描述' }}
          </div>

          <div class="job-stats">
            <div class="stat-item">
              <i class="fas fa-eye"></i>
              <span>{{ job.viewCount || 0 }} 次浏览</span>
            </div>
            <div class="stat-item">
              <i class="fas fa-users"></i>
              <span>{{ job.applicationCount || 0 }} 份申请</span>
            </div>
            <div class="stat-item">
              <i class="fas fa-clock"></i>
              <span>发布于 {{ formatDate(job.createTime) }}</span>
            </div>
          </div>

          <div class="job-actions">
            <el-button 
              type="primary" 
              size="small" 
              @click="viewJobDetail(job)"
              icon="el-icon-view">
              查看详情
            </el-button>
            <el-button 
              type="success" 
              size="small" 
              @click="viewApplicants(job)"
              icon="el-icon-user">
              查看候选人 ({{ job.applicationCount || 0 }})
            </el-button>
            <el-button 
              type="warning" 
              size="small" 
              @click="editJob(job)"
              icon="el-icon-edit">
              编辑
            </el-button>
            <el-button 
              :type="job.status === 'active' ? 'warning' : 'success'" 
              size="small" 
              @click="toggleJobStatus(job)"
              :icon="job.status === 'active' ? 'el-icon-close' : 'el-icon-check'">
              {{ job.status === 'active' ? '下线' : '重新上架' }}
            </el-button>
            <el-button 
              type="danger" 
              size="small" 
              @click="deleteJob(job)"
              icon="el-icon-delete">
              删除
            </el-button>
          </div>
        </div>

        <!-- 分页 -->
        <div class="pagination">
          <el-pagination
            background
            layout="prev, pager, next, total"
            :total="total"
            :page-size="pageSize"
            :current-page="currentPage"
            @current-change="handlePageChange">
          </el-pagination>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-else class="empty-state">
        <i class="fas fa-briefcase empty-icon"></i>
        <h3>暂无职位</h3>
        <p>你还没有发布任何职位，点击右上角按钮发布一个职位吧！</p>
        <el-button type="primary" @click="showCreateDialog" icon="el-icon-plus">
          发布新职位
        </el-button>
      </div>
    </div>

    <!-- 新建/编辑职位对话框 -->
    <el-dialog 
      :title="isEditing ? '编辑职位' : '发布新职位'" 
      :visible.sync="dialogVisible" 
      width="700px"
      :close-on-click-modal="false">
      <el-form :model="jobForm" :rules="jobRules" ref="jobForm" label-width="120px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="职位名称" prop="title">
              <el-input v-model="jobForm.title" placeholder="例如：Java高级开发工程师"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="薪资范围" prop="salary">
              <el-input v-model="jobForm.salary" placeholder="例如：15k-25k"></el-input>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="工作地点" prop="city">
              <el-select v-model="jobForm.city" placeholder="请选择城市" style="width: 100%">
                <el-option label="北京" value="北京"></el-option>
                <el-option label="上海" value="上海"></el-option>
                <el-option label="广州" value="广州"></el-option>
                <el-option label="深圳" value="深圳"></el-option>
                <el-option label="杭州" value="杭州"></el-option>
                <el-option label="成都" value="成都"></el-option>
                <el-option label="武汉" value="武汉"></el-option>
                <el-option label="南京" value="南京"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="经验要求" prop="experience">
              <el-select v-model="jobForm.experience" placeholder="请选择经验要求" style="width: 100%">
                <el-option label="应届生" value="应届生"></el-option>
                <el-option label="1-3年" value="1-3年"></el-option>
                <el-option label="3-5年" value="3-5年"></el-option>
                <el-option label="5年以上" value="5年以上"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="学历要求" prop="education">
              <el-select v-model="jobForm.education" placeholder="请选择学历要求" style="width: 100%">
                <el-option label="大专" value="大专"></el-option>
                <el-option label="本科" value="本科"></el-option>
                <el-option label="硕士" value="硕士"></el-option>
                <el-option label="博士" value="博士"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="职位标签" prop="tags">
              <el-input v-model="jobForm.tags" placeholder="多个标签用逗号分隔，例如：Java,Spring,MySQL"></el-input>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="职位描述" prop="description">
          <el-input 
            v-model="jobForm.description" 
            type="textarea" 
            :rows="6"
            placeholder="请详细描述职位要求职责等信息">
          </el-input>
        </el-form-item>

        <el-form-item label="任职要求" prop="requirements">
          <el-input 
            v-model="jobForm.requirements" 
            type="textarea" 
            :rows="4"
            placeholder="请输入该职位的任职要求">
          </el-input>
        </el-form-item>
      </el-form>

      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitJob" :loading="submitting">
          {{ isEditing ? '保 存' : '发 布' }}
        </el-button>
      </div>
    </el-dialog>
  </div>
  </div>

</template>

<script>
import AppHeader from '@/components/common/AppHeader.vue';

export default {
  name: 'HrPosition',
  components: { AppHeader },
  data() {
    return {
      currentUser: null,
      companyInfo: null,
      jobList: [],
      loading: true,
      currentPage: 1,
      pageSize: 10,
      total: 0,
      
      // 对话框状态
      dialogVisible: false,
      isEditing: false,
      submitting: false,
      
      // 职位表单
      jobForm: {
        title: '',
        salary: '',
        city: '',
        experience: '',
        education: '',
        tags: '',
        description: '',
        requirements: ''
      },
      
      // 表单验证规则
      jobRules: {
        title: [
          { required: true, message: '请输入职位名称', trigger: 'blur' }
        ],
        salary: [
          { required: true, message: '请输入薪资范围', trigger: 'blur' }
        ],
        city: [
          { required: true, message: '请选择工作地点', trigger: 'change' }
        ],
        experience: [
          { required: true, message: '请选择经验要求', trigger: 'change' }
        ],
        education: [
          { required: true, message: '请选择学历要求', trigger: 'change' }
        ]
      }
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
  computed: {
    pagedJobs() {
      const start = (this.currentPage - 1) * this.pageSize;
      return this.jobList.slice(start, start + this.pageSize);
    }
  },
  methods: {
    async loadCompanyInfo() {
      try {
        if (!this.currentUser || !this.currentUser.companyId) {
          this.$message.warning('未找到你的公司信息');
          this.loading = false;
          return;
        }
        
        // 获取公司信息
        const response = await this.$axios.get(
          this.$apiBaseUrl + '/company/detail/' + this.currentUser.companyId
        );
        
        if (response.data.code === 200 && response.data.data) {
          this.companyInfo = response.data.data;
          // 加载该公司的职位列表
          this.loadJobs();
        } else {
          this.$message.error('获取公司信息失败');
          this.loading = false;
        }
      } catch (error) {
        console.error('加载公司信息失败:', error);
        this.$message.error('加载公司信息失败');
        this.loading = false;
      }
    },
    
    async loadJobs() {
      try {
        this.loading = true;
        
        if (!this.companyInfo) {
          this.loading = false;
          return;
        }
        
        // 获取公司下的职位列表
        const response = await this.$axios.get(
          this.$apiBaseUrl + '/job/company/' + encodeURIComponent(this.companyInfo.name)
        );
        
        if (response.data.code === 200) {
          this.jobList = response.data.data || [];
          this.total = this.jobList.length;
          
          // 为每个职位获取申请数量
          await this.loadApplicationCounts();
        }
      } catch (error) {
        console.error('加载职位列表失败:', error);
        this.$message.error('加载职位列表失败');
      } finally {
        this.loading = false;
      }
    },
    
    async loadApplicationCounts() {
      try {
        // 为每个职位获取申请数量
        for (let job of this.jobList) {
          try {
            const response = await this.$axios.get(
              this.$apiBaseUrl + '/job-application/job/' + job.id + '/count'
            );
            if (response.data.code === 200) {
              job.applicationCount = response.data.data || 0;
            }
          } catch (error) {
            console.error(`获取职位 ${job.id} 申请数量失败:`, error);
            job.applicationCount = 0;
          }
        }
      } catch (error) {
        console.error('批量获取申请数量失败:', error);
      }
    },
    
    showCreateDialog() {
      this.isEditing = false;
      this.resetForm();
      this.dialogVisible = true;
    },
    
    editJob(job) {
      this.isEditing = true;
      this.jobForm = {
        id: job.id,
        title: job.title,
        salary: job.salary,
        city: job.city,
        experience: job.experience,
        education: job.education,
        tags: job.tags || '',
        description: job.description || '',
        requirements: job.requirements || ''
      };
      this.dialogVisible = true;
    },
    
    resetForm() {
      this.jobForm = {
        title: '',
        salary: '',
        city: '',
        experience: '',
        education: '',
        tags: '',
        description: '',
        requirements: ''
      };
      if (this.$refs.jobForm) {
        this.$refs.jobForm.clearValidate();
      }
    },
    
    submitJob() {
      this.$refs.jobForm.validate(async (valid) => {
        if (valid) {
          try {
            this.submitting = true;
            
            // 添加公司信息
            const jobData = {
              ...this.jobForm,
              companyName: this.companyInfo.name,
              industry: this.companyInfo.industry,
              companySize: this.companyInfo.scale,
              status: 'active'
            };
            
            let response;
            if (this.isEditing) {
              // 更新职位
              response = await this.$axios.put(
                this.$apiBaseUrl + '/job/' + this.jobForm.id,
                jobData
              );
            } else {
              // 创建职位
              response = await this.$axios.post(
                this.$apiBaseUrl + '/job',
                jobData
              );
            }
            
            if (response.data.code === 200) {
              this.$message.success(this.isEditing ? '职位更新成功' : '职位发布成功');
              this.dialogVisible = false;
              this.loadJobs(); // 重新加载列表
            } else {
              this.$message.error(response.data.msg || '保存失败');
            }
          } catch (error) {
            console.error('提交职位失败:', error);
            this.$message.error('保存失败，请稍后重试');
          } finally {
            this.submitting = false;
          }
        } else {
          this.$message.warning('请填写完整信息');
          return false;
        }
      });
    },
    
    async toggleJobStatus(job) {
      try {
        const newStatus = job.status === 'active' ? 'inactive' : 'active';
        const action = newStatus === 'active' ? '上架' : '下线';
        
        const confirmResult = await this.$confirm(
          `确定要${action}该职位吗？`,
          '提示',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }
        ).catch(() => 'cancel');
        
        if (confirmResult === 'cancel') {
          return;
        }
        
        const response = await this.$axios.put(
          this.$apiBaseUrl + '/job/' + job.id,
          { ...job, status: newStatus }
        );
        
        if (response.data.code === 200) {
          this.$message.success(`${action}成功`);
          this.loadJobs(); // 重新加载列表
        } else {
          this.$message.error(response.data.msg || '操作失败');
        }
      } catch (error) {
        if (error !== 'cancel') {
          console.error('切换职位状态失败:', error);
          this.$message.error('操作失败，请稍后重试');
        }
      }
    },
    
    async deleteJob(job) {
      try {
        const confirmResult = await this.$confirm(
          `确定要删除职位「${job.title}」吗？此操作不可恢复！`,
          '警告',
          {
            confirmButtonText: '确定删除',
            cancelButtonText: '取消',
            type: 'error'
          }
        ).catch(() => 'cancel');
        
        if (confirmResult === 'cancel') {
          return;
        }
        
        const response = await this.$axios.delete(
          this.$apiBaseUrl + '/job/' + job.id
        );
        
        if (response.data.code === 200) {
          this.$message.success('职位删除成功');
          this.loadJobs(); // 重新加载列表
        } else {
          this.$message.error(response.data.msg || '删除失败');
        }
      } catch (error) {
        if (error !== 'cancel') {
          console.error('删除职位失败:', error);
          this.$message.error('删除失败，请稍后重试');
        }
      }
    },
    
    viewJobDetail(job) {
      // HR 查看职位详情 - 直接在当前页显示
      this.$alert(
        `<div style="text-align: left; line-height: 1.8;">
          <p><strong>职位名称：</strong>${job.title}</p>
          <p><strong>薪资范围：</strong>${job.salary}</p>
          <p><strong>工作地点：</strong>${job.city}</p>
          <p><strong>经验要求：</strong>${job.experience}</p>
          <p><strong>学历要求：</strong>${job.education}</p>
          <p><strong>职位标签：</strong>${job.tags || '无'}</p>
          <hr>
          <p><strong>职位描述：</strong></p>
          <p>${job.description || '暂无描述'}</p>
        </div>`,
        '职位详情',
        { dangerouslyUseHTMLString: true, confirmButtonText: '知道了' }
      );
    },
    
    viewApplicants(job) {
      // HR 查看候选人 - 跳转到 HR 个人中心的候选人管理页面
      this.$router.push({
        path: '/HrPerson',
        query: { tab: 'candidates', jobId: job.id }
      });
    },
    
    handlePageChange(page) {
      this.currentPage = page;
      // 后续可实际实现分页逻辑
    },
    
    formatDate(dateString) {
      if (!dateString) return '未知';
      const date = new Date(dateString);
      return date.toLocaleDateString('zh-CN');
    },
    
    goToHome() {
      this.$router.push('/HrHomePage');
    },
    
    company() {
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
    
    logout() {
      sessionStorage.removeItem('user');
      this.$router.push('/');
      this.$message.success('已退出登录');
    }
  }
}
</script>

<style scoped>
.hr-position-container {
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
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.page-title {
  font-size: 32px;
  font-weight: bold;
  color: #2c3e50;
  display: flex;
  align-items: center;
  gap: 12px;
  margin: 0;
}

.page-title i {
  color: #667eea;
}

.page-subtitle {
  font-size: 16px;
  color: #909399;
  margin: 5px 0 0 0;
}

.loading-container {
  background: white;
  padding: 30px;
  margin-top: 30px;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
}

.job-management {
  margin-top: 30px;
}

.job-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
  transition: all 0.3s;
}

.job-card:hover {
  
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.12);
}

.job-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}

.job-info {
  flex: 1;
}

.job-title {
  font-size: 22px;
  font-weight: bold;
  color: #2c3e50;
  margin: 0 0 12px 0;
}

.job-meta {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #606266;
  font-size: 14px;
}

.meta-item i {
  color: #667eea;
}

.job-status {
  flex-shrink: 0;
  margin-left: 20px;
}

.job-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 16px;
}

.job-description {
  color: #606266;
  line-height: 1.8;
  margin-bottom: 16px;
  padding: 12px;
  background: #f8fafc;
  border-radius: 8px;
}

.job-stats {
  display: flex;
  gap: 24px;
  padding: 16px 0;
  border-top: 1px solid #ebeef5;
  border-bottom: 1px solid #ebeef5;
  margin-bottom: 16px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #909399;
  font-size: 14px;
}

.stat-item i {
  color: #667eea;
}

.job-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.pagination {
  margin-top: 30px;
  text-align: center;
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

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    gap: 20px;
    text-align: center;
  }
  
  .job-header {
    flex-direction: column;
    gap: 12px;
  }
  
  .job-status {
    margin-left: 0;
  }
  
  .job-meta {
    flex-direction: column;
    gap: 8px;
  }
  
  .job-stats {
    flex-direction: column;
    gap: 12px;
  }
  
  .job-actions {
    flex-direction: column;
  }
}
</style>
