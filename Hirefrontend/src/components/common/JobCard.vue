<template>
  <div class="job-card" @click="handleClick">
    <div class="job-header">
      <div class="job-title">{{ job.title }}</div>
      <div class="job-salary" :class="getSalaryClass(job.salary)">{{ job.salary }}</div>
    </div>
    
    <div class="job-tags">
      <span 
        class="job-tag" 
        v-for="(tag, index) in getJobTags(job)" 
        :key="index">
        {{ tag }}
      </span>
    </div>
    
    <div class="job-info">
      <div class="job-info-item">
        <i class="fas fa-map-marker-alt"></i> 
        {{ job.city || '不限' }}
      </div>
      <div class="job-info-item">
        <i class="fas fa-user-tie"></i> 
        {{ job.experience || '经验不限' }}
      </div>
      <div class="job-info-item">
        <i class="fas fa-graduation-cap"></i> 
        {{ job.education || '学历不限' }}
      </div>
    </div>
    
    <div class="company-info">
      <div class="company-logo">
        <i class="fas fa-building" v-if="!job.companyLogo"></i>
        <img :src="job.companyLogo" :alt="job.companyName" v-if="job.companyLogo" />
      </div>
      <div class="company-detail">
        <div class="company-name">{{ job.companyName }}</div>
        <div class="company-desc">
          {{ job.industry || '行业不限' }} · {{ job.companySize || '规模不限' }}
        </div>
      </div>
      <el-button 
        type="primary" 
        size="small" 
        @click.stop="handleApply"
        :loading="applying">
        {{ applyBtnText }}
      </el-button>
    </div>
  </div>
</template>

<script>
export default {
  name: 'JobCard',
  props: {
    job: {
      type: Object,
      required: true
    },
    showApplyButton: {
      type: Boolean,
      default: true
    },
    applyBtnText: {
      type: String,
      default: '立即申请'
    },
    applying: {
      type: Boolean,
      default: false
    }
  },
  methods: {
    handleClick() {
      this.$emit('click', this.job);
    },
    handleApply() {
      this.$emit('apply', this.job);
    },
    getJobTags(job) {
      if (job.tags) {
        return job.tags.split(',');
      }
      const tags = [];
      if (job.experience) tags.push(job.experience);
      if (job.education) tags.push(job.education);
      return tags;
    },
    getSalaryClass(salary) {
      if (!salary) return '';
      if (salary.includes('25k')) return 'salary-high';
      if (salary.includes('15k')) return 'salary-mid';
      return '';
    }
  }
}
</script>

<style scoped lang="scss">
.job-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 15px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: all 0.3s;
  cursor: pointer;

  &:hover {
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
    transform: translateY(-2px);
  }

  .job-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;

    .job-title {
      font-size: 18px;
      font-weight: bold;
      color: #303133;
    }

    .job-salary {
      font-size: 16px;
      font-weight: bold;
      
      &.salary-high {
        color: #f56c6c;
      }
      
      &.salary-mid {
        color: #e6a23c;
      }
    }
  }

  .job-tags {
    display: flex;
    gap: 8px;
    margin-bottom: 12px;

    .job-tag {
      display: inline-block;
      padding: 4px 10px;
      background: #f0f9ff;
      color: #409EFF;
      border-radius: 4px;
      font-size: 12px;
    }
  }

  .job-info {
    display: flex;
    gap: 20px;
    margin-bottom: 15px;
    color: #909399;
    font-size: 13px;

    .job-info-item {
      display: flex;
      align-items: center;
      gap: 5px;

      i {
        color: #409EFF;
      }
    }
  }

  .company-info {
    display: flex;
    align-items: center;
    gap: 12px;
    padding-top: 15px;
    border-top: 1px solid #EBEEF5;

    .company-logo {
      width: 50px;
      height: 50px;
      border-radius: 8px;
      background: #f5f7fa;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #C0C4CC;
      font-size: 24px;

      img {
        width: 100%;
        height: 100%;
        object-fit: cover;
        border-radius: 8px;
      }
    }

    .company-detail {
      flex: 1;

      .company-name {
        font-size: 14px;
        font-weight: bold;
        color: #303133;
        margin-bottom: 4px;
      }

      .company-desc {
        font-size: 12px;
        color: #909399;
      }
    }
  }
}
</style>
