create or replace
procedure alternar_status (tokenSolicitante uuid,
tokenUsuario uuid)
language plpgsql
as $$
declare
    v_solicitante usuario;
    v_usuario usuario;
    v_status boolean;

begin
-- 1 VALIDA USUARIO SOLICITANTE:

select 	* into 	v_solicitante
from 	usuario u
where 	u.token = tokenSolicitante;

if not found then
            raise exception 'Solicitante não encontrado';
end if;

if v_solicitante.perfil != 'admin' then
            raise exception 'Solicitação não permitida';
end if;

-- 2 VALIDA USUARIO:
    select 	*  into v_usuario
from 	usuario u
where
	u.token = tokenUsuario;

    if not found then
            raise exception ' Usuário afetado não encontrado';
    end if;

    v_status := not v_usuario.ativo;

update 	usuario set ativo = v_status
where 	token = tokenUsuario;
end;
$$;

--call alternar_status('550e8400-e29b-41d4-a716-446655440002', 'f8b9e295-bbd8-4cfb-8795-9dc70d1b32f5');
