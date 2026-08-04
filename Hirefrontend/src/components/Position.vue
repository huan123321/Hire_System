<template>
  <div class="job-home-container">
    <!-- 顶部导航 -->
    <AppHeader />

    <div class="container">
      <!-- 搜索区域 -->
      <div class="search-section">
        <div class="search-row">
          <div class="search-input">
            <el-input placeholder="输入职位关键词，如：前端开发、Java工程师" v-model="searchKeyword"></el-input>
          </div>
          <div class="city-select">
            <el-select v-model="searchCity" placeholder="城市">
              <el-option label="全国" value=""></el-option>
              <el-option label="北京" value="北京"></el-option>
              <el-option label="上海" value="上海"></el-option>
              <el-option label="广州" value="广州"></el-option>
              <el-option label="深圳" value="深圳"></el-option>
              <el-option label="杭州" value="杭州"></el-option>
            </el-select>
          </div>
          <div class="search-btn">
            <el-button type="primary" icon="el-icon-search" @click="searchJobs" style="width:100%;height:40px;">搜索</el-button>
          </div>
        </div>
      </div>

      <div class="section-title">
        热门职位
        <span class="sort-tabs">
          <span :class="{ active: sortBy !== 'hot' }" @click="sortBy='time'; loadJobs()">最新</span>
          <span :class="{ active: sortBy === 'hot' }" @click="sortBy='hot'; loadJobs()">🔥 最热</span>
        </span>
      </div>

      <!-- 主要内容区 -->
      <div class="main-content">
        <!-- 筛选侧边栏 -->
        <div class="filter-sidebar">
          <div class="filter-section">
            <div class="filter-title">薪资范围</div>
            <el-radio-group v-model="filters.salary" @change="filterJobs">
              <el-radio label="">不限</el-radio>
              <el-radio label="5k以下">5k以下</el-radio>
              <el-radio label="5k-10k">5k-10k</el-radio>
              <el-radio label="10k-15k">10k-15k</el-radio>
              <el-radio label="15k-25k">15k-25k</el-radio>
              <el-radio label="25k以上">25k以上</el-radio>
            </el-radio-group>
          </div>

          <div class="filter-section">
            <div class="filter-title">经验要求</div>
            <el-checkbox-group v-model="filters.experience" @change="filterJobs">
              <el-checkbox label="应届生">应届生</el-checkbox>
              <el-checkbox label="1-3年">1-3年</el-checkbox>
              <el-checkbox label="3-5年">3-5年</el-checkbox>
              <el-checkbox label="5年以上">5年以上</el-checkbox>
            </el-checkbox-group>
          </div>

          <div class="filter-section">
            <div class="filter-title">学历要求</div>
            <el-checkbox-group v-model="filters.education" @change="filterJobs">
              <el-checkbox label="大专">大专</el-checkbox>
              <el-checkbox label="本科">本科</el-checkbox>
              <el-checkbox label="硕士">硕士</el-checkbox>
              <el-checkbox label="博士">博士</el-checkbox>
            </el-checkbox-group>
          </div>

          <div class="filter-section">
            <div class="filter-title">公司规模</div>
            <el-radio-group v-model="filters.companySize" @change="filterJobs">
              <el-radio label="">不限</el-radio>
              <el-radio label="少于50人">少于50人</el-radio>
              <el-radio label="50-200人">50-200人</el-radio>
              <el-radio label="200-500人">200-500人</el-radio>
              <el-radio label="500人以上">500人以上</el-radio>
            </el-radio-group>
          </div>
        </div>

        <!-- 职位列表 -->
        <div class="job-list">
          <div v-for="job in filteredJobs" :key="job.id" class="job-card" @click="viewJobDetail(job)">
            <div class="job-header">
              <div class="job-title">{{ job.title }}</div>
              <div class="job-salary">{{ job.salary }}</div>
            </div>
            <i class="el-icon-star-off favorite-btn" 
               :class="{ 'is-fav': favJobs[job.id] }"
               @click.stop="toggleFavorite(job)"></i>
            <div class="job-tags">
              <span class="job-tag" v-for="(tag, index) in job.tags ? job.tags.split(',') : []" :key="index">{{ tag }}</span>
            </div>
            <div class="job-info">
              <div class="job-info-item">
                <i class="fas fa-map-marker-alt"></i> {{ job.city }}
              </div>
              <div class="job-info-item">
                <i class="fas fa-user-tie"></i> {{ job.experience }}
              </div>
              <div class="job-info-item">
                <i class="fas fa-graduation-cap"></i> {{ job.education }}
              </div>
            </div>
            <div class="company-info">
              <div class="company-logo">
                <i class="fas fa-building"></i>
              </div>
              <div class="company-detail">
                <div class="company-name">{{ job.companyName }}</div>
                <div class="company-desc">{{ job.industry }} · {{ job.companySize }}</div>
              </div>
              <div class="job-actions">
                <span class="job-hot" v-if="jobScores[job.id]">{{ jobScores[job.id] }} 热度</span>
                <el-button type="primary" size="small" @click.stop="applyJob(job)">立即申请</el-button>
              </div>
            </div>
          </div>

          <!-- 分页 -->
          <div class="pagination">
            <el-pagination
                background
                layout="prev, pager, next"
                :total="total"
                :page-size="10"
                @current-change="handlePageChange"
                :current-page="currentPage">
            </el-pagination>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import AppHeader from '@/components/common/AppHeader.vue'

