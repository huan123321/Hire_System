<template>
  <div class="app-wrapper">
    <AppHeader />
<div class="company-home-container">
    <!-- �������� -->

    <div class="container">
      <!-- �������� -->
      <div class="search-section">
        <div class="search-row">
          <div class="search-input">
            <el-input placeholder="������˾���ƣ��磺����Ͱ͡���Ѷ" v-model="searchKeyword"></el-input>
          </div>
          <div class="industry-select">
            <el-select v-model="searchIndustry" placeholder="��ҵ">
              <el-option label="ȫ����ҵ" value=""></el-option>
              <el-option label="������" value="������"></el-option>
              <el-option label="��������" value="��������"></el-option>
              <el-option label="����" value="����"></el-option>
              <el-option label="����" value="����"></el-option>
              <el-option label="���ز�" value="���ز�"></el-option>
              <el-option label="ͨ�ż���" value="ͨ�ż���"></el-option>
            </el-select>
          </div>
          <div class="search-btn">
            <el-button type="primary" icon="el-icon-search" @click="searchCompanies" style="width:100%;height: 40px">������˾</el-button>
          </div>
        </div>
      </div>

      <div class="section-title">���Ź�˾</div>

      <!-- ��Ҫ������ -->
      <div class="main-content">
        <!-- ɸѡ����� -->
        <div class="filter-sidebar">
          <div class="filter-section">
            <div class="filter-title">��ҵ����</div>
            <el-radio-group v-model="filters.industry" @change="filterCompanies">
              <el-radio label="">ȫ��</el-radio>
              <el-radio label="������">������</el-radio>
              <el-radio label="��������">��������</el-radio>
              <el-radio label="����">����</el-radio>
              <el-radio label="����">����</el-radio>
              <el-radio label="���ز�">���ز�</el-radio>
              <el-radio label="ͨ�ż���">ͨ�ż���</el-radio>
            </el-radio-group>
          </div>

          <div class="filter-section">
            <div class="filter-title">��˾��ģ</div>
            <el-radio-group v-model="filters.scale" @change="filterCompanies">
              <el-radio label="">����</el-radio>
              <el-radio label="����50��">����50��</el-radio>
              <el-radio label="50-200��">50-200��</el-radio>
              <el-radio label="200-500��">200-500��</el-radio>
              <el-radio label="500������">500������</el-radio>
            </el-radio-group>
          </div>

          <div class="filter-section">
            <div class="filter-title">���ʽ׶�</div>
            <el-radio-group v-model="filters.financeStage" @change="filterCompanies">
              <el-radio label="">����</el-radio>
              <el-radio label="δ����">δ����</el-radio>
              <el-radio label="��ʹ��">��ʹ��</el-radio>
              <el-radio label="A��">A��</el-radio>
              <el-radio label="B��">B��</el-radio>
              <el-radio label="C��">C��</el-radio>
              <el-radio label="D�ּ�����">D�ּ�����</el-radio>
              <el-radio label="���й�˾">���й�˾</el-radio>
            </el-radio-group>
          </div>

          <div class="filter-section">
            <div class="filter-title">�����ص�</div>
            <el-checkbox-group v-model="filters.location" @change="filterCompanies">
              <el-checkbox label="">ȫ��</el-checkbox>
              <el-checkbox label="����">����</el-checkbox>
              <el-checkbox label="�Ϻ�">�Ϻ�</el-checkbox>
              <el-checkbox label="����">����</el-checkbox>
              <el-checkbox label="����">����</el-checkbox>
              <el-checkbox label="����">����</el-checkbox>
              <el-checkbox label="�ɶ�">�ɶ�</el-checkbox>
            </el-checkbox-group>
          </div>
        </div>

        <!-- ��˾�б� -->
        <div class="company-list">
          <div v-for="company in filteredCompanies" :key="company.id" class="company-card" @click="goToCompanyDetail(company.id)">
            <div class="company-header">
              <div class="company-logo">
                <i class="fas fa-building" v-if="!company.logo"></i>
                <img :src="company.logo" :alt="company.name" v-if="company.logo" style="width: 100%; height: 100%; object-fit: cover;">
              </div>
              <div class="company-info">
                <div class="company-name">{{ company.name }}</div>
                <div class="company-desc">{{ company.description }}</div>
                <div class="company-tags">
                  <!-- ��˾��ǩ���� -->
                  <span class="company-tag" v-for="(tag, index) in getCompanyTags(company)" :key="index">{{ tag }}</span>
                </div>
              </div>
            </div>

            <div class="company-details">
              <div class="detail-item">
                <i class="fas fa-users"></i>
                <span>��ģ��{{ company.scale }}</span>
              </div>
              <div class="detail-item">
                <i class="fas fa-map-marker-alt"></i>
                <span>�ص㣺{{ company.address }}</span>
              </div>
              <div class="detail-item">
                <i class="fas fa-industry"></i>
                <span>��ҵ��{{ company.industry }}</span>
              </div>
              <div class="detail-item">
                <i class="fas fa-phone"></i>
                <span>�绰��{{ company.phone }}</span>
              </div>
            </div>

            <div class="company-actions">
              <el-button type="primary" size="small" @click.stop="goToCompanyDetail(company.id)">�鿴����</el-button>
            </div>
          </div>

          <!-- ��ҳ -->
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
    <SimpleFooter />
  </div>


</template>

<script>

