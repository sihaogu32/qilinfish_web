# 文章 app 產品頁與問卷（content-product）票單

狀態：CP1–CP11 已驗收（2026-11-04）。規格：`spec.md`（同目錄）。決策編號：規格未決第 N 項定案為 D(30+N)；拆票時的缺口定案為 D55–D61（見檔尾「已定案」）。

| 票單 | 行為 | 規格 | 相依 | 狀態 |
|---|---|---|---|---|
| CP1 | 查核頁與查核問卷搬家，`/survey` 301 | B7、B26、B27、B29 | — | 已驗收 2026-11-04 |
| CP2 | 導覽列拿掉「填寫問卷」 | B28 | CP1 | 已驗收 2026-11-04 |
| CP3 | 文章 app 問卷：填完送出，新表多一列 | B16–B18、B22–B25 | CP1 | 已驗收 2026-11-04 |
| CP4 | 問卷盲問：先問，按「下一步」才揭曉，然後鎖住 | B19–B21 | CP3 | 已驗收 2026-11-04 |
| CP5 | 文章 app 產品頁五段（示範先不能點） | B8、B11、B13–B15 | CP3 | 已驗收 2026-11-04 |
| CP6 | 產品頁示範：點一句展開證據 | B9、B10 | CP5 | 已驗收 2026-11-04 |
| CP7 | 有證據標記的文章，最後多一行連到產品頁 | B30 | CP1、CP5 | 已驗收 2026-11-04 |
| CP8 | 產品頁「實際案例」自動列出有證據標記的文章 | B12 | CP5、CP7 | 已驗收 2026-11-04 |
| CP9 | `/products` 改成入口頁（內容卡先用占位圖） | B1–B6 | CP1、CP5 | 已驗收 2026-11-04 |
| CP10 | 內容卡示意圖換上（完成才能上線，D56） | B4（圖） | CP9 | 已驗收 2026-11-04 |
| CP11 | `preview.html` 涵蓋新頁 | B32 | CP1–CP9 | 已驗收 2026-11-04 |

B31（英文頁）不單獨開票。每一張票都交付中文頁與英文頁。

## 共同規則

- 每張票交付一個可觀察的行為。Controller、ViewBinder、Model、模板、i18n 中英文、CSS、JS、測試在同一張票裡完成。
- 寫法照 README「撰寫規則」：MVVM；function 雙駝峰；function 的傳入變數加 `__` 前綴。
- 每張票都要：
  1. `cd backend && mvn test` 全綠（沒有 mvnw）。
  2. `mvn package` 成功，用正式模式（`java -jar` 或 `docker compose up`）啟動，用 curl 與瀏覽器驗證。
  3. 中文頁與英文頁一起驗。英文頁的站內連結都是 `/en/...`；語言切換鍵在兩頁之間互相指向同一頁（B31）。
  4. 在網站上跑一次 `python tools/build_preview.py`，結束碼 0。`preview.html` 的最後產生在 CP11。
- **文案**：中文與英文文案都由助理起草（P3、D31、D58）。文案隨票附中英對照表，使用者驗收時審。對照表寫在本檔該票的「英文對照」小節。
- **i18n**：`WebConfig.java:42-50` 的 basename 清單寫死，新增語系檔要加進清單。`WebConfig.java:54` 是 `setUseCodeAsDefaultMessage(true)`：key 打錯不報錯，頁面直接顯示 key。所以每張有新 key 的票，驗收時要在中英頁的輸出搜尋自己的前綴，結果是 0。
- **改 `qilinfish.css` 的票**：兩份 `qilinfish.css`（網站 `backend/src/main/resources/static/css/qilinfish.css`、CompanyofOne `content/assets/css/qilinfish.css`）用 sync-qilinfish-css 同步。驗收步驟包含 `cmp` 兩份檔案，沒有輸出。`fragments/layout.html:17` 的 CSS 版本參數一起更新。
- **改 `qilinfish.js` 的票**：載入它的模板，版本參數一起更新（`products.html:71`、`blog/article.html:63`、`about.html:58`、`blog.html:27` 是 `v='20261026-pick'`；`survey.html:320` 是 `v='20261026-en'`；新頁照新值）。
- 新頁的 `<title>` 用該頁 H1；description 由助理起草，使用者審（D52）。
- 證據放在每張票的回報裡。截圖與暫存資料庫放暫存目錄，驗收當輪刪除。不提交，整份清單做完一次交使用者驗收。
- 不新增規格沒有的行為。實作時發現規格有缺口，停下來，列在本檔檔尾，問使用者；使用者定案後記進「已定案」並寫上 D 編號。

---

## CP1 查核頁與查核問卷搬家，`/survey` 301

- 交付的行為：
  - B7：`/products/verify` 顯示現有查核產品頁，文字與圖片和現在的 `/products` 相同。這張票不改 `/products`，`/products` 照舊顯示查核頁（CP9 才換成入口頁）。
  - B26：`/survey/verify` 顯示現有查核問卷，`?pick=` 預選照舊，送出存進 `survey` 表。
  - B27：GET `/survey`、`/en/survey` 回 301 到 `/survey/verify`、`/en/survey/verify`，帶過整個 query string（含 `?pick=<值>`，D60）。
  - B29：部落格文章 `.bl-invite` 的問卷按鈕連到 `/survey/verify`、`/en/survey/verify`。
  - POST `/survey`、`/en/survey` 照舊收下，存進 `survey` 表，顯示和 `/survey/verify` 送出後相同的結果頁。只有 GET 做 301，不用 308（D55）。
