# AI 能力

## 能力

- OpenAI、Claude、Google 及兼容接口；支持 Chat Completions、Responses API 和流式输出。
- 模型参数、推理等级、自定义 Header/Body、独立 OCR/压缩接口。
- 本地工具：时间、JavaScript、剪贴板、TTS、询问用户、用量统计、应用锁、天气、朋友圈。
- MCP 工具、联网搜索、长期记忆工具、技能工具和提示词转换链。

## 核心入口

- 通用协议：`ai/core/`、`ai/provider/`
- 供应商实现：`ai/provider/providers/`
- 请求编排：`app/.../data/ai/GenerationHandler.kt`、`service/ChatService.kt`
- 本地工具：`app/.../data/ai/tools/LocalTools.kt`
- MCP：`app/.../data/ai/mcp/`
- 输入/输出转换：`app/.../data/ai/transformers/`

## 修改提示

- 新供应商放 `ai/provider/providers/`，通过 `ProviderManager` 注册；不要在 UI 判断供应商类型。
- 新应用工具放 `app/data/ai/tools/`，明确参数 schema、审批需求和注入条件。
- 修改系统上下文时检查压缩摘要、会话独立提示词、记忆、技能、模式注入和朋友圈上下文的顺序。
- 记录请求问题看 `AIRequestInterceptor.kt`、`RequestLoggingInterceptor.kt` 和日志页。
