<template>
  <div>
    <h2 style="margin-bottom: 20px;">商品管理</h2>

    <div style="margin-bottom: 20px; display: flex; justify-content: space-between; align-items: center;">
      <el-input v-model="query.productName" placeholder="搜索商品" style="width: 300px;" clearable @keyup.enter="loadData">
        <template #append>
          <el-button @click="loadData">
            <el-icon><Search /></el-icon>
          </el-button>
        </template>
      </el-input>
      <div style="display: flex; gap: 8px;">
        <el-button type="primary" @click="$router.push('/product/add')">
          <el-icon><Plus /></el-icon> 新增商品
        </el-button>
        <el-button @click="handleExport">
          <el-icon><Download /></el-icon> 导出
        </el-button>
      </div>
    </div>

    <el-table :data="list" border v-loading="loading">
      <el-table-column label="商品图" width="100">
        <template #default="{ row }">
          <el-image :src="row.mainImage || 'https://via.placeholder.com/60?text=No+Img'" style="width: 60px; height: 60px; border-radius: 4px;" fit="cover" />
        </template>
      </el-table-column>
      <el-table-column prop="productCode" label="商品编码" />
      <el-table-column prop="productName" label="商品名称" />
      <el-table-column prop="description" label="描述" show-overflow-tooltip />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
            {{ row.status === 1 ? '上架' : '下架' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button text size="small" @click="$router.push(`/product/${row.id}`)">查看</el-button>
          <el-button text size="small" @click="$router.push(`/product/${row.id}/edit`)">编辑</el-button>
          <el-switch v-model="row.status" :active-value="1" :inactive-value="0" size="small" @change="handleStatusChange(row)" />
        </template>
      </el-table-column>
    </el-table>

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
import { ElMessage } from 'element-plus'
import { getProductPage, updateProductStatus, exportProduct } from '../../api/product'

const list = ref([])
const total = ref(0)
const loading = ref(false)
const query = reactive({
  current: 1,
  size: 10,
  productName: ''
})

async function loadData() {
  loading.value = true
  try {
    const res = await getProductPage(query)
    list.value = res.records || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

async function handleStatusChange(item) {
  try {
    await updateProductStatus(item.id, item.status)
    ElMessage.success('状态更新成功')
  } catch {
    item.status = item.status === 1 ? 0 : 1
  }
}

async function handleExport() {
  try {
    const url = await exportProduct()
    window.open(url, '_blank')
    ElMessage.success('导出成功')
  } catch {
    ElMessage.error('导出失败')
  }
}

onMounted(loadData)
</script>