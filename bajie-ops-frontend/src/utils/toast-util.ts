import { createVNode, render } from 'vue';
import BaseToast from "@/components/common/BaseToast.vue"


// 函数式调用 Alert
export function showFailureToast(title: unknown, closable = false) {
    //根据toast定义生成虚拟节点
    const toast = createVNode(BaseToast, { title: coerceToString(title), type: 'error', closable })
    //创建一个div来渲染这个虚拟节点
    const div = document.createElement("div")
    render(toast, div)
    //将div添加到body上
    document.body.append(div)
}

export function showSuccessToast(title: unknown, closable = false) {
    //根据toast定义生成虚拟节点
    const toast = createVNode(BaseToast, { title: coerceToString(title), type: 'success', closable })
    //创建一个div来渲染这个虚拟节点
    const div = document.createElement("div")
    render(toast, div)
    //将div添加到body上
    document.body.append(div)
}

function coerceToString(value: unknown): string {
    return `${value}`
}