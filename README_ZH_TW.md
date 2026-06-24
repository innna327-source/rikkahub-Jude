<div align="center">
  <img src="docs/icon.png" alt="App 圖標" width="100" />
  <h1>RikkaHub Auto Compress</h1>

一個原生Android LLM 聊天客戶端，支持切換不同的供應商進行聊天 🤖💬

點擊加入我們的Discord伺服器 👉 [【RikkaHub】](https://discord.gg/9weBqxe5c4)

[English](README.md) | 繁體中文 | [简体中文](README_ZH_CN.md)

</div>

> [!IMPORTANT]
> 這是基於 [RikkaHub](https://github.com/rikkahub/rikkahub) 的**非官方二次開發版本**，目前公開原始碼基於 RikkaHub `2.2.5` 程式碼線，並合入部分後續修正及下述二創功能；本專案不代表 RikkaHub 官方版本。可下載版本以本倉庫的 [Releases](https://github.com/innna327-source/rikkahub-auto-compress/releases) 頁面為準。

<div align="center">
  <img src="docs/img/chat.png" alt="Chat Interface" width="150" />
  <img src="docs/img/desktop.png" alt="Models Picker" width="450" />
</div>

## 🚀 下載

🔗 **[前往 GitHub Releases 下載 APK](https://github.com/innna327-source/rikkahub-auto-compress/releases)**

如果只想安裝應用程式，請不要點擊 `Code → Download ZIP`，那裡下載的是原始碼，不是 APK。

目前發布包為 `arm64-v8a` Debug APK。安裝前請先備份應用資料。由於本二創版本與官方版本可能使用不同簽章，Android 可能不允許直接覆蓋安裝；若提示簽章衝突，請先在原應用內匯出備份，再考慮解除安裝舊版本。

需要官方版本時，請前往 [RikkaHub 上游專案](https://github.com/rikkahub/rikkahub)。

## 🌿 本二創主要新增功能

- **自動滾動摘要與上下文壓縮**：保留最近訊息，自動壓縮較早的可見對話；摘要可檢視、編輯，並可設定目標長度。
- **長對話分塊壓縮**：超長歷史與既有摘要會按容量分塊處理，避免單次請求內容過大。
- **獨立壓縮 API 與模型**：壓縮任務可單獨設定 OpenAI 相容位址、API Key、模型、Chat Completions 路徑或 Responses API。
- **獨立 OCR API 與模型**：OCR 可使用單獨的 OpenAI 相容介面與模型，不影響一般聊天提供商。
- **分段 TTS 播放**：支援按段落朗讀回覆，以及僅朗讀引用、僅朗讀英文與生成後自動播放。
- **使用統計與提醒**：提供本機應用使用統計與可設定的使用時間提醒。

除 Release 說明特別標註外，本版本仍保留下方列出的 RikkaHub 上游功能。

## 💖 贊助商

<div align="center">
  <img src="app/src/main/assets/icons/aihubmix-color.svg" alt="Aihubmix" width="50" />
  <p style="font-size: 16px; font-weight: bold;">Aihubmix</p>
  <p style="font-size: 14px;">感謝 <a href="https://aihubmix.com?aff=pG7r">aihubmix.com</a> 的資金支持。我們推薦使用 aihubmix 作為全球主流模型的一站式服務平台。（OpenAI、Claude、Google Gemini、DeepSeek、Qwen 以及數百種其他模型）。</p>
</div>

## ✨ 功能特色

- 🎨 現代化安卓APP設計（Material You / 預測性返回）
- 🌙 暗色模式
- 🖥️ Web多端訪問支持
- 🛠️ MCP 支持
- 🔄 多種類型的供應商支持，自定義 API / URL / 模型（目前支持 OpenAI、Google、Anthropic）
- 🖼️ 多模態輸入支持
- 📝 Markdown 渲染（支持代碼高亮、數學公式、表格、Mermaid）
- 🔍 搜尋功能（Exa、Tavily、Zhipu、LinkUp、Brave、Perplexity、..）
- 🧩 Prompt 變量（模型名稱、時間等）
- 🤳 二維碼導出和導入提供商
- 🤖 智能體自定義
- 🧠 類ChatGPT記憶功能
- 📝 AI翻譯
- 🌐 自定義HTTP請求頭和請求體

## ✨ 貢獻

本項目使用[Android Studio](https://developer.android.com/studio)開發，歡迎提交PR

技術棧文檔:

- [Kotlin](https://kotlinlang.org/) (開發語言)
- [Koin](https://insert-koin.io/) (依賴注入)
- [Jetpack Compose](https://developer.android.com/jetpack/compose) (UI 框架)
- [DataStore](https://developer.android.com/topic/libraries/architecture/datastore?hl=zh-cn#preferences-datastore) (
  偏好數據存儲)
- [Room](https://developer.android.com/training/data-storage/room) (數據庫)
- [Coil](https://coil-kt.github.io/coil/) (圖片加載)
- [Material You](https://m3.material.io/) (UI 設計)
- [Navigation Compose](https://developer.android.com/develop/ui/compose/navigation) (導航)
- [Okhttp](https://square.github.io/okhttp/) (HTTP 客戶端)
- [kotlinx.serialization](https://github.com/Kotlin/kotlinx.serialization) (Json序列化)
- [compose-icons/lucide](https://composeicons.com/icon-libraries/lucide) (圖標庫)

> [!TIP]
> 你需要在 `app` 資料夾下添加 `google-services.json` 檔案才能構建應用。

> [!IMPORTANT]  
> 以下PR將被拒絕：
> 1. 添加新語言，因為添加新語言會增加後續本地化的工作量
> 2. 添加新功能，這個項目是有態度的
> 3. AI生成的大規模重構和更改

## 💰 捐贈

* [Patreon](https://patreon.com/rikkahub)
* [愛發電](https://afdian.com/a/reovo)

## ⭐ Star History

如果喜歡這個項目，請給個Star ⭐

[![Star History Chart](https://api.star-history.com/svg?repos=re-ovo/rikkahub&type=Date)](https://star-history.com/#re-ovo/rikkahub&Date)

## 📄 許可證

[License](LICENSE) 
