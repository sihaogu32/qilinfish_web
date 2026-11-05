# 麒麟魚 Qilinfish — 工作室網頁

暗色系極簡程式工業風形象站。Spring Boot 3 + Thymeleaf MVVM 架構，前端走暗色 + 藏青單色強調，預計部署於 `qilinfish.com` (Cloudflare)。

---

## 預覽
打開 `preview.html` 直接在瀏覽器看完整網站（中英所有頁面與文章），不必啟動伺服器。

`preview.html` 由 `tools/build_preview.py` 從真的網站產生，**不要手改**。網站或文章有改動時重新產生：

```bash
# 先在本機啟動網站（backend/ 下 java -jar target/qilinfish-website-*.jar，或 docker compose up）
python tools/build_preview.py                  # 預設抓 http://localhost:8080
python tools/build_preview.py --base http://localhost:18080
```

- CSS、JS、圖片、影片不複製，直接指向 repo 裡的檔案，所以 `preview.html` 要放在 repo 根目錄。
- 預覽裡表單不會送出；產品頁情境卡連到問卷時不會預選（`?pick=`）。

## 部署架構

```
content/
├── articles/<slug>/                                # 文章包，CompanyofOne 匯出時寫進來（見下方「文章包」）
└── article-pairs.txt                               # 對應文章的配對，由網站決定（見下方「對應文章」）
backend/
├── pom.xml
├── src/main/java/com/qilinfish/website/
│   ├── QilinfishWebsiteApplication.java
│   ├── config/                                     # 中英文：/en/... 是英文，其餘是中文
│   │   ├── WebConfig.java                          # 每頁一組語系檔、語言由網址決定；文章包只公開 media/
│   │   ├── LangPathFilter.java                     # 拿掉 /en 再轉給同一個 Controller
│   │   ├── LangLocaleResolver.java
│   │   └── LangModelAdvice.java                    # 每頁都有的 lp（連結前綴）、htmlLang、zhUrl / enUrl
│   ├── controller/                                 # Controller 層
│   │   ├── HomeController.java                     # 沒有獨立首頁：/ 依切換鍵記住的語言或瀏覽器語言，導到 /about 或 /en/about
│   │   ├── AboutController.java
│   │   ├── ProductsController.java                 # /products、/products/verify、/products/content
│   │   ├── CasesController.java
│   │   ├── ContactController.java
│   │   ├── ResourcesController.java
│   │   ├── SurveyController.java                   # 查核問卷 /survey/verify；舊網址 GET /survey 301 過去（帶整段 query），POST /survey 照收
│   │   ├── SurveyContentController.java            # 文章 app 問卷 /survey/content，答案存 survey_content 表
│   │   └── BlogController.java                     # /blog、/blog/{slug}；清單從文章包產生；/en/blog/shadow-ai 301 到 shadow-ai-en
│   ├── model/                                      # 純資料 Model
│   │   ├── ScenarioModel.java                      # 產品頁情境卡
│   │   ├── CaseModel.java
│   │   ├── ContactFormModel.java
│   │   ├── ResourceLinkModel.java
│   │   ├── SurveyFormModel.java                    # 查核問卷；金額與聯絡方式的檢查兩份問卷共用
│   │   ├── SurveyContentFormModel.java             # 文章 app 問卷；選項存值（中文）也在這裡
│   │   └── ArticlePackageModel.java                # 文章包的 article.json
│   ├── repository/
│   │   ├── SurveyRepository.java                   # 問卷答案寫進 SQLite（survey、survey_content 兩張表，同一個檔）
│   │   ├── ArticlePackageRepository.java           # 啟動時讀所有文章包並檢查，有錯就拒絕啟動
│   │   └── ArticlePairRepository.java              # 讀 article-pairs.txt 並檢查
│   ├── service/MailService.java                    # 聯絡表單寄信
│   └── vb/                                         # ViewBinder（MVVM 的 VM）
│       ├── AboutViewBinder.java
│       ├── ProductsViewBinder.java
│       ├── ProductsContentViewBinder.java
│       ├── ProductsIndexViewBinder.java
│       ├── CasesViewBinder.java
│       ├── ContactViewBinder.java
│       ├── ResourcesViewBinder.java
│       ├── SurveyViewBinder.java
│       ├── SurveyContentViewBinder.java
│       └── BlogViewBinder.java
└── src/main/resources/
    ├── application.yml                             # DB 連接埠保留 (3306)，autoconfig 已停用
    ├── i18n/                                       # 每頁一組：<頁>.properties 中文、<頁>_en.properties 英文；新頁的 key 加頁面前綴（survey_content 用 sc.）
    ├── static/css/qilinfish.css
    ├── static/js/qilinfish.js
    ├── static/media/                               # 頁面用的圖與短片（AI 生成的示意畫面）；入口頁兩張卡是 report_clean.jpg（查核）與 content_clean.jpg（文章 app）
    └── templates/
        ├── fragments/layout.html                   # navbar / footer / background fragment
        ├── fragments/head.html                     # 新頁的 <head>：title、description、og（D52）
        ├── fragments/blog.html                     # 文章清單卡片的封面
        ├── blog.html                               # 文章列表
        ├── blog/article.html                       # 所有文章共用的模板：網站外框 + 頁首 + body.html（提問框補上問卷按鈕）
        ├── about.html
        ├── products_index.html                     # 產品方案入口頁 /products（兩張產品卡）
        ├── products.html                           # 查核產品頁 /products/verify
        ├── products_content.html                   # 文章 app 產品頁 /products/content
        ├── cases.html
        ├── contact.html
        ├── resources.html
        ├── survey.html                             # 查核問卷 /survey/verify
        └── survey_content.html                     # 文章 app 問卷 /survey/content
```

