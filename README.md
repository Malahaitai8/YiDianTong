# 医点通 - 校园在线挂号系统

本项目为“医点通”的 Git 仓库，目前包含小程序前端。

## 1. 小程序 (client-mini-app)

`client-mini-app` 文件夹是本项目的小程序前端，基于 uni-app 和 Vue 2 构建。

### 技术栈

* 框架: `Vue 2.x`
* 开发: `uni-app`
* UI 库: `Vant Weapp` (已按需引入)
* 状态管理: `Vuex 3.x` (已配置)
* HTTP: 已封装 `utils/request.js` (自动处理 Token 和错误)
* 规范: `ESLint` (待配置)

### 快速开始

1.  **进入小程序目录**:
    ```bash
    cd client-mini-app
    ```

2.  **安装依赖**:
    ```bash
    npm install
    ```

3.  **运行到微信开发者工具**:
    ```bash
    npm run dev:mp-weixin
    ```
    (然后在 HBuilderX 或微信开发者工具中打开 `client-mini-app/dist/dev/mp-weixin` 目录)

### 目录与开发规范

* **API 请求**: 统一放在 `src/api/` 目录下 (如 `user.js`, `order.js`)。
* **状态管理**: Token 和用户信息已配置在 `src/store/modules/user.js` 中。
* **HTTP 调用**:
    * 请勿使用 `uni.request`。
    * 应引入 `import request from '@/utils/request.js'`。
    * 使用 `request({ url: '/user/login', method: 'POST', data: { ... } })`。
* **路由白名单**: 需要登录才能访问的页面，请在 `src/main.js` 的 `uni.addInterceptor` 中配置。
* **环境变量**: API 地址在 `.env.development` 中配置。
