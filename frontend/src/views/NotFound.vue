<template>
  <div class="not-found">
    <div class="container">
      <h1>404</h1>
      <h2>页面不存在</h2>
      <p>抱歉，您访问的页面不存在或已被删除。</p>
      <el-button type="primary" @click="goHome">返回首页</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

const goHome = () => {
  router.push({ name: 'Knowledge' })
}
</script>

<style scoped>
.not-found {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f5f5f5;
}

.container {
  text-align: center;
}

h1 {
  font-size: 120px;
  font-weight: bold;
  color: #409eff;
  margin: 0;
}

h2 {
  font-size: 32px;
  color: #333;
  margin: 20px 0 10px;
}

p {
  font-size: 16px;
  color: #999;
  margin-bottom: 30px;
}
</style>