export default {
  name: 'JobHome',
  components: {
    AppHeader
  },
  data() {
    return {
      searchKeyword: '',
      searchCity: '',
      currentPage: 1,
      total: 0, // 总数据量
      filters: {
        salary: '',
        experience: [],
        education: [],
        companySize: ''
      },
      jobs: [],
      filteredJobs: [],
      allJobs: [],
      favJobs: {},
      jobScores: {},
      sortBy: 'time'
    }
  },
  mounted() {
    this.loadJobs();
    this.loadFavorites();
  },
  methods: {
    async loadJobs() {
      try {
        const params = { page: this.currentPage, size: 10, keyword: this.searchKeyword, city: this.searchCity };
        if (this.sortBy === 'hot') params.sort = 'hot';
        const response = await this.$axios.get(this.$apiBaseUrl + '/job/list', { params });
        if (response.data.code === 200) {
          const pageData = response.data.data;
          this.jobs = pageData.records;
          this.allJobs = pageData.records;
          this.filteredJobs = pageData.records;
          this.total = pageData.total;
          this.loadScores();
        }
      } catch (error) {
        console.error('加载职位数据失败:', error);
        this.$message.error('加载职位数据失败');
      }
    },

    // 申请职位
    async applyJob(job) {
      // 从 sessionStorage 获取当前用户信息
      const currentUser = JSON.parse(sessionStorage.getItem('user'));
      if (!currentUser) {
        this.$message({
          message: '请先登录',
          type: 'warning'
        });
        this.$router.push('/');
        return;
      }
    
      // 显示确认对话框
      try {
        const confirmResult = await this.$confirm(
          `确定要申请「${job.title}」这个职位吗？`,
          '申请职位确认',
          {
            confirmButtonText: '确定申请',
            cancelButtonText: '再想想',
            type: 'warning',
            center: true
          }
        ).catch(() => {
          // 用户取消操作
          return 'cancel';
        });
    
        if (confirmResult === 'cancel') {
          return;
        }
    
        // 显示加载提示
        const loading = this.$loading({
          lock: true,
          text: '正在申请职位...',
          spinner: 'el-icon-loading',
          background: 'rgba(0, 0, 0, 0.7)'
        });
    
        // 调用后端 API 申请职位
        const response = await this.$axios.post(
          `${this.$apiBaseUrl}/job-application/apply`,
          null,
          {
            params: {
              jobId: job.id,
              userId: currentUser.id,
              resumeId: null // 可选的简历 ID，如果有简历可以选择
            }
          }
        );
    
        loading.close();
    
        if (response.data.code === 200) {
          this.$message({
            message: '职位申请成功！您可以在个人中心查看申请记录',
            type: 'success',
            duration: 3000
          });
        } else {
          this.$message({
            message: '申请失败：' + response.data.msg,
            type: 'error'
          });
        }
      } catch (error) {
        // 关闭加载提示（如果有的话）
        if (this.$loading && this.$loading.service) {
          this.$loading.service().close();
        }
            
        console.error('职位申请失败:', error);
            
        // 如果不是用户取消操作，显示错误信息
        if (error !== 'cancel') {
          this.$message({
            message: '职位申请失败，请稍后重试',
            type: 'error'
          });
        }
      }
    },

    viewJobDetail(job) {
      // 跳转到职位详情页
      this.$router.push('/JobDetail/' + job.id);
    },

    person(){
      this.$router.push("/PerSon")
    },
    certificate(){
      this.$router.push('/Certification');
    },
    async loadFavorites() {
      const user = JSON.parse(sessionStorage.getItem('user') || '{}');
      if (!user.id) return;
      try {
        const res = await this.$axios.get(`${this.$apiBaseUrl}/favorite/ids/${user.id}`);
        if (res.data.code === 200) {
          (res.data.data || []).forEach(id => { this.$set(this.favJobs, id, true); });
        }
      } catch (e) { /* ignore */ }
    },
    async toggleFavorite(job) {
      const user = JSON.parse(sessionStorage.getItem('user') || '{}');
      if (!user.id) { this.$message.warning('请先登录'); return; }
      const isFav = this.favJobs[job.id];
      const url = isFav ? '/favorite/remove' : '/favorite/add';
      try {
        const res = await this.$axios.post(`${this.$apiBaseUrl}${url}`, { userId: user.id, jobId: job.id });
        if (res.data.code === 200) {
          this.$set(this.favJobs, job.id, !isFav);
          this.$message.success(isFav ? '已取消收藏' : '收藏成功');
        }
      } catch (e) { /* ignore */ }
    },
    async loadScores() {
      const ids = this.filteredJobs.map(j => j.id);
      if (ids.length === 0) return;
      try {
        const res = await this.$axios.post(`${this.$apiBaseUrl}/job/scores`, ids);
        if (res.data.code === 200) {
          Object.entries(res.data.data || {}).forEach(([k, v]) => {
            this.$set(this.jobScores, Number(k), v);
          });
        }
      } catch (e) { /* ignore */ }
    },
    async searchJobs() {
      try {
        // 重置页码为第一页
        this.currentPage = 1;
        
        const response = await this.$axios.get(this.$apiBaseUrl + '/job/list', {
          params: {
            page: this.currentPage,
            size: 10,
            keyword: this.searchKeyword,
            city: this.searchCity
          }
        });
        if (response.data.code === 200) {
          const pageData = response.data.data;
          this.filteredJobs = pageData.records || pageData;
          this.allJobs = this.filteredJobs;
          this.jobs = this.filteredJobs;
          this.total = pageData.total || (Array.isArray(pageData) ? pageData.length : 0);
          this.$message({
            message: `搜索到 ${this.filteredJobs.length} 条职位信息`,
            type: 'success'
          });
        }
      } catch (error) {
        console.error('搜索职位失败:', error);
        this.$message.error('搜索职位失败');
      }
    },
    async filterJobs() {
      try {
        // 重置页码为第一页
        this.currentPage = 1;
        
        // 构建筛选参数
        const params = {
          page: this.currentPage,
          size: 10
        };
        
        if (this.filters.salary) params.salary = this.filters.salary;
        if (this.filters.experience.length > 0) params.experience = this.filters.experience[0];
        if (this.filters.education.length > 0) params.education = this.filters.education[0];
        
        const response = await this.$axios.get(this.$apiBaseUrl + '/job/filter', {
          params: params
        });
        
        if (response.data.code === 200) {
          const pageData = response.data.data;
          this.filteredJobs = pageData.records || pageData;
          this.allJobs = this.filteredJobs;
          this.jobs = this.filteredJobs;
          this.total = pageData.total || (Array.isArray(pageData) ? pageData.length : 0);
        }
      } catch (error) {
        console.error('筛选职位失败:', error);
        this.$message.error('筛选职位失败');
      }
    },

    handlePageChange(page) {
      console.log(`切换到第 ${page} 页`);
      this.currentPage = page;
      // 实际应用中这里应该请求对应页面的数据
      this.loadJobsByPage(page);
    },
    
    async loadJobsByPage(page) {
      try {
        const response = await this.$axios.get(this.$apiBaseUrl + '/job/list', {
          params: {
            page: page,
            size: 10,
            keyword: this.searchKeyword,
            city: this.searchCity
          }
        });
        
        if (response.data.code === 200) {
          const pageData = response.data.data;
          this.jobs = pageData.records || pageData;
          this.allJobs = pageData.records || pageData;
          this.filteredJobs = pageData.records || pageData;
          this.total = pageData.total || (Array.isArray(pageData) ? pageData.length : 0);
        }
      } catch (error) {
        console.error('加载职位数据失败:', error);
        this.$message.error('加载职位数据失败');
      }
    }
  }
}
</script>

