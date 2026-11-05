# article-evidence 票單

狀態：已完成（2026-11-05 驗收，提交 qilinfish_web 049169f–b964772、CompanyofOne 1c34b9c–f9a0bf9；F10–F13 已併入規格）。規格：`spec.md`（同目錄）。票單清單由 Fable 依使用者授權核准（2026-11-05，F2–F9 已併入）。

| 票單 | 行為 | 規格 | 相依 | repo | 狀態 |
|---|---|---|---|---|---|
| AE1 | 匯出帶 evidence.json，缺必填就不匯出 | B1–B5、F2–F5 | — | CompanyofOne | 已完成（2026-11-05 驗收，提交 CompanyofOne 1c34b9c） |
| AE2 | 網站啟動時檢查證據 | B6–B8 | 證據檔格式 | qilinfish_web | 已完成（2026-11-05 驗收，提交 049169f） |
| AE3 | 重新匯出 solo-trust、ai-handover，網站以真實文章包啟動 | B1、B6–B8 | AE1、AE2 | 兩邊 | 已完成（2026-11-05 驗收，提交 qilinfish_web bfca667、CompanyofOne cc0f51a） |
| AE4 | 文章頁點 [n] 在段落下方展開證據 | B9–B13、F6 | AE3 | qilinfish_web（CSS 同步到 CompanyofOne） | 已完成（2026-11-05 驗收，提交 4c92ef6；CSS 同步 CompanyofOne 7877717） |
| AE5 | 產品頁示範改讀文章包、兩行文案、刪手寫內容 | B14–B18、F1、F7、F8 | AE4 | qilinfish_web | 已完成（2026-11-05 驗收，提交 b964772） |

## 證據檔格式（AE1 寫、AE2 讀）

文章包根目錄 `evidence.json`：

```json
{"evidence":[{"id":"E10","publisher":"Ipsos","title":"Ipsos AI Monitor 2025","date":"2025-06-06",
  "quote":[{"text":"\"Seventy-nine percent agree …\"","translation":false},
           {"text":"中文：79% 同意…","translation":true}],
  "note":null,"qualifiers":"…","url":"網址欄原文（可含多個網址與說明文字）"}]}
```

- 只含正文 `data-e` 用到的證據，順序照 E 編號。
- `quote` 每行一個元素，照查核員原文；「中文：」開頭的行 `translation=true`，保留前綴（F4）。
- `publisher`、`title`、`date` 照原文字串，缺欄才 `null`（F3）。`note` 是選用欄，不顯示（F2）。
- 必填：`quote` 至少一行、`qualifiers` 非空、`url` 至少含一個 http(s) 網址。

## AE1　匯出帶 evidence.json（CompanyofOne）

- 範圍：`app/` 的匯出。解析 `verified/evidence.md` 的中英兩套欄名；F2 的解析規則；寫出 `evidence.json`；缺必填就匯出失敗，列出哪幾筆缺哪一項；沒有標記的文章不帶證據檔。
- 驗收步驟：真實 solo-trust、ai-handover 匯出到暫存目錄；筆數 58、24；article.json、body.html、media 跟 qilinfish_web 現有文章包相同（F5）；複製一篇測試文章、刪一筆限定條件，匯出失敗並列出那一筆。
- 預期證據：測試結果（相對於基準）、兩份 evidence.json 摘要、比對結果。
- 停止條件：F5 的比對不同。

## AE2　網站啟動時檢查證據（qilinfish_web）

- 範圍：`ArticlePackageRepository`、`ArticlePackageModel`、測試資料。
- 驗收步驟：測試資料覆蓋：有標記沒有證據檔、E 找不到、缺必填、全部齊全、沒有標記。
- 預期證據：測試結果（相對於基準）、失敗訊息列出哪一包、哪幾筆。

## AE3　重新匯出兩篇文章包

- 範圍：用 AE1 的匯出，寫進 `qilinfish_web/content/articles/{solo-trust,ai-handover}/`。
- 驗收步驟：git diff 只多了 `evidence.json`；網站以真實文章包啟動成功。
- 預期證據：git status／diff 摘要、啟動紀錄。
- 停止條件：除了 `evidence.json` 還有別的差異。

## AE4　文章頁點 [n] 展開證據（qilinfish_web）

- 範圍：文章頁 template、JS、CSS；兩份 qilinfish.css 同步（F6）。
- 驗收步驟：中英各一篇，點開 F9 的 5 筆標記；一次只開一個；再點關掉；鍵盤操作；欄名跟文章語言一致；沒有 JS 時跳到參考文獻。
- 預期證據：桌機與手機寬度截圖、測試結果。

## AE5　產品頁示範改讀文章包（qilinfish_web）

- 範圍：`/products/content` 中英版；兩行文案（B16、B17）；刪手寫 properties；README「後續」登記殘留物並結案本功能那條（F8）；重新產生 `preview.html`。
- 驗收步驟：中文示範是 solo-trust 第 20 行那段，顯示到最後一個標記 [4] 為止（F7、D11），英文是 ai-handover 第 45 行那段（F1）；點法與面板跟文章頁相同。
- 預期證據：中英截圖、測試結果、刪除清單。
