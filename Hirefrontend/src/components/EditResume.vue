<template>
  <div class="edit-resume-container">
    <!-- 顶部导航 -->
    <AppHeader />

    <div class="container">
      <div class="main-content">
        <!-- 左侧导航 -->
        <div class="side-nav">
          <el-menu
              :default-active="activeNav"
              class="el-menu-vertical"
              @select="handleNavSelect">
            <el-menu-item index="profile">
              <i class="fas fa-user"></i>
              <span>个人资料</span>
            </el-menu-item>
            <el-menu-item index="resume">
              <i class="fas fa-file-alt"></i>
              <span>我的简历</span>
            </el-menu-item>
            <el-menu-item index="applications">
              <i class="fas fa-paper-plane"></i>
              <span>职位申请</span>
            </el-menu-item>
            <el-menu-item index="interviews">
              <i class="fas fa-handshake"></i>
              <span>面试安排</span>
            </el-menu-item>
          </el-menu>
        </div>

        <!-- 右侧内容区 -->
        <div class="content-area">
          <div class="section-title">编辑简历</div>
          
          <!-- 简历选择和管理 -->
          <div class="resume-management">
            <div class="resume-selector">
              <el-select v-model="selectedResumeId" placeholder="选择简历" @change="loadResume">
                <el-option
                  v-for="item in resumeList"
                  :key="item.id"
                  :label="item.name + (item.isDefault ? ' (默认)' : '')"
                  :value="item.id">
                </el-option>
              </el-select>
              <el-button type="primary" @click="createNewResume" size="small">新建简历</el-button>
              <el-button v-if="selectedResume && !selectedResume.isDefault" type="success" @click="setDefaultResume" size="small">设为默认</el-button>
              <el-button v-if="selectedResume && !selectedResume.isDefault" type="danger" @click="deleteResume" size="small">删除</el-button>
            </div>
          </div>

          <el-form v-if="selectedResume" :model="resumeForm" label-width="100px" class="resume-form">
            <!-- 基本信息 -->
            <div class="form-section">
              <h3>基本信息</h3>
              <el-form-item label="简历名称">
                <el-input v-model="resumeForm.name" placeholder="例如：张三的个人简历"></el-input>
              </el-form-item>

              <el-form-item label="姓名">
                <el-input v-model="resumeForm.basicInfo.name"></el-input>
              </el-form-item>

              <el-form-item label="性别">
                <el-radio-group v-model="resumeForm.basicInfo.gender">
                  <el-radio label="男">男</el-radio>
                  <el-radio label="女">女</el-radio>
                </el-radio-group>
              </el-form-item>

              <el-form-item label="年龄">
                <el-input-number v-model="resumeForm.basicInfo.age" :min="16" :max="100"></el-input-number>
              </el-form-item>

              <el-form-item label="所在地">
                <el-cascader
                    v-model="resumeForm.basicInfo.location"
                    :options="cityOptions"
                    placeholder="请选择地区">
                </el-cascader>
              </el-form-item>

              <el-form-item label="手机号">
                <el-input v-model="resumeForm.basicInfo.phone"></el-input>
              </el-form-item>

              <el-form-item label="邮箱">
                <el-input v-model="resumeForm.basicInfo.email" type="email"></el-input>
              </el-form-item>
            </div>

            <!-- 教育背景 -->
            <div class="form-section">
              <div class="section-header">
                <h3>教育背景</h3>
                <el-button type="primary" size="small" @click="addEducation">添加教育经历</el-button>
              </div>

              <div v-for="(edu, index) in resumeForm.education" :key="index" class="education-item">
                <el-divider v-if="index > 0"></el-divider>
                <el-form-item label="学校名称">
                  <el-input v-model="edu.school"></el-input>
                </el-form-item>

                <el-form-item label="专业">
                  <el-input v-model="edu.major"></el-input>
                </el-form-item>

                <el-form-item label="学历">
                  <el-select v-model="edu.degree" placeholder="请选择学历">
                    <el-option label="大专" value="大专"></el-option>
                    <el-option label="本科" value="本科"></el-option>
                    <el-option label="硕士" value="硕士"></el-option>
                    <el-option label="博士" value="博士"></el-option>
                  </el-select>
                </el-form-item>

                <el-form-item label="时间">
                  <el-date-picker
                      v-model="edu.startDate"
                      type="month"
                      placeholder="开始时间"
                      style="width: 45%">
                  </el-date-picker>
                  <span style="margin: 0 10px">至</span>
                  <el-date-picker
                      v-model="edu.endDate"
                      type="month"
                      placeholder="结束时间"
                      style="width: 45%">
                  </el-date-picker>
                </el-form-item>

                <el-form-item label="描述">
                  <el-input
                      type="textarea"
                      :rows="3"
                      v-model="edu.description"
                      placeholder="描述您的学习经历、主修课程等">
                  </el-input>
                </el-form-item>

                <el-form-item>
                  <el-button type="danger" size="small" @click="removeEducation(index)">删除</el-button>
                </el-form-item>
              </div>
            </div>

            <!-- 工作经历 -->
            <div class="form-section">
              <div class="section-header">
                <h3>工作经历</h3>
                <el-button type="primary" size="small" @click="addExperience">添加工作经历</el-button>
              </div>

              <div v-for="(exp, index) in resumeForm.experience" :key="index" class="experience-item">
                <el-divider v-if="index > 0"></el-divider>
                <el-form-item label="公司名称">
                  <el-input v-model="exp.company"></el-input>
                </el-form-item>

                <el-form-item label="职位">
                  <el-input v-model="exp.position"></el-input>
                </el-form-item>

                <el-form-item label="时间">
                  <el-date-picker
                      v-model="exp.startDate"
                      type="month"
                      placeholder="开始时间"
                      style="width: 45%">
                  </el-date-picker>
                  <span style="margin: 0 10px">至</span>
                  <el-date-picker
                      v-model="exp.endDate"
                      type="month"
                      placeholder="结束时间"
                      style="width: 45%">
                  </el-date-picker>
                </el-form-item>

                <el-form-item label="工作描述">
                  <el-input
                      type="textarea"
                      :rows="4"
                      v-model="exp.description"
                      placeholder="描述您的工作内容、职责和成就">
                  </el-input>
                </el-form-item>

                <el-form-item>
                  <el-button type="danger" size="small" @click="removeExperience(index)">删除</el-button>
                </el-form-item>
              </div>
            </div>

            <!-- 项目经验 -->
            <div class="form-section">
              <div class="section-header">
                <h3>项目经验</h3>
                <el-button type="primary" size="small" @click="addProject">添加项目经验</el-button>
              </div>

              <div v-for="(project, index) in resumeForm.projects" :key="index" class="project-item">
                <el-divider v-if="index > 0"></el-divider>
                <el-form-item label="项目名称">
                  <el-input v-model="project.name"></el-input>
                </el-form-item>

                <el-form-item label="担任角色">
                  <el-input v-model="project.role"></el-input>
                </el-form-item>

                <el-form-item label="项目时间">
                  <el-input v-model="project.time" placeholder="例如：2020.03 - 2020.08"></el-input>
                </el-form-item>

                <el-form-item label="项目描述">
                  <el-input
                      type="textarea"
                      :rows="4"
                      v-model="project.description"
                      placeholder="描述项目背景、您的职责和项目成果">
                  </el-input>
                </el-form-item>

                <el-form-item>
                  <el-button type="danger" size="small" @click="removeProject(index)">删除</el-button>
                </el-form-item>
              </div>
            </div>

            <el-form-item>
              <el-button type="primary" @click="saveResume">保存简历</el-button>
              <el-button @click="resetForm">重置</el-button>
            </el-form-item>
          </el-form>
          
          <div v-else class="empty-resume">
            <p>请创建一份新简历开始编辑</p>
            <el-button type="primary" @click="createNewResume">新建简历</el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import AppHeader from '@/components/common/AppHeader.vue'

