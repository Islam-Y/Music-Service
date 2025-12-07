create table if not exists tracks
(
    id                varchar(64) primary key,
    title             varchar(255) not null,
    artist            varchar(255) not null,
    album             varchar(255),
    cover_url         varchar(1024),
    duration_ms       bigint       not null,
    year              int,
    audio_storage_key varchar(1024) not null,
    explicit          boolean      not null default false,
    created_at        timestamp with time zone not null,
    updated_at        timestamp with time zone not null
);

create index if not exists idx_tracks_artist on tracks (artist);
create index if not exists idx_tracks_title on tracks (title);
