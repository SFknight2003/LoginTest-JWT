<script setup>
import {computed, reactive, ref} from "vue";
import {User,Lock,Message,EditPen } from "@element-plus/icons-vue";
import router from "@/router/index.js";
import {get, post} from "@/net";
import {ElMessage} from 'element-plus'

// 计时器
const coldTime = ref(0)
//验证完整表单是否有问题
const formRef = ref()
//注册表单
const  form = reactive({
  username: '',
  password: '',
  password_repeat: '',
  email: '',
  code: ''
})

const validateUsername = (rule, value, callback) => {
  if(value === '') {
    callback(new Error('请输入用户名'))
  } else if( !/^[a-zA-Z0-9\u4e00-\u9fa5]+$/.test(value) ){
    callback(new Error('用户名不能包含特殊字符，只能是中/英文'))
  } else {
    callback()
  }
}
const validatePassword = (rule, value, callback) => {
  if ( value === ''){
    callback(new Error("请再次输入密码"))
  } else if (value !== form.password){
    callback(new Error("两次密码不一致"))
  } else {
    callback()
  }
}

const rule = {
  username:[
    { validator: validateUsername, trigger: ['blur','change'] },
  ],
  password:[
    {required: true, message:'请输入密码',trigger:'blur'},
    {min: 6, max:20, message:'密码的长度必须在6-20个字符之间', trigger:['blur','change']}
  ],
  password_repeat:[
    {validator: validatePassword, trigger:['blur','change']}
  ],
  email:[
    {required: true, message:'请输入邮件地址',trigger:'blur'},
    {type:'email', message:'请输入合法的电子邮件地址', trigger:['blur','change']}
  ],
  code:[
    {required: true, message:'请输入获取的邮件',trigger:'blur'},
  ]
}

function askCode(){
  if(isEmailValid) {
    coldTime.value=60;
    get(`/api/auth/ask-code?email=${form.email}&type=register`,()=> {
      ElMessage.success(`验证码已发送到邮箱：${form.email}，请注意查收`)
      setInterval(()=>coldTime.value--, 1000)
    },()=>{
      ElMessage.warning(message)
      coldTime.value=0
    })
  } else {
    ElMessage.warning("请输入正确的电子邮件");
  }
}

const isEmailValid = computed(() => /^[\w.-]+@[\w.-]+\.\w+$/.test(form.email) )

function register(){
  formRef.value.validate((valid) => {
    if(valid) {
      post("/api/auth/register",{...form},()=>{
        ElMessage.success("注册成功，欢迎加入我们")
        router.push("/")
      })
    } else {
      ElMessage.warning("请完整填写注册内容")
    }
  })
}
</script>

<template>
  <div style="text-align: center; margin: 0 35px">
    <div style="margin-top: 200px">
      <div style="font-size: 30px; font-weight: bold">注&nbsp;册&nbsp;新&nbsp;用&nbsp;户</div>
      <div style="font-size: 16px;  color:#595959; margin-top: 15px">欢迎注册学习平台，请在下面填写相关信息</div>
    </div>
    <div style="margin-top: 30px">
      <el-form :model="form" :rules="rule" ref="formRef">
        <el-form-item prop="username">
          <el-input v-model="form.username" maxlength="10" type="text" placeholder="用户名" style="font-size: 16px; height: 40px" >
            <template #prefix>
              <el-icon><User/></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" maxlength="20" type="password" placeholder="密码" style="font-size: 16px; height: 40px" show-password>
            <template #prefix>
              <el-icon><Lock/></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item prop="password_repeat">
          <el-input v-model="form.password_repeat" maxlength="20" type="password" placeholder="请再次输入密码" style="font-size: 16px; height: 40px" show-password>
            <template #prefix>
              <el-icon><Lock/></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item prop="email">
          <el-input v-model="form.email" type="email" placeholder="电子邮件地址" style="font-size: 16px; height: 40px">
            <template #prefix>
              <el-icon><Message/></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item prop="code">
          <el-row :gutter="15" style="width: 100%">  <!-- 同行间隔 -->
            <el-col :span="17">
              <el-input v-model="form.code" maxlength="10" type="text" placeholder="请输入验证码" style="font-size: 16px; height: 40px">
                <template #prefix>
                  <el-icon><EditPen/></el-icon>
                </template>
              </el-input>
            </el-col>
            <el-col :span="5">
              <el-button @click="askCode" :disabled="!isEmailValid || coldTime>0" type="primary" style="font-size: 14px; height: 38px" plain>
                {{ coldTime > 0 ? `请稍后 ${coldTime}秒` : '获取验证码'}}
              </el-button>
            </el-col>
          </el-row>
        </el-form-item>
      </el-form>
    </div>
    <div style="margin-top: 20px">
        <el-button  @click="register" type="primary" style="width: 270px; height: 50px; font-size: 18px" round>注&nbsp;&nbsp;册</el-button>
    </div>
    <div style="margin-top: 20px; ">
      <span style="font-size: 14px; line-height: 15px; color: grey">已有账号?</span>&nbsp;
      <el-link style="translate: 0 -2px" @click="router.push('/')">立即登录</el-link>
    </div>
  </div>

</template>

<style scoped>
.el-divider {
  border-color: white; /*设置分隔线颜色为白色*/
}
</style>