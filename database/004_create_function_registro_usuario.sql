
CREATE OR REPLACE FUNCTION criar_usuario(p_perfil varchar)
RETURNS UUID
LANGUAGE plpgsql
AS $$
DECLARE
    v_token UUID;
BEGIN
    INSERT INTO usuario (perfil)
    VALUES (p_perfil )
    RETURNING token INTO v_token;

    RETURN v_token;

EXCEPTION
    WHEN foreign_key_violation THEN
        RAISE EXCEPTION 'Perfil "%" não existe.', p_perfil;
END;
$$;

-- select criar_usuario('pago_02');
-- select * from usuario;
-- select criar_usuario(2222);
