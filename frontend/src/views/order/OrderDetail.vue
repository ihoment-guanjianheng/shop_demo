<template>
  <div v-loading="loading">
    <el-button text @click="$router.back()" style="margin-bottom: 16px;">
      <el-icon><ArrowLeft /></el-icon> 返回
    </el-button>

    <el-empty v-if="!order" description="订单不存在" />

    <template v-else>
      <el-card style="margin-bottom: 20px;">
        <template #header>
          <div style="display: flex; justify-content: space-between; align-items: center;">
            <span>订单信息</span>
            <el-tag :type="statusType(order.status)">{{ statusText(order.status) }}</el-tag>
          </div>
        </template>
        <el-descriptions :column="2">
          <el-descriptions-item label="订单号">{{ order.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="下单时间">{{ order.createTime }}</el-descriptions-item>
          <el-descriptions-item label="商品总额">¥{{ order.totalAmount }}</el-descriptions-item>
          <el-descriptions-item label="运费">¥{{ order.freightAmount }}</el-descriptions-item>
          <el-descriptions-item label="优惠">-¥{{ order.discountAmount }}</el-descriptions-item>
          <el-descriptions-item label="应付金额"><span style="color: #f56c6c; font-weight: bold;">¥{{ order.payAmount }}</span></el-descriptions-item>
          <el-descriptions-item label="备注">{{ order.remark || '无' }}</el-descriptions-item>
        </el-descriptions>
      </el-card>

      <el-card style="margin-bottom: 20px;">
        <template #header>
          <span>商品明细</span>
        </template>
        <el-table :data="order.items">
          <el-table-column label="商品">
            <template #default="{ row }">
              <div style="display: flex; align-items: center; gap: 12px;">
                <el-image :src="row.skuImage || 'https://via.placeholder.com/50?text=No+Img'" style="width: 50px; height: 50px; border-radius: 4px;" fit="cover" />
                <span>{{ row.skuName }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="单价" width="120">
            <template #default="{ row }">¥{{ row.unitPrice }}</template>
          </el-table-column>
          <el-table-column label="数量" width="120">
            <template #default="{ row }">{{ row.quantity }}</template>
          </el-table-column>
          <el-table-column label="小计" width="120">
            <template #default="{ row }">¥{{ row.totalAmount }}</template>
          </el-table-column>
        </el-table>
      </el-card>

      <div style="text-align: center;" v-if="order.status === 0">
        <el-button type="primary" size="large" @click="handlePay">立即支付</el-button>
        <el-button type="danger" text size="large" @click="handleCancel">取消订单</el-button>
      </div>
      <div style="text-align: center;" v-if="order.status === 1">
        <el-button type="warning" size="large" @click="handleDeliver">发货</el-button>
      </div>
      <div style="text-align: center;" v-if="order.status === 2">
        <el-button type="success" size="large" @click="handleReceive">确认收货</el-button>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getOrderDetail, payOrder, cancelOrder, deliverOrder, receiveOrder } from '../../api/order'

const route = useRoute()
const router = useRouter()

const order = ref(null)
const loading = ref(false)

const statusMap = {
  0: { text: '待付款', type: 'warning' },
  1: { text: '已付款', type: 'success' },
  2: { text: '已发货', type: 'primary' },
  3: { text: '已完成', type: 'info' },
  4: { text: '已取消', type: 'danger' }
}

function statusText(status) {
  return statusMap[status]?.text || '未知'
}

function statusType(status) {
  return statusMap[status]?.type || 'info'
}

async function loadData() {
  loading.value = true
  try {
    order.value = await getOrderDetail(route.params.id)
  } finally {
    loading.value = false
  }
}

async function handlePay() {
  await payOrder(order.value.id)
  ElMessage.success('支付成功')
  loadData()
}

async function handleCancel() {
  await ElMessageBox.confirm('确定要取消该订单吗？', '提示', { type: 'warning' })
  await cancelOrder(order.value.id, '用户取消')
  ElMessage.success('订单已取消')
  router.push('/orders')
}

async function handleDeliver() {
  await ElMessageBox.confirm('确认发货？', '提示', { type: 'info' })
  await deliverOrder(order.value.id)
  ElMessage.success('发货成功')
  loadData()
}

async function handleReceive() {
  await ElMessageBox.confirm('确认已收到商品？', '提示', { type: 'info' })
  await receiveOrder(order.value.id)
  ElMessage.success('确认收货成功')
  loadData()
}

onMounted(loadData)
</script>