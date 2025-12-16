<template>
  <div class="profile-page">
    <el-row :gutter="20">
      <!-- 左侧个人信息卡片 -->
      <el-col :span="8">
        <el-card class="profile-card">
          <div class="avatar-section">
            <el-upload
              class="avatar-uploader"
              :show-file-list="false"
              :before-upload="beforeAvatarUpload"
              :http-request="handleAvatarUpload"
            >
              <el-avatar :size="100" :src="userStore.user?.avatar">
                {{ userStore.user?.username?.charAt(0).toUpperCase() }}
              </el-avatar>
              <div class="avatar-mask">
                <el-icon><Camera /></el-icon>
              </div>
            </el-upload>
            <h3>{{ userStore.user?.username }}</h3>
            <div class="role-tags">
              <el-tag
                v-for="role in userStore.roles"
                :key="role"
                :type="getRoleTagType(role)"
                class="role-tag"
              >
                {{ getRoleName(role) }}
              </el-tag>
            </div>
          </div>
          
          <el-divider />
          
          <div class="info-list">
            <div class="info-item">
              <el-icon><Message /></el-icon>
              <span>{{ userStore.user?.email || '未设置' }}</span>
            </div>
            <div class="info-item">
              <el-icon><Phone /></el-icon>
              <span>{{ userStore.user?.phone || '未设置' }}</span>
            </div>
            <div class="info-item">
              <el-icon><Clock /></el-icon>
              <span>注册于 {{ formatDate(userStore.user?.createdAt) }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <!-- 右侧设置面板 -->
      <el-col :span="16">
        <el-card>
          <template #header>
            <span>账户设置</span>
          </template>
          
          <el-tabs v-model="activeTab">
            <!-- 基本信息 -->
            <el-tab-pane label="基本信息" name="basic">
              <el-form
                ref="basicFormRef"
                :model="basicForm"
                label-width="100px"
                style="max-width: 500px"
              >
                <el-form-item label="用户名">
                  <el-input v-model="basicForm.username" disabled />
                </el-form-item>
                <el-form-item label="邮箱">
                  <el-input v-model="basicForm.email" />
                </el-form-item>
                <el-form-item label="手机号">
                  <el-input v-model="basicForm.phone" />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="handleSaveBasic">
                    保存修改
                  </el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>
            
            <!-- 修改密码 -->
            <el-tab-pane label="修改密码" name="password">
              <el-form
                ref="passwordFormRef"
                :model="passwordForm"
                :rules="passwordRules"
                label-width="100px"
                style="max-width: 500px"
              >
                <el-form-item label="当前密码" prop="oldPassword">
                  <el-input v-model="passwordForm.oldPassword" type="password" show-password />
                </el-form-item>
                <el-form-item label="新密码" prop="newPassword">
                  <el-input v-model="passwordForm.newPassword" type="password" show-password />
                </el-form-item>
                <el-form-item label="确认密码" prop="confirmPassword">
                  <el-input v-model="passwordForm.confirmPassword" type="password" show-password />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="handleChangePassword">
                    修改密码
                  </el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>
            
            <!-- 权限信息 -->
            <el-tab-pane label="权限信息" name="permission">
              <div class="permission-section">
                <h4>当前角色</h4>
                <div class="role-list">
                  <el-tag
                    v-for="role in userStore.roles"
                    :key="role"
                    :type="getRoleTagType(role)"
                    size="large"
                  >
                    {{ getRoleName(role) }}
                  </el-tag>
                </div>
                
                <h4>拥有权限</h4>
                <div class="permission-list">
                  <el-tag
                    v-for="perm in userStore.permissions"
                    :key="perm"
                    type="info"
                    class="permission-tag"
                  >
                    {{ getPermissionName(perm) }}
                  </el-tag>
                </div>
              </div>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, type FormInstance, type FormRules, type UploadRawFile } from 'element-plus'
import { useUserStore } from '@/store/user'
import { uploadAvatar } from '@/api/upload'
import dayjs from 'dayjs'

const userStore = useUserStore()

const activeTab = ref('basic')
const basicFormRef = ref<FormInstance>()
const passwordFormRef = ref<FormInstance>()