- 範圍（卡點 b：以下全部在同一張票）：
  - `ProductsController.java:25-41`：`/products/verify` 用同一份 ViewBinder 與模板。`/products` 暫時也顯示同一頁。
  - `products.html:46` 情境卡連結改成 `lp+'/survey/verify?pick='`；`:62-63` 主要按鈕改成 `lp+'/survey/verify'`；`:67` 的 `pd-also` 保留。`qilinfish.js:456-460` 的 `?pick=` 讀取不改（卡點 e）。
  - `SurveyController.java:93` GET 改成 `/survey/verify`；`:101` POST 改成 `/survey/verify`。另加 GET `/survey` 301：照 `BlogController.java:57-59` 的寫法（`View.RESPONSE_STATUS_ATTRIBUTE`＝301，`"redirect:"+(isEn?"/en":"")+...+query`，query 取 `qf.query`，同 `LangModelAdvice.java:408`），帶過整個 query string（D60）。另留 POST `/survey`，走和 POST `/survey/verify` 同一段處理。
  - `survey.html:121` 的 `th:action` 改成 `lp+'/survey/verify'`。
  - `blog/article.html:57` 的問卷按鈕改成 `lp+'/survey/verify'`。
  - `about.html:52` 的主要按鈕改成 `lp+'/survey/verify'`。
  - `fragments/layout.html:32` 導覽列「填寫問卷」的連結先改成 `lp+'/survey/verify'`（CP2 才拿掉這一項）。目的是讓沒有任何頁面連到 `/survey`。
  - `BlogControllerTest.java:97-103` 的 `href="/survey"`、`href="/en/survey"` 改成 `/survey/verify`、`/en/survey/verify`。
  - 新增測試（照 `BlogControllerTest.java:106-113` 的 NEVER-follow 寫法）：GET `/survey`、`/survey?pick=verify`、`/en/survey`、`/en/survey?pick=verify`、`/survey?pick=verify&utm_source=x` 回 301 與正確的 `Location`（最後一個帶過整個 query）；POST `/survey`、`/en/survey` 回 200，`survey` 表多一列；GET `/products/verify`、`/en/products/verify` 回 200。
  - `tools/build_preview.py:34` 的 `TOP`：拿掉 `/survey`，加上 `/products/verify`、`/survey/verify`。
  - README：第 69 行的 `/survey?pick=` 改成 `/survey/verify?pick=`；「部署架構」的 `SurveyController` 註解寫上 `/survey` 301。
- 相依：無。
- 依據的決策：
  - D42：連結直接改成 `/survey/verify`，不靠 301。
  - D43：`/products/verify` 保留底部「也接 Web 開發與專案設計」。
  - D53：`TOP` 換成新網址。
  - D55：POST `/survey` 照舊收下，只有 GET 做 301，不用 308；上線一段時間後再決定要不要拿掉 POST 的處理。
  - D60：301 帶過整個 query string。
- 驗收步驟：
  1. 改之前，截 `/products`、`/en/products` 整頁圖（1440 與 390）。
  2. 改之後，截 `/products/verify`、`/en/products/verify` 同尺寸整頁圖，和第 1 步比對。放大鏡開場要能動。
  3. `curl -I` 打 B27 的四個網址，加上 `/survey?pick=verify&utm_source=x`。
  4. 打開 `/survey/verify?pick=verify`，看預選；送一份。用 `curl -X POST` 對 `/survey`、`/en/survey` 各送一份（模擬舊快取頁）。
  5. 每次送出前後，查 `survey` 表的列數與最後一筆舊列。
  6. 打開四篇文章（`solo-trust`、`ai-handover`、`shadow-ai`、`shadow-ai-en`），看 `.bl-invite` 按鈕的 `href`。
  7. 在 `backend/src/main/resources/templates/` 搜尋 `'/survey'`（後面不是 `/verify`），結果是 0。
  8. 跑 `python tools/build_preview.py`。
- 預期證據：
  - `mvn test` 全綠的輸出（含新測試）。
  - 兩組截圖並排，相同。
  - 五個 `curl -I`：`HTTP/1.1 301`，`Location` 分別是 `/survey/verify`、`/survey/verify?pick=verify`、`/en/survey/verify`、`/en/survey/verify?pick=verify`、`/survey/verify?pick=verify&utm_source=x`。
  - `sqlite3`（或 `python -c "import sqlite3..."`）查 `SELECT COUNT(*) FROM survey` 與最後一筆舊列：三次送出後多三列，舊列不變。
  - 四篇文章 `.bl-invite` 按鈕的 HTML。
  - `build_preview.py` 結束碼 0。
- 停止條件：
  - 截圖比對有差異，且差異不是網址。
  - 301 打不到 `/en/survey`（`LangPathFilter.java:264-275` 的 forward 沒有帶到新 mapping）。
  - `survey` 表的舊列有任何改變。
  - 出現以上任一項就停，回報，不往下做。

