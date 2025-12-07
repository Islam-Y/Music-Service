create table if not exists track_facts
(
    track_id    varchar(64) primary key,
    facts_json  text,
    generated_at timestamp with time zone,
    updated_at   timestamp with time zone
);

alter table track_facts
    add constraint fk_track_facts_track
        foreign key (track_id) references tracks (id);