## 文章包
CompanyofOne 匯出的一篇文章，放在 `content/articles/<slug>/`（網站讀的資料夾是設定值 `qilinfish.articles.dir`，環境變數 `ARTICLES_DIR`，預設 `../content/articles`，從 `backend/` 啟動時就是這個 repo 的 `content/articles`）：

```
content/articles/<slug>/
├── article.json    # slug、lang（zh/en）、title、lede、summary、date（yyyy-MM-dd）、hero（image 必要、video 可省略）、cover
├── body.html       # 只有文章內容（<article class="bl-post"> 裡面那一段），不能有 <html>／<head>／<style>
└── media/          # 素材；body.html 和 article.json 都寫 media/<檔名>
```

- 一篇文章只有一種語言：中文文章只在 `/blog`，英文文章只在 `/en/blog`；另一種語言的同一網址是 404。
- 網站外框（head、導覽列、頁尾、語言切換、CTA）、description、og、JSON-LD 都由網站依 `article.json` 產生，模板是 `blog/article.html`。
- CTA：文章自己在參考資料前面寫一個提問框 `.bl-invite`（只有提問，沒有連結）；網站顯示時把問卷按鈕補進這個框的最後面，
  導到 `/survey/verify`（中文）、`/en/survey/verify`（英文），按鈕文字是 `i18n/blog*.properties` 的 `blog.cta.button`。
  body.html 不會被重新輸出：讀包時用 jsoup 找到提問框結束標籤在原檔的位置，在那裡切成前後兩段，模板在兩段中間放按鈕。
- 頁首：有 `hero.video` 就播影片、`hero.image` 當載入前畫面；沒有影片就只顯示圖片。
- 只有 `media/` 對外公開，網址是 `/articles/<slug>/media/<檔名>`；`article.json`、`body.html` 不公開。
- 啟動檢查：slug 與資料夾同名、lang、必要欄位、引用的素材存在且在 `media/` 裡、`body.html` 不含 `<html>`／`<head>`／`<style>`、
  `.bl-invite` 剛好一個且框內沒有 `<a>`（和 CompanyofOne 匯出的檢查同一條規則）、`article.json` 沒有不認得的欄位。有錯就拒絕啟動，訊息列出每一篇、每一個錯。
- 樣式只用 `static/css/qilinfish.css`。這份和 CompanyofOne 的 `content/assets/css/qilinfish.css` 必須逐字相同，改一邊就要同步另一邊。
- 測試用的文章包在 `backend/src/test/resources/articles/`（配對是同層的 `article-pairs.txt`），不放 `content/`。

## 對應文章
同主題、不同語言的兩篇文章由網站配成一對，記在文章包資料夾旁邊的 `article-pairs.txt`（即 `content/article-pairs.txt`），
一行一對，`#` 開頭是註解：

```
solo-trust = solo-trust-en
```

- 有配對：文章頁的語言切換鍵互相指向對方，`<head>` 輸出兩語的 hreflang（完整網址）。
- 沒有配對：切換鍵到另一種語言的文章清單，不輸出 hreflang。沒有這個檔案等於沒有配對。
- 啟動檢查：兩篇都要是文章包、一中一英、每篇最多出現在一對；有錯就拒絕啟動，訊息指出第幾行。
- 文章包和 CompanyofOne 都不知道配對；重新匯出文章包不會動到這個檔案。

## 撰寫規則
- **MVVM** — Model（純資料）/ ViewBinder（VM、view-only 欄位）/ Controller（綁 VB → 模板）/ View（Thymeleaf）
- **命名** — function 雙駝峰；所有 function 傳入變數加 `__` 前綴（如 `renderHome(Model __model)`）
- **暫不使用 DB** — `application.yml` 已關閉 `DataSourceAutoConfiguration` / `HibernateJpaAutoConfiguration`，但保留 `qilinfish.db.reserved-port: 3306` 與註解的 datasource block，未來啟用只要解註

## 設計系統
- 配色：`--bg-0:#0a0a0c` 主底；`--accent: oklch(0.58 0.12 252)` 藏青單色強調
- 字型：Inter / Noto Sans TC（內文）+ JetBrains Mono（技術細節）
- 風格：細線條格線、十字準星、終端機 prompt、單色 status pill
- 互動：打字機（關於頁的開場標題與 WHAT 那句，各打一次）、滾動淡入 (IntersectionObserver)、聯絡表單前端驗證、中英切換（英文在 `/en/...`，切換時記在 cookie `qf_lang`；新頁面要中英文一起上線，文章可以只有一種語言）、產品頁放大鏡開場、情境卡連到問卷並預選經驗（`/survey/verify?pick=`）

## 部署
詳見 `DEPLOY.md` — Cloudflare Tunnel + Email Routing 的完整步驟。簡述：

```bash
cp .env.example .env       # 填 Gmail App Password、Cloudflare Tunnel Token
docker compose up -d --build
```

`content/` 唯讀掛進容器（`ARTICLES_DIR=/content/articles`）。上線文章：匯入文章包 → 提交 → 伺服器 `git pull` → `docker compose restart qilinfish-web cloudflared`（cloudflared 借用 web 的網路，要一起重啟），不用重建 image（見 `DEPLOY.md`「上線一篇文章」）。

## GitHub
Repo：<https://github.com/sihaogu32/qilinfish_web>

寄件流程：表單送出 → Spring Boot 透過 Gmail SMTP 寄到 `qilinfish@qilinfish.com` → Cloudflare Email Routing 自動轉發到 `qilinfish@gmail.com`。
（Cloudflare Email Routing 只支援接信轉發，不提供寄信 SMTP，所以寄送端仍走 Gmail。）

## 問卷資料
查核問卷 `/survey/verify` 的答案不寄信，存在容器裡的 SQLite 檔 `/data/survey.db`（compose 的 `qilinfish-data` volume，重建容器不會不見）。一份答案一列。
英文頁 `/en/survey/verify` 的答案存在同一張表：選擇題一律存中文字，`lang` 欄標 `zh` 或 `en`，開放題照填寫者寫的語言存。
文章 app 問卷 `/survey/content`（中英）存在同一個檔的 `survey_content` 表，規則相同；複選題存成以「、」分隔的中文字，「其他」存成「其他：填寫的字」；`blind_locked` 是 1 表示揭曉題是按「下一步」之後才出現的（只是旁證）。

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
- 文章 app 的對外產品名稱未定（2026-11-04 列入追蹤）。候選：Sourced／有據、Traceable／可溯、掛品牌名、不取名只用描述。決定後要查商標與網域，並更新產品頁、問卷標題、部落格文章最後那一行
- 每篇部落格文章都能「點一句看出處」（2026-11-04 列入追蹤）：文章包加帶證據（原文引句、限定條件、網址），網站讀它顯示。要同時改 CompanyofOne 的匯出與網站的啟動檢查。`/products/content` 的示範目前是手寫在網站裡的兩筆證據，做完這項後改讀文章包。**文章 app 產品頁驗收時要提醒使用者做這項**。2026-11-05 已討論定案（點 [n] 展開證據），規格將放在 `design/features/article-evidence/`
- 定位句「每一句都查得到出處」字面上不成立（2026-11-05 列入追蹤）：實測 solo-trust 46/91、ai-handover 7/44 個文字區塊（段落、列點、表格格子）沒有任何證據標記。這句出現在產品頁標題、產品總覽、部落格文章最後一行、問卷與 `CONTEXT.md`。待決：改定位句，或要求文章每一句都有標記
