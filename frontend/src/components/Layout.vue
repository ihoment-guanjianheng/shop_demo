<template>
  <el-container style="min-height: 100vh;">
    <el-header style="background: #fff; border-bottom: 1px solid #eee; display: flex; align-items: center; justify-content: space-between;">
      <div style="font-size: 20px; font-weight: bold; color: #409eff; cursor: pointer;" @click="$router.push('/')">
        Shop Demo
      </div>
      <div style="display: flex; align-items: center; gap: 16px;">
        <el-button text @click="$router.push('/cart')">
          <el-icon><ShoppingCart /></el-icon> 购物车
        </el-button>
        <el-button text @click="$router.push('/orders')">
          <el-icon><List /></el-icon> 我的订单
        </el-button>
        <el-button text @click="$router.push('/orders/manage')">
          <el-icon><Document /></el-icon> 订单管理
        </el-button>
        <el-button text @click="$router.push('/products/manage')">
          <el-icon><Goods /></el-icon> 商品管理
        </el-button>
        <template v-if="userStore.isLoggedIn">
          <el-dropdown>
            <span style="cursor: pointer; color: #409eff;">
              {{ userStore.userInfo.nickname || userStore.userInfo.username }}
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="$router.push('/profile')">个人中心</el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
        <template v-else>
          <el-button type="primary" size="small" @click="$router.push('/login')">登录</el-button>
        </template>
      </div>
    </el-header>
    <el-main>
      <router-view />
    </el-main>
  </el-container>
</template>

<script setup>
import { useUserStore } from '../stores/user'
import { logout } from '../api/auth'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()

async function handleLogout() {
  await logout()
  userStore.logout()
  ElMessage.success('已退出登录')
  window.location.href = '/login'
}
</script>