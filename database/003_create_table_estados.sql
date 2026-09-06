
create table if not exists estado_nfe (
    sigla varchar unique,
    registro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ativo BOOLEAN NOT NULL DEFAULT TRUE

    );



insert into estado_nfe (sigla)
    values ('BA'),
    ('PE'),
    ('PR')
on conflict (sigla)
    do nothing;
-- drop table estado_nfe;
