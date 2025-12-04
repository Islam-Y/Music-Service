# API Contracts — Internal Music Service

Черновой план контрактов для внутренних эндпоинтов Music Service. Стиль — `camelCase`, JSON, UTC timestamps в ISO 8601. Все ответы возвращают тело без обёрток, кроме явных структур (например, `tracks`). Ошибки — единый формат.

## Общие правила
- `Content-Type: application/json`.
- Аутентификация: service-to-service Bearer токен или mTLS (фиксируется отдельно).
- Идемпотентность загрузки: `Idempotency-Key` для `POST /internal/tracks`.
- Корреляция: `X-Request-Id` (пробрасывается в логи/трейсы).
- Ошибки: `{"error":"code","message":"human readable","details":{...}}`.
  - `not_found`, `validation_failed`, `conflict`, `forbidden`, `internal_error`.
- Валидация: строки non-empty, `durationMs > 0`, `year` в [1900; now+1], длины строк ограничены (например, 255).

## Базовые схемы
- `TrackSummary`: `id`, `title`, `artist`, `album?`, `coverUrl?`, `durationMs`, `year?`, `explicit`, `popularity?`.
- `TrackDetails`: `TrackSummary` + `audioStorageKey`, `createdAt`, `updatedAt`.
- `StreamUrlResponse`: `url`, `expiresAt`.
- `BatchRequest`: `ids: string[]` (минимум 1, максимум 500, уникальные).
- `BatchResponse`: `tracks: TrackSummary[]`, `notFound?: string[]`.
- `CreateTrackRequest`: `title`*, `artist`*, `album?`, `durationMs`*, `year?`, `explicit` (default false), `fileLocation`*, `coverUrl?`, `popularity?`.
- `CreateTrackResponse`: `id`, `createdAt?`.

## Эндпоинты

### GET /internal/tracks/{trackId}
Возвращает метаданные одного трека.
- 200: `TrackDetails`.
- 404: если трек не найден или удалён.

Пример:
```json
{
  "id": "t1",
  "title": "Song",
  "artist": "Artist",
  "album": "Album",
  "coverUrl": "https://cdn/cover.jpg",
  "durationMs": 210000,
  "year": 2023,
  "explicit": false,
  "popularity": 82,
  "audioStorageKey": "s3://bucket/audio/t1/original.mp3",
  "createdAt": "2024-01-01T12:00:00Z",
  "updatedAt": "2024-01-02T12:00:00Z"
}
```

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
- Query params: `query?`, `limit` (default 20, max 100), `offset` (default 0).
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
  "album": "Album",
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

## Дополнительно для OpenAPI
- Версия API: `/internal` без версии в пути, версия в заголовке `X-API-Version` (опционально).
- Коды ответов: 200/201/400/404/409/500 документировать для каждого маршрута.
- Пример error schema:
```json
{
  "error": "validation_failed",
  "message": "durationMs must be greater than 0",
  "details": { "field": "durationMs" }
}
```
