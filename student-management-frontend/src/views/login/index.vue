<template>
  <div class="login-container">
    <div class="login-card">
      <div class="login-header">
        <h1>学生管理系统</h1>
        <p>Student Management System</p>
      </div>
      
      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        class="login-form"
        @keyup.enter="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入用户名"
            prefix-icon="User"
            size="large"
          />
        </el-form-item>
        
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            prefix-icon="Lock"
            size="large"
            show-password
          />
        </el-form-item>
        
        <el-form-item prop="captcha">
          <div class="captcha-row">
            <el-input
              v-model="loginForm.captcha"
              placeholder="请输入验证码"
              prefix-icon="Key"
              size="large"
              class="captcha-input"
            />
            <img
              v-if="captchaImage"
              :src="captchaImage"
              class="captcha-image"
              @click="refreshCaptcha"
              title="点击刷新验证码"
            />
            <el-button
              v-else
              size="large"
              @click="refreshCaptcha"
              :loading="captchaLoading"
            >
              获取验证码
            </el-button>
          </div>
        </el-form-item>
        
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            class="login-button"
            :loading="loading"
            @click="handleLogin"
          >
            登录
          </el-button>
        </el-form-item>
        
        <div class="login-footer">
          <span>还没有账号？</span>
          <router-link to="/register">立即注册</router-link>
        </div>
      </el-form>
      
      <div class="test-accounts">
        <el-divider>测试账号</el-divider>
        <div class="account-list">
          <el-tag @click="fillAccount('admin', '123456')">管理员: admin / 123456</el-tag>
          <el-tag type="success" @click="fillAccount('teacher', '123456')">教师: teacher / 123456</el-tag>
          <el-tag type="warning" @click="fillAccount('student', '123456')">学生: student / 123456</el-tag>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { useUserStore } from '@/store/user'
import { getCaptcha } from '@/api/auth'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const loginFormRef = ref<FormInstance>()
const loading = ref(false)
const captchaLoading = ref(false)
const captchaImage = ref('')
const captchaKey = ref('')

const loginForm = reactive({
  username: '',
  password: '',
  captcha: ''
})

const loginRules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 50, message: '用户名长度在3-50个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 100, message: '密码长度在6-100个字符', trigger: 'blur' }
  ]
}

const refreshCaptcha = async () => {
  captchaLoading.value = true
  try {
    const res = await getCaptcha()
    captchaImage.value = res.data.image
    captchaKey.value = res.data.key
  } catch (error) {
    console.error('获取验证码失败', error)
  } finally {
    captchaLoading.value = false
  }
}

const handleLogin = async () => {
  if (!loginFormRef.value) return
  
  await loginFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    loading.value = true
    try {
      await userStore.login({
        username: loginForm.username,
        password: loginForm.password,
        captchaKey: captchaKey.value,
        captcha: loginForm.captcha
      })
      
      ElMessage.success('登录成功')
      
      const redirect = route.query.redirect as string
      router.push(redirect || '/')
    } catch (error: any) {
      // 登录失败刷新验证码
      refreshCaptcha()
      loginForm.captcha = ''
    } finally {
      loading.value = false
    }
  })
}

const fillAccount = (username: string, password: string) => {
  loginForm.username = username
  loginForm.password = password
}

onMounted(() => {
  refreshCaptcha()
})
</script>

<style lang="scss" scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-card {
  width: 420px;
  padding: 40px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
  
  h1 {
    margin: 0;
    font-size: 28px;
    color: #333;
  }
  
  p {
    margin: 8px 0 0;
    color: #999;
    font-size: 14px;
  }
}

.login-form {
  .captcha-row {
    display: flex;
    gap: 10px;
    width: 100%;
    
    .captcha-input {
      flex: 1;
    }
    
    .captcha-image {
      height: 40px;
      cursor: pointer;
      border-radius: 4px;
      border: 1px solid #dcdfe6;
    }
  }
  
  .login-button {
    width: 100%;
  }
}

.login-footer {
  text-align: center;
  color: #999;
  
  a {
    color: #409eff;
    text-decoration: none;
    margin-left: 5px;
    
    &:hover {
      text-decoration: underline;
    }
  }
}

.test-accounts {
  margin-top: 20px;
  
  .account-list {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
    justify-content: center;
    
    .el-tag {
      cursor: pointer;
      
      &:hover {
        opacity: 0.8;
      }
    }
  }
}
</style>
