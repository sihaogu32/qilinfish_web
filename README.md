# 麒麟魚 Qilinfish — 工作室網頁

暗色系極簡程式工業風形象站。Spring Boot 3 + Thymeleaf MVVM 架構，前端走暗色 + 藏青單色強調，預計部署於 `qilinfish.com` (Cloudflare)。

---

## 預覽
打開 `preview.html` 直接在瀏覽器看完整流程（單檔靜態 SPA，模擬多頁切換）。

## 部署架構

```
backend/
├── pom.xml
├── src/main/java/com/qilinfish/website/
│   ├── QilinfishWebsiteApplication.java
│   ├── config/                                     # 中英文：/en/... 是英文，其餘是中文
│   │   ├── WebConfig.java                          # 每頁一組語系檔、語言由網址決定
│   │   ├── LangPathFilter.java                     # 拿掉 /en 再轉給同一個 Controller
│   │   ├── LangLocaleResolver.java
│   │   └── LangModelAdvice.java                    # 每頁都有的 lp（連結前綴）、htmlLang、zhUrl / enUrl
│   ├── controller/                                 # Controller 層
│   │   ├── HomeController.java                     # 沒有獨立首頁：/ 依切換鍵記住的語言或瀏覽器語言，導到 /about 或 /en/about
│   │   ├── AboutController.java
│   │   ├── ProductsController.java
│   │   ├── CasesController.java
│   │   ├── ContactController.java
│   │   ├── ResourcesController.java
│   │   ├── SurveyController.java
│   │   └── BlogController.java                     # /blog、/blog/{slug}；文章清單也在這裡
│   ├── model/                                      # 純資料 Model
│   │   ├── ScenarioModel.java                      # 產品頁情境卡
│   │   ├── CaseModel.java
│   │   ├── ContactFormModel.java
│   │   ├── ResourceLinkModel.java
│   │   ├── SurveyFormModel.java
│   │   └── BlogPostModel.java                      # 一篇文章的 slug、標題、摘要、日期、封面
│   ├── repository/SurveyRepository.java            # 問卷答案寫進 SQLite
│   ├── service/MailService.java                    # 聯絡表單寄信
│   └── vb/                                         # ViewBinder（MVVM 的 VM）
│       ├── AboutViewBinder.java
│       ├── ProductsViewBinder.java
│       ├── CasesViewBinder.java
│       ├── ContactViewBinder.java
│       ├── ResourcesViewBinder.java
│       ├── SurveyViewBinder.java
│       └── BlogViewBinder.java
└── src/main/resources/
    ├── application.yml                             # DB 連接埠保留 (3306)，autoconfig 已停用
    ├── i18n/                                       # 每頁一組：<頁>.properties 中文、<頁>_en.properties 英文
    ├── static/css/qilinfish.css
    ├── static/js/qilinfish.js
    ├── static/media/                               # 頁面用的圖與短片（AI 生成的示意畫面）
    └── templates/
        ├── fragments/layout.html                   # navbar / footer / background fragment
        ├── fragments/blog.html                     # 文章封面（缺圖時顯示底圖區塊）與文章標頭
        ├── blog.html                               # 文章列表
        ├── blog/shadow-ai.html                     # 每篇文章的內文一個檔，檔名＝slug
        ├── about.html
        ├── products.html
        ├── cases.html
        ├── contact.html
        ├── resources.html
        └── survey.html
```

## 撰寫規則
- **MVVM** — Model（純資料）/ ViewBinder（VM、view-only 欄位）/ Controller（綁 VB → 模板）/ View（Thymeleaf）
- **命名** — function 雙駝峰；所有 function 傳入變數加 `__` 前綴（如 `renderHome(Model __model)`）
- **暫不使用 DB** — `application.yml` 已關閉 `DataSourceAutoConfiguration` / `HibernateJpaAutoConfiguration`，但保留 `qilinfish.db.reserved-port: 3306` 與註解的 datasource block，未來啟用只要解註

## 設計系統
- 配色：`--bg-0:#0a0a0c` 主底；`--accent: oklch(0.58 0.12 252)` 藏青單色強調
- 字型：Inter / Noto Sans TC（內文）+ JetBrains Mono（技術細節）
- 風格：細線條格線、十字準星、終端機 prompt、單色 status pill
- 互動：打字機（關於頁的開場標題與 WHAT 那句，各打一次）、滾動淡入 (IntersectionObserver)、聯絡表單前端驗證、中英切換（英文在 `/en/...`，切換時記在 cookie `qf_lang`；新頁面或文章要中英文一起上線）、產品頁放大鏡開場、情境卡連到問卷並預選經驗（`/survey?pick=`）

## 部署
詳見 `DEPLOY.md` — Cloudflare Tunnel + Email Routing 的完整步驟。簡述：

```bash
cp .env.example .env       # 填 Gmail App Password、Cloudflare Tunnel Token
docker compose up -d --build
```

## GitHub
Repo：<https://github.com/sihaogu32/qilinfish_web>

寄件流程：表單送出 → Spring Boot 透過 Gmail SMTP 寄到 `qilinfish@qilinfish.com` → Cloudflare Email Routing 自動轉發到 `qilinfish@gmail.com`。
（Cloudflare Email Routing 只支援接信轉發，不提供寄信 SMTP，所以寄送端仍走 Gmail。）

## 問卷資料
`/survey` 的答案不寄信，存在容器裡的 SQLite 檔 `/data/survey.db`（compose 的 `qilinfish-data` volume，重建容器不會不見）。一份答案一列。
英文頁 `/en/survey` 的答案存在同一張表：選擇題一律存中文字，`lang` 欄標 `zh` 或 `en`，開放題照填寫者寫的語言存。

在跑 `docker compose` 的那台機器上，把檔案複製出來：

```bash
docker cp qilinfish-web:/data/survey.db ./survey.db
```

再用免費的 [DB Browser for SQLite](https://sqlitebrowser.org/) 打開 `survey.db` → `survey` 表，可以篩選，也可以「檔案 → 匯出 → 表格為 CSV」。
`survey.db` 有填寫者的名字和聯絡方式，不要放進 repo（`.gitignore` 已排除 `*.db`）。

## 後續
- 補上案例資料後解開 `cases.html` 的列表（VB 的 `empty=false`）
- DB 啟用：解開 `application.yml` 的 datasource，移除 autoconfigure exclude
- Cloudflare 部署：建議走 Cloudflare Tunnel 連到後端 Spring Boot
