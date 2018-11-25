CREATE EXTENSION IF NOT EXISTS citext;



CREATE TABLE IF NOT EXISTS "users" (
  id SERIAL NOT NULL PRIMARY KEY ,
  username CITEXT NOT NULL UNIQUE ,
  email CITEXT NOT NULL UNIQUE ,
  password TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS  "notes" (
  id SERIAL NOT NULL  PRIMARY KEY,
  author CITEXT NOT NULL,
  FOREIGN KEY (author) REFERENCES "users"(username),
  title CITEXT NOT NULL,
  body CITEXT NOT NULL
);

