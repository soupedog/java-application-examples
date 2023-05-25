create schema local_test;

create table local_test."user"
(
    uid            bigint generated by default as identity
        constraint user_pk
            primary key,
    sequence       bigint,
    "NAME"         varchar,
    user_sex       varchar,
    balance        numeric(12, 2),
    user_state     varchar,
    configuration  varchar,
    create_ts      timestamp,
    last_update_ts timestamp
);

-- 手动获取序列用
create sequence local_test.user_sequence;
