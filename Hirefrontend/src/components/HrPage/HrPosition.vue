<template>
  <div class="app-wrapper">
    <AppHeader />
<div class="hr-position-container">


    <div class="container">
      <!-- ҳ�����Ͳ�����ť -->
      <div class="page-header">
        <h1 class="page-title">
          <i class="fas fa-list-alt"></i>
          ְλ����
        </h1>
        <p class="page-subtitle">��������˾������ְλ��Ϣ</p>
        <el-button type="primary" size="large" @click="showCreateDialog" icon="el-icon-plus">
          ������ְλ
        </el-button>
      </div>

      <!-- ����״̬ -->
      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="8" animated />
      </div>

      <!-- ְλ�б� -->
      <div v-else-if="jobList.length > 0" class="job-management">
        <div v-for="job in jobList" :key="job.id" class="job-card">
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
                {{ job.status === 'active' ? '��Ƹ��' : '���¼�' }}
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
            {{ job.description || '����ְλ����' }}
          </div>

          <div class="job-stats">
            <div class="stat-item">
              <i class="fas fa-eye"></i>
              <span>{{ job.viewCount || 0 }} �����</span>
            </div>
            <div class="stat-item">
              <i class="fas fa-users"></i>
              <span>{{ job.applicationCount || 0 }} ������</span>
            </div>
            <div class="stat-item">
              <i class="fas fa-clock"></i>
              <span>������ {{ formatDate(job.createTime) }}</span>
            </div>
          </div>

          <div class="job-actions">
            <el-button 
              type="primary" 
              size="small" 
              @click="viewJobDetail(job)"
              icon="el-icon-view">
              �鿴����
            </el-button>
            <el-button 
              type="success" 
              size="small" 
              @click="viewApplicants(job)"
              icon="el-icon-user">
              �鿴������ ({{ job.applicationCount || 0 }})
            </el-button>
            <el-button 
              type="warning" 
              size="small" 
              @click="editJob(job)"
              icon="el-icon-edit">
              �༭
            </el-button>
            <el-button 
              :type="job.status === 'active' ? 'warning' : 'success'" 
              size="small" 
              @click="toggleJobStatus(job)"
              :icon="job.status === 'active' ? 'el-icon-close' : 'el-icon-check'">
              {{ job.status === 'active' ? '�¼�' : '�����ϼ�' }}
            </el-button>
            <el-button 
              type="danger" 
              size="small" 
              @click="deleteJob(job)"
              icon="el-icon-delete">
              ɾ��
            </el-button>
          </div>
        </div>

        <!-- ��ҳ -->
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

      <!-- ��״̬ -->
      <div v-else class="empty-state">
        <i class="fas fa-briefcase empty-icon"></i>
        <h3>����ְλ</h3>
        <p>����û�з����κ�ְλ��������Ͻǰ�ť������һ��ְλ�ɣ�</p>
        <el-button type="primary" @click="showCreateDialog" icon="el-icon-plus">
          ������ְλ
        </el-button>
      </div>
    </div>

    <!-- ����/�༭ְλ�Ի��� -->
    <el-dialog 
      :title="isEditing ? '�༭ְλ' : '������ְλ'" 
      :visible.sync="dialogVisible" 
      width="700px"
      :close-on-click-modal="false">
      <el-form :model="jobForm" :rules="jobRules" ref="jobForm" label-width="120px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="ְλ����" prop="title">
              <el-input v-model="jobForm.title" placeholder="���磺Java�߼���������ʦ"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="н�ʷ�Χ" prop="salary">
              <el-input v-model="jobForm.salary" placeholder="���磺15k-25k"></el-input>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="�����ص�" prop="city">
              <el-select v-model="jobForm.city" placeholder="��ѡ�����" style="width: 100%">
                <el-option label="����" value="����"></el-option>
                <el-option label="�Ϻ�" value="�Ϻ�"></el-option>
                <el-option label="����" value="����"></el-option>
                <el-option label="����" value="����"></el-option>
                <el-option label="����" value="����"></el-option>
                <el-option label="�ɶ�" value="�ɶ�"></el-option>
                <el-option label="�人" value="�人"></el-option>
                <el-option label="�Ͼ�" value="�Ͼ�"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="����Ҫ��" prop="experience">
              <el-select v-model="jobForm.experience" placeholder="��ѡ����Ҫ��" style="width: 100%">
                <el-option label="Ӧ����" value="Ӧ����"></el-option>
                <el-option label="1-3��" value="1-3��"></el-option>
                <el-option label="3-5��" value="3-5��"></el-option>
                <el-option label="5������" value="5������"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="ѧ��Ҫ��" prop="education">
              <el-select v-model="jobForm.education" placeholder="��ѡ��ѧ��Ҫ��" style="width: 100%">
                <el-option label="��ר" value="��ר"></el-option>
                <el-option label="����" value="����"></el-option>
                <el-option label="˶ʿ" value="˶ʿ"></el-option>
                <el-option label="��ʿ" value="��ʿ"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="ְλ��ǩ" prop="tags">
              <el-input v-model="jobForm.tags" placeholder="�����ǩ�ö��ŷָ���磺Java,Spring,MySQL"></el-input>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="ְλ����" prop="description">
          <el-input 
            v-model="jobForm.description" 
            type="textarea" 
            :rows="6"
            placeholder="����ϸ����ְλҪ��ְ�����Ϣ">
          </el-input>
        </el-form-item>

        <el-form-item label="��ְҪ��" prop="requirements">
          <el-input 
            v-model="jobForm.requirements" 
            type="textarea" 
            :rows="4"
            placeholder="��������ְλ����ְҪ��">
          </el-input>
        </el-form-item>
      </el-form>

      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">ȡ ��</el-button>
        <el-button type="primary" @click="submitJob" :loading="submitting">
          {{ isEditing ? '�� ��' : '�� ��' }}
        </el-button>
      </div>
    </el-dialog>
  </div>
  </div>

