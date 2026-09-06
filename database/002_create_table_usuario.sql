create table if not exists usuario (
    id bigserial primary key,
    token uuid not null default gen_random_uuid () unique,
    registro timestamp not null default current_timestamp,
    ultima_solicitacao timestamp,
    perfil varchar references perfil (nome_perfil),
    ativo boolean not null default true
);

insert into usuario (token, registro, perfil)
    values ('550e8400-e29b-41d4-a716-446655440002', now(), 'admin')
on conflict (token)
    do nothing;

--select * from usuario;
--drop table usuario cascade;
