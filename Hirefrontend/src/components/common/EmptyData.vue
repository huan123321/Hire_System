<template>
  <div class="empty-data" v-if="!hasData">
    <i class="fas fa-inbox"></i>
    <span>{{ text }}</span>
  </div>
</template>

<script>
export default {
  name: 'EmptyData',
  props: {
    hasData: {
      type: Boolean,
      default: false
    },
    text: {
      type: String,
      default: '暂无数据'
    }
  }
}
</script>

<style scoped lang="scss">
.empty-data {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 200px;
  gap: 15px;

  i {
    font-size: 60px;
    color: #C0C4CC;
  }

  span {
    color: #909399;
    font-size: 14px;
  }
}
</style>
