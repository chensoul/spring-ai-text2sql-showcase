SELECT
    cols.column_name,
    cols.data_type AS column_type,
    cols.is_nullable,
    cols.column_default,
    pg_catalog.col_description(c.oid, cols.ordinal_position::int) AS column_comment,
    CASE WHEN pk.column_name IS NOT NULL THEN 'PRI' ELSE '' END AS column_key,
    '' AS extra
FROM information_schema.columns cols
JOIN pg_catalog.pg_class c ON c.relname = cols.table_name
JOIN pg_catalog.pg_namespace n ON n.oid = c.relnamespace AND n.nspname = cols.table_schema
LEFT JOIN (
    SELECT kcu.table_schema, kcu.table_name, kcu.column_name
    FROM information_schema.table_constraints tc
    JOIN information_schema.key_column_usage kcu
      ON tc.constraint_name = kcu.constraint_name
     AND tc.table_schema = kcu.table_schema
     AND tc.table_name = kcu.table_name
    WHERE tc.constraint_type = 'PRIMARY KEY'
) pk ON pk.table_schema = cols.table_schema
   AND pk.table_name = cols.table_name
   AND pk.column_name = cols.column_name
WHERE cols.table_schema = 'public' AND cols.table_name = ?
ORDER BY cols.ordinal_position
