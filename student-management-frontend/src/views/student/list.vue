<template>
  <div class="student-list-page">
    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="关键词">
          <el-input
            v-model="searchForm.keyword"
            placeholder="请输入姓名/学号"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="班级">
          <el-select v-model="searchForm.classId" placeholder="请选择班级" clearable>
            <el-option
              v-for="item in classList"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="正常" value="active" />
            <el-option label="休学" value="inactive" />
            <el-option label="毕业" value="graduated" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <!-- 操作栏 -->
    <el-card class="table-card">
      <template #header>
        <div class="card-header">
          <span>学生列表</span>
          <div class="header-actions">
            <el-button type="primary" @click="handleCreate" v-if="userStore.isAdmin">
              <el-icon><Plus /></el-icon>新增学生
            </el-button>
            <el-button
              type="danger"
              :disabled="selectedIds.length === 0"
              @click="handleBatchDelete"
              v-if="userStore.isAdmin"
            >
              <el-icon><Delete /></el-icon>批量删除
            </el-button>
          </div>
        </div>
      </template>
      
      <!-- 表格 -->
      <el-table
        ref="tableRef"
        v-loading="loading"
        :data="tableData"
        @selection-change="handleSelectionChange"
        stripe
        border
      >
        <el-table-column type="selection" width="55" v-if="userStore.isAdmin" />
        <el-table-column label="头像" width="80">
          <template #default="{ row }">
            <el-avatar :size="40" :src="row.avatarUrl">
              {{ row.name?.charAt(0) }}
            </el-avatar>
          </template>
        </el-table-column>
        <el-table-column prop="studentId" label="学号" width="120" />
        <el-table-column prop="name" label="姓名" width="100" />
        <el-table-column prop="gender" label="性别" width="80">
          <template #default="{ row }">
            {{ GENDER_MAP[row.gender] || row.gender }}
          </template>
        </el-table-column>
        <el-table-column prop="age" label="年龄" width="80" />
        <el-table-column prop="className" label="班级" width="120" />
        <el-table-column prop="major" label="专业" min-width="120" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="email" label="邮箱" min-width="180" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="STUDENT_STATUS_MAP[row.status]?.color as any">
              {{ STUDENT_STATUS_MAP[row.status]?.label || row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button
              type="danger"
              link
              @click="handleDelete(row)"
              v-if="userStore.isAdmin"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
    
    <!-- 新增/编辑弹窗 -->
    <el-drawer
      v-model="drawerVisible"
      :title="isEdit ? '编辑学生' : '新增学生'"
      size="500px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="80px"
      >
        <el-form-item label="学号" prop="studentId">
          <el-input v-model="formData.studentId" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="formData.name" />
        </el-form-item>
        <el-form-item label="班级" prop="classId">
          <el-select v-model="formData.classId" placeholder="请选择班级" style="width: 100%">
            <el-option
              v-for="item in classList"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="formData.gender">
            <el-radio value="M">男</el-radio>
            <el-radio value="F">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="年龄" prop="age">
          <el-input-number v-model="formData.age" :min="1" :max="100" />
        </el-form-item>
        <el-form-item label="专业" prop="major">
          <el-input v-model="formData.major" />
        </el-form-item>
        <el-form-item label="入学年份" prop="admissionYear">
          <el-date-picker
            v-model="formData.admissionYear"
            type="year"
            placeholder="选择入学年份"
            format="YYYY"
            value-format="YYYY"
          />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="formData.phone" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="formData.email" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="formData.status" style="width: 100%">
            <el-option label="正常" value="active" />
            <el-option label="休学" value="inactive" />
            <el-option label="毕业" value="graduated" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="drawerVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">
          确定
        </el-button>
      </template>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { getStudentList, createStudent, updateStudent, deleteStudent, batchDeleteStudents } from '@/api/student'
import { getClasses } from '@/api/dict'
import { useUserStore } from '@/store/user'
import { STUDENT_STATUS_MAP, GENDER_MAP, type Student, type StudentFormData, type ClassInfo } from '@/types'

const userStore = useUserStore()

// 搜索表单
const searchForm = reactive({
  keyword: '',
  classId: undefined as number | undefined,
  status: ''
})

// 班级列表
const classList = ref<ClassInfo[]>([])

// 表格数据
const loading = ref(false)
const tableData = ref<Student[]>([])
const selectedIds = ref<number[]>([])

// 分页
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

// 弹窗
const drawerVisible = ref(false)
const isEdit = ref(false)
const submitLoading = ref(false)
const formRef = ref<FormInstance>()
const editingId = ref<number>()

// 表单数据
const formData = reactive<StudentFormData>({
  studentId: '',
  name: '',
  classId: undefined,
  gender: 'M',
  age: undefined,
  phone: '',
  email: '',
  major: '',
  admissionYear: undefined,
  status: 'active'
})

// 表单验证规则
const formRules: FormRules = {
  studentId: [
    { required: true, message: '请输入学号', trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' }
  ],
  classId: [
    { required: true, message: '请选择班级', trigger: 'change' }
  ],
  gender: [
    { required: true, message: '请选择性别', trigger: 'change' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ]
}

// 获取班级列表
const fetchClasses = async () => {
  try {
    const res = await getClasses()
    classList.value = res.data || []
  } catch (e) {
    // 忽略错误
  }
}

// 获取学生列表
const fetchData = async () => {
  loading.value = true
  try {
    const res = await getStudentList({
      current: pagination.current,
      size: pagination.size,
      keyword: searchForm.keyword || undefined,
      classId: searchForm.classId,
      status: searchForm.status || undefined
    })
    tableData.value = res.data?.records || []
    pagination.total = res.data?.total || 0
  } catch (e) {
    // 错误已在拦截器中处理
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.current = 1
  fetchData()
}

// 重置
const handleReset = () => {
  searchForm.keyword = ''
  searchForm.classId = undefined
  searchForm.status = ''
  handleSearch()
}

// 分页变化
const handleSizeChange = () => {
  pagination.current = 1
  fetchData()
}

const handleCurrentChange = () => {
  fetchData()
}

// 多选
const handleSelectionChange = (selection: Student[]) => {
  selectedIds.value = selection.map(item => item.id)
}

// 新增
const handleCreate = () => {
  isEdit.value = false
  editingId.value = undefined
  resetForm()
  drawerVisible.value = true
}

// 编辑
const handleEdit = (row: Student) => {
  isEdit.value = true
  editingId.value = row.id
  Object.assign(formData, {
    studentId: row.studentId,
    name: row.name,
    classId: row.classId,
    gender: row.gender,
    age: row.age,
    phone: row.phone || '',
    email: row.email || '',
    major: row.major || '',
    admissionYear: row.admissionYear,
    status: row.status
  })
  drawerVisible.value = true
}

// 删除
const handleDelete = async (row: Student) => {
  try {
    await ElMessageBox.confirm(`确定要删除学生"${row.name}"吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteStudent(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch (e) {
    // 取消或错误
  }
}

// 批量删除
const handleBatchDelete = async () => {
  try {
    await ElMessageBox.confirm(`确定要删除选中的${selectedIds.value.length}个学生吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await batchDeleteStudents(selectedIds.value)
    ElMessage.success('批量删除成功')
    fetchData()
  } catch (e) {
    // 取消或错误
  }
}

// 重置表单
const resetForm = () => {
  formData.studentId = ''
  formData.name = ''
  formData.classId = undefined
  formData.gender = 'M'
  formData.age = undefined
  formData.phone = ''
  formData.email = ''
  formData.major = ''
  formData.admissionYear = undefined
  formData.status = 'active'
  formRef.value?.resetFields()
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitLoading.value = true
    try {
      const data = {
        ...formData,
        admissionYear: formData.admissionYear ? Number(formData.admissionYear) : undefined
      }
      
      if (isEdit.value && editingId.value) {
        await updateStudent(editingId.value, data)
        ElMessage.success('更新成功')
      } else {
        await createStudent(data)
        ElMessage.success('创建成功')
      }
      drawerVisible.value = false
      fetchData()
    } catch (e) {
      // 错误已在拦截器中处理
    } finally {
      submitLoading.value = false
    }
  })
}

onMounted(() => {
  fetchClasses()
  fetchData()
})
</script>

<style lang="scss" scoped>
.student-list-page {
  .search-card {
    margin-bottom: 20px;
    
    .search-form {
      .el-form-item {
        margin-bottom: 0;
      }
    }
  }
  
  .table-card {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
    
    .header-actions {
      display: flex;
      gap: 10px;
    }
  }
  
  .pagination-container {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }
}
</style>
