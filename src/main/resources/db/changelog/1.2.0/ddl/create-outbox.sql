create table if not exists outbox_messages
(
    id             uuid primary key,
    aggregate_type varchar(64)  not null,
    aggregate_id   varchar(128) not null,
    event_type     varchar(128) not null,
    payload        jsonb        not null,
    status         varchar(32)  not null,
    attempts       int          not null default 0,
    available_at   timestamp with time zone not null default now(),
    created_at     timestamp with time zone not null default now(),
    updated_at     timestamp with time zone not null default now(),
    trace_id       varchar(128)
);

create index if not exists idx_outbox_status_available on outbox_messages (status, available_at);
