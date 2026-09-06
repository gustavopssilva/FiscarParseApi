CREATE TABLE IF NOT EXISTS usuario (
    id bigserial PRIMARY KEY,
    token uuid NOT NULL DEFAULT gen_random_uuid () UNIQUE,
    registro timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ultima_solicitacao timestamp,
    perfil varchar REFERENCES perfil (nome_perfil),
    ativo boolean NOT NULL DEFAULT TRUE
);

INSERT INTO usuario (token, registro, perfil)
    VALUES ('550e8400-e29b-41d4-a716-446655440002', now(), 'admin')
ON CONFLICT (token)
    DO NOTHING;

--select * from usuario;
--drop table usuario cascade;
