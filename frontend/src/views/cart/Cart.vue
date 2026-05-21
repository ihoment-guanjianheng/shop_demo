<template>
  <div>
    <h2 style="margin-bottom: 20px;">购物车</h2>

    <el-empty v-if="cartStore.items.length === 0" description="购物车是空的" />

    <template v-else>
      <el-table :data="cartStore.items" style="margin-bottom: 20px;">
        <el-table-column label="商品" width="300">
          <template #default="{ row }">
            <div style="display: flex; align-items: center; gap: 12px;">
              <el-image :src="row.skuImage || 'https://via.placeholder.com/60?text=No+Img'" style="width: 60px; height: 60px; border-radius: 4px;" fit="cover" />
              <span>{{ row.skuName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="单价" width="120">
          <template #default="{ row }">¥{{ row.unitPrice }}</template>
        </el-table-column>
        <el-table-column label="数量" width="180">
          <template #default="{ row }">
            <el-input-number v-model="row.quantity" :min="1" :max="99" @change="cartStore.updateQuantity(row.skuId, row.quantity)" />
          </template>
        </el-table-column>
        <el-table-column label="小计" width="120">
          <template #default="{ row }">¥{{ (row.unitPrice * row.quantity).toFixed(2) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button type="danger" text @click="cartStore.removeItem(row.skuId)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div style="display: flex; justify-content: space-between; align-items: center; padding: 20px; background: #fff; border-radius: 8px;">
        <el-button text @click="cartStore.clear()">清空购物车</el-button>
        <div style="display: flex; align-items: center; gap: 20px;">
          <span>共 {{ cartStore.totalCount }} 件商品</span>
          <span style="font-size: 20px; color: #f56c6c; font-weight: bold;">合计：¥{{ cartStore.totalAmount.toFixed(2) }}</span>
          <el-button type="danger" size="large" @click="$router.push('/order/create')">去结算</el-button>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { useCartStore } from '../../stores/cart'

const cartStore = useCartStore()
</script>