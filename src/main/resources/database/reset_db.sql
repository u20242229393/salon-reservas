-- ============================================================
-- reset_db.sql — Script de limpieza y reinicio de la base de datos
-- The Girls Club · Universidad Surcolombiana 2026
--
-- USO: Ejecutar en pgAdmin cuando se necesite reiniciar los datos.
-- ADVERTENCIA: Elimina TODOS los datos existentes.
--
-- Pasos:
-- 1. Abrir pgAdmin → Query Tool en la BD salon_reservas
-- 2. Pegar y ejecutar este script
-- 3. Reiniciar la aplicación desde IntelliJ
--    (schema.sql y data.sql recrean las tablas automáticamente)
-- ============================================================

-- Eliminar tablas en orden correcto (reservas primero por las FK)
DROP TABLE IF EXISTS reservas CASCADE;
DROP TABLE IF EXISTS servicios CASCADE;
DROP TABLE IF EXISTS usuarios CASCADE;

-- ============================================================
-- fix_sequences.sql — Corregir secuencias de IDs desfasadas
--
-- Ejecutar si aparece el error:
-- "llave duplicada viola restricción de unicidad «xxx_pkey»"
--
-- Ocurre cuando el data.sql inserta registros con IDs específicos
-- (1, 2, 3) pero la secuencia de PostgreSQL sigue en 1.
-- ============================================================
SELECT setval('servicios_id_seq', (SELECT MAX(id) FROM servicios));
SELECT setval('usuarios_id_seq',  (SELECT MAX(id) FROM usuarios));
SELECT setval('reservas_id_seq',  (SELECT MAX(id) FROM reservas));
