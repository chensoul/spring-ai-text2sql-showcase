SELECT
    t.table_name,
    pg_catalog.obj_description(c.oid, 'pg_class') AS table_comment,
    string_agg(
        format(
            '  %I %s%s%s',
            col.column_name,
            CASE
                WHEN col.character_maximum_length IS NOT NULL
                THEN col.data_type || '(' || col.character_maximum_length || ')'
                WHEN col.numeric_precision IS NOT NULL AND col.data_type = 'numeric'
                THEN col.data_type || '(' || col.numeric_precision || ',' || col.numeric_scale || ')'
                ELSE col.data_type
            END,
            CASE WHEN col.is_nullable = 'NO' THEN ' NOT NULL' ELSE '' END,
            CASE WHEN col.column_default IS NOT NULL THEN ' DEFAULT ' || col.column_default ELSE '' END
        ),
        ',' || chr(10) ORDER BY col.ordinal_position
    ) AS column_definitions,
    (
        SELECT string_agg(format('  PRIMARY KEY (%I)', kcu.column_name), ',' || chr(10) ORDER BY kcu.ordinal_position)
        FROM information_schema.table_constraints tc
        JOIN information_schema.key_column_usage kcu
          ON tc.constraint_name = kcu.constraint_name
         AND tc.table_schema = kcu.table_schema
         AND tc.table_name = kcu.table_name
        WHERE tc.constraint_type = 'PRIMARY KEY'
          AND tc.table_schema = t.table_schema
          AND tc.table_name = t.table_name
    ) AS primary_keys,
    (
        SELECT string_agg(format('  UNIQUE (%I)', kcu.column_name), ',' || chr(10))
        FROM information_schema.table_constraints tc
        JOIN information_schema.key_column_usage kcu
          ON tc.constraint_name = kcu.constraint_name
         AND tc.table_schema = kcu.table_schema
         AND tc.table_name = kcu.table_name
        WHERE tc.constraint_type = 'UNIQUE'
          AND tc.table_schema = t.table_schema
          AND tc.table_name = t.table_name
    ) AS unique_keys
FROM information_schema.tables t
JOIN pg_catalog.pg_class c ON c.relname = t.table_name AND c.relkind = 'r'
JOIN pg_catalog.pg_namespace n ON n.oid = c.relnamespace AND n.nspname = t.table_schema
JOIN information_schema.columns col
  ON col.table_schema = t.table_schema AND col.table_name = t.table_name
WHERE t.table_schema = 'public' AND t.table_type = 'BASE TABLE'
GROUP BY t.table_name, t.table_schema, c.oid
ORDER BY t.table_name
