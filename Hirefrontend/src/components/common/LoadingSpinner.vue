<template>
  <div class="loading-container" v-if="loading">
    <div class="loading-content">
      <i class="fas fa-spinner fa-spin"></i>
      <span>{{ text }}</span>
    </div>
  </div>
</template>

<script>
export default {
  name: 'LoadingSpinner',
  props: {
    loading: {
      type: Boolean,
      default: true
    },
    text: {
      type: String,
      default: '加载中...'
    }
  }
}
</script>

<style scoped lang="scss">
.loading-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 200px;
  width: 100%;

  .loading-content {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 15px;

    i {
      font-size: 40px;
      color: #409EFF;
    }

    span {
      color: #909399;
      font-size: 14px;
    }
  }
}
</style>