export default {
  name: 'CompanyHome',
  components: {},
  data() {
    return {
      searchKeyword: '',
      searchIndustry: '',
      currentPage: 1,
      total: 0,
      filters: {
        industry: [],
        companySize: '',
        financeStage: '',
        location: []
      },
      companies: [],
      filteredCompanies: []
    }
  },
  mounted() {
    // ��ʼ����ʾ���й�˾
    this.loadCompanies();
    
    // ��sessionStorage��ȡ�û���Ϣ

  },
  methods: {
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
          this.companies = pageData.records;
          this.filteredCompanies = pageData.records;
          this.total = pageData.total;
        }
      } catch (error) {
        console.error('���ع�˾����ʧ��:', error);
        this.$message.error('���ع�˾����ʧ��');
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
          this.companies = pageData.records;
          this.filteredCompanies = pageData.records;
          this.total = pageData.total;
        }
      } catch (error) {
        console.error('���ع�˾����ʧ��:', error);
        this.$message.error('���ع�˾����ʧ��');
      }
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
    position() {
      this.$router.push('/Position');
    },
    goToCompany() {
      // ��ǰ�Ѿ��ڹ�˾ҳ�棬ˢ��ҳ������κβ���
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
        // ����ҳ��Ϊ��һҳ
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
          this.companies = pageData.records;
          this.filteredCompanies = pageData.records;
          this.total = pageData.total;
          
          this.$message({
            message: `������ ${pageData.total} �ҹ�˾`,
            type: 'success'
          });
        }
      } catch (error) {
        console.error('������˾ʧ��:', error);
        this.$message.error('������˾ʧ��');
      }
    },
    
    async filterCompanies() {
      try {
        // ����ҳ��Ϊ��һҳ
        this.currentPage = 1;
        
        // ����ɸѡ����
        const params = {
          page: this.currentPage,
          size: 10
        };
        
        if (this.filters.scale) params.scale = this.filters.scale;
        if (this.filters.industry) params.industry = this.filters.industry;
        
        const response = await this.$axios.get(this.$apiBaseUrl + '/company/filter', {
          params: params
        });
        
        if (response.data.code === 200) {
          this.companies = response.data.data;
          this.filteredCompanies = response.data.data;
          // ע�⣺ɸѡ�ӿڿ��ܲ�����total���������ʾ��
          if (response.data.total !== undefined) {
            this.total = response.data.total;
          }
        }
      } catch (error) {
        console.error('ɸѡ��˾ʧ��:', error);
        this.$message.error('ɸѡ��˾ʧ��');
      }
    },
    
    handlePageChange(page) {
      this.currentPage = page;
      this.loadCompaniesByPage(page);
    },
    
    getCompanyTags(company) {
      // ���ݹ�˾��Ϣ���ɱ�ǩ
      const tags = [];
      if (company.scale) tags.push(company.scale);
      if (company.industry) tags.push(company.industry);
      return tags;
    },
    
    goToCompanyDetail(companyId) {
      // ��ת����˾����ҳ
      this.$router.push('/CompanyDetail/' + companyId);
    },
    
    viewJobs(company) {
      this.$message({
        message: `�鿴${company.name}��ְλ`,
        type: 'info'
      });
      // ���������ת���ù�˾��ְλ�б�ҳ��
    },
    
    /**
     * ��ת����Ϣ����
     */
    goToMessageCenter() {
      this.$router.push({
        name: 'MessageCenter'
      });
    },
    
    followCompany(company) {
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
          
      // ��ʾ������ʾ
      const loading = this.$loading({
        lock: true,
        text: '������...',
        spinner: 'el-icon-loading',
        background: 'rgba(0, 0, 0, 0.7)'
      });
            
      // ����Ƿ��ѹ�ע�������ǹ�ע����ȡ����ע
      const isFollowed = company.isFollowed || false;
      const url = isFollowed 
        ? `${this.$apiBaseUrl}/user-follow-company/unfollow`
        : `${this.$apiBaseUrl}/user-follow-company/follow`;
      
      const message = isFollowed ? 'ȷ��Ҫȡ����ע��' : `ȷ��Ҫ��ע${company.name}��`;
      
      // ��ʾȷ�϶Ի���
      this.$confirm(message, '��ʾ', {
        confirmButtonText: 'ȷ��',
        cancelButtonText: 'ȡ��',
        type: isFollowed ? 'warning' : 'info'
      }).then(() => {
        // ���ú�� API ��ע/ȡ����ע��˾
        this.$axios.post(url, null, {
          params: {
            userId: currentUser.id,
            companyId: company.id
          }
        })
        .then(response => {
          loading.close();
          if (response.data.code === 200) {
            // ���¹�˾�Ĺ�ע״̬
            company.isFollowed = !isFollowed;
            const message = isFollowed ? '��ȡ����ע' : `�ѹ�ע${company.name}`;
            this.$message({
              message: message,
              type: isFollowed ? 'info' : 'success',
              duration: 2000
            });
            // ����ǹ�ע��������ʾ�û����Ե��������Ĳ鿴
            if (!isFollowed) {
              setTimeout(() => {
                this.$message({
                  message: '�����Ե��������Ĳ鿴�ѹ�ע�Ĺ�˾',
                  type: 'info',
                  duration: 3000
                });
              }, 1000);
            }
          } else {
            this.$message({
              message: '����ʧ�ܣ�' + response.data.msg,
              type: 'error'
            });
          }
        })
        .catch(error => {
          loading.close();
          console.error('����ʧ��:', error);
          this.$message({
            message: '����ʧ��',
            type: 'error'
          });
        });
      }).catch(() => {
        this.$message({
          message: '��ȡ������',
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