## CP2 導覽列拿掉「填寫問卷」

- 交付的行為：B28。每一頁（中英）的導覽列沒有「填寫問卷」／英文對應項。
- 範圍：
  - `fragments/layout.html:32` 刪掉這一項；`layout.properties:6`、`layout_en.properties:6` 的 `nav.survey` 刪掉。
  - 目前頁的標示：`survey.html` 的 `navbar('survey')`（第 6 行）改成 `navbar('products')`。三個產品頁標「產品方案」。高亮只看 `navbar('xxx')` 參數；Controller 的 `activeNav` 是死碼，不動。
  - 新增測試：抓 `/about`、`/en/about`、`/survey/verify`、`/en/survey/verify`，輸出沒有「填寫問卷」與 `Take the survey` 的導覽連結。
- 相依：CP1。
- 依據的決策：D44（產品頁與問卷頁都標「產品方案」）。
- 驗收步驟：
  1. 逐頁打開中英各頁（about、products、products/verify、cases、contact、resources、blog、一篇文章、survey/verify），看導覽列。
  2. 在 `/survey/verify` 看「產品方案」有沒有標成目前頁。
- 預期證據：`mvn test` 全綠；`curl` 每頁輸出 `grep -c 'nav.survey\|填寫問卷'` 在導覽列區段是 0；`/survey/verify` 導覽列截圖。
- 停止條件：拿掉之後有頁面出現 `nav.survey` 原字（key 沒刪乾淨）。

## CP3 文章 app 問卷：填完送出，新表多一列

- 交付的行為：
  - B16：開場一句標題、一句為什麼問、三項承諾。
  - B17：五段題目與選項和附錄 A 相同。
  - B18：「交出去之後」選「有人問出處」或「有人指出錯誤」才顯示追問。
  - B22：金額一律 US$。
  - B23：第 05 段只有三個欄位；沒有公司、品牌、圖片、影片的題目。
  - B24：必填題沒填送不出去，頁面指出是哪一題。
  - B25：一次送出整份；新表多一列，選擇題存中文字，`lang` 記 `zh`／`en`；`survey` 表沒有新增列。
  - 這張票的第 04 段：盲問與揭曉兩題**一次全部顯示**（等於沒有 JavaScript 時的樣子）。隱藏、「下一步」、鎖定在 CP4。
- 範圍：
  - **i18n 獨立前綴（卡點 a）**：新檔 `i18n/survey_content.properties`、`survey_content_en.properties`，所有 key 用 `sc.` 前綴。加進 `WebConfig.java:42-50` 的 basename 清單。`survey.properties` 的 key 沒有前綴，共用同一個 MessageSource，同名會互蓋且不報錯，所以新問卷不可以用沒有前綴的 key。
  - Controller：GET、POST `/survey/content`（新 Controller 或在 `SurveyController` 加方法；照 MVVM 拆 `SurveyContentFormModel`、`SurveyContentViewBinder`）。驗證寫在 Model 註解，條件必填（7a 不必填）與金額、聯絡方式寫在 Controller，比照 `SurveyController.java:127`（`checkConditionalFields`）、`:186`（`checkPay`）。
  - 共用：`SurveyFormModel.java:134-153` 的 `usdProblem`／`normalizeUsd`／`isBlankUsd`（static）直接用。`SurveyController.java:73-74` 的 EMAIL／PHONE regex 抽出來，兩份問卷共用。
  - Repository：`SurveyRepository.java:272-292` 旁邊加新表 `survey_content` 的 CREATE／INSERT 常數；同檔 `/data/survey.db`；每次 save 照 `:313-315` 先 `CREATE TABLE IF NOT EXISTS`。欄位：`id`、`created_at`、`lang`、每題一欄（複選存成「、」分隔的中文字串）、`blind_locked`（這張票一律存 0，CP4 才會有 1）。
  - 模板 `survey_content.html`：radio 與 checkbox 的 `value` 寫中文字，英文頁只換顯示文字（比照 `survey.html:130-151`）。`navbar('products')`。
  - **前端另寫 init（卡點 c）**：`qilinfish.js` 新增 `initContentSurvey`，綁 `[data-content-survey]`，不改 `initSurveyForm`（`:210`）與 `initSurveyVisuals`（`:421-460`）。可以呼叫 `plainAmount`（`:169`）、`isValidContact`（`:205`）、`data-show-if`（`:226`）。必填檢查（`:284-299`）不支援 checkbox，新 init 自己處理。「都沒用」勾了就清掉其他勾選。載入 `qilinfish.js` 的模板版本參數更新。
  - CSS：checkbox 樣式（`.qf-choice` `:410` 只為 radio）。可沿用 `.sv-section`、`.sv-two`（`:767`）、`.qf-field.is-invalid`（`:444`）。兩份 `qilinfish.css` 同步。
  - 送出後：只顯示成功訊息，不寄信。
  - 新增測試：POST 一份完整的中文答案、一份英文答案，新表多兩列、`lang` 正確、選擇題是中文字、`survey` 表列數不變；必填各缺一次，回應指出該題；開放題全空可以送出；盲問金額格式錯誤時被擋。
  - `tools/build_preview.py:34` 的 `TOP` 加 `/survey/content`。
  - README「部署架構」補新檔。
