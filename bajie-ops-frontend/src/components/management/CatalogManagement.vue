<template>
  <section class="management-page">
    <div class="management-card">
      <header class="management-header">
        <div>
          <h1>{{ config.title }}</h1>
          <p>{{ config.description }}</p>
        </div>
        <el-button type="primary" :icon="Plus" @click="openCreate">新增{{ config.label }}</el-button>
      </header>

      <div class="management-filters">
        <el-input
          v-model="keyword"
          class="filter-input"
          clearable
          :placeholder="`搜索${config.label}编码或名称`"
          :prefix-icon="Search"
          @keyup.enter="loadList"
          @clear="loadList"
        />
        <el-button type="primary" :icon="Search" @click="loadList">查询</el-button>
        <el-button :icon="Refresh" @click="resetSearch">重置</el-button>
      </div>

      <div class="management-table">
        <el-table v-loading="loading" :data="items" stripe row-key="id">
          <el-table-column :prop="config.codeKey" :label="`${config.label}编码`" min-width="180" />
          <el-table-column :prop="config.nameKey" :label="`${config.label}名称`" min-width="240" />
          <el-table-column label="创建时间" min-width="180">
            <template #default="{ row }">{{ formatTime(row.gmtCreate) }}</template>
          </el-table-column>
          <el-table-column label="修改时间" min-width="180">
            <template #default="{ row }">{{ formatTime(row.gmtModify) }}</template>
          </el-table-column>
          <el-table-column label="操作" fixed="right" width="150">
            <template #default="{ row }">
              <div class="operation-buttons">
                <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
                <el-button link type="danger" @click="removeItem(row)">删除</el-button>
              </div>
            </template>
          </el-table-column>
          <template #empty>
            <el-empty :description="`暂无${config.label}数据`" />
          </template>
        </el-table>
      </div>
    </div>

    <el-dialog
      v-model="dialogVisible"
      :title="editingId ? `修改${config.label}` : `新增${config.label}`"
      width="520px"
      destroy-on-close
      append-to-body
      @closed="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="92px" status-icon>
        <el-form-item :label="`${config.label}编码`" prop="code">
          <el-input
            v-model="form.code"
            maxlength="32"
            show-word-limit
            :placeholder="config.codePlaceholder"
          />
        </el-form-item>
        <el-form-item :label="`${config.label}名称`" prop="name">
          <el-input
            v-model="form.name"
            maxlength="100"
            show-word-limit
            :placeholder="`请输入${config.label}名称`"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { Plus, Refresh, Search } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import dayjs from 'dayjs'
import { API } from '@/api'
import type { SchoolClass, Course } from '@/interfaces/student-management'

type CatalogKind = 'course' | 'class'
type CatalogItem = Course | SchoolClass

const props = defineProps<{ kind: CatalogKind }>()

const configs = {
  course: {
    title: '课程管理',
    label: '课程',
    description: '维护可供学生选择的课程基础信息',
    codeKey: 'courseCode',
    nameKey: 'courseName',
    codePlaceholder: '例如：JAVA-101'
  },
  class: {
    title: '班级管理',
    label: '班级',
    description: '维护学生所属班级的基础信息',
    codeKey: 'classCode',
    nameKey: 'className',
    codePlaceholder: '例如：CS-2026-01'
  }
} as const

const config = computed(() => configs[props.kind])
const keyword = ref('')
const items = ref<CatalogItem[]>([])
const loading = ref(false)
const dialogVisible = ref(false)
const submitting = ref(false)
const editingId = ref<number>()
const formRef = ref<FormInstance>()
const form = reactive({ code: '', name: '' })
const formRules: FormRules = {
  code: [
    { required: true, message: '请输入编码', trigger: 'blur' },
    { min: 2, max: 32, message: '编码长度为2到32个字符', trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入名称', trigger: 'blur' },
    { max: 100, message: '名称不能超过100个字符', trigger: 'blur' }
  ]
}

onMounted(loadList)

async function loadList() {
  loading.value = true
  try {
    items.value =
      props.kind === 'course'
        ? await API.getCourses(keyword.value.trim())
        : await API.getClasses(keyword.value.trim())
  } catch (error) {
    ElMessage.error(String(error))
  } finally {
    loading.value = false
  }
}

function resetSearch() {
  keyword.value = ''
  loadList()
}

function openCreate() {
  editingId.value = undefined
  resetForm()
  dialogVisible.value = true
}

function openEdit(item: CatalogItem) {
  editingId.value = item.id
  if (props.kind === 'course') {
    const course = item as Course
    form.code = course.courseCode
    form.name = course.courseName
  } else {
    const schoolClass = item as SchoolClass
    form.code = schoolClass.classCode
    form.name = schoolClass.className
  }
  dialogVisible.value = true
}

async function submitForm() {
  if (!formRef.value || !(await formRef.value.validate())) return
  submitting.value = true
  try {
    if (props.kind === 'course') {
      const payload = { courseCode: form.code.trim(), courseName: form.name.trim() }
      if (editingId.value) await API.updateCourse(editingId.value, payload)
      else await API.createCourse(payload)
    } else {
      const payload = { classCode: form.code.trim(), className: form.name.trim() }
      if (editingId.value) await API.updateClass(editingId.value, payload)
      else await API.createClass(payload)
    }
    ElMessage.success(`${config.value.label}${editingId.value ? '修改' : '新增'}成功`)
    dialogVisible.value = false
    await loadList()
  } catch (error) {
    ElMessage.error(String(error))
  } finally {
    submitting.value = false
  }
}

async function removeItem(item: CatalogItem) {
  const name =
    props.kind === 'course' ? (item as Course).courseName : (item as SchoolClass).className
  try {
    await ElMessageBox.confirm(
      `确定删除${config.value.label}“${name}”吗？`,
      '删除确认',
      {
        confirmButtonText: '删除',
        cancelButtonText: '取消',
        confirmButtonClass: 'el-button--danger',
        type: 'warning'
      }
    )
    if (props.kind === 'course') await API.deleteCourse(item.id)
    else await API.deleteClass(item.id)
    ElMessage.success('删除成功')
    await loadList()
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') ElMessage.error(String(error))
  }
}

function resetForm() {
  form.code = ''
  form.name = ''
  formRef.value?.clearValidate()
}

function formatTime(value: string) {
  return value ? dayjs(value).format('YYYY-MM-DD HH:mm') : '—'
}
</script>
