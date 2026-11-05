# fact-limits 票單

狀態：已核准（2026-11-05 Fable 依使用者授權核准；F1–F15 見 `spec.md`「Fable 決斷」），實作中。規格：`spec.md`（同目錄）。

| 票單 | 行為 | 規格 | 相依 | repo | 狀態 |
|---|---|---|---|---|---|
| FL1 | 製作與段落修改的提示詞要求「每個事實說法」都有標記 | B1–B4、F13 | — | CompanyofOne | 待實作 |
| FL2 | 程式標記檢查：表格列、查無句缺標記就不匯出，文章頁列出位置 | B5、B9（程式部分）、F1、F2、F9、F11 | — | CompanyofOne | 待實作 |
| FL3 | AI 標記檢查清單：跑 3 次取聯集，逐項處理才匯出 | B6–B8、B9（AI 部分）、F3–F8、F14 | FL2 | CompanyofOne | 待實作 |
| FL4 | solo-trust、ai-handover 補標記，通過兩道檢查，重新匯出 | B12、F10 | FL1、FL2、FL3 | 兩邊 | 待實作 |
| FL5 | 網站改成新定位文案；README 待決條結案 | B10、B11、B13、F12、F15 | FL4（上線順序） | qilinfish_web（另改 CompanyofOne `docs/tracking.md` TR55 的舊句） | 待實作 |

相依順序：FL1 與 FL2 互不相依，可同時做；FL3 在 FL2 之後；FL4 在 FL1–FL3 之後；FL5 在 FL4 之後。

## FL1　製作與段落修改規則

- 範圍：`ProduceService.prompt`、`promptEn`；`SectionEditService.prompt`、`promptEn`。把「每個數字和引言都對應 E 編號」改成 B1–B3 的規則：要標的類別（數字、引言、事件、和同節證據意思一致的重點句、查無結果、表格每一列、限度句）；不標的類別（作者的判斷、建議、提問、轉場；重點句後面的建議半句）；限度句和它說明的事實共用一個標記、標記放在限度句後面；表格每一列（表頭除外）至少一個標記，該列標記指向的證據涵蓋這一列每一格。中英各一份，意思相同。
- 連帶：golden 固定檔 `zh-produce-prompt.txt`、`zh-section-edit-prompt.txt` 用 `-Dgolden.update=true` 重產（F13）。CompanyofOne `CONTEXT.md` 已改、未提交的「事實說法」詞條隨本票提交（F15）。
- 不在範圍：既有文章、檢查、匯出。
- 驗收步驟：單元測試斷言四份提示詞含上述每一類；舊固定檔與新固定檔的差異只在這些規則；`mvn test` 全綠。
- 預期證據：測試輸出、兩份固定檔的 diff。
- 停止條件：改提示詞會改到其他已決定的寫作規則（例如參考資料格式、SEO、素材位置）。

## FL2　程式標記檢查

- 範圍：一個檢查元件，讀成品 HTML，回傳不通過的位置清單：
  1. 表格每一列（F2：有 `<tbody>` 時是 tbody 的每個 `tr`；沒有時是 `thead` 以外、不是全由 `th` 組成的 `tr`）至少一個 `sup.cite`。位置寫「第 n 個表格第 m 列：列首文字」。
  2. 含「我們沒有找到」或 "we did not find"／"we found no"／"we could not find"（不分大小寫）的句子，在它後面、同一區塊內（F1：最近一層 `p`／`li`／`td`／`th`／`figcaption`／`blockquote`，都沒有時用最近一層 `div`／`aside`／`section`；文件順序）有 `sup.cite`。位置寫句子片段。
  匯出前執行（F9：既有檢查之後、素材檔之前），不通過就丟例外、不動舊包，訊息列出每個位置。文章頁在驗收、已完成階段顯示同一結果（通過／不通過＋位置）。
- 連帶：開工時先把 `content/articles/solo-trust/`、`ai-handover/` 整個資料夾複製成 `fl-solo-trust/`、`fl-ai-handover/`，`.gitignore` 加 `content/articles/fl-*/`（F11）。
- 驗收步驟：合成片段的單元測試（有／無 tbody、全 th 列、查無句在 p／span.sv-src／標記之後／標記之前、英文三種寫法）；對 `fl-solo-trust` 跑檢查，列出表 1 四列、表 2 兩列、三句查無（含表 1 下方 `span.sv-src` 那句）；`fl-ai-handover` 通過；`fl-solo-trust` 在 app 按匯出被擋下、訊息列出 9 個位置；文章頁截圖。
- 預期證據：測試輸出、匯出被擋的訊息、文章頁截圖。
- 停止條件：真實文章出現規則沒涵蓋的寫法（例如查無句另有說法），需要改規則。

## FL3　AI 標記檢查清單

