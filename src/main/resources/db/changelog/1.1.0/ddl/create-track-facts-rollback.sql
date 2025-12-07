alter table track_facts drop constraint if exists fk_track_facts_track;
drop table if exists track_facts cascade;
