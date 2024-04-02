

CREATE KEYSPACE IF NOT EXISTS calendar_app
  WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1};

USE calendar_app;

CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY,
    first_name TEXT,
    last_name TEXT,
    email_address TEXT
);

CREATE TABLE IF NOT EXISTS notifications (
id UUID PRIMARY KEY,
user_id UUID,
name TEXT,
description TEXT,
notification_date TIMESTAMP,
is_delivered BOOLEAN
);


insert into users(id,first_name,last_name,email_address)
  values(uuid(),'John','Doe','john.doe@gmail.com');
insert into users(id,first_name,last_name,email_address)
  values(uuid(),'Jane','Doe','jane.doe@gmail.com');