<template>
  <div v-loading="loading">
    <el-button text @click="$router.back()" style="margin-bottom: 16px;">
      <el-icon><ArrowLeft /></el-icon> 返回
    </el-button>

    <el-row :gutter="40" v-if="product">
      <el-col :span="10">
        <el-image :src="product.mainImage || 'https://via.placeholder.com/400x400?text=No+Image'" style="width: 100%; border-radius: 8px;" fit="cover" />
      </el-col>
      <el-col :span="14">
        <h1 style="margin-bottom: 16px;">{{ product.productName }}</h1>
        <p style="color: #666; margin-bottom: 20px;">{{ product.description }}</p>

        <div style="margin-bottom: 20px;">
          <div style="margin-bottom: 12px; font-weight: bold;">选择规格：</div>
          <el-radio-group v-model="selectedSkuId">
            <el-radio-button v-for="sku in product.skuList" :key="sku.id" :label="sku.id">
              {{ sku.skuName }} - ¥{{ sku.price }}
            </el-radio-button>
          </el-radio-group>
        </div>

        <div style="margin-bottom: 20px;">
          <div style="margin-bottom: 12px; font-weight: bold;">数量：</div>
          <el-input-number v-model="quantity" :min="1" :max="99" />
        </div>

        <div style="font-size: 24px; color: #f56c6c; font-weight: bold; margin-bottom: 20px;" v-if="selectedSku">
          ¥{{ selectedSku.price * quantity }}
        </div>

        <el-button type="danger" size="large" @click="addToCart" :disabled="!selectedSku">
          <el-icon><ShoppingCart /></el-icon> 加入购物车
        </el-button>
        <el-button type="primary" size="large" @click="buyNow" :disabled="!selectedSku">
          立即购买
        </el-button>
      </el-col>
    </el-row>

    <el-card style="margin-top: 24px;" v-if="product">
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <span>SKU 列表</span>
          <el-button type="primary" size="small" @click="openSkuDialog()">
            <el-icon><Plus /></el-icon> 新增 SKU
          </el-button>
        </div>
      </template>
      <el-table :data="product.skuList" border>
        <el-table-column label="SKU图" width="80">
          <template #default="{ row }">
            <el-image :src="row.image || 'https://via.placeholder.com/50?text=No+Img'" style="width: 50px; height: 50px; border-radius: 4px;" fit="cover" />
          </template>
        </el-table-column>
        <el-table-column prop="skuCode" label="SKU编码" />
        <el-table-column prop="skuName" label="SKU名称" />
        <el-table-column prop="skuSpecs" label="规格" />
        <el-table-column prop="price" label="价格" width="100">
          <template #default="{ row }">¥{{ row.price }}</template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" width="80" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '上架' : '下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button text size="small" @click="openSkuDialog(row)">编辑</el-button>
            <el-button text size="small" @click="openStockDialog(row)">调库存</el-button>
          <el-button type="danger" text size="small" @click="handleSkuDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="skuDialogVisible" :title="isEditSku ? '编辑 SKU' : '新增 SKU'" width="500px">
      <el-form :model="skuForm" :rules="skuRules" ref="skuFormRef" label-width="100px">
        <el-form-item label="SKU编码" prop="skuCode">
          <el-input v-model="skuForm.skuCode" placeholder="请输入SKU编码" />
        </el-form-item>
        <el-form-item label="SKU名称" prop="skuName">
          <el-input v-model="skuForm.skuName" placeholder="请输入SKU名称" />
        </el-form-item>
        <el-form-item label="规格">
          <el-input v-model="skuForm.skuSpecs" placeholder="如：颜色-红色；尺寸-L" />
        </el-form-item>
        <el-form-item label="价格" prop="price">
          <el-input-number v-model="skuForm.price" :min="0" :precision="2" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="库存" prop="stock">
          <el-input-number v-model="skuForm.stock" :min="0" :precision="0" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="SKU图片">
          <el-upload
            accept="image/*"
            :show-file-list="false"
            :before-upload="beforeSkuUpload"
            :http-request="handleSkuUpload"
          >
            <img v-if="skuForm.image" :src="skuForm.image" style="width: 120px; height: 120px; object-fit: cover; border-radius: 8px;" />
            <el-icon v-else style="font-size: 28px; color: #8c939d;"><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        <el-form-item label="上架状态">
          <el-switch v-model="skuForm.status" :active-value="1" :inactive-value="0" active-text="上架" inactive-text="下架" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="skuDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="skuLoading" @click="handleSkuSubmit">提交</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="stockDialogVisible" title="调整库存" width="400px">
      <el-form label-width="80px">
        <el-form-item label="当前库存">
          <span>{{ currentSku?.stock }}</span>
        </el-form-item>
        <el-form-item label="调整量">
          <el-input-number v-model="stockDelta" :precision="0" style="width: 100%;" />
          <div style="color: #999; font-size: 12px; margin-top: 4px;">正数增加，负数减少</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="stockDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="stockLoading" @click="handleStockSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getProductDetail, addSku, updateSku, deleteSku, updateSkuStock, getImagePresign, uploadToPresign } from '../../api/product'
import { useCartStore } from '../../stores/cart'

const route = useRoute()
const router = useRouter()
const cartStore = useCartStore()

const product = ref(null)
const loading = ref(false)
const selectedSkuId = ref(null)
const quantity = ref(1)

const skuDialogVisible = ref(false)
const skuFormRef = ref()
const skuLoading = ref(false)
const isEditSku = ref(false)

const stockDialogVisible = ref(false)
const stockLoading = ref(false)
const currentSku = ref(null)
const stockDelta = ref(0)

const skuForm = reactive({
  id: null,
  productId: null,
  skuCode: '',
  skuName: '',
  skuSpecs: '',
  price: 0,
  stock: 0,
  image: '',
  status: 1
})

const skuRules = {
  skuCode: [{ required: true, message: '请输入SKU编码', trigger: 'blur' }],
  skuName: [{ required: true, message: '请输入SKU名称', trigger: 'blur' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }],
  stock: [{ required: true, message: '请输入库存', trigger: 'blur' }]
}

const selectedSku = computed(() => {
  return product.value?.skuList?.find(s => s.id === selectedSkuId.value)
})

async function loadData() {
  loading.value = true
  try {
    product.value = await getProductDetail(route.params.id)
  } finally {
    loading.value = false
  }
}

function addToCart() {
  cartStore.addItem({
    productId: product.value.id,
    skuId: selectedSku.value.id,
    skuName: selectedSku.value.skuName,
    skuImage: selectedSku.value.image || product.value.mainImage,
    unitPrice: selectedSku.value.price,
    quantity: quantity.value
  })
  ElMessage.success('已加入购物车')
}

function buyNow() {
  addToCart()
  router.push('/cart')
}

function openSkuDialog(row) {
  isEditSku.value = !!row
  if (row) {
    Object.assign(skuForm, {
      id: row.id,
      productId: product.value.id,
      skuCode: row.skuCode,
      skuName: row.skuName,
      skuSpecs: row.skuSpecs,
      price: row.price,
      stock: row.stock,
      image: row.image,
      status: row.status
    })
  } else {
    resetSkuForm()
    skuForm.productId = product.value.id
  }
  skuDialogVisible.value = true
}

function beforeSkuUpload(file) {
  if (!file.type.startsWith('image/')) {
    ElMessage.error('请上传图片文件')
    return false
  }
  return true
}

async function handleSkuUpload({ file }) {
  try {
    const presign = await getImagePresign(file.name)
    await uploadToPresign(presign.uploadUrl, file, presign.contentType)
    skuForm.image = presign.accessUrl
    ElMessage.success('图片上传成功')
  } catch {
    ElMessage.error('图片上传失败')
  }
}

async function handleSkuSubmit() {
  await skuFormRef.value.validate()
  skuLoading.value = true
  try {
    if (isEditSku.value) {
      await updateSku(skuForm.id, { ...skuForm })
      ElMessage.success('SKU 更新成功')
    } else {
      await addSku({ ...skuForm })
      ElMessage.success('SKU 创建成功')
    }
    skuDialogVisible.value = false
    resetSkuForm()
    loadData()
  } finally {
    skuLoading.value = false
  }
}

function resetSkuForm() {
  skuForm.id = null
  skuForm.productId = null
  skuForm.skuCode = ''
  skuForm.skuName = ''
  skuForm.skuSpecs = ''
  skuForm.price = 0
  skuForm.stock = 0
  skuForm.image = ''
  skuForm.status = 1
}

function openStockDialog(row) {
  currentSku.value = row
  stockDelta.value = 0
  stockDialogVisible.value = true
}

async function handleStockSubmit() {
  if (stockDelta.value === 0) {
    stockDialogVisible.value = false
    return
  }
  stockLoading.value = true
  try {
    await updateSkuStock(currentSku.value.id, stockDelta.value)
    ElMessage.success('库存调整成功')
    stockDialogVisible.value = false
    loadData()
  } finally {
    stockLoading.value = false
  }
}

async function handleSkuDelete(row) {
  await ElMessageBox.confirm('确定要删除该 SKU 吗？', '提示', { type: 'warning' })
  try {
    await deleteSku(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch {
    ElMessage.error('删除失败')
  }
}

onMounted(loadData)
</script>