- 範圍：
  - 執行：用 app 現有 `AiSource`（`codex exec`），提示詞以原型 v2 為底並補 D12、D13 與英文查無寫法（F4）；body.html、evidence.json 走匯出同一條路產生（F14）；一個背景工作內 3 個執行緒同時跑 3 次，任何一次失敗整次失敗，執行中 state 為 RUNNING、完成後還原（F3）；結果取聯集（F5）存進 `cite-check.json`（F6），record.md 加一列（耗時、項數）。
  - 文章頁（驗收、已完成階段）「標記檢查」區：最近一次清單，每項顯示原文片段、類別、理由、狀態；每項有「補標記」（連到該片段所在段的修改意見表單）與「不是事實說法」按鈕；區塊有「跑 AI 檢查」按鈕（F8）。選過「不是事實說法」的片段記在 `cite-check.json`，重跑時以片段比對不再列出（U1）。
  - 匯出關卡（F7）：`cite-check.json` 存在、SHA-256 等於目前 `article.html`、每項都是「不是事實說法」；否則不匯出，訊息說明缺哪一個或卡在哪幾項。
  - 補標記用現有的段落修改，不新增編輯功能。
- 驗收步驟：假 AI 的流程測試：3 次輸出合併、重複與包含片段合併、一次失敗整次失敗、標「不是事實說法」後重跑不列、沒跑過／文章改過／有未處理項時匯出被擋、全部處理後匯出通過。真實執行：在 `fl-ai-handover` 上跑一次，記 3 次耗時與聯集項數；標一項後重跑，該項不再列出；文章頁截圖；按匯出被擋，訊息列出未處理項。「全部處理後匯出通過」在 FL4 的真實文章上證明，副本不匯出（F11）。
- 預期證據：測試輸出、`cite-check.json` 內容、耗時與項數、文章頁截圖、匯出被擋的訊息。
- 停止條件：3 次聯集在 ai-handover 上列出的項目超過 15 項（遠超原型量測），或真實執行失敗率高於 1/3。

## FL4　補標記並重新匯出

- 範圍：solo-trust（中文）、ai-handover（英文，段落修改走 `promptEn`）。用段落修改補標記：FL2 列出的位置（表 1 四列、表 2 兩列、三句查無）；FL3 清單裡屬於事實說法的項目（含 3 句粗體導言、限度句）；其餘按「不是事實說法」。只用 `verified/evidence.md` 已有的 E 編號，不新增證據、不刪句子、不改句意。補完後文章會退回驗收階段，按 app 的「驗收」回到已驗收（F10），再跑 FL2、FL3 通過，匯出到 qilinfish_web `content/articles/`。加真實內容測試：兩篇現行成品通過 B5（照 `EvidenceRealContentTest` 的做法）。
- 驗收步驟：兩篇 FL2 通過；FL3 最近一次每項已處理、SHA-256 相符；匯出成功；qilinfish_web 測試全綠、網站啟動檢查通過；補標記前後的標記數（solo-trust 49 →、ai-handover 37 →）。
- 預期證據：每篇補了哪些標記（位置、E 編號、依據的原文引句）、按「不是事實說法」的清單與理由（寫在驗收報告）、record.md 新增列、匯出結果、兩邊測試輸出。
- 停止條件：P2（某個事實說法在 evidence.md 找不到支撐）；補標記要改寫句子意思；FL3 在補完的 solo-trust 上仍列出看起來真的漏標、卻找不到證據的項目（規格停止條件第 2 條）。

## FL5　網站新定位文案

- 範圍：B10 的 8 個鍵 × 中英 = 16 條 properties 照討論頁第 11 節的對照表改（F12）；`products_content.html`、`survey_content.html` 的 3 處靜態備用文字；`BlogControllerTest`、`ProductsContentControllerTest`、`ProductsIndexControllerTest` 的斷言；`python tools/build_preview.py` 重新產生 `preview.html`；README「後續」的「定位句『每一句都查得到出處』字面上不成立」那條蓋日期結案並指向本規格；qilinfish_web `CONTEXT.md` 已改、未提交的詞條與 `spec.md`、`tickets.md` 隨本票提交（F15）；CompanyofOne `docs/tracking.md` TR55 的「每一句都查得到出處」改成新句。content-product 規格不改（決策文件，保留為歷史，D2 由本規格取代）。網站 D17 不改（B11）。
- 驗收步驟：中英產品頁、產品總覽、部落格文章最後一行（solo-trust、ai-handover）、問卷開場的截圖顯示新文案；點 solo-trust 表 1 一列的標記，看到證據與限定條件；`grep -ri` 確認 i18n、模板、測試沒有「每一句」與 "every sentence"；兩個 repo 測試全綠；FL4 的文章包提交在本票之前（B13）。
- 預期證據：截圖、grep 結果、測試輸出、提交順序。
- 停止條件：文案在版面上換行或溢出（例如標題太長），需要改字。
