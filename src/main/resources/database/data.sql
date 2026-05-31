-- ============================================================
-- data.sql — Datos iniciales de prueba
-- The Girls Club · Universidad Surcolombiana 2026
--
-- Este archivo se ejecuta automáticamente al iniciar la app.
-- ON CONFLICT (correo) DO NOTHING evita duplicados si ya existen.
-- Los IDs son generados por la secuencia de PostgreSQL (NO se especifican)
-- para evitar el error de "llave duplicada en servicios_pkey".
--
-- Contraseña de todos los usuarios: 123456
-- (almacenadas como hash BCrypt $2a$10$...)
-- ============================================================

-- ── Usuarios de prueba ──────────────────────────────────────
INSERT INTO usuarios (correo, nombre, password, rol, activo) VALUES
    ('admin@thegirlsclub.com',
     'Administradora Club',
     '$2a$10$YewUOHGlWQItM/HxlQTo6Ou1KGaKavUkdihH2sKaN3rNU3DmGyHFy',
     'ROLE_ADMIN', true),

    ('lina.cardozo@thegirlsclub.com',
     'Lina Cardozo',
     '$2a$10$znXfJ4YQI/CbaAmMcfam6OxYtNbEf/kN7o0uEvyr4zoWL.uoZlCvu',
     'ROLE_PROFESIONAL', true),

    ('diana.silva@thegirlsclub.com',
     'Diana Silva',
     '$2a$10$FlhPZv47zl6uZ5yEEDJs..7G89vKZIdTLOuprKnylSgRd9pi4dwQe',
     'ROLE_PROFESIONAL', true),

    ('valeria@gmail.com',
     'Valeria Silva',
     '$2a$10$Ns4lupyjT5hqtNIBeGI8zuZ2WmY7bo7HKyCPE.kmjgARmZvy3G6je',
     'ROLE_CLIENTE', true)
ON CONFLICT (correo) DO NOTHING;

-- ── Servicios del catálogo ───────────────────────────────────
-- imagenUrl: URL externa de imagen de referencia para el catálogo.
-- Para usar imágenes locales: /images/servicios/nombre-imagen.jpg
-- (colocar el archivo en src/main/resources/static/images/servicios/)
INSERT INTO servicios (nombre, descripcion, duracion_minutos, precio, activo, imagen_url)
SELECT * FROM (VALUES
    ('Acrílicas Esculpidas',
     'Extensión de uñas premium con esculpido artesanal y diseño personalizado.',
     120, 120000.00, true,
     'https://images.unsplash.com/photo-1604654894610-df63bc536371?w=300'),

    ('Diseño Semipermanente',
     'Esmaltado de alta durabilidad con nivelación y cristales decorativos.',
     60, 65000.00, true,
     'https://images.unsplash.com/photo-1604654894610-df63bc536371?w=300'),

    ('Pedicura de Alta Precisión',
     'Spa de pies profundo con exfoliación orgánica y esmaltado semipermanente.',
     75, 55000.00, true,
     'https://images.unsplash.com/photo-1604654894610-df63bc536371?w=300')
) AS nuevos(nombre, descripcion, duracion_minutos, precio, activo, imagen_url)
WHERE NOT EXISTS (SELECT 1 FROM servicios LIMIT 1);

-- ── Reservas de prueba ───────────────────────────────────────
INSERT INTO reservas (cliente_id, profesional_id, servicio_id, fecha_hora_cita, estado, metodo_pago)
SELECT
    c.id, p.id, s.id,
    '2026-05-26 10:30:00',
    'PENDIENTE',
    'EFECTIVO'
FROM usuarios c, usuarios p, servicios s
WHERE c.correo = 'valeria@gmail.com'
  AND p.correo = 'lina.cardozo@thegirlsclub.com'
  AND s.nombre = 'Acrílicas Esculpidas'
  AND NOT EXISTS (SELECT 1 FROM reservas LIMIT 1);

INSERT INTO reservas (cliente_id, profesional_id, servicio_id, fecha_hora_cita, estado, metodo_pago)
SELECT
    c.id, p.id, s.id,
    '2026-05-27 15:00:00',
    'PENDIENTE',
    'TARJETA'
FROM usuarios c, usuarios p, servicios s
WHERE c.correo = 'valeria@gmail.com'
  AND p.correo = 'diana.silva@thegirlsclub.com'
  AND s.nombre = 'Diseño Semipermanente'
  AND NOT EXISTS (SELECT 1 FROM reservas WHERE fecha_hora_cita = '2026-05-27 15:00:00');
