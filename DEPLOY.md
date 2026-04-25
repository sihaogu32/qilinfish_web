# 部署指南 — Cloudflare Tunnel + Email Routing

把 `qilinfish.com` 接到你自己機器（或 VPS）上跑的 Spring Boot，全程不用開公網埠。

---

## 架構

```
            ┌─────────────────────────┐
 訪客 ─────►│ Cloudflare Edge          │
            │  · CDN / WAF / TLS       │
            │  · DNS qilinfish.com     │
            │  · Email Routing (MX)    │
            └────────┬────────────────┘
                     │ Tunnel (outbound only)
                     ▼
            ┌─────────────────────────┐
            │ 你的伺服器               │
            │  ├─ qilinfish-web :8080  │  Spring Boot
            │  └─ cloudflared          │  Tunnel client
            └─────────────────────────┘

寄信流程:
  使用者填表單 → Spring Boot → Gmail SMTP
                              → 寄到 qilinfish@qilinfish.com
                              → Cloudflare Email Routing 轉發
                              → 你的 qilinfish@gmail.com 收到
```

---

## Step 1 — 在 Cloudflare 加入網域

1. Cloudflare Dashboard → **Add a site** → 輸入 `qilinfish.com`
2. 依指示把 Name Servers 切到 Cloudflare 提供的兩個 NS

---

## Step 2 — 設定 Cloudflare Email Routing（收信）

1. Dashboard → 選擇 `qilinfish.com` → **Email** → **Email Routing**
2. **Enable Email Routing**（系統會自動幫你加 MX / SPF / DKIM）
3. **Destination addresses** → 加入 `qilinfish@gmail.com` → 認證
4. **Routing rules** → Custom address：
   - `qilinfish@qilinfish.com` → `qilinfish@gmail.com`
   - 也可以加 catch-all：`*@qilinfish.com` → `qilinfish@gmail.com`

> 重要：Email Routing 只負責**接信轉發**。寄信走 Gmail SMTP（下一步）。

---

## Step 3 — 開 Gmail App Password（寄信）

1. 必須先開啟 Google 帳號的 **2-Step Verification**
2. 前往 https://myaccount.google.com/apppasswords
3. 產生一組 16 字元應用程式密碼，記下來

---

## Step 4 — 建立 Cloudflare Tunnel

1. Dashboard → **Zero Trust** → **Networks** → **Tunnels** → **Create a tunnel**
2. 選 **Cloudflared**，命名為 `qilinfish-web`
3. 複製出現的 **Tunnel Token**（很長一串）
4. 進入 Tunnel 的 **Public Hostname** 頁籤，新增：
   - Subdomain: 留空（或 `www`）
   - Domain: `qilinfish.com`
   - Service: `HTTP` → `qilinfish-web:8080`
   - 同樣再加一筆 `www.qilinfish.com` → `qilinfish-web:8080`

---

## Step 5 — 部署到伺服器

```bash
# 1. 把整個 repo clone 到伺服器
git clone <repo-url> qilinfish.com
cd qilinfish.com

# 2. 建立 .env
cp .env.example .env
vi .env
# 填入：
#   MAIL_USERNAME = qilinfish@gmail.com
#   MAIL_PASSWORD = (Step 3 拿到的 App Password)
#   MAIL_TO       = qilinfish@qilinfish.com
#   CLOUDFLARE_TUNNEL_TOKEN = (Step 4 拿到的 Token)

# 3. 啟動
docker compose up -d --build

# 4. 查看狀態
docker compose ps
docker compose logs -f qilinfish-web
docker compose logs -f cloudflared
```

幾分鐘後，`https://qilinfish.com` 就會指向你的 Spring Boot 服務。

---

## 驗收

| 檢查項                                   | 怎麼測 |
|------------------------------------------|--------|
| 網站可開                                 | 瀏覽器打開 `https://qilinfish.com` |
| TLS 憑證                                 | 由 Cloudflare 自動發放，瀏覽器顯示鎖頭 |
| 表單寄信                                 | 在 `/contact` 送出 → Gmail 應該幾秒內收到 |
| 收信轉發                                 | 直接寄信到 `qilinfish@qilinfish.com` → Gmail 應該收到 |
| 不開公網埠                               | 伺服器 firewall 不用開 80/443 |

---

## 後續

- **加入 DB**：把 `application.yml` 的 datasource 區塊解註，移除 `autoconfigure.exclude` 兩行；compose 加一個 `mysql:` service
- **Cloudflare Access** 鎖後台：未來若加 admin 介面，可在 Tunnel 對應 hostname 開 Access policy（社群帳號 / OTP / SSO）
- **Logs / Metrics**：可加 Loki + Grafana 或直接接 Cloudflare Logpush
