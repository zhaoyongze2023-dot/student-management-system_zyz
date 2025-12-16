<template>
  <div class="layout-container">
    <!-- 侧边栏 -->
    <aside class="layout-sidebar" :class="{ collapsed: appStore.sidebarCollapsed }">
      <div class="sidebar-logo">
        <span v-if="!appStore.sidebarCollapsed">学生管理系统</span>
        <span v-else>SMS</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        :collapse="appStore.sidebarCollapsed"
        :collapse-transition="false"
        router
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409eff"
      >
        <template v-for="item in menuList" :key="item.path || item.title">
          <!-- 单个菜单项 -->
          <el-menu-item v-if="!item.children" :index="item.path">
            <el-icon><component :is="item.icon" /></el-icon>
            <template #title>{{ item.title }}</template>
          </el-menu-item>
          
          <!-- 子菜单组 -->
          <el-sub-menu v-else :index="item.title">
            <template #title>
              <el-icon><component :is="item.icon" /></el-icon>
              <span>{{ item.title }}</span>
            </template>
            <el-menu-item
              v-for="child in item.children"
              :key="child.path"
              :index="child.path"
            >
              {{ child.title }}
            </el-menu-item>
          </el-sub-menu>
        </template>
      </el-menu>
    </aside>
    
    <!-- 主内容区 -->
    <div class="layout-main" :class="{ collapsed: appStore.sidebarCollapsed }">
      <!-- 顶部导航 -->
      <header class="layout-header">
        <div class="header-left">
          <el-icon class="collapse-btn" @click="appStore.toggleSidebar">
            <Fold v-if="!appStore.sidebarCollapsed" />
            <Expand v-else />
          </el-icon>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item v-for="item in breadcrumbs" :key="item.path">
              {{ item.meta?.title }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        
        <div class="header-right">
          <!-- 消息通知 -->
          <el-badge :value="unreadCount" :hidden="unreadCount === 0" class="notification-badge">
            <el-icon class="header-icon" @click="router.push('/notification')">
              <Bell />
            </el-icon>
          </el-badge>
          
          <!-- 用户信息 -->
          <el-dropdown trigger="click" @command="handleCommand">
            <div class="user-info">
              <el-avatar :size="32" :src="userStore.user?.avatar">
                {{ userStore.user?.username?.charAt(0).toUpperCase() }}
              </el-avatar>
              <span class="username">{{ userStore.user?.username }}</span>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>个人中心
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>
      
      <!-- 页面内容 -->
      <main class="layout-content">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore, useAppStore } from '@/store'
import { authRoutes } from '@/router/routes'
import { getUnreadCount } from '@/api/notification'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const appStore = useAppStore()

const unreadCount = ref(0)

// 菜单项类型
interface MenuItem {
  title: string
  path?: string
  icon?: string
  children?: MenuItem[]
}

// 构建菜单列表
const menuList = computed(() => {
  const mainRoute = authRoutes.find(r => r.path === '/')
  if (!mainRoute || !mainRoute.children) return []
  
  const routes = mainRoute.children.filter(r => {
    // 过滤隐藏的路由
    if (r.meta?.hidden) return false
    // 检查角色权限
    const roles = r.meta?.roles as string[] | undefined
    if (roles && roles.length > 0) {
      return userStore.hasAnyRole(roles)
    }
    return true
  })
  
  // 根据 parentTitle 分组
  const grouped = new Map<string, MenuItem>()
  const result: MenuItem[] = []
  
  routes.forEach(r => {
    const parentTitle = r.meta?.parentTitle as string | undefined
    const item: MenuItem = {
      title: r.meta?.title as string,
      path: '/' + r.path,
      icon: r.meta?.icon as string
    }
    
    if (parentTitle) {
      // 有父级标题，归入分组
      if (!grouped.has(parentTitle)) {
        // 使用第一个子项的 icon 作为组图标
        grouped.set(parentTitle, {
          title: parentTitle,
          icon: r.meta?.icon as string,
          children: []
        })
      }
      grouped.get(parentTitle)!.children!.push(item)
    } else {
      // 无父级标题，直接作为一级菜单
      result.push(item)
    }
  })
  
  // 将分组插入到合适位置
  // Dashboard 在最前面
  const dashboard = result.find(r => r.path === '/dashboard')
  const others = result.filter(r => r.path !== '/dashboard')
  
  const final: MenuItem[] = []
  if (dashboard) final.push(dashboard)
  
  // 添加学生管理
  if (grouped.has('学生管理')) final.push(grouped.get('学生管理')!)
  // 添加课程管理
  if (grouped.has('课程管理')) final.push(grouped.get('课程管理')!)
  // 添加选课中心
  if (grouped.has('选课中心')) final.push(grouped.get('选课中心')!)
  // 添加其他独立菜单
  others.forEach(item => final.push(item))
  
  return final
})

// 当前激活菜单
const activeMenu = computed(() => {
  return route.path
})

// 面包屑
const breadcrumbs = computed(() => {
  return route.matched.filter(item => item.meta?.title)
})

// 获取未读消息数
const fetchUnreadCount = async () => {
  try {
    const res = await getUnreadCount()
    unreadCount.value = res.data?.count || 0
  } catch (e) {
    // 忽略错误
  }
}

// 处理下拉命令
const handleCommand = async (command: string) => {
  if (command === 'profile') {
    router.push('/profile')
  } else if (command === 'logout') {
    try {
      await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      await userStore.logout()
      ElMessage.success('已退出登录')
      router.push('/login')
    } catch (e) {
      // 取消
    }
  }
}

onMounted(() => {
  fetchUnreadCount()
  // 每分钟刷新一次未读消息数
  setInterval(fetchUnreadCount, 60000)
})
</script>

<style lang="scss" scoped>
.layout-container {
  display: flex;
  min-height: 100vh;
}

.layout-sidebar {
  width: 210px;
  background: #304156;
  transition: width 0.3s;
  position: fixed;
  left: 0;
  top: 0;
  bottom: 0;
  z-index: 100;
  overflow-y: auto;
  
  &.collapsed {
    width: 64px;
  }
  
  .sidebar-logo {
    height: 60px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    font-size: 18px;
    font-weight: bold;
    background: #263445;
    white-space: nowrap;
    overflow: hidden;
  }
  
  .el-menu {
    border-right: none;
  }
}

.layout-main {
  flex: 1;
  margin-left: 210px;
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  transition: margin-left 0.3s;
  
  &.collapsed {
    margin-left: 64px;
  }
}

.layout-header {
  height: 60px;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  position: sticky;
  top: 0;
  z-index: 99;
  
  .header-left {
    display: flex;
    align-items: center;
    gap: 15px;
    
    .collapse-btn {
      font-size: 20px;
      cursor: pointer;
      color: #606266;
      
      &:hover {
        color: #409eff;
      }
    }
  }
  
  .header-right {
    display: flex;
    align-items: center;
    gap: 20px;
    
    .notification-badge {
      cursor: pointer;
      
      .header-icon {
        font-size: 20px;
        color: #606266;
        
        &:hover {
          color: #409eff;
        }
      }
    }
    
    .user-info {
      display: flex;
      align-items: center;
      gap: 8px;
      cursor: pointer;
      
      .username {
        color: #606266;
      }
      
      .el-icon {
        color: #909399;
      }
    }
  }
}

.layout-content {
  flex: 1;
  padding: 20px;
  background: #f5f7fa;
}

// 页面切换动画
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
