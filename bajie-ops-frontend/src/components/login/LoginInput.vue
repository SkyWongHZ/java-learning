<template>
  <div
    class="login-input"
    :class="{
      'login-input-focus': states.isFocus,
      'login-input-disabled': props.disabled
    }"
  >
    <img class="login-input-icon" :src="props.icon" />
    <input
      class="login-input-input"
      :type="props.type"
      :placeholder="props.placeholder"
      v-model.trim="value"
      autocomplete="off"
      :disabled="props.disabled"
      @focus="onFocus"
      @blur="onBlur"
    />
    <slot></slot>
  </div>
</template>

<script lang="ts" setup>
import { reactive } from 'vue'

const value = defineModel('value', {
  type: String,
  default: ''
})

interface IState {
  isFocus: boolean
}
const states: IState = reactive({
  isFocus: false
})

const props = withDefaults(
  defineProps<{
    type?: string
    placeholder: string
    icon: string
    disabled?: boolean
  }>(),
  {
    type: 'text',
    disabled: false
  }
)

function onFocus() {
  states.isFocus = true
}

function onBlur() {
  states.isFocus = false
}
</script>

<style lang="scss" scoped>
.login-input {
  width: 100%;
  height: 46px;
  box-sizing: border-box;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 14px;
  background-color: #ffffff;
  border: 1px solid #e3e7ed;
  border-radius: 10px;
  transition:
    border-color $animation-duration,
    background-color $animation-duration,
    box-shadow $animation-duration;

  &-focus {
    background-color: #f5f8ff;
    border-color: $theme-color;
    box-shadow: 0 0 0 3px rgba(84, 71, 232, 0.1);
  }

  &-disabled {
    background-color: #f1f3f6;
  }

  &-icon {
    width: 18px;
    height: 18px;
    object-fit: contain;
    opacity: 0.55;
    filter: grayscale(1) brightness(0.72);
  }

  &-input {
    min-width: 0;
    color: #1f2733;
    background-color: transparent;
    font-size: 14px;
    font-weight: 400;
    line-height: 20px;
    flex: 1;
  }

  input::-webkit-input-placeholder {
    color: #a7afbc;
    font-size: 14px;
    font-weight: 400;
  }
}
</style>
