CREATE TABLE rooms (
    id BIGSERIAL PRIMARY KEY not null ,
    name varchar(30) not null ,
    type varchar(30) not null ,
    capacity BIGINT not null ,
    status varchar(30) not null
)