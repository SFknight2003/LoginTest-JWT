<script setup>
import {logout} from '@/net/index.js'
import router from "@/router/index.js";
function userLogout(){
      logout(() => {router.push('/')})
}
</script>

<template>
  <div>
    <button @click="userLogout">退出登录</button>
  </div>
</template>

<style scoped>

</style>