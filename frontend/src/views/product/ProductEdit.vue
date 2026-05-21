<template>
  <div>
    <el-page-header @back="$router.back()" title="编辑商品" />

    <el-card style="margin-top: 20px; max-width: 800px;">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="商品编码" prop="productCode">
          <el-input v-model="form.productCode" placeholder="请输入商品编码" />
        </el-form-item>
        <el-form-item label="商品名称" prop="productName">
          <el-input v-model="form.productName" placeholder="请输入商品名称" />
        </el-form-item>
        <el-form-item label="商品描述">
          <el-input v-model="form.description" type="textarea" rows="3" placeholder="请输入商品描述" />
        </el-form-item>
        <el-form-item label="商品主图">
          <el-upload
            accept="image/*"
            :show-file-list="false"
            :before-upload="beforeUpload"
            :http-request="handleUpload"
          >
            <img v-if="form.mainImage" :src="form.mainImage" style="width: 200px; height: 200px; object-fit: cover; border-radius: 8px;" />
            <el-icon v-else style="font-size: 28px; color: #8c939d;"><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        <el-form-item label="上架状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" active-text="上架" inactive-text="下架" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleSubmit">保存</el-button>
          <el-button @click="$router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getProductDetail, updateProduct, getImagePresign, uploadToPresign } from '../../api/product'

const route = useRoute()
const router = useRouter()
const formRef = ref()
const loading = ref(false)

const form = reactive({
  id: null,
  productCode: '',
  productName: '',
  description: '',
  mainImage: '',
  status: 1
})

const rules = {
  productCode: [{ required: true, message: '请输入商品编码', trigger: 'blur' }],
  productName: [{ required: true, message: '请输入商品名称', trigger: 'blur' }]
}

async function loadData() {
  const data = await getProductDetail(route.params.id)
  Object.assign(form, {
    id: data.id,
    productCode: data.productCode,
    productName: data.productName,
    description: data.description,
    mainImage: data.mainImage,
    status: data.status
  })
}

function beforeUpload(file) {
  if (!file.type.startsWith('image/')) {
    ElMessage.error('请上传图片文件')
    return false
  }
  return true
}

async function handleUpload({ file }) {
  try {
    const presign = await getImagePresign(file.name)
    await uploadToPresign(presign.uploadUrl, file, presign.contentType)
    form.mainImage = presign.accessUrl
    ElMessage.success('图片上传成功')
  } catch {
    ElMessage.error('图片上传失败')
  }
}

async function handleSubmit() {
  await formRef.value.validate()
  loading.value = true
  try {
    await updateProduct(form.id, {
      id: form.id,
      productCode: form.productCode,
      productName: form.productName,
      description: form.description,
      mainImage: form.mainImage,
      status: form.status
    })
    ElMessage.success('商品更新成功')
    router.push('/')
  } finally {
    loading.value = false
  }
}

onMounted(loadData)
</script>