create or replace procedure alterar_perfil (tokenSolicitante uuid,
tokenUsuario uuid, p_perfil VARCHAR)

language plpgsql
as $$
declare
    v_solicitante usuario;
    v_usuario usuario;
	  v_perfil perfil;

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

-- 2 Validar o perfil
select *  into v_perfil from perfil
  where nome_perfil= p_perfil;


  if not found then
    raise EXCEPTION 'Perfil % não foi localizado', p_perfil;
  end if;



-- 3 VALIDA USUARIO:
select 	*  into v_usuario
  from 	usuario u
    where u.token = tokenUsuario;

    if not found then
       raise exception ' Usuário afetado não encontrado';
    end if;



-- 4 Atualizando o usuario
update 	usuario set perfil = p_perfil
where 	token = tokenUsuario;

end;
$$;

-- call alterar_perfil('550e8400-e29b-41d4-a716-446655440002', 'f8b9e295-bbd8-4cfb-8795-9dc70d1b32f5','teste');









