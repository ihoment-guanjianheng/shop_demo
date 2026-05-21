<template>
  <div>
    <h2 style="margin-bottom: 20px;">我的订单</h2>

    <el-empty v-if="list.length === 0 && !loading" description="暂无订单" />

    <el-card v-for="order in list" :key="order.id" style="margin-bottom: 16px;" shadow="hover">
      <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px;">
        <div>
          <span style="font-weight: bold; margin-right: 16px;">订单号：{{ order.orderNo }}</span>
          <el-tag :type="statusType(order.status)">{{ statusText(order.status) }}</el-tag>
        </div>
        <div style="color: #999; font-size: 12px;">{{ order.createTime }}</div>
      </div>

      <div style="display: flex; justify-content: space-between; align-items: center;">
        <div style="font-size: 18px; color: #f56c6c; font-weight: bold;">
          ¥{{ order.payAmount }}
        </div>
        <div style="display: flex; gap: 8px;">
          <el-button text @click="$router.push(`/orders/${order.id}`)">查看详情</el-button>
          <el-button v-if="order.status === 0" type="primary" size="small" @click="handlePay(order.id)">立即支付</el-button>
          <el-button v-if="order.status === 0" type="danger" text size="small" @click="handleCancel(order.id)">取消订单</el-button>
          <el-button v-if="order.status === 2" type="success" size="small" @click="handleReceive(order.id)">确认收货</el-button>
        </div>
      </div>
    </el-card>

    <el-pagination
      v-model:current-page="query.current"
      v-model:page-size="query.size"
      :total="total"
      layout="prev, pager, next"
      style="justify-content: center; margin-top: 20px;"
      @current-change="loadData"
    />
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMyOrderPage, payOrder, cancelOrder, receiveOrder } from '../../api/order'

const list = ref([])
const total = ref(0)
const loading = ref(false)
const query = reactive({
  current: 1,
  size: 10
})

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
    const res = await getMyOrderPage(query)
    list.value = res.records || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

async function handlePay(id) {
  await payOrder(id)
  ElMessage.success('支付成功')
  loadData()
}

async function handleCancel(id) {
  await ElMessageBox.confirm('确定要取消该订单吗？', '提示', { type: 'warning' })
  await cancelOrder(id, '用户取消')
  ElMessage.success('订单已取消')
  loadData()
}

async function handleReceive(id) {
  await ElMessageBox.confirm('确认已收到商品？', '提示', { type: 'info' })
  await receiveOrder(id)
  ElMessage.success('确认收货成功')
  loadData()
}

onMounted(loadData)
</script>