BEGIN;

CREATE TABLE IF NOT EXISTS users
(
    id                  SERIAL PRIMARY KEY,
    user_id             CHARACTER VARYING(255) NOT NULL,
    first_name          CHARACTER VARYING(50)  NOT NULL,
    last_name           CHARACTER VARYING(50)  NOT NULL,
    email               CHARACTER VARYING(100) NOT NULL,
    phone               CHARACTER VARYING(30)           DEFAULT NULL,
    bio                 CHARACTER VARYING(255)          DEFAULT NULL,
    reference_id        CHARACTER VARYING(255) NOT NULL,
    qr_code_secret      CHARACTER VARYING(255)          DEFAULT NULL,
    qr_code_image_uri   TEXT                            DEFAULT NULL,
    image_url           CHARACTER VARYING(255)          DEFAULT 'https://cdn-icons-png.flaticon.com/512/149/149071.png',
    last_login          TIMESTAMP(6) WITH TIME ZONE     DEFAULT CURRENT_TIMESTAMP,
    login_attempts      INTEGER                         DEFAULT 0,
    mfa                 BOOLEAN                NOT NULL DEFAULT FALSE,
    enabled             BOOLEAN                NOT NULL DEFAULT FALSE,
    account_non_expired BOOLEAN                NOT NULL DEFAULT FALSE,
    account_non_locked  BOOLEAN                NOT NULL DEFAULT FALSE,
    created_by          BIGINT                 NOT NULL,
    updated_by          BIGINT                 NOT NULL,
    created_at          TIMESTAMP(6) WITH TIME ZONE     DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP(6) WITH TIME ZONE     DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_users_email UNIQUE (email),
    CONSTRAINT uq_users_user_id UNIQUE (user_id),
    CONSTRAINT fk_users_created_by FOREIGN KEY (created_by) REFERENCES users (id) MATCH SIMPLE ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_users_updated_by FOREIGN KEY (updated_by) REFERENCES users (id) MATCH SIMPLE ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS confirmations
(
    id           SERIAL PRIMARY KEY,
    key          CHARACTER VARYING(255) NOT NULL,
    user_id      BIGINT                 NOT NULL,
    reference_id CHARACTER VARYING(255) NOT NULL,
    created_by   BIGINT                 NOT NULL,
    updated_by   BIGINT                 NOT NULL,
    created_at   TIMESTAMP(6) WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP(6) WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_confirmations_user_id UNIQUE (user_id),
    CONSTRAINT uq_confirmations_key UNIQUE (key),
    CONSTRAINT fk_confirmations_user_id FOREIGN KEY (user_id) REFERENCES users (id) MATCH SIMPLE ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_confirmations_created_by FOREIGN KEY (created_by) REFERENCES users (id) MATCH SIMPLE ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_confirmations_updated_by FOREIGN KEY (updated_by) REFERENCES users (id) MATCH SIMPLE ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS credentials
(
    id           SERIAL PRIMARY KEY,
    password     CHARACTER VARYING(255) NOT NULL,
    reference_id CHARACTER VARYING(255) NOT NULL,
    user_id      BIGINT                 NOT NULL,
    created_by   BIGINT                 NOT NULL,
    updated_by   BIGINT                 NOT NULL,
    created_at   TIMESTAMP(6) WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP(6) WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_credentials_user_id UNIQUE (user_id),
    CONSTRAINT fk_credentials_user_id FOREIGN KEY (user_id) REFERENCES users (id) MATCH SIMPLE ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_credentials_created_by FOREIGN KEY (created_by) REFERENCES users (id) MATCH SIMPLE ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_credentials_updated_by FOREIGN KEY (updated_by) REFERENCES users (id) MATCH SIMPLE ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS documents
(
    id             SERIAL PRIMARY KEY,
    document_id    CHARACTER VARYING(255) NOT NULL,
    reference_id   CHARACTER VARYING(255) NOT NULL,
    extension      CHARACTER VARYING(10)  NOT NULL,
    formatted_size CHARACTER VARYING(20)  NOT NULL,
    icon           CHARACTER VARYING(255) NOT NULL,
    name           CHARACTER VARYING(50)  NOT NULL,
    size           BIGINT                 NOT NULL,
    uri            CHARACTER VARYING(255) NOT NULL,
    description    CHARACTER VARYING(255),
    created_by     BIGINT                 NOT NULL,
    updated_by     BIGINT                 NOT NULL,
    created_at     TIMESTAMP(6) WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP(6) WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_documents_document_id UNIQUE (document_id),
    CONSTRAINT fk_documents_created_by FOREIGN KEY (created_by) REFERENCES users (id) MATCH SIMPLE ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_documents_updated_by FOREIGN KEY (updated_by) REFERENCES users (id) MATCH SIMPLE ON UPDATE CASCADE ON DELETE RESTRICT,
);

alter table if exists "documents"
       add constraint fk_documents_owner FOREIGN KEY (user_id) REFERENCES users (id) MATCH SIMPLE ON UPDATE CASCADE ON DELETE RESTRICT

CREATE TABLE IF NOT EXISTS roles
(
    id           SERIAL PRIMARY KEY,
    authorities  CHARACTER VARYING(255) NOT NULL,
    name         CHARACTER VARYING(255) NOT NULL,
    reference_id CHARACTER VARYING(255) NOT NULL,
    created_by   BIGINT                 NOT NULL,
    updated_by   BIGINT                 NOT NULL,
    created_at   TIMESTAMP(6) WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP(6) WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_roles_created_by FOREIGN KEY (created_by) REFERENCES users (id) MATCH SIMPLE ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_roles_updated_by FOREIGN KEY (updated_by) REFERENCES users (id) MATCH SIMPLE ON UPDATE CASCADE ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS user_roles
(
    id      SERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    CONSTRAINT fk_user_roles_user_id FOREIGN KEY (user_id) REFERENCES users (id) MATCH SIMPLE ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_user_roles_role_id FOREIGN KEY (role_id) REFERENCES roles (id) MATCH SIMPLE ON UPDATE CASCADE ON DELETE RESTRICT
);

CREATE INDEX IF NOT EXISTS index_users_email ON users (email);

CREATE INDEX IF NOT EXISTS index_users_user_id ON users (user_id);

CREATE INDEX IF NOT EXISTS index_confirmations_user_id ON confirmations (user_id);

CREATE INDEX IF NOT EXISTS index_credentials_user_id ON credentials (user_id);

CREATE INDEX IF NOT EXISTS index_user_roles_user_id ON user_roles (user_id);

END;