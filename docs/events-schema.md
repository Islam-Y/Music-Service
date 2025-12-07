# Kafka Event Schemas — Music Service

## Topic: music.track.domain
Domain events for track lifecycle. Key = `trackId`.

Payload (version 1):
```json
{
  "version": "1",
  "eventType": "created | updated | deleted",
  "trackId": "string",
  "title": "string",
  "artist": "string",
  "durationMs": 210000,
  "year": 2023,
  "explicit": false,
  "audioStorageKey": "s3://bucket/audio/t1/original.mp3",
  "timestamp": "2024-01-01T12:00:00Z",
  "changedFields": "all | comma,separated | null"
}
```
- `eventType`: enum, required.
- `version`: required, string; breaking changes → new version/topic.
- `trackId`: required, also used as key.
- `title`, `artist`, `durationMs`, `year`, `explicit`, `audioStorageKey`: required (per current spec).
- `timestamp`: required, ISO-8601 UTC.
- `changedFields`: for `updated` — comma-separated list or `all`; null for created/deleted.
- Optional fields are excluded to keep schema minimal (album/popularity omitted per spec request).

Notes:
- Producers must send after DB commit (transactional listener). Current impl uses transactional event listener.
- Consumers should ignore unknown fields for forward compatibility and rely on `version`.
- Partitioning by `trackId` keeps track events ordered.

## Topic: music.track.facts.refresh
Trigger Facts Service to regenerate facts.
Key = `trackId`.
Payload (version 1):
```json
{
  "version": "1",
  "trackId": "string",
  "priority": 0,
  "timestamp": "2024-01-01T12:00:00Z"
}
```

## Topic: music.track.facts.generated
Facts Service → Music Service with generated facts.
Key = `trackId`.
Payload (version 1):
```json
{
  "version": "1",
  "trackId": "string",
  "factsJson": "{...}",
  "generatedAt": "2024-01-01T12:00:00Z"
}
```

## Topic: music.facts.events
Outbox-driven facts events (for Facts Service). Key = `trackId`.
Payload (version 1):
```json
{
  "version": "1",
  "eventType": "created | updated | deleted | refresh",
  "trackId": "string",
  "priority": 0,
  "timestamp": "2024-01-01T12:00:00Z"
}
```
DLT: `<topic>.dlq` if consumer fails after retries.

## Topic: music.track.created / updated / deleted (legacy)
Kept for backward compatibility; payload mirrors `music.track.domain` but without `version` and `changedFields`. Prefer using `music.track.domain`.

## Headers (recommended)
- `eventId`: UUID for de-duplication.
- `traceId` / `spanId`: for tracing.
- `correlationId`: if correlating with client request.

## Error handling
- Consumers should implement retry + DLQ (manual ack or DeadLetterPublishingRecoverer).
- Producers should use idempotent/transactional Kafka configs if needed. Current service publishes after commit but not transactionally to Kafka. 