// 基本信息表单
const basicForm = reactive({
  username: userStore.user?.username || '',
  email: userStore.user?.email || '',
  phone: userStore.user?.phone || ''
})

// 密码表单
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const validateConfirmPassword = (_rule: any, value: string, callback: any) => {
  if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const passwordRules: FormRules = {
  oldPassword: [
    { required: true, message: '请输入当前密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 100, message: '密码长度在6-100个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

// 获取角色标签类型
const getRoleTagType = (role: string) => {
  switch (role) {
    case 'admin': return 'danger'
    case 'teacher': return 'warning'
    case 'student': return 'success'
    default: return 'info'
  }
}

// 获取角色名称
const getRoleName = (role: string) => {
  switch (role) {
    case 'admin': return '管理员'
    case 'teacher': return '教师'
    case 'student': return '学生'
    default: return role
  }
}

// 获取权限名称
const getPermissionName = (perm: string) => {
  const permMap: Record<string, string> = {
    'student:view': '查看学生',
    'student:edit': '编辑学生',
    'student:delete': '删除学生',
    'course:view': '查看课程',
    'course:create': '创建课程',
    'course:edit': '编辑课程',
    'course:delete': '删除课程',
    'enrollment:view': '查看选课',
    'enrollment:manage': '管理选课',
    'grade:view': '查看成绩',
    'grade:edit': '编辑成绩',
    'data:export': '导出数据'
  }
  return permMap[perm] || perm
}

// 格式化日期
const formatDate = (date: string | undefined) => {
  if (!date) return '-'
  return dayjs(date).format('YYYY-MM-DD')
}

// 头像上传前校验
const beforeAvatarUpload = (file: UploadRawFile) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!')
    return false
  }
  return true
}

// 头像上传
const handleAvatarUpload = async (options: any) => {
  try {
    const res = await uploadAvatar(options.file)
    ElMessage.success('头像上传成功')
    // 刷新用户信息
    await userStore.fetchUserInfo()
  } catch (e) {
    // 错误已在拦截器中处理
  }
}

// 保存基本信息
const handleSaveBasic = () => {
  ElMessage.info('功能开发中...')
}

// 修改密码
const handleChangePassword = async () => {
  if (!passwordFormRef.value) return
  
  await passwordFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    ElMessage.info('功能开发中...')
  })
}

onMounted(() => {
  basicForm.username = userStore.user?.username || ''
  basicForm.email = userStore.user?.email || ''
  basicForm.phone = userStore.user?.phone || ''
})
</script>

<style lang="scss" scoped>
.profile-page {
  .profile-card {
    .avatar-section {
      text-align: center;
      
      .avatar-uploader {
        position: relative;
        display: inline-block;
        cursor: pointer;
        
        .el-avatar {
          border: 3px solid #fff;
          box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
        }
        
        .avatar-mask {
          position: absolute;
          top: 0;
          left: 0;
          right: 0;
          bottom: 0;
          background: rgba(0, 0, 0, 0.5);
          border-radius: 50%;
          display: flex;
          align-items: center;
          justify-content: center;
          opacity: 0;
          transition: opacity 0.3s;
          
          .el-icon {
            font-size: 24px;
            color: #fff;
          }
        }
        
        &:hover .avatar-mask {
          opacity: 1;
        }
      }
      
      h3 {
        margin: 15px 0 10px;
      }
      
      .role-tags {
        .role-tag {
          margin: 0 5px;
        }
      }
    }
    
    .info-list {
      .info-item {
        display: flex;
        align-items: center;
        gap: 10px;
        padding: 10px 0;
        color: #666;
        
        .el-icon {
          color: #409eff;
        }
      }
    }
  }
  
  .permission-section {
    h4 {
      margin: 20px 0 10px;
      color: #333;
      
      &:first-child {
        margin-top: 0;
      }
    }
    
    .role-list {
      display: flex;
      gap: 10px;
    }
    
    .permission-list {
      display: flex;
      flex-wrap: wrap;
      gap: 10px;
      
      .permission-tag {
        margin: 0;
      }
    }
  }
}
</style>
