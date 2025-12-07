# API Contracts — Internal Music Service

Черновой план контрактов для внутренних эндпоинтов Music Service и публичных read-only маршрутов. Стиль — `camelCase`, JSON, UTC timestamps в ISO 8601. Все ответы возвращают тело без обёрток, кроме явных структур (например, `tracks`). Ошибки — единый формат.

## Общие правила
- `Content-Type: application/json`.
- Аутентификация: service-to-service Bearer токен или mTLS (фиксируется отдельно). Публичные маршруты — за Gateway.
- Идемпотентность: `Idempotency-Key` для `POST /internal/tracks` (рекомендовано).
- Корреляция: `X-Request-Id` (пробрасывается в логи/трейсы).
- Ошибки: `{"error":"code","message":"human readable","details":{...}}`.
  - `not_found`, `validation_failed`, `conflict`, `forbidden`, `internal_error`.
- Валидация: строки non-empty, `durationMs > 0`, `year` в [1900; now+1], длины строк ограничены (например, 255/1024).

## Базовые схемы
- `TrackSummary`: `id`, `title`, `artist`, `coverUrl?`, `durationMs`, `year?`, `explicit`.
- `TrackDetails`: `TrackSummary` + `audioStorageKey`, `createdAt`, `updatedAt`.
- `StreamUrlResponse`: `url`, `expiresAt`.
- `BatchRequest`: `ids: string[]` (1..500, уникальные).
- `BatchResponse`: `tracks: TrackSummary[]`, `notFound?: string[] | null`.
- `CreateTrackRequest`: `title`*, `artist`*, `durationMs`*, `year?`, `explicit` (default false), `fileLocation`*, `coverUrl?`.
- `UpdateTrackRequest`: все поля опциональны (`title`, `artist`, `durationMs`, `year`, `explicit`, `coverUrl`), но хотя бы одно должно быть передано.
- `CreateTrackResponse`: `id`, `createdAt?`.
- `UploadTrackRequest`: `fileName`*, `contentType`*.
- `UploadTrackResponse`: `storageKey`, `uploadUrl`, `expiresAt`.
- `TrackFacts`: `trackId`, `factsJson`, `generatedAt?`, `updatedAt?`.
- `TrackFactsRefreshResponse`: `trackId`, `status`, `requestedAt`.

## Внутренние эндпоинты (`/internal/...`)

### GET /internal/tracks/{trackId}
Возвращает метаданные одного трека.
- 200: `TrackDetails`.
- 404: если трек не найден.

Пример:
```json
{
  "id": "t1",
  "title": "Song",
  "artist": "Artist",
  "coverUrl": "https://cdn/cover.jpg",
  "durationMs": 210000,
  "year": 2023,
  "explicit": false,
  "audioStorageKey": "s3://bucket/audio/t1/original.mp3",
  "createdAt": "2024-01-01T12:00:00Z",
  "updatedAt": "2024-01-02T12:00:00Z"
}
```

### PATCH /internal/tracks/{trackId}
Частично обновляет мету трека.
- 200: `TrackDetails`.
- 400: пустое тело или неверные поля.
- 404: не найден.

### DELETE /internal/tracks/{trackId}
Удаляет трек (аудио в S3, записи в БД, кэш, события).
- 204: удалено.
- 404: не найден.

### POST /internal/tracks/batch
Возвращает мету по списку id.
- Request: `BatchRequest`.
- 200: `BatchResponse`.
- 400: пустой список, дубли, превышение лимита.

Пример:
```json
{
  "ids": ["t1", "t2"]
}
```
```json
{
  "tracks": [
    { "id": "t1", "title": "...", "artist": "...", "coverUrl": "...", "durationMs": 200000, "explicit": false },
    { "id": "t2", "title": "...", "artist": "...", "coverUrl": "...", "durationMs": 180000, "explicit": true }
  ],
  "notFound": ["t3"]
}
```

### GET /internal/tracks
Поиск/листинг для внутренних вызовов.
- Query params: `query?`, `limit` (default 30, max 100), `offset` (default 0).
- 200: `{ "tracks": TrackSummary[], "total"?: number }`.
- 400: неверные параметры.

### GET /internal/tracks/{trackId}/stream-url
Получить подписанный URL для стриминга/скачивания.
- 200: `StreamUrlResponse`.
- 404: трек не найден.
- 409: трек удалён или файл недоступен.

Пример:
```json
{ "url": "https://cdn...signed...", "expiresAt": "2024-01-01T12:10:00Z" }
```

### POST /internal/tracks/upload-url
Получить presigned URL для загрузки аудио в S3/MinIO.
- Request: `UploadTrackRequest`.
- 201: `UploadTrackResponse`.
- 400: валидация.

### POST /internal/tracks
Создание трека (для админок/создателей).
- Headers: `Idempotency-Key` (рекомендовано).
- Request: `CreateTrackRequest`.
- 201: `CreateTrackResponse`.
- 400: валидация.
- 409: конфликт (дубликат файла/idempotency hit с другой формой).

Пример:
```json
{
  "title": "Song",
  "artist": "Artist",
  "durationMs": 210000,
  "year": 2023,
  "fileLocation": "s3://bucket/audio/t1/original.mp3",
  "coverUrl": "https://cdn/covers/t1.jpg",
  "explicit": false
}
```
```json
{ "id": "t1", "createdAt": "2024-01-01T12:00:00Z" }
```

### GET /internal/tracks/{trackId}/facts
Получить сохранённые факты по треку.
- 200: `TrackFacts`.
- 404: нет трека или фактов.

### POST /internal/tracks/{trackId}/facts/refresh
Запросить перегенерацию фактов (отправка события).
- Query: `priority` (default 0).
- 200: `TrackFactsRefreshResponse`.
- 404: трек не найден.

## Публичные эндпоинты (`/tracks/...`)
- GET `/tracks/{trackId}` — карточка трека (`TrackDetails`), 404 если нет.
- GET `/tracks` — поиск/листинг (поля как в internal, `limit` default 20).
- GET `/tracks/{trackId}/stream-url` — `StreamUrlResponse`, 404/409 аналогично внутреннему.
- GET `/tracks/{trackId}/facts` — факты о треке, 404 если не найдены/нет трека.

## Ошибки (общие для internal/public)
```json
{ "error": "validation_failed", "message": "durationMs must be greater than 0", "details": { "field": "durationMs" } }
```
- 400: `validation_failed`
- 404: `not_found`
- 409: `conflict`
- 500: `internal_error`
