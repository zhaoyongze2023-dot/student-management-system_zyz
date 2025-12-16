<template>
  <div class="error-page">
    <div class="error-content">
      <h1>403</h1>
      <h2>抱歉，您没有权限访问此页面</h2>
      <p>请联系管理员获取相应权限</p>
      <el-button type="primary" @click="goBack">返回上一页</el-button>
      <el-button @click="goHome">返回首页</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

const goBack = () => {
  router.go(-1)
}

const goHome = () => {
  router.push('/')
}
</script>

<style lang="scss" scoped>
.error-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
}

.error-content {
  text-align: center;
  
  h1 {
    font-size: 120px;
    color: #409eff;
    margin: 0;
    line-height: 1;
  }
  
  h2 {
    font-size: 24px;
    color: #333;
    margin: 20px 0 10px;
  }
  
  p {
    color: #999;
    margin-bottom: 30px;
  }
  
  .el-button {
    margin: 0 10px;
  }
}
</style>
