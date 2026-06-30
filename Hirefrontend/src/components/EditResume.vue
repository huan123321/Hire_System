<template>
  <div class="app-wrapper">
    <AppHeader />
<div class="edit-resume-container">
    <!-- �������� -->
    <div class="container">
      <div class="main-content">
        <!-- ��ർ�� -->
        <div class="side-nav">
          <el-menu
              :default-active="activeNav"
              class="el-menu-vertical"
              @select="handleNavSelect">
            <el-menu-item index="profile">
              <i class="fas fa-user"></i>
              <span>��������</span>
            </el-menu-item>
            <el-menu-item index="resume">
              <i class="fas fa-file-alt"></i>
              <span>�ҵļ���</span>
            </el-menu-item>
            <el-menu-item index="applications">
              <i class="fas fa-paper-plane"></i>
              <span>ְλ����</span>
            </el-menu-item>
            <el-menu-item index="interviews">
              <i class="fas fa-handshake"></i>
              <span>���԰���</span>
            </el-menu-item>
          </el-menu>
        </div>

        <!-- �Ҳ������� -->
        <div class="content-area">
          <div class="section-title">�༭����</div>
          
          <!-- ����ѡ��͹��� -->
          <div class="resume-management">
            <div class="resume-selector">
              <el-select v-model="selectedResumeId" placeholder="ѡ�����" @change="loadResume">
                <el-option
                  v-for="item in resumeList"
                  :key="item.id"
                  :label="item.name + (item.isDefault ? ' (Ĭ��)' : '')"
                  :value="item.id">
                </el-option>
              </el-select>
              <el-button type="primary" @click="createNewResume" size="small">�½�����</el-button>
              <el-button v-if="selectedResume && !selectedResume.isDefault" type="success" @click="setDefaultResume" size="small">��ΪĬ��</el-button>
              <el-button v-if="selectedResume && !selectedResume.isDefault" type="danger" @click="deleteResume" size="small">ɾ��</el-button>
            </div>
          </div>

          <el-form v-if="selectedResume" :model="resumeForm" label-width="100px" class="resume-form">
            <!-- ������Ϣ -->
            <div class="form-section">
              <h3>������Ϣ</h3>
              <el-form-item label="��������">
                <el-input v-model="resumeForm.name" placeholder="���磺�����ĸ��˼���"></el-input>
              </el-form-item>

              <el-form-item label="����">
                <el-input v-model="resumeForm.basicInfo.name"></el-input>
              </el-form-item>

              <el-form-item label="�Ա�">
                <el-radio-group v-model="resumeForm.basicInfo.gender">
                  <el-radio label="��">��</el-radio>
                  <el-radio label="Ů">Ů</el-radio>
                </el-radio-group>
              </el-form-item>

              <el-form-item label="����">
                <el-input-number v-model="resumeForm.basicInfo.age" :min="16" :max="100"></el-input-number>
              </el-form-item>

              <el-form-item label="���ڵ�">
                <el-cascader
                    v-model="resumeForm.basicInfo.location"
                    :options="cityOptions"
                    placeholder="��ѡ�����">
                </el-cascader>
              </el-form-item>

              <el-form-item label="�ֻ���">
                <el-input v-model="resumeForm.basicInfo.phone"></el-input>
              </el-form-item>

              <el-form-item label="����">
                <el-input v-model="resumeForm.basicInfo.email" type="email"></el-input>
              </el-form-item>
            </div>

            <!-- �������� -->
            <div class="form-section">
              <div class="section-header">
                <h3>��������</h3>
                <el-button type="primary" size="small" @click="addEducation">��ӽ�������</el-button>
              </div>

              <div v-for="(edu, index) in resumeForm.education" :key="index" class="education-item">
                <el-divider v-if="index > 0"></el-divider>
                <el-form-item label="ѧУ����">
                  <el-input v-model="edu.school"></el-input>
                </el-form-item>

                <el-form-item label="רҵ">
                  <el-input v-model="edu.major"></el-input>
                </el-form-item>

                <el-form-item label="ѧ��">
                  <el-select v-model="edu.degree" placeholder="��ѡ��ѧ��">
                    <el-option label="��ר" value="��ר"></el-option>
                    <el-option label="����" value="����"></el-option>
                    <el-option label="˶ʿ" value="˶ʿ"></el-option>
                    <el-option label="��ʿ" value="��ʿ"></el-option>
                  </el-select>
                </el-form-item>

                <el-form-item label="ʱ��">
                  <el-date-picker
                      v-model="edu.startDate"
                      type="month"
                      placeholder="��ʼʱ��"
                      style="width: 45%">
                  </el-date-picker>
                  <span style="margin: 0 10px">��</span>
                  <el-date-picker
                      v-model="edu.endDate"
                      type="month"
                      placeholder="����ʱ��"
                      style="width: 45%">
                  </el-date-picker>
                </el-form-item>

                <el-form-item label="����">
                  <el-input
                      type="textarea"
                      :rows="3"
                      v-model="edu.description"
                      placeholder="��������ѧϰ���������޿γ̵�">
                  </el-input>
                </el-form-item>

                <el-form-item>
                  <el-button type="danger" size="small" @click="removeEducation(index)">ɾ��</el-button>
                </el-form-item>
              </div>
            </div>

            <!-- �������� -->
            <div class="form-section">
              <div class="section-header">
                <h3>��������</h3>
                <el-button type="primary" size="small" @click="addExperience">��ӹ�������</el-button>
              </div>

              <div v-for="(exp, index) in resumeForm.experience" :key="index" class="experience-item">
                <el-divider v-if="index > 0"></el-divider>
                <el-form-item label="��˾����">
                  <el-input v-model="exp.company"></el-input>
                </el-form-item>

                <el-form-item label="ְλ">
                  <el-input v-model="exp.position"></el-input>
                </el-form-item>

                <el-form-item label="ʱ��">
                  <el-date-picker
                      v-model="exp.startDate"
                      type="month"
                      placeholder="��ʼʱ��"
                      style="width: 45%">
                  </el-date-picker>
                  <span style="margin: 0 10px">��</span>
                  <el-date-picker
                      v-model="exp.endDate"
                      type="month"
                      placeholder="����ʱ��"
                      style="width: 45%">
                  </el-date-picker>
                </el-form-item>

                <el-form-item label="��������">
                  <el-input
                      type="textarea"
                      :rows="4"
                      v-model="exp.description"
                      placeholder="�������Ĺ������ݡ�ְ��ͳɾ�">
                  </el-input>
                </el-form-item>

                <el-form-item>
                  <el-button type="danger" size="small" @click="removeExperience(index)">ɾ��</el-button>
                </el-form-item>
              </div>
            </div>

            <!-- ��Ŀ���� -->
            <div class="form-section">
              <div class="section-header">
                <h3>��Ŀ����</h3>
                <el-button type="primary" size="small" @click="addProject">�����Ŀ����</el-button>
              </div>

              <div v-for="(project, index) in resumeForm.projects" :key="index" class="project-item">
                <el-divider v-if="index > 0"></el-divider>
                <el-form-item label="��Ŀ����">
                  <el-input v-model="project.name"></el-input>
                </el-form-item>

                <el-form-item label="���ν�ɫ">
                  <el-input v-model="project.role"></el-input>
                </el-form-item>

                <el-form-item label="��Ŀʱ��">
                  <el-input v-model="project.time" placeholder="���磺2020.03 - 2020.08"></el-input>
                </el-form-item>

                <el-form-item label="��Ŀ����">
                  <el-input
                      type="textarea"
                      :rows="4"
                      v-model="project.description"
                      placeholder="������Ŀ����������ְ�����Ŀ�ɹ�">
                  </el-input>
                </el-form-item>

                <el-form-item>
                  <el-button type="danger" size="small" @click="removeProject(index)">ɾ��</el-button>
                </el-form-item>
              </div>
            </div>

            <el-form-item>
              <el-button type="primary" @click="saveResume">�������</el-button>
              <el-button @click="resetForm">����</el-button>
            </el-form-item>
          </el-form>
          
          <div v-else class="empty-resume">
            <p>�봴��һ���¼�����ʼ�༭</p>
            <el-button type="primary" @click="createNewResume">�½�����</el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
  </div>

