<template>
  <div class="app-wrapper">
    <AppHeader />
<div class="job-home-container">
    <!-- �������� -->

    <div class="container">
      <!-- �������� -->
      <div class="search-section">
        <div class="search-row">
          <div class="search-input">
            <el-input placeholder="����ְλ�ؼ��ʣ��磺ǰ�˿�����Java����ʦ" v-model="searchKeyword"></el-input>
          </div>
          <div class="city-select">
            <el-select v-model="searchCity" placeholder="����">
              <el-option label="ȫ��" value=""></el-option>
              <el-option label="����" value="����"></el-option>
              <el-option label="�Ϻ�" value="�Ϻ�"></el-option>
              <el-option label="����" value="����"></el-option>
              <el-option label="����" value="����"></el-option>
              <el-option label="����" value="����"></el-option>
            </el-select>
          </div>
          <div class="search-btn">
            <el-button type="primary" icon="el-icon-search" @click="searchJobs" style="width:100%;height:40px;">����</el-button>
          </div>
        </div>
      </div>

      <div class="section-title">
        ����ְλ
        <span class="sort-tabs">
          <span :class="{ active: sortBy !== 'hot' }" @click="sortBy='time'; loadJobs()">����</span>
          <span :class="{ active: sortBy === 'hot' }" @click="sortBy='hot'; loadJobs()">?? ����</span>
        </span>
      </div>

      <!-- ��Ҫ������ -->
      <div class="main-content">
        <!-- ɸѡ����� -->
        <div class="filter-sidebar">
          <div class="filter-section">
            <div class="filter-title">н�ʷ�Χ</div>
            <el-radio-group v-model="filters.salary" @change="filterJobs">
              <el-radio label="">����</el-radio>
              <el-radio label="5k����">5k����</el-radio>
              <el-radio label="5k-10k">5k-10k</el-radio>
              <el-radio label="10k-15k">10k-15k</el-radio>
              <el-radio label="15k-25k">15k-25k</el-radio>
              <el-radio label="25k����">25k����</el-radio>
            </el-radio-group>
          </div>

          <div class="filter-section">
            <div class="filter-title">����Ҫ��</div>
            <el-checkbox-group v-model="filters.experience" @change="filterJobs">
              <el-checkbox label="Ӧ����">Ӧ����</el-checkbox>
              <el-checkbox label="1-3��">1-3��</el-checkbox>
              <el-checkbox label="3-5��">3-5��</el-checkbox>
              <el-checkbox label="5������">5������</el-checkbox>
            </el-checkbox-group>
          </div>

          <div class="filter-section">
            <div class="filter-title">ѧ��Ҫ��</div>
            <el-checkbox-group v-model="filters.education" @change="filterJobs">
              <el-checkbox label="��ר">��ר</el-checkbox>
              <el-checkbox label="����">����</el-checkbox>
              <el-checkbox label="˶ʿ">˶ʿ</el-checkbox>
              <el-checkbox label="��ʿ">��ʿ</el-checkbox>
            </el-checkbox-group>
          </div>

          <div class="filter-section">
            <div class="filter-title">��˾��ģ</div>
            <el-radio-group v-model="filters.companySize" @change="filterJobs">
              <el-radio label="">����</el-radio>
              <el-radio label="����50��">����50��</el-radio>
              <el-radio label="50-200��">50-200��</el-radio>
              <el-radio label="200-500��">200-500��</el-radio>
              <el-radio label="500������">500������</el-radio>
            </el-radio-group>
          </div>
        </div>

        <!-- ְλ�б� -->
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
                <div class="company-desc">{{ job.industry }} �� {{ job.companySize }}</div>
              </div>
              <div class="job-actions">
                <span class="job-hot" v-if="jobScores[job.id]">{{ jobScores[job.id] }} �ȶ�</span>
                <el-button type="primary" size="small" @click.stop="applyJob(job)">��������</el-button>
              </div>
            </div>
          </div>

          <!-- ��ҳ -->
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
    <SimpleFooter />
  </div>


</template>

<script>

