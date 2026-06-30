<template>
  <div class="app-wrapper">
    <AppHeader />
<div class="hr-company-container">
    <!-- �������� -->


    <div class="container">
      <!-- ҳ����� -->
      <div class="page-header">
        <h1 class="page-title">
          <i class="fas fa-building"></i>
          ��˾��Ϣ����
        </h1>
        <p class="page-subtitle">�����ά����˾��Ϣ</p>
      </div>

      <!-- ����״̬ -->
      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="10" animated />
      </div>

      <!-- ��˾��Ϣչʾ -->
      <div v-else-if="companyInfo" class="company-management">
        <!-- ��˾������Ϣ��Ƭ -->
        <div class="company-card">
          <div class="card-header">
            <h2 class="card-title">
              <i class="fas fa-info-circle"></i>
              ��˾������Ϣ
            </h2>
            <el-button 
              type="primary" 
              size="small"
              @click="toggleEdit"
              :icon="isEditing ? 'el-icon-close' : 'el-icon-edit'">
              {{ isEditing ? 'ȡ���༭' : '�༭��Ϣ' }}
            </el-button>
          </div>

          <div class="card-body">
            <!-- �鿴ģʽ -->
            <div v-if="!isEditing" class="info-display">
              <div class="info-row">
                <div class="info-item">
                  <label>��˾���ƣ�</label>
                  <span>{{ companyInfo.name }}</span>
                </div>
                <div class="info-item">
                  <label>������ҵ��</label>
                  <span>{{ companyInfo.industry }}</span>
                </div>
              </div>
              <div class="info-row">
                <div class="info-item">
                  <label>��˾��ģ��</label>
                  <span>{{ companyInfo.scale }}</span>
                </div>
                <div class="info-item">
                  <label>��ϵ�绰��</label>
                  <span>{{ companyInfo.phone }}</span>
                </div>
              </div>
              <div class="info-row">
                <div class="info-item full-width">
                  <label>��˾��ַ��</label>
                  <span>{{ companyInfo.address }}</span>
                </div>
              </div>
              <div class="info-row">
                <div class="info-item full-width">
                  <label>��˾��飺</label>
                  <div class="description-text">{{ companyInfo.description || '���޼��' }}</div>
                </div>
              </div>
            </div>

            <!-- �༭ģʽ -->
            <el-form v-else :model="editForm" :rules="rules" ref="editForm" label-width="100px" class="edit-form">
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="��˾����" prop="name">
                    <el-input v-model="editForm.name" placeholder="�����빫˾����"></el-input>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="������ҵ" prop="industry">
                    <el-select v-model="editForm.industry" placeholder="��ѡ����ҵ" style="width: 100%">
                      <el-option label="������" value="������"></el-option>
                      <el-option label="��������" value="��������"></el-option>
                      <el-option label="����" value="����"></el-option>
                      <el-option label="����" value="����"></el-option>
                      <el-option label="���ز�" value="���ز�"></el-option>
                      <el-option label="ͨ�ż���" value="ͨ�ż���"></el-option>
                      <el-option label="����ҵ" value="����ҵ"></el-option>
                      <el-option label="����ҵ" value="����ҵ"></el-option>
                    </el-select>
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="��˾��ģ" prop="scale">
                    <el-select v-model="editForm.scale" placeholder="��ѡ���ģ" style="width: 100%">
                      <el-option label="����50��" value="����50��"></el-option>
                      <el-option label="50-200��" value="50-200��"></el-option>
                      <el-option label="200-500��" value="200-500��"></el-option>
                      <el-option label="500������" value="500������"></el-option>
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="��ϵ�绰" prop="phone">
                    <el-input v-model="editForm.phone" placeholder="��������ϵ�绰"></el-input>
                  </el-form-item>
                </el-col>
              </el-row>
              <el-form-item label="��˾��ַ" prop="address">
                <el-input v-model="editForm.address" placeholder="�����빫˾��ַ"></el-input>
              </el-form-item>
              <el-form-item label="��˾���" prop="description">
                <el-input 
                  v-model="editForm.description" 
                  type="textarea" 
                  :rows="4"
                  placeholder="�����빫˾���">
                </el-input>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="saveCompany" :loading="saving">�����޸�</el-button>
                <el-button @click="cancelEdit">ȡ��</el-button>
              </el-form-item>
            </el-form>
          </div>
        </div>

        <!-- ͳ�����ݿ�Ƭ -->
        <div class="stats-section">
          <div class="stat-card" @click="viewJobs">
            <div class="stat-icon" style="background: #00C2A1;">
              <i class="fas fa-briefcase"></i>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ jobCount }}</div>
              <div class="stat-label">����ְλ</div>
            </div>
          </div>
          <div class="stat-card" @click="viewApplicants">
            <div class="stat-icon" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);">
              <i class="fas fa-users"></i>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ applicantCount }}</div>
              <div class="stat-label">��������</div>
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-icon" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);">
              <i class="fas fa-eye"></i>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ viewCount }}</div>
              <div class="stat-label">��˾�����</div>
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-icon" style="background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);">
              <i class="fas fa-heart"></i>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ followCount }}</div>
              <div class="stat-label">��ע����</div>
            </div>
          </div>
        </div>

        <!-- ��ݲ��� -->
        <div class="quick-actions">
          <h3 class="section-title">
            <i class="fas fa-thumbs-up"></i>
            ��ݲ���
          </h3>
          <div class="action-buttons">
            <el-button type="primary" @click="createPosition">
              <i class="fas fa-plus"></i>
              ������ְλ
            </el-button>
            <el-button type="success" @click="viewJobs">
              <i class="fas fa-list"></i>
              ����ְλ
            </el-button>
            <el-button type="warning" @click="viewApplicants">
              <i class="fas fa-users"></i>
              �鿴������
            </el-button>
            <el-button type="info" @click="viewCompanyDetail">
              <i class="fas fa-eye"></i>
              Ԥ����˾ҳ
            </el-button>
          </div>
        </div>

        <!-- ��������� -->
        <div class="applicants-section" v-if="recentApplicants.length > 0">
          <h3 class="section-title">
            <i class="fas fa-user-clock"></i>
            ���������
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
                  ����ְλ��{{ applicant.jobTitle }}
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
                  �鿴����
                </el-button>
              </div>
            </div>
          </div>
        </div>

        <!-- ��˾ְλ�б� -->
        <div class="jobs-section" v-if="companyJobs.length > 0">
          <h3 class="section-title">
            <i class="fas fa-briefcase"></i>
            ����ְλ
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
                  {{ job.status === 'active' ? '��Ƹ��' : '�ѹر�' }}
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
                  {{ job.applyCount || 0 }} ������
                </span>
                <span class="publish-time">{{ formatTime(job.createTime) }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- �޹�˾��Ϣ����ʾ -->
      <div v-else class="empty-state">
        <i class="fas fa-building empty-icon"></i>
        <h3>���޹�˾��Ϣ</h3>
        <p>����ϵ����ԱΪ��������˾��Ϣ</p>
        <el-button type="primary" @click="goToHome">������ҳ</el-button>
      </div>
    </div>
  </div>
  </div>

</template>

<script>

export default {
  name: 'HrCompany',
  components: {},
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
          { required: true, message: '�����빫˾����', trigger: 'blur' }
        ],
        industry: [
          { required: true, message: '��ѡ����ҵ', trigger: 'change' }
        ],
        scale: [
          { required: true, message: '��ѡ��˾��ģ', trigger: 'change' }
        ],
        phone: [
          { required: true, message: '��������ϵ�绰', trigger: 'blur' },
          { pattern: /^1[3-9]\d{9}$/, message: '��������ȷ���ֻ���', trigger: 'blur' }
        ],
        address: [
          { required: true, message: '�����빫˾��ַ', trigger: 'blur' }
        ]
      },
      // ͳ������
      jobCount: 0,
      applicantCount: 0,
      viewCount: 0,
      followCount: 0,
      // ���������
      recentApplicants: [],
      // ��˾ְλ�б�
      companyJobs: [],
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
        this.loading = true;
        
        // ���û���Ϣ�л�ȡ companyId
        if (!this.currentUser || !this.currentUser.companyId) {
          this.$message.warning('δ�ҵ������Ĺ�˾��Ϣ');
          this.loading = false;
          return;
        }
        
        // ֱ��ͨ�� companyId ��ȡ��˾��Ϣ
        const response = await this.$axios.get(
          this.$apiBaseUrl + '/company/detail/' + this.currentUser.companyId
        );
        
        if (response.data.code === 200 && response.data.data) {
          // �ҵ���Ӧ�Ĺ�˾
          this.companyInfo = response.data.data;
          
          // ��ʼ���༭���
          this.editForm = {
            name: this.companyInfo.name,
            industry: this.companyInfo.industry,
            scale: this.companyInfo.scale,
            phone: this.companyInfo.phone,
            address: this.companyInfo.address,
            description: this.companyInfo.description
          };
          
          // ����ͳ������
          this.loadStats();
        } else {
          this.$message.warning('δ�ҵ���˾��Ϣ������ϵ����Ա');
        }
      } catch (error) {
        console.error('���ع�˾��Ϣʧ��:', error);
        this.$message.error('���ع�˾��Ϣʧ��');
      } finally {
        this.loading = false;
      }
    },
    
    async loadStats() {
      try {
        // ����ְλ������ְλ�б�
        if (this.currentUser && this.currentUser.companyId && this.companyInfo) {
          const jobResponse = await this.$axios.get(
            this.$apiBaseUrl + '/job/company/' + encodeURIComponent(this.companyInfo.name)
          );
          
          if (jobResponse.data.code === 200) {
            const jobs = jobResponse.data.data || [];
            this.jobCount = jobs.length;
            this.companyJobs = jobs;
            
            // ����ÿ��ְλ����������
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
                console.error('��ȡְλ��������ʧ��:', error);
              }
            }
            this.applicantCount = totalApplicants;
          }
          
          // ���ع�ע����
          try {
            const followResponse = await this.$axios.get(
              this.$apiBaseUrl + '/user-follow-company/companies/' + this.currentUser.id
            );
            if (followResponse.data.code === 200) {
              const followedCompanies = followResponse.data.data || [];
              // ɸѡ����ǰ��˾
              const currentCompanyFollows = followedCompanies.filter(
                c => c.id === this.companyInfo.id
              );
              this.followCount = currentCompanyFollows.length;
            }
          } catch (error) {
            console.error('��ȡ��ע����ʧ��:', error);
            this.followCount = 0;
          }
          
          // �������������
          await this.loadRecentApplicants();
        }
        
        // ��ȡ��˾��ʵ�����
        this.viewCount = await this.getViewCount();
        
        // ��¼���
        await this.recordView();
      } catch (error) {
        console.error('����ͳ������ʧ��:', error);
      }
    },
    
    async loadRecentApplicants() {
      try {
        // ��ȡ��˾����ְλ��������
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
            console.error('��ȡְλ������ʧ��:', error);
          }
        }
        
        // ������ʱ������ȡ���10��
        this.recentApplicants = allApplicants
          .sort((a, b) => new Date(b.applyTime) - new Date(a.applyTime))
          .slice(0, 10);
      } catch (error) {
        console.error('�������������ʧ��:', error);
      }
    },
    
    toggleEdit() {
      this.isEditing = !this.isEditing;
      if (!this.isEditing) {
        // ȡ���༭ʱ�ָ�ԭֵ
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
            
            // ���ø��½ӿ�
            const response = await this.$axios.put(
              this.$apiBaseUrl + '/company/update/' + this.companyInfo.id,
              this.editForm
            );
            
            if (response.data.code === 200) {
              this.$message.success('��˾��Ϣ���³ɹ�');
              
              // ������ʾ����Ϣ
              this.companyInfo = { ...this.companyInfo, ...this.editForm };
              this.isEditing = false;
            } else {
              this.$message.error(response.data.msg || '����ʧ��');
            }
          } catch (error) {
            console.error('���¹�˾��Ϣʧ��:', error);
            this.$message.error('���¹�˾��Ϣʧ��');
          } finally {
            this.saving = false;
          }
        } else {
          this.$message.warning('����д������Ϣ');
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
      // ��ת�� HR �������� - ��ѡ�˹���
      this.$router.push({
        path: '/HrPerson',
        query: { tab: 'candidates' }
      });
    },
    
    viewApplicantDetail(applicant) {
      // ��ת�� HR �������Ĳ鿴����������
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
        'new': '������',
        'viewed': '�Ѳ鿴',
        'interview': '������',
        'hired': '��¼��',
        'rejected': '�Ѿܾ�'
      };
      return textMap[status] || 'δ֪';
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
    },
    
    /**
     * ��ȡ��˾�����
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
        console.error('��ȡ�����ʧ��:', error);
        return 0;
      }
    },
    
    /**
     * ��¼���
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
                ipAddress: null // ��˿��Դ������л�ȡIP
              }
            }
          );
          console.log('�����¼�ѱ���');
        }
      } catch (error) {
        console.error('��¼���ʧ��:', error);
        // ��¼ʧ�ܲ�Ӱ��ҳ����ʾ
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

/* ������������� */
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

/* ��˾ְλ�б����� */
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
  
  .header-content {
    flex-direction: column;
    gap: 10px;
  }
  
  .nav-menu {
    flex-wrap: wrap;
    justify-content: center;
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
