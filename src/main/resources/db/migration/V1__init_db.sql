CREATE TABLE member (
    id                  BIGSERIAL PRIMARY KEY,
    login               VARCHAR,
    chat_id             VARCHAR,
    review_group        INTEGER NOT NULL,
    can_review_design   BOOLEAN NOT NULL,
    is_omni             BOOLEAN NOT NULL
);

CREATE TABLE task (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR NOT NULL,
    link            VARCHAR NOT NULL,
    task_type       VARCHAR NOT NULL,
    status          VARCHAR NOT NULL,
    creation_time   TIMESTAMP,
    close_time      TIMESTAMP,
    author_id       INTEGER,

    CONSTRAINT fk_author FOREIGN KEY (author_id) REFERENCES member(id)
);

CREATE TABLE review (
    id              BIGSERIAL PRIMARY KEY,
    review_stage    INTEGER NOT NULL,
    task_id         INTEGER,
    reviewer_id     INTEGER,

    CONSTRAINT fk_task FOREIGN KEY (task_id) REFERENCES task(id),
    CONSTRAINT fk_reviewer FOREIGN KEY (reviewer_id) REFERENCES member(id)
);