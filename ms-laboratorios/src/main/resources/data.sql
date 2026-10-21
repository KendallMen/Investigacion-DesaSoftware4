-- Datos de ejemplo. Cada INSERT solo se hace si el nombre no existe,
-- así el script se puede ejecutar en cada arranque sin duplicar filas.
-- El Lab 4 queda no disponible para probar esa regla en reservas.
INSERT INTO laboratorios (nombre, edificio, capacidad, disponible)
SELECT N'Laboratorio de Cómputo 1', N'Edificio de Ciencias Sociales', 30, 1
WHERE NOT EXISTS (SELECT 1 FROM laboratorios WHERE nombre = N'Laboratorio de Cómputo 1');

INSERT INTO laboratorios (nombre, edificio, capacidad, disponible)
SELECT N'Laboratorio de Cómputo 2', N'Edificio de Ciencias Sociales', 25, 1
WHERE NOT EXISTS (SELECT 1 FROM laboratorios WHERE nombre = N'Laboratorio de Cómputo 2');

INSERT INTO laboratorios (nombre, edificio, capacidad, disponible)
SELECT N'Laboratorio de Redes', N'Edificio de Ingeniería', 20, 1
WHERE NOT EXISTS (SELECT 1 FROM laboratorios WHERE nombre = N'Laboratorio de Redes');

INSERT INTO laboratorios (nombre, edificio, capacidad, disponible)
SELECT N'Laboratorio de Electrónica', N'Edificio de Ingeniería', 15, 0
WHERE NOT EXISTS (SELECT 1 FROM laboratorios WHERE nombre = N'Laboratorio de Electrónica');
