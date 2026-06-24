<div align="center">
  <img src="docs/icon.png" alt="App 图标" width="100" />
  <h1>RikkaHub Auto Compress</h1>

一个原生Android LLM 聊天客户端，支持切换不同的供应商进行聊天 🤖💬

[English](README.md) | [繁體中文](README_ZH_TW.md) | 简体中文

点击链接加入群聊 👉 [【RikkaHub】](https://qm.qq.com/q/I8MSU0FkOu)

</div>

> [!IMPORTANT]
> 这是基于 [RikkaHub](https://github.com/rikkahub/rikkahub) 的**非官方二次开发版本**，当前公开源码基于 RikkaHub `2.2.5` 代码线，并合入部分后续修复及下述二创功能；本项目不代表 RikkaHub 官方版本。具体可下载版本以本仓库的 [Releases](https://github.com/innna327-source/rikkahub-auto-compress/releases) 页面为准。

<div align="center">
  <img src="docs/img/chat.png" alt="Chat Interface" width="150" />
  <img src="docs/img/desktop.png" alt="Models Picker" width="450" />
</div>


## 🚀 下载

🔗 **[前往 GitHub Releases 下载 APK](https://github.com/innna327-source/rikkahub-auto-compress/releases)**

如果只是安装应用，请不要点击 `Code → Download ZIP`，那里下载的是源代码，不是 APK。

当前发布包为 `arm64-v8a` Debug APK。安装前请先备份应用数据。由于本二创版本与官方版本可能使用不同签名，Android 可能不允许直接覆盖安装；若提示签名冲突，请务必先在原应用内导出备份，再考虑卸载旧版本。

需要官方版本时，请前往 [RikkaHub 上游项目](https://github.com/rikkahub/rikkahub)。

## 🌿 本二创主要新增功能

- **自动滚动摘要与上下文压缩**：保留最近消息，自动压缩较早的可见对话；摘要可查看、编辑，并可设置目标长度。
- **长对话分块压缩**：超长历史和已有摘要会按容量分块处理，避免一次请求塞入过多内容。
- **独立压缩 API 与模型**：压缩任务可单独配置 OpenAI 兼容地址、API Key、模型、Chat Completions 路径或 Responses API，不占用普通聊天模型配置。
- **独立 OCR API 与模型**：OCR 可以使用单独的 OpenAI 兼容接口和模型，不影响正常聊天提供商。
- **分段 TTS 播放**：支持按段落朗读回复，并提供仅朗读引用、仅朗读英文和生成后自动播放等选项。
- **使用统计与提醒**：提供本地应用使用统计和可配置的使用时长提醒。

除 Release 说明特别标注外，本版本仍保留下面列出的 RikkaHub 上游功能。


## 💖 赞助商

<div align="center">
  <img src="app/src/main/assets/icons/aihubmix-color.svg" alt="Aihubmix" width="50" />
  <p style="font-size: 16px; font-weight: bold;">Aihubmix</p>
  <p style="font-size: 14px;">感谢 <a href="https://aihubmix.com?aff=pG7r">aihubmix.com</a> 的资金支持。我们推荐使用 aihubmix 作为全球主流模型的一站式服务平台。（OpenAI、Claude、Google Gemini、DeepSeek、Qwen 以及数百种其他模型）。</p>
</div>

## ✨ 功能特色

- 🎨 现代化安卓APP设计（Material You / 预测性返回）
- 🌙 暗色模式
- 🖥️ Web多端访问支持
- 🛠️ MCP 支持
- 🔄 多种类型的供应商支持，自定义 API / URL / 模型（目前支持 OpenAI、Google、Anthropic）
- 🖼️ 多模态输入支持
- 📝 Markdown 渲染（支持代码高亮、数学公式、表格、Mermaid）
- 🔍 搜索功能（Exa、Tavily、Zhipu、LinkUp、Brave、Perplexity、..）
- 🧩 Prompt 变量（模型名称、时间等）
- 🤳 二维码导出和导入提供商
- 🤖 智能体自定义
- 🧠 类ChatGPT记忆功能
- 📝 AI翻译
- 🌐 自定义HTTP请求头和请求体

## ✨ 贡献

本项目使用[Android Studio](https://developer.android.com/studio)开发，欢迎提交PR

技术栈文档:

- [Kotlin](https://kotlinlang.org/) (开发语言)
- [Koin](https://insert-koin.io/) (依赖注入)
- [Jetpack Compose](https://developer.android.com/jetpack/compose) (UI 框架)
- [DataStore](https://developer.android.com/topic/libraries/architecture/datastore?hl=zh-cn#preferences-datastore) (
  偏好数据存储)
- [Room](https://developer.android.com/training/data-storage/room) (数据库)
- [Coil](https://coil-kt.github.io/coil/) (图片加载)
- [Material You](https://m3.material.io/) (UI 设计)
- [Navigation Compose](https://developer.android.com/develop/ui/compose/navigation) (导航)
- [Okhttp](https://square.github.io/okhttp/) (HTTP 客户端)
- [kotlinx.serialization](https://github.com/Kotlin/kotlinx.serialization) (Json序列化)
- [compose-icons/lucide](https://composeicons.com/icon-libraries/lucide) (图标库)

> [!TIP]
> 你需要在 `app` 文件夹下添加 `google-services.json` 文件才能构建应用。

> [!IMPORTANT]  
> 以下PR将被拒绝：
> 1. 添加新语言，因为添加新语言会增加后续本地化的工作量
> 2. 添加新功能，这个项目是有态度的
> 3. AI生成的大规模重构和更改

## 💰 捐赠

* [Patreon](https://patreon.com/rikkahub)
* [爱发电](https://afdian.com/a/reovo)

## ⭐ Star History

如果喜欢这个项目，请给个Star ⭐

[![Star History Chart](https://api.star-history.com/svg?repos=re-ovo/rikkahub&type=Date)](https://star-history.com/#re-ovo/rikkahub&Date)

## 📄 许可证

[License](LICENSE)
