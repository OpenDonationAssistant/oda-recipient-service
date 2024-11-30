create table gateway_config (
  id varchar(255) not null,
  recipient_id varchar(255) not null,
  gateway varchar(255) not null,
	config jsonb not null
);
