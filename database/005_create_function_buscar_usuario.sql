create or replace function busca_usuarios (p_ativo boolean default null)
    returns setof usuario
    language plpgsql
    as $$
begin
    return QUERY
    select
        *
    from
        usuario
    where
        p_ativo is null
        or ativo = p_ativo;
end;
$$;


-- Todos
-- SELECT * FROM busca_usuarios();

-- Todos
-- SELECT * FROM busca_usuarios(NULL);

-- Somente ativos
-- SELECT * FROM busca_usuarios(true);

-- Somente inativos
-- SELECT * FROM busca_usuarios(false);