<style scoped>
.job-home-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
  font-family: "Helvetica Neue", Helvetica, "PingFang SC", "Hiragino Sans GB", Arial, sans-serif;
}

body {
  background-color: #f5f7fa;
  color: #333;
  line-height: 1.6;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 15px;
}

.search-section {
  background: white;
  padding: 25px;
  border-radius: 4px;
  margin-top: 30px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.search-row {
  display: flex;
  gap: 15px;
}

.search-input {
  flex: 1;
}

.city-select {
  width: 150px;
}

.search-btn {
  width: 120px;
}

.main-content {
  display: flex;
  margin-top: 30px;
  gap: 20px;
  flex: 1;
}

.filter-sidebar {
  width: 280px;
  background: white;
  border-radius: 4px;
  padding: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  align-self: flex-start;
}

.filter-section {
  margin-bottom: 20px;
}

.filter-title {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 10px;
  color: #303133;
}

.job-list {
  flex: 1;
}

.job-card {
  background: white;
  border-radius: 4px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  transition: all 0.3s;
  cursor: pointer;
  display: flex;
  flex-direction: column;
}

.job-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.15);
}

.job-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.job-title {
  font-size: 18px;
  font-weight: bold;
  color: #303133;
}

.job-salary {
  color: #e74c3c;
  font-weight: bold;
  font-size: 16px;
}

