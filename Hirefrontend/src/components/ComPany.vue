<template>
  <div class="company-home-container">
    <!-- 顶部导航 -->
    <AppHeader />

    <div class="container">
      <!-- 搜索区域 -->
      <div class="search-section">
        <div class="search-row">
          <div class="search-input">
            <el-input placeholder="搜索公司名称，如：阿里巴巴、腾讯" v-model="searchKeyword"></el-input>
          </div>
          <div class="industry-select">
            <el-select v-model="searchIndustry" placeholder="行业">
              <el-option label="全部行业" value=""></el-option>
              <el-option label="互联网" value="互联网"></el-option>
              <el-option label="电子商务" value="电子商务"></el-option>
              <el-option label="金融" value="金融"></el-option>
              <el-option label="教育" value="教育"></el-option>
              <el-option label="房地产" value="房地产"></el-option>
              <el-option label="通信技术" value="通信技术"></el-option>
            </el-select>
          </div>
          <div class="search-btn">
            <el-button type="primary" icon="el-icon-search" @click="searchCompanies" style="width:100%;height: 40px">搜索公司</el-button>
          </div>
        </div>
      </div>

      <div class="section-title">热门公司</div>

      <!-- 主要内容区 -->
      <div class="main-content">
        <!-- 筛选侧边栏 -->
        <div class="filter-sidebar">
          <div class="filter-section">
            <div class="filter-title">行业领域</div>
            <el-radio-group v-model="filters.industry" @change="filterCompanies">
              <el-radio label="">全部</el-radio>
              <el-radio label="互联网">互联网</el-radio>
              <el-radio label="电子商务">电子商务</el-radio>
              <el-radio label="金融">金融</el-radio>
              <el-radio label="教育">教育</el-radio>
              <el-radio label="房地产">房地产</el-radio>
              <el-radio label="通信技术">通信技术</el-radio>
            </el-radio-group>
          </div>

          <div class="filter-section">
            <div class="filter-title">公司规模</div>
            <el-radio-group v-model="filters.scale" @change="filterCompanies">
              <el-radio label="">不限</el-radio>
              <el-radio label="少于50人">少于50人</el-radio>
              <el-radio label="50-200人">50-200人</el-radio>
              <el-radio label="200-500人">200-500人</el-radio>
              <el-radio label="500人以上">500人以上</el-radio>
            </el-radio-group>
          </div>

          <div class="filter-section">
            <div class="filter-title">融资阶段</div>
            <el-radio-group v-model="filters.financeStage" @change="filterCompanies">
              <el-radio label="">不限</el-radio>
              <el-radio label="未融资">未融资</el-radio>
              <el-radio label="天使轮">天使轮</el-radio>
              <el-radio label="A轮">A轮</el-radio>
              <el-radio label="B轮">B轮</el-radio>
              <el-radio label="C轮">C轮</el-radio>
              <el-radio label="D轮及以上">D轮及以上</el-radio>
              <el-radio label="上市公司">上市公司</el-radio>
            </el-radio-group>
          </div>

          <div class="filter-section">
            <div class="filter-title">工作地点</div>
            <el-select v-model="filters.location" placeholder="全部" clearable filterable style="width:100%" @change="filterCompanies">
              <el-option label="全部" value=""></el-option>
              <el-option-group
                  v-for="province in cityOptions"
                  :key="province.value"
                  :label="province.value">
                <el-option :label="province.label" :value="province.value"></el-option>
                <el-option
                    v-for="city in province.children"
                    :key="city.value"
                    :label="city.label"
                    :value="city.value">
                </el-option>
              </el-option-group>
            </el-select>
          </div>
        </div>

        <!-- 公司列表 -->
        <div class="company-list">
          <div v-for="company in filteredCompanies" :key="company.id" class="company-card" @click="goToCompanyDetail(company.id)">
            <div class="company-header">
              <div class="company-logo">
                <img :src="companyLogo(company)" :alt="company.name" style="width: 100%; height: 100%; object-fit: cover;">
              </div>
              <div class="company-info">
                <div class="company-name">{{ company.name }}</div>
                <div class="company-desc">{{ company.description }}</div>
                <div class="company-tags">
                  <!-- 公司标签处理 -->
                  <span class="company-tag" v-for="(tag, index) in getCompanyTags(company)" :key="index">{{ tag }}</span>
                </div>
              </div>
            </div>

            <div class="company-details">
              <div class="detail-item">
                <i class="fas fa-users"></i>
                <span>规模：{{ company.scale }}</span>
              </div>
              <div class="detail-item">
                <i class="fas fa-map-marker-alt"></i>
                <span>地点：{{ company.address }}</span>
              </div>
              <div class="detail-item">
                <i class="fas fa-industry"></i>
                <span>行业：{{ company.industry }}</span>
              </div>
              <div class="detail-item">
                <i class="fas fa-phone"></i>
                <span>电话：{{ company.phone }}</span>
              </div>
            </div>

            <div class="company-actions">
              <el-button type="primary" size="small" @click.stop="goToCompanyDetail(company.id)">查看详情</el-button>
            </div>
          </div>

          <!-- 分页 -->
          <div class="pagination">
            <el-pagination
                background
                layout="prev, pager, next"
                :total="total"
                :page-size="10"
                :current-page="currentPage"
                @current-change="handlePageChange">
            </el-pagination>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import AppHeader from '@/components/common/AppHeader.vue'
