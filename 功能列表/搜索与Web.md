# 搜索与 Web

## 能力

- 多种搜索服务和网页抓取，统一暴露为 AI 工具。
- Android 内启动 Ktor Web 服务，局域网或 localhost 访问聊天数据。
- React Web 客户端支持会话、模型/工具选择、Markdown 和响应式布局。

## 核心入口

- 搜索统一接口：`search/SearchService.kt`
- 各搜索实现：`search/*Service.kt`
- AI 搜索工具：`app/.../data/ai/tools/SearchTools.kt`
- Android Web 编排：`app/.../web/WebServerManager.kt`、`service/WebServerService.kt`
- Ktor 模块：`web/`
- React：`web-ui/app/`，API 在 `web-ui/app/services/api.ts`

## 构建规则

- `web:preBuild` 会执行 `web-ui` 的 `pnpm run build`。
- 静态产物复制到 `web/src/main/resources/static`。
- 修改 Web API 时同时检查 Kotlin DTO 和 TypeScript `types/`。
