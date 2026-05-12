# Study Coach AI

Spring Boot + React + PostgreSQL で作る、資格・技術学習の管理アプリです。

## 構成

```text
study-coach-ai
├─ backend
│  └─ src/main/java/com/studycoachai
│     ├─ controller
│     ├─ dto
│     ├─ entity
│     ├─ exception
│     ├─ repository
│     ├─ security
│     └─ service
├─ frontend
│  └─ src
│     ├─ components
│     ├─ pages
│     ├─ services
│     └─ types
└─ docker-compose.yml
```

## 主な機能

- ユーザー登録・ログイン
- 学習対象、学習タスク、学習ログ、演習ログの管理
- 問題登録、問題演習、解答履歴、間違えた問題の復習
- 問題CSVインポート
- ダッシュボード、分析画面、AIアドバイス

## 起動

Docker Desktop を起動してから実行します。

```bash
docker compose up --build
```

- frontend: http://localhost:5173
- backend: http://localhost:8080
