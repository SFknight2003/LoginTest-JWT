import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import axios from 'axios'
import 'element-plus/theme-chalk/dark/css-vars.css'

const app = createApp(App)

axios.defaults.baseURL = 'http://localhost:8080'    // 默认后端url
app.use(router)
app.mount('#app')