export default {
  name: 'EditResume',
  components: {
    AppHeader
  },
  data() {
    return {
      activeNav: 'resume',
      resumeList: [], // 用户的所有简历列表
      selectedResumeId: null, // 当前选中的简历ID
      selectedResume: null, // 当前选中的简历对象
      resumeForm: {
        name: '张三的个人简历',
        basicInfo: {
          name: '张三',
          gender: '男',
          age: 28,
          location: ['中国', '北京', '北京市'],
          phone: '13800138000',
          email: 'zhangsan@example.com'
        },
        education: [
          {
            school: '北京大学',
            major: '计算机科学与技术',
            degree: '本科',
            startDate: '2010-09',
            endDate: '2014-06',
            description: '主修课程: 数据结构、算法、操作系统、计算机网络等'
          }
        ],
        experience: [
          {
            company: '字节跳动',
            position: '高级前端开发工程师',
            startDate: '2018-07',
            endDate: '至今',
            description: '负责公司核心产品的前端开发工作，使用Vue.js框架'
          }
        ],
        projects: [
          {
            name: '招聘网站前端重构',
            role: '前端负责人',
            time: '2020-03 - 2020-08',
            description: '带领团队完成招聘网站前端重构，使用Vue.js + Element UI'
          }
        ]
      },
      cityOptions: [
        {
          value: '中国',
          label: '中国',
          children: [
            {
              value: '北京',
              label: '北京',
              children: [
                { value: '北京市', label: '北京市' }
              ]
            },
            {
              value: '上海',
              label: '上海',
              children: [
                { value: '上海市', label: '上海市' }
              ]
            }
          ]
        }
      ]
    }
  },
  mounted() {
    // 页面加载时获取用户的所有简历
    this.loadResumeList();
  },
  methods: {
    handleNavSelect(index) {
      if (index !== 'resume') {
        this.$router.push('/PerSon');
      }
    },
    
    // 加载用户的所有简历
    loadResumeList() {
      const currentUser = JSON.parse(sessionStorage.getItem('user'));
      if (!currentUser) {
        this.$message.warning('请先登录');
        return;
      }
      
      this.$axios.get(`${this.$apiBaseUrl}/resume/list/${currentUser.id}`)
        .then(response => {
          if (response.data.code === 200) {
            this.resumeList = response.data.data;
            if (this.resumeList.length > 0) {
              // 默认选中第一个简历
              this.selectedResumeId = this.resumeList[0].id;
              this.loadResume(this.selectedResumeId);
            }
          } else {
            this.$message.error('获取简历列表失败: ' + response.data.msg);
          }
        })
        .catch(error => {
          console.error('获取简历列表失败:', error);
          this.$message.error('获取简历列表失败');
        });
    },
    
    // 加载选中的简历
    loadResume(resumeId) {
      const resume = this.resumeList.find(item => item.id === resumeId);
      if (resume) {
        this.selectedResume = resume;
        // 解析简历数据
        this.resumeForm.name = resume.name;
        this.resumeForm.basicInfo = resume.basicInfo ? JSON.parse(resume.basicInfo) : {};
        this.resumeForm.education = resume.education ? JSON.parse(resume.education) : [];
        this.resumeForm.experience = resume.experience ? JSON.parse(resume.experience) : [];
        this.resumeForm.projects = resume.projects ? JSON.parse(resume.projects) : [];
      }
    },
    
    // 创建新简历
    createNewResume() {
      const currentUser = JSON.parse(sessionStorage.getItem('user'));
      if (!currentUser) {
        this.$message.warning('请先登录');
        return;
      }
      
      // 创建一个空的简历对象
      const newResume = {
        userId: currentUser.id,
        name: '新简历',
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
            this.$message.success('新简历创建成功');
            // 重新加载简历列表
            this.loadResumeList();
          } else {
            this.$message.error('创建新简历失败: ' + response.data.msg);
          }
        })
        .catch(error => {
          console.error('创建新简历失败:', error);
          this.$message.error('创建新简历失败');
        });
    },
    
    // 设置默认简历
    setDefaultResume() {
      const currentUser = JSON.parse(sessionStorage.getItem('user'));
      if (!currentUser || !this.selectedResume) {
        this.$message.warning('请先选择简历');
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
            this.$message.success('默认简历设置成功');
            // 重新加载简历列表
            this.loadResumeList();
          } else {
            this.$message.error('设置默认简历失败: ' + response.data.msg);
          }
        })
        .catch(error => {
          console.error('设置默认简历失败:', error);
          this.$message.error('设置默认简历失败');
        });
    },
    
    // 删除简历
    deleteResume() {
      if (!this.selectedResume) {
        this.$message.warning('请先选择简历');
        return;
      }
      
      this.$confirm('确定要删除这份简历吗?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.$axios.delete(`${this.$apiBaseUrl}/resume/${this.selectedResume.id}`)
          .then(response => {
            if (response.data.code === 200) {
              this.$message.success('简历删除成功');
              // 重新加载简历列表
              this.loadResumeList();
              // 清空当前选中
              this.selectedResume = null;
              this.selectedResumeId = null;
            } else {
              this.$message.error('删除简历失败: ' + response.data.msg);
            }
          })
          .catch(error => {
            console.error('删除简历失败:', error);
            this.$message.error('删除简历失败');
          });
      }).catch(() => {
        this.$message.info('已取消删除');
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
        this.$message.warning('至少保留一条教育经历');
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
        this.$message.warning('至少保留一条工作经历');
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
      // 从sessionStorage获取当前用户信息
      const currentUser = JSON.parse(sessionStorage.getItem('user'));
      if (!currentUser) {
        this.$message({
          message: '请先登录',
          type: 'warning'
        });
        return;
      }

      // 构造要发送到后端的简历对象
      const resume = {
        id: this.selectedResume ? this.selectedResume.id : null,
        userId: currentUser.id,
        name: this.resumeForm.name,
        basicInfo: JSON.stringify(this.resumeForm.basicInfo),
        education: JSON.stringify(this.resumeForm.education),
        experience: JSON.stringify(this.resumeForm.experience),
        projects: JSON.stringify(this.resumeForm.projects)
      };

      // 调用后端API保存简历
      this.$axios.post(`${this.$apiBaseUrl}/resume/save`, resume)
        .then(response => {
          if (response.data.code === 200) {
            this.$message.success('简历保存成功');
            // 重新加载简历列表
            this.loadResumeList();
          } else {
            this.$message.error('简历保存失败: ' + response.data.msg);
          }
        })
        .catch(error => {
          console.error('简历保存失败:', error);
          this.$message.error('简历保存失败');
        });
    },
    resetForm() {
      this.$confirm('确定要重置所有内容吗?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        // 重置表单逻辑
        this.resumeForm = {
          name: '新简历',
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
        this.$message.info('表单已重置');
      }).catch(() => {
        this.$message.info('已取消重置');
      });
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
     * 跳转到消息中心
     */
    goToMessageCenter() {
      this.$router.push({
        name: 'MessageCenter'
      });
    },
    logout() {
      this.$confirm('确定要退出登录吗?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.$router.push('/');
        this.$message({
          type: 'success',
          message: '退出成功!'
        });
      }).catch(() => {
        this.$message({
          type: 'info',
          message: '已取消操作'
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