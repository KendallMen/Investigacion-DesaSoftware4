-- Datos de ejemplo. Cada INSERT solo se hace si el carnet no existe,
-- así el script se puede ejecutar en cada arranque sin duplicar filas.
INSERT INTO usuarios (nombre, carnet, correo, carrera)
SELECT N'Ana Rodríguez Mora', 'B10001', 'ana.rodriguez@ucr.ac.cr', N'Informática Empresarial'
WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE carnet = 'B10001');

INSERT INTO usuarios (nombre, carnet, correo, carrera)
SELECT N'Carlos Jiménez Solano', 'B10002', 'carlos.jimenez@ucr.ac.cr', N'Informática Empresarial'
WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE carnet = 'B10002');

INSERT INTO usuarios (nombre, carnet, correo, carrera)
SELECT N'María Fernández Castro', 'B10003', 'maria.fernandez@ucr.ac.cr', N'Computación e Informática'
WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE carnet = 'B10003');

INSERT INTO usuarios (nombre, carnet, correo, carrera)
SELECT N'José Vargas Quesada', 'B10004', 'jose.vargas@ucr.ac.cr', N'Ingeniería Eléctrica'
WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE carnet = 'B10004');

INSERT INTO usuarios (nombre, carnet, correo, carrera)
SELECT N'Laura Chaves Ramírez', 'B10005', 'laura.chaves@ucr.ac.cr', N'Ingeniería Industrial'
WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE carnet = 'B10005');
