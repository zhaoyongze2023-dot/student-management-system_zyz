<template>
  <div class="course-list-page">
    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="关键词">
          <el-input
            v-model="searchForm.keyword"
            placeholder="请输入课程名称/代码"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="开放选课" value="open" />
            <el-option label="已关闭" value="closed" />
            <el-option label="已满员" value="full" />
            <el-option label="已归档" value="archived" />
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
    
    <!-- 课程列表 -->
    <el-card class="table-card">
      <template #header>
        <div class="card-header">
          <span>课程列表</span>
          <div class="header-actions">
            <el-button
              type="primary"
              @click="handleCreate"
              v-if="userStore.isAdmin || userStore.isTeacher"
            >
              <el-icon><Plus /></el-icon>创建课程
            </el-button>
          </div>
        </div>
      </template>
      
      <el-table v-loading="loading" :data="tableData" stripe border>
        <el-table-column prop="code" label="课程代码" width="120" />
        <el-table-column prop="name" label="课程名称" min-width="150" />
        <el-table-column prop="teacherName" label="授课教师" width="100" />
        <el-table-column prop="credits" label="学分" width="80" align="center" />
        <el-table-column label="容量" width="120" align="center">
          <template #default="{ row }">
            <span :class="{ 'text-danger': row.enrolled >= row.capacity }">
              {{ row.enrolled || 0 }} / {{ row.capacity }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="location" label="上课地点" width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="COURSE_STATUS_MAP[row.status]?.color as any">
              {{ COURSE_STATUS_MAP[row.status]?.label || row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleDetail(row)">
              详情
            </el-button>
            <el-button
              type="primary"
              link
              @click="handleEdit(row)"
              v-if="userStore.isAdmin || userStore.isTeacher"
            >
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
      :title="isEdit ? '编辑课程' : '创建课程'"
      size="600px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item label="课程名称" prop="name">
          <el-input v-model="formData.name" />
        </el-form-item>
        <el-form-item label="课程代码" prop="code">
          <el-input v-model="formData.code" />
        </el-form-item>
        <el-form-item label="课程描述" prop="description">
          <el-input v-model="formData.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="课程分类" prop="category">
          <el-input v-model="formData.category" placeholder="如：专业必修、公共选修" />
        </el-form-item>
        <el-form-item label="学分" prop="credits">
          <el-input-number v-model="formData.credits" :min="1" :max="10" />
        </el-form-item>
        <el-form-item label="课程容量" prop="capacity">
          <el-input-number v-model="formData.capacity" :min="1" :max="500" />
        </el-form-item>
        <el-form-item label="上课地点" prop="location">
          <el-input v-model="formData.location" />
        </el-form-item>
        <el-form-item label="开课日期" prop="startDate">
          <el-date-picker
            v-model="formData.startDate"
            type="date"
            placeholder="选择开课日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="结课日期" prop="endDate">
          <el-date-picker
            v-model="formData.endDate"
            type="date"
            placeholder="选择结课日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="课程大纲" prop="syllabus">
          <el-input v-model="formData.syllabus" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="先修要求" prop="requirements">
          <el-input v-model="formData.requirements" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="formData.status" style="width: 100%">
            <el-option label="开放选课" value="open" />
            <el-option label="已关闭" value="closed" />
            <el-option label="已归档" value="archived" />
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
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { getCourseList, createCourse, updateCourse, deleteCourse } from '@/api/course'
import { useUserStore } from '@/store/user'
import { COURSE_STATUS_MAP, type Course, type CourseFormData } from '@/types'

const router = useRouter()
const userStore = useUserStore()

// 搜索表单
const searchForm = reactive({
  keyword: '',
  status: ''
})

// 表格数据
const loading = ref(false)
const tableData = ref<Course[]>([])

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
const formData = reactive<CourseFormData>({
  name: '',
  code: '',
  description: '',
  category: '',
  capacity: 50,
  status: 'open',
  startDate: '',
  endDate: '',
  credits: 2,
  location: '',
  syllabus: '',
  requirements: ''
})

// 表单验证规则
const formRules: FormRules = {
  name: [
    { required: true, message: '请输入课程名称', trigger: 'blur' }
  ],
  capacity: [
    { required: true, message: '请输入课程容量', trigger: 'blur' }
  ]
}

// 获取课程列表
const fetchData = async () => {
  loading.value = true
  try {
    const res = await getCourseList({
      current: pagination.current,
      size: pagination.size,
      keyword: searchForm.keyword || undefined,
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

// 查看详情
const handleDetail = (row: Course) => {
  router.push(`/course/detail/${row.id}`)
}

// 新增
const handleCreate = () => {
  isEdit.value = false
  editingId.value = undefined
  resetForm()
  drawerVisible.value = true
}

// 编辑
const handleEdit = (row: Course) => {
  isEdit.value = true
  editingId.value = row.id
  Object.assign(formData, {
    name: row.name,
    code: row.code || '',
    description: row.description || '',
    category: row.category || '',
    capacity: row.capacity,
    status: row.status,
    startDate: row.startDate || '',
    endDate: row.endDate || '',
    credits: row.credits,
    location: row.location || '',
    syllabus: row.syllabus || '',
    requirements: row.requirements || ''
  })
  drawerVisible.value = true
}

// 删除
const handleDelete = async (row: Course) => {
  try {
    await ElMessageBox.confirm(`确定要删除课程"${row.name}"吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteCourse(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch (e) {
    // 取消或错误
  }
}

// 重置表单
const resetForm = () => {
  formData.name = ''
  formData.code = ''
  formData.description = ''
  formData.category = ''
  formData.capacity = 50
  formData.status = 'open'
  formData.startDate = ''
  formData.endDate = ''
  formData.credits = 2
  formData.location = ''
  formData.syllabus = ''
  formData.requirements = ''
  formRef.value?.resetFields()
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitLoading.value = true
    try {
      if (isEdit.value && editingId.value) {
        await updateCourse(editingId.value, formData)
        ElMessage.success('更新成功')
      } else {
        await createCourse(formData)
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
  fetchData()
})
</script>

<style lang="scss" scoped>
.course-list-page {
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
  
  .text-danger {
    color: #f56c6c;
    font-weight: bold;
  }
}
</style>
