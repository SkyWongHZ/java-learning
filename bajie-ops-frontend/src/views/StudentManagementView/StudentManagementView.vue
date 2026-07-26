<template>
  <section class="management-page">
    <div class="management-card">
      <header class="management-header">
        <div>
          <h1>学生管理</h1>
          <p>维护学生基础信息、班级归属和选课关系</p>
        </div>
        <el-button type="primary" :icon="Plus" @click="openCreate">新增学生</el-button>
      </header>

      <div class="management-filters">
        <el-input
          v-model="query.keyword"
          class="filter-input"
          clearable
          placeholder="搜索学号或姓名"
          :prefix-icon="Search"
          @keyup.enter="search"
          @clear="search"
        />
        <el-select
          v-model="query.classId"
          class="filter-select"
          clearable
          filterable
          placeholder="全部班级"
          @change="search"
        >
          <el-option
            v-for="item in classOptions"
            :key="item.id"
            :label="`${item.className}（${item.classCode}）`"
            :value="item.id"
          />
        </el-select>
        <el-select
          v-model="query.courseId"
          class="filter-select"
          clearable
          filterable
          placeholder="全部课程"
          @change="search"
        >
          <el-option
            v-for="item in courseOptions"
            :key="item.id"
            :label="`${item.courseName}（${item.courseCode}）`"
            :value="item.id"
          />
        </el-select>
        <el-button type="primary" :icon="Search" @click="search">查询</el-button>
        <el-button :icon="Refresh" @click="resetSearch">重置</el-button>
      </div>

      <div class="management-table">
        <el-table v-loading="loading" :data="students" stripe row-key="id">
          <el-table-column prop="studentNo" label="学号" min-width="130" />
          <el-table-column prop="name" label="姓名" min-width="110" />
          <el-table-column label="性别" width="80">
            <template #default="{ row }">
              <el-tag :type="genderTagType(row.gender)" effect="plain">
                {{ genderText(row.gender) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="phone" label="手机号" min-width="130">
            <template #default="{ row }">{{ row.phone || '—' }}</template>
          </el-table-column>
          <el-table-column label="班级" min-width="220">
            <template #default="{ row }">
              {{ row.classInfo?.className || '—' }}
              <span v-if="row.classInfo" class="muted-code">{{ row.classInfo.classCode }}</span>
            </template>
          </el-table-column>
          <el-table-column label="创建时间" min-width="160">
            <template #default="{ row }">{{ formatTime(row.gmtCreate) }}</template>
          </el-table-column>
          <el-table-column label="操作" fixed="right" width="210">
            <template #default="{ row }">
              <div class="operation-buttons">
                <el-button link type="primary" @click="openDetail(row.id)">详情</el-button>
                <el-button link type="primary" @click="openEdit(row.id)">编辑</el-button>
                <el-button link type="danger" @click="removeStudent(row)">删除</el-button>
              </div>
            </template>
          </el-table-column>
          <template #empty>
            <el-empty description="暂无学生数据" />
          </template>
        </el-table>
      </div>

      <div class="management-pagination">
        <el-pagination
          v-model:current-page="query.pageNum"
          v-model:page-size="query.pageSize"
          background
          layout="total, sizes, prev, pager, next, jumper"
          :page-sizes="[10, 20, 50]"
          :total="total"
          @current-change="loadStudents"
          @size-change="handleSizeChange"
        />
      </div>
    </div>

    <el-dialog
      v-model="formVisible"
      :title="editingId ? '修改学生' : '新增学生'"
      width="600px"
      destroy-on-close
      append-to-body
      @closed="resetForm"
    >
      <p v-if="editingId" class="dialog-tip">学号用于唯一识别学生，创建后不可修改。</p>
      <el-form
        ref="formRef"
        :model="form"
        :rules="formRules"
        label-width="84px"
        status-icon
      >
        <el-form-item label="学号" prop="studentNo">
          <el-input
            v-model="form.studentNo"
            :disabled="Boolean(editingId)"
            maxlength="32"
            show-word-limit
            placeholder="例如：S2026001"
          />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" maxlength="50" show-word-limit placeholder="请输入学生姓名" />
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="form.gender">
            <el-radio :value="0">未知</el-radio>
            <el-radio :value="1">男</el-radio>
            <el-radio :value="2">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" maxlength="11" placeholder="选填，中国大陆11位手机号" />
        </el-form-item>
        <el-form-item label="班级" prop="classId">
          <el-select v-model="form.classId" filterable placeholder="请选择班级" style="width: 100%">
            <el-option
              v-for="item in classOptions"
              :key="item.id"
              :label="`${item.className}（${item.classCode}）`"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="选修课程" prop="courseIds">
          <el-select
            v-model="form.courseIds"
            multiple
            filterable
            collapse-tags
            collapse-tags-tooltip
            placeholder="可不选课程"
            style="width: 100%"
          >
            <el-option
              v-for="item in courseOptions"
              :key="item.id"
              :label="`${item.courseName}（${item.courseCode}）`"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailVisible" title="学生详情" width="620px" append-to-body>
      <div v-loading="detailLoading">
        <dl v-if="detail" class="detail-grid">
          <div><dt>学号</dt><dd>{{ detail.studentNo }}</dd></div>
          <div><dt>姓名</dt><dd>{{ detail.name }}</dd></div>
          <div><dt>性别</dt><dd>{{ genderText(detail.gender) }}</dd></div>
          <div><dt>手机号</dt><dd>{{ detail.phone || '—' }}</dd></div>
          <div>
            <dt>班级</dt>
            <dd>{{ detail.classInfo.className }}（{{ detail.classInfo.classCode }}）</dd>
          </div>
          <div><dt>创建时间</dt><dd>{{ formatTime(detail.gmtCreate) }}</dd></div>
          <div class="detail-courses">
            <dt>已选课程</dt>
            <dd v-if="detail.courses.length">
              <el-tag v-for="course in detail.courses" :key="course.id" effect="plain">
                {{ course.courseName }}（{{ course.courseCode }}）
              </el-tag>
            </dd>
            <dd v-else>暂未选课</dd>
          </div>
        </dl>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { Plus, Refresh, Search } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import dayjs from 'dayjs'
import { API } from '@/api'
import type {
  Course,
  CreateStudentPayload,
  SchoolClass,
  StudentDetail,
  StudentListItem,
  StudentPageQuery,
  UpdateStudentPayload
} from '@/interfaces/student-management'

defineOptions({ name: 'StudentManagementView' })

const query = reactive<StudentPageQuery>({
  pageNum: 1,
  pageSize: 10,
  keyword: '',
  classId: undefined,
  courseId: undefined
})
const students = ref<StudentListItem[]>([])
const classOptions = ref<SchoolClass[]>([])
const courseOptions = ref<Course[]>([])
const total = ref(0)
const loading = ref(false)

const formVisible = ref(false)
const submitting = ref(false)
const editingId = ref<number>()
const formRef = ref<FormInstance>()
const form = reactive({
  studentNo: '',
  name: '',
  gender: 0,
  phone: '',
  classId: undefined as number | undefined,
  courseIds: [] as number[]
})

const phoneValidator = (_rule: unknown, value: string, callback: (error?: Error) => void) => {
  if (!value || /^1[3-9]\d{9}$/.test(value.trim())) callback()
  else callback(new Error('请输入正确的中国大陆11位手机号'))
}

const formRules: FormRules = {
  studentNo: [
    { required: true, message: '请输入学号', trigger: 'blur' },
    { min: 2, max: 32, message: '学号长度为2到32个字符', trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入学生姓名', trigger: 'blur' },
    { max: 50, message: '姓名不能超过50个字符', trigger: 'blur' }
  ],
  gender: [{ required: true, message: '请选择性别', trigger: 'change' }],
  phone: [{ validator: phoneValidator, trigger: 'blur' }],
  classId: [{ required: true, message: '请选择班级', trigger: 'change' }]
}

const detailVisible = ref(false)
const detailLoading = ref(false)
const detail = ref<StudentDetail>()

onMounted(async () => {
  await Promise.all([loadOptions(), loadStudents()])
})

async function loadOptions() {
  try {
    const [classes, courses] = await Promise.all([API.getClasses(), API.getCourses()])
    classOptions.value = classes
    courseOptions.value = courses
  } catch (error) {
    ElMessage.error(String(error))
  }
}

async function loadStudents() {
  loading.value = true
  try {
    const result = await API.getStudents(query)
    students.value = result.list
    total.value = result.total
  } catch (error) {
    ElMessage.error(String(error))
  } finally {
    loading.value = false
  }
}

function search() {
  query.pageNum = 1
  loadStudents()
}

function resetSearch() {
  query.keyword = ''
  query.classId = undefined
  query.courseId = undefined
  search()
}

function handleSizeChange() {
  query.pageNum = 1
  loadStudents()
}

function openCreate() {
  editingId.value = undefined
  resetForm()
  formVisible.value = true
}

async function openEdit(id: number) {
  editingId.value = id
  formVisible.value = true
  submitting.value = true
  try {
    const student = await API.getStudent(id)
    form.studentNo = student.studentNo
    form.name = student.name
    form.gender = student.gender ?? 0
    form.phone = student.phone || ''
    form.classId = student.classInfo.id
    form.courseIds = student.courses.map((item) => item.id)
  } catch (error) {
    formVisible.value = false
    ElMessage.error(String(error))
  } finally {
    submitting.value = false
  }
}

async function submitForm() {
  if (!formRef.value || !(await formRef.value.validate())) return
  submitting.value = true
  try {
    if (editingId.value) {
      const payload: UpdateStudentPayload = {
        name: form.name.trim(),
        gender: form.gender,
        phone: form.phone.trim(),
        classId: form.classId!,
        courseIds: [...form.courseIds]
      }
      await API.updateStudent(editingId.value, payload)
      ElMessage.success('学生信息修改成功')
    } else {
      const payload: CreateStudentPayload = {
        studentNo: form.studentNo.trim(),
        name: form.name.trim(),
        gender: form.gender,
        phone: form.phone.trim(),
        classId: form.classId!,
        courseIds: [...form.courseIds]
      }
      await API.createStudent(payload)
      ElMessage.success('学生新增成功')
    }
    formVisible.value = false
    await loadStudents()
  } catch (error) {
    ElMessage.error(String(error))
  } finally {
    submitting.value = false
  }
}

async function openDetail(id: number) {
  detail.value = undefined
  detailVisible.value = true
  detailLoading.value = true
  try {
    detail.value = await API.getStudent(id)
  } catch (error) {
    detailVisible.value = false
    ElMessage.error(String(error))
  } finally {
    detailLoading.value = false
  }
}

async function removeStudent(student: StudentListItem) {
  try {
    await ElMessageBox.confirm(`确定删除学生“${student.name}（${student.studentNo}）”吗？`, '删除确认', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      confirmButtonClass: 'el-button--danger',
      type: 'warning'
    })
    await API.deleteStudent(student.id)
    ElMessage.success('删除成功')
    if (students.value.length === 1 && query.pageNum > 1) query.pageNum -= 1
    await loadStudents()
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') ElMessage.error(String(error))
  }
}

function resetForm() {
  form.studentNo = ''
  form.name = ''
  form.gender = 0
  form.phone = ''
  form.classId = undefined
  form.courseIds = []
  formRef.value?.clearValidate()
}

function genderText(gender: number | null) {
  return gender === 1 ? '男' : gender === 2 ? '女' : '未知'
}

function genderTagType(gender: number | null) {
  return gender === 1 ? 'primary' : gender === 2 ? 'danger' : 'info'
}

function formatTime(value: string) {
  return value ? dayjs(value).format('YYYY-MM-DD HH:mm') : '—'
}
</script>

<style lang="scss" scoped>
.muted-code {
  margin-left: 6px;
  color: #9aa1ac;
  font-size: 12px;
}
</style>
