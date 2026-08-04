<template>
  <div class="boss-home">
    <!-- 顶部导航 -->
    <AppHeader />

    <!-- 搜索 Hero 区域 BOSS直聘风格 -->
    <section class="search-hero">
      <div class="hero-inner">
        <h1 class="hero-title">找到你理想的工作</h1>
        <div class="search-box">
          <div class="search-tabs">
            <span class="search-tab active">找工作</span>
          </div>
          <div class="search-input-row">
            <div class="search-field">
              <i class="el-icon-search"></i>
              <input v-model="searchForm.keyword" placeholder="搜索职位、公司" @keyup.enter="searchJobs" />
            </div>
            <div class="search-divider"></div>
            <div class="search-field">
              <i class="el-icon-location-outline"></i>
              <input v-model="searchForm.city" placeholder="工作地点" @keyup.enter="searchJobs" />
            </div>
            <button class="search-btn" @click="searchJobs">搜索</button>
          </div>
          <div class="hot-tags">
            热门搜索：
            <span v-for="tag in hotTags" :key="tag" class="hot-tag" @click="quickSearch(tag)">{{ tag }}</span>
          </div>
        </div>
      </div>
    </section>

    <div class="content-area">
      <div class="content-left">
        <!-- 推荐职位 -->
        <section class="boss-section">
          <div class="section-top">
            <h2>推荐职位</h2>
            <span class="section-more" @click="viewAllPositions">查看全部 &gt;</span>
          </div>
          <div class="job-list">
            <div v-for="job in recommendedJobs" :key="job.id" class="job-card" @click="viewJobDetail(job)">
              <div class="job-card-top">
                <h3 class="job-card-title">{{ job.title }}</h3>
                <span class="job-card-salary">{{ job.salary }}</span>
              </div>
              <div class="job-card-tags">
                <span class="job-tag-item" v-for="(tag, i) in (job.tags || '').split(',')" :key="i">{{ tag.trim() }}</span>
                <span class="job-tag-item">{{ job.experience }}</span>
                <span class="job-tag-item">{{ job.education }}</span>
              </div>
              <div class="job-card-bottom">
                <div class="job-card-company">
                  <i class="el-icon-office-building"></i>
                  {{ job.companyName }}
                </div>
                <span class="job-card-time">{{ formatTime(job.createTime) }}</span>
              </div>
            </div>
          </div>
        </section>

        <!-- 热门公司 -->
        <section class="boss-section">
          <div class="section-top">
            <h2>热门公司</h2>
            <span class="section-more" @click="viewAllCompanies">查看全部 &gt;</span>
          </div>
          <div class="company-grid">
            <div v-for="c in hotCompanies" :key="c.id" class="company-card" @click="viewCompany(c)">
              <div class="company-card-avatar">
                <i class="el-icon-office-building"></i>
              </div>
              <div class="company-card-name">{{ c.name }}</div>
              <div class="company-card-industry">{{ c.industry }}</div>
            </div>
          </div>
        </section>
      </div>

      <!-- 侧边栏 -->
      <aside class="content-right">
        <div class="side-box">
          <h4>快速入口</h4>
          <el-button type="primary" plain size="small" @click="createResume">创建简历</el-button>
          <el-button type="success" plain size="small" @click="viewResumes">我的简历</el-button>
          <el-button type="warning" plain size="small" @click="viewApplications">我的申请</el-button>
          <el-button type="info" plain size="small" @click="viewInterviews">面试邀请</el-button>
        </div>
        <div class="side-box">
          <h4>求职贴士</h4>
          <ul class="tips-list">
            <li>完善简历能提高被HR发现的概率</li>
            <li>主动投递比被动等待更有效</li>
            <li>关注心仪公司，及时获取新职位</li>
            <li>面试前做好准备，了解公司背景</li>
          </ul>
        </div>
      </aside>
    </div>
  </div>
</template>

<script>
import AppHeader from '@/components/common/AppHeader.vue'

