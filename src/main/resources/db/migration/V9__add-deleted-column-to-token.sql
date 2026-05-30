alter table token add deleted boolean;
update token set deleted = FALSE;
