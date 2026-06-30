<template>
  <div class="personal-center">
    <!-- 顶部导航 -->
    <AppHeader />

    <div class="container">
      <!-- 个人信息概览 -->
      <div class="profile-overview">
        <div class="profile-header">
          <div class="avatar-container">
            <el-avatar :size="100" :src="userInfo.avatar" class="user-avatar">
              <i class="fas fa-user" v-if="!userInfo.avatar"></i>
            </el-avatar>
            <el-upload
                class="avatar-uploader"
                action="/api/upload"
                :show-file-list="false"
                :on-success="handleAvatarSuccess"
                :before-upload="beforeAvatarUpload">
              <el-button size="small" type="primary" plain>更换头像</el-button>
            </el-upload>
          </div>
          <div class="profile-info">
            <h1 class="username">{{ userInfo.name }}</h1>
            <p class="user-title">{{ userInfo.title || '未设置个人昵称' }}</p>
            <div class="profile-meta">
              <span><i class="fas fa-envelope"></i> {{ userInfo.email }}</span>
              <span><i class="fas fa-phone"></i> {{ userInfo.phone || '未绑定手机' }}</span>
              <span><i class="fas fa-map-marker-alt"></i> {{ userInfo.location || '未设置所在地' }}</span>
              <span><i class="fas fa-user-circle"></i> 角色: {{ userRole === 'hr' ? 'HR' : '求职者' }}</span>
              <span><i class="fas fa-info-circle"></i> 状态: {{ userInfo.status ? '正常' : '禁用' }}</span>
              <span v-if="userInfo.createTime"><i class="fas fa-calendar-plus"></i> 注册时间: {{ formatDate(userInfo.createTime) }}</span>
              <span v-if="userInfo.lastLoginTime"><i class="fas fa-calendar-check"></i> 最后登录: {{ formatDate(userInfo.lastLoginTime) }}</span>
            </div>
            <div class="profile-actions">

              <el-button @click="viewResume">查看我的简历</el-button>
            </div>
          </div>
        </div>

        <!-- 求职者专属面板 -->
        <div class="job-seeker-panel" v-if="userRole === 'jobSeeker'">
          <div class="stats-card">
            <div class="stat-item">
              <div class="stat-value">{{ jobStats.applied }}</div>
              <div class="stat-label">已申请职位</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ jobStats.interview }}</div>
              <div class="stat-label">面试邀请</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ jobStats.favorite }}</div>
              <div class="stat-label">收藏职位</div>
            </div>

          </div>
        </div>

        <!-- HR专属面板 -->
        <div class="hr-panel" v-if="userRole === 'hr'">
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

            <!-- 求职者菜单 -->
            <template v-if="userRole === 'jobSeeker'">
              <el-menu-item index="resume">
                <i class="fas fa-file-alt"></i>
                <span>我的简历</span>
              </el-menu-item>
              <el-menu-item index="applications">
                <i class="fas fa-paper-plane"></i>
                <span>职位申请</span>
              </el-menu-item>
              <el-menu-item index="followed">
                <i class="fas fa-heart"></i>
                <span>我的关注</span>
              </el-menu-item>
            </template>

            <!-- HR菜单 -->
            <template v-if="userRole === 'hr'">
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

            </template>

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

              <el-form-item label="性别">
                <el-radio-group v-model="userInfo.gender">
                  <el-radio label="male">男</el-radio>
                  <el-radio label="female">女</el-radio>
                  <el-radio label="other">其他</el-radio>
                </el-radio-group>
              </el-form-item>

              <el-form-item label="出生日期">
                <el-date-picker
                    v-model="userInfo.birthday"
                    type="date"
                    placeholder="选择日期">
                </el-date-picker>
              </el-form-item>

              <el-form-item label="所在地">
                <el-cascader
                    v-model="userInfo.location"
                    :options="cityOptions"
                    placeholder="请选择地区">
                </el-cascader>
              </el-form-item>

              <el-form-item label="个人简介">
                <el-input
                    type="textarea"
                    :rows="3"
                    v-model="userInfo.bio"
                    placeholder="简单介绍一下自己">
                </el-input>
              </el-form-item>

              <el-form-item label="邮箱">
                <el-input v-model="userInfo.email"></el-input>
              </el-form-item>

              <el-form-item label="手机号">
                <el-input v-model="userInfo.phone"></el-input>
              </el-form-item>



              <el-form-item label="注册时间" v-if="userInfo.createTime">
                <el-input :value="formatDate(userInfo.createTime)" disabled></el-input>
              </el-form-item>

              <el-form-item label="最后登录" v-if="userInfo.lastLoginTime">
                <el-input :value="formatDate(userInfo.lastLoginTime)" disabled></el-input>
              </el-form-item>

              <el-form-item label="最后登录IP" v-if="userInfo.lastLoginIp">
                <el-input v-model="userInfo.lastLoginIp" disabled></el-input>
              </el-form-item>

              <el-form-item>
                <el-button type="primary" @click="saveProfile">保存修改</el-button>
              </el-form-item>
            </el-form>
          </div>

          <!-- 我的简历 -->
          <div class="resume-section" v-if="activeNav === 'resume' && userRole === 'jobSeeker'">
            <div class="section-title">我的简历</div>
            <div class="resume-container">
              <div class="resume-preview">
                <div class="resume-header">
                  <h2>{{ resume.name || '未命名简历' }} <el-tag v-if="isDefaultResume" type="success">默认简历</el-tag></h2>
                  <div class="resume-meta">
                    <span>{{ resume.gender }}</span>
                    <span>{{ resume.age }}岁</span>
                    <span>{{ resume.location }}</span>
                    <span>{{ resume.phone }}</span>
                    <span>{{ resume.email }}</span>
                  </div>
                </div>
                <div class="resume-section">
                  <h3>教育背景</h3>
                  <div class="resume-item" v-for="(edu, index) in resume.education" :key="index">
                    <div class="resume-item-header">
                      <span class="time">{{ edu.startDate }} - {{ edu.endDate }}</span>
                      <span class="title">{{ edu.school }}</span>
                      <span class="subtitle">{{ edu.major }} | {{ edu.degree }}</span>
                    </div>
                    <div class="resume-item-content" v-if="edu.description">
                      {{ edu.description }}
                    </div>
                  </div>
                </div>
                <div class="resume-section">
                  <h3>工作经历</h3>
                  <div class="resume-item" v-for="(exp, index) in resume.experience" :key="index">
                    <div class="resume-item-header">
                      <span class="time">{{ exp.startDate }} - {{ exp.endDate }}</span>
                      <span class="title">{{ exp.company }}</span>
                      <span class="subtitle">{{ exp.position }}</span>
                    </div>
                    <div class="resume-item-content" v-if="exp.description">
                      {{ exp.description }}
                    </div>
                  </div>
                </div>
                <div class="resume-section">
                  <h3>项目经验</h3>
                  <div class="resume-item" v-for="(project, index) in resume.projects" :key="index">
                    <div class="resume-item-header">
                      <span class="time">{{ project.time }}</span>
                      <span class="title">{{ project.name }}</span>
                      <span class="subtitle">{{ project.role }}</span>
                    </div>
                    <div class="resume-item-content" v-if="project.description">
                      {{ project.description }}
                    </div>
                  </div>
                </div>
              </div>
              <div class="resume-actions">
                <el-button type="primary" @click="editResume">编辑简历</el-button>
                <el-button @click="downloadResume">下载简历</el-button>
                <el-button @click="previewResume">预览简历</el-button>
              </div>
            </div>
          </div>

          <!-- 职位申请 -->
          <div class="applications-section" v-if="activeNav === 'applications' && userRole === 'jobSeeker'">
            <div class="section-title">职位申请</div>
            <el-table :data="applications" style="width: 100%" v-loading="applicationsLoading">
              <el-table-column prop="jobTitle" label="职位名称" width="200"></el-table-column>
              <el-table-column prop="company" label="公司名称" width="150"></el-table-column>
              <el-table-column prop="salary" label="薪资" width="120"></el-table-column>
              <el-table-column prop="city" label="工作地点" width="100"></el-table-column>
              <el-table-column prop="applyDate" label="申请日期" width="120"></el-table-column>
              <el-table-column prop="status" label="状态" width="100">
                <template #default="{row}">
                  <el-tag :type="getStatusType(row.statusValue)">
                    {{ row.status }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="feedback" label="反馈信息" min-width="200" show-overflow-tooltip></el-table-column>
              <el-table-column label="操作" width="150" fixed="right">
                <template #default="{row}">
                  <el-button size="mini" type="primary" @click="viewJobDetail(row.jobId)">查看</el-button>
                  <el-button size="mini" type="danger" @click="cancelApplication(row.id)" v-if="row.statusValue === 0">取消</el-button>
                </template>
              </el-table-column>
            </el-table>
            <div v-if="applications.length === 0 && !applicationsLoading" class="empty-data">
              <i class="el-icon-document"></i>
              <p>暂无申请记录</p>
            </div>
          </div>
          
          <!-- 我的关注 -->
          <div class="followed-section" v-if="activeNav === 'followed' && userRole === 'jobSeeker'">
            <div class="section-title">我的关注</div>
            <el-table :data="followedCompanies" style="width: 100%" v-loading="followedLoading">
              <el-table-column prop="name" label="公司名称" width="200"></el-table-column>
              <el-table-column prop="industry" label="所属行业" width="120"></el-table-column>
              <el-table-column prop="scale" label="公司规模" width="120"></el-table-column>
              <el-table-column prop="city" label="工作地点" width="120"></el-table-column>
              <el-table-column prop="phone" label="联系电话" width="150"></el-table-column>
              <el-table-column label="操作" width="200" fixed="right">
                <template #default="{row}">
                  <el-button size="mini" type="primary" @click="viewCompanyDetail(row.id)">查看详情</el-button>
                  <el-button size="mini" type="danger" @click="unfollowCompany(row.id)">取消关注</el-button>
                </template>
              </el-table-column>
            </el-table>
            <div v-if="followedCompanies.length === 0 && !followedLoading" class="empty-data">
              <i class="fas fa-heart"></i>
              <p>暂无关注的公司</p>
            </div>
          </div>
          
          
          
          <!-- HR：发布职位 -->
          <div class="post-job-section" v-if="activeNav === 'post-job' && userRole === 'hr'">
            <div class="section-title">发布新职位</div>
            <el-form :model="newJob" label-width="100px" class="job-form">
              <el-form-item label="职位名称" required>
                <el-input v-model="newJob.title" placeholder="例如: Java高级开发工程师"></el-input>
              </el-form-item>
              <el-form-item label="职位类型" required>
                <el-select v-model="newJob.type" placeholder="请选择职位类型">
                  <el-option label="技术" value="technology"></el-option>
                  <el-option label="产品" value="product"></el-option>
                  <el-option label="设计" value="design"></el-option>
                  <el-option label="运营" value="operation"></el-option>
                  <el-option label="市场" value="marketing"></el-option>
                </el-select>
              </el-form-item>
              <el-form-item label="工作地点" required>
                <el-cascader
                    v-model="newJob.location"
                    :options="cityOptions"
                    placeholder="请选择工作地点">
                </el-cascader>
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
                <el-button @click="saveDraft">保存草稿</el-button>
              </el-form-item>
            </el-form>
          </div>

          <!-- HR: 职位管理 -->
          <div class="job-management-section" v-if="activeNav === 'job-management' && userRole === 'hr'">
            <div class="section-title">职位管理</div>
            <el-table :data="jobList" style="width: 100%">
              <el-table-column prop="title" label="职位名称" width="180"></el-table-column>
              <el-table-column prop="city" label="工作地点" width="120"></el-table-column>
              <el-table-column prop="salary" label="薪资范围" width="120"></el-table-column>
              <el-table-column prop="experience" label="经验要求" width="120"></el-table-column>
              <el-table-column prop="education" label="学历要求" width="120"></el-table-column>
              <el-table-column label="操作" width="260">
                <template #default="{row}">
                  <el-button size="mini" @click="editJob(row)">编辑</el-button>
                  <el-button size="mini" type="danger" @click="deleteJob(row.id)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
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
          <div class="candidates-section" v-if="activeNav === 'candidates' && userRole === 'hr'">
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
            <el-table :data="filteredCandidates" style="width: 100%">
              <el-table-column prop="name" label="姓名" width="120"></el-table-column>
              <el-table-column prop="user.username" label="用户名" width="150"></el-table-column>
              <el-table-column prop="applyDate" label="申请时间" width="150"></el-table-column>
              <el-table-column prop="status" label="状态" width="120">
                <template #default="{row}">
                  <el-tag :type="getCandidateStatusType(row.status)">
                    {{ row.status }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="matchRate" label="匹配度" width="100">
                <template #default="{row}">
                  <el-progress :percentage="row.matchRate" :show-text="false"></el-progress>
                  <span>{{ row.matchRate }}%</span>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="380">
                <template #default="{row}">
                  <!-- 沟通按钮 -->
                  <el-button size="mini" type="primary" @click="chatWithCandidate(row)">
                    <i class="el-icon-chat-dot-round"></i>
                    沟通
                  </el-button>
                  
                  <!-- 查看简历 -->
                  <el-button size="mini" @click="viewCandidateResume(row.userId)">查看简历</el-button>
                  
                  <!-- 下拉菜单：更多操作 -->
                  <el-dropdown
                    size="mini"
                    style="margin-left: 8px;"
                    @command="handleCandidateAction">
                    <el-button size="mini" type="success" plain>
                      更多操作<i class="el-icon-arrow-down el-icon--right"></i>
                    </el-button>
                    <el-dropdown-menu slot="dropdown">
                      <el-dropdown-item :command="{ action: 'view', candidate: row }">
                        <i class="el-icon-view"></i> 查看详情
                      </el-dropdown-item>
                      <el-dropdown-item :command="{ action: 'interview', candidate: row }" :disabled="!canUpdateCandidateStatus(row, 'interview')">
                        <i class="el-icon-date"></i> 安排面试
                      </el-dropdown-item>
                      <el-dropdown-item :command="{ action: 'hired', candidate: row }" :disabled="!canUpdateCandidateStatus(row, 'hired')">
                        <i class="el-icon-check"></i> 录用
                      </el-dropdown-item>
                      <el-dropdown-item :command="{ action: 'rejected', candidate: row }" :disabled="!canUpdateCandidateStatus(row, 'rejected')">
                        <i class="el-icon-close"></i> 拒绝
                      </el-dropdown-item>
                      <el-dropdown-item :command="{ action: 'viewed', candidate: row }" :disabled="!canUpdateCandidateStatus(row, 'viewed')">
                        <i class="el-icon-document-checked"></i> 标记已查看
                      </el-dropdown-item>
                    </el-dropdown-menu>
                  </el-dropdown>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </div>
      </div>
    </div>

    <!-- 页脚 -->
    <div class="footer">
      <div class="container">
        <div class="footer-links">

        </div>

      </div>
    </div>

    <!-- 修改密码对话框 -->
    <el-dialog title="修改密码" :visible.sync="passwordDialogVisible" width="30%">
      <el-form :model="passwordForm" :rules="passwordRules" ref="passwordForm" label-width="100px">
        <el-form-item label="当前密码" prop="oldPassword">
          <el-input type="password" v-model="passwordForm.oldPassword" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input type="password" v-model="passwordForm.newPassword" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input type="password" v-model="passwordForm.confirmPassword" autocomplete="off"></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="passwordDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitPassword">确 定</el-button>
      </span>
    </el-dialog>

    <!-- 候选人详情对话框 -->
    <el-dialog title="候选人详情" :visible.sync="candidateDetailDialogVisible" width="60%">
      <div v-if="selectedCandidate" class="candidate-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="姓名">{{ selectedCandidate.name }}</el-descriptions-item>
          <el-descriptions-item label="用户名">{{ selectedCandidate.user?.username }}</el-descriptions-item>
          <el-descriptions-item label="申请职位">{{ selectedCandidate.jobTitle }}</el-descriptions-item>
          <el-descriptions-item label="申请时间">{{ selectedCandidate.applyDate }}</el-descriptions-item>
          <el-descriptions-item label="当前状态">
            <el-tag :type="getCandidateStatusType(selectedCandidate.statusValue)">
              {{ selectedCandidate.status }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="匹配度">
            <el-progress :percentage="selectedCandidate.matchRate" style="width: 150px;"></el-progress>
          </el-descriptions-item>
        </el-descriptions>
        
        <div style="margin-top: 20px;">
          <h4>联系方式</h4>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="邮箱">{{ selectedCandidate.user?.email || '未填写' }}</el-descriptions-item>
            <el-descriptions-item label="电话">{{ selectedCandidate.user?.phone || '未填写' }}</el-descriptions-item>
          </el-descriptions>
        </div>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="candidateDetailDialogVisible = false">关 闭</el-button>
        <el-button type="primary" @click="viewCandidateResume(selectedCandidate.userId)">查看简历</el-button>
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
            <strong>{{ selectedCandidate.name }}</strong>
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
            <strong>{{ selectedCandidate.name }}</strong>
          </el-form-item>
          <el-form-item label="申请职位">
            {{ selectedCandidate.jobTitle }}
          </el-form-item>
          <el-form-item label="拒绝原因" prop="reason">
            <el-select v-model="rejectForm.reason" placeholder="请选择拒绝原因" style="width: 100%; margin-bottom: 10px;">
              <el-option label="经验不匹配" value="经验不匹配"></el-option>
              <el-option label="技能不符合要求" value="技能不符合要求"></el-option>
              <el-option label="薪资期望不符" value="薪资期望不符"></el-option>
              <el-option label="已有更合适人选" value="已有更合适人选"></el-option>
              <el-option label="其他原因" value="其他原因"></el-option>
            </el-select>
            <el-input
              type="textarea"
              v-model="rejectForm.detailReason"
              :rows="4"
              placeholder="请详细说明拒绝原因（必填）">
            </el-input>
          </el-form-item>
        </el-form>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="rejectDialogVisible = false">取 消</el-button>
        <el-button type="danger" @click="confirmReject" :loading="rejectLoading">确认拒绝</el-button>
      </span>
    </el-dialog>

    <!-- 安排面试对话框 -->
    <el-dialog title="安排面试" :visible.sync="scheduleInterviewDialogVisible" width="600px">
      <div v-if="selectedCandidate">
        <el-alert
          :title="`为 ${selectedCandidate.name} 安排面试 - ${selectedCandidate.jobTitle}`"
          type="success"
          :closable="false"
          show-icon
          style="margin-bottom: 20px;">
        </el-alert>
        
        <el-form :model="scheduleInterviewForm" :rules="scheduleInterviewRules" ref="scheduleInterviewForm" label-width="120px">
          <el-form-item label="面试时间" prop="interviewTime">
            <el-date-picker
              v-model="scheduleInterviewForm.interviewTime"
              type="datetime"
              placeholder="选择面试时间"
              style="width: 100%">
            </el-date-picker>
          </el-form-item>
          
          <el-form-item label="面试类型" prop="interviewType">
            <el-select v-model="scheduleInterviewForm.interviewType" placeholder="选择面试类型" style="width: 100%">
              <el-option label="电话面试" value="phone"></el-option>
              <el-option label="视频面试" value="video"></el-option>
              <el-option label="现场面试" value="onsite"></el-option>
            </el-select>
          </el-form-item>
          
          <el-form-item 
            v-if="scheduleInterviewForm.interviewType === 'onsite'" 
            label="面试地点" 
            prop="location">
            <el-input v-model="scheduleInterviewForm.location" placeholder="请输入面试地点"></el-input>
          </el-form-item>
          
          <el-form-item 
            v-if="scheduleInterviewForm.interviewType === 'video'" 
            label="会议链接" 
            prop="meetingUrl">
            <el-input v-model="scheduleInterviewForm.meetingUrl" placeholder="请输入视频会议链接（如腾讯会议、Zoom等）"></el-input>
          </el-form-item>
          
          <el-form-item label="面试官" prop="interviewer">
            <el-input v-model="scheduleInterviewForm.interviewer" placeholder="请输入面试官姓名"></el-input>
          </el-form-item>
          
          <el-form-item label="联系电话" prop="interviewerPhone">
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
  </div>
</template>

<script>
import AppHeader from '@/components/common/AppHeader.vue'

export default {
  name: 'PersonalCenter',
  components: {
    AppHeader
  },
  data() {
    return {
      userRole: 'jobSeeker', // 'jobSeeker' 或 'hr'
      activeNav: 'profile',
      userInfo: {
        id: null,
        name: '',
        avatar: '',
        title: '',
        email: '',
        phone: '',
        location: [],
        gender: '',
        birthday: '',
        bio: '',
        status: true,
        createTime: '',
        lastLoginTime: '',
        lastLoginIp: '',
        companyId: null,      // HR用户的公司ID
        companyName: ''       // HR用户的公司名称
      },
      jobStats: {
        applied: 0,
        interview: 0,
        favorite: 0,
        follow: 0
      },
      hrStats: {
        posted: 0,
        candidates: 0,
        interviews: 0,
        hired: 0
      },
      resume: {
        name: '',
        gender: '',
        age: null,
        location: '',
        phone: '',
        email: '',
        education: [],
        experience: [],
        projects: [],
        isDefault: false
      },
      applications: [],
      applicationsLoading: false,
      followedCompanies: [],
      followedLoading: false,
      interviews: [],

      newJob: {
        title: '',
        type: '',
        location: [],
        salary: '',
        experience: '',
        education: '',
        description: ''
      },
      // 添加编辑职位相关数据
      editJobDialogVisible: false,
      editingJob: {
        id: null,
        title: '',
        salary: '',
        city: '',
        experience: '',
        education: '',
        companyName: ''
      },
      jobList: [],
      candidateFilter: {
        job: '',
        status: ''
      },
      candidates: [],
      accountSettings: {
        email: '',
        phone: '',
        notifications: ['email', 'app']
      },
      securitySettings: {
        protection: true
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
      ],
      passwordDialogVisible: false,
      passwordForm: {
        oldPassword: '',
        newPassword: '',
        confirmPassword: ''
      },
      passwordRules: {
        oldPassword: [
          { required: true, message: '请输入当前密码', trigger: 'blur' }
        ],
        newPassword: [
          { required: true, message: '请输入新密码', trigger: 'blur' },
          { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
        ],
        confirmPassword: [
          { required: true, message: '请确认新密码', trigger: 'blur' },
          { validator: this.validateConfirmPassword, trigger: 'blur' }
        ]
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
  computed: {
    filteredCandidates() {
      let result = this.candidates;
      if (this.candidateFilter.job) {
        result = result.filter(c => c.jobId === this.candidateFilter.job);
      }
      if (this.candidateFilter.status) {
        result = result.filter(c => c.application.status === this.candidateFilter.status);
      }
      return result;
    },
    isDefaultResume() {
      // 检查当前显示的简历是否为默认简历
      return this.resume && this.resume.basicInfo && this.resume.basicInfo.isDefault;
    }
  },
  mounted() {
    // 页面加载时获取用户信息
    this.loadUserInfo();
    
    // 检查是否有 tab 参数，自动切换到对应标签页
    const tab = this.$route.query.tab;
    if (tab) {
      // 延迟执行，等待用户信息加载完成
      setTimeout(() => {
        this.handleNavSelect(tab);
      }, 500);
    }
    
    // 如果是求职者，加载关注的公司
  },
  methods: {
    formatDate(date) {
      if (!date) return '';
      // 如果是字符串，转换为Date对象
      if (typeof date === 'string') {
        date = new Date(date);
      }
      // 格式化日期为 YYYY-MM-DD HH:mm:ss
      const year = date.getFullYear();
      const month = String(date.getMonth() + 1).padStart(2, '0');
      const day = String(date.getDate()).padStart(2, '0');
      const hours = String(date.getHours()).padStart(2, '0');
      const minutes = String(date.getMinutes()).padStart(2, '0');
      const seconds = String(date.getSeconds()).padStart(2, '0');
      return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
    },

    loadUserInfo() {
      // 从sessionStorage获取当前用户信息
      const currentUser = JSON.parse(sessionStorage.getItem('user'));
      if (currentUser) {
        console.log('原始用户数据:', currentUser);
        console.log('原始用户ID类型:', typeof currentUser.id, '值:', currentUser.id);
        
        // 提取纯净的用户ID（去除可能的额外字符）
        let userId = currentUser.id;
        
        // 如果ID包含冒号（如"1762643969:1"），只取冒号前的部分
        if (typeof userId === 'string' && userId.includes(':')) {
          console.log('检测到ID包含冒号，进行清理');
          userId = userId.split(':')[0];
        }
        
        // 转换为数字
        userId = parseInt(userId);
        
        console.log('清理后的用户ID类型:', typeof userId, '值:', userId);
        console.log('即将发送的请求URL:', `${this.$apiBaseUrl}/user/current?id=${userId}`);
        
        // 从后端获取完整的用户信息
        this.$axios.get(`${this.$apiBaseUrl}/user/current?id=${userId}`)
          .then(response => {
            if (response.data.code === 200) {
              const user = response.data.data;
              // 更新用户信息
              this.userInfo.id = user.id;
              this.userInfo.name = user.realName || user.username;
              this.userInfo.avatar = user.avatar || '';
              this.userInfo.title = user.nickname || '';
              this.userInfo.email = user.email || '';
              this.userInfo.phone = user.phone || '';
              this.userInfo.location = user.location ? user.location.split(',') : [];
              this.userInfo.gender = user.gender !== null ? (user.gender ? 'male' : 'female') : '';
              this.userInfo.birthday = user.birthday || '';
              this.userInfo.bio = user.bio || '';
              this.userInfo.status = user.status;
              this.userInfo.createTime = user.createTime;
              this.userInfo.lastLoginTime = user.lastLoginTime;
              this.userInfo.lastLoginIp = user.lastLoginIp;

              // 设置用户角色
              this.userRole = user.role === 1 ? 'hr' : 'jobSeeker';
              
              // 保存公司信息（HR用户使用）
              this.userInfo.companyId = user.companyId || null;
              this.userInfo.companyName = user.companyName || '';

              // 更新账户设置
              this.accountSettings.email = user.email || '';
              this.accountSettings.phone = user.phone || '';

              // 初始化简历信息
              this.initResumeInfo();

              // 如果是求职者，加载职位申请记录
              if (this.userRole === 'jobSeeker') {
                this.loadJobApplications();
                this.loadFollowedCompanies();
              }
              
              // 如果是 HR，加载职位列表
              if (this.userRole === 'hr') {
                this.loadHrJobList();
              }
            } else {
              this.$message.error('获取用户信息失败: ' + response.data.message);
            }
          })
          .catch(error => {
            console.error('获取用户信息失败:', error);
            console.error('请求配置:', error.config);
            this.$message.error('获取用户信息失败: ' + (error.response?.data?.message || error.message || '网络错误'));
          });
      }
    },
    // 加载职位申请记录
    loadJobApplications() {
      if (this.userInfo && this.userInfo.id) {
        this.$axios.get(`${this.$apiBaseUrl}/job-application/user/${this.userInfo.id}`)
          .then(response => {
            if (response.data && response.data.code === 200 && response.data.data) {
              // 处理申请记录数据
              this.applications = response.data.data.map(item => {
                const jobId = item.jobId ?? item.job_id;
                const applyTime = item.applyTime ?? item.apply_time;
                const companyName = item.companyName ?? item.company_name;
                return {
                  id: item.id,
                  jobId: jobId,
                  jobTitle: item.title || '职位已删除',
                  company: companyName || '公司已删除',
                  salary: item.salary || '',
                  city: item.city || '',
                  experience: item.experience || '',
                  education: item.education || '',
                  applyDate: applyTime ? new Date(applyTime).toLocaleDateString() : '',
                  status: this.getApplicationStatusText(item.status),
                  statusValue: item.status,
                  feedback: item.feedback || '暂无反馈'
                };
              });

              // 更新已申请职位数量
              this.jobStats.applied = this.applications.length;
            } else {
              console.error('加载职位申请记录失败:', response.data ? response.data.msg : '未知错误');
            }
          })
          .catch(error => {
            console.error('加载职位申请记录失败:', error);
          });
      }
    },

    // 加载关注的公司列表
    loadFollowedCompanies() {
      this.followedLoading = true;
      if (this.userInfo && this.userInfo.id) {
        this.$axios.get(`${this.$apiBaseUrl}/user-follow-company/companies/${this.userInfo.id}`)
          .then(response => {
            this.followedLoading = false;
            if (response.data && response.data.code === 200 && response.data.data) {
              this.followedCompanies = response.data.data;
            } else {
              console.error('加载关注公司列表失败:', response.data ? response.data.msg : '未知错误');
            }
          })
          .catch(error => {
            this.followedLoading = false;
            console.error('加载关注公司列表失败:', error);
          });
      } else {
        this.followedLoading = false;
      }
    },

    initResumeInfo() {
      // 根据用户信息初始化简历信息
      this.resume.name = (this.userInfo.name || '未命名') + '的个人简历';
      this.resume.gender = this.userInfo.gender === 'male' ? '男' : (this.userInfo.gender === 'female' ? '女' : '');
      // 计算年龄
      if (this.userInfo.birthday) {
        const birthDate = new Date(this.userInfo.birthday);
        const today = new Date();
        this.resume.age = Math.floor((today - birthDate) / (365.25 * 24 * 60 * 60 * 1000));
      }
      this.resume.location = this.userInfo.location && this.userInfo.location.length > 0 ? this.userInfo.location.join(' ') : '';
      this.resume.phone = this.userInfo.phone || '';
      this.resume.email = this.userInfo.email || '';

      // 从后端获取用户的简历数据
      this.loadResumeData();
    },

    // 加载用户的简历数据
    loadResumeData() {
      if (this.userInfo.id) {
        // 获取用户的所有简历
        this.$axios.get(`${this.$apiBaseUrl}/resume/list/${this.userInfo.id}`)
          .then(response => {
            if (response.data.code === 200) {
              const resumes = response.data.data;
              // 查找默认简历
              let defaultResume = resumes.find(resume => resume.isDefault);
              // 如果没有默认简历，则使用最新的简历
              if (!defaultResume && resumes.length > 0) {
                defaultResume = resumes[0];
              }

              if (defaultResume) {
                // 解析默认简历数据
                this.resume.name = defaultResume.name;

                if (defaultResume.basicInfo) {
                  this.resume.basicInfo = JSON.parse(defaultResume.basicInfo);
                  this.resume.basicInfo.isDefault = defaultResume.isDefault; // 保存默认标识
                  this.resume.gender = this.resume.basicInfo.gender;
                  this.resume.age = this.resume.basicInfo.age;
                  this.resume.location = this.resume.basicInfo.location ? this.resume.basicInfo.location.join(' ') : '';
                  this.resume.phone = this.resume.basicInfo.phone;
                  this.resume.email = this.resume.basicInfo.email;
                }

                if (defaultResume.education) {
                  this.resume.education = JSON.parse(defaultResume.education);
                }

                if (defaultResume.experience) {
                  this.resume.experience = JSON.parse(defaultResume.experience);
                }

                if (defaultResume.projects) {
                  this.resume.projects = JSON.parse(defaultResume.projects);
                }
              } else {
                console.log('用户尚未创建简历');
              }
            } else {
              console.error('加载简历失败:', response.data.msg);
            }
          })
          .catch(error => {
            console.error('加载简历失败:', error);
          });
      }
    },

    handleNavSelect(index) {
      this.activeNav = index;
      
      // 如果切换到候选人管理页面且有职位，则加载第一个职位的候选人
      if (index === 'candidates' && this.jobList.length > 0) {
        // 如果还没有设置筛选条件或者候选人列表为空，则加载第一个职位的候选人
        if (!this.candidateFilter.job || this.candidates.length === 0) {
          this.candidateFilter.job = this.jobList[0].id;
          this.loadCandidatesForJob(this.jobList[0].id);
        }
      }
      
      // 如果切换到我的关注页面，重新加载关注的公司列表
      if (index === 'followed' && this.userRole === 'jobSeeker') {
        this.loadFollowedCompanies();
      }
      
      // 如果切换到职位管理或候选人管理页面，且是 HR，重新加载职位列表
      if ((index === 'post-job' || index === 'job-management' || index === 'candidates') && this.userRole === 'hr') {
        this.loadHrJobList();
      }
    },
    getStatusType(status) {
      const types = {
        '新申请': 'info',
        '已查看': '',
        '面试中': 'warning',
        '已录用': 'success',
        '已拒绝': 'danger'
      };
      return types[status] || '';
    },

    // 获取申请状态文本（从数字转换为中文）
    getApplicationStatusText(statusValue) {
      const statusMap = {
        0: '待处理',
        1: '已查看',
        2: '面试中',
        3: '已录用',
        4: '已拒绝',
        new: '新申请',
        viewed: '已查看',
        interview: '面试中',
        hired: '已录用',
        rejected: '已拒绝'
      };
      return statusMap[statusValue] || '未知';
    },

    getCandidateStatusType(status) {
      const types = {
        '新申请': 'info',
        '已查看': '',
        '面试中': 'warning',
        '已录用': 'success',
        '已拒绝': 'danger'
      };
      return types[status] || '';
    },
    handleAvatarSuccess(res, file) {
      this.userInfo.avatar = URL.createObjectURL(file.raw);
    },
    beforeAvatarUpload(file) {
      const isJPG = file.type === 'image/jpeg';
      const isPNG = file.type === 'image/png';
      const isLt2M = file.size / 1024 / 1024 < 2;

      if (!isJPG && !isPNG) {
        this.$message.error('上传头像图片只能是 JPG/PNG 格式!');
      }
      if (!isLt2M) {
        this.$message.error('上传头像图片大小不能超过 2MB!');
      }
      return (isJPG || isPNG) && isLt2M;
    },
    editResume() {
      this.$router.replace('/EditResume')
    },

    viewResume() {
      this.activeNav = 'resume';
    },
    downloadResume() {
      // 模拟下载简历
      this.$message({
        message: '简历下载功能已触发',
        type: 'success'
        });
    },
    previewResume() {
      // 模拟预览简历
      this.$message({
        message: '简历预览功能已触发',
        type: 'success'
      });
    },
    viewJobDetail(jobId) {
     //这里可以跳转到职位详情页面
      this.$router.push(`/JobDetail/${jobId}`);
    },
    
    // 查看公司详情
    viewCompanyDetail(companyId) {
      this.$router.push(`/CompanyDetail/${companyId}`);
    },
    
    // 取消关注公司
    unfollowCompany(companyId) {
      this.$confirm('确定要取消关注该公司吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.$axios.post(`${this.$apiBaseUrl}/user-follow-company/unfollow`, null, {
          params: {
            userId: this.userInfo.id,
            companyId: companyId
          }
        }).then(response => {
          if (response.data && response.data.code === 200) {
            this.$message({
              type: 'success',
              message: '取消关注成功!'
            });
            // 重新加载关注的公司列表
            this.loadFollowedCompanies();
          } else {
            this.$message({
              type: 'error',
              message: '取消关注失败：' + (response.data ? response.data.msg : '未知错误')
            });
          }
        }).catch(error => {
          console.error('取消关注失败:', error);
          this.$message({
            type: 'error',
            message: '取消关注失败：' + (error.response?.data?.message || error.message || '网络错误')
          });
        });
      }).catch(() => {
        this.$message({
          type: 'info',
          message: '已取消操作'
        });
      });
    },
    
    // eslint-disable-next-line no-unused-vars
    cancelApplication(applicationId) {
      this.$confirm('确定要取消申请吗?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.$http.post(`/api/applications/${applicationId}/cancel`)
          .then(() => {
            this.$message({
              type: 'success',
              message: '取消申请成功!'
            });
            // 更新申请状态
            const application = this.applications.find(a => a.id === applicationId);
            if (application) {
              application.status = '已取消';
            }
          })
          .catch(() => {
            this.$message({
              type: 'error',
              message: '取消申请失败!'
            });
          });

        this.$message({
          type: 'success',
          message: '取消申请成功!'
        });
        // 更新申请状态
        const application = this.applications.find(a => a.id === applicationId);
        if (application) {
          application.status = '已取消';
        }
      }).catch(() => {
        this.$message({
          type: 'info',
          message: '已取消操作'
        });
      });
    },

    submitJob() {
      // 表单验证
      if (!this.newJob.title) {
        this.$message({
          message: '请输入职位名称',
          type: 'warning'
        });
        return;
      }

      if (!this.newJob.type) {
        this.$message({
          message: '请选择职位类型',
          type: 'warning'
        });
        return;
      }

      if (!this.newJob.location || this.newJob.location.length === 0) {
        this.$message({
          message: '请选择工作地点',
          type: 'warning'
        });
        return;
      }

      if (!this.newJob.salary) {
        this.$message({
          message: '请输入薪资范围',
          type: 'warning'
        });
        return;
      }

      if (!this.newJob.experience) {
        this.$message({
          message: '请选择经验要求',
          type: 'warning'
        });
        return;
      }

      if (!this.newJob.education) {
        this.$message({
          message: '请选择学历要求',
          type: 'warning'
        });
        return;
      }

      if (!this.newJob.description) {
        this.$message({
          message: '请输入职位描述',
          type: 'warning'
        });
        return;
      }

      // 构造要发送到后端的职位对象
      const job = {
        title: this.newJob.title,
        salary: this.newJob.salary,
        city: this.newJob.location[this.newJob.location.length - 1], // 取最后一级地区
        experience: this.newJob.experience,
        education: this.newJob.education,
        // 公司名称需要从用户信息中获取
        companyName: this.userInfo.name || '未设置公司名称'
      };

      // 调用后端API创建职位
      this.$axios.post(`${this.$apiBaseUrl}/job`, job)
        .then(response => {
          if (response.data.code === 200) {
            this.$message({
              message: '职位发布成功',
              type: 'success'
            });
            // 重置表单
            this.resetJobForm();

            // 重新加载职位列表以显示新发布的职位
            this.loadHrJobList();

            // 如果在HR主页，需要刷新职位列表
            if (this.$route.name === 'HrHome') {
              this.$emit('job-posted');
            }
          } else {
            this.$message({
              message: '职位发布失败: ' + response.data.msg,
              type: 'error'
            });
          }
        })
        .catch(error => {
          this.$message({
            message: '职位发布失败: ' + (error.response?.data?.message || error.message || '网络错误'),
            type: 'error'
          });
        });
    },

    resetJobForm() {
      this.newJob = {
        title: '',
        type: '',
        location: [],
        salary: '',
        experience: '',
        education: '',
        description: ''
      };
    },
    saveDraft() {
      // 保存草稿到本地存储
      const draft = {
        ...this.newJob,
        savedAt: new Date().toISOString()
      };
      localStorage.setItem('jobDraft', JSON.stringify(draft));
      
      this.$message({
        message: '草稿已保存',
        type: 'success'
      });
    },
    filterCandidates() {
      this.$message({
        message: '筛选完成，找到 ' + this.filteredCandidates.length + ' 条记录',
        type: 'success'
      });
    },
    viewCandidateResume(userId) {
      // 跳转到候选人简历详情页面
      this.$router.push({
        name: 'ResumeDetail',
        params: { userId: userId }
      });
    },

    /**
     * 与候选人沟通
     */
    chatWithCandidate(candidate) {
      // 跳转到聊天页面，传递候选人信息
      this.$router.push({
        name: 'Chat',
        query: {
          partnerId: candidate.userId,
          partnerType: 'user',
          partnerName: candidate.name || '候选人'
        }
      });
    },

    saveProfile() {
      // 表单验证
      if (!this.userInfo.name) {
        this.$message({
          message: '请输入姓名',
          type: 'warning'
        });
        return;
      }

      if (!this.userInfo.gender) {
        this.$message({
          message: '请选择性别',
          type: 'warning'
        });
        return;
      }

      if (!this.userInfo.birthday) {
        this.$message({
          message: '请选择出生日期',
          type: 'warning'
        });
        return;
      }

      if (!this.userInfo.location || this.userInfo.location.length === 0) {
        this.$message({
          message: '请选择所在地',
          type: 'warning'
        });
        return;
      }

      // 构造要发送到后端的用户对象
      const user = {
        id: this.userInfo.id,
        username: this.userInfo.name,
        realName: this.userInfo.name,
        gender: this.userInfo.gender === 'male' ? true : (this.userInfo.gender === 'female' ? false : null),
        birthday: this.userInfo.birthday,
        location: this.userInfo.location.join(','),
        bio: this.userInfo.bio,
        email: this.userInfo.email,
        phone: this.userInfo.phone,
        nickname: this.userInfo.title,
        avatar: this.userInfo.avatar,
        status: this.userInfo.status
      };
      
      console.log('保存用户信息:', user);
      
      // 调用后端 API 保存个人资料
      this.$axios.put(`${this.$apiBaseUrl}/user/${this.userInfo.id}`, user)
        .then(response => {
          console.log('保存响应:', response.data);
          if (response.data.code === 200) {
            this.$message({
              message: '个人资料保存成功',
              type: 'success'
            });
            // 更新sessionStorage中的用户信息
            const currentUser = JSON.parse(sessionStorage.getItem('user'));
            if (currentUser) {
              currentUser.realName = this.userInfo.name;
              currentUser.email = this.userInfo.email;
              currentUser.phone = this.userInfo.phone;
              sessionStorage.setItem('user', JSON.stringify(currentUser));
            }
          } else {
            this.$message({
              message: '保存失败: ' + response.data.message,
              type: 'error'
            });
          }
        })
        .catch(error => {
          console.error('保存失败:', error);
          console.error('错误响应:', error.response?.data);
          this.$message({
            message: '保存失败：' + (error.response?.data?.message || error.message || '网络错误'),
            type: 'error'
          });
        });
    },
    saveSettings() {
      // 模拟保存设置的API调用
      // 实际项目中这里应该调用后端API
      // this.$http.post('/api/user/settings', this.accountSettings)
      //   .then(response => {
      //     this.$message({
      //       message: '账号设置保存成功',
      //       type: 'success'
      //     });
      //   })
      //   .catch(error => {
      //     this.$message({
      //       message: '保存失败: ' + error.message,
      //       type: 'error'
      //     });
      //   });

      this.$message({
        message: '账号设置保存成功',
        type: 'success'
      });
    },

    validateConfirmPassword(rule, value, callback) {
      if (value !== this.passwordForm.newPassword) {
        callback(new Error('两次输入密码不一致!'));
      } else {
        callback();
      }
    },
    submitPassword() {
      this.$refs.passwordForm.validate(valid => {
        if (valid) {
          this.$message({
            message: '密码修改成功',
            type: 'success'
          });
          this.passwordDialogVisible = false;
        } else {
          return false;
        }
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
    goToPosition() {
      this.$router.push('/Position');
    },
    goToCompany() {
      this.$router.push('/Company');
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
    },
    // HR: 候选人管理
    loadHrJobList() {
      console.log('加载 HR 职位列表，userInfo:', this.userInfo);
      console.log('userRole:', this.userRole);
      
      // 检查是否有公司ID或公司名称
      if (!this.userInfo.companyId && !this.userInfo.companyName) {
        console.error('未找到公司信息，无法加载职位列表');
        this.$message.warning('未找到关联的公司信息，请联系管理员');
        return;
      }
      
      // 后端只有 /job/company/{companyName} 接口，所以使用公司名称查询
      // 如果有公司名称就使用公司名称，否则提示错误
      if (!this.userInfo.companyName) {
        console.error('未找到公司名称，无法加载职位列表');
        this.$message.warning('未找到公司名称，请联系管理员');
        return;
      }
      
      // 构建API URL - 使用公司名称查询
      const apiUrl = `${this.$apiBaseUrl}/job/company/${encodeURIComponent(this.userInfo.companyName)}`;
      
      console.log('请求URL:', apiUrl);
      console.log('公司名称:', this.userInfo.companyName);
      
      // 加载 HR 发布的职位列表
      this.$axios.get(apiUrl)
        .then(response => {
          console.log('职位列表响应:', response.data);
          if (response.data.code === 200) {
            this.jobList = response.data.data || [];
            // 更新发布职位数量
            this.hrStats.posted = this.jobList.length;
            console.log('职位列表加载成功，职位数量:', this.jobList.length);
            
            // 如果有职位，默认加载第一个职位的候选人
            if (this.jobList.length > 0 && this.activeNav === 'candidates') {
              this.$nextTick(() => {
                this.candidateFilter.job = this.jobList[0].id;
                this.loadCandidatesForJob(this.jobList[0].id);
              });
            }
          } else {
            console.error('加载职位列表失败:', response.data.msg);
            this.$message.error('加载职位列表失败：' + response.data.msg);
          }
        })
        .catch(error => {
          console.error('加载职位列表失败:', error);
          this.$message.error('加载职位列表失败：' + (error.response?.data?.message || error.message || '网络错误'));
        });
    },
    
    // HR: 编辑职位
    editJob(job) {
      // 将选中的职位信息填充到编辑表单中
      this.editingJob = {
        id: job.id,
        title: job.title,
        salary: job.salary,
        city: job.city,
        experience: job.experience,
        education: job.education,
        companyName: job.companyName
      };
      this.editJobDialogVisible = true;
    },
    
    // HR: 更新职位信息
    updateJob() {
      const updatedJob = {
        id: this.editingJob.id,
        title: this.editingJob.title,
        salary: this.editingJob.salary,
        city: this.editingJob.city,
        experience: this.editingJob.experience,
        education: this.editingJob.education,
        companyName: this.editingJob.companyName
      };

      // 调用后端API更新职位信息
      this.$axios.put(`${this.$apiBaseUrl}/job/${updatedJob.id}`, updatedJob)
        .then(response => {
          if (response.data.code === 200) {
            this.$message({
              message: '职位更新成功',
              type: 'success'
            });
            
            // 关闭对话框
            this.editJobDialogVisible = false;
            
            // 重新加载职位列表以显示更新后的信息
            this.loadHrJobList();
          } else {
            this.$message({
              message: '职位更新失败: ' + response.data.msg,
              type: 'error'
            });
          }
        })
        .catch(error => {
          this.$message({
            message: '职位更新失败: ' + (error.response?.data?.message || error.message || '网络错误'),
            type: 'error'
          });
        });
    },
    
    // HR: 删除职位
    deleteJob(jobId) {
      this.$confirm('确定要删除这个职位吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.$axios.delete(`${this.$apiBaseUrl}/job/${jobId}`)
          .then(response => {
            if (response.data.code === 200) {
              this.$message({
                message: '职位删除成功',
                type: 'success'
              });
              // 重新加载职位列表
              this.loadHrJobList();
            } else {
              this.$message({
                message: '职位删除失败: ' + response.data.msg,
                type: 'error'
              });
            }
          })
          .catch(error => {
            console.error('删除职位失败:', error);
            this.$message({
              message: '职位删除失败',
              type: 'error'
            });
          });
      }).catch(() => {
        this.$message({
          type: 'info',
          message: '已取消删除'
        });
      });
    },
    
    // 加载职位的候选人列表
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
                name: item.realName || item.username || '未知用户',
                user: { username: item.username || '' },
                applyDate: item.applyTime ? new Date(item.applyTime).toLocaleDateString() : '',
                statusValue: item.status,
                status: this.mapApplicationStatus(item.status),
                matchRate: Math.floor(Math.random() * 40) + 60,
                application: item
              };
            });
            console.log('处理后的候选人数据:', this.candidates);
            this.refreshCandidateStats();
          } else {
            console.error('加载候选人列表失败:', response.data.msg);
            this.candidates = [];
            this.refreshCandidateStats();
          }
        })
        .catch(error => {
          console.error('加载候选人列表失败:', error);
          this.candidates = [];
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
        this.refreshCandidateStats();
      }
    },

    refreshCandidateStats() {
      this.hrStats.candidates = this.candidates.length;
      this.hrStats.interviews = this.candidates.filter(c => c.statusValue === 'interview').length;
      this.hrStats.hired = this.candidates.filter(c => c.statusValue === 'hired').length;
    },

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
        case 'hired':
          this.showHireDialog(candidate);
          break;
        case 'rejected':
          this.showRejectDialog(candidate);
          break;
        case 'viewed':
          this.updateCandidateStatusSimple(candidate, 'viewed');
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
              this.loadCandidates();
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

        // TODO: 后端需要支持传递remark参数
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
            
            const messageMap = {
              'hired': `已录用 ${candidate.name}！${remark ? '备注：' + remark : ''}`,
              'rejected': `已拒绝 ${candidate.name}。原因：${remark || '未填写'}`,
              'interview': `已安排 ${candidate.name} 进入面试环节`
            };
            
            this.$message.success(messageMap[nextStatus] || '状态已更新');
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
  }
}
</script>

