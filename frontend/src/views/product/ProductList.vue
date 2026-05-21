<template>
  <div>
    <div style="margin-bottom: 20px;">
      <el-input v-model="query.productName" placeholder="搜索商品" style="width: 300px;" clearable @keyup.enter="loadData">
        <template #append>
          <el-button @click="loadData">
            <el-icon><Search /></el-icon>
          </el-button>
        </template>
      </el-input>
    </div>

    <el-row :gutter="20">
      <el-col :span="6" v-for="item in list" :key="item.id" style="margin-bottom: 20px;">
        <el-card :body-style="{ padding: '0px' }" shadow="hover" style="cursor: pointer;" @click="$router.push(`/product/${item.id}`)">
          <img :src="item.mainImage || 'https://via.placeholder.com/300x200?text=No+Image'" style="width: 100%; height: 200px; object-fit: cover;" />
          <div style="padding: 14px;">
            <div style="font-weight: bold; margin-bottom: 8px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">{{ item.productName }}</div>
            <div style="color: #999; font-size: 12px; margin-bottom: 8px;">{{ item.description || '暂无描述' }}</div>
            <el-tag size="small" :type="item.status === 1 ? 'success' : 'info'">{{ item.status === 1 ? '上架' : '下架' }}</el-tag>
          </div>
        </el-card>
      </el-col>
    </el-row>

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
import { getProductPage } from '../../api/product'

const list = ref([])
const total = ref(0)
const query = reactive({
  current: 1,
  size: 12,
  productName: ''
})

async function loadData() {
  const res = await getProductPage(query)
  list.value = res.records || []
  total.value = res.total || 0
}

onMounted(loadData)
</script>