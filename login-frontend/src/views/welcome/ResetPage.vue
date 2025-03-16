<script setup>
import {computed, reactive, ref} from "vue"
import router from "@/router/index.js";
import {User,Lock,Message,EditPen } from "@element-plus/icons-vue";
import {get, post} from "@/net/index.js";
import {ElMessage} from "element-plus";

const active = ref(0)
// 计时器
const coldTime = ref(0)
//验证完整表单是否有问题
const formRef = ref()

const form = reactive({
  email: '',
  code: '',
  password:'',
  password_repeat:''
})

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
  email:[
    {required: true, message:'请输入邮件地址',trigger:'blur'},
    {type:'email', message:'请输入合法的电子邮件地址', trigger:['blur','change']}
  ],
  code:[
    {required: true, message:'请输入获取的邮件',trigger:'blur'},
  ],
  password:[
    {required: true, message:'请输入密码',trigger:'blur'},
    {min: 6, max:20, message:'密码的长度必须在6-20个字符之间', trigger:['blur','change']}
  ],
  password_repeat:[
    {validator: validatePassword, trigger:['blur','change']}
  ]
}

function askCode() {
  if (isEmailValid) {
    coldTime.value = 60;
    get(`/api/auth/ask-code?email=${form.email}&type=register`, () => {
      ElMessage.success(`验证码已发送到邮箱：${form.email}，请注意查收`)
      setInterval(() => coldTime.value--, 1000)
    }, () => {
      ElMessage.warning(message)
      coldTime.value = 0
    })
  } else {
    ElMessage.warning("请输入正确的电子邮件");
  }
}

const isEmailValid = computed(() => /^[\w.-]+@[\w.-]+\.\w+$/.test(form.email))

function confirmReset(){
  formRef.value.validate((valid) => {
    if(valid) {
      post('/api/auth/reset-confirm',{
        email: form.email,
        code: form.code,
      },() => active.value++)
    }
  })
}

function doReset(){
  formRef.value.validate((valid) => {
    if(valid) {
      post('/api/auth/reset-password',{...form}, ()=> {
        ElMessage.success("密码重置成功，请重新登录")
        router.push('/')
      })
    }
  })
}
</script>

<template>
  <div style="text-align: center; margin: 0 35px;">
    <div style="margin-top: 150px">
      <el-steps :active="active" finish-status="success" align-center>
        <el-step title="验证电子邮件"/>
        <el-step title="重置密码"/>
      </el-steps>
    </div>
    <div v-if="active === 0">
      <div style="margin-top: 80px">
        <div style="font-size: 25px; font-weight: bold">重置密码</div>
        <div style="font-size: 14px; color: grey; margin-top: 20px">请输入需要重置密码的电子邮件地址</div>
      </div>
      <div style="margin-top: 50px">
      <el-form :model="form" :rules="rule" ref="formRef">
        <el-form-item prop="email">
          <el-input v-model="form.email" type="email" placeholder="电子邮件地址" style="font-size: 16px; height: 40px">
            <el-icon><Message/></el-icon>
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
        <el-button  @click="confirmReset" type="primary" style="width: 270px; height: 50px; font-size: 18px" round>
          开始重置密码
        </el-button>
      </div>
    </div>
    <div v-if="active === 1">
      <div style="margin-top: 80px">
        <div style="font-size: 25px; font-weight: bold">重置密码</div>
        <div style="font-size: 14px; color: grey; margin-top: 20px">请输入填写新密码，务必牢记防止丢失。</div>
      </div>
      <div style="margin-top: 50px">
        <el-form :model="form" :rules="rule" ref="formRef">
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
        </el-form>
      </div>
      <div style="margin-top: 20px">
        <el-button  @click="doReset" type="primary" style="width: 270px; height: 50px; font-size: 18px" round>
          立即重置密码
        </el-button>
      </div>
    </div>
    <el-button @click="router.push('/')" type="primary" style="width: 270px; height: 50px; font-size: 18px; margin-top: 20px"  text bg round>
      返回登录
    </el-button>
  </div>

</template>

<style scoped>

</style>