.job-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 15px;
}

.job-tag {
  background-color: #f0f5ff;
  color: #3498db;
  padding: 4px 10px;
  border-radius: 4px;
  font-size: 12px;
}

.job-info {
  display: flex;
  margin-bottom: 15px;
  color: #606266;
  flex-wrap: wrap;
  gap: 15px;
}

.job-info-item {
  display: flex;
  align-items: center;
}

.job-info-item i {
  margin-right: 5px;
}

.company-info {
  display: flex;
  align-items: center;
  padding-top: 15px;
  border-top: 1px solid #ebeef5;
}

.company-logo {
  width: 40px;
  height: 40px;
  border-radius: 4px;
  margin-right: 10px;
  object-fit: cover;
  background-color: #f5f7fa;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #909399;
}

.company-detail {
  flex: 1;
}

.company-name {
  font-weight: bold;
  color: #303133;
}

.company-desc {
  font-size: 13px;
  color: #909399;
}

.pagination {
  margin-top: 30px;
  text-align: center;
}

.section-title {
  font-size: 22px;
  font-weight: bold;
  margin: 30px 0 20px;
  color: #303133;
  position: relative;
  padding-left: 15px;
}

.section-title::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  height: 20px;
  width: 5px;
  background-color: #3498db;
  border-radius: 2px;
}

@media (max-width: 768px) {
  .main-content {
    flex-direction: column;
  }

  .filter-sidebar {
    width: 100%;
  }

  .search-row {
    flex-direction: column;
  }

  .city-select, .search-btn {
    width: 100%;
  }
}

.job-card { position: relative; }
.favorite-btn {
  position: absolute; top: 16px; right: 16px; font-size: 20px;
  color: #ccc; cursor: pointer; z-index: 1; transition: color 0.2s;
}
.favorite-btn:hover { color: #ffb800; }
.favorite-btn.is-fav { color: #ffb800; }
.sort-tabs { float: right; font-size: 14px; font-weight: 400; }
.sort-tabs span { cursor: pointer; margin-left: 16px; color: #999; }
.sort-tabs span.active { color: #00C2A1; font-weight: 600; }
.job-actions { display: flex; align-items: center; gap: 12px; }
.job-hot { font-size: 12px; color: #FF8A00; white-space: nowrap; }</style>