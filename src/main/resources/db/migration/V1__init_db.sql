CREATE TABLE member (
    id                  BIGSERIAL PRIMARY KEY,
    login               VARCHAR,
    chat_id             VARCHAR,
    review_group        INTEGER NOT NULL,
    can_review_design   BOOLEAN NOT NULL,
    is_omni             BOOLEAN NOT NULL
);

CREATE TABLE task (
    id                  BIGSERIAL PRIMARY KEY,
    uuid                VARCHAR(36) NOT NULL UNIQUE,
    name                VARCHAR NOT NULL,
    link                VARCHAR NOT NULL,
    task_type           VARCHAR NOT NULL,
    status              VARCHAR,
    creation_time       TIMESTAMP,
    close_time          TIMESTAMP,
    last_review_time    TIMESTAMP,
    author_id           INTEGER,

    CONSTRAINT fk_author FOREIGN KEY (author_id) REFERENCES member(id)
);

CREATE TABLE review (
    id              BIGSERIAL PRIMARY KEY,
    review_stage    INTEGER NOT NULL,
    task_id         INTEGER,

    CONSTRAINT fk_task FOREIGN KEY (task_id) REFERENCES task(id)
);

CREATE TABLE member_review (
    id BIGSERIAL PRIMARY KEY,
    review_id INTEGER,
    reviewer_id INTEGER,
    start_time TIMESTAMP,
    end_time TIMESTAMP,

    CONSTRAINT fk_review FOREIGN KEY (review_id) REFERENCES review(id),
    CONSTRAINT fk_reviewer FOREIGN KEY (reviewer_id) REFERENCES member(id)
);