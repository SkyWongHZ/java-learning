export interface CurrentUser {
  id: number
  username: string
  displayName: string
  systemType: number
}

export interface PageResult<T> {
  pageNum: number
  pageSize: number
  total: number
  pages: number
  list: T[]
  boolLastPage: boolean
}

export interface ClassSimple {
  id: number
  classCode: string
  className: string
}

export interface CourseSimple {
  id: number
  courseCode: string
  courseName: string
}

export interface SchoolClass extends ClassSimple {
  gmtCreate: string
  gmtModify: string
}

export interface Course extends CourseSimple {
  gmtCreate: string
  gmtModify: string
}

export interface StudentListItem {
  id: number
  studentNo: string
  name: string
  gender: number | null
  phone: string | null
  classInfo: ClassSimple
  gmtCreate: string
  gmtModify: string
}

export interface StudentDetail extends StudentListItem {
  courses: CourseSimple[]
}

export interface StudentPageQuery {
  pageNum: number
  pageSize: number
  keyword?: string
  classId?: number
  courseId?: number
}

export interface CreateStudentPayload {
  studentNo: string
  name: string
  gender: number
  phone?: string
  classId: number
  courseIds?: number[]
}

export interface UpdateStudentPayload {
  name: string
  gender: number
  phone?: string
  classId: number
  courseIds: number[]
}

export interface CoursePayload {
  courseCode: string
  courseName: string
}

export interface ClassPayload {
  classCode: string
  className: string
}
