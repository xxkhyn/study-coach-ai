# Study Coach AI

資格・技術学習を管理するWebアプリのMVP第1段階です。

## 推奨ディレクトリ構成

```text
.
├── backend
│   ├── src/main/java/com/studycoachai
│   │   ├── config
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

## DB設計

### users

| column | type | note |
| --- | --- | --- |
| id | bigint | PK |
| email | varchar(120) | unique |
| password | varchar(120) | 仮実装 |
| display_name | varchar(80) | 表示名 |

### study_targets

| column | type | note |
| --- | --- | --- |
| id | bigint | PK |
| user_id | bigint | users.id |
| name | varchar(120) | 学習対象名 |
| category | varchar(80) | 資格、技術など |
| exam_date | date | 試験日 |
| goal_date | date | 目標日 |
| memo | varchar(1000) | メモ |
| created_at | timestamptz | 作成日時 |
| updated_at | timestamptz | 更新日時 |

### study_tasks

| column | type | note |
| --- | --- | --- |
| id | bigint | PK |
| user_id | bigint | users.id |
| study_target_id | bigint | study_targets.id |
| title | varchar(160) | タスク名 |
| field_name | varchar(80) | 分野 |
| planned_minutes | integer | 予定学習時間 |
| due_date | date | 期限 |
| completed | boolean | 完了状態 |
| created_at | timestamptz | 作成日時 |
| updated_at | timestamptz | 更新日時 |

## API一覧

当面は `userId=1` の仮ユーザーで動かします。

| method | path | description |
| --- | --- | --- |
| GET | `/api/dashboard?userId=1` | ダッシュボード集計 |
| GET | `/api/study-targets?userId=1` | 学習対象一覧 |
| POST | `/api/study-targets?userId=1` | 学習対象作成 |
| PUT | `/api/study-targets/{id}?userId=1` | 学習対象更新 |
| DELETE | `/api/study-targets/{id}?userId=1` | 学習対象削除 |
| GET | `/api/study-tasks?userId=1` | タスク一覧 |
| POST | `/api/study-tasks?userId=1` | タスク作成 |
| PUT | `/api/study-tasks/{id}?userId=1` | タスク更新 |
| DELETE | `/api/study-tasks/{id}?userId=1` | タスク削除 |

## 実装順序

1. Spring Boot雛形を作成
2. users / study_targets / study_tasks のEntity、Repositoryを作成
3. DTO、Service、Controller、例外処理を作成
4. React雛形を作成
5. ダッシュボード、学習対象、タスクの画面を作成
6. 学習対象とタスクの一覧・登録・編集・削除を作成
7. Docker Composeで frontend / backend / db を起動

## 起動

```bash
docker compose up --build
```

frontend: http://localhost:5173

backend: http://localhost:8080
