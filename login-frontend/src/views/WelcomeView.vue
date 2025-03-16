<script setup>

</script>

<template>
  <div style="width: 100vw; height: 100vh; overflow: hidden; display: flex">
    <!-- 左侧区域 -->
    <div style="flex: 1; background-color: black">
<!--      <el-image style="width:100%; height: 100%" fit="cover"-->
<!--          src="../../assets/images/WelcomeBack.jpg"/>-->
      <el-image style="position: absolute; top: 0; left: 0; right: 0; bottom: 0;" fit="cover"
                src="../../assets/images/WelcomeBack.jpg"/>
    </div>
    <div class="welcome-title">
      <div style="font-size: 30px; font-weight: bold">欢迎来到学习平台</div>
      <div style="font-size: 20px; margin-top: 5px">在这里我们可以密切交流一同学习</div>
    </div>
    <!-- 右侧区域 -->
    <div class="right-card">
      <router-view v-slot="{ Component }">
        <transition name="el-zoom-in-center" mode="out-in">
          <component :is="Component"/>
        </transition>
      </router-view>
    </div>
  </div>
</template>

<style scoped>
.welcome-title{
  position: absolute;
  bottom: 30px;
  left: 30px;
  color: white;
  text-shadow: 0 0 10px black;
}
/* 右侧登录框固定不变 */
.right-card{
  width: 400px;
  z-index: 1;
  background-color: rgba(255, 255, 255, 0.8);
}
</style>