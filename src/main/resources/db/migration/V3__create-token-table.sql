create table token(
  id varchar(255) not null,
  recipient_id varchar(255) not null,
  token varchar(255) not null,
  type varchar(255),
  system varchar(255) not null
);
