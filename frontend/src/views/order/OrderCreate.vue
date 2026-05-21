<template>
  <div>
    <h2 style="margin-bottom: 20px;">确认订单</h2>

    <el-card style="margin-bottom: 20px;">
      <template #header>
        <span>商品信息</span>
      </template>
      <el-table :data="cartStore.items">
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
          <template #default="{ row }">¥{{ (row.unitPrice * row.quantity).toFixed(2) }}</template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-card style="margin-bottom: 20px;">
      <template #header>
        <span>订单信息</span>
      </template>
      <el-form :model="form" label-width="100px">
        <el-form-item label="运费">
          <el-input-number v-model="form.freightAmount" :min="0" :precision="2" />
        </el-form-item>
        <el-form-item label="优惠金额">
          <el-input-number v-model="form.discountAmount" :min="0" :precision="2" disabled />
        </el-form-item>
        <el-form-item label="支付方式">
          <el-radio-group v-model="form.payType">
            <el-radio :label="1">在线支付</el-radio>
            <el-radio :label="2">货到付款</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" rows="2" placeholder="请输入订单备注" />
        </el-form-item>
      </el-form>
    </el-card>

    <div style="display: flex; justify-content: flex-end; align-items: center; gap: 20px; padding: 20px; background: #fff; border-radius: 8px;">
      <div style="text-align: right;">
        <div>商品总额：¥{{ cartStore.totalAmount.toFixed(2) }}</div>
        <div>运费：¥{{ form.freightAmount.toFixed(2) }}</div>
        <div>优惠：-¥{{ form.discountAmount.toFixed(2) }}</div>
        <div style="font-size: 20px; color: #f56c6c; font-weight: bold; margin-top: 8px;">
          应付总额：¥{{ payAmount.toFixed(2) }}
        </div>
      </div>
      <el-button type="danger" size="large" :loading="loading" @click="submitOrder">提交订单</el-button>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useCartStore } from '../../stores/cart'
import { createOrder } from '../../api/order'

const router = useRouter()
const cartStore = useCartStore()
const loading = ref(false)

const form = reactive({
  freightAmount: 10,
  discountAmount: 0,
  payType: 1,
  remark: ''
})

const payAmount = computed(() => {
  const amount = cartStore.totalAmount + form.freightAmount - form.discountAmount
  return amount > 0 ? amount : 0
})

async function submitOrder() {
  if (cartStore.items.length === 0) {
    ElMessage.warning('购物车为空')
    return
  }
  loading.value = true
  try {
    const data = {
      freightAmount: form.freightAmount,
      discountAmount: form.discountAmount,
      payType: form.payType,
      remark: form.remark,
      items: cartStore.items.map(item => ({
        productId: item.productId,
        skuId: item.skuId,
        skuName: item.skuName,
        skuImage: item.skuImage,
        unitPrice: item.unitPrice,
        quantity: item.quantity
      }))
    }
    const orderId = await createOrder(data)
    ElMessage.success('订单提交成功')
    cartStore.clear()
    router.push(`/orders/${orderId}`)
  } finally {
    loading.value = false
  }
}
</script>