<template>
  <router-view />
</template>

<script setup>
// App.vue 作为根组件，只需要渲染路由视图
</script>

<style>
/* 全局样式重置 */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body {
  height: 100%;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

#app {
  height: 100%;
}

/* Element Plus 样式覆盖 */
.el-menu {
  border-right: none !important;
}

.el-menu-item:hover {
  background-color: rgba(64, 158, 255, 0.1) !important;
}

.el-menu-item.is-active {
  background-color: rgba(64, 158, 255, 0.2) !important;
}
</style>
