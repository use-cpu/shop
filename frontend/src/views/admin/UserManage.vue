<template>
  <div>
    <div class="toolbar">
      <el-input v-model="query.keyword" placeholder="搜索用户名/昵称/手机号" clearable style="width:300px" @keyup.enter="search" @clear="search" />
      <el-button :icon="Search" @click="search">搜索</el-button>
    </div>

    <el-table :data="list" v-loading="loading" border>
      <el-table-column label="ID" prop="id" width="70" />
      <el-table-column label="用户名" prop="username" width="140" />
      <el-table-column label="昵称" prop="nickname" width="140" />
      <el-table-column label="手机号" prop="phone" width="140" />
      <el-table-column label="邮箱" prop="email" min-width="180" />
      <el-table-column label="角色" width="100">
        <template #default="{ row }">
          <el-tag :type="row.role === 1 ? 'danger' : 'primary'" size="small">
            {{ row.role === 1 ? '管理员' : '用户' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
            {{ row.status === 1 ? '正常' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="注册时间" prop="createTime" width="170" />
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.role !== 1" link :type="row.status === 1 ? 'warning' : 'success'" @click="toggleStatus(row)">
            {{ row.status === 1 ? '禁用' : '启用' }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pager">
      <el-pagination background layout="prev, pager, next" :total="total" :page-size="query.pageSize" v-model:current-page="query.pageNum" @current-change="loadData" />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import adminApi from '../../api/admin'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ keyword: '', pageNum: 1, pageSize: 10 })

async function loadData() {
  loading.value = true
  try {
    const res = await adminApi.userList(query)
    list.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

function search() {
  query.pageNum = 1
  loadData()
}

async function toggleStatus(row) {
  const newStatus = row.status === 1 ? 0 : 1
  await ElMessageBox.confirm(`确定${newStatus === 1 ? '启用' : '禁用'}该用户吗?`, '提示', { type: 'warning' })
  await adminApi.updateStatus({ userId: row.id, status: newStatus })
  ElMessage.success('操作成功')
  loadData()
}

onMounted(loadData)
</script>

<style scoped>
.toolbar { margin-bottom: 16px; display: flex; gap: 12px; }
.pager { margin-top: 16px; display: flex; justify-content: center; }
</style>