export default {
  name: 'HomepageE',
  components: {
    AppHeader
  },
  data() {
    return {
      searchForm: { keyword: '', city: '' },
      hotTags: ['Java', '前端', 'Python', '产品经理', '数据分析', '测试工程师'],
      recommendedJobs: [
        { id: 1, title: '高级 Java 工程师', salary: '20K-35K', companyName: '字节跳动', city: '北京', experience: '5年以上', education: '本科', tags: '互联网,弹性工作,五险一金', createTime: '2024-01-15T10:00:00' },
        { id: 2, title: '产品经理', salary: '25K-40K', companyName: '阿里巴巴', city: '杭州', experience: '3-5年', education: '本科', tags: '电商,年终奖,带薪年假', createTime: '2024-01-14T10:00:00' },
        { id: 3, title: '前端开发工程师', salary: '18K-30K', companyName: '腾讯', city: '深圳', experience: '1-3年', education: '本科', tags: '大厂,技术氛围好,晋升快', createTime: '2024-01-15T10:00:00' },
        { id: 4, title: '数据分析师', salary: '15K-25K', companyName: '美团', city: '北京', experience: '1-3年', education: '本科', tags: '数据分析,发展前景好,团队年轻', createTime: '2024-01-13T10:00:00' }
      ],
      hotCompanies: [
        { id: 1, name: '字节跳动', industry: '互联网' },
        { id: 2, name: '阿里巴巴', industry: '电子商务' },
        { id: 3, name: '腾讯', industry: '互联网' },
        { id: 4, name: '华为', industry: '通信技术' },
        { id: 5, name: '百度', industry: '互联网' },
        { id: 6, name: '京东', industry: '电子商务' }
      ]
    }
  },
  methods: {
    searchJobs() {
      this.$router.push('/Position');
    },
    quickSearch(tag) {
      this.searchForm.keyword = tag;
      this.searchJobs();
    },
    viewAllPositions() { this.$router.push('/Position'); },
    viewAllCompanies() { this.$router.push('/ComPany'); },
    viewJobDetail(job) { this.$router.push('/JobDetail/' + job.id); },
    viewCompany(c) { this.$router.push('/CompanyDetail/' + c.id); },
    createResume() { this.$router.push('/EditResume'); },
    viewResumes() { this.$router.push('/PerSon'); },
    viewApplications() { this.$router.push('/PerSon'); },
    viewInterviews() { this.$router.push('/InterviewManagement'); },
    formatTime(time) {
      if (!time) return '';
      const diff = new Date() - new Date(time);
      const m = Math.floor(diff / 60000);
      const h = Math.floor(diff / 3600000);
      const d = Math.floor(diff / 86400000);
      if (m < 60) return m + '分钟前';
      if (h < 24) return h + '小时前';
      if (d < 30) return d + '天前';
      return new Date(time).toLocaleDateString('zh-CN');
    }
  }
}
</script>