export default {
  name: 'JobHome',
  components: {},
  data() {
    return {
      searchKeyword: '',
      searchCity: '',
      currentPage: 1,
      total: 0,
      sortBy: 'time',
      jobScores: {}, // ��������
      filters: {
        salary: '',
        experience: [],
        education: [],
        companySize: ''
      },
      jobs: [],
      filteredJobs: [],
      allJobs: [],
      favJobs: {}
    }
  },
  mounted() {
    // ��ʼ����ʾ����ְλ
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
        console.error('����ְλ����ʧ��:', error);
        this.$message.error('����ְλ����ʧ��');
      }
    },

    // ����ְλ
    async applyJob(job) {
      // �� sessionStorage ��ȡ��ǰ�û���Ϣ
      const currentUser = JSON.parse(sessionStorage.getItem('user'));
      if (!currentUser) {
        this.$message({
          message: '���ȵ�¼',
          type: 'warning'
        });
        this.$router.push('/');
        return;
      }
    
      // ��ʾȷ�϶Ի���
      try {
        const confirmResult = await this.$confirm(
          `ȷ��Ҫ���롸${job.title}�����ְλ��`,
          '����ְλȷ��',
          {
            confirmButtonText: 'ȷ������',
            cancelButtonText: '������',
            type: 'warning',
            center: true
          }
        ).catch(() => {
          // �û�ȡ������
          return 'cancel';
        });
    
        if (confirmResult === 'cancel') {
          return;
        }
    
        // ��ʾ������ʾ
        const loading = this.$loading({
          lock: true,
          text: '��������ְλ...',
          spinner: 'el-icon-loading',
          background: 'rgba(0, 0, 0, 0.7)'
        });
    
        // ���ú�� API ����ְλ
        const response = await this.$axios.post(
          `${this.$apiBaseUrl}/job-application/apply`,
          null,
          {
            params: {
              jobId: job.id,
              userId: currentUser.id,
              resumeId: null // ��ѡ�ļ��� ID������м�������ѡ��
            }
          }
        );
    
        loading.close();
    
        if (response.data.code === 200) {
          this.$message({
            message: 'ְλ����ɹ����������ڸ������Ĳ鿴�����¼',
            type: 'success',
            duration: 3000
          });
        } else {
          this.$message({
            message: '����ʧ�ܣ�' + response.data.msg,
            type: 'error'
          });
        }
      } catch (error) {
        // �رռ�����ʾ������еĻ���
        if (this.$loading && this.$loading.service) {
          this.$loading.service().close();
        }
            
        console.error('ְλ����ʧ��:', error);
            
        // ��������û�ȡ����������ʾ������Ϣ
        if (error !== 'cancel') {
          this.$message({
            message: 'ְλ����ʧ�ܣ����Ժ�����',
            type: 'error'
          });
        }
      }
    },

    viewJobDetail(job) {
      // ��ת��ְλ����ҳ
      this.$router.push('/JobDetail/' + job.id);
    },

    person(){
      this.$router.push("/PerSon")
    },
    certificate(){
      this.$router.push('/Certification');
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

    async loadFavorites() {
      const user = JSON.parse(sessionStorage.getItem('user') || '{}');
      if (!user.id) return;
      try {
        const res = await this.$axios.get(`${this.$apiBaseUrl}/favorite/ids/${user.id}`);
        if (res.data.code === 200) {
          const ids = res.data.data || [];
          ids.forEach(id => { this.$set(this.favJobs, id, true); });
        }
      } catch (e) { /* ignore */ }
    },
    async toggleFavorite(job) {
      const user = JSON.parse(sessionStorage.getItem('user') || '{}');
      if (!user.id) { this.$message.warning('���ȵ�¼'); return; }
      const isFav = this.favJobs[job.id];
      const url = isFav ? '/favorite/remove' : '/favorite/add';
      try {
        const res = await this.$axios.post(`${this.$apiBaseUrl}${url}`, { userId: user.id, jobId: job.id });
        if (res.data.code === 200) {
          this.$set(this.favJobs, job.id, !isFav);
          this.$message.success(isFav ? '��ȡ���ղ�' : '�ղسɹ�');
        }
      } catch (e) { /* ignore */ }
    },
    async searchJobs() {
      try {
        // ����ҳ��Ϊ��һҳ
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
          this.filteredJobs = pageData.records;
          this.total = pageData.total;
          this.$message({
            message: `������ ${pageData.total} ��ְλ��Ϣ`,
            type: 'success'
          });
        }
      } catch (error) {
        console.error('����ְλʧ��:', error);
        this.$message.error('����ְλʧ��');
      }
    },
    async filterJobs() {
      try {
        // ����ҳ��Ϊ��һҳ
        this.currentPage = 1;
        
        // ����ɸѡ����
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
          this.filteredJobs = response.data.data;
          // ע�⣺ɸѡ�ӿڿ��ܲ�����total���������ʾ��
          if (response.data.total !== undefined) {
            this.total = response.data.total;
          }
        }
      } catch (error) {
        console.error('ɸѡְλʧ��:', error);
        this.$message.error('ɸѡְλʧ��');
      }
    },

    handlePageChange(page) {
      console.log(`�л����� ${page} ҳ`);
      this.currentPage = page;
      // ʵ��Ӧ��������Ӧ�������Ӧҳ�������
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
          this.jobs = pageData.records;
          this.allJobs = pageData.records;
          this.filteredJobs = pageData.records;
          this.total = pageData.total;
        }
      } catch (error) {
        console.error('����ְλ����ʧ��:', error);
        this.$message.error('����ְλ����ʧ��');
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

.header {
  background: linear-gradient(135deg, #3498db, #2c3e50);
  color: white;
  padding: 15px 0;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 100;
}

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
}

.nav-menu .nav-btn {
  margin-left: 15px;
  color: white;
  text-decoration: none;
  padding: 8px 15px;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.nav-menu .nav-btn:hover {
  background-color: rgba(255, 255, 255, 0.1);
}

.nav-menu .primary-btn {
  background-color: #e74c3c;
}

.nav-menu .primary-btn:hover {
  background-color: #c0392b;
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

.job-card { position: relative;
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

.footer {
  text-align: center;
  padding: 40px 0;
  margin-top: 50px;
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

.footer-link {
  color: #606266;
  text-decoration: none;
}

.footer-link:hover {
  color: #3498db;
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

  .header-content {
    flex-direction: column;
    text-align: center;
  }

  .nav-menu {
    margin-top: 15px;
    justify-content: center;
    flex-wrap: wrap;
  }

  .nav-menu .nav-btn {
    margin: 5px;
  }

  .search-row {
    flex-direction: column;
  }

  .city-select, .search-btn {
    width: 100%;
  }
}

.favorite-btn {
  position: absolute;
  top: 16px;
  right: 16px;
  font-size: 20px;
  color: #ccc;
  cursor: pointer;
  transition: color 0.2s;
  z-index: 1;
}
.favorite-btn:hover { color: #ffb800; }
.favorite-btn.is-fav { color: #ffb800; }
.sort-tabs { float: right; font-size: 14px; font-weight: 400; }
.sort-tabs span { cursor: pointer; margin-left: 16px; color: #999; }
.sort-tabs span.active { color: #00C2A1; font-weight: 600; }
.job-actions { display: flex; align-items: center; gap: 12px; }
.job-hot { font-size: 12px; color: #FF8A00; white-space: nowrap; }</style>