import chinaRegions from '@/utils/chinaRegions'
import { getCompanyLogo } from '@/utils/companyLogo'

export default {
  name: 'CompanyHome',
  components: {
    AppHeader
  },
  data() {
    return {
      searchKeyword: '',
      searchIndustry: '',
      currentPage: 1,
      total: 0,
      cityOptions: chinaRegions,
      filters: {
        industry: [],
        companySize: '',
        financeStage: '',
        location: ''
      },
      companies: [],
      filteredCompanies: []
    }
  },
  mounted() {
    // 初始化显示所有公司
    this.loadCompanies();
    
    // 从sessionStorage获取用户信息

  },
  methods: {
    // 公司头像：优先 logo，否则按行业/名称生成默认头像
    companyLogo(company) {
      return getCompanyLogo(company);
    },
    async loadCompanies() {
      try {
        const response = await this.$axios.get(this.$apiBaseUrl + '/company/list', {
          params: {
            page: this.currentPage,
            size: 10,
            keyword: this.searchKeyword,
            industry: this.searchIndustry
          }
        });
        
        if (response.data.code === 200) {
          const pageData = response.data.data;
          this.companies = pageData.records || pageData;
          this.filteredCompanies = pageData.records || pageData;
          this.total = pageData.total || 0;
        }
      } catch (error) {
        console.error('加载公司数据失败:', error);
        this.$message.error('加载公司数据失败');
      }
    },
    
    async loadCompaniesByPage(page) {
      try {
        const response = await this.$axios.get(this.$apiBaseUrl + '/company/list', {
          params: {
            page: page,
            size: 10,
            keyword: this.searchKeyword,
            industry: this.searchIndustry
          }
        });
        
        if (response.data.code === 200) {
          const pageData = response.data.data;
          this.companies = pageData.records || pageData;
          this.filteredCompanies = pageData.records || pageData;
          this.total = pageData.total || 0;
        }
      } catch (error) {
        console.error('加载公司数据失败:', error);
        this.$message.error('加载公司数据失败');
      }
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
    position() {
      this.$router.push('/Position');
    },
    goToCompany() {
      // 当前已经在公司页面，刷新页面或不做任何操作
      this.$router.push('/ComPany');
    },
    certificate(){
      this.$router.push('/Certification');
    },

    logout() {
      this.$router.push('/');
    },
    person(){
      this.$router.push("/PerSon")
    },
    
    async searchCompanies() {
      try {
        // 重置页码为第一页
        this.currentPage = 1;
        
        const response = await this.$axios.get(this.$apiBaseUrl + '/company/list', {
          params: {
            page: this.currentPage,
            size: 10,
            keyword: this.searchKeyword,
            industry: this.searchIndustry
          }
        });
        
        if (response.data.code === 200) {
          const pageData = response.data.data;
          this.companies = pageData.records || pageData;
          this.filteredCompanies = pageData.records || pageData;
          this.total = pageData.total || 0;
          
          this.$message({
            message: `搜索到 ${this.companies.length} 家公司`,
            type: 'success'
          });
        }
      } catch (error) {
        console.error('搜索公司失败:', error);
        this.$message.error('搜索公司失败');
      }
    },
    
    async filterCompanies() {
      try {
        // 重置页码为第一页
        this.currentPage = 1;
        
        // 构建筛选参数
        const params = {
          page: this.currentPage,
          size: 10
        };
        
        if (this.filters.scale) params.scale = this.filters.scale;
        if (this.filters.industry) params.industry = this.filters.industry;
        if (this.filters.location) params.location = this.filters.location;
        
        const response = await this.$axios.get(this.$apiBaseUrl + '/company/filter', {
          params: params
        });
        
        if (response.data.code === 200) {
          const pageData = response.data.data;
          this.companies = pageData.records || pageData;
          this.filteredCompanies = pageData.records || pageData;
          this.total = pageData.total || (Array.isArray(pageData) ? pageData.length : this.companies.length);
        }
      } catch (error) {
        console.error('筛选公司失败:', error);
        this.$message.error('筛选公司失败');
      }
    },
    
    handlePageChange(page) {
      this.currentPage = page;
      this.loadCompaniesByPage(page);
    },
    
    getCompanyTags(company) {
      // 根据公司信息生成标签
      const tags = [];
      if (company.scale) tags.push(company.scale);
      if (company.industry) tags.push(company.industry);
      return tags;
    },
    
    goToCompanyDetail(companyId) {
      // 跳转到公司详情页
      this.$router.push('/CompanyDetail/' + companyId);
    },
    
    viewJobs(company) {
      this.$message({
        message: `查看${company.name}的职位`,
        type: 'info'
      });
      // 这里可以跳转到该公司的职位列表页面
    },
    
    /**
     * 跳转到消息中心
     */
    goToMessageCenter() {
      this.$router.push({
        name: 'MessageCenter'
      });
    },
    
    followCompany(company) {
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
          
      // 显示加载提示
      const loading = this.$loading({
        lock: true,
        text: '操作中...',
        spinner: 'el-icon-loading',
        background: 'rgba(0, 0, 0, 0.7)'
      });
            
      // 检查是否已关注，决定是关注还是取消关注
      const isFollowed = company.isFollowed || false;
      const url = isFollowed 
        ? `${this.$apiBaseUrl}/user-follow-company/unfollow`
        : `${this.$apiBaseUrl}/user-follow-company/follow`;
      
      const message = isFollowed ? '确定要取消关注吗？' : `确定要关注${company.name}吗？`;
      
      // 显示确认对话框
      this.$confirm(message, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: isFollowed ? 'warning' : 'info'
      }).then(() => {
        // 调用后端 API 关注/取消关注公司
        this.$axios.post(url, null, {
          params: {
            userId: currentUser.id,
            companyId: company.id
          }
        })
        .then(response => {
          loading.close();
          if (response.data.code === 200) {
            // 更新公司的关注状态
            company.isFollowed = !isFollowed;
            const message = isFollowed ? '已取消关注' : `已关注${company.name}`;
            this.$message({
              message: message,
              type: isFollowed ? 'info' : 'success',
              duration: 2000
            });
            // 如果是关注操作，提示用户可以到个人中心查看
            if (!isFollowed) {
              setTimeout(() => {
                this.$message({
                  message: '您可以到个人中心查看已关注的公司',
                  type: 'info',
                  duration: 3000
                });
              }, 1000);
            }
          } else {
            this.$message({
              message: '操作失败：' + response.data.msg,
              type: 'error'
            });
          }
        })
        .catch(error => {
          loading.close();
          console.error('操作失败:', error);
          this.$message({
            message: '操作失败',
            type: 'error'
          });
        });
      }).catch(() => {
        this.$message({
          message: '已取消操作',
          type: 'info'
        });
      });
    }
  }
}
</script>

