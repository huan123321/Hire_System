/**
 * 通用混入 - 处理用户认证和导航
 */
export const authMixin = {
  data() {
    return {
      userInfo: null,
      userRole: ''
    };
  },
  created() {
    this.initUserInfo();
  },
  methods: {
    initUserInfo() {
      const userInfoStr = sessionStorage.getItem('userInfo');
      if (userInfoStr) {
        try {
          this.userInfo = JSON.parse(userInfoStr);
          // 根据角色字段判断用户类型
          this.userRole = this.userInfo.role || 'jobSeeker';
        } catch (e) {
          console.error('解析用户信息失败:', e);
          this.userInfo = null;
          this.userRole = '';
        }
      }
    },
    
    goToHome() {
      this.$router.push('/');
    },
    
    goToPosition() {
      this.$router.push('/position');
    },
    
    goToCompany() {
      this.$router.push('/company');
    },
    
    goToCertificate() {
      this.$router.push('/certification');
    },
    
    goToPerson() {
      this.$router.push('/person');
    },
    
    logout() {
      this.$confirm('确定要退出登录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        sessionStorage.removeItem('userInfo');
        sessionStorage.removeItem('token');
        this.$message.success('已退出登录');
        this.$router.push('/login');
      }).catch(() => {});
    },
    
    requireAuth() {
      const userInfo = sessionStorage.getItem('userInfo');
      if (!userInfo) {
        this.$message.error('请先登录');
        this.$router.push('/login');
        return false;
      }
      return true;
    }
  }
};

/**
 * 分页混入
 */
export const paginationMixin = {
  data() {
    return {
      currentPage: 1,
      pageSize: 10,
      total: 0
    };
  },
  methods: {
    handlePageChange(page) {
      this.currentPage = page;
      this.loadData();
      window.scrollTo(0, 0);
    },
    
    loadData() {
      // 由具体组件实现
      console.warn('loadData method should be implemented');
    }
  }
};

/**
 * 搜索混入
 */
export const searchMixin = {
  data() {
    return {
      searchKeyword: '',
      searchFilter: '',
      resetSearch: false
    };
  },
  methods: {
    handleSearch({ keyword, filter }) {
      this.searchKeyword = keyword;
      this.searchFilter = filter;
      this.currentPage = 1;
      this.loadData();
    },
    
    resetSearchCondition() {
      this.searchKeyword = '';
      this.searchFilter = '';
      this.resetSearch = !this.resetSearch;
      this.currentPage = 1;
      this.loadData();
    }
  }
};

/**
 * 加载状态混入
 */
export const loadingMixin = {
  data() {
    return {
      loading: false,
      loadingText: '加载中...'
    };
  },
  methods: {
    showLoading(text = '加载中...') {
      this.loading = true;
      this.loadingText = text;
    },
    
    hideLoading() {
      this.loading = false;
    },
    
    withLoading(asyncFn) {
      return async () => {
        this.showLoading();
        try {
          await asyncFn();
        } finally {
          this.hideLoading();
        }
      };
    }
  }
};

export default {
  authMixin,
  paginationMixin,
  searchMixin,
  loadingMixin
};
