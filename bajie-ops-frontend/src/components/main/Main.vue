<template>
  <div class="main-shell" :class="{ 'is-sidebar-collapsed': sidebarCollapsed }">
    <aside class="sidebar">
      <div class="brand">
        <span class="brand-mark">教</span>
        <div class="brand-text">
          <span class="brand-name">学生信息管理</span>
          <span class="brand-sub">教务管理后台</span>
        </div>
      </div>

      <nav class="sidebar-menu">
        <div class="menu-group">教务管理</div>
        <router-link
          class="sidebar-item"
          to="/students"
          exact-active-class="active"
          title="学生管理"
        >
          <el-icon>
            <User />
          </el-icon>
          <span>学生管理</span>
        </router-link>
        <router-link
          class="sidebar-item"
          to="/courses"
          exact-active-class="active"
          title="课程管理"
        >
          <el-icon><Reading /></el-icon>
          <span>课程管理</span>
        </router-link>
        <router-link
          class="sidebar-item"
          to="/classes"
          exact-active-class="active"
          title="班级管理"
        >
          <el-icon><OfficeBuilding /></el-icon>
          <span>班级管理</span>
        </router-link>
      </nav>
    </aside>

    <div class="main-col">
      <header class="topbar">
        <div class="top-left">
          <button
            class="sidebar-toggle"
            type="button"
            :aria-label="sidebarCollapsed ? '展开导航' : '收起导航'"
            @click="toggleSidebar"
          >
            <el-icon>
              <Expand v-if="sidebarCollapsed" />
              <Fold v-else />
            </el-icon>
          </button>
          <div class="crumb">
            <span class="crumb-home">首页</span>
            <span class="crumb-sep">/</span>
            <span class="crumb-cur">{{ currentTitle }}</span>
          </div>
        </div>

        <div class="top-actions">
          <button class="notice-button" type="button">
            <el-icon><Bell /></el-icon>
            <span></span>
          </button>
          <span class="top-divider"></span>
          <el-dropdown class="top-profile" trigger="click" @command="handleUserCommand">
            <div class="top-profile-inner">
              <div class="top-avatar">{{ sideAvatar }}</div>
              <span class="top-user">{{ userName }}</span>
              <el-icon class="top-caret"><CaretBottom /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout" :icon="SwitchButton">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <main class="workspace">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { computed, ref } from 'vue'
import {
  Bell,
  CaretBottom,
  Expand,
  Fold,
  OfficeBuilding,
  Reading,
  SwitchButton,
  User
} from '@element-plus/icons-vue'
import { useRoute } from 'vue-router'
import { useCachedLoginModelStore } from '@/stores/cachedLoginModel'
import { logout } from '@/utils/common-util'
import { API } from '@/api'

defineOptions({
  name: 'MainLayout'
})

const route = useRoute()
const loginStore = useCachedLoginModelStore()
const sidebarCollapsed = ref(false)

const userName = computed(() => loginStore.userName || '管理员')
const sideAvatar = computed(() => (userName.value || '用').slice(0, 1))
const currentTitle = computed(() => String(route.meta.title || '学生管理'))

async function onLogout() {
  try {
    await API.logout()
  } finally {
    logout()
  }
}

function handleUserCommand(command: string) {
  if (command === 'logout') onLogout()
}

function toggleSidebar() {
  sidebarCollapsed.value = !sidebarCollapsed.value
}
</script>

<style lang="scss" scoped>
.main-shell {
  min-width: 1180px;
  height: 100vh;
  display: flex;
  background: #f6f7f9;
  color: #1f2430;
}

.sidebar {
  width: 240px;
  flex: 0 0 240px;
  display: flex;
  flex-direction: column;
  background: #151b2e;
  overflow: hidden;
  transition:
    width 0.2s ease,
    flex-basis 0.2s ease;
}

.brand {
  display: flex;
  align-items: center;
  gap: 11px;
  padding: 20px 20px 16px;
  transition: padding 0.2s ease;
}

.brand-mark {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, $theme-color, #6366f1);
  box-shadow: 0 8px 18px -8px $theme-color;
  color: #ffffff;
  font-size: 14px;
  font-weight: 700;
}

.brand-text {
  display: flex;
  flex-direction: column;
  line-height: 1.3;
  white-space: nowrap;
}

.brand-name {
  color: #e7eaf3;
  font-size: 15px;
  font-weight: 700;
}