<style scoped>
.company-home-container {
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

.industry-select {
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

.company-list {
  flex: 1;
}

.company-card {
  background: white;
  border-radius: 4px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  transition: all 0.3s;
  cursor: pointer;
}

.company-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.15);
}

.company-header {
  display: flex;
  margin-bottom: 15px;
}

.company-logo {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  margin-right: 15px;
  background-color: #f5f7fa;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #909399;
  font-size: 24px;
}

.company-info {
  flex: 1;
}

.company-name {
  font-size: 20px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 8px;
}

.company-desc {
  color: #606266;
  font-size: 14px;
  margin-bottom: 10px;
  line-height: 1.5;
}

.company-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.company-tag {
  background-color: #f0f5ff;
  color: #3498db;
  padding: 3px 8px;
  border-radius: 12px;
  font-size: 12px;
}

.company-details {
  margin-bottom: 15px;
}

.detail-item {
  display: flex;
  align-items: center;
  color: #606266;
  font-size: 13px;
  margin-bottom: 6px;
}

.detail-item i {
  margin-right: 6px;
  width: 14px;
  color: #909399;
}

.company-stats {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
  padding: 15px;
  background-color: #f8f9fa;
  border-radius: 4px;
}

.stat-item {
  text-align: center;
  flex: 1;
}

.stat-number {
  font-size: 18px;
  font-weight: bold;
  color: #3498db;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 12px;
  color: #909399;
}

.company-actions {
  display: flex;
  gap: 10px;
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

  .industry-select, .search-btn {
    width: 100%;
  }

  .company-stats {
    flex-direction: column;
    gap: 10px;
  }

  .company-actions {
    flex-direction: column;
  }
}
</style>