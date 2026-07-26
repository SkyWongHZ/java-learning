import http from '@/utils/http-tool'
import type { ILoginRequest } from '@/interfaces/request/IPasswordLoginRequest'
import type { ILogin } from '@/interfaces/response/ILogin'
import type {
  ClassPayload,
  CoursePayload,
  CreateStudentPayload,
  CurrentUser,
  PageResult,
  SchoolClass,
  Course,
  StudentDetail,
  StudentListItem,
  StudentPageQuery,
  UpdateStudentPayload
} from '@/interfaces/student-management'

export class API {
  static login(params: ILoginRequest) {
    return http.post<ILogin>('/api/v1/auth/login', params)
  }

  static currentUser() {
    return http.get<CurrentUser>('/api/v1/auth/me')
  }

  static logout() {
    return http.post<null>('/api/v1/auth/logout')
  }

  static getStudents(params: StudentPageQuery) {
    return http.get<PageResult<StudentListItem>>('/api/v1/students', params)
  }

  static getStudent(id: number) {
    return http.get<StudentDetail>(`/api/v1/students/${id}`)
  }

  static createStudent(params: CreateStudentPayload) {
    return http.post<StudentDetail>('/api/v1/students', params)
  }

  static updateStudent(id: number, params: UpdateStudentPayload) {
    return http.put<StudentDetail>(`/api/v1/students/${id}`, params)
  }

  static deleteStudent(id: number) {
    return http.delete<null>(`/api/v1/students/${id}`)
  }

  static getCourses(keyword?: string) {
    return http.get<Course[]>('/api/v1/courses', { keyword })
  }

  static createCourse(params: CoursePayload) {
    return http.post<Course>('/api/v1/courses', params)
  }

  static updateCourse(id: number, params: CoursePayload) {
    return http.put<Course>(`/api/v1/courses/${id}`, params)
  }

  static deleteCourse(id: number) {
    return http.delete<null>(`/api/v1/courses/${id}`)
  }

  static getClasses(keyword?: string) {
    return http.get<SchoolClass[]>('/api/v1/classes', { keyword })
  }

  static createClass(params: ClassPayload) {
    return http.post<SchoolClass>('/api/v1/classes', params)
  }

  static updateClass(id: number, params: ClassPayload) {
    return http.put<SchoolClass>(`/api/v1/classes/${id}`, params)
  }

  static deleteClass(id: number) {
    return http.delete<null>(`/api/v1/classes/${id}`)
  }
}