<style scoped>
.personal-center {
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

.profile-overview {
  background: white;
  border-radius: 4px;
  padding: 30px;
  margin: 30px 0;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.profile-header {
  display: flex;
  margin-bottom: 30px;
}

.avatar-container {
  width: 120px;
  text-align: center;
  margin-right: 40px;
}

.user-avatar {
  margin-bottom: 15px;
}

.profile-info {
  flex: 1;
}

.username {
  font-size: 24px;
  margin-bottom: 10px;
}

.user-title {
  font-size: 16px;
  color: #606266;
  margin-bottom: 15px;
}

.profile-meta {
  margin-bottom: 20px;
}

.profile-meta span {
  margin-right: 20px;
  color: #606266;
}

.profile-meta i {
  margin-right: 5px;
  color: #3498db;
}

.stats-card {
  display: flex;
  background: #f5f7fa;
  border-radius: 4px;
  padding: 20px;
  margin-bottom: 30px;
}

.stat-item {
  flex: 1;
  text-align: center;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #3498db;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 14px;
  color: #606266;
}

.main-content {
  display: flex;
  margin-bottom: 40px;
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

.profile-form, .job-form, .settings-form {
  max-width: 600px;
}

.resume-container {
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 20px;
}

.resume-header {
  margin-bottom: 20px;
  text-align: center;
}

.resume-header h2 {
  margin-bottom: 10px;
}

.resume-meta {
  display: flex;
  justify-content: center;
  flex-wrap: wrap;
  color: #606266;
  font-size: 14px;
}

.resume-meta span {
  margin: 0 10px;
}

.resume-section {
  margin-bottom: 20px;
}

.resume-section h3 {
  font-size: 16px;
  color: #303133;
  margin-bottom: 15px;
  padding-bottom: 5px;
  border-bottom: 1px solid #ebeef5;
}

.resume-item {
  margin-bottom: 15px;
}

.resume-item-header {
  display: flex;
  margin-bottom: 5px;
}

.resume-item-header .time {
  width: 150px;
  color: #606266;
}

.resume-item-header .title {
  flex: 1;
  font-weight: bold;
}

.resume-item-header .subtitle {
  width: 200px;
  color: #606266;
  text-align: right;
}

.resume-item-content {
  padding-left: 150px;
  color: #606266;
  line-height: 1.6;
}

.resume-actions {
  text-align: center;
  margin-top: 30px;
}



.detail-item i {
  margin-right: 8px;
  color: #3498db;
}


.candidate-filter {
  margin-bottom: 20px;
}

.candidate-filter .el-select {
  width: 200px;
  margin-right: 15px;
}



.item-header i {
  margin-right: 10px;
  color: #3498db;
}



.footer {
  text-align: center;
  padding: 40px 0;
  margin-top: auto;
  color: #909399;
  border-top: 1px solid #ebeef5;
  background-color: white;
}

.footer-links {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-bottom: 20px;
}



.company-details .detail-item {
  display: flex;
  align-items: flex-start;
  margin-bottom: 8px;
  font-size: 13px;
  color: #606266;
}

.company-details .detail-item i {
  margin-right: 8px;
  color: #3498db;
  min-width: 16px;
}


.empty-state i {
  margin-bottom: 20px;
  color: #c0c4cc;
}

.empty-state p {
  margin-bottom: 20px;
  font-size: 16px;
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

  .profile-header {
    flex-direction: column;
    align-items: center;
    text-align: center;
  }

  .avatar-container {
    margin-right: 0;
    margin-bottom: 20px;
  }

  .profile-meta {
    justify-content: center;
  }

  .stats-card {
    flex-wrap: wrap;
  }

  .stat-item {
    width: 50%;
    margin-bottom: 15px;
  }
}

@media (max-width: 768px) {
  .profile-meta span {
    display: block;
    margin: 5px 0;
    text-align: center;
  }

  .resume-item-header {
    flex-direction: column;
  }

  .resume-item-header .time,
  .resume-item-header .subtitle {
    width: auto;
    text-align: left;
  }

  .resume-item-content {
    padding-left: 0;
  }

}
</style>