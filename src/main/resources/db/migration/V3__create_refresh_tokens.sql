CREATE TABLE refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token VARCHAR(255) NOT NULL UNIQUE,
    expiry_date TIMESTAMP with time zone not null,

    CONSTRAINT fk_refresh_tokens_user
                            foreign key (user_id)
                            references users(id)
                            on delete cascade
);