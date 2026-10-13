<template>
  <div class="hr-personal-center">
    <!-- 顶部导航 -->
    <AppHeader />

    <div class="container">
      <!-- 个人信息概览 -->
      <div class="profile-overview">
        <div class="profile-header">
          <div class="avatar-container">
            <el-avatar :size="100" :src="displayAvatar" class="user-avatar">
              <i class="fas fa-user" v-if="!userInfo.avatar"></i>
            </el-avatar>
            <el-upload
                class="avatar-uploader"
                :action="$apiBaseUrl + '/upload/avatar'"
                :headers="avatarUploadHeaders()"
                :show-file-list="false"
                :on-success="handleAvatarSuccess"
                :before-upload="beforeAvatarUpload">
              <el-button size="small" type="primary" plain>更换头像</el-button>
            </el-upload>
          </div>
          <div class="profile-info">
            <h1 class="username">{{ displayName }}</h1>
            <p class="user-title" v-if="userInfo.username && userInfo.username !== displayName">@{{ userInfo.username }}</p>
            <div class="profile-meta">
              <span><i class="fas fa-envelope"></i> {{ userInfo.email }}</span>
              <span><i class="fas fa-phone"></i> {{ userInfo.phone || '未绑定手机' }}</span>
              <span><i class="fas fa-building"></i> 所属公司: {{ userInfo.companyName || '未关联公司' }}</span>
              <span><i class="fas fa-user-circle"></i> 角色: HR</span>
            </div>
            <div class="profile-actions">
              <el-button @click="saveProfile">保存修改</el-button>
            </div>
          </div>
        </div>

        <!-- HR 专属面板 -->
        <div class="hr-panel">
          <div class="stats-card">
            <div class="stat-item">
              <div class="stat-value">{{ hrStats.posted }}</div>
              <div class="stat-label">发布职位</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ hrStats.candidates }}</div>
              <div class="stat-label">候选人</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ hrStats.interviews }}</div>
              <div class="stat-label">待面试</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ hrStats.hired }}</div>
              <div class="stat-label">已录用</div>
            </div>
          </div>
        </div>
      </div>

      <!-- 主要内容区 -->
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
            <el-menu-item index="post-job">
              <i class="fas fa-plus-circle"></i>
              <span>发布职位</span>
            </el-menu-item>
            <el-menu-item index="job-management">
              <i class="fas fa-briefcase"></i>
              <span>职位管理</span>
            </el-menu-item>
            <el-menu-item index="candidates">
              <i class="fas fa-users"></i>
              <span>候选人管理</span>
            </el-menu-item>
          </el-menu>
        </div>

        <!-- 右侧内容区 -->
        <div class="content-area">
          <!-- 个人资料 -->
          <div class="profile-section" v-if="activeNav === 'profile'">
            <div class="section-title">个人资料</div>
            <el-form :model="userInfo" label-width="100px" class="profile-form">
              <el-form-item label="用户名">
                <el-input v-model="userInfo.name"></el-input>
              </el-form-item>

              <el-form-item label="昵称">
                <el-input v-model="userInfo.title"></el-input>
              </el-form-item>

              <el-form-item label="邮箱">
                <el-input v-model="userInfo.email"></el-input>
              </el-form-item>

              <el-form-item label="手机号">
                <el-input v-model="userInfo.phone"></el-input>
              </el-form-item>

              <el-form-item label="个人简介">
                <el-input
                    type="textarea"
                    :rows="3"
                    v-model="userInfo.bio"
                    placeholder="简单介绍一下自己">
                </el-input>
              </el-form-item>

              <el-form-item>
                <el-button type="primary" @click="saveProfile">保存修改</el-button>
              </el-form-item>
            </el-form>
          </div>

          <!-- HR：发布职位 -->
          <div class="post-job-section" v-if="activeNav === 'post-job'">
            <div class="section-title">发布新职位</div>
            <el-form :model="newJob" label-width="100px" class="job-form">
              <el-form-item label="职位名称" required>
                <el-input v-model="newJob.title" placeholder="例如: Java高级开发工程师"></el-input>
              </el-form-item>
              <el-form-item label="工作地点" required>
                <el-input v-model="newJob.city" placeholder="例如: 北京"></el-input>
              </el-form-item>
              <el-form-item label="薪资范围" required>
                <el-input v-model="newJob.salary" placeholder="例如: 15K-25K"></el-input>
              </el-form-item>
              <el-form-item label="经验要求" required>
                <el-select v-model="newJob.experience" placeholder="请选择经验要求">
                  <el-option label="不限" value="不限"></el-option>
                  <el-option label="应届生" value="应届生"></el-option>
                  <el-option label="1-3年" value="1-3年"></el-option>
                  <el-option label="3-5年" value="3-5年"></el-option>
                  <el-option label="5年以上" value="5年以上"></el-option>
                </el-select>
              </el-form-item>
              <el-form-item label="学历要求" required>
                <el-select v-model="newJob.education" placeholder="请选择学历要求">
                  <el-option label="不限" value="不限"></el-option>
                  <el-option label="大专" value="大专"></el-option>
                  <el-option label="本科" value="本科"></el-option>
                  <el-option label="硕士" value="硕士"></el-option>
                  <el-option label="博士" value="博士"></el-option>
                </el-select>
              </el-form-item>
              <el-form-item label="职位描述" required>
                <el-input
                    type="textarea"
                    :rows="5"
                    v-model="newJob.description"
                    placeholder="详细描述职位职责和要求">
                </el-input>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="submitJob">发布职位</el-button>
              </el-form-item>
            </el-form>
          </div>

          <!-- HR: 职位管理 -->
          <div class="job-management-section" v-if="activeNav === 'job-management'">
            <div class="section-title">职位管理</div>
            <div class="job-list" v-loading="jobsLoading">
              <div v-for="job in jobList" :key="job.id" class="job-card">
                <div class="job-header">
                  <h3 class="job-title">{{ job.title }}</h3>
                  <el-tag :type="job.status === 'active' ? 'success' : 'info'">
                    {{ job.status === 'active' ? '招聘中' : '已下架' }}
                  </el-tag>
                </div>
                <div class="job-meta">
                  <span>{{ job.salary }}</span>
                  <span>{{ job.city }}</span>
                  <span>{{ job.experience }}</span>
                  <span>{{ job.education }}</span>
                </div>
                <div class="job-actions">
                  <el-button size="mini" @click="editJob(job)">编辑</el-button>
                  <el-button size="mini" type="danger" @click="deleteJob(job.id)">删除</el-button>
                  <el-button size="mini" type="success" @click="toggleJobStatus(job)">
                    {{ job.status === 'active' ? '下架' : '上架' }}
                  </el-button>
                </div>
              </div>
              <div v-if="jobList.length === 0 && !jobsLoading" class="empty-data">
                <i class="el-icon-briefcase"></i>
                <p>暂无职位，点击右上角发布新职位</p>
              </div>
            </div>
          </div>

          <!-- HR: 编辑职位对话框 -->
          <el-dialog title="编辑职位" :visible.sync="editJobDialogVisible" width="50%">
            <el-form :model="editingJob" label-width="100px">
              <el-form-item label="职位名称" required>
                <el-input v-model="editingJob.title"></el-input>
              </el-form-item>
              <el-form-item label="工作地点" required>
                <el-input v-model="editingJob.city"></el-input>
              </el-form-item>
              <el-form-item label="薪资范围" required>
                <el-input v-model="editingJob.salary"></el-input>
              </el-form-item>
              <el-form-item label="经验要求" required>
                <el-select v-model="editingJob.experience" placeholder="请选择经验要求">
                  <el-option label="不限" value="不限"></el-option>
                  <el-option label="应届生" value="应届生"></el-option>
                  <el-option label="1-3年" value="1-3年"></el-option>
                  <el-option label="3-5年" value="3-5年"></el-option>
                  <el-option label="5年以上" value="5年以上"></el-option>
                </el-select>
              </el-form-item>
              <el-form-item label="学历要求" required>
                <el-select v-model="editingJob.education" placeholder="请选择学历要求">
                  <el-option label="不限" value="不限"></el-option>
                  <el-option label="大专" value="大专"></el-option>
                  <el-option label="本科" value="本科"></el-option>
                  <el-option label="硕士" value="硕士"></el-option>
                  <el-option label="博士" value="博士"></el-option>
                </el-select>
              </el-form-item>
            </el-form>
            <span slot="footer" class="dialog-footer">
              <el-button @click="editJobDialogVisible = false">取 消</el-button>
              <el-button type="primary" @click="updateJob">确 定</el-button>
            </span>
          </el-dialog>

          <!-- HR: 候选人管理 -->
          <div class="candidates-section" v-if="activeNav === 'candidates'">
            <div class="section-title">候选人管理</div>
            <div class="candidate-filter">
              <el-select v-model="candidateFilter.job" placeholder="按职位筛选" clearable @change="onJobFilterChange">
                <el-option
                    v-for="job in jobList"
                    :key="job.id"
                    :label="job.title"
                    :value="job.id">
                </el-option>
              </el-select>
              <el-select v-model="candidateFilter.status" placeholder="按状态筛选" clearable @change="filterCandidates">
                <el-option label="新申请" value="new"></el-option>
                <el-option label="已查看" value="viewed"></el-option>
                <el-option label="面试中" value="interview"></el-option>
                <el-option label="已录用" value="hired"></el-option>
                <el-option label="已拒绝" value="rejected"></el-option>
              </el-select>
              <el-button type="primary" @click="filterCandidates">筛选</el-button>
            </div>
            <el-table :data="filteredCandidates" style="width: 100%" v-loading="candidatesLoading">
              <el-table-column prop="applicantName" label="姓名" width="120"></el-table-column>
              <el-table-column prop="username" label="用户名" width="150"></el-table-column>
              <el-table-column prop="jobTitle" label="申请职位" width="150"></el-table-column>
              <el-table-column prop="applyDate" label="申请时间" width="150"></el-table-column>
              <el-table-column prop="status" label="状态" width="120">
                <template slot-scope="{row}">
                  <el-tag :type="getCandidateStatusType(row.statusValue)">
                    {{ row.status }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="300">
                <template slot-scope="{row}">
                  <el-button size="mini" type="primary" @click="chatWithCandidate(row)">沟通</el-button>
                  <el-button size="mini" @click="viewCandidateResume(row.userId)">查看简历</el-button>
                  <el-dropdown size="mini" @command="handleCandidateAction">
                    <el-button size="mini" type="success" plain>
                      更多操作<i class="el-icon-arrow-down el-icon--right"></i>
                    </el-button>
                    <el-dropdown-menu slot="dropdown">
                      <el-dropdown-item :command="{ action: 'view', candidate: row }">查看详情</el-dropdown-item>
                      <el-dropdown-item :command="{ action: 'interview', candidate: row }">安排面试</el-dropdown-item>
                      <el-dropdown-item :command="{ action: 'hire', candidate: row }">录用</el-dropdown-item>
                      <el-dropdown-item :command="{ action: 'reject', candidate: row }">拒绝</el-dropdown-item>
                    </el-dropdown-menu>
                  </el-dropdown>
                </template>
              </el-table-column>
            </el-table>
            <div v-if="filteredCandidates.length === 0 && !candidatesLoading" class="empty-data">
              <i class="fas fa-users"></i>
              <p>暂无候选人</p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 候选人详情对话框 -->
    <el-dialog title="候选人详情" :visible.sync="candidateDetailDialogVisible" width="60%">
      <div v-if="selectedCandidate" class="candidate-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="姓名">{{ selectedCandidate.applicantName }}</el-descriptions-item>
          <el-descriptions-item label="用户名">{{ selectedCandidate.username }}</el-descriptions-item>
          <el-descriptions-item label="申请职位">{{ selectedCandidate.jobTitle }}</el-descriptions-item>
          <el-descriptions-item label="申请时间">{{ selectedCandidate.applyDate }}</el-descriptions-item>
          <el-descriptions-item label="当前状态">
            <el-tag :type="getCandidateStatusType(selectedCandidate.statusValue)">
              {{ selectedCandidate.status }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="candidateDetailDialogVisible = false">关 闭</el-button>
        <el-button type="primary" @click="viewCandidateResume(selectedCandidate.userId)">查看简历</el-button>
      </span>
    </el-dialog>

    <!-- 安排面试对话框 -->
    <el-dialog title="安排面试" :visible.sync="scheduleInterviewDialogVisible" width="600px">
      <div v-if="selectedCandidate">
        <el-alert
          title="请填写面试安排信息，将通知候选人"
          type="info"
          :closable="false"
          show-icon
          style="margin-bottom: 20px;">
        </el-alert>
        
        <el-form :model="scheduleInterviewForm" :rules="scheduleInterviewRules" ref="scheduleInterviewForm" label-width="120px">
          <el-form-item label="候选人">
            <strong>{{ selectedCandidate.applicantName }}</strong>
          </el-form-item>
          <el-form-item label="申请职位">
            {{ selectedCandidate.jobTitle }}
          </el-form-item>
          <el-form-item label="面试时间" prop="interviewTime">
            <el-date-picker
              v-model="scheduleInterviewForm.interviewTime"
              type="datetime"
              placeholder="选择面试时间"
              style="width: 100%"
              value-format="yyyy-MM-dd HH:mm:ss"
            ></el-date-picker>
          </el-form-item>
          <el-form-item label="面试类型" prop="interviewType">
            <el-select v-model="scheduleInterviewForm.interviewType" placeholder="选择面试类型" style="width: 100%">
              <el-option label="电话面试" value="phone"></el-option>
              <el-option label="视频面试" value="video"></el-option>
              <el-option label="现场面试" value="onsite"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item 
            label="面试地点" 
            v-if="scheduleInterviewForm.interviewType === 'onsite'" 
            prop="location">
            <el-input v-model="scheduleInterviewForm.location" placeholder="请输入面试地点"></el-input>
          </el-form-item>
          <el-form-item 
            label="会议链接" 
            v-if="scheduleInterviewForm.interviewType === 'video'" 
            prop="meetingUrl">
            <el-input v-model="scheduleInterviewForm.meetingUrl" placeholder="请输入视频会议链接（如腾讯会议、Zoom等）"></el-input>
          </el-form-item>
          <el-form-item label="面试官" prop="interviewer">
            <el-input v-model="scheduleInterviewForm.interviewer" placeholder="请输入面试官姓名"></el-input>
          </el-form-item>
          <el-form-item label="联系电话">
            <el-input v-model="scheduleInterviewForm.interviewerPhone" placeholder="请输入面试官联系电话"></el-input>
          </el-form-item>
          <el-form-item label="备注">
            <el-input
              type="textarea"
              v-model="scheduleInterviewForm.remark"
              :rows="3"
              placeholder="请输入备注信息（可选）">
            </el-input>
          </el-form-item>
        </el-form>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="scheduleInterviewDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="confirmScheduleInterview" :loading="scheduleInterviewLoading">确认安排</el-button>
      </span>
    </el-dialog>

    <!-- 录用确认对话框 -->
    <el-dialog title="录用候选人" :visible.sync="hireDialogVisible" width="50%">
      <div v-if="selectedCandidate">
        <el-alert
          title="请确认是否录用该候选人"
          type="warning"
          :closable="false"
          show-icon
          style="margin-bottom: 20px;">
        </el-alert>
        
        <el-form :model="hireForm" label-width="100px">
          <el-form-item label="候选人">
            <strong>{{ selectedCandidate.applicantName }}</strong>
          </el-form-item>
          <el-form-item label="申请职位">
            {{ selectedCandidate.jobTitle }}
          </el-form-item>
          <el-form-item label="备注信息">
            <el-input
              type="textarea"
              v-model="hireForm.remark"
              :rows="4"
              placeholder="请输入录用备注（可选）">
            </el-input>
          </el-form-item>
        </el-form>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="hireDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="confirmHire" :loading="hireLoading">确认录用</el-button>
      </span>
    </el-dialog>

    <!-- 拒绝确认对话框 -->
    <el-dialog title="拒绝候选人" :visible.sync="rejectDialogVisible" width="50%">
      <div v-if="selectedCandidate">
        <el-alert
          title="请说明拒绝原因，这将发送给候选人"
          type="info"
          :closable="false"
          show-icon
          style="margin-bottom: 20px;">
        </el-alert>
        
        <el-form :model="rejectForm" :rules="rejectRules" ref="rejectForm" label-width="100px">
          <el-form-item label="候选人">
            <strong>{{ selectedCandidate.applicantName }}</strong>
          </el-form-item>
          <el-form-item label="申请职位">
            {{ selectedCandidate.jobTitle }}
          </el-form-item>
          <el-form-item label="拒绝原因" prop="reason">
            <el-select v-model="rejectForm.reason" placeholder="请选择拒绝原因" style="width: 100%; margin-bottom: 10px;">
              <el-option label="经验不匹配" value="经验不匹配"></el-option>
              <el-option label="技能不符合要求" value="技能不符合要求"></el-option>
              <el-option label="薪资期望不符" value="薪资期望不符"></el-option>
              <el-option label="学历不符合要求" value="学历不符合要求"></el-option>
              <el-option label="其他原因" value="其他原因"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="详细说明" prop="detailReason">
            <el-input
              type="textarea"
              v-model="rejectForm.detailReason"
              :rows="4"
              placeholder="请详细说明拒绝原因">
            </el-input>
          </el-form-item>
        </el-form>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="rejectDialogVisible = false">取 消</el-button>
        <el-button type="danger" @click="confirmReject" :loading="rejectLoading">确认拒绝</el-button>
      </span>
    </el-dialog>

  </div>
</template>

<script>
import AppHeader from "@/components/common/AppHeader.vue";
import { defaultAvatar } from '@/utils/avatar';

export default {
  name: 'HrPerson',
  components: { AppHeader },
  data() {
    return {
      activeNav: 'profile',
      userInfo: {},
      userRole: 'hr',
      
      // HR 统计数据
      hrStats: {
        posted: 0,
        candidates: 0,
        interviews: 0,
        hired: 0
      },
      
      // 职位列表
      jobList: [],
      jobsLoading: false,
      
      // 候选人管理
      candidatesLoading: false,
      candidates: [],
      filteredCandidates: [],
      candidateFilter: {
        job: null,
        status: null
      },
      
      // 编辑职位
      editJobDialogVisible: false,
      editingJob: {},
      
      // 发布新职位
      newJob: {
        title: '',
        city: '',
        salary: '',
        experience: '',
        education: '',
        description: ''
      },
      
      // 候选人管理相关对话框
      candidateDetailDialogVisible: false,
      hireDialogVisible: false,
      rejectDialogVisible: false,
      scheduleInterviewDialogVisible: false,
      selectedCandidate: null,
      hireLoading: false,
      rejectLoading: false,
      scheduleInterviewLoading: false,
      hireForm: {
        remark: ''
      },
      rejectForm: {
        reason: '',
        detailReason: ''
      },
      scheduleInterviewForm: {
        interviewTime: '',
        interviewType: 'phone',
        location: '',
        meetingUrl: '',
        interviewer: '',
        interviewerPhone: '',
        remark: ''
      },
      scheduleInterviewRules: {
        interviewTime: [
          { required: true, message: '请选择面试时间', trigger: 'change' }
        ],
        interviewType: [
          { required: true, message: '请选择面试类型', trigger: 'change' }
        ],
        location: [
          { 
            required: true, 
            message: '请输入面试地点', 
            trigger: 'blur',
            validator: (rule, value, callback) => {
              if (this.scheduleInterviewForm.interviewType === 'onsite' && !value) {
                callback(new Error('现场面试必须填写地点'));
              } else {
                callback();
              }
            }
          }
        ],
        meetingUrl: [
          { 
            required: true, 
            message: '请输入会议链接', 
            trigger: 'blur',
            validator: (rule, value, callback) => {
              if (this.scheduleInterviewForm.interviewType === 'video' && !value) {
                callback(new Error('视频面试必须填写会议链接'));
              } else {
                callback();
              }
            }
          }
        ],
        interviewer: [
          { required: true, message: '请输入面试官姓名', trigger: 'blur' }
        ]
      },
      rejectRules: {
        reason: [
          { required: true, message: '请选择拒绝原因', trigger: 'change' }
        ],
        detailReason: [
          { required: true, message: '请详细说明拒绝原因', trigger: 'blur' }
        ]
      }
    }
  },
  created() {
    this.loadUserInfo();
    // loadJobList() 会在 loadUserInfo() 成功后自动调用
  },
  watch: {
    // 监听路由 query 参数变化，自动切换 tab
    '$route.query.tab': {
      handler(newTab) {
        if (newTab) {
          this.activeNav = newTab;
        }
      },
      immediate: true
    }
  },
  computed: {
    // 显示名称：优先昵称/名称，最后才是用户名
    displayName() {
      return this.userInfo.title || this.userInfo.name || this.userInfo.nickname || this.userInfo.username || 'HR';
    },
    // 无自定义头像时按性别显示默认头像
    displayAvatar() {
      return this.userInfo.avatar || defaultAvatar(this.userInfo.gender);
    }
  },
  methods: {
    // 加载用户信息
    async loadUserInfo() {
      try {
        const userStr = sessionStorage.getItem('user');
        if (!userStr) {
          console.error('sessionStorage 中没有用户信息');
          return;
        }
        const user = JSON.parse(userStr);
        const userId = user.id;
        
        console.log('从 sessionStorage 获取到的用户 ID:', userId);
        
        // 调用后端 API 获取完整用户信息（包含 companyName）
        const response = await this.$axios.get(`${this.$apiBaseUrl}/user/current?id=${userId}`);
        console.log('用户信息响应:', response.data);
        
        if (response.data.code === 200) {
          const userData = response.data.data;
          this.userInfo = {
            id: userData.id,
            username: userData.username || '',
            name: userData.realName || userData.nickname || userData.username,
            avatar: userData.avatar || '',
            gender: userData.gender,
            title: userData.nickname || '',
            email: userData.email || '',
            phone: userData.phone || '',
            bio: userData.bio || '',
            companyId: userData.companyId || null,
            companyName: userData.companyName || ''
          };
          this.userRole = 'hr';
          console.log('设置后的 userInfo:', this.userInfo);
          
          // 加载职位列表
          if (this.userRole === 'hr') {
            this.loadJobList();
          }
        } else {
          this.$message.error('获取用户信息失败: ' + response.data.message);
        }
      } catch (error) {
        console.error('加载用户信息失败:', error);
        this.$message.error('获取用户信息失败: ' + (error.response?.data?.message || error.message || '网络错误'));
      }
    },
    
    // 加载 HR 统计数据
    async loadHRStats() {
      try {
        // 这里可以调用后端接口获取真实数据
        // 暂时使用模拟数据
        this.hrStats = {
          posted: 5,
          candidates: 20,
          interviews: 3,
          hired: 2
        };
      } catch (error) {
        console.error('加载 HR 统计数据失败:', error);
      }
    },
    
    // 导航选择
    handleNavSelect(index) {
      this.activeNav = index;
      
      // 如果切换到候选人管理页面且有职位，则加载第一个职位的候选人
      if (index === 'candidates' && this.jobList.length > 0) {
        if (!this.candidateFilter.job || this.candidates.length === 0) {
          this.candidateFilter.job = this.jobList[0].id;
          this.loadCandidatesForJob(this.jobList[0].id);
        }
      }
      
      // 如果切换到职位管理或候选人管理页面，重新加载职位列表
      if (index === 'job-management' || index === 'candidates') {
        this.loadJobList();
      }
    },
    
    // 加载职位列表
    async loadJobList() {
      console.log('加载 HR 职位列表，userInfo:', this.userInfo);
      
      // 检查是否有公司ID或公司名称
      if (!this.userInfo.companyId && !this.userInfo.companyName) {
        console.error('未找到公司信息，无法加载职位列表');
        this.$message.warning('未找到关联的公司信息，请联系管理员');
        this.jobsLoading = false;
        return;
      }
      
      // 后端只有 /job/company/{companyName} 接口，所以使用公司名称查询
      if (!this.userInfo.companyName) {
        console.error('未找到公司名称，无法加载职位列表');
        this.$message.warning('未找到公司名称，请联系管理员');
        this.jobsLoading = false;
        return;
      }
      
      // 构建API URL
      const apiUrl = `${this.$apiBaseUrl}/job/company/${encodeURIComponent(this.userInfo.companyName)}`;
      
      console.log('请求URL:', apiUrl);
      console.log('公司名称:', this.userInfo.companyName);
      
      try {
        this.jobsLoading = true;
        const response = await this.$axios.get(apiUrl);
        console.log('职位列表响应:', response.data);
        if (response.data.code === 200) {
          this.jobList = response.data.data || [];
          // 更新发布职位数量
          this.hrStats.posted = this.jobList.length;
          console.log('职位列表加载成功，职位数量:', this.jobList.length);
        } else {
          console.error('加载职位列表失败:', response.data.msg);
          this.$message.error('加载职位列表失败：' + response.data.msg);
        }
      } catch (error) {
        console.error('加载职位列表失败:', error);
        this.$message.error('加载职位列表失败');
      } finally {
        this.jobsLoading = false;
      }
    },
    
    // 发布职位
    async submitJob() {
      if (!this.newJob.title || !this.newJob.city || !this.newJob.salary) {
        this.$message.warning('请填写完整信息');
        return;
      }
      try {
        const jobData = {
          ...this.newJob,
          companyName: this.userInfo.companyName,
          status: 'active'
        };
        const response = await this.$axios.post(this.$apiBaseUrl + '/job', jobData);
        if (response.data.code === 200) {
          this.$message.success('职位发布成功');
          this.newJob = { title: '', city: '', salary: '', experience: '', education: '', description: '' };
          this.loadJobList();
        }
      } catch (error) {
        console.error('发布职位失败:', error);
        this.$message.error('发布失败');
      }
    },
    
    // 编辑职位
    editJob(job) {
      this.editingJob = { ...job };
      this.editJobDialogVisible = true;
    },
    
    // 更新职位
    async updateJob() {
      try {
        const response = await this.$axios.put(this.$apiBaseUrl + '/job/' + this.editingJob.id, this.editingJob);
        if (response.data.code === 200) {
          this.$message.success('职位更新成功');
          this.editJobDialogVisible = false;
          this.loadJobList();
        }
      } catch (error) {
        console.error('更新职位失败:', error);
        this.$message.error('更新失败');
      }
    },
    
    // 删除职位
    async deleteJob(jobId) {
      try {
        await this.$confirm('确定要删除该职位吗？', '提示', { type: 'warning' });
        const response = await this.$axios.delete(this.$apiBaseUrl + '/job/' + jobId);
        if (response.data.code === 200) {
          this.$message.success('删除成功');
          this.loadJobList();
        }
      } catch (error) {
        if (error !== 'cancel') {
          console.error('删除职位失败:', error);
        }
      }
    },
    
    // 切换职位状态
    async toggleJobStatus(job) {
      const newStatus = job.status === 'active' ? 'inactive' : 'active';
      try {
        const response = await this.$axios.put(this.$apiBaseUrl + '/job/' + job.id, { ...job, status: newStatus });
        if (response.data.code === 200) {
          this.$message.success(newStatus === 'active' ? '上架成功' : '下架成功');
          this.loadJobList();
        }
      } catch (error) {
        console.error('切换状态失败:', error);
      }
    },
    
    // 加载候选人列表（按职位加载）
    loadCandidatesForJob(jobId) {
      console.log('加载职位候选人，jobId:', jobId);
      
      // 先获取职位标题
      const currentJob = this.jobList.find(job => job.id === jobId);
      const jobTitle = currentJob ? currentJob.title : '未知职位';
      
      this.$axios.get(`${this.$apiBaseUrl}/job-application/job/${jobId}/applicants`)
        .then(response => {
          if (response.data.code === 200) {
            const rawData = response.data.data;
            this.candidates = (Array.isArray(rawData) ? rawData : []).map(item => {
              return {
                id: item.id,
                userId: item.userId,
                jobId: item.jobId,
                jobTitle: jobTitle, // 添加职位标题
                applicantName: item.realName || item.nickname || item.username || '未知用户',
                username: item.username || '',
                applyDate: item.applyTime ? new Date(item.applyTime).toLocaleDateString() : '',
                statusValue: item.status,
                status: this.mapApplicationStatus(item.status),
                matchRate: Math.floor(Math.random() * 40) + 60,
                application: item
              };
            });
            this.filteredCandidates = this.candidates;
            // 重新应用筛选（保持当前职位/状态筛选条件）
            this.filterCandidates();
            console.log('处理后的候选人数据:', this.candidates);
            this.refreshCandidateStats();
          } else {
            console.error('加载候选人列表失败:', response.data.msg);
            this.candidates = [];
            this.filteredCandidates = [];
            this.refreshCandidateStats();
          }
        })
        .catch(error => {
          console.error('加载候选人列表失败:', error);
          this.candidates = [];
          this.filteredCandidates = [];
          this.refreshCandidateStats();
        });
    },
    
    // 映射申请状态显示文本
    mapApplicationStatus(status) {
      const statusMap = {
        'new': '新申请',
        'viewed': '已查看',
        'interview': '面试中',
        'hired': '已录用',
        'rejected': '已拒绝'
      };
      return statusMap[status] || status;
    },
    
    // 当职位筛选改变时加载对应的候选人
    onJobFilterChange() {
      if (this.candidateFilter.job) {
        this.loadCandidatesForJob(this.candidateFilter.job);
      } else {
        this.candidates = [];
        this.filteredCandidates = [];
        this.refreshCandidateStats();
      }
    },
    
    refreshCandidateStats() {
      this.hrStats.candidates = this.candidates.length;
      this.hrStats.interviews = this.candidates.filter(c => c.statusValue === 'interview').length;
      this.hrStats.hired = this.candidates.filter(c => c.statusValue === 'hired').length;
    },
    
    // 候选人筛选
    filterCandidates() {
      this.filteredCandidates = this.candidates.filter(c => {
        if (this.candidateFilter.job && c.jobId !== this.candidateFilter.job) return false;
        // statusValue 为英文原始值，与筛选项（new/viewed/interview/hired/rejected）匹配
        if (this.candidateFilter.status && c.statusValue !== this.candidateFilter.status) return false;
        return true;
      });
    },
    
    getCandidateStatusType(status) {
      const map = { 'new': 'info', 'viewed': '', 'interview': 'warning', 'hired': 'success', 'rejected': 'danger' };
      return map[status] || '';
    },
    
    // 沟通候选人
    chatWithCandidate(candidate) {
      this.$router.push({
        path: '/Chat',
        query: { userId: candidate.userId }
      });
    },
    
    // 查看候选人简历
    viewCandidateResume(userId) {
      this.$router.push('/ResumeDetail/' + userId);
    },
    
    // 处理候选人操作
    handleCandidateAction(payload) {
      if (!payload || !payload.action || !payload.candidate) {
        this.$message.error('操作参数异常');
        return;
      }

      const { action, candidate } = payload;

      switch (action) {
        case 'view':
          this.viewCandidateDetail(candidate);
          break;
        case 'interview':
          this.scheduleInterviewForCandidate(candidate);
          break;
        case 'hire':
          this.showHireDialog(candidate);
          break;
        case 'reject':
          this.showRejectDialog(candidate);
          break;
        default:
          this.$message.warning('未知操作');
      }
    },
    
    // 查看候选人详情
    viewCandidateDetail(candidate) {
      this.selectedCandidate = candidate;
      this.candidateDetailDialogVisible = true;
    },

    // 为候选人安排面试（弹出对话框）
    scheduleInterviewForCandidate(candidate) {
      if (!candidate.application || !candidate.application.id) {
        this.$message.error('候选人数据异常');
        return;
      }

      this.selectedCandidate = candidate;
      // 重置表单
      this.scheduleInterviewForm = {
        interviewTime: '',
        interviewType: 'phone',
        location: '',
        meetingUrl: '',
        interviewer: '',
        interviewerPhone: '',
        remark: ''
      };
      
      // 重置表单验证
      this.$nextTick(() => {
        if (this.$refs.scheduleInterviewForm) {
          this.$refs.scheduleInterviewForm.clearValidate();
        }
      });
      
      this.scheduleInterviewDialogVisible = true;
    },

    // 确认安排面试
    confirmScheduleInterview() {
      this.$refs.scheduleInterviewForm.validate((valid) => {
        if (valid) {
          this.scheduleInterviewLoading = true;
          
          const interviewData = {
            applicationId: this.selectedCandidate.application.id,
            jobId: this.selectedCandidate.jobId,
            userId: this.selectedCandidate.userId,
            interviewTime: this.formatDateTimeForBackend(this.scheduleInterviewForm.interviewTime),
            interviewType: this.scheduleInterviewForm.interviewType,
            location: this.scheduleInterviewForm.location,
            meetingUrl: this.scheduleInterviewForm.meetingUrl,
            interviewer: this.scheduleInterviewForm.interviewer,
            interviewerPhone: this.scheduleInterviewForm.interviewerPhone,
            remark: this.scheduleInterviewForm.remark
          };

          this.$axios.post(
            `${this.$apiBaseUrl}/interview/schedule`,
            interviewData
          ).then(response => {
            if (response.data.code === 200) {
              this.$message.success('面试安排成功！');
              this.scheduleInterviewDialogVisible = false;
              
              // 更新候选人状态为面试中
              this.selectedCandidate.application.status = 'interview';
              this.selectedCandidate.statusValue = 'interview';
              this.selectedCandidate.status = this.mapApplicationStatus('interview');
              this.refreshCandidateStats();
              
              // 刷新列表
              this.loadCandidatesForJob(this.candidateFilter.job);
            } else {
              this.$message.error('面试安排失败: ' + (response.data.message || '未知错误'));
            }
          }).catch(error => {
            console.error('面试安排错误:', error);
            this.$message.error('面试安排失败: ' + (error.response?.data?.message || error.message || '网络错误'));
          }).finally(() => {
            this.scheduleInterviewLoading = false;
          });
        }
      });
    },

    // 格式化日期时间为后端格式
    formatDateTimeForBackend(date) {
      if (!date) return '';
      const d = new Date(date);
      const year = d.getFullYear();
      const month = String(d.getMonth() + 1).padStart(2, '0');
      const day = String(d.getDate()).padStart(2, '0');
      const hours = String(d.getHours()).padStart(2, '0');
      const minutes = String(d.getMinutes()).padStart(2, '0');
      const seconds = String(d.getSeconds()).padStart(2, '0');
      return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
    },

    // 显示录用对话框
    showHireDialog(candidate) {
      this.selectedCandidate = candidate;
      this.hireForm.remark = '';
      this.hireDialogVisible = true;
    },

    // 确认录用
    confirmHire() {
      this.hireLoading = true;
      this.updateCandidateStatusWithRemark(
        this.selectedCandidate,
        'hired',
        this.hireForm.remark
      ).finally(() => {
        this.hireLoading = false;
        this.hireDialogVisible = false;
      });
    },

    // 显示拒绝对话框
    showRejectDialog(candidate) {
      this.selectedCandidate = candidate;
      this.rejectForm.reason = '';
      this.rejectForm.detailReason = '';
      this.rejectDialogVisible = true;
      
      // 重置表单验证
      this.$nextTick(() => {
        if (this.$refs.rejectForm) {
          this.$refs.rejectForm.clearValidate();
        }
      });
    },

    // 确认拒绝
    confirmReject() {
      this.$refs.rejectForm.validate((valid) => {
        if (valid) {
          this.rejectLoading = true;
          const reason = `${this.rejectForm.reason}：${this.rejectForm.detailReason}`;
          this.updateCandidateStatusWithRemark(
            this.selectedCandidate,
            'rejected',
            reason
          ).finally(() => {
            this.rejectLoading = false;
            this.rejectDialogVisible = false;
          });
        }
      });
    },

    // 简单更新状态（无备注）
    updateCandidateStatusSimple(candidate, nextStatus) {
      return new Promise((resolve, reject) => {
        if (!candidate || !candidate.application || !candidate.application.id) {
          this.$message.error('候选人数据异常，无法更新状态');
          reject(new Error('数据异常'));
          return;
        }
        if (!this.canUpdateCandidateStatus(candidate, nextStatus)) {
          this.$message.warning('当前状态不支持该操作');
          reject(new Error('状态不允许'));
          return;
        }

        this.$axios.put(
          `${this.$apiBaseUrl}/job-application/${candidate.application.id}/status`,
          null,
          { params: { status: nextStatus } }
        ).then(response => {
          if (response.data.code === 200) {
            candidate.application.status = nextStatus;
            candidate.statusValue = nextStatus;
            candidate.status = this.mapApplicationStatus(nextStatus);
            this.refreshCandidateStats();
            // 重新应用筛选（录用/拒绝后状态筛选下列表同步更新）
            this.filterCandidates();
            this.$message.success(`状态已更新为：${candidate.status}`);
            resolve();
          } else {
            this.$message.error('状态更新失败: ' + (response.data.msg || '未知错误'));
            reject(new Error(response.data.msg));
          }
        }).catch(error => {
          this.$message.error('状态更新失败: ' + (error.response?.data?.message || error.message || '网络错误'));
          reject(error);
        });
      });
    },

    // 带备注的状态更新
    updateCandidateStatusWithRemark(candidate, nextStatus, remark) {
      return new Promise((resolve, reject) => {
        if (!candidate || !candidate.application || !candidate.application.id) {
          this.$message.error('候选人数据异常，无法更新状态');
          reject(new Error('数据异常'));
          return;
        }
        if (!this.canUpdateCandidateStatus(candidate, nextStatus)) {
          this.$message.warning('当前状态不支持该操作');
          reject(new Error('状态不允许'));
          return;
        }

        this.$axios.put(
          `${this.$apiBaseUrl}/job-application/${candidate.application.id}/status`,
          null,
          { 
            params: { 
              status: nextStatus,
              remark: remark || ''
            } 
          }
        ).then(response => {
          if (response.data.code === 200) {
            candidate.application.status = nextStatus;
            candidate.statusValue = nextStatus;
            candidate.status = this.mapApplicationStatus(nextStatus);
            this.refreshCandidateStats();
            // 重新应用筛选（录用/拒绝后状态筛选下列表同步更新）
            this.filterCandidates();
            this.$message.success(`状态已更新为：${candidate.status}`);
            resolve();
          } else {
            this.$message.error('状态更新失败: ' + (response.data.msg || '未知错误'));
            reject(new Error(response.data.msg));
          }
        }).catch(error => {
          this.$message.error('状态更新失败: ' + (error.response?.data?.message || error.message || '网络错误'));
          reject(error);
        });
      });
    },
    
    // 判断状态是否允许流转
    canUpdateCandidateStatus(candidate, nextStatus) {
      const current = candidate?.statusValue;
      const transitions = {
        new: ['viewed', 'interview', 'rejected'],
        viewed: ['interview', 'rejected'],
        interview: ['hired', 'rejected'],
        hired: [],
        rejected: []
      };
      return current && transitions[current] ? transitions[current].includes(nextStatus) : false;
    },
    
    // 保存个人资料
    async saveProfile() {
      try {
        const response = await this.$axios.put(this.$apiBaseUrl + '/user/' + this.userInfo.id, this.userInfo);
        if (response.data.code === 200) {
          this.$message.success('保存成功');
          sessionStorage.setItem('user', JSON.stringify(this.userInfo));
        }
      } catch (error) {
        console.error('保存失败:', error);
        this.$message.error('保存失败');
      }
    },
    
    // 头像上传
    avatarUploadHeaders() {
      // 上传接口需要登录 token（后端 /upload/** 受鉴权保护）
      const token = sessionStorage.getItem('token');
      return token ? { Authorization: 'Bearer ' + token } : {};
    },
    async handleAvatarSuccess(res) {
      // 后端返回 { code:200, data: "/uploads/avatars/xxx.jpg" }
      if (res && res.code === 200 && res.data) {
        const avatarUrl = this.$apiBaseUrl + res.data;
        this.userInfo.avatar = avatarUrl;
        // 构造请求体并规范化字段（location 可能是数组、gender 可能是 male/female 字符串）
        const payload = { ...this.userInfo, avatar: avatarUrl };
        if (Array.isArray(payload.location)) {
          payload.location = payload.location.join(',');
        }
        if (payload.gender === 'male') {
          payload.gender = true;
        } else if (payload.gender === 'female') {
          payload.gender = false;
        } else {
          payload.gender = null;
        }
        // 持久化到用户资料（PUT /user/{id}），并同步 sessionStorage
        try {
          await this.$axios.put(this.$apiBaseUrl + '/user/' + this.userInfo.id, payload);
          const userStr = sessionStorage.getItem('user');
          if (userStr) {
            const user = JSON.parse(userStr);
            user.avatar = avatarUrl;
            sessionStorage.setItem('user', JSON.stringify(user));
          }
          this.$message.success('头像更新成功');
        } catch (e) {
          this.$message.error('头像已上传但保存失败，请重试');
        }
      } else {
        this.$message.error((res && res.message) || '头像上传失败');
      }
    },
    beforeAvatarUpload(file) {
      const isImage = file.type === 'image/jpeg' || file.type === 'image/png';
      if (!isImage) {
        this.$message.error('只能上传 JPG/PNG 格式的图片');
      }
      return isImage;
    }
  }
}
</script>

<style scoped>
.hr-personal-center {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f5f7fa;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 15px;
}

.profile-overview {
  background: white;
  padding: 30px;
  margin-top: 30px;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.profile-header {
  display: flex;
  gap: 30px;
  margin-bottom: 30px;
}

.avatar-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 15px;
}

.profile-info {
  flex: 1;
}

.username {
  font-size: 28px;
  color: #2c3e50;
  margin: 0 0 10px 0;
}

.user-title {
  font-size: 16px;
  color: #909399;
  margin: 0 0 15px 0;
}

.profile-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
  margin-bottom: 20px;
}

.profile-meta span {
  color: #606266;
  font-size: 14px;
}

.profile-meta i {
  color: #667eea;
  margin-right: 5px;
}

.hr-panel {
  margin-top: 20px;
}

.stats-card {
  display: flex;
  gap: 20px;
}

.stat-item {
  flex: 1;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 20px;
  border-radius: 8px;
  text-align: center;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 14px;
  opacity: 0.9;
}

.main-content {
  display: flex;
  gap: 30px;
  margin-top: 30px;
}

.side-nav {
  width: 220px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  height: fit-content;
}

.content-area {
  flex: 1;
  background: white;
  padding: 30px;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.section-title {
  font-size: 24px;
  font-weight: bold;
  color: #2c3e50;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 2px solid #667eea;
}

.job-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.job-card {
  padding: 20px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
}

.job-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.job-title {
  font-size: 18px;
  color: #2c3e50;
  margin: 0;
}

.job-meta {
  display: flex;
  gap: 15px;
  color: #909399;
  font-size: 14px;
  margin-bottom: 15px;
}

.job-actions {
  display: flex;
  gap: 10px;
}

.candidate-filter {
  display: flex;
  gap: 15px;
  margin-bottom: 20px;
}

.empty-data {
  text-align: center;
  padding: 40px;
  color: #909399;
}

.empty-data i {
  font-size: 48px;
  margin-bottom: 15px;
}
</style>
