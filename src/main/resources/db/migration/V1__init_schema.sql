create table courses (
    id binary(16) primary key,
    title varchar(120) not null
);

create table offerings (
    id binary(16) primary key,
    course_id binary(16) not null references courses(id),
    teacher_id binary(16) not null,
    name varchar(120) not null,
    timezone varchar(80) not null
);

create table sessions (
    id binary(16) primary key,
    offering_id binary(16) not null references offerings(id) on delete cascade,
    start_at_utc datetime(6) not null,
    end_at_utc datetime(6) not null
);

create index idx_sessions_offering on sessions(offering_id);
create index idx_sessions_start_end on sessions(start_at_utc, end_at_utc);

create table parents (
    id binary(16) primary key,
    external_id binary(16) not null unique
);

create table bookings (
    id binary(16) primary key,
    parent_id binary(16) not null references parents(id),
    offering_id binary(16) not null references offerings(id),
    booked_at_utc datetime(6) not null,
    constraint uk_parent_offering unique (parent_id, offering_id)
);

create index idx_bookings_parent on bookings(parent_id);
