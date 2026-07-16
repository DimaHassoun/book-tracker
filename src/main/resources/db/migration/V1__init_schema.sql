CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TYPE book_format AS ENUM (
  'PHYSICAL',
  'EBOOK',
  'AUDIOBOOK'
);

CREATE TYPE reading_status AS ENUM (
  'READING',
  'PAUSED',
  'DNF',
  'READ'
);

CREATE TYPE question_type AS ENUM (
  'SCALE_NUMERIC',
  'SINGLE_CHOICE'
);

CREATE TYPE external_source AS ENUM (
  'GOOGLE_BOOKS',
  'OPEN_LIBRARY'
);

CREATE TYPE feed_event_type AS ENUM (
  'BOOK_ADDED_TO_LIST',
  'READING_STARTED',
  'READING_FINISHED',
  'READING_PAUSED',
  'READING_DNF',
  'LIST_CREATED'
);

CREATE TYPE feed_visibility AS ENUM (
  'PRIVATE',
  'FOLLOWERS',
  'PUBLIC'
);

CREATE TABLE users (
  id UUID PRIMARY KEY DEFAULT (gen_random_uuid()),
  username VARCHAR(50) UNIQUE NOT NULL,
  email VARCHAR(255) UNIQUE NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  bio TEXT,
  avatar_url VARCHAR(500),
  is_private BOOLEAN NOT NULL DEFAULT true,
  timezone VARCHAR(50) DEFAULT 'UTC',
  created_at TIMESTAMPTZ NOT NULL DEFAULT (NOW()),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT (NOW())
);

CREATE TABLE books (
  id UUID PRIMARY KEY DEFAULT (gen_random_uuid()),
  isbn_13 VARCHAR(13),
  isbn_10 VARCHAR(10),
  title VARCHAR(500) NOT NULL,
  subtitle VARCHAR(500),
  authors TEXT[],
  publisher VARCHAR(255),
  published_date DATE,
  description TEXT,
  page_count INTEGER,
  genres TEXT[],
  cover_image_url VARCHAR(500),
  book_external_source external_source NOT NULL,
  external_id VARCHAR(50) NOT NULL,
  language VARCHAR(10) DEFAULT 'en',
  created_at TIMESTAMPTZ NOT NULL DEFAULT (NOW())
);

CREATE TABLE user_books (
  id UUID PRIMARY KEY DEFAULT (gen_random_uuid()),
  user_id UUID NOT NULL,
  book_id UUID NOT NULL,
  owned_format book_format,
  created_at TIMESTAMPTZ NOT NULL DEFAULT (NOW()),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT (NOW())
);

CREATE TABLE reading_instances (
  id UUID PRIMARY KEY DEFAULT (gen_random_uuid()),
  user_book_id UUID NOT NULL,
  read_number INTEGER NOT NULL DEFAULT 1,
  status reading_status NOT NULL,
  current_page INTEGER CHECK (current_page >= 0),
  start_date DATE,
  end_date DATE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT (NOW()),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT (NOW())
);

CREATE TABLE status_change_log (
  id UUID PRIMARY KEY DEFAULT (gen_random_uuid()),
  reading_instance_id UUID NOT NULL,
  old_status reading_status,
  new_status reading_status NOT NULL,
  page_at_change INTEGER CHECK (page_at_change >= 0),
  changed_at TIMESTAMPTZ NOT NULL DEFAULT (NOW())
);

CREATE TABLE response_questions (
  id UUID PRIMARY KEY DEFAULT (gen_random_uuid()),
  key VARCHAR(50) UNIQUE NOT NULL,
  label VARCHAR(255) NOT NULL,
  question_type question_type NOT NULL,
  is_active BOOLEAN NOT NULL DEFAULT true,
  sort_order INTEGER NOT NULL DEFAULT 0,
  created_at TIMESTAMPTZ NOT NULL DEFAULT (NOW())
);

CREATE TABLE response_options (
  id UUID PRIMARY KEY DEFAULT (gen_random_uuid()),
  question_id UUID NOT NULL,
  value VARCHAR(100) NOT NULL,
  label VARCHAR(255) NOT NULL,
  sort_order INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE reading_instance_responses (
  id UUID PRIMARY KEY DEFAULT (gen_random_uuid()),
  reading_instance_id UUID NOT NULL,
  question_id UUID NOT NULL,
  numeric_value DECIMAL(3,1),
  option_id UUID,
  answered_at TIMESTAMPTZ NOT NULL DEFAULT (NOW()),
  CHECK ((numeric_value IS NOT NULL AND option_id IS NULL)
      OR
     (numeric_value IS NULL AND option_id IS NOT NULL))
);

CREATE TABLE reading_lists (
  id UUID PRIMARY KEY DEFAULT (gen_random_uuid()),
  user_id UUID NOT NULL,
  name VARCHAR(255) NOT NULL,
  description TEXT,
  is_public BOOLEAN NOT NULL DEFAULT false,
  created_at TIMESTAMPTZ NOT NULL DEFAULT (NOW()),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT (NOW())
);

CREATE TABLE reading_list_books (
  reading_list_id UUID NOT NULL,
  book_id UUID NOT NULL,
  sort_order INTEGER NOT NULL DEFAULT 0,
  added_at TIMESTAMPTZ NOT NULL DEFAULT (NOW()),
  PRIMARY KEY (reading_list_id, book_id)
);

CREATE TABLE series (
  id UUID PRIMARY KEY DEFAULT (gen_random_uuid()),
  name VARCHAR(500) NOT NULL,
  description TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT (NOW()),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT (NOW())
);

CREATE TABLE series_books (
  series_id UUID NOT NULL,
  book_id UUID NOT NULL,
  volume_number DECIMAL(5,1) NOT NULL,
  PRIMARY KEY (series_id, book_id)
);

CREATE TABLE followers (
  follower_id UUID NOT NULL,
  followee_id UUID NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT (NOW()),
  CHECK (follower_id != followee_id),
  PRIMARY KEY (follower_id, followee_id)
);

CREATE TABLE feed_events (
  id UUID PRIMARY KEY DEFAULT (gen_random_uuid()),
  actor_user_id UUID NOT NULL,
  event_type feed_event_type NOT NULL,
  reading_list_id UUID,
  book_id UUID,
  reading_instance_id UUID,
  text TEXT,
  visibility feed_visibility NOT NULL DEFAULT 'PRIVATE',
  created_at TIMESTAMPTZ NOT NULL DEFAULT (NOW())
);

CREATE TABLE reading_sessions (
  id UUID PRIMARY KEY DEFAULT (gen_random_uuid()),
  reading_instance_id UUID NOT NULL,
  session_date DATE NOT NULL,
  start_page INTEGER NOT NULL CHECK (start_page >= 0),
  end_page INTEGER NOT NULL CHECK (end_page > start_page),
  duration_minutes INTEGER CHECK (duration_minutes > 0),
  created_at TIMESTAMPTZ NOT NULL DEFAULT (NOW())
);

CREATE TABLE book_notes (
  id UUID PRIMARY KEY DEFAULT (gen_random_uuid()),
  reading_instance_id UUID NOT NULL,
  reading_session_id UUID,
  page_number INTEGER CHECK (page_number >= 0),
  content TEXT NOT NULL,
  note_date DATE NOT NULL DEFAULT (CURRENT_DATE),
  created_at TIMESTAMPTZ NOT NULL DEFAULT (NOW()),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT (NOW())
);

CREATE INDEX idx_reading_list_books_book_id ON reading_list_books (book_id); 

CREATE UNIQUE INDEX ON books (book_external_source, external_id);

CREATE UNIQUE INDEX ON user_books (user_id, book_id);

CREATE INDEX idx_user_books_user_id ON user_books (user_id);

CREATE UNIQUE INDEX ON reading_instances (user_book_id, read_number);

CREATE INDEX idx_instances_user_book_id ON reading_instances (user_book_id);

CREATE INDEX idx_instances_status ON reading_instances (status);

CREATE INDEX idx_status_log_instance ON status_change_log (reading_instance_id, changed_at);

CREATE UNIQUE INDEX ON response_options (question_id, value);

CREATE UNIQUE INDEX ON reading_instance_responses (reading_instance_id, question_id);

CREATE INDEX idx_responses_instance ON reading_instance_responses (reading_instance_id);

CREATE INDEX idx_responses_question ON reading_instance_responses (question_id);

CREATE INDEX idx_followers_followee ON followers (followee_id);

CREATE INDEX idx_followers_follower ON followers (follower_id);

CREATE INDEX idx_feed_visibility_time ON feed_events (visibility, created_at);

CREATE INDEX idx_feed_actor_time ON feed_events (actor_user_id, created_at);

CREATE INDEX idx_sessions_reading_instance ON reading_sessions (reading_instance_id);

CREATE INDEX idx_notes_reading_instance ON book_notes (reading_instance_id);

ALTER TABLE user_books ADD FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE user_books ADD FOREIGN KEY (book_id) REFERENCES books (id) ON DELETE RESTRICT DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE reading_instances ADD FOREIGN KEY (user_book_id) REFERENCES user_books (id) ON DELETE CASCADE DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE response_options ADD FOREIGN KEY (question_id) REFERENCES response_questions (id) ON DELETE CASCADE DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE reading_instance_responses ADD FOREIGN KEY (reading_instance_id) REFERENCES reading_instances (id) ON DELETE CASCADE DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE reading_instance_responses ADD FOREIGN KEY (question_id) REFERENCES response_questions (id) ON DELETE RESTRICT DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE reading_instance_responses ADD FOREIGN KEY (option_id) REFERENCES response_options (id) ON DELETE RESTRICT DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE reading_lists ADD FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE reading_list_books ADD FOREIGN KEY (reading_list_id) REFERENCES reading_lists (id) ON DELETE CASCADE DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE reading_list_books ADD FOREIGN KEY (book_id) REFERENCES books (id) ON DELETE CASCADE DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE series_books ADD FOREIGN KEY (series_id) REFERENCES series (id) ON DELETE CASCADE DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE series_books ADD FOREIGN KEY (book_id) REFERENCES books (id) ON DELETE CASCADE DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE followers ADD FOREIGN KEY (follower_id) REFERENCES users (id) ON DELETE CASCADE DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE followers ADD FOREIGN KEY (followee_id) REFERENCES users (id) ON DELETE CASCADE DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE feed_events ADD FOREIGN KEY (actor_user_id) REFERENCES users (id) ON DELETE CASCADE DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE feed_events ADD FOREIGN KEY (reading_list_id) REFERENCES reading_lists (id) ON DELETE SET NULL DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE feed_events ADD FOREIGN KEY (book_id) REFERENCES books (id) ON DELETE SET NULL DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE feed_events ADD FOREIGN KEY (reading_instance_id) REFERENCES reading_instances (id) ON DELETE SET NULL DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE reading_sessions ADD FOREIGN KEY (reading_instance_id) REFERENCES reading_instances (id) ON DELETE CASCADE DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE book_notes ADD FOREIGN KEY (reading_instance_id) REFERENCES reading_instances (id) ON DELETE CASCADE DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE status_change_log ADD FOREIGN KEY (reading_instance_id) REFERENCES reading_instances (id) ON DELETE CASCADE DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE book_notes ADD FOREIGN KEY (reading_session_id) REFERENCES reading_sessions (id) ON DELETE SET NULL DEFERRABLE INITIALLY IMMEDIATE;
