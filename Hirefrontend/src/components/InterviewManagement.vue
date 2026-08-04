<template>
  <div class="interview-management">
    <!-- 顶部导航 -->
    <AppHeader />

    <!-- 主要内容 -->
    <div class="main-content">
      <div class="container">
        <h2 class="page-title">
          <i class="fas fa-handshake"></i>
          {{ isHR ? '面试安排管理' : '我的面试邀请' }}
        </h2>

        <!-- 统计卡片 -->
        <div class="stats-cards">
          <div class="stat-card">
            <div class="stat-value">{{ stats.total }}</div>
            <div class="stat-label">总面试数</div>
          </div>
          <div class="stat-card scheduled">
            <div class="stat-value">{{ stats.scheduled }}</div>
            <div class="stat-label">已安排</div>
          </div>
          <div class="stat-card confirmed">
            <div class="stat-value">{{ stats.confirmed }}</div>
            <div class="stat-label">已确认</div>
          </div>
          <div class="stat-card completed">
            <div class="stat-value">{{ stats.completed }}</div>
            <div class="stat-label">已完成</div>
          </div>
        </div>

        <!-- 筛选器 -->
        <div class="filter-section">
          <el-select v-model="filterStatus" placeholder="按状态筛选" clearable @change="loadInterviews">
            <el-option label="已安排" value="scheduled"></el-option>
            <el-option label="已确认" value="confirmed"></el-option>
            <el-option label="已完成" value="completed"></el-option>
            <el-option label="已取消" value="cancelled"></el-option>
            <el-option label="未出席" value="no_show"></el-option>
          </el-select>
          <el-button type="primary" @click="loadInterviews">刷新</el-button>
        </div>

        <!-- 面试列表 -->
        <el-table :data="filteredInterviews" style="width: 100%" v-loading="loading">
          <el-table-column prop="jobTitle" label="职位" width="180"></el-table-column>
          <el-table-column prop="companyName" label="公司" width="150" v-if="!isHR"></el-table-column>
          <el-table-column prop="candidateName" label="候选人" width="120" v-if="isHR"></el-table-column>
          
          <el-table-column label="面试时间" width="180">
            <template #default="{row}">
              {{ formatDateTime(row.interviewTime) }}
            </template>
          </el-table-column>
          
          <el-table-column label="面试类型" width="120">
            <template #default="{row}">
              <el-tag :type="getInterviewTypeTag(row.interviewType)">
                {{ getInterviewTypeText(row.interviewType) }}
              </el-tag>
            </template>
          </el-table-column>
          
          <el-table-column label="地点/链接" min-width="200">
            <template #default="{row}">
              <div v-if="row.interviewType === 'onsite'">
                <i class="fas fa-map-marker-alt"></i> {{ row.location || '待定' }}
              </div>
              <div v-else-if="row.meetingUrl">
                <el-link type="primary" :href="row.meetingUrl" target="_blank">
                  <i class="fas fa-video"></i> 进入会议室
                </el-link>
              </div>
              <div v-else>电话面试</div>
            </template>
          </el-table-column>
          
          <el-table-column prop="interviewer" label="面试官" width="120" v-if="!isHR"></el-table-column>
          
          <el-table-column label="状态" width="100">
            <template #default="{row}">
              <el-tag :type="getStatusTag(row.status)">
                {{ getStatusText(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          
          <el-table-column label="操作" width="280" fixed="right">
            <template #default="{row}">
              <!-- 求职者操作 -->
              <template v-if="!isHR">
                <el-button 
                  v-if="row.status === 'scheduled'" 
                  size="mini" 
                  type="success"
                  @click="confirmInterview(row, true)">
                  确认参加
                </el-button>
                <el-button 
                  v-if="row.status === 'scheduled'" 
                  size="mini" 
                  type="danger"
                  @click="confirmInterview(row, false)">
                  拒绝
                </el-button>
              </template>
              
              <!-- HR操作 -->
              <template v-else>
                <el-button 
                  size="mini" 
                  type="primary"
                  @click="viewDetail(row)">
                  详情
                </el-button>
                <el-button 
                  v-if="row.status === 'confirmed'" 
                  size="mini" 
                  type="success"
                  @click="completeInterview(row)">
                  完成面试
                </el-button>
                <el-button 
                  size="mini" 
                  type="warning"
                  @click="editInterview(row)">
                  编辑
                </el-button>
              </template>
            </template>
          </el-table-column>
        </el-table>

        <!-- 空状态 -->
        <el-empty v-if="!loading && filteredInterviews.length === 0" description="暂无面试记录"></el-empty>
      </div>
    </div>

    <!-- 安排/编辑面试对话框 -->
    <el-dialog 
      :title="dialogMode === 'edit' ? '编辑面试' : '安排面试'" 
      :visible.sync="dialogVisible"
      width="600px">
      <el-form :model="interviewForm" :rules="interviewRules" ref="interviewForm" label-width="120px">
        <el-form-item label="面试时间" prop="interviewTime">
          <el-date-picker
            v-model="interviewForm.interviewTime"
            type="datetime"
            placeholder="选择面试时间"
            style="width: 100%">
          </el-date-picker>
        </el-form-item>
        
        <el-form-item label="面试类型" prop="interviewType">
          <el-select v-model="interviewForm.interviewType" placeholder="选择面试类型" style="width: 100%">
            <el-option label="电话面试" value="phone"></el-option>
            <el-option label="视频面试" value="video"></el-option>
            <el-option label="现场面试" value="onsite"></el-option>
          </el-select>
        </el-form-item>
        
        <el-form-item 
          v-if="interviewForm.interviewType === 'onsite'" 
          label="面试地点" 
          prop="location">
          <el-input v-model="interviewForm.location" placeholder="请输入面试地点"></el-input>
        </el-form-item>
        
        <el-form-item 
          v-if="interviewForm.interviewType === 'video'" 
          label="会议链接" 
          prop="meetingUrl">
          <el-input v-model="interviewForm.meetingUrl" placeholder="请输入视频会议链接"></el-input>
        </el-form-item>
        
        <el-form-item label="面试官" prop="interviewer">
          <el-input v-model="interviewForm.interviewer" placeholder="请输入面试官姓名"></el-input>
        </el-form-item>
        
        <el-form-item label="联系电话" prop="interviewerPhone">
          <el-input v-model="interviewForm.interviewerPhone" placeholder="请输入面试官联系电话"></el-input>
        </el-form-item>
        
        <el-form-item label="备注">
          <el-input 
            type="textarea" 
            :rows="3"
            v-model="interviewForm.remark" 
            placeholder="备注信息（可选）">
          </el-input>
        </el-form-item>
      </el-form>
      
      <span slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitInterview">确 定</el-button>
      </span>
    </el-dialog>

    <!-- 面试反馈对话框 -->
    <el-dialog 
      title="面试反馈" 
      :visible.sync="feedbackDialogVisible"
      width="500px">
      <el-form :model="feedbackForm" label-width="100px">
        <el-form-item label="面试评分">
          <el-rate v-model="feedbackForm.rating" :max="5"></el-rate>
        </el-form-item>
        
        <el-form-item label="面试反馈">
          <el-input 
            type="textarea" 
            :rows="5"
            v-model="feedbackForm.feedback" 
            placeholder="请输入面试反馈">
          </el-input>
        </el-form-item>
      </el-form>
      
      <span slot="footer" class="dialog-footer">
        <el-button @click="feedbackDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitFeedback">提 交</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import AppHeader from '@/components/common/AppHeader.vue'

export default {
  name: 'InterviewManagement',
  components: {
    AppHeader
  },
  data() {
    return {
      isHR: false,
      loading: false,
      interviews: [],
      filterStatus: '',
      dialogVisible: false,
      dialogMode: 'create', // 'create' or 'edit'
      feedbackDialogVisible: false,
      currentInterview: null,
      
      stats: {
        total: 0,
        scheduled: 0,
        confirmed: 0,
        completed: 0
      },
      
      interviewForm: {
        applicationId: null,
        interviewTime: '',
        interviewType: 'phone',
        location: '',
        meetingUrl: '',
        interviewer: '',
        interviewerPhone: '',
        remark: ''
      },
      
      interviewRules: {
        interviewTime: [
          { required: true, message: '请选择面试时间', trigger: 'change' }
        ],
        interviewType: [
          { required: true, message: '请选择面试类型', trigger: 'change' }
        ]
      },
      
      feedbackForm: {
        rating: 0,
        feedback: ''
      }
    };
  },
  
  computed: {
    currentUser() {
      const userStr = sessionStorage.getItem('user');
      return userStr ? JSON.parse(userStr) : null;
    },
    
    filteredInterviews() {
      if (!this.filterStatus) {
        return this.interviews;
      }
      return this.interviews.filter(item => item.status === this.filterStatus);
    }
  },
  
  mounted() {
    this.checkUserRole();
    this.loadInterviews();
  },
  
  methods: {
    checkUserRole() {
      if (this.currentUser) {
        this.isHR = this.currentUser.role === 1;
      }
    },
    
    async loadInterviews() {
      try {
        this.loading = true;
        
        let url = '';
        if (this.isHR) {
          // HR查看公司的面试
          url = `${this.$apiBaseUrl}/interview/company/${this.currentUser.companyId}`;
        } else {
          // 求职者查看自己的面试
          url = `${this.$apiBaseUrl}/interview/user/${this.currentUser.id}`;
        }
        
        const response = await this.$axios.get(url);
        
        if (response.data.code === 200) {
          this.interviews = response.data.data || [];
          this.calculateStats();
        } else {
          this.$message.error(response.data.message || '加载失败');
        }
      } catch (error) {
        console.error('加载面试列表失败:', error);
        this.$message.error('加载面试列表失败');
      } finally {
        this.loading = false;
      }
    },
    
    calculateStats() {
      this.stats.total = this.interviews.length;
      this.stats.scheduled = this.interviews.filter(i => i.status === 'scheduled').length;
      this.stats.confirmed = this.interviews.filter(i => i.status === 'confirmed').length;
      this.stats.completed = this.interviews.filter(i => i.status === 'completed').length;
    },
    
    async confirmInterview(interview, confirmed) {
      try {
        const response = await this.$axios.post(
          `${this.$apiBaseUrl}/interview/confirm/${interview.id}`,
          null,
          {
            params: {
              userId: this.currentUser.id,
              confirmed: confirmed
            }
          }
        );
        
        if (response.data.code === 200) {
          this.$message.success(confirmed ? '已确认参加面试' : '已拒绝面试邀请');
          this.loadInterviews();
        } else {
          this.$message.error(response.data.message || '操作失败');
        }
      } catch (error) {
        console.error('确认面试失败:', error);
        this.$message.error('操作失败');
      }
    },
    
    viewDetail(interview) {
      this.currentInterview = interview;
      this.$alert(`
        <p><strong>职位：</strong>${interview.jobTitle}</p>
        <p><strong>候选人：</strong>${interview.candidateName}</p>
        <p><strong>面试时间：</strong>${this.formatDateTime(interview.interviewTime)}</p>
        <p><strong>面试类型：</strong>${this.getInterviewTypeText(interview.interviewType)}</p>
        <p><strong>面试官：</strong>${interview.interviewer || '待定'}</p>
        <p><strong>状态：</strong>${this.getStatusText(interview.status)}</p>
        ${interview.feedback ? `<p><strong>反馈：</strong>${interview.feedback}</p>` : ''}
        ${interview.rating ? `<p><strong>评分：</strong>${interview.rating}分</p>` : ''}
      `, '面试详情', {
        dangerouslyUseHTMLString: true
      });
    },
    
    editInterview(interview) {
      this.dialogMode = 'edit';
      this.currentInterview = interview;
      this.interviewForm = {
        ...interview,
        interviewTime: interview.interviewTime ? new Date(interview.interviewTime) : ''
      };
      this.dialogVisible = true;
    },
    
    completeInterview(interview) {
      this.currentInterview = interview;
      this.feedbackForm = {
        rating: 0,
        feedback: ''
      };
      this.feedbackDialogVisible = true;
    },
    
    async submitFeedback() {
      try {
        const response = await this.$axios.put(
          `${this.$apiBaseUrl}/interview/status/${this.currentInterview.id}`,
          null,
          {
            params: {
              status: 'completed',
              feedback: this.feedbackForm.feedback,
              rating: this.feedbackForm.rating
            }
          }
        );
        
        if (response.data.code === 200) {
          this.$message.success('反馈提交成功');
          this.feedbackDialogVisible = false;
          this.loadInterviews();
        } else {
          this.$message.error(response.data.message || '提交失败');
        }
      } catch (error) {
        console.error('提交反馈失败:', error);
        this.$message.error('提交失败');
      }
    },
    
    submitInterview() {
      this.$refs.interviewForm.validate(async (valid) => {
        if (valid) {
          try {
            const formData = {
              ...this.interviewForm,
              interviewTime: this.interviewForm.interviewTime instanceof Date 
                ? this.interviewForm.interviewTime.toISOString().replace('T', ' ').substring(0, 19)
                : this.interviewForm.interviewTime
            };
            
            let response;
            if (this.dialogMode === 'create') {
              // 创建新面试
              response = await this.$axios.post(
                `${this.$apiBaseUrl}/interview/schedule`,
                formData
              );
            } else {
              // 更新现有面试
              response = await this.$axios.put(
                `${this.$apiBaseUrl}/interview/${this.currentInterview.id}`,
                formData
              );
            }
            
            if (response.data.code === 200) {
              this.$message.success(this.dialogMode === 'create' ? '面试安排成功' : '修改成功');
              this.dialogVisible = false;
              this.loadInterviews();
            } else {
              this.$message.error(response.data.message || '操作失败');
            }
          } catch (error) {
            console.error('提交失败:', error);
            this.$message.error('操作失败');
          }
        }
      });
    },
    
    formatDateTime(dateTime) {
      if (!dateTime) return '';
      const date = new Date(dateTime);
      return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      });
    },
    
    getInterviewTypeTag(type) {
      const tagMap = {
        'phone': '',
        'video': 'success',
        'onsite': 'warning'
      };
      return tagMap[type] || '';
    },
    
    getInterviewTypeText(type) {
      const textMap = {
        'phone': '电话面试',
        'video': '视频面试',
        'onsite': '现场面试'
      };
      return textMap[type] || type;
    },
    
    getStatusTag(status) {
      const tagMap = {
        'scheduled': '',
        'confirmed': 'success',
        'completed': 'info',
        'cancelled': 'danger',
        'no_show': 'warning'
      };
      return tagMap[status] || '';
    },
    
    getStatusText(status) {
      const textMap = {
        'scheduled': '已安排',
        'confirmed': '已确认',
        'completed': '已完成',
        'cancelled': '已取消',
        'no_show': '未出席'
      };
      return textMap[status] || status;
    },
    
    goToHome() {
      if (this.isHR) {
        this.$router.push('/HrHomePage');
      } else {
        this.$router.push('/HomePage');
      }
    },
    
    goBack() {
      this.$router.back();
    }
  }
};
</script>

<style scoped>
.interview-management {
  min-height: 100vh;
  background: #f5f7fa;
}

.main-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 30px 20px;
}

.page-title {
  font-size: 28px;
  color: #2c3e50;
  margin-bottom: 30px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.stat-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 20px;
  border-radius: 10px;
  text-align: center;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.stat-card.scheduled {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.stat-card.confirmed {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.stat-card.completed {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.stat-value {
  font-size: 36px;
  font-weight: bold;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 14px;
  opacity: 0.9;
}

.filter-section {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
  display: flex;
  gap: 10px;
  align-items: center;
}
</style>
