<template>
  <div>
    <el-page-header @back="$router.back()" title="新增商品" />

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
          <el-button type="primary" :loading="loading" @click="handleSubmit">提交</el-button>
          <el-button @click="$router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { addProduct, getImagePresign, uploadToPresign } from '../../api/product'

const router = useRouter()
const formRef = ref()
const loading = ref(false)
const uploading = ref(false)

const form = reactive({
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

function beforeUpload(file) {
  const isImage = file.type.startsWith('image/')
  if (!isImage) {
    ElMessage.error('请上传图片文件')
    return false
  }
  return true
}

async function handleUpload({ file }) {
  uploading.value = true
  try {
    const presign = await getImagePresign(file.name)
    await uploadToPresign(presign.uploadUrl, file, presign.contentType)
    form.mainImage = presign.accessUrl
    ElMessage.success('图片上传成功')
  } catch (e) {
    ElMessage.error('图片上传失败')
  } finally {
    uploading.value = false
  }
}

async function handleSubmit() {
  await formRef.value.validate()
  loading.value = true
  try {
    await addProduct(form)
    ElMessage.success('商品创建成功')
    router.push('/')
  } finally {
    loading.value = false
  }
}
</script>