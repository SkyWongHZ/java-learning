<template>
    <div v-if="states.isShowToast" class="base-toast">
        <el-alert v-show="states.isShowLabel" class='base-toast-label' :title="title" :type="props.type" center
            :closable="props.closable" @close="onClose" />
    </div>
</template>

<script lang='ts' setup>
import { reactive, watchEffect } from 'vue';
const props = defineProps<{
    title: string,
    type: string,
    closable: boolean
}>()

interface IState {
    isShowToast: boolean,
    isShowLabel: boolean
}
const states: IState = reactive({
    isShowToast: true,
    isShowLabel: true
})

const DURATION = 2000

watchEffect(() => {
    document.body.style.overflow = states.isShowToast ? 'hidden' : '';
    if (!props.closable) {
        setTimeout(() => {
            states.isShowToast = false
        }, DURATION);
    }
})

function onClose() {
    states.isShowToast = false
}

</script>

<style scoped lang='scss'>
.base-toast {
    @include z-index-max-pro;
    background-color: transparent;
    position: fixed;
    top: 0px;
    left: 0px;
    right: 0;
    bottom: 0;
    display: flex;
    align-items: center;
    justify-content: center;



    &-label {
        width: auto;

        :deep(.el-alert__content) {
            flex-direction: row;
            align-items: center;
        }

        :deep(.el-icon) {
            position: relative;
            top: 0;
            right: 0;
            margin-left: 10px;

        }

    }


}
</style>