-- Ajustes de esquema que o ddl-auto=update não faz sozinho. Roda antes do Hibernate, a cada subida,
-- e é idempotente: só age se ainda encontrar a estrutura antiga.

-- F-07: jogando_agora.data_inicio (DATE) virou iniciado_em (TIMESTAMPTZ), guardando o instante exato.
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns
               WHERE table_schema = current_schema() AND table_name = 'jogando_agora'
                 AND column_name = 'data_inicio') THEN
        IF EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_schema = current_schema() AND table_name = 'jogando_agora'
                     AND column_name = 'iniciado_em') THEN
            UPDATE jogando_agora SET iniciado_em = data_inicio::timestamp AT TIME ZONE 'UTC'
                WHERE iniciado_em IS NULL;
            ALTER TABLE jogando_agora DROP COLUMN data_inicio;
        ELSE
            ALTER TABLE jogando_agora RENAME COLUMN data_inicio TO iniciado_em;
            ALTER TABLE jogando_agora ALTER COLUMN iniciado_em TYPE timestamp(6) with time zone
                USING iniciado_em::timestamp AT TIME ZONE 'UTC';
        END IF;
    END IF;
END
$$
@@@