- 相依：CP1。
- 依據的決策：
  - D31、D58：中英文文案由助理起草，隨票附對照表。開場的「一句標題、一句為什麼問」也由助理起草（附錄 A 指向 D31、D58）。
  - D32：沒有 JavaScript 時第 04 段一次全部顯示。
  - D34：表名 `survey_content`，同檔，複選以「、」分隔，含 `blind_locked` 欄。
  - D35：盲問單一金額、只收數字、可帶小數，沿用現有金額檢查訊息；選「不會付費」「不知道」時揭曉兩題照樣要答。
  - D36：兩題時數用數字欄，單位寫在欄旁（小時／每篇），選填。
  - D37：「交出去之後」選填；沒有 JavaScript 時追問一直顯示，旁邊標「選了『有人問出處』或『有人指出錯誤』才需要填」。
  - D38：工具題的「其他」也給自填欄。
  - D39：「都沒用」與其他工具互斥。
  - D40：聯絡方式沿用現有 email／電話檢查。
  - D41：送出後只顯示成功訊息，不寄信。
  - D54：「約 6 分鐘」由使用者驗收時自己填一次計時。
  - P1：必填範圍。
- 中英文文案隨票附對照表，使用者驗收時審。
- 驗收步驟：
  1. 打開 `/survey/content`、`/en/survey/content`，逐題和附錄 A 比對（題目、選項、順序、必填標記）。
  2. 「交出去之後」逐一選四個選項，看追問。
  3. 搜尋頁面文字「圖片」「影片」「公司」「品牌」（英文頁搜 image、video、company、brand）。
  4. 每個必填題各留空一次送出。開放題全空送一次。
  5. 中文頁送一份、英文頁送一份。取出 `survey.db` 查兩張表。
  6. 在中英頁輸出搜尋 `sc.`（未翻譯的 key），結果是 0。
  7. `cmp` 兩份 `qilinfish.css`。
  8. 390px 手機寬截圖，checkbox 可以點。
- 預期證據：
  - `mvn test` 全綠（含新測試）。
  - 中英整頁截圖（1440、390）。
  - 追問只在兩個選項出現的截圖或錄影。
  - 必填各缺一次的錯誤提示截圖。
  - `SELECT lang, role, tools, blind_locked FROM survey_content ORDER BY id DESC LIMIT 2` 的結果：`zh`、`en` 各一列，選擇題是中文字；`SELECT COUNT(*) FROM survey` 前後相同。
  - `cmp` 沒有輸出。
- 停止條件：
  - 頁面出現 `sc.` 開頭的原字，或查核問卷 `/survey/verify` 的任何文字變了（key 互蓋）。
  - `survey` 表列數改變。
  - 附錄 A 的題目在實作上做不出來，停下來問使用者。

### 英文對照

（實作時填：中文｜英文 兩欄，含開場兩句。）

## CP4 問卷盲問：先問，按「下一步」才揭曉，然後鎖住

- 交付的行為：
  - B19：有 JavaScript 時，第 04 段一開始只顯示盲問；揭曉兩題隱藏。
  - B20：填完盲問、按「下一步」，揭曉兩題出現；盲問鎖住不能改；盲問旁邊顯示小字。
  - B21：揭曉兩題的文字與選項。
  - 鎖定只在瀏覽器（卡點 d）。送出時帶 `blind_locked`：經過「下一步」才出現揭曉題是 1，否則 0。
- 範圍：
  - `qilinfish.js` 的 `initContentSurvey`（CP3 新增）加：隱藏揭曉題、「下一步」按鈕、盲問沒填時「下一步」指出盲問、鎖定盲問（含「不會付費」「不知道」）、寫 hidden 欄位 `blind_locked`。不動 `initSurveyForm`。版本參數更新。
  - 模板 `survey_content.html` 加「下一步」按鈕、小字、hidden 欄位。沒有 JavaScript 時「下一步」與小字都不顯示（D61），揭曉題照 CP3 一次顯示。
  - i18n：小字、「下一步」用 `sc.` 前綴，中英一起加。
  - CSS：盲問鎖定的樣子與小字。兩份 `qilinfish.css` 同步。
  - Controller／Repository：`blind_locked` 收 0／1，其他值存 0。
  - 新增測試：POST 帶 `blind_locked=1` 存 1；不帶存 0。
- 相依：CP3。
- 依據的決策：
  - D32：沒有 JavaScript 時一次全部顯示，接受這一小部分答案不是盲問。
  - D33：接受鎖定只在瀏覽器；記 `blind_locked` 當旁證，不是保證。
  - D35：選「不會付費」「不知道」時揭曉兩題照樣出現。
  - D61：沒有 JavaScript 時不顯示小字。
- 中英文文案隨票附對照表，使用者驗收時審（D58）。
- 驗收步驟：
  1. 打開 `/survey/content`：看不到揭曉兩題。
  2. 盲問空著按「下一步」：頁面指出盲問。
  3. 填 `120`，按「下一步」：揭曉兩題出現，盲問不能改，小字出現。核對揭曉文字與選項。
  4. 重新載入，選「不會付費」，按「下一步」：揭曉兩題照樣出現。
  5. 關掉 JavaScript 重新載入：三題一次顯示，沒有「下一步」，沒有小字。
  6. 有 JavaScript 送一份、關掉 JavaScript 送一份，查 `blind_locked`。
  7. 英文頁重做第 1–3 步，金額是 US$。
  8. `cmp` 兩份 `qilinfish.css`。
- 預期證據：`mvn test` 全綠；第 1、3、5 步的截圖（中英、1440 與 390）；`SELECT blind_locked FROM survey_content ORDER BY id DESC LIMIT 2` 是 `0`、`1`；`cmp` 沒有輸出。
- 停止條件：鎖定後用一般操作（點、鍵盤、Tab）還能改盲問；或關掉 JavaScript 後送不出去。

### 英文對照

（實作時填。）

## CP5 文章 app 產品頁五段（示範先不能點）

- 交付的行為：
  - B8：H1「每一句都查得到出處」；下面是摘錄（中文附錄 B、英文附錄 C）。這張票只顯示摘錄文字，點了沒有反應（CP6 做）。
  - B11：兩排步驟與說明。
  - B13：付費結構的文字，含維護合約不含一對一技術支援、沒有維護合約就拿不到任何更新。
  - B14：任何地方都不顯示金額。
  - B15：階段說明，主要按鈕連到 `/survey/content`。
  - 第 3 段「實際案例」這張票先放標題與空位，CP8 才填內容。頁面沒有情境卡。
- 範圍：
  - Controller：GET `/products/content`（在 `ProductsController.java` 加方法，或新 Controller），新 ViewBinder。
  - 模板 `products_content.html`，`navbar('products')`。
  - i18n：用獨立前綴 `pc.`，放進新檔 `i18n/products_content(_en).properties` 並加進 `WebConfig.java:42-50`；或放進 `products(_en).properties`，前綴仍是 `pc.`。
  - CSS：「斷鏈 vs 同一條線」兩排圖（`.bl-steps` `:1245` 不夠，要新增）。可沿用 `.pd-stage`（`:1085`）、`.qf-btn--primary`（`:262`）。兩份 `qilinfish.css` 同步。
  - 新增測試：GET `/products/content`、`/en/products/content` 回 200；H1 正確；按鈕 `href` 是 `/survey/content`、`/en/survey/content`；輸出不含 `US$`、`$150`、`$30`、`150 美元`、`30 美元`（D57）。
  - `tools/build_preview.py:34` 的 `TOP` 加 `/products/content`。
- 相依：CP3（按鈕的目標頁要先存在）。
- 依據的決策：D31、D58（中英文文案由助理起草；B11 的說明句、B15「……」之後的文字也由助理起草）、D52（title／description）、D57（金額的驗收搜尋字）。
- 中英文文案隨票附對照表，使用者驗收時審。
- 驗收步驟：
  1. 打開中英頁，核對五段順序、H1、兩排步驟的文字與順序、付費文字。
  2. 在中英頁原始碼搜尋 `US$`、`$150`、`$30`、`150 美元`、`30 美元`（D57）。不搜「30」「150」：示範摘錄有「30 個國家」，證據有「2024-05-30」，會誤判。
  3. 點主要按鈕，到 `/survey/content`（英文頁到 `/en/survey/content`）。
  4. 在中英頁輸出搜尋 `pc.`，結果是 0。
  5. `git diff content/` 沒有改動。
  6. `cmp` 兩份 `qilinfish.css`。
- 預期證據：`mvn test` 全綠；中英整頁截圖（1440、390）；第 2 步搜尋結果 0；`git diff content/` 沒有輸出；`cmp` 沒有輸出。
- 停止條件：頁面需要顯示金額才講得清楚付費結構（和 D10 衝突），停下來問使用者。

### 英文對照

（實作時填。）

## CP6 產品頁示範：點一句展開證據

- 交付的行為：
  - B9：在摘錄裡點任何一句（手機輕觸），那一句旁邊展開原文引句、限定條件、原始網址。限定條件一定和引句一起顯示。
  - B10：引句與限定條件寫在網站自己的文案（i18n `pc.` 前綴）。文章包不變。
- 範圍：
  - 模板 `products_content.html`：每一句標成可操作的元素（可用 Tab 移到、Enter 展開），旁邊放證據面板。
  - i18n：中文 E10、E12（附錄 B）；英文 E12（附錄 C）。逐字照附錄。
  - JS：`qilinfish.js` 新增 `initEvidenceDemo`，綁自己的 data 屬性。一次一句，再點一次收起。版本參數更新。
  - CSS：「點一句展開出處面板」（`.bl-prose sup.cite` `:1233` 只是上標，要新增）。兩份 `qilinfish.css` 同步。
  - 新增測試：中英頁輸出含附錄的引句、限定條件、網址（逐字）。
- 相依：CP5。
- 依據的決策：
  - D47：E10 用 PDF 網址；英文 E12 顯示 `2024-05-30`，日期附註放進限定條件。
  - D48：限定條件顯示附錄全文。
  - D49：中文頁先顯示英文原文，下一行附中文翻譯。
  - D50：一次一句、再點一次收起；Tab 移到、Enter 展開。