</template>

<script>

export default {
  name: 'HrPosition',
  components: {},
  data() {
    return {
      currentUser: null,
      companyInfo: null,
      jobList: [],
      loading: true,
      currentPage: 1,
      pageSize: 10,
      total: 0,
      
      // �Ի������
      dialogVisible: false,
      isEditing: false,
      submitting: false,
      
      // ְλ���
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
      
      // �����֤����
      jobRules: {
        title: [
          { required: true, message: '������ְλ����', trigger: 'blur' }
        ],
        salary: [
          { required: true, message: '������н�ʷ�Χ', trigger: 'blur' }
        ],
        city: [
          { required: true, message: '��ѡ�����ص�', trigger: 'change' }
        ],
        experience: [
          { required: true, message: '��ѡ����Ҫ��', trigger: 'change' }
        ],
        education: [
          { required: true, message: '��ѡ��ѧ��Ҫ��', trigger: 'change' }
        ]
      }
    }
  },
  created() {
    // ��ȡ��ǰ�û���Ϣ
    const userStr = sessionStorage.getItem('user');
    if (userStr) {
      this.currentUser = JSON.parse(userStr);
    }
    
    // ���ع�˾��Ϣ
    this.loadCompanyInfo();
  },
  methods: {
    async loadCompanyInfo() {
      try {
        if (!this.currentUser || !this.currentUser.companyId) {
          this.$message.warning('δ�ҵ������Ĺ�˾��Ϣ');
          this.loading = false;
          return;
        }
        
        // ��ȡ��˾��Ϣ
        const response = await this.$axios.get(
          this.$apiBaseUrl + '/company/detail/' + this.currentUser.companyId
        );
        
        if (response.data.code === 200 && response.data.data) {
          this.companyInfo = response.data.data;
          // ���ظù�˾��ְλ�б�
          this.loadJobs();
        } else {
          this.$message.error('��ȡ��˾��Ϣʧ��');
          this.loading = false;
        }
      } catch (error) {
        console.error('���ع�˾��Ϣʧ��:', error);
        this.$message.error('���ع�˾��Ϣʧ��');
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
        
        // ��ȡ��˾������ְλ�б�
        const response = await this.$axios.get(
          this.$apiBaseUrl + '/job/company/' + encodeURIComponent(this.companyInfo.name)
        );
        
        if (response.data.code === 200) {
          this.jobList = response.data.data || [];
          this.total = this.jobList.length;
          
          // Ϊÿ��ְλ��ȡ��������
          await this.loadApplicationCounts();
        }
      } catch (error) {
        console.error('����ְλ�б�ʧ��:', error);
        this.$message.error('����ְλ�б�ʧ��');
      } finally {
        this.loading = false;
      }
    },
    
    async loadApplicationCounts() {
      try {
        // Ϊÿ��ְλ��ȡ��������
        for (let job of this.jobList) {
          try {
            const response = await this.$axios.get(
              this.$apiBaseUrl + '/job-application/job/' + job.id + '/count'
            );
            if (response.data.code === 200) {
              job.applicationCount = response.data.data || 0;
            }
          } catch (error) {
            console.error(`��ȡְλ ${job.id} ��������ʧ��:`, error);
            job.applicationCount = 0;
          }
        }
      } catch (error) {
        console.error('������ȡ��������ʧ��:', error);
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
            
            // ��ӹ�˾��Ϣ
            const jobData = {
              ...this.jobForm,
              companyName: this.companyInfo.name,
              industry: this.companyInfo.industry,
              companySize: this.companyInfo.scale,
              status: 'active'
            };
            
            let response;
            if (this.isEditing) {
              // ����ְλ
              response = await this.$axios.put(
                this.$apiBaseUrl + '/job/' + this.jobForm.id,
                jobData
              );
            } else {
              // ����ְλ
              response = await this.$axios.post(
                this.$apiBaseUrl + '/job',
                jobData
              );
            }
            
            if (response.data.code === 200) {
              this.$message.success(this.isEditing ? 'ְλ���³ɹ�' : 'ְλ�����ɹ�');
              this.dialogVisible = false;
              this.loadJobs(); // ���¼����б�
            } else {
              this.$message.error(response.data.msg || '����ʧ��');
            }
          } catch (error) {
            console.error('�ύְλʧ��:', error);
            this.$message.error('����ʧ�ܣ����Ժ�����');
          } finally {
            this.submitting = false;
          }
        } else {
          this.$message.warning('����д������Ϣ');
          return false;
        }
      });
    },
    
    async toggleJobStatus(job) {
      try {
        const newStatus = job.status === 'active' ? 'inactive' : 'active';
        const action = newStatus === 'active' ? '�ϼ�' : '�¼�';
        
        const confirmResult = await this.$confirm(
          `ȷ��Ҫ${action}��ְλ��`,
          '��ʾ',
          {
            confirmButtonText: 'ȷ��',
            cancelButtonText: 'ȡ��',
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
          this.$message.success(`${action}�ɹ�`);
          this.loadJobs(); // ���¼����б�
        } else {
          this.$message.error(response.data.msg || '����ʧ��');
        }
      } catch (error) {
        if (error !== 'cancel') {
          console.error('�л�ְλ״̬ʧ��:', error);
          this.$message.error('����ʧ�ܣ����Ժ�����');
        }
      }
    },
    
    async deleteJob(job) {
      try {
        const confirmResult = await this.$confirm(
          `ȷ��Ҫɾ��ְλ��${job.title}���𣿴˲������ɻָ���`,
          '����',
          {
            confirmButtonText: 'ȷ��ɾ��',
            cancelButtonText: 'ȡ��',
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
          this.$message.success('ְλɾ���ɹ�');
          this.loadJobs(); // ���¼����б�
        } else {
          this.$message.error(response.data.msg || 'ɾ��ʧ��');
        }
      } catch (error) {
        if (error !== 'cancel') {
          console.error('ɾ��ְλʧ��:', error);
          this.$message.error('ɾ��ʧ�ܣ����Ժ�����');
        }
      }
    },
    
    viewJobDetail(job) {
      // HR �鿴ְλ���� - ֱ���ڵ�ǰҳ��ʾ
      this.$alert(
        `<div style="text-align: left; line-height: 1.8;">
          <p><strong>ְλ���ƣ�</strong>${job.title}</p>
          <p><strong>н�ʷ�Χ��</strong>${job.salary}</p>
          <p><strong>�����ص㣺</strong>${job.city}</p>
          <p><strong>����Ҫ��</strong>${job.experience}</p>
          <p><strong>ѧ��Ҫ��</strong>${job.education}</p>
          <p><strong>ְλ��ǩ��</strong>${job.tags || '��'}</p>
          <hr>
          <p><strong>ְλ������</strong></p>
          <p>${job.description || '��������'}</p>
        </div>`,
        'ְλ����',
        { dangerouslyUseHTMLString: true, confirmButtonText: '֪����' }
      );
    },
    
    viewApplicants(job) {
      // HR �鿴������ - ��ת�� HR �������ĺ�ѡ�˹���ҳ��
      this.$router.push({
        path: '/HrPerson',
        query: { tab: 'candidates', jobId: job.id }
      });
    },
    
    handlePageChange(page) {
      this.currentPage = page;
      // �������ʵ�ַ�ҳ�߼�
    },
    
    formatDate(dateString) {
      if (!dateString) return 'δ֪';
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
     * ��ת����Ϣ����
     */
    goToMessageCenter() {
      this.$router.push({
        name: 'MessageCenter'
      });
    },
    
    logout() {
      sessionStorage.removeItem('user');
      this.$router.push('/');
      this.$message.success('���˳���¼');
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

.header {
  background: #00C2A1;
  color: white;
  padding: 15px 0;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 100;}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.logo {
  font-size: 24px;
  font-weight: bold;
  display: flex;
  align-items: center;
}

.logo i {
  margin-right: 10px;
  font-size: 28px;
}

.nav-menu {
  display: flex;
  gap: 10px;
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

.footer {
  text-align: center;
  padding: 30px 0;
  margin-top: 40px;
  color: #909399;
  border-top: 2px solid rgba(102, 126, 234, 0.1);
  background: white;
}

.footer-content {
  font-size: 14px;
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
  
  .header-content {
    flex-direction: column;
    gap: 10px;
  }
  
  .nav-menu {
    flex-wrap: wrap;
    justify-content: center;
  }
}
</style>
