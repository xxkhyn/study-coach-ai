# Study Coach AI

Spring Boot + React + PostgreSQLで作る、資格・技術学習管理アプリの第1段階です。

## 構成

```text
study-coach-ai
├── backend
│   ├── src/main/java/com/studycoachai
│   │   ├── controller
│   │   ├── dto
│   │   ├── entity
│   │   ├── exception
│   │   ├── repository
│   │   └── service
│   └── pom.xml
├── frontend
│   ├── src
│   │   ├── components
│   │   ├── pages
│   │   ├── services
│   │   └── types
│   └── package.json
└── docker-compose.yml
```

## 第1段階でできること

- 学習対象の登録、一覧、編集、削除
- 学習タスクの登録、一覧、編集、削除
- タスクの完了切り替え
- `userId=1` の仮ユーザーで動作

## API

| method | path | description |
| --- | --- | --- |
| GET | `/api/study-targets` | 学習対象一覧 |
| POST | `/api/study-targets` | 学習対象作成 |
| GET | `/api/study-targets/{id}` | 学習対象詳細 |
| PUT | `/api/study-targets/{id}` | 学習対象更新 |
| DELETE | `/api/study-targets/{id}` | 学習対象削除 |
| GET | `/api/study-tasks` | 学習タスク一覧 |
| POST | `/api/study-tasks` | 学習タスク作成 |
| GET | `/api/study-tasks/{id}` | 学習タスク詳細 |
| PUT | `/api/study-tasks/{id}` | 学習タスク更新 |
| PATCH | `/api/study-tasks/{id}/complete` | 学習タスクの完了状態更新 |
| DELETE | `/api/study-tasks/{id}` | 学習タスク削除 |
| GET | `/api/study-logs` | 学習ログ一覧 |
| POST | `/api/study-logs` | 学習ログ作成 |
| GET | `/api/study-logs/{id}` | 学習ログ詳細 |
| PUT | `/api/study-logs/{id}` | 学習ログ更新 |
| DELETE | `/api/study-logs/{id}` | 学習ログ削除 |
| GET | `/api/study-logs/weekly-summary` | 今週の学習時間集計 |
| GET | `/api/study-logs/by-target/{studyTargetId}` | 学習対象別の学習ログ一覧 |

## 起動

Docker Desktopを起動してから以下を実行します。

```bash
docker compose up --build
```

frontend: http://localhost:5173

backend: http://localhost:8080
