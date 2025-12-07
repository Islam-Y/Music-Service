# Music-Service

Сервис треков: хранение метаданных, выдача ссылок на стрим/загрузку через S3, кэш в Redis, идемпотентное создание, публикация доменных событий в Kafka и обработка фактов о треках.

## Local development
Run supporting services:
```
docker-compose up -d
```

Configure env or `application-local.yaml` (not committed) for secrets:
```
export S3_BUCKET=your-bucket
export S3_ENDPOINT=http://localhost:9000
export S3_ACCESS_KEY=...
export S3_SECRET_KEY=...
```

Run the app:
```
./gradlew bootRun
```

## Profiles
- Default: uses `application.yaml`
- Prod: `--spring.profiles.active=prod` reads `application-prod.yaml` (env overrides supported)