- 英文文案：示範內容全部取自附錄，不另起草。其他介面文字（例如「原文」「限定條件」「出處」標籤）由助理起草，隨票附中英對照表，使用者驗收時審（D58）。
- 驗收步驟：
  1. 桌面：點摘錄的每一句，核對展開內容和附錄 B／C 逐字相同。再點一次收起。點另一句，前一句收起。
  2. 390px 手機寬：輕觸每一句，面板不超出畫面。
  3. 只用鍵盤：Tab 到每一句，Enter 展開。
  4. 每一個展開的面板都看得到限定條件。
  5. `git diff content/` 沒有改動。
  6. `cmp` 兩份 `qilinfish.css`。
- 預期證據：`mvn test` 全綠；每一句展開後的截圖（中英、1440 與 390）；`git diff content/` 沒有輸出；`cmp` 沒有輸出。
- 停止條件：限定條件全文（D48）在手機上蓋住摘錄、無法收起——停下來回報，不自行改成摘要。

### 英文對照

（實作時填。）

## CP7 有證據標記的文章，最後多一行連到產品頁

- 交付的行為：B30。`body.html` 有至少一個 `<sup class="cite" data-e="…">` 的文章，最後多一行「這篇文章由麒麟魚的內容工具製作，每一句都查得到出處 →」，連到 `/products/content`；英文文章用英文文案，連到 `/en/products/content`。沒有標記的文章沒有這一行。文章包與 CompanyofOne 不改。
- 範圍：
  - `ArticlePackageRepository.java`：在 `loadOne` 用 `findCtaPosition`（`:168-187`）已經解析的 jsoup `Document`（`:169`），`select("sup.cite[data-e]")` 算一個布林。啟動時算一次（卡點 f）。**一定要篩 `data-e`**：測試 fixture `test-en-image`、`test-zh-video` 有沒有 `data-e` 的 `sup.cite`。
  - `ArticlePackageModel.java`：加布林欄位，比照 `bodyBeforeCta`（`:23`、`:64-65`）。mapper 開 `FAIL_ON_UNKNOWN_PROPERTIES`（`ArticlePackageRepository.java:52`），這個欄位只由程式設定，不從 `article.json` 讀。
  - `blog/article.html`：`:58` 之後、`</article>` 之前加一行，`th:if` 用這個布林。位置在參考資料清單之後、頁尾之前。
  - i18n：`blog(_en).properties` 加兩個 key，用獨立前綴（例如 `blog.sourced.`）。
  - 測試：在 `src/test/resources/articles/` 新增有 `data-e` 的 fixture（或在現有 fixture 加一個），`ArticlePackageRepositoryTest` 測布林；`BlogControllerTest` 測有標記的文章有這一行、連結正確，沒有標記的文章沒有。
- 相依：CP1（同改 `article.html` 與 `BlogControllerTest`）、CP5（連結目標要先存在）。
- 依據的決策：D45（位置在參考資料清單之後、頁尾之前）、D31、D58（文案）。
- 英文文案隨票附對照表，使用者驗收時審。
- 驗收步驟：
  1. 打開 `solo-trust`、`ai-handover`、`shadow-ai`、`shadow-ai-en`，看文章最後。
  2. 點那一行，到正確語言的產品頁。
  3. `git diff content/` 沒有改動。
- 預期證據：`mvn test` 全綠（含新測試）；四篇文章最後的 HTML 片段：前兩篇有、後兩篇沒有；兩張截圖；`git diff content/` 沒有輸出。
- 停止條件：判斷結果和規格「今天的結果」不同（例如 `shadow-ai` 被判成有標記），停下來查 fixture 與篩選條件。

### 英文對照

（實作時填。）

## CP8 產品頁「實際案例」自動列出有證據標記的文章

- 交付的行為：B12。第 3 段列出有證據標記的文章，每一篇連到文章頁。判斷規則和 CP7 相同。該語言沒有任何有證據標記的文章時，第 3 段整段隱藏（D59）。
- 範圍：
  - 產品頁 ViewBinder：用 `ArticlePackageRepository` 的 `findByLang` 加 CP7 的布林篩選，只列同語言文章。
  - 模板 `products_content.html` 第 3 段：用部落格清單的卡片樣式（`templates/fragments/blog.html`），顯示封面、標題、日期。
  - 模板用 `th:if` 判斷清單是否為空；空的就不輸出第 3 段（含段落標題）。
  - 測試：用測試文章包（有標記一篇、沒標記一篇）跑一次，產品頁只列有標記的那篇；中英頁各只列同語言文章。另用一組該語言全部沒有標記的測試文章包，第 3 段（含標題）不出現在輸出裡。
- 相依：CP5、CP7。
- 依據的決策：D46（只列同語言；用部落格清單卡片）、D59（沒有文章就整段隱藏）。
- 英文文案：段落標題隨票附對照表，使用者驗收時審（D58）。
- 驗收步驟：
  1. 打開 `/products/content`：列出 `solo-trust`，沒有 `shadow-ai`。
  2. 打開 `/en/products/content`：列出 `ai-handover`，沒有 `shadow-ai-en`。
  3. 點每一張卡，文章頁打得開。
  4. 空清單：跑「全部沒有標記」那組測試，確認第 3 段整段不在輸出裡，第 2 段之後直接接第 4 段。
