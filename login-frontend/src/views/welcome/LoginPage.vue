<script setup>
import {User,Lock} from '@element-plus/icons-vue'
import {reactive,ref} from "vue";
import {login} from "@/net"
import router from "@/router/index.js";
const formRef = ref()

const form = reactive({
  username: '',
  password: '',
  remember: false,
})
const rule= {
  username: [
    {required: true, message:"请输入用户名"}
  ],
  password: [
    {required: true, message:"请输入用户密码"}
  ]
}
function userLogin(){
  formRef.value.validate((valid) => {
    if(valid) {
      login(form.username,form.password,form.remember,()=>{
        router.push('/index')   // 跳转至首页面
      })
    }
  })
}
</script>
<template>
  <div style="text-align: center; margin: 0 35px">  <!-- 字体居中 -->
    <div style="margin-top: 200px">
      <div style="font-size: 30px; font-weight: bold">登&nbsp;&nbsp;录</div>
      <div style="font-size:16px; color:#595959; margin-top: 20px">请先输入用户名和密码进行登录</div>
    </div>
    <!--  输入框  -->
    <div style="margin-top: 20px">
      <el-form :model="form" :rules="rule" ref="formRef">
        <el-form-item prop="username">
          <el-input v-model="form.username" maxlength="15" type="text" placeholder="用户名/邮箱" style="font-size: 16px; height: 40px" clearable>
            <template #prefix>
              <el-icon><User /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" maxlength="15" type="password" placeholder="用户密码"  style="margin-top: 10px; font-size: 16px; height: 40px" show-password clearable>
            <template #prefix>
              <el-icon><Lock /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <!--   记住我/忘记密码   -->
        <el-row style="margin-top: 15px;">
          <!--    记住我勾选框   -->
          <el-col :span="12" style="text-align: left">
            <el-form-item>
              <el-checkbox v-model="form.remember" label="记住我"/>
            </el-form-item>
          </el-col>
          <!--    忘记密码    -->
          <el-col :span="12" style="text-align: right">
            <el-form-item>
              <el-link @click="router.push('/reset')">忘记密码</el-link>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </div>
    <!--   登录注册按钮   -->
    <div>
      <el-button type="primary" @click="userLogin" style="width: 270px; height: 50px; font-size: 18px" round>立即登陆</el-button>
    </div>
    <!--   分割线   -->
    <el-divider>没有账号</el-divider>
    <div>
      <el-button @click="router.push('/register')" type="primary" style="width: 270px; height: 50px; font-size: 18px;" text bg round>立即注册</el-button>
    </div>
  </div>
</template>
<style scoped>
/* 分割线 */
.el-divider {
  border-color: white; /*设置分隔线颜色为白色*/
}
</style>
