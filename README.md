# NFC Museum Guide — Android + FastAPI + PostgreSQL

Проект состоит из двух частей:

```text
NfcMuseumGuide/
├── app/        # Android-приложение на Kotlin + Jetpack Compose
├── backend/    # FastAPI-сервер
└── docker-compose.yml  # PostgreSQL + API в Docker
```

Android-приложение можно использовать локально, в админ-панели есть блок.
## Что делает сервер

Backend хранит данные в PostgreSQL:

- экспонаты;
- фото экспонатов;
- историю NFC-сканов;
- избранное / паспортные отметки по устройству (пока не реализовано, тк для учебной не создавались пользователи, роли и аккаунты)

FastAPI автоматически даёт Swagger-документацию по адресу:

```text
http://localhost:8000/docs
```

## Почему FastAPI + PostgreSQL

FastAPI хорошо подходит для REST API и поддерживает работу с SQL-базами через SQLModel/SQLAlchemy. В официальном гайде FastAPI указано, что FastAPI не привязывает проект к конкретной базе, а SQLModel построен поверх SQLAlchemy и Pydantic; также в списке поддерживаемых SQLAlchemy баз есть PostgreSQL.  
Retrofit часто используют на Android, но в этой версии я сделал сетевой слой проще — через OkHttp и `org.json`, чтобы не усложнять проект дополнительными DTO/конвертерами.

## Запуск сервера

В корне проекта выполни:

```bash
docker compose up --build
```

После запуска будут доступны:

```text
API:     http://localhost:8000
Swagger: http://localhost:8000/docs
Postgres: localhost:5432
```

Локальная БД подключение:

```text
database: museum
user:     museum
password: museum
```

## Как подключить Android к серверу

### Реальный телефон (ИСПОЛЬЗУЙ ТОЛЬКО РЕАЛЬНЫЙ)
Телефон и компьютер должны быть в одной Wi‑Fi сети.

1. Узнай IP компьютера, например `192.168.1.10`.
2. В админ-панели приложения укажи:

```text
http://192.168.1.10:8000
```

3. Нажми **Проверить**.

## Админ-панель и автоматическая синхронизация

В админ-панели настройки сервера:

### Сохранить
Сохраняет адрес сервера в `SharedPreferences`. После изменения адреса приложение сразу пробует подтянуть каталог с нового сервера.

### Проверить
Вызывает:

```text
GET /health
```

Если сервер работает, приложение покажет `OK`.

### Как теперь работает синхронизация

Приложение само понимает, когда нужно работать с сервером:

| Событие в приложении | Что происходит |
|---|---|
| Запуск приложения | подтягивается каталог и избранное с сервера |
| Возврат в приложение | каталог обновляется, но не чаще одного раза в минуту |
| Изменение адреса сервера | сразу выполняется загрузка данных с нового сервера |
| Добавление экспоната | экспонат сохраняется локально и сразу отправляется через `PUT /api/exhibits/{id}` |
| Редактирование экспоната | новая версия сохраняется локально и сразу отправляется на сервер |
| Удаление экспоната | экспонат удаляется локально и сразу отправляется `DELETE /api/exhibits/{id}` |
| Добавление в избранное | локально меняется сердечко и отправляется `POST /api/favorites` |
| Удаление из избранного | локально убирается сердечко и отправляется `DELETE /api/favorites/{device_id}/{exhibit_id}` |
| NFC-скан | событие сохраняется локально и отправляется через `POST /api/scan-logs` |
| Импорт JSON | каталог сохраняется локально и отправляется на сервер |

### Что будет без интернета

Если сервер недоступен, приложение продолжит работать локально. Вверху/админ-панели будет статус вроде:

```text
Офлайн-режим: сервер недоступен
```

Для добавленных/отредактированных/удалённых экспонатов приложение хранит локальные “грязные” изменения:

```text
dirty_exhibit_ids
pending_delete_ids
```

При следующей успешной автосинхронизации приложение сначала отправит эти ожидающие изменения на сервер, а потом скачает свежий каталог обратно.

## Структура backend

```text
backend/
├── Dockerfile
├── requirements.txt
├── uploads/              # сюда сервер сохраняет фото
└── app/
    ├── __init__.py
    ├── database.py       # подключение к PostgreSQL
    ├── main.py           # FastAPI endpoints
    ├── models.py         # SQLModel-таблицы и схемы
    └── seed_data.py      # стартовые экспонаты
```

## Главные endpoints

```text
GET    /health
GET    /api/exhibits
GET    /api/exhibits/{id}
POST   /api/exhibits
PUT    /api/exhibits/{id}
PATCH  /api/exhibits/{id}
DELETE /api/exhibits/{id}
POST   /api/exhibits/{id}/image
POST   /api/scan-logs
GET    /api/scan-logs
GET    /api/stats/scan-counts
POST   /api/favorites
GET    /api/favorites/{device_id}
DELETE /api/favorites/{device_id}/{exhibit_id}
```

## Что добавлено в Android

Новые файлы:

```text
app/src/main/java/com/example/nfcmuseumguide/remote/
├── ServerConfig.kt       # хранит base URL сервера
└── MuseumApiClient.kt    # HTTP-запросы к FastAPI через OkHttp
```

ЧТО НУЖНО ДЛЯ РАБОТЫ:

- разрешение `INTERNET` в `AndroidManifest.xml`;
- зависимость OkHttp;

автоматическая событийную синхронизацию:
  - `autoPullFromServer()`;
  - `pushExhibitToServer()`;
  - `deleteExhibitFromServer()`;
  - `pushFavoriteToServer()`;
  - `pushScanLogToServer()`;
  - `pushPendingLocalChanges()`.

## Где теперь хранятся данные

### На телефоне

Приложение всё ещё хранит локальную копию каталога в `SharedPreferences`, чтобы работать без интернета.

Фото, выбранные из галереи, копируются сюда:

```text
/data/data/com.example.nfcmuseumguide/files/exhibit_photos/
```

### На сервере

PostgreSQL хранит таблицы:

```text
exhibits
scan_logs
favorites
```

Фото, отправленные на сервер, сохраняются в Docker volume:

```text
museum_uploads
```

и доступны через URL вида:

```text
http://localhost:8000/uploads/<filename>.jpg
```

## Как работает поток данных

### Добавление или редактирование экспоната

```text
EditorScreen
        ↓
MainActivity.addDraft()/updateDraft()
        ↓
MuseumRepository сохраняет локально
        ↓
MuseumApiClient.upsertExhibit()
        ↓
PUT /api/exhibits/{id}
        ↓
PostgreSQL
```

### Загрузка каталога

```text
FastAPI + PostgreSQL
        ↓
GET /api/exhibits
        ↓
MuseumApiClient.downloadExhibits()
        ↓
MuseumRepository.syncFromServer()
        ↓
локальный каталог Android
```

## Почему включён `usesCleartextTraffic=true`

Для учебного локального запуска сервер работает по обычному HTTP:

```xml
android:usesCleartextTraffic="true"
```

Это нужно, чтобы Android-приложение могло подключаться к `http://10.0.2.2:8000` или `http://192.168.x.x:8000`. Для настоящего релиза лучше включать HTTPS и убрать cleartext-трафик.


### Для запуска или перезапуска:
```bash
docker compose down -v
docker compose up --build
```
