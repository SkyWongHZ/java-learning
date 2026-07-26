<template>
  <div class="login">
    <section class="login-hero">
      <div class="login-hero-glow login-hero-glow-top"></div>
      <div class="login-hero-glow login-hero-glow-bottom"></div>

      <div class="login-brand">
        <span class="login-brand-mark">教</span>
        <span class="login-brand-name">学生信息管理系统</span>
      </div>

      <div class="login-hero-content">
        <p class="login-hero-kicker">STUDENT · COURSE · CLASS</p>
        <h1 class="login-hero-title">
          学生信息统一维护<br />
          简洁高效的教务管理
        </h1>
        <ul class="login-hero-points">
          <li>
            <span class="login-hero-check">✓</span>
            <span>学生基础信息集中管理</span>
          </li>
          <li>
            <span class="login-hero-check">✓</span>
            <span>课程与班级数据独立维护</span>
          </li>
          <li>
            <span class="login-hero-check">✓</span>
            <span>学生班级归属清晰可查</span>
          </li>
          <li>
            <span class="login-hero-check">✓</span>
            <span>学生选课关系便捷配置</span>
          </li>
        </ul>
      </div>

      <p class="login-version">© 2026 学生信息管理系统</p>
    </section>

    <section class="login-panel">
      <div class="login-card">
        <div class="login-card-header">
          <h2>欢迎登录</h2>
          <p>请输入管理员账号与密码进入系统</p>
        </div>

        <Password class="login-form" @onLoginSucceed="onLoginSucceed" />
      </div>
    </section>
  </div>
</template>

<script lang="ts" setup>
import Password from '@/components/login/Password.vue'
import router from '@/router'
import { LoginModel } from './LoginModel'
import type { ILogin } from '@/interfaces/response/ILogin'
import { showSuccessToast, showFailureToast } from '@/utils/toast-util'
import { useCachedLoginModelStore } from '@/stores/cachedLoginModel'
import { API } from '@/api'

async function onLoginSucceed(loginInfo: ILogin) {
  const loginModel = new LoginModel(loginInfo)
  useCachedLoginModelStore().save(loginModel)
  try {
    await API.currentUser()
    showSuccessToast('登录成功')
    router.push({ name: 'students' })
  } catch (error) {
    useCachedLoginModelStore().remove()
    showFailureToast(error)
  }
}
</script>

<style lang="scss" scoped>
.login {
  width: 100vw;
  min-height: 100vh;
  display: flex;
  overflow: hidden;
  background-color: #fbfcfd;

  &-hero {
    position: relative;
    flex: 1.05;
    min-width: 540px;
    min-height: 100vh;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    box-sizing: border-box;
    padding: 44px 52px;
    color: #ffffff;
    background:
      radial-gradient(circle at 82% 8%, rgba(88, 80, 236, 0.5), transparent 28%),
      radial-gradient(circle at 2% 95%, rgba(28, 108, 126, 0.42), transparent 30%),
      #151b2e;

    &-glow {
      position: absolute;
      pointer-events: none;
      filter: blur(8px);
      opacity: 0.9;
    }

    &-glow-top {
      top: -90px;
      right: -70px;
      width: 340px;
      height: 340px;
      background: radial-gradient(circle at 30% 30%, rgba(99, 102, 241, 0.55), transparent 70%);
      border-radius: 50%;
    }

    &-glow-bottom {
      bottom: -120px;
      left: -50px;
      width: 380px;
      height: 380px;
      background: radial-gradient(circle, rgba(6, 182, 212, 0.26), transparent 70%);
      border-radius: 50%;
      filter: blur(20px);
    }

    &-content {
      position: relative;
      max-width: 440px;
      margin-top: auto;
      margin-bottom: auto;
    }

    &-kicker {
      margin: 0 0 18px;
      color: rgba(255, 255, 255, 0.5);
      font-size: 12.5px;
      font-weight: 700;
      letter-spacing: 0;
    }

    &-title {
      margin: 0;
      color: #ffffff;
      font-size: 34px;
      font-weight: 700;
      line-height: 1.32;
      letter-spacing: 0;
    }

    &-points {
      display: flex;
      flex-direction: column;
      gap: 14px;
      margin: 28px 0 0;
      padding: 0;
      list-style: none;
    }

    &-points li {
      display: flex;
      align-items: center;
      gap: 14px;
      color: rgba(231, 234, 243, 0.9);
      font-size: 14px;
      font-weight: 500;
      line-height: 1.4;
    }

    &-check {
      width: 22px;
      height: 22px;
      display: inline-flex;
      align-items: center;
      justify-content: center;
      flex: 0 0 22px;
      color: #55d77f;
      background-color: rgba(255, 255, 255, 0.12);
      border-radius: 7px;
      font-size: 14px;
      font-weight: 900;
    }
  }

  &-brand {
    position: relative;
    display: flex;
    align-items: center;
    gap: 14px;

    &-mark {
      width: 40px;
      height: 40px;
      display: inline-flex;
      align-items: center;
      justify-content: center;
      color: #ffffff;
      background: linear-gradient(135deg, $theme-color 0%, #6366f1 100%);
      border-radius: 11px;
      box-shadow: 0 10px 24px -8px $theme-color;
      font-size: 15px;
      font-weight: 700;
      letter-spacing: 0;
    }

    &-name {
      color: #ffffff;
      font-size: 18px;
      font-weight: 700;
      letter-spacing: 0;
    }
  }

  &-version {
    position: relative;
    margin: 0;
    color: rgba(255, 255, 255, 0.4);
    font-size: 12.5px;
  }

  &-panel {
    flex: 0.95;
    min-width: 520px;
    min-height: 100vh;
    box-sizing: border-box;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 40px;
    background-color: #fbfcfd;
  }

  &-card {
    width: min(380px, 100%);

    &-header {
      margin-bottom: 32px;
    }

    &-header h2 {
      margin: 0;
      color: #1b2029;
      font-size: 25px;
      font-weight: 700;
      line-height: 1.32;
      letter-spacing: 0;
    }

    &-header p {
      margin: 8px 0 0;
      color: #7a828f;
      font-size: 14px;
      font-weight: 400;
      line-height: 1.5;
    }
  }

  &-form {
    width: 100%;
  }
}

@media (max-width: 1100px) {
  .login {
    &-hero {
      min-width: 440px;
      padding: 36px 38px 30px;

      &-title {
        font-size: 32px;
      }
    }

    &-panel {
      min-width: 460px;
      padding: 42px;
    }
  }
}

@media (max-width: 860px) {
  .login {
    flex-direction: column;
    overflow-y: auto;

    &-hero {
      min-width: 0;
      min-height: 420px;
    }

    &-panel {
      min-width: 0;
      min-height: auto;
      padding: 42px 24px 56px;
    }
  }
}
</style>
