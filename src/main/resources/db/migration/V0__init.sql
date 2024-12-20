CREATE TABLE public.socks
(
    id          BIGSERIAL PRIMARY KEY,
    color       VARCHAR(255)     NOT NULL,
    cotton_part DOUBLE PRECISION NOT NULL,
    quantity    INT
);