- 預期證據：`mvn test` 全綠（含空清單測試）；中英第 3 段截圖；空清單測試的輸出（第 3 段標題的 key 與文字都不在頁面裡）。
- 停止條件：清單結果和規格「今天的結果」不同，停下來查 CP7 的判斷。

### 英文對照

（實作時填。）

## CP9 `/products` 改成入口頁（內容卡先用占位圖）

- 交付的行為：
  - B1：H1「交出去之前，先確定它站得住」。
  - B2：兩張靜態卡，不放影片。
  - B3：查核卡的標題、說明、給誰、`report_clean.jpg`，點了到 `/products/verify`。
  - B4：內容卡的標題、說明、給誰，點了到 `/products/content`。**圖先用占位圖**（素色底加文字，不標「AI 生成」）。示意圖在 CP10 換上。
  - B3、B4「給誰」的「……」由助理起草成完整列舉（D58），不照字面顯示「……」。
  - B5：沒有「治療」「預防」。
  - B6：底部「也接 Web 開發與專案設計 → 聯絡我們」，連到 `/contact`。
- 範圍：
  - `ProductsController.java:25-41`：`/products` 改成入口頁的 ViewBinder 與模板；`/products/verify` 繼續用原本的。
  - 新模板（例如 `products_index.html`），`navbar('products')`。
  - i18n：獨立前綴（例如 `pe.`）。
  - CSS：沿用 `.pd-scenes`／`.pd-scene*`（`:1065-1080` 兩欄卡）、`.pd-also`（`:1092`）。若要新增樣式，兩份 `qilinfish.css` 同步。
  - 占位圖：存 `static/media/`，檔名和 CP10 的正式圖分開（例如 `content_card_placeholder.*`），方便 CP10 換掉後刪除。
  - 新增測試：GET `/products`、`/en/products` 回 200；H1；兩張卡的 `href`；底部連到 `/contact`、`/en/contact`；輸出不含「治療」「預防」。
- 相依：CP1（`/products/verify` 要先存在）、CP5（`/products/content` 要先存在）。
- 依據的決策：
  - D51、D56：內容卡先用占位圖，示意圖獨立一張票（CP10）。**上線條件：CP10 完成才能上線**（D56）。這張票驗收通過，入口頁仍然不能上線。
  - D31、D52、D58：中英文文案（含「給誰」的完整列舉）與 title／description。
- 英文文案隨票附對照表，使用者驗收時審。
- 驗收步驟：
  1. 打開 `/products`、`/en/products`，核對 H1、兩張卡的文字與圖、兩張卡的連結、底部聯絡連結。
  2. 在頁面原始碼搜尋「治療」「預防」（英文頁搜 cure、treat、prevent），結果是 0。
  3. 確認 `/products/verify` 和 CP1 的截圖仍然相同。
  4. 在中英頁輸出搜尋 `pe.`，結果是 0。
  5. 改了 CSS 就 `cmp` 兩份 `qilinfish.css`。
- 預期證據：`mvn test` 全綠；中英整頁截圖（1440、390）；第 2、4 步搜尋結果 0；`cmp` 沒有輸出（有改 CSS 時）。
- 停止條件：`/products/verify` 的截圖和 CP1 不同。

### 英文對照

（實作時填。）

## CP10 內容卡示意圖換上

- 交付的行為：B4 的圖。內容卡顯示新的示意圖，圖上標「示意畫面 · AI 生成」（英文頁用英文標示）。
- 範圍：
  - 助理寫兩三個提示詞，使用者挑一個。
  - 用 CompanyofOne 的 ComfyUI 產生圖（P2）。這是網站以外的依賴（卡點 g）。
  - 圖存 `static/media/`。入口頁內容卡改用新圖；刪掉 CP9 的占位圖。
  - 「示意畫面 · AI 生成」標示：用文字疊在圖上（i18n，中英各一）或直接畫進圖裡，實作時二選一，使用者驗收時看。
- 相依：CP9（換圖的位置）。提示詞與出圖可以先做，不等其他票。**這張票不擋其他票。**
- 依據的決策：D51（圖的內容：助理寫提示詞，使用者挑）、D56（外部依賴；**CP10 完成才能上線**；驗收 B4 時檢查內容卡不是占位圖）、P2。
- 英文文案：英文標示（例如「Illustration · AI-generated」）隨票附對照表，使用者驗收時審。
- 驗收步驟：
  1. 使用者看提示詞，挑一個。
  2. 出圖；使用者看圖。
  3. 打開 `/products`、`/en/products`，內容卡顯示新圖與標示。
  4. 搜尋 repo，沒有任何地方還引用占位圖檔名。
- 預期證據：提示詞清單與使用者的選擇；圖檔；中英入口頁截圖（1440、390）；搜尋占位圖檔名結果 0；`mvn test` 全綠。
- 停止條件：ComfyUI 不能用。停下來回報，入口頁繼續用占位圖，**整份清單不能上線**，直到這張票完成（D56）。

### 英文對照

（實作時填。）

## CP11 `preview.html` 涵蓋新頁

