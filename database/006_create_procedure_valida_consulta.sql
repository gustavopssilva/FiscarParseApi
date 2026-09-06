create or replace procedure validador_consulta (p_token uuid, p_estado varchar)
language plpgsql
as $$
declare
    v_usuario usuario;
    v_estado estado_nfe;
    v_perfil perfil;
begin

    -- 1 Recupera Estado da requisição:
    select
        * into v_estado
    from
        estado_nfe e
    where
        e.sigla = p_estado;
    if not found then
        raise exception 'Estado % não localizado', p_estado;
    end if;

    --RAISE NOTICE 'CHEGOU NO IF - estado=% ativo=%', v_estado.sigla , v_estado.ativo;
    -- Verifica se o estado esta ativo
    if v_estado.ativo = false then
        raise exception 'Consulta para o estado % esta desabilitada.', p_estado;
    end if;

    --2. busca usuario pelo token
    select
        * into v_usuario
    from
        usuario u
    where
        u.token = p_token;
    if not found then
        raise exception 'Usuário não encontrado para o token informado.';
    end if;

    --3. Valida usuario ativo
    if v_usuario.ativo = false then
        raise exception 'Usuário não esta ativo.';
    end if;

    -- Valida o intervalo da ultima solicitação:
    if v_usuario.ultima_solicitacao is not null then
        select * into v_perfil
        from perfil p
        where
            p.nome_perfil = v_usuario.perfil;

        if v_usuario.ultima_solicitacao + (v_perfil.intervalo * interval '1 millisecond') > NOW() then
            raise exception 'Aguarde o intervalo minimo do seu perfil entre as solicitações';
        else
            update
                usuario
            set
                ultima_solicitacao = now()
            where
                token = v_usuario.token;
        end if;
    end if;
end;
$$;

--call validador_consulta('f65821cc-8b49-430e-be88-2b96b0f3a8bf' ,'PR');
