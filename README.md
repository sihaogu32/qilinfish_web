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
│   ├── config/WebConfig.java                       # i18n + locale interceptor
│   ├── controller/                                 # Controller 層
│   │   ├── HomeController.java
│   │   ├── AboutController.java
│   │   ├── ProductsController.java
│   │   ├── CasesController.java
│   │   ├── ContactController.java
│   │   └── ResourcesController.java
│   ├── model/                                      # 純資料 Model
│   │   ├── ProductModel.java
│   │   ├── CaseModel.java
│   │   ├── ContactFormModel.java
│   │   └── ResourceLinkModel.java
│   └── vb/                                         # ViewBinder（MVVM 的 VM）
│       ├── AboutViewBinder.java
│       ├── ProductsViewBinder.java
│       ├── CasesViewBinder.java
│       ├── ContactViewBinder.java
│       └── ResourcesViewBinder.java
└── src/main/resources/
    ├── application.yml                             # DB 連接埠保留 (3306)，autoconfig 已停用
    ├── i18n/                                       # 中英 messages
    ├── static/css/qilinfish.css
    ├── static/js/qilinfish.js
    └── templates/
        ├── fragments/layout.html                   # navbar / footer / background fragment
        ├── index.html
        ├── about.html
        ├── products.html
        ├── cases.html
        ├── contact.html
        └── resources.html
```

## 撰寫規則
- **MVVM** — Model（純資料）/ ViewBinder（VM、view-only 欄位）/ Controller（綁 VB → 模板）/ View（Thymeleaf）
- **命名** — function 雙駝峰；所有 function 傳入變數加 `__` 前綴（如 `renderHome(Model __model)`）
- **暫不使用 DB** — `application.yml` 已關閉 `DataSourceAutoConfiguration` / `HibernateJpaAutoConfiguration`，但保留 `qilinfish.db.reserved-port: 3306` 與註解的 datasource block，未來啟用只要解註

## 設計系統
- 配色：`--bg-0:#0a0a0c` 主底；`--accent: oklch(0.58 0.12 252)` 藏青單色強調
- 字型：Inter / Noto Sans TC（內文）+ JetBrains Mono（技術細節）
- 風格：細線條格線、十字準星、終端機 prompt、單色 status pill
- 互動：打字機 hero、滾動淡入 (IntersectionObserver)、聯絡表單前端驗證、中英 (`?lang=`) 切換、Tabs 切換產品分類

## 部署
詳見 `DEPLOY.md` — Cloudflare Tunnel + Email Routing 的完整步驟。簡述：

```bash
cp .env.example .env       # 填 Gmail App Password、Cloudflare Tunnel Token
docker compose up -d --build
```

## GitHub
Repo：<https://github.com/sihaogu32/qilinfish_web>
首次推送步驟見 `GITHUB.md`。

寄件流程：表單送出 → Spring Boot 透過 Gmail SMTP 寄到 `qilinfish@qilinfish.com` → Cloudflare Email Routing 自動轉發到 `qilinfish@gmail.com`。
（Cloudflare Email Routing 只支援接信轉發，不提供寄信 SMTP，所以寄送端仍走 Gmail。）

## 後續
- 補上案例資料後解開 `cases.html` 的列表（VB 的 `empty=false`）
- DB 啟用：解開 `application.yml` 的 datasource，移除 autoconfigure exclude
- Cloudflare 部署：建議走 Cloudflare Tunnel 連到後端 Spring Boot
