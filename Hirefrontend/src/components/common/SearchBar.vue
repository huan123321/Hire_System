<template>
  <div class="search-section">
    <div class="search-row">
      <div class="search-input">
        <el-input 
          :placeholder="keywordPlaceholder" 
          v-model="searchKeyword"
          @keyup.enter.native="handleSearch"
          clearable>
        </el-input>
      </div>
      <div class="filter-select">
        <el-select 
          v-model="selectedFilter" 
          :placeholder="filterPlaceholder"
          clearable>
          <el-option 
            v-for="option in filterOptions" 
            :key="option.value" 
            :label="option.label" 
            :value="option.value">
          </el-option>
        </el-select>
      </div>
      <div class="search-btn">
        <el-button 
          type="primary" 
          icon="el-icon-search" 
          @click="handleSearch"
          :loading="loading"
          style="width:100%;height:40px;">
          {{ searchBtnText }}
        </el-button>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'SearchBar',
  props: {
    keywordPlaceholder: {
      type: String,
      default: '输入关键词搜索'
    },
    filterPlaceholder: {
      type: String,
      default: '筛选条件'
    },
    filterOptions: {
      type: Array,
      default: () => []
    },
    searchBtnText: {
      type: String,
      default: '搜索'
    },
    loading: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      searchKeyword: '',
      selectedFilter: ''
    }
  },
  methods: {
    handleSearch() {
      this.$emit('search', {
        keyword: this.searchKeyword,
        filter: this.selectedFilter
      });
    }
  },
  watch: {
    // 监听外部重置
    '$parent.resetSearch'(newVal) {
      if (newVal) {
        this.searchKeyword = '';
        this.selectedFilter = '';
      }
    }
  }
}
</script>

<style scoped lang="scss">
.search-section {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;

  .search-row {
    display: grid;
    grid-template-columns: 2fr 1fr auto;
    gap: 15px;
    align-items: center;

    @media (max-width: 768px) {
      grid-template-columns: 1fr;
    }
  }

  .search-input,
  .filter-select {
    width: 100%;
  }

  .search-btn {
    min-width: 120px;
  }
}
</style>
