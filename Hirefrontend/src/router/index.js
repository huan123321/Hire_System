import VueRouter from "vue-router"
import Vue from "vue";
import LoginView from "@/components/LoginView.vue";
import RegisTer from "@/components/RegisTer.vue";
import HomePage from "@/components/HomePage/HomePage.vue";

Vue.use(VueRouter);

const ComPany = () => import("@/components/ComPany.vue");
const PerSon = () => import("@/components/PerSon.vue");
const HrPerson = () => import("@/components/HrPage/HrPerson.vue");
const EditResume = () => import("@/components/EditResume.vue");
const Position = () => import("@/components/Position.vue");
const Certification = () => import("@/components/Certification.vue");
const CompanyDetail = () => import("@/components/CompanyDetail.vue");
const JobDetail = () => import("@/components/JobDetail.vue");
const HrHomePage = () => import("@/components/HrPage/HrHomePage.vue");
const HrCompany = () => import("@/components/HrPage/HrCompany.vue");
const HrPosition = () => import("@/components/HrPage/HrPosition.vue");
const ChatBox = () => import("@/components/ChatBox.vue");
const ResumeDetail = () => import("@/components/ResumeDetail.vue");
const MessageCenter = () => import("@/components/MessageCenter.vue");
const InterviewManagement = () => import("@/components/InterviewManagement.vue");
const AiChat = () => import("@/components/AiChat.vue");

Vue.use(VueRouter);

const routes = [
    {
        path: '/',
        name: 'LoginView',
        component: LoginView,
    },
    {
        path: '/RegisTer',
        name: 'RegisTer',
        component: RegisTer,
    },
    {
        path: '/Position',
        name: 'Position',
        component: Position,
    },
    {
        path: '/ComPany',
        name: 'ComPany',
        component: ComPany
    },
    {
        path: '/HomePage',
        name: 'HomePage',
        component: HomePage
    },
    {
        path: '/PerSon',
        name: 'Person',
        component: PerSon
    },
    {
        path: '/HrPerson',
        name: 'HrPerson',
        component: HrPerson
    },
    {
        path: '/EditResume',
        name: 'EditResume',
        component: EditResume
    },
    {
        path: '/Certification',
        name: 'Certification',
        component: Certification
    },
    {
        path: '/CompanyDetail/:id',
        name: 'CompanyDetail',
        component: CompanyDetail
    },
    {
        path: '/JobDetail/:id',
        name: 'JobDetail',
        component: JobDetail
    },
    {
        path: '/HrHomePage',
        name: 'HrHomePage',
        component: HrHomePage
    },
    {
        path: '/HrCompany',
        name: 'HrCompany',
        component: HrCompany
    },
    {
        path: '/HrPosition',
        name: 'HrPosition',
        component: HrPosition
    },
    {
        path: '/Chat',
        name: 'Chat',
        component: ChatBox,
        meta: { requiresAuth: true }
    },
    {
        path: '/ResumeDetail/:userId',
        name: 'ResumeDetail',
        component: ResumeDetail,
        meta: { requiresAuth: true }
    },
    {
        path: '/MessageCenter',
        name: 'MessageCenter',
        component: MessageCenter,
        meta: { requiresAuth: true }
    },
    {
        path: '/InterviewManagement',
        name: 'InterviewManagement',
        component: InterviewManagement,
        meta: { requiresAuth: true }
    },
    {
        path: '/AiChat',
        name: 'AiChat',
        component: AiChat,
        meta: { requiresAuth: true }
    },
]

const router = new VueRouter({
    mode: 'history',
    routes
})

// ========== 路由守卫配置 ==========

// 白名单：不需要登录就能访问的页面
const whiteList = [
    '/',           // 登录页
    '/RegisTer',   // 注册页
    '/Position',   // 职位列表页（允许未登录查看）
    '/ComPany',    // 公司列表页（允许未登录查看）
    '/CompanyDetail',  // 注意：这是路径前缀，需要特殊处理
    '/JobDetail'       // 职位详情页
]

// HR专属页面
const hrOnlyPages = ['/HrHomePage', '/HrCompany', '/HrPosition', '/HrPerson']

// 角色定义
const ROLES = {
    JOB_SEEKER: 0,  // 求职者
    HR: 1,          // 公司 HR
    ADMIN: 2        // 管理员
}

// 角色对应的首页
const roleHomeMap = {
    [ROLES.JOB_SEEKER]: '/HomePage',
    [ROLES.HR]: '/HrHomePage',
    [ROLES.ADMIN]: '/HomePage'  // 管理员暂时也用求职者首页
}

router.beforeEach((to, from, next) => {
    //日志
    console.log('路由跳转:', from.path, '→', to.path)
    console.log('用户信息:', sessionStorage.getItem('user'))
    
    // 防止无限重定向：检查是否正在重定向
    if (to.meta.isRedirecting) {
        next()
        return
    }
    
    // 1. 获取用户信息
    const userStr = sessionStorage.getItem('user')
    const user = userStr ? JSON.parse(userStr) : null

    // 2. 白名单页面直接放行（不需要登录）
    if (whiteList.includes(to.path)) {
        next()
        return
    }

    // 3. 未登录 → 跳转到登录页
    if (!user) {
        // 保存登录后要跳转的页面
        sessionStorage.setItem('redirectPath', to.fullPath)
        next('/')
        return
    }

    // 4. 已登录但访问登录页 → 跳转到对应首页
    if (to.path === '/') {
        const homePath = roleHomeMap[user.role] || '/HomePage'
        // 避免重复重定向
        if (from.path !== homePath) {
            next({ path: homePath, meta: { isRedirecting: true } })
        } else {
            next()
        }
        return
    }

    // 5. 已登录且访问 /HomePage → 根据角色处理
    if (to.path === '/HomePage') {
        if (user.role === ROLES.HR) {
            // HR 用户应该去 HR 首页，避免重复重定向
            if (from.path !== '/HrHomePage') {
                next({ path: '/HrHomePage', meta: { isRedirecting: true } })
            } else {
                next()
            }
            return
        }
        // 求职者和管理员正常访问
        next()
        return
    }

    // 6. 权限检查：HR 不能访问求职者专属页面（可选）
    const seekerOnlyPages = ['/EditResume', '/Certification']
    if (user.role === ROLES.HR && seekerOnlyPages.includes(to.path)) {
        // HR 访问求职者页面，重定向到 HR 首页
        if (from.path !== '/HrHomePage') {
            next({ path: '/HrHomePage', meta: { isRedirecting: true } })
        } else {
            next()
        }
        return
    }
    
    // 6.5. HR 访问 /Position 时重定向到 /HrPosition
    if (to.path === '/Position' && user.role === ROLES.HR) {
        if (from.path !== '/HrPosition') {
            next({ path: '/HrPosition', meta: { isRedirecting: true } })
        } else {
            next()
        }
        return
    }
    
    // 6.6. 求职者访问 /HrPosition 时重定向到 /Position
    if (to.path === '/HrPosition' && user.role === ROLES.JOB_SEEKER) {
        if (from.path !== '/Position') {
            next({ path: '/Position', meta: { isRedirecting: true } })
        } else {
            next()
        }
        return
    }

    // 7. 求职者不能访问 HR 专属页面
    if (user.role === ROLES.JOB_SEEKER && hrOnlyPages.includes(to.path)) {
        if (from.path !== '/HomePage') {
            next({ path: '/HomePage', meta: { isRedirecting: true } })
        } else {
            next()
        }
        return
    }
    
    // 7.5. HR 访问求职者个人中心 /PerSon 时重定向到 /HrPerson
    if (to.path === '/PerSon' && user.role === ROLES.HR) {
        if (from.path !== '/HrPerson') {
            next({ path: '/HrPerson', meta: { isRedirecting: true } })
        } else {
            next()
        }
        return
    }
    
    // 7.6. 求职者访问 HR 个人中心 /HrPerson 时重定向到 /PerSon
    if (to.path === '/HrPerson' && user.role === ROLES.JOB_SEEKER) {
        if (from.path !== '/PerSon') {
            next({ path: '/PerSon', meta: { isRedirecting: true } })
        } else {
            next()
        }
        return
    }

    // 8. 放行
    next()
})

export default router
