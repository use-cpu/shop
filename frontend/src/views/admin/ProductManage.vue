<template>
  <div>
    <div class="toolbar">
      <el-input v-model="query.keyword" placeholder="搜索商品名" clearable style="width:240px" @keyup.enter="search" @clear="search" />
      <el-button :icon="Search" @click="search">搜索</el-button>
      <el-button type="primary" @click="openDialog()">新增商品</el-button>
    </div>

    <el-table :data="list" v-loading="loading" border>
      <el-table-column label="ID" prop="id" width="70" />
      <el-table-column label="图片" width="80">
        <template #default="{ row }">
          <el-image :src="row.mainImage" class="thumb" fit="cover" />
        </template>
      </el-table-column>
      <el-table-column label="商品名" prop="name" min-width="160" />
      <el-table-column label="分类" prop="categoryName" width="100" />
      <el-table-column label="价格" width="100">
        <template #default="{ row }">¥{{ row.price }}</template>
      </el-table-column>
      <el-table-column label="库存" prop="stock" width="80" />
      <el-table-column label="销量" prop="sales" width="80" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
            {{ row.status === 1 ? '上架' : '下架' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="260" fixed="right">
        <template #default="{ row }">
          <el-button link @click="openDialog(row)">编辑</el-button>
          <el-button link @click="openStockDialog(row)">改库存</el-button>
          <el-button link :type="row.status === 1 ? 'warning' : 'success'" @click="toggleStatus(row)">
            {{ row.status === 1 ? '下架' : '上架' }}
          </el-button>
          <el-button link type="danger" @click="remove(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pager">
      <el-pagination background layout="prev, pager, next" :total="total" :page-size="query.pageSize" v-model:current-page="query.pageNum" @current-change="loadData" />
    </div>

    <!-- 商品编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑商品' : '新增商品'" width="640px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="90px">
        <el-form-item label="分类" prop="categoryId">
          <el-cascader :options="categoryOptions" :props="{ checkStrictly: true, value: 'id', label: 'name', emitPath: false }" v-model="form.categoryId" placeholder="选择分类" />
        </el-form-item>
        <el-form-item label="商品名" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="副标题">
          <el-input v-model="form.subtitle" />
        </el-form-item>
        <el-form-item label="主图">
          <div class="upload-area">
            <el-upload
              class="main-image-uploader"
              action="/api/file/upload"
              :headers="uploadHeaders"
              :show-file-list="false"
              :on-success="onMainImageSuccess"
              :before-upload="beforeUpload"
              accept="image/*"
            >
              <img v-if="form.mainImage" :src="form.mainImage" class="main-image-preview" />
              <el-icon v-else class="upload-icon"><Plus /></el-icon>
            </el-upload>
            <el-input v-model="form.mainImage" placeholder="或手动输入URL" style="flex:1; margin-left:12px" />
          </div>
        </el-form-item>
        <el-form-item label="多图">
          <div class="upload-area">
            <el-upload
              action="/api/file/upload"
              :headers="uploadHeaders"
              list-type="picture-card"
              :file-list="imageList"
              :on-success="onImagesSuccess"
              :on-remove="onImagesRemove"
              :before-upload="beforeUpload"
              accept="image/*"
              multiple
            >
              <el-icon><Plus /></el-icon>
            </el-upload>
            <el-input v-model="form.images" type="textarea" :rows="2" placeholder="或手动输入, 多个URL用逗号分隔" style="flex:1; margin-left:12px" />
          </div>
        </el-form-item>
        <el-form-item label="售价" prop="price">
          <el-input-number v-model="form.price" :min="0" :precision="2" />
        </el-form-item>
        <el-form-item label="原价">
          <el-input-number v-model="form.originalPrice" :min="0" :precision="2" />
        </el-form-item>
        <el-form-item label="库存">
          <el-input-number v-model="form.stock" :min="0" />
        </el-form-item>
        <el-form-item label="商品详情">
          <el-input v-model="form.detail" type="textarea" :rows="4" />
        </el-form-item>
        <el-form-item label="上架">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>

    <!-- 库存维护弹窗 -->
    <el-dialog v-model="stockVisible" title="库存维护" width="360px">
      <p>商品: {{ stockForm.productName || '-' }}</p>
      <el-form label-width="80px">
        <el-form-item label="库存数量">
          <el-input-number v-model="stockForm.stock" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="stockVisible = false">取消</el-button>
        <el-button type="primary" @click="saveStock">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { Search, Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import productApi from '../../api/product'
import categoryApi from '../../api/category'
import { useUserStore } from '../../stores/user'

const userStore = useUserStore()
const uploadHeaders = computed(() => ({
  Authorization: userStore.token ? 'Bearer ' + userStore.token : ''
}))
const imageList = ref([])

const loading = ref(false)
const list = ref([])
const total = ref(0)
const categoryOptions = ref([])
const query = reactive({ keyword: '', pageNum: 1, pageSize: 10 })

const dialogVisible = ref(false)
const saving = ref(false)
const formRef = ref()
const form = reactive({ id: null, categoryId: null, name: '', subtitle: '', mainImage: '', images: '', detail: '', price: 0, originalPrice: null, stock: 0, status: 1 })
const rules = {
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  name: [{ required: true, message: '请输入商品名', trigger: 'blur' }],
  price: [{ required: true, message: '请输入售价', trigger: 'blur' }]
}

const stockVisible = ref(false)
const stockForm = reactive({ productId: null, stock: 0, productName: '' })

async function loadData() {
  loading.value = true
  try {
    const res = await productApi.adminList(query)
    list.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

async function loadCategories() {
  const res = await categoryApi.tree()
  categoryOptions.value = res.data
}

function search() {
  query.pageNum = 1
  loadData()
}

function openDialog(row) {
  if (row) {
    Object.assign(form, row, { images: row.images?.join(',') || '' })
    // 从 images 字符串恢复多图列表
    const urls = form.images ? form.images.split(',').filter(Boolean) : []
    imageList.value = urls.map((url, i) => ({ name: i + '', url }))
  } else {
    Object.assign(form, { id: null, categoryId: null, name: '', subtitle: '', mainImage: '', images: '', detail: '', price: 0, originalPrice: null, stock: 0, status: 1 })
    imageList.value = []
  }
  dialogVisible.value = true
}

// ---------- 图片上传 ----------
function beforeUpload(file) {
  const isImage = ['image/jpeg', 'image/png', 'image/gif', 'image/webp'].includes(file.type)
  if (!isImage) {
    ElMessage.error('只能上传图片文件(jpg/png/gif/webp)')
    return false
  }
  const isLt10M = file.size / 1024 / 1024 < 10
  if (!isLt10M) {
    ElMessage.error('图片大小不能超过 10MB')
    return false
  }
  return true
}

function onMainImageSuccess(res) {
  if (res.code === 0) {
    form.mainImage = res.data
    ElMessage.success('主图上传成功')
  } else {
    ElMessage.error(res.message || '上传失败')
  }
}

function onImagesSuccess(res) {
  if (res.code === 0) {
    // 追加到 images 字段(逗号分隔)
    if (form.images) form.images += ',' + res.data
    else form.images = res.data
    ElMessage.success('图片上传成功')
  } else {
    ElMessage.error(res.message || '上传失败')
  }
}

function onImagesRemove(file) {
  // 从 images 字段中移除被删的 URL
  const removedUrl = file.url || file.response?.data
  if (!removedUrl) return
  const urls = form.images ? form.images.split(',').filter(url => url !== removedUrl) : []
  form.images = urls.join(',')
}

async function save() {
  await formRef.value.validate()
  saving.value = true
  try {
    if (form.id) await productApi.update(form)
    else await productApi.add(form)
    ElMessage.success('保存成功')
    dialogVisible.value = false
    loadData()
  } finally {
    saving.value = false
  }
}

function openStockDialog(row) {
  Object.assign(stockForm, { productId: row.id, stock: row.stock, productName: row.name })
  stockVisible.value = true
}

async function saveStock() {
  await productApi.updateStock({ productId: stockForm.productId, stock: stockForm.stock })
  ElMessage.success('库存已更新')
  stockVisible.value = false
  loadData()
}

async function toggleStatus(row) {
  const newStatus = row.status === 1 ? 0 : 1
  await productApi.toggleStatus(row.id, newStatus)
  ElMessage.success(newStatus === 1 ? '已上架' : '已下架')
  loadData()
}

async function remove(id) {
  await ElMessageBox.confirm('确定删除该商品吗?', '提示', { type: 'warning' })
  await productApi.remove(id)
  ElMessage.success('已删除')
  loadData()
}

onMounted(() => {
  loadCategories()
  loadData()
})
</script>

<style scoped>
.toolbar { display: flex; gap: 12px; margin-bottom: 16px; }
.thumb { width: 40px; height: 40px; border-radius: 4px; }
.pager { margin-top: 16px; display: flex; justify-content: center; }
.upload-area { display: flex; align-items: flex-start; }
.main-image-uploader :deep(.el-upload) {
  width: 120px; height: 120px; border: 1px dashed #d9d9d9; border-radius: 6px;
  display: flex; align-items: center; justify-content: center; overflow: hidden;
}
.main-image-uploader :deep(.el-upload:hover) { border-color: #409eff; }
.main-image-preview { width: 120px; height: 120px; object-fit: cover; }
.upload-icon { font-size: 28px; color: #8c939d; }
</style>
