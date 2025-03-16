import {createRouter, createWebHashHistory} from "vue-router";
import {unauthorized} from "@/net/index.js";
const router = createRouter({
    history: createWebHashHistory(import.meta.env.BASE_URL),
    routes: [
        {
            path: '/',
            name: 'welcome',
            component: () => import("@/views/WelcomeView.vue"),  // 组件
            // 子路由
            children:[
                {
                    path: '',
                    name: 'welcome-login',  // 登录页面
                    component: ()=>import("@/views/welcome/LoginPage.vue")
                },
                {
                    path: 'register',
                    name: 'welcome-register',  // 注册页面
                    component: ()=>import("@/views/welcome/RegisterPage.vue")
                },
                {
                    path: 'reset',
                    name: 'welcome-reset',  // 重置密码页面
                    component: ()=>import("@/views/welcome/ResetPage.vue")
                },
            ]
        },
        {
            path: '/index',
            name: 'index',  // 系统首页
            component: () => import("@/views/IndexView.vue"),  // 组件
            // 子路由
            children:[
            ]
        }
    ]
})

router.beforeEach((to, from, next) => {
    const isUnauthorized = unauthorized()
    // 访问名为 ‘welcome-’ 下的url，登录的情况下，直接跳转/index
    if (to.name.startsWith('welcome') && !isUnauthorized){
        next('/index')
    // 即将进入的路由以index开头，且未登录，请求 /welcome下的页面跳转至 /
    } else if (to.fullPath.startsWith('/index') && isUnauthorized) {
        next('/')
    } else {
        next()  // 正常情况
    }
})

export default router;