<template>
  <div>
    <h2 style="margin-bottom: 20px;">个人中心</h2>

    <el-card style="max-width: 600px;">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="用户ID">{{ userStore.userInfo.id }}</el-descriptions-item>
        <el-descriptions-item label="用户名">{{ userStore.userInfo.username }}</el-descriptions-item>
        <el-descriptions-item label="昵称">{{ userStore.userInfo.nickname || '-' }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ userStore.userInfo.email || '-' }}</el-descriptions-item>
        <el-descriptions-item label="手机">{{ userStore.userInfo.phone || '-' }}</el-descriptions-item>
      </el-descriptions>

      <div style="margin-top: 20px; text-align: center;">
        <el-button type="danger" @click="handleLogout">退出登录</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ElMessage } from 'element-plus'
import { useUserStore } from '../../stores/user'
import { logout } from '../../api/auth'

const userStore = useUserStore()

async function handleLogout() {
  await logout()
  userStore.logout()
  ElMessage.success('已退出登录')
  window.location.href = '/login'
}
</script>