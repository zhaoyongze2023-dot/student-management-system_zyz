<template>
  <div class="course-detail-page" v-loading="loading">
    <el-page-header @back="router.back()">
      <template #content>
        <span class="page-title">{{ course?.name }}</span>
        <el-tag :type="COURSE_STATUS_MAP[course?.status || '']?.color as any" class="status-tag">
          {{ COURSE_STATUS_MAP[course?.status || '']?.label || course?.status }}
        </el-tag>
      </template>
    </el-page-header>
    
    <div class="course-content" v-if="course">
      <el-tabs v-model="activeTab" class="course-tabs">
        <!-- 基本信息 -->
        <el-tab-pane label="基本信息" name="info">
          <el-card>
            <el-descriptions :column="2" border>
              <el-descriptions-item label="课程代码">{{ course.code || '-' }}</el-descriptions-item>
              <el-descriptions-item label="课程名称">{{ course.name }}</el-descriptions-item>
              <el-descriptions-item label="授课教师">{{ course.teacherName || '-' }}</el-descriptions-item>
              <el-descriptions-item label="学分">{{ course.credits || '-' }}</el-descriptions-item>
              <el-descriptions-item label="课程容量">
                <span :class="{ 'text-danger': (course.enrolled || 0) >= course.capacity }">
                  {{ course.enrolled || 0 }} / {{ course.capacity }}
                </span>
              </el-descriptions-item>
              <el-descriptions-item label="上课地点">{{ course.location || '-' }}</el-descriptions-item>
              <el-descriptions-item label="开课日期">{{ course.startDate || '-' }}</el-descriptions-item>
              <el-descriptions-item label="结课日期">{{ course.endDate || '-' }}</el-descriptions-item>
              <el-descriptions-item label="课程分类">{{ course.category || '-' }}</el-descriptions-item>
              <el-descriptions-item label="状态">
                <el-tag :type="COURSE_STATUS_MAP[course.status]?.color as any">
                  {{ COURSE_STATUS_MAP[course.status]?.label || course.status }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="课程描述" :span="2">
                {{ course.description || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="课程大纲" :span="2">
                <pre class="pre-text">{{ course.syllabus || '-' }}</pre>
              </el-descriptions-item>
              <el-descriptions-item label="先修要求" :span="2">
                {{ course.requirements || '-' }}
              </el-descriptions-item>
            </el-descriptions>
          </el-card>
        </el-tab-pane>
        
        <!-- 课程日程 -->
        <el-tab-pane label="课程日程" name="schedule">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>上课时间安排</span>
                <el-button
                  type="primary"
                  size="small"
                  @click="showScheduleDialog = true"
                  v-if="userStore.isAdmin || userStore.isTeacher"
                >
                  <el-icon><Plus /></el-icon>添加日程
                </el-button>
              </div>
            </template>
            
            <el-table :data="course.schedules || []" stripe border>
              <el-table-column label="星期" width="100">
                <template #default="{ row }">
                  {{ DAY_OF_WEEK_MAP[row.dayOfWeek] }}
                </template>
              </el-table-column>
              <el-table-column prop="startTime" label="开始时间" width="120" />
              <el-table-column prop="endTime" label="结束时间" width="120" />
              <el-table-column prop="location" label="上课地点" />
              <el-table-column label="操作" width="100" v-if="userStore.isAdmin || userStore.isTeacher">
                <template #default="{ row }">
                  <el-button type="danger" link @click="handleDeleteSchedule(row)">
                    删除
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
            
            <el-empty v-if="!course.schedules?.length" description="暂无课程日程" />
          </el-card>
        </el-tab-pane>
        
        <!-- 课件附件 -->
        <el-tab-pane label="课件附件" name="attachment">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>课程资料</span>
                <el-button
                  type="primary"
                  size="small"
                  @click="showAttachmentDialog = true"
                  v-if="userStore.isAdmin || userStore.isTeacher"
                >
                  <el-icon><Plus /></el-icon>添加附件
                </el-button>
              </div>
            </template>
            
            <el-table :data="course.attachments || []" stripe border>
              <el-table-column prop="fileName" label="文件名" />
              <el-table-column prop="fileType" label="类型" width="100" />
              <el-table-column label="大小" width="100">
                <template #default="{ row }">
                  {{ formatFileSize(row.fileSize) }}
                </template>
              </el-table-column>
              <el-table-column label="操作" width="100">
                <template #default="{ row }">
                  <el-button type="primary" link @click="downloadFile(row)">
                    下载
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
            
            <el-empty v-if="!course.attachments?.length" description="暂无课件附件" />
          </el-card>
        </el-tab-pane>
      </el-tabs>
    </div>
    
    <!-- 添加日程弹窗 -->
    <el-dialog v-model="showScheduleDialog" title="添加课程日程" width="400px">
      <el-form ref="scheduleFormRef" :model="scheduleForm" :rules="scheduleRules" label-width="80px">
        <el-form-item label="星期" prop="dayOfWeek">
          <el-select v-model="scheduleForm.dayOfWeek" style="width: 100%">
            <el-option
              v-for="(label, value) in DAY_OF_WEEK_MAP"
              :key="value"
              :label="label"
              :value="Number(value)"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="开始时间" prop="startTime">
          <el-time-picker
            v-model="scheduleForm.startTime"
            format="HH:mm"
            value-format="HH:mm"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="结束时间" prop="endTime">
          <el-time-picker
            v-model="scheduleForm.endTime"
            format="HH:mm"
            value-format="HH:mm"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="上课地点" prop="location">
          <el-input v-model="scheduleForm.location" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showScheduleDialog = false">取消</el-button>
        <el-button type="primary" @click="handleAddSchedule">确定</el-button>
      </template>
    </el-dialog>
    
    <!-- 添加附件弹窗 -->
    <el-dialog v-model="showAttachmentDialog" title="添加课件附件" width="400px">
      <el-form ref="attachmentFormRef" :model="attachmentForm" :rules="attachmentRules" label-width="80px">
        <el-form-item label="文件名" prop="fileName">
          <el-input v-model="attachmentForm.fileName" />
        </el-form-item>
        <el-form-item label="文件URL" prop="fileUrl">
          <el-input v-model="attachmentForm.fileUrl" />
        </el-form-item>
        <el-form-item label="文件类型" prop="fileType">
          <el-input v-model="attachmentForm.fileType" placeholder="如：pdf, doc, ppt" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAttachmentDialog = false">取消</el-button>
        <el-button type="primary" @click="handleAddAttachment">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { getCourse, addCourseSchedule, deleteCourseSchedule, addCourseAttachment } from '@/api/course'
import { useUserStore } from '@/store/user'
import { COURSE_STATUS_MAP, DAY_OF_WEEK_MAP, type Course, type CourseSchedule, type CourseAttachment } from '@/types'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const loading = ref(false)
const course = ref<Course | null>(null)
const activeTab = ref('info')

// 日程表单
const showScheduleDialog = ref(false)
const scheduleFormRef = ref<FormInstance>()
const scheduleForm = reactive<CourseSchedule>({
  dayOfWeek: 1,
  startTime: '',
  endTime: '',
  location: ''
})

const scheduleRules: FormRules = {
  dayOfWeek: [{ required: true, message: '请选择星期', trigger: 'change' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }]
}

// 附件表单
const showAttachmentDialog = ref(false)
const attachmentFormRef = ref<FormInstance>()
const attachmentForm = reactive<CourseAttachment>({
  fileName: '',
  fileUrl: '',
  fileType: ''
})

const attachmentRules: FormRules = {
  fileName: [{ required: true, message: '请输入文件名', trigger: 'blur' }],
  fileUrl: [{ required: true, message: '请输入文件URL', trigger: 'blur' }]
}

// 获取课程详情
const fetchCourse = async () => {
  const id = Number(route.params.id)
  if (!id) return
  
  loading.value = true
  try {
    const res = await getCourse(id)
    course.value = res.data
  } catch (e) {
    // 错误已在拦截器中处理
  } finally {
    loading.value = false
  }
}

// 添加日程
const handleAddSchedule = async () => {
  if (!scheduleFormRef.value) return
  
  await scheduleFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    try {
      await addCourseSchedule(Number(route.params.id), scheduleForm)
      ElMessage.success('添加成功')
      showScheduleDialog.value = false
      fetchCourse()
    } catch (e) {
      // 错误已在拦截器中处理
    }
  })
}

// 删除日程
const handleDeleteSchedule = async (schedule: CourseSchedule) => {
  if (!schedule.id) return
  
  try {
    await ElMessageBox.confirm('确定要删除该日程吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteCourseSchedule(schedule.id)
    ElMessage.success('删除成功')
    fetchCourse()
  } catch (e) {
    // 取消或错误
  }
}

// 添加附件
const handleAddAttachment = async () => {
  if (!attachmentFormRef.value) return
  
  await attachmentFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    try {
      await addCourseAttachment(Number(route.params.id), attachmentForm)
      ElMessage.success('添加成功')
      showAttachmentDialog.value = false
      fetchCourse()
    } catch (e) {
      // 错误已在拦截器中处理
    }
  })
}

// 下载文件
const downloadFile = (attachment: CourseAttachment) => {
  if (attachment.fileUrl) {
    window.open(attachment.fileUrl, '_blank')
  }
}

// 格式化文件大小
const formatFileSize = (size: number | undefined) => {
  if (!size) return '-'
  if (size < 1024) return size + ' B'
  if (size < 1024 * 1024) return (size / 1024).toFixed(2) + ' KB'
  return (size / 1024 / 1024).toFixed(2) + ' MB'
}

onMounted(() => {
  fetchCourse()
})
</script>

<style lang="scss" scoped>
.course-detail-page {
  .page-title {
    font-size: 18px;
    font-weight: bold;
  }
  
  .status-tag {
    margin-left: 10px;
  }
  
  .course-content {
    margin-top: 20px;
  }
  
  .course-tabs {
    :deep(.el-tabs__content) {
      padding: 0;
    }
  }
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  
  .text-danger {
    color: #f56c6c;
    font-weight: bold;
  }
  
  .pre-text {
    white-space: pre-wrap;
    word-wrap: break-word;
    margin: 0;
    font-family: inherit;
  }
}
</style>