.brand-sub {
  color: #5b6480;
  font-size: 11px;
  letter-spacing: 0;
}

.menu-group {
  padding: 12px 22px 6px;
  color: #5b6480;
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 0;
}

.sidebar-menu {
  flex: 1;
  overflow: auto;
  padding: 4px 0 12px;
}

.sidebar-item {
  width: calc(100% - 20px);
  height: 40px;
  margin: 1px 10px;
  padding: 0 12px;
  display: flex;
  align-items: center;
  gap: 11px;
  border: 0;
  border-radius: 10px;
  color: #99a1b7;
  background: transparent;
  cursor: pointer;
  font-size: 14px;
  line-height: 40px;
  text-align: left;
  text-decoration: none;
  transition:
    width 0.2s ease,
    margin 0.2s ease,
    padding 0.2s ease,
    background 0.2s ease,
    color 0.2s ease;

  &:hover {
    background: rgba(255, 255, 255, 0.06);
    color: #e7eaf3;
  }

  &.active {
    background: $theme-color;
    box-shadow: 0 8px 18px -8px $theme-color;
    color: #ffffff;
    font-weight: 600;
  }

  .el-icon {
    flex: 0 0 auto;
    color: inherit;
    font-size: 18px;
  }
}

.is-sidebar-collapsed {
  .sidebar {
    width: 64px;
    flex-basis: 64px;
  }

  .brand {
    justify-content: center;
    padding: 20px 0 16px;
  }

  .brand-text,
  .menu-group,
  .sidebar-item > span {
    display: none;
  }

  .sidebar-menu {
    padding-top: 12px;
  }

  .sidebar-item {
    width: 44px;
    justify-content: center;
    margin: 4px 10px;
    padding: 0;
  }
}

.main-col {
  min-width: 0;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.topbar {
  height: 60px;
  flex: 0 0 60px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  border-bottom: 1px solid #edeff2;
  background: #ffffff;
}

.top-left {
  min-width: 0;
  display: flex;
  align-items: center;
  gap: 14px;
}

.sidebar-toggle {
  width: 38px;
  height: 38px;
  flex: 0 0 auto;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border: 1px solid #eceef1;
  border-radius: 10px;
  background: #ffffff;
  color: #5a616d;
  cursor: pointer;
  transition:
    background 0.2s ease,
    color 0.2s ease,
    border-color 0.2s ease;

  &:hover {
    border-color: #d8dce3;
    background: #f7f8fa;
    color: $theme-color;
  }
}

.crumb {
  display: flex;
  align-items: center;
  gap: 9px;
  font-size: 14px;
}

.crumb-home {
  color: #9198a5;
}

.crumb-sep {
  color: #c7ccd4;
}

.crumb-cur {
  color: #1f2430;
  font-weight: 600;
}

.top-actions {
  display: flex;
  align-items: center;
  gap: 14px;
}

.notice-button {
  width: 38px;
  height: 38px;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid #eceef1;
  border-radius: 10px;
  background: #ffffff;
  color: #5a616d;
  cursor: pointer;

  &:hover {
    background: #f7f8fa;
  }

  span {
    position: absolute;
    top: 9px;
    right: 10px;
    width: 7px;
    height: 7px;
    border: 1.5px solid #ffffff;
    border-radius: 50%;
    background: #ef4444;
  }
}

.top-divider {
  width: 1px;
  height: 24px;
  background: #eaecef;
}

.top-user {
  color: #2a2f3a;
  font-size: 14px;
  font-weight: 600;
}

.top-profile {
  cursor: pointer;
  outline: none;
}

.top-profile-inner {
  display: flex;
  align-items: center;
  gap: 9px;
  height: 44px;
  padding: 0 10px;
  border-radius: 10px;
  outline: none;
  transition: background 0.2s;

  &:hover {
    background: #f4f5f7;
  }
}

.top-caret {
  color: #9aa1ac;
  font-size: 14px;
}

.top-avatar {
  width: 34px;
  height: 34px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: linear-gradient(135deg, #6366f1, #06b6d4);
  color: #ffffff;
  font-size: 13px;
  font-weight: 600;
}

.workspace {
  min-width: 0;
  min-height: 0;
  flex: 1;
  overflow: auto;
  padding: 16px 24px;
  box-sizing: border-box;
}

</style>
