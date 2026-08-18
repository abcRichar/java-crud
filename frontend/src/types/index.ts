// Common API types
export interface ApiResponse<T = unknown> {
  code: number
  message: string
  data: T
}

export interface PageResult<T> {
  records: T[]
  total: number
  page: number
  pageSize: number
}

// Auth types
export interface LoginRequest {
  username: string
  password: string
}

export interface LoginVO {
  accessToken: string
  refreshToken: string
  expiresIn: number
  user: UserInfoVO
}

export interface UserInfoVO {
  id: number
  username: string
  nickname: string
  avatar: string
  email: string
  phone: string
  roles: string[]
  permissions: string[]
}

// User types
export interface UserVO {
  id: number
  username: string
  nickname: string
  avatar: string
  email: string
  phone: string
  status: number
  createdAt: string
  updatedAt: string
  roleIds: number[]
}

export interface UserCreateDTO {
  username: string
  nickname: string
  password: string
  email?: string
  phone?: string
  avatar?: string
  roleIds?: number[]
}

export interface UserUpdateDTO {
  id: number
  nickname?: string
  email?: string
  phone?: string
  avatar?: string
  status?: number
  roleIds?: number[]
}

// Role types
export interface RoleVO {
  id: number
  name: string
  code: string
  remark: string
  status: number
  createdAt: string
  updatedAt: string
  menuIds: number[]
}

export interface RoleCreateDTO {
  name: string
  code: string
  remark?: string
  menuIds?: number[]
}

export interface RoleUpdateDTO {
  id: number
  name?: string
  code?: string
  remark?: string
  status?: number
  menuIds?: number[]
}

// Menu types
export interface MenuVO {
  id: number
  parentId: number
  name: string
  title: string
  path: string
  component: string
  icon: string
  type: string // DIRECTORY / MENU / BUTTON
  permission: string
  sort: number
  visible: number
  status: number
  children?: MenuVO[]
}

export interface MenuCreateDTO {
  parentId?: number
  name: string
  title: string
  path?: string
  component?: string
  icon?: string
  type: string
  permission?: string
  sort?: number
  visible?: number
  status?: number
}

export interface MenuUpdateDTO {
  id: number
  parentId?: number
  name?: string
  title?: string
  path?: string
  component?: string
  icon?: string
  type?: string
  permission?: string
  sort?: number
  visible?: number
  status?: number
}

// Category types
export interface CategoryVO {
  id: number
  parentId: number
  name: string
  slug: string
  sort: number
  status: number
  children?: CategoryVO[]
}

export interface CategoryCreateDTO {
  parentId?: number
  name: string
  slug?: string
  sort?: number
  status?: number
}

export interface CategoryUpdateDTO {
  id: number
  parentId?: number
  name?: string
  slug?: string
  sort?: number
  status?: number
}

// Article types
export interface ArticleVO {
  id: number
  title: string
  summary: string
  content: string
  cover: string
  categoryId: number
  categoryName: string
  authorId: number
  authorName: string
  status: string
  viewCount: number
  likeCount: number
  seoTitle: string
  seoDescription: string
  publishedAt: string
  createdAt: string
  updatedAt: string
}

export interface ArticleCreateDTO {
  title: string
  summary?: string
  content?: string
  cover?: string
  categoryId?: number
  seoTitle?: string
  seoDescription?: string
  status?: string
}

export interface ArticleUpdateDTO {
  id: number
  title: string
  summary?: string
  content?: string
  cover?: string
  categoryId?: number
  seoTitle?: string
  seoDescription?: string
}

// Notice types
export interface NoticeVO {
  id: number
  title: string
  content: string
  type: string
  status: string
  createdBy: number
  createdAt: string
  updatedAt: string
}

export interface NoticeCreateDTO {
  title: string
  content: string
  type?: string
  userIds?: number[]
}

export interface NoticeUpdateDTO {
  id: number
  title: string
  content: string
  type?: string
  userIds?: number[]
}
