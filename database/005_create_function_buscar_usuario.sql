drop function if exists busca_usuarios (boolean);

create or replace function busca_usuarios (p_ativo boolean default null)
    returns table (
        id bigint,
        token uuid,
        registro timestamp,
        ultima_solicitacao timestamp,
        perfil varchar,
        intervalo integer,
        ativo boolean
    )
    language sql
    stable
as $$
    select
        u.id,
        u.token,
        u.registro,
        u.ultima_solicitacao,
        u.perfil,
        p.intervalo,
        u.ativo
    from
        usuario u
        left join perfil p on p.nome_perfil = u.perfil
    where
        p_ativo is null
        or u.ativo = p_ativo;
$$;


-- Todos
-- SELECT * FROM busca_usuarios();

-- Todos
-- SELECT * FROM busca_usuarios(NULL);

-- Somente ativos
-- SELECT * FROM busca_usuarios(true);

-- Somente inativos
-- SELECT * FROM busca_usuarios(false);