- 交付的行為：B32。`/survey` 是 301 的網站上，`python tools/build_preview.py` 跑完、結束碼 0；`preview.html` 可以打開 `/products`、`/products/verify`、`/products/content`、`/survey/verify`、`/survey/content`，中英文共十頁。
- 範圍：
  - `tools/build_preview.py:34` 的 `TOP`：確認是 `/about`、`/products`、`/products/verify`、`/products/content`、`/cases`、`/contact`、`/resources`、`/survey/verify`、`/survey/content`、`/blog`（CP1、CP3、CP5 已逐張加入，這張票核對）。`:92-93` 的 fetch 遇轉址回 None、`:132` 要求 START 全部成功——`TOP` 不可以有會轉址的網址。
  - 預覽裡盲問的「下一步」能按（只是前端）；送出照舊不動作。
  - 重新產生 `preview.html`。README「預覽」一節若有提到 `/survey?pick=`，改成 `/survey/verify?pick=`。
- 相依：CP1–CP9。CP10 晚於這張完成時，CP10 完成後再跑一次。
- 依據的決策：D53（`TOP` 換成新網址；預覽裡「下一步」能按）。
- 驗收步驟：
  1. 正式模式啟動網站，跑 `python tools/build_preview.py`，記結束碼。
  2. 打開 `preview.html`，逐一打開十頁。
  3. 在預覽的 `/survey/content` 按「下一步」。
- 預期證據：命令輸出與結束碼 0；十頁截圖；「下一步」按下後揭曉題出現的截圖。
- 停止條件：`build_preview.py` 中止，或有頁面在預覽裡打不開。

---

## 相依圖

```
CP1 ─┬─ CP2
     ├─ CP3 ─┬─ CP4
     │       └─ CP5 ─┬─ CP6
     │               ├─ CP7 (也依 CP1) ── CP8 (也依 CP5)
     │               └─ CP9 (也依 CP1) ── CP10（不擋其他票；提示詞與出圖可提早做）
     └────────────────────────────────────── CP11（依 CP1–CP9）
```

建議順序：CP1 → CP2 → CP3 → CP4 → CP5 → CP6 → CP7 → CP8 → CP9 → CP11；CP10 平行進行，CP10 完成才能上線（D56）。

## 整份清單的驗收方式

- 全部做完之後，**一次交使用者驗收**。驗收前不提交。
- 驗收照 `spec.md`「驗收方式」逐列走：本機正式模式啟動（`docker compose up` 或 `java -jar`），瀏覽器桌面與 390px 手機寬，SQLite 用 `docker cp` 取出 `survey.db` 查。
- 交驗收時一起交：
  1. 每張票的證據。
  2. 每張票的「英文對照」，請使用者審中英文文案（P3、D31、D58）。
  3. 兩份 `qilinfish.css` 的 `cmp` 結果（沒有輸出）。
  4. `mvn test` 全綠與 `build_preview.py` 結束碼 0。
- **產品頁驗收時，提醒使用者 README「後續」裡「每篇部落格文章都能點一句看出處」那一項**（D21）。`/products/content` 的示範目前是手寫在網站裡的兩筆證據，做完那一項後要改讀文章包。
- CP10 若還沒完成，入口頁是占位圖。使用者可以先驗收其他票，但**CP10 完成才能上線**（D56）。驗收 B4 時，檢查內容卡不是占位圖。
- 驗收通過後：本檔與 `spec.md` 的狀態欄蓋驗收戳記（日期、提交 hash）；`CONTEXT.md`、README 跟著更新的部分一起提交。

## 已定案

拆票時發現的規格缺口，使用者 2026-11-04 定案如下。規格未決 1–26 全部照建議，定案為 D31–D56。

| 缺口 | 定案 | 用到的票 |
|---|---|---|
| 規格沒有未決 26 | 已不存在：規格已補未決 26，編號連續（定案為 D56） | CP9、CP10 |
| B4 的驗收在 CP10 完成前過不了 | D56：CP10 完成才能上線 | CP9、CP10、整份驗收 |
| B13–B14 搜「30」「150」會誤判 | D57：搜 `US$`、`$150`、`$30`、`150 美元`、`30 美元` | CP5 |
| 中文文案不完整（問卷開場、B11 說明句、B15 後文、B3/B4「……」） | D58：中英文文案都由助理起草，各票附對照表；「……」由起草完整列舉 | 全部有文案的票 |
| 「實際案例」沒有文章時顯示什麼 | D59：整段隱藏 | CP8 |
| B27 只講 `?pick=` | D60：301 帶整個 query string | CP1 |
| POST `/survey` 和 B27 的範圍 | D55：POST 照收、GET 301，不用 308 | CP1 |
| 沒有 JavaScript 時盲問小字 | D61：不顯示 | CP4 |

### 程式盤點更正（不是規格缺口，給實作者）

- `code-map.md` 第 2 項寫 `WebConfig.java:319-327`（basename）與 `:331`（`setUseCodeAsDefaultMessage`）。實際檔案只有 62 行：basename 在 `:42-50`，`setUseCodeAsDefaultMessage(true)` 在 `:54`。本檔用實際行號。
- `survey.html:320` 的 `qilinfish.js` 版本參數是 `v='20261026-en'`，不是 `20261026-pick`；`about.html:58`、`blog.html:27` 也載入 `v='20261026-pick'`。改 `qilinfish.js` 的票要一起更新這幾處。