<style scoped>
.boss-home { min-height: 100vh; background: #F5F6FA; }

.search-hero {
  background: linear-gradient(135deg, #00C2A1 0%, #00BFA5 100%);
  padding: 50px 20px 40px;
}
.hero-inner { max-width: 780px; margin: 0 auto; }
.hero-title {
  color: #fff; font-size: 32px; font-weight: 700;
  text-align: center; margin-bottom: 24px;
}
.search-box {
  background: #fff; border-radius: 8px; padding: 16px 20px;
  box-shadow: 0 6px 20px rgba(0,0,0,0.12);
}
.search-tabs { margin-bottom: 12px; }
.search-tab { font-size: 16px; font-weight: 600; color: #00C2A1; cursor: pointer; }
.search-input-row {
  display: flex; align-items: center; border: 1px solid #E8E8E8;
  border-radius: 6px; overflow: hidden; height: 44px;
}
.search-field {
  display: flex; align-items: center; padding: 0 14px; flex: 1; gap: 8px; color: #999;
}
.search-field input { border: none; outline: none; width: 100%; font-size: 14px; color: #333; }
.search-field input::placeholder { color: #BBB; }
.search-divider { width: 1px; height: 24px; background: #E8E8E8; }
.search-btn {
  background: #00C2A1; color: #fff; border: none; padding: 0 32px;
  font-size: 15px; font-weight: 600; cursor: pointer; height: 100%;
  transition: background 0.2s;
}
.search-btn:hover { background: #00B593; }
.hot-tags { margin-top: 12px; font-size: 13px; color: #999; }
.hot-tag { color: #666; cursor: pointer; margin: 0 8px; }
.hot-tag:hover { color: #00C2A1; }

.content-area {
  max-width: 1200px; margin: 24px auto; padding: 0 20px; display: flex; gap: 20px;
}
.content-left { flex: 1; min-width: 0; }
.content-right { width: 280px; flex-shrink: 0; }

.boss-section {
  background: #fff; border-radius: 8px; padding: 24px; margin-bottom: 20px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.04);
}
.section-top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 18px; }
.section-top h2 { font-size: 18px; font-weight: 700; color: #222; }
.section-more { font-size: 13px; color: #00C2A1; cursor: pointer; }

.job-list { display: flex; flex-direction: column; gap: 1px; }
.job-card { padding: 16px 0; border-bottom: 1px solid #F0F0F0; cursor: pointer; transition: background 0.15s; }
.job-card:last-child { border-bottom: none; }
.job-card:hover { background: #FAFFFE; }
.job-card-top { display: flex; justify-content: space-between; align-items: baseline; margin-bottom: 8px; }
.job-card-title { font-size: 16px; font-weight: 600; color: #222; margin: 0; }
.job-card-salary { font-size: 16px; font-weight: 700; color: #FA604A; white-space: nowrap; }
.job-card-tags { display: flex; gap: 8px; flex-wrap: wrap; margin-bottom: 8px; }
.job-tag-item {
  font-size: 12px; color: #666; background: #F5F7FA; padding: 2px 8px; border-radius: 3px;
}
.job-card-bottom { display: flex; justify-content: space-between; align-items: center; }
.job-card-company { font-size: 13px; color: #999; display: flex; align-items: center; gap: 4px; }
.job-card-time { font-size: 12px; color: #BBB; }

.company-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px; }
.company-card { text-align: center; padding: 20px 10px; cursor: pointer; border-radius: 8px; transition: box-shadow 0.2s; }
.company-card:hover { box-shadow: 0 2px 12px rgba(0,0,0,0.08); }
.company-card-avatar {
  width: 48px; height: 48px; border-radius: 12px; background: #E6F9F5;
  color: #00C2A1; margin: 0 auto 10px; display: flex; align-items: center;
  justify-content: center; font-size: 22px;
}
.company-card-name { font-size: 15px; font-weight: 600; color: #333; margin-bottom: 4px; }
.company-card-industry { font-size: 13px; color: #999; }

.side-box {
  background: #fff; border-radius: 8px; padding: 20px; margin-bottom: 16px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.04);
}
.side-box h4 { font-size: 15px; font-weight: 600; color: #333; margin-bottom: 14px; }
.side-box .el-button { display: block; width: 100%; margin-bottom: 8px; }
.tips-list { padding: 0 0 0 16px; margin: 0; }
.tips-list li { font-size: 13px; color: #666; margin-bottom: 8px; line-height: 1.5; }

::v-deep .el-button--primary { background: #00C2A1; border-color: #00C2A1; }
::v-deep .el-button--primary:hover { background: #00B593; border-color: #00B593; }
::v-deep .el-button--primary.is-plain { color: #00C2A1; background: #E6F9F5; border-color: #00C2A1; }
::v-deep .el-button--primary.is-plain:hover { background: #00C2A1; color: #fff; }

@media (max-width: 768px) {
  .content-area { flex-direction: column; }
  .content-right { width: 100%; }
  .company-grid { grid-template-columns: repeat(2, 1fr); }
}
</style>