</template>

<script>

export default {
  name: 'EditResume',
  components: {},
  data() {
    return {
      activeNav: 'resume',
      resumeList: [], // �û������м����б�
      selectedResumeId: null, // ��ǰѡ�еļ���ID
      selectedResume: null, // ��ǰѡ�еļ�������
      resumeForm: {
        name: '�����ĸ��˼���',
        basicInfo: {
          name: '����',
          gender: '��',
          age: 28,
          location: ['�й�', '����', '������'],
          phone: '13800138000',
          email: 'zhangsan@example.com'
        },
        education: [
          {
            school: '������ѧ',
            major: '�������ѧ�뼼��',
            degree: '����',
            startDate: '2010-09',
            endDate: '2014-06',
            description: '���޿γ�: ���ݽṹ���㷨������ϵͳ������������'
          }
        ],
        experience: [
          {
            company: '�ֽ�����',
            position: '�߼�ǰ�˿�������ʦ',
            startDate: '2018-07',
            endDate: '����',
            description: '����˾���Ĳ�Ʒ��ǰ�˿���������ʹ��Vue.js���'
          }
        ],
        projects: [
          {
            name: '��Ƹ��վǰ���ع�',
            role: 'ǰ�˸�����',
            time: '2020-03 - 2020-08',
            description: '�����Ŷ������Ƹ��վǰ���ع���ʹ��Vue.js + Element UI'
          }
        ]
      },
      cityOptions: [
        {
          value: '�й�',
          label: '�й�',
          children: [
            {
              value: '����',
              label: '����',
              children: [
                { value: '������', label: '������' }
              ]
            },
            {
              value: '�Ϻ�',
              label: '�Ϻ�',
              children: [
                { value: '�Ϻ���', label: '�Ϻ���' }
              ]
            }
          ]
        }
      ]
    }
  },
  mounted() {
    // ҳ�����ʱ��ȡ�û������м���
    this.loadResumeList();
  },
  methods: {
    handleNavSelect(index) {
      if (index !== 'resume') {
        this.$router.push('/PerSon');
      }
    },
    
    // �����û������м���
    loadResumeList() {
      const currentUser = JSON.parse(sessionStorage.getItem('user'));
      if (!currentUser) {
        this.$message.warning('���ȵ�¼');
        return;
      }
      
      this.$axios.get(`${this.$apiBaseUrl}/resume/list/${currentUser.id}`)
        .then(response => {
          if (response.data.code === 200) {
            this.resumeList = response.data.data;
            if (this.resumeList.length > 0) {
              // Ĭ��ѡ�е�һ������
              this.selectedResumeId = this.resumeList[0].id;
              this.loadResume(this.selectedResumeId);
            }
          } else {
            this.$message.error('��ȡ�����б�ʧ��: ' + response.data.msg);
          }
        })
        .catch(error => {
          console.error('��ȡ�����б�ʧ��:', error);
          this.$message.error('��ȡ�����б�ʧ��');
        });
    },
    
    // ����ѡ�еļ���
    loadResume(resumeId) {
      const resume = this.resumeList.find(item => item.id === resumeId);
      if (resume) {
        this.selectedResume = resume;
        // ������������
        this.resumeForm.name = resume.name;
        this.resumeForm.basicInfo = resume.basicInfo ? JSON.parse(resume.basicInfo) : {};
        this.resumeForm.education = resume.education ? JSON.parse(resume.education) : [];
        this.resumeForm.experience = resume.experience ? JSON.parse(resume.experience) : [];
        this.resumeForm.projects = resume.projects ? JSON.parse(resume.projects) : [];
      }
    },
    
    // �����¼���
    createNewResume() {
      const currentUser = JSON.parse(sessionStorage.getItem('user'));
      if (!currentUser) {
        this.$message.warning('���ȵ�¼');
        return;
      }
      
      // ����һ���յļ�������
      const newResume = {
        userId: currentUser.id,
        name: '�¼���',
        basicInfo: JSON.stringify({
          name: '',
          gender: '',
          age: null,
          location: [],
          phone: '',
          email: ''
        }),
        education: JSON.stringify([]),
        experience: JSON.stringify([]),
        projects: JSON.stringify([])
      };
      
      this.$axios.post(`${this.$apiBaseUrl}/resume/save`, newResume)
        .then(response => {
          if (response.data.code === 200) {
            this.$message.success('�¼��������ɹ�');
            // ���¼��ؼ����б�
            this.loadResumeList();
          } else {
            this.$message.error('�����¼���ʧ��: ' + response.data.msg);
          }
        })
        .catch(error => {
          console.error('�����¼���ʧ��:', error);
          this.$message.error('�����¼���ʧ��');
        });
    },
    
    // ����Ĭ�ϼ���
    setDefaultResume() {
      const currentUser = JSON.parse(sessionStorage.getItem('user'));
      if (!currentUser || !this.selectedResume) {
        this.$message.warning('����ѡ�����');
        return;
      }
      
      this.$axios.post(`${this.$apiBaseUrl}/resume/set-default`, null, {
        params: {
          resumeId: this.selectedResume.id,
          userId: currentUser.id
        }
      })
        .then(response => {
          if (response.data.code === 200) {
            this.$message.success('Ĭ�ϼ������óɹ�');
            // ���¼��ؼ����б�
            this.loadResumeList();
          } else {
            this.$message.error('����Ĭ�ϼ���ʧ��: ' + response.data.msg);
          }
        })
        .catch(error => {
          console.error('����Ĭ�ϼ���ʧ��:', error);
          this.$message.error('����Ĭ�ϼ���ʧ��');
        });
    },
    
    // ɾ������
    deleteResume() {
      if (!this.selectedResume) {
        this.$message.warning('����ѡ�����');
        return;
      }
      
      this.$confirm('ȷ��Ҫɾ����ݼ�����?', '��ʾ', {
        confirmButtonText: 'ȷ��',
        cancelButtonText: 'ȡ��',
        type: 'warning'
      }).then(() => {
        this.$axios.delete(`${this.$apiBaseUrl}/resume/${this.selectedResume.id}`)
          .then(response => {
            if (response.data.code === 200) {
              this.$message.success('����ɾ���ɹ�');
              // ���¼��ؼ����б�
              this.loadResumeList();
              // ��յ�ǰѡ��
              this.selectedResume = null;
              this.selectedResumeId = null;
            } else {
              this.$message.error('ɾ������ʧ��: ' + response.data.msg);
            }
          })
          .catch(error => {
            console.error('ɾ������ʧ��:', error);
            this.$message.error('ɾ������ʧ��');
          });
      }).catch(() => {
        this.$message.info('��ȡ��ɾ��');
      });
    },
    
    addEducation() {
      this.resumeForm.education.push({
        school: '',
        major: '',
        degree: '',
        startDate: '',
        endDate: '',
        description: ''
      });
    },
    removeEducation(index) {
      if (this.resumeForm.education.length > 1) {
        this.resumeForm.education.splice(index, 1);
      } else {
        this.$message.warning('���ٱ���һ����������');
      }
    },
    addExperience() {
      this.resumeForm.experience.push({
        company: '',
        position: '',
        startDate: '',
        endDate: '',
        description: ''
      });
    },
    removeExperience(index) {
      if (this.resumeForm.experience.length > 1) {
        this.resumeForm.experience.splice(index, 1);
      } else {
        this.$message.warning('���ٱ���һ����������');
      }
    },
    addProject() {
      this.resumeForm.projects.push({
        name: '',
        role: '',
        time: '',
        description: ''
      });
    },
    removeProject(index) {
      this.resumeForm.projects.splice(index, 1);
    },
    saveResume() {
      // ��sessionStorage��ȡ��ǰ�û���Ϣ
      const currentUser = JSON.parse(sessionStorage.getItem('user'));
      if (!currentUser) {
        this.$message({
          message: '���ȵ�¼',
          type: 'warning'
        });
        return;
      }

      // ����Ҫ���͵���˵ļ�������
      const resume = {
        id: this.selectedResume ? this.selectedResume.id : null,
        userId: currentUser.id,
        name: this.resumeForm.name,
        basicInfo: JSON.stringify(this.resumeForm.basicInfo),
        education: JSON.stringify(this.resumeForm.education),
        experience: JSON.stringify(this.resumeForm.experience),
        projects: JSON.stringify(this.resumeForm.projects)
      };

      // ���ú��API�������
      this.$axios.post(`${this.$apiBaseUrl}/resume/save`, resume)
        .then(response => {
          if (response.data.code === 200) {
            this.$message.success('��������ɹ�');
            // ���¼��ؼ����б�
            this.loadResumeList();
          } else {
            this.$message.error('��������ʧ��: ' + response.data.msg);
          }
        })
        .catch(error => {
          console.error('��������ʧ��:', error);
          this.$message.error('��������ʧ��');
        });
    },
    resetForm() {
      this.$confirm('ȷ��Ҫ��������������?', '��ʾ', {
        confirmButtonText: 'ȷ��',
        cancelButtonText: 'ȡ��',
        type: 'warning'
      }).then(() => {
        // ���ñ���߼�
        this.resumeForm = {
          name: '�¼���',
          basicInfo: {
            name: '',
            gender: '',
            age: null,
            location: [],
            phone: '',
            email: ''
          },
          education: [],
          experience: [],
          projects: []
        };
        this.$message.info('���������');
      }).catch(() => {
        this.$message.info('��ȡ������');
      });
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
    certificate(){
      this.$router.push('/Certification');
    },

    goToPosition() {
      this.$router.push('/Position');
    },
    goToCompany() {
      this.$router.push('/company');
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
      this.$confirm('ȷ��Ҫ�˳���¼��?', '��ʾ', {
        confirmButtonText: 'ȷ��',
        cancelButtonText: 'ȡ��',
        type: 'warning'
      }).then(() => {
        this.$router.push('/');
        this.$message({
          type: 'success',
          message: '�˳��ɹ�!'
        });
      }).catch(() => {
        this.$message({
          type: 'info',
          message: '��ȡ������'
        });
      });
    }
  }
}
</script>

<style scoped>
.edit-resume-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.header {
  background: linear-gradient(135deg, #3498db, #2c3e50);
  color: white;
  padding: 15px 0;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 100;
}

.main-content {
  display: flex;
  margin: 30px 0 40px;
}

.side-nav {
  width: 220px;
  margin-right: 20px;
}

.el-menu-vertical {
  height: 100%;
  border-right: none;
}

.el-menu-vertical i {
  margin-right: 8px;
}

.content-area {
  flex: 1;
  background: white;
  border-radius: 4px;
  padding: 30px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.section-title {
  font-size: 20px;
  font-weight: bold;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #ebeef5;
}

.resume-management {
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #ebeef5;
}

.resume-selector {
  display: flex;
  gap: 15px;
  align-items: center;
}

.resume-selector .el-select {
  flex: 1;
}

.resume-form {
  max-width: 800px;
}

.form-section {
  margin-bottom: 30px;
}

.form-section h3 {
  margin-top: 0;
  margin-bottom: 20px;
  color: #303133;
  padding-bottom: 10px;
  border-bottom: 1px solid #ebeef5;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-header h3 {
  margin: 0;
}

.education-item,
.experience-item,
.project-item {
  padding: 15px 0;
}

.empty-resume {
  text-align: center;
  padding: 50px 0;
  color: #909399;
}

.empty-resume p {
  font-size: 16px;
  margin-bottom: 20px;
}

@media (max-width: 992px) {
  .main-content {
    flex-direction: column;
  }

  .side-nav {
    width: 100%;
    margin-right: 0;
    margin-bottom: 20px;
  }
  
  .resume-selector {
    flex-direction: column;
    align-items: stretch;
  }
  
  .resume-selector .el-button {
    margin-top: 10px;
  }
}
</style>