# itemPoint — 日幣兌換券

日出伺服器（sunrisemc-tw）自製插件。玩家手持「**日幣兌換 右鍵**」金錠右鍵即可兌換成日幣（PlayerPoints）。

## 功能

- **兌換**：手持金錠 `日幣兌換 右鍵`（lore：`每個可兌換 <數量> 日幣`）按右鍵 → 發放對應日幣並消耗 1 張。
- **發券（管理）**：`/givecoin <每個可兌換日幣數量> [張數]`（權限 `itemcoin.use`）。

## 行為細節

- 只認「主手 + 右鍵（空氣/方塊）」，名稱比對忽略顏色碼，材質限定 `GOLD_INGOT`。
- 兌換金額從 lore `每個可兌換 N 日幣` 解析，找不到數字則不動作。
- **先給點數、成功才扣券**（失敗不吞券）。
- 同名事件 250ms 去抖，避免一次點擊重複觸發。
- 兌換成功寫入 console log（`[itemPoint] <玩家> 兌換 ... -> +N 日幣`）。

## 相容性

- Paper / Canvas（Folia）皆可。`plugin.yml` 標記 `folia-supported: true`（缺此行 Canvas 會直接拒載）。
- 依賴：PlayerPoints（`softdepend`）、Spigot API 1.21.x。
- 建置：`mvn -DskipTests clean package`（需本機 Maven repo 內有 `org.black_ixx:PlayerPoints:3.3.5`）。

## 部署

產出 `target/itemPoint-<version>.jar` → 放進 `plugins/` → `plugman load itemPoint`（免重啟）。
