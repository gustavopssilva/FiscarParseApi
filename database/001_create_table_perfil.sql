create table if not exists perfil (
    id bigserial primary key,
    nome_perfil varchar unique,
    intervalo integer not null default 10000
);

insert into perfil (nome_perfil, intervalo)
    values ('teste', 1000),
    ('admin', 1000),
    ('gratuito', 600000),
    ('pago_01', 10000),
    ('pago_02', 30000),
    ('pago_03', 60000);

--    select * from perfil;
--drop table perfil cascade;
