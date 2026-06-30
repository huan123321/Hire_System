<template>
  <div class="company-card" @click="handleClick">
    <div class="company-header">
      <div class="company-logo">
        <i class="fas fa-building" v-if="!company.logo"></i>
        <img :src="company.logo" :alt="company.name" v-if="company.logo" />
      </div>
      <div class="company-info">
        <div class="company-name">{{ company.name }}</div>
        <div class="company-desc">{{ company.description || '暂无描述' }}</div>
        <div class="company-tags">
          <span 
            class="company-tag" 
            v-for="(tag, index) in getCompanyTags(company)" 
            :key="index">
            {{ tag }}
          </span>
        </div>
      </div>
    </div>

    <div class="company-details">
      <div class="detail-item">
        <i class="fas fa-users"></i>
        <span>规模：{{ company.scale || '不限' }}</span>
      </div>
      <div class="detail-item">
        <i class="fas fa-map-marker-alt"></i>
        <span>地点：{{ company.address || '不限' }}</span>
      </div>
      <div class="detail-item">
        <i class="fas fa-industry"></i>
        <span>行业：{{ company.industry || '不限' }}</span>
      </div>
      <div class="detail-item">
        <i class="fas fa-phone"></i>
        <span>电话：{{ company.phone || '未公布' }}</span>
      </div>
    </div>

    <div class="company-actions">
      <el-button 
        type="primary" 
        size="small" 
        @click.stop="handleViewDetail">
        查看详情
      </el-button>
      <el-button 
        v-if="showFavorite" 
        size="small" 
        :type="isFavorited ? 'success' : 'default'"
        @click.stop="handleFavorite">
        {{ isFavorited ? '已收藏' : '收藏' }}
      </el-button>
    </div>
  </div>
</template>

<script>
export default {
  name: 'CompanyCard',
  props: {
    company: {
      type: Object,
      required: true
    },
    showFavorite: {
      type: Boolean,
      default: false
    },
    isFavorited: {
      type: Boolean,
      default: false
    }
  },
  methods: {
    handleClick() {
      this.$emit('click', this.company);
    },
    handleViewDetail() {
      this.$emit('view-detail', this.company);
    },
    handleFavorite() {
      this.$emit('favorite', this.company);
    },
    getCompanyTags(company) {
      const tags = [];
      if (company.industry) tags.push(company.industry);
      if (company.scale) tags.push(company.scale);
      if (company.financeStage) tags.push(company.financeStage);
      return tags;
    }
  }
}
</script>

<style scoped lang="scss">
.company-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 15px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: all 0.3s;
  cursor: pointer;

  &:hover {
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.12);
    
  }

  .company-header {
    display: flex;
    gap: 15px;
    margin-bottom: 15px;

    .company-logo {
      width: 80px;
      height: 80px;
      border-radius: 8px;
      background: #f5f7fa;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #C0C4CC;
      font-size: 32px;
      flex-shrink: 0;

      img {
        width: 100%;
        height: 100%;
        object-fit: cover;
        border-radius: 8px;
      }
    }

    .company-info {
      flex: 1;

      .company-name {
        font-size: 18px;
        font-weight: bold;
        color: #303133;
        margin-bottom: 8px;
      }

      .company-desc {
        font-size: 14px;
        color: #606266;
        margin-bottom: 10px;
        display: -webkit-box;
        -webkit-line-clamp: 2;
        -webkit-box-orient: vertical;
        overflow: hidden;
      }

      .company-tags {
        display: flex;
        gap: 8px;
        flex-wrap: wrap;

        .company-tag {
          display: inline-block;
          padding: 4px 10px;
          background: #f0f9ff;
          color: #409EFF;
          border-radius: 4px;
          font-size: 12px;
        }
      }
    }
  }

  .company-details {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 10px;
    margin-bottom: 15px;
    padding: 15px 0;
    border-top: 1px solid #EBEEF5;
    border-bottom: 1px solid #EBEEF5;

    .detail-item {
      display: flex;
      align-items: center;
      gap: 8px;
      color: #909399;
      font-size: 13px;

      i {
        color: #409EFF;
        width: 16px;
      }
    }
  }

  .company-actions {
    display: flex;
    gap: 10px;
    justify-content: flex-end;
  }
}
</style>
