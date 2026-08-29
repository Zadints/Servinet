USE master;
GO

--DROP DATABASE ServiNet;

-- 1. Crear base de datos
IF NOT EXISTS (SELECT * FROM sys.databases WHERE name = 'ServiNet')
BEGIN
    CREATE DATABASE ServiNet2;
END
GO

USE ServiNet2;
GO

-- 2. Limpieza de tablas existentes en orden de dependencia
IF OBJECT_ID('logs', 'U') IS NOT NULL DROP TABLE logs;
IF OBJECT_ID('pagos', 'U') IS NOT NULL DROP TABLE pagos;
IF OBJECT_ID('clientes', 'U') IS NOT NULL DROP TABLE clientes;
IF OBJECT_ID('planes', 'U') IS NOT NULL DROP TABLE planes;
IF OBJECT_ID('antenas', 'U') IS NOT NULL DROP TABLE antenas;
IF OBJECT_ID('personal', 'U') IS NOT NULL DROP TABLE personal;
IF OBJECT_ID('usuarios', 'U') IS NOT NULL DROP TABLE usuarios;
GO

-- 3. Tabla: planes
CREATE TABLE planes (
                        id varchar(10) NOT NULL PRIMARY KEY,
                        nombre varchar(100) NOT NULL,
                        velocidad int NOT NULL,
                        precio decimal(10,2) NOT NULL,
                        estado varchar(10) NOT NULL DEFAULT 'Activo' CHECK (estado IN ('Activo', 'Inactivo'))
);
GO

-- 4. Tabla: antenas
CREATE TABLE antenas (
                         id varchar(10) NOT NULL PRIMARY KEY,
                         nombre varchar(120) NOT NULL,
                         ubicacion varchar(200) NULL,
                         sector varchar(50) NULL,
                         foto varchar(255) NULL
);
GO

-- 5. Tabla: clientes
CREATE TABLE clientes (
                          id varchar(10) NOT NULL PRIMARY KEY,
                          nombre varchar(150) NOT NULL,
                          dni varchar(8) NULL,
                          tel varchar(20) NULL,
                          direccion varchar(200) NULL,
                          sector varchar(50) NULL,
                          plan_id varchar(10) NULL,
                          antena_id varchar(10) NULL,
                          fecha_instalacion date NULL,
                          estado varchar(20) NOT NULL DEFAULT 'Con deuda' CHECK (estado IN ('Al día', 'Con deuda')),
                          router_foto varchar(255) NULL,
                          CONSTRAINT FK_clientes_planes FOREIGN KEY (plan_id) REFERENCES planes(id) ON UPDATE CASCADE ON DELETE SET NULL,
                          CONSTRAINT FK_clientes_antenas FOREIGN KEY (antena_id) REFERENCES antenas(id) ON UPDATE CASCADE ON DELETE SET NULL
);
GO



-- 7. Tabla: usuarios (Administradores / Sistema)
CREATE TABLE usuarios (
                          id int IDENTITY(1,1) NOT NULL PRIMARY KEY,
                          usuario varchar(50) NOT NULL UNIQUE,
                          password_hash varchar(255) NOT NULL,
                          creado_en datetime2 NOT NULL DEFAULT GETDATE()
);
GO

-- 8. Tabla: personal (Técnicos / Operadores)
CREATE TABLE personal (
                          id int IDENTITY(1,1) NOT NULL PRIMARY KEY,
                          usuario varchar(50) NOT NULL UNIQUE,
                          password_hash varchar(255) NOT NULL,
                          creado_en datetime2 NOT NULL DEFAULT GETDATE()
);
GO

-- 9. Tabla: logs (Registro de actividades del personal)
CREATE TABLE logs (
                      id int IDENTITY(1,1) NOT NULL PRIMARY KEY,
                      personal_id int NOT NULL,
                      accion varchar(max) NOT NULL,
    fecha datetime2 NOT NULL DEFAULT GETDATE(),
    CONSTRAINT FK_logs_personal FOREIGN KEY (personal_id) REFERENCES personal(id) ON UPDATE CASCADE ON DELETE CASCADE
);
GO

-- 6. Tabla: pagos
CREATE TABLE pagos (
                       id varchar(20) NOT NULL PRIMARY KEY,
                       cliente_id varchar(10) NOT NULL,
                       mes varchar(50) NOT NULL,
                       monto decimal(10,2) NOT NULL,
                       fecha date NULL,
                       estado varchar(15) NOT NULL DEFAULT 'Pendiente' CHECK (estado IN ('Pagado', 'Pendiente')),
                       metodo varchar(50) NULL,
                       obs varchar(max) NULL,
    CONSTRAINT FK_pagos_clientes FOREIGN KEY (cliente_id) REFERENCES clientes(id) ON UPDATE CASCADE ON DELETE CASCADE
);

-- Índice para optimizar búsquedas de auditoría por personal
CREATE INDEX idx_logs_personal_id ON logs(personal_id);
GO


INSERT INTO clientes (
    id, nombre, dni, tel, direccion, sector, plan_id, antena_id, fecha_instalacion, estado, router_foto
) VALUES
('001', 'Viviana Rubio', '47061631', '995506514', 'Mz C lote 27 Barrio 1-A (Alto Trujillo)', 'Sector 1', 'P02', 'A1', '2026-05-01', 'Con deuda', NULL),
('002', 'Pochita Pacalla', '40599298', '943378227', 'MZ B2 lote 7 Barrio 1 (Alto Trujillo)', 'Sector 1', 'P02', 'A1', '2026-05-03', 'Con deuda', NULL),
('003', 'Miuler Aguirre', '72939707', '939833866', 'Mz LL lote 41 Barrio 1-A (Alto Trujillo)', 'Sector 1', 'P02', 'A1', '2026-05-05', 'Con deuda', NULL),
('004', 'Lucero Aquino', '60197460', '968291593', 'Mz F lote 12 Barrio 1-A (Alto Trujillo)', 'Sector 1', 'P02', 'A1', '2026-05-17', 'Con deuda', NULL),
('005', 'Yaquelin', '42747053', '932881754', 'MZ M lote 4 Barrio 1-A (Alto Trujillo)', 'Sector 1', 'P02', 'A1', '2026-05-01', 'Con deuda', NULL),
('006', 'Alessia Gamboa', '12345678', '943972269', 'Mz Q lote 04 Barrio 1-A (Alto Trujillo', 'Sector 1', 'P02', 'A1', '2026-05-17', 'Con deuda', 'uploads/routers/008_1782092533.jpg'),
('007', 'Anthony', '77141022', '992126160', 'Mz F lote 18 Barrio 1-A (Alto Trujillo)', 'Sector 1', 'P02', 'A1', '2026-05-14', 'Al día', 'uploads/routers/009_1782092546.jpg'),
('008', 'Aron Gamboa', '18222765', '948436804', 'Mz Q lote 4 Barrio 1-A (Alto Trujillo)', 'Sector 1', 'P02', 'A1', '2026-04-11', 'Al día', 'uploads/routers/010_1782092629.jpg'),
('009', 'Artemio Vega', '48791741', '914368812', 'Mz X lote 26 Sector Nuevo Jerusalen', 'Sector 1', 'P02', 'A1', '2026-05-25', 'Con deuda', 'uploads/routers/011_1782092647.jpg'),
('010', 'Robert Goicochea', '43887198', '925473313', 'Mz C lote 14 Barrio 1-A (Alto Trujillo', 'Sector 1', 'P02', 'A1', '2026-05-14', 'Con deuda', NULL),
('011', 'Debora', '75894958', '928518529', 'Mz N lote 03 Barrio 1-A (Alto Trujillo)', 'Sector 1', 'P02', 'A1', '2026-05-15', 'Con deuda', NULL),
('012', 'Nelida', '18185118', '910888736', 'Mz C lote 19 Barrio 1-A (Alto Trujillo)', 'Sector 1', 'P02', 'A1', '2026-05-02', 'Con deuda', NULL),
('013', 'Yobana Galarreta', '44128179', '940666196', 'Mz F lote 2 Barrio 1-A (Alto Trujillo)', 'Sector 1', 'P02', 'A1', '2026-05-03', 'Con deuda', NULL),
('014', 'Ilton Mantilla', '47356250', '964632989', 'Mz B lote 28 Barrio 1-A (Alto Trujillo)', 'Sector 1', 'P02', 'A1', '2026-05-20', 'Con deuda', NULL),
('015', 'Claudia', '44941076', '956688981', 'Mz B lote 23 Barrio 1-A (Alto Trujillo)', 'Sector 1', 'P02', 'A1', '2026-05-01', 'Con deuda', NULL),
('016', 'Analy Ruiz', '48917304', '931159823', 'Mz D lote 15 Barrio 1-A (Alto Trujillo)', 'Sector 1', 'P02', 'A1', '2026-05-14', 'Con deuda', NULL),
('017', 'Jhorvis', '71620554', '974842954', 'Mz C lote 22 Barrio 1-A (Alto Trujillo)', 'Sector 1', 'P02', 'A1', '2026-05-23', 'Con deuda', NULL),
('018', 'Ines Rodriguez', '42868835', '929928311', 'Mz Ll lote 16 Barrio 1-A (Alto Trujillo)', 'Sector 1', 'P02', 'A1', '2026-05-11', 'Con deuda', NULL),
('019', 'Juan Reyna', '80239543', '944592375', 'Mz K lote 7 Barrio 2 (Alto Trujillo)', 'Sector 1', 'P02', 'A1', '2026-05-01', 'Con deuda', NULL),
('020', 'Mazinhio Ulloa', '46276904', '964285439', 'Mz W lote 6 Barrio 1', 'Sector 1', 'P02', 'A1', '2026-05-06', 'Con deuda', NULL),
('021', 'Luis Teran', '48195723', '941891653', 'Mz Ñ lote 9 Barrio 1 (Alto Trujillo)', 'Sector 1', 'P02', 'A1', '2026-05-14', 'Con deuda', NULL),
('022', 'Jesica', '44270426', '990846753', 'Mz F lote 11 Ampliación los Laureles', 'Sector 1', 'P02', 'A1', '2026-05-21', 'Con deuda', NULL),
('023', 'Cintia', '61370229', '967311700', 'Mz Ll lote 35 Barrio 1-A (Alto Trujillo)', 'Sector 1', 'P02', 'A1', '2026-05-08', 'Con deuda', NULL),
('024', 'Yanina', '44802991', '941726226', 'Mz LL lote 13 Barrio 1-A (Alto Trujillo)', 'Sector 1', 'P02', 'A1', '2026-05-05', 'Con deuda', NULL),
('025', 'Aresio Villegas', '41665916', '928965464', 'Mz C lote 13 Barrio 1-A (Alto Trujillo)', 'Sector 1', 'P02', 'A1', '2026-05-10', 'Con deuda', 'uploads/routers/030_1782092402.jpg'),
('026', 'Fermin Garcia', '47847391', '988901438', 'Mz C lote 6 Barrio 1-A (Alto Trujillo)', 'Sector 1', 'P02', 'A1', '2026-05-15', 'Con deuda', NULL),
('027', 'Jhonatan Horna', '71943287', '910370702', 'Mz A6 lote 18 Barrio 1-A (Alto Trujillo)', 'Sector 1', 'P02', 'A1', '2026-05-08', 'Con deuda', NULL),
('028', 'Agustin', '80209592', '966119870', 'Mz K lote 8 Calle 22 de Febrero (Florencia de Mora)', 'Sector 1', 'P02', 'A1', '2026-05-22', 'Con deuda', NULL),
('029', 'Luis Chunga', '44264956', '912373594', 'Mz B6 lote 4 22 de Febrero (Florencia de Mora)', 'Sector 1', 'P02', 'A1', '2026-05-25', 'Con deuda', NULL),
('030', 'Raquel Valderrama', '45677844', '918177695', 'Mz K lote 6 Calle los Laureles (Florencia de Mora)', 'Sector 1', 'P02', 'A1', '2026-05-20', 'Con deuda', NULL),
('031', 'Sheyla Infantes', '47870098', '946931232', 'Mz 6B lote 9A Pasaje 22 de febrero (Florencia de Mora)', 'Sector 1', 'P02', 'A1', '2026-05-18', 'Con deuda', NULL),
('032', 'Roxana Minachoque', '48181338', '936718246', 'Mz J lote 3 Nuevo Jerusalen 1°ra Etapa (La Esperanza)', 'Sector 1', 'P02', 'A1', '2026-05-22', 'Con deuda', NULL),
('033', 'Gladis', '43263733', '958259110', 'MZ Q lote 18 Barrio 1-A (Alto Trujillo)', 'Sector 1', 'P02', 'A1', '2026-05-10', 'Con deuda', NULL),
('034', 'Estefany', '77092202', '943976773', 'Mz Q lote 28 Barrio 1-A (Alto Trujillo)', 'Sector 1', 'P02', 'A1', '2026-05-03', 'Con deuda', NULL),
('035', 'Yessenia Aguilar', '48185061', '922498835', 'MZ W lote 37 Barrio 1 (Alto Trujillo)', 'Sector 1', 'P02', 'A1', '2026-05-22', 'Con deuda', NULL),
('036', 'Aide Salinas', '32969982', '925673269', 'MZ D lote 12 Barrio 1-A (Alto Trujillo)', 'Sector 1', 'P02', 'A1', '2026-05-11', 'Al día', NULL),
('037', 'Analy Uriol', '80079318', '952581605', 'MZ A lote 34 Nuevo Jerusalen 3°ra Etapa', 'Sector 1', 'P02', 'A1', '2026-05-19', 'Con deuda', NULL),
('038', 'Andres Dominguez', '45201774', '950010871', 'MZ 29 lote 19 Nuevo Jerusalen 4°ta Etapa (La Esperanza)', 'Sector 1', 'P02', 'A1', '2026-05-14', 'Con deuda', NULL),
('039', 'Doris Castillo', '44252491', '951827593', 'MZ A lote 34 Barrio 1 - B Las Flores', 'Sector 1', 'P02', 'A1', '2026-05-18', 'Con deuda', NULL),
('040', 'Rose Rubio', '45782564', '957302936', 'MZ A1 lote 7 Barrio 1-A (Alto Trujillo)', 'Sector 1', 'P02', 'A1', '2026-05-05', 'Con deuda', NULL),
('041', 'Nelson Laguna', '48153971', '978725885', 'MZ A1 lote 09 Barrio 1-A (Alto Trujillo)', 'Sector 1', 'P02', 'A1', '2026-05-08', 'Con deuda', NULL),
('042', 'Silvia Ullilen', '76875658', '982843842', 'MZ N lote 09 Barrio 1-A (Alto Trujillo)', 'Sector 1', 'P02', 'A1', '2026-05-01', 'Con deuda', NULL),
('043', 'Jefersson Laiza', '73437877', '967370884', 'Mz LL lote 40 Barrio 1-A (Alto Trujillo)', 'Sector 1', 'P02', 'A1', '2026-05-16', 'Con deuda', NULL),
('044', 'Manuel Diestra', '80430576', '930547803', 'Mz A1 lote 4 Barrio 1-A (Alto Trujillo)', 'Sector 1', 'P02', 'A1', '2026-05-07', 'Con deuda', NULL),
('045', 'Milagros Campos', '75259024', '912107332', 'Mz A2 lote 14 Barrio 1-A (Alto Trujillo)', 'Sector 1', 'P02', 'A1', '2026-05-23', 'Con deuda', NULL),
('046', 'Luz', '70001825', '975130427', 'Nuevo Jerusalen', 'Sector 1', 'P02', 'A1', '2026-05-15', 'Al día', NULL),
('047', 'Carlos Manuel', '80143640', '947338112', 'Mz M lote 17 Barrio 1-A (Alto Trujillo)', 'Sector 1', 'P02', 'A1', '2026-05-20', 'Con deuda', NULL),
('048', 'Sara Laguna', '48217419', '916185806', 'Mz A lote 12 Barrio 1-B Las Flores (Alto Trujillo)', 'Sector 2', 'P02', 'A2', '2026-06-09', 'Al día', NULL),
('049', 'Adriana Vargaz', '75499040', '928238353', 'Mz T lote 2 Barrio 1-B Ampliación las Flores (Alto Trujillo)', 'Sector 2', 'P02', 'A2', '2026-05-23', 'Con deuda', NULL),
('050', 'Marilin', '46241016', '939359938', 'Mz T lote 3 Barrio 1-B Ampliación las Flores (Alto Trujillo)', 'Sector 2', 'P02', 'A2', '2026-05-13', 'Con deuda', NULL),
('051', 'Abel Reyes', '45955529', '929261007', 'Mz A lote 18 Barrio 1-B Las Flores (Alto Trujillo)', 'Sector 2', 'P02', 'A2', '2026-05-13', 'Con deuda', NULL),
('052', 'Eduard Villareal', '48554823', '924837324', 'Mz 12 lote 3 Nuevo Jerusalen 4ta Etapa (La Esperanza)', 'Sector 1', 'P02', 'A2', '2026-05-10', 'Con deuda', NULL),
('053', 'Yamil Corrales', '74157231', '903388441', 'Mz 12 lote 4 Nuevo Jerusalen 4ta Etapa (La Esperanza)', 'Sector 2', 'P02', 'A2', '2026-05-21', 'Con deuda', NULL),
('054', 'Paul Chavez', '47049120', '927544787', 'Mz C lote 22 Barrio 1-B (Alto Trujillo)', 'Sector 1', 'P02', 'A2', '2026-05-30', 'Al día', NULL),
('055', 'Carlos Pisco', '43212409', '930561512', 'Mz J lote 22 Barrio 1-A (Alto Trujillo)', 'Sector 1', 'P02', 'A2', '2026-05-29', 'Al día', NULL),
('056', 'Cesar', '48937285', '959547999', 'Mz I lote 22 Barrio 1-A (Alto Trujillo)', 'Sector 2', 'P02', 'A2', '2026-05-22', 'Con deuda', NULL),
('057', 'Dilser Mendez', '75810909', '987834747', 'Mz J lote 34 Barrio 1-A (Alto Trujillo)', 'Sector 1', 'P02', 'A2', '2026-05-01', 'Con deuda', NULL),
('058', 'Elvis Leiva', '74560815', '938275260', 'Mz X lote 5 Barrio 2-A (Alto Trujillo)', 'Sector 1', 'P02', 'A2', '2026-05-07', 'Con deuda', NULL),
('059', 'Freddy Sidan', '40982943', '916755538', 'Mz K lote 30 Barrio 1-A (Alto Trujillo)', 'Sector 1', 'P02', 'A2', '2026-05-01', 'Con deuda', NULL),
('060', 'Ermenegildo Mesa', '71200345', '928333730', 'Mz B lote 27 Barrio 1-A (Alto Trujillo)', 'Sector 2', 'P02', 'A2', '2026-05-05', 'Al día', NULL),
('061', 'Lucy Crisanto', '72510583', '930823426', 'Mz J lote 12 Barrio 1-A (Alto Trujillo)', 'Sector 2', 'P02', 'A2', '2026-05-20', 'Con deuda', NULL),
('062', 'Luisa Rojas', '43056539', '918379173', 'Mz A lote 20 Barrio 1-A (Alto Trujillo)', 'Sector 2', 'P02', 'A2', '2026-05-20', 'Con deuda', NULL),
('063', 'Reymundo', '41834456', '959584036', 'Mz I lote 18 Barrio 1-A (Alto Trujillo)', 'Sector 1', 'P02', 'A2', '2026-05-05', 'Con deuda', NULL),
('064', 'Richard Rodriguez', '41084736', '916350882', 'Mz A lote 31 Barrio 1-B (Alto Trujillo)', 'Sector 1', 'P02', 'A2', '2026-05-03', 'Con deuda', NULL),
('065', 'Maria Vigo', '71797338', '918975792', 'Mz B lote 10 Nuevo Jerusalen (La Esperanza )', 'Sector 1', 'P02', 'A2', '2026-05-27', 'Al día', NULL),
('066', 'Adriana Liñan', '41552752', '946860317', 'Mz 6 lote 7 Barrio 1-A (Alto Trujillo)', 'Sector 2', 'P02', 'A2', '2026-05-22', 'Con deuda', NULL),
('067', 'Caldas Pisan', '44853868', '926088648', 'Mz B lote 24 Barrio 1-B Las Flores (Alto Trujillo)', 'Sector 1', 'P02', 'A2', '2026-04-01', 'Con deuda', NULL),
('068', 'Asuncion Salinas', '42482140', '922276320', 'Mz A4 lote 17 Barrio 1-A (Alto Trujillo)', 'Sector 2', 'P02', 'A2', '2026-05-16', 'Con deuda', NULL),
('069', 'Fausto', '46956170', '983060609', 'Mz A4 lote 21 Barrio 1-A (Alto Trujillo)', 'Sector 2', 'P02', 'A2', '2026-05-18', 'Con deuda', NULL),
('070', 'Fiorella Calderon', '71277152', '910261364', 'Mz D lote 25 Barrio 1-B Sector Las Flores (Alto Trujillo)', 'Sector 1', 'P02', 'A2', '2026-05-05', 'Con deuda', NULL),
('071', 'Isabel Morales', '70348832', '991295048', 'Mz A5 lote 25 Barrio 1-A (Alto Trujillo)', 'Sector 1', 'P02', 'A2', '2026-05-01', 'Con deuda', NULL),
('072', 'Miguel Capristan', '44420108', '940198882', 'Mz 5 lote 6 Barrio 1-A (Alto Trujillo)', 'Sector 1', 'P02', 'A2', '2026-05-01', 'Con deuda', NULL),
('073', 'Omar Samana', '46599108', '967482241', 'Mz Y lote 9 Nuevo Jerusalen (La Esperanza)', 'Sector 1', 'P02', 'A2', '2026-05-07', 'Con deuda', NULL),
('074', 'Tejada', '77086584', '901210944', 'Mz D lote 7 Barrio 1-B Las Flores (Alto Trujillo)', 'Sector 2', 'P02', 'A2', '2026-05-14', 'Con deuda', NULL),
('075', 'Estefany Rios', '74119594', '962274979', 'Mz J lote 29 Barrio 1-A (Alto Trujillo)', 'Sector 1', 'P02', 'A2', '2026-05-30', 'Al día', NULL),
('076', 'Mirta Crisanto', '73452861', '934866981', 'Mz I lote 16 Barrio 1-B Las Flores (Alto Trujillo)', 'Sector 1', 'P02', 'A2', '2026-05-06', 'Con deuda', NULL),
('077', 'Isolina Gonzales', '17955215', '915994891', 'Mz B lote 1 Barrio 1-B Las Flores (Alto Trujillo)', 'Sector 1', 'P02', 'A2', '2026-05-30', 'Al día', NULL),
('078', 'Luis Aguilar', '19696889', '978816618', 'Mz 4A lote 26 Barrio 1-A (Alto Trujillo)', 'Sector 2', 'P02', 'A2', '2026-05-20', 'Con deuda', NULL),
('079', 'Maria Goicochea', '19415649', '986427016', 'Mz A5 lote 24 Barrio 1-A (Alto Trujillo)', 'Sector 2', 'P02', 'A2', '2026-05-16', 'Con deuda', NULL),
('080', 'Maryet', '47530866', '948291763', 'Mz A7 lote 5 Barrio 1-A (Alto Trujillo)', 'Sector 1', 'P02', 'A1', '2026-05-10', 'Con deuda', NULL),
('081', 'Angie Lopez', '75103151', '916365771', 'Mz Q lote 05 Barrio 1-B Pedro Ordoñez (Alto Trujillo)', 'Sector 1', 'P02', 'A3', '2026-05-06', 'Con deuda', NULL),
('082', 'Davsi More', '77060634', '928833876', 'Mz Y lote 28 Barrio 1-b Sector Las Flores (Alto Trujillo)', 'Sector 3', 'P02', 'A3', '2026-05-25', 'Con deuda', NULL),
('083', 'Jose Avila', '17956668', '951974122', 'Mz I lote 34 Barrio 1-A Sector las Flores (Alto Trujillo)', 'Sector 3', 'P02', 'A3', '2026-05-25', 'Con deuda', NULL),
('084', 'Lucia Acuña', '80311117', '917736055', 'Mz Y lote 35 Barrio 1-B Sector Las Flores (Alto Trujillo)', 'Sector 3', 'P02', 'A3', '2026-05-21', 'Con deuda', NULL),
('085', 'Minsi Samana', '48616896', '930780407', 'Mz  J lote 30 Barrio 1-B Sector Las Flores(Alto Trujillo)', 'Sector 3', 'P02', 'A3', '2026-05-19', 'Con deuda', NULL),
('086', 'Breidin', '70865122', '983791994', 'Mz A7 lote 14 Barrio 1-A Los Rosales(Alto Trujillo)', 'Sector 1', 'P02', 'A3', '2026-05-03', 'Con deuda', NULL),
('087', 'Coronel Padilla', '46525725', '929796070', 'Mz E lote 16 Barrio 1-B Las Flores(Alto Trujillo)', 'Sector 1', 'P02', 'A3', '2026-05-28', 'Al día', NULL),
('088', 'Edilson Juarez', '74450732', '968305164', 'Mz C lote 17 Barrio 1-A Las Flores (Alto Trujillo)', 'Sector 3', 'P02', 'A3', '2026-05-22', 'Con deuda', NULL),
('089', 'Jean Mariños', '71196705', '929812665', 'Mz M lote 10 Barrio 1-B Las Flores (Alto Trujillo)', 'Sector 1', 'P02', 'A3', '2026-05-01', 'Con deuda', NULL),
('090', 'Jose Torres', '46715593', '969557746', 'Mz C lote 13 Barrio 1-B Las Flores (Alto Trujillo)', 'Sector 3', 'P02', 'A3', '2026-05-18', 'Con deuda', NULL),
('091', 'Oscar Sierra', '43318909', '992865901', 'Mz B lote 13 Barrio 1-B (Alto Trujillo)', 'Sector 3', 'P02', 'A3', '2026-05-26', 'Con deuda', NULL),
('092', 'Kevin Pilsen', '48170807', '921250001', 'Mz A lote 6 Barrio 1-B (Alto Trujillo)', 'Sector 1', 'P02', 'A3', '2026-05-08', 'Con deuda', NULL),
('093', 'Tomas Alcantara', '18055177', '996551888', 'Mz E lote 14 Barrio 1-B Las Flores (Alto Trujillo)', 'Sector 1', 'P02', 'A3', '2026-05-01', 'Con deuda', NULL),
('094', 'Valeria Ibañez', '70355704', '966588766', 'Mz 7 lote 1 Barrio 1-A Sector Los Rosales (Alto Trujillo)', 'Sector 1', 'P02', 'A3', '2026-05-01', 'Con deuda', NULL),
('095', 'Vanessa Laiza', '60240859', '936770915', 'Mz E lote 6 Barrio 1-B Las Flores (Alto Trujillo)', 'Sector 1', 'P02', 'A3', '2026-05-01', 'Con deuda', NULL),
('096', 'Irma Cruz', '73931364', '986149868', 'Mz A lote 3 Barrio 1-B Pedro Ordoñes (Alto Trujillo)', 'Sector 3', 'P02', 'A3', '2026-05-16', 'Con deuda', NULL),
('097', 'Serapio', '46191640', '926188507', 'Mz A6 lote 15 Barrio 1-A (Alto Trujillo)', 'Sector 1', 'P02', 'A3', '2026-05-11', 'Con deuda', NULL),
('098', 'Ronald Vasquez', '44242405', '918160324', 'Mz D lote 3 Barrio 1-B Pedro Ordoñez (Alto Trujillo)', 'Sector 3', 'P02', 'A3', '2026-05-14', 'Con deuda', NULL),
('099', 'Cintia Velasquez', '46923997', '910906065', 'Mz F lote 12Barrio 1-B Las Flores (Alto Trujillo)', 'Sector 3', 'P02', 'A3', '2026-05-24', 'Con deuda', NULL),
('100', 'Yudith', '45331108', '952998164', 'Mz I lote 3 Barrio 1-B (Alto Trujillo)', 'Sector 1', 'P02', 'A3', '2026-05-09', 'Con deuda', NULL),
('101', 'Kely Rodriguez', '48701259', '954915788', 'Mz V lote 6 Barrio 2-A (Alto Trujillo)', 'Sector 1', 'P02', 'A3', '2026-05-04', 'Con deuda', NULL),
('102', 'Roger Quiliche', '47157722', '986280850', 'Mz A7 lote 11 Barrio 1-A (Alto Trujillo)', 'Sector 1', 'P02', 'A3', '2026-05-07', 'Con deuda', NULL),
('103', 'Santos', '32789342', '949067373', 'Mz A4 lote 8 Barrio 1-B (Alto Trujillo)', 'Sector 1', 'P02', 'A3', '2026-05-10', 'Con deuda', NULL),
('104', 'Yesenia Vera', '73004033', '913969870', 'Mz A7 lote 43 Barrio 1-A Los Rosales (Alto Trujillo)', 'Sector 3', 'P02', 'A3', '2026-05-15', 'Con deuda', NULL),
('105', 'Joana Hernandez', '48342237', '902855886', 'Mz LL lote 18 Barrio 2 (Alto Trujillo)', 'Sector 3', 'P02', 'A3', '2026-05-21', 'Con deuda', NULL),
('106', 'Marco Quizpe', '60836352', '935964561', 'Mz G lote 21 Pedro Ordoñez (Alto Trujillo)', 'Sector 3', 'P02', 'A3', '2026-05-18', 'Con deuda', NULL),
('107', 'Rolando Izuisa', '45662781', '919168198', 'Mz A1 lote 4 Los Rosales(Alto Trujillo)', 'Sector 1', 'P02', 'A3', '2026-05-05', 'Con deuda', NULL),
('108', 'Jaime Escovedo', '60852266', '902398978', 'Mz D lote 23  Pedro Ordoñez(Alto Trujillo)', 'Sector 1', 'P02', 'A3', '2026-05-09', 'Con deuda', NULL),
('109', 'Cristina Garcia', '60491054', '938357103', 'Mz T lote 1 Ampliacion Las Flores (Alto Trujillo)', 'Sector 1', 'P02', 'A3', '2026-05-30', 'Al día', NULL),
('110', 'Neri Rodriguez', '45098905', '972546287', 'Mz F lote 13 Barrio 1-B Las Flores (Alto Trujillo)', 'Sector 1', 'P02', 'A3', '2026-05-03', 'Con deuda', NULL),
('111', 'Analy Avaloz', '74532349', '922406957', 'Mz U lote 2 Barrio 2-B  (Alto Trujillo)', 'Sector 1', 'P02', 'A4', '2026-05-11', 'Con deuda', NULL),
('112', 'Karina Rubio', '40163355', '923081953', 'Mz T lote 30 Barrio 1-B Sector Monte de Ore (Alto Trujillo)', 'Sector 1', 'P02', 'A4', '2026-05-08', 'Con deuda', NULL),
('113', 'Manuel Lucas', '75048072', '953556086', 'Mz U lote 20 Barrio 1-B (Alto Trujillo)', 'Sector 4', 'P02', 'A4', '2026-05-18', 'Con deuda', NULL),
('114', 'Victor', '41971290', '926205900', 'Mz G lote 6 Barrio 1-A (Alto Trujillo)', 'Sector 1', 'P02', 'A2', '2026-05-10', 'Con deuda', NULL),
('115', 'Jesus Castillo', '44894854', '975339744', 'Mz D lote 2 Pedro Ordoñez (Alto Trujillo)', 'Sector 3', 'P02', 'A3', '2026-05-26', 'Con deuda', NULL),
('116', 'Yaquelin Rios', '48800562', '935574746', 'Mz A7 lote 40 Barrio 1-A (Alto Trujillo)', 'Sector 1', 'P02', 'A3', '2026-05-10', 'Con deuda', NULL),
('117', 'Augusto Alfaro', '18119534', '992012768', 'Mz K lote 9 Barrio 3-B (Alto Trujillo)', 'Sector 1', 'P02', 'A4', '2026-05-01', 'Con deuda', NULL),
('118', 'Geiner', '44955046', '941405755', 'Mz D lote 42 Barrio 3-B (Alto Trujillo)', 'Sector 4', 'P02', 'A4', '2026-05-17', 'Con deuda', NULL),
('119', 'Deysi Castillo', '71781633', '942055656', 'Mz G lote 25 Barrio 2-B Roberto Solar (Alto Trujillo)', 'Sector 1', 'P02', 'A4', '2026-05-01', 'Con deuda', NULL),
('120', 'Diana Rodriguez', '45725175', '929335308', 'Mz E lote 94 Barrio 2-B Villa Clementina (Alto Trujillo)', 'Sector 1', 'P02', 'A4', '2026-05-01', 'Con deuda', NULL),
('121', 'Elton Ruiz', '72103370', '901702680', 'Mz 12 lote 31 Barrio 2-B (Alto Trujillo)', 'Sector 4', 'P02', 'A4', '2026-05-14', 'Con deuda', NULL),
('122', 'Lili Games', '19434169', '969398008', 'Mz 12 lote 19 Barrio 2-B (Alto Trujillo)', 'Sector 1', 'P02', 'A4', '2026-05-01', 'Con deuda', NULL),
('123', 'Melva Zavaleta', '45202795', '960302569', 'Mz O lote 6 Barrio 2-B Villa Clementina (Alto Trujillo)', 'Sector 4', 'P02', 'A4', '2026-05-20', 'Con deuda', NULL),
('124', 'Aide Mejia', '46211046', '953230124', 'Mz A lote 28  Las Flores (Alto Trujillo)', 'Sector 1', 'P02', 'A3', '2026-05-08', 'Con deuda', NULL),
('125', 'Marcos Rojas', '44353061', '972555807', 'Mz U lote 2 Barrio 2-B(Alto Trujillo)', 'Sector 4', 'P02', 'A4', '2026-05-18', 'Con deuda', NULL),
('126', 'Monica Alvarado', '74910009', '928241443', 'Mz X lote 28 Ampliacion las Flores (Alto Trujillo)', 'Sector 1', 'P02', 'A2', '2026-05-07', 'Con deuda', NULL),
('127', 'Flor Urrutia', '77658800', '900867636', 'Mz F lote 14 Barrio 2-B (Alto Trujillo)', 'Sector 1', 'P02', 'A4', '2026-05-08', 'Con deuda', NULL),
('128', 'Teodoro', '71742290', '957392956', 'Mz G lote 5 Barrio 2-C Roberto del Solar (Alto Trujillo)', 'Sector 1', 'P02', 'A4', '2026-05-01', 'Con deuda', NULL),
('129', 'Jacinto Varela', '19548946', '970183296', 'Mz D lote 15 Barrio 2-B (Alto Trujillo)', 'Sector 4', 'P02', 'A4', '2026-05-22', 'Con deuda', NULL),
('130', 'Juli Rojas', '75022571', '902463405', 'Mz U lote 3 Roberto Solar(Alto Trujillo)', 'Sector 4', 'P02', 'A4', '2026-05-16', 'Con deuda', NULL),
('131', 'Arelis Gutierrez', '18205593', '940029717', 'Mz B lote 7 Barrio 2-A (Alto Trujillo)', 'Sector 1', 'P02', 'A4', '2026-05-01', 'Con deuda', NULL),
('132', 'Nathaly', '44720125', '973423424', 'Mz N lote 8 Barrio 2-B Villa Clementina, Las Margaritas(Alto Trujillo)', 'Sector 1', 'P02', 'A4', '2026-05-01', 'Con deuda', NULL),
('133', 'Cristian Castañeda', '71492343', '921309473', 'Mz R lote 20 Barrio 2-B (Alto Trujillo)', 'Sector 4', 'P02', 'A4', '2026-05-25', 'Con deuda', NULL),
('134', 'Mirian Barrios', '73197038', '913004877', 'Mz 10 lote 1 Barrio 2-B (Alto Trujillo)', 'Sector 1', 'P02', 'A4', '2026-05-04', 'Con deuda', NULL),
('135', 'Persy', '41093614', '920523424', 'Mz 12 lote 1 Barrio 2-B (Alto Trujillo)', 'Sector 1', 'P02', 'A4', '2026-05-30', 'Al día', NULL),
('136', 'Eliana Zavaleta', '76866114', '923817672', 'Mz R lote 12 Barrio 2-C (Alto Trujillo)', 'Sector 4', 'P02', 'A4', '2026-05-20', 'Con deuda', NULL),
('137', 'Lenin', '46993918', '944474424', 'Mz 15 lote 23 Barrio 1-B (Alto Trujillo)', 'Sector 4', 'P02', 'A4', '2026-05-24', 'Con deuda', NULL),
('138', 'Noemi Rodriguez', '42766565', '955549220', 'Mz N lote 9 Barrio 1-A Villa Clementina, Las Margaritas(Alto Trujillo)', 'Sector 4', 'P02', 'A4', '2026-05-17', 'Con deuda', NULL),
('139', 'Yaquelin Urquizo', '46584550', '972786520', 'Mz T lote 31  Norberto Solar(Alto Trujillo)', 'Sector 4', 'P02', 'A4', '2026-05-24', 'Con deuda', NULL),
('140', 'Jhonatan Mercedez', '45594057', '921801697', 'Mz D lote 3 Barrio 3-D (Alto Trujillo)', 'Sector 4', 'P02', 'A4', '2026-05-24', 'Con deuda', NULL),
('141', 'Irma Rojas', '76744445', '967543839', 'Mz U lote 6 Barrio 2-B (Alto Trujillo)', 'Sector 4', 'P02', 'A4', '2026-05-26', 'Con deuda', NULL),
('142', 'Germen Castillo', '17925258', '916218287', 'Mz LL lote 3 Barrio 1-b (Alto Trujillo)', 'Sector 1', 'P02', 'A3', '2026-05-01', 'Con deuda', NULL),
('143', 'Carmen Avila', '46686796', '929349334', 'Mz Y lote 29 Barrio 1-B (Alto Trujillo)', 'Sector 3', 'P02', 'A3', '2026-05-25', 'Con deuda', NULL),
('144', 'Nayla Fiorela', '43996949', '948279832', 'Mz A lote 36 Barrio 1-B (Alto Trujillo)', 'Sector 3', 'P02', 'A3', '2026-05-22', 'Con deuda', NULL),
('145', 'Yenifer Cernna', '47442949', '929774174', 'Mz A lote 5 Barrio 1-B (Alto Trujillo)', 'Sector 1', 'P02', 'A3', '2026-05-07', 'Con deuda', NULL),
('146', 'Francisco Ruiz', '62095854', '997333831', 'Mz U lote 13 Barrio 1-B (Alto Trujillo)', 'Sector 1', 'P02', 'A3', '2026-05-04', 'Con deuda', NULL),
('147', 'Dilson Castillo', '71697867', '973629880', 'Mz 12 lote 16 Barrio 2-B (Alto Trujillo)', 'Sector 4', 'P02', 'A4', '2026-05-22', 'Con deuda', NULL),
('148', 'Manuel Gutierrez', '18119898', '910874372', 'Mz Elote 26 Barrio 2-B (Alto Trujillo)', 'Sector 1', 'P02', 'A4', '2026-05-01', 'Con deuda', NULL),
('149', 'Diana Merejildo', '47989894', '907745235', 'Mz N lote 27 Barrio 2-A (Alto Trujillo)', 'Sector 4', 'P02', 'A4', '2026-05-20', 'Con deuda', NULL),
('150', 'Rosa Mily', '71797498', '977544976', 'Mz U lote 7 Barrio 1-B (Alto Trujillo)', 'Sector 1', 'P02', 'A2', '2026-05-02', 'Con deuda', NULL),
('151', 'Royber Juares', '71770863', '986497959', 'Mz S lote 8 Barrio 1-B (Alto Trujillo)', 'Sector 1', 'P02', 'A2', '2026-05-08', 'Con deuda', NULL),
('152', 'Pedro Sanchez', '47770591', '926139392', 'Mz D lote 30 Barrio 1-B (Alto Trujillo)', 'Sector 1', 'P02', 'A2', '2026-05-05', 'Con deuda', NULL),
('153', 'Antonio CHilon', '43136529', '949087691', 'Mz A lote 01 Nuevo Jerusalen la Esperanza', 'Sector 1', 'P02', 'A2', '2026-05-07', 'Con deuda', NULL),
('154', 'Kimberly Carranza', '74951923', '953425616', 'Mz B lote 20 Barrio 1-B las flores (Alto Trujillo)', 'Sector 1', 'P02', 'A2', '2026-06-11', 'Con deuda', NULL),
('155', 'Florencia', '18156212', '901489857', 'Mz J lote 10 Barrio 1-A (Alto Trujillo)', 'Sector 2', 'P02', 'A2', '2026-05-22', 'Con deuda', NULL),
('156', 'Carlos Calderon Verde', '41901703', '935061343', 'Mz J lote 6 Barrio 1-A (Alto Trujillo)', 'Sector 2', 'P02', 'A2', '2026-05-18', 'Con deuda', NULL),
('157', 'Santos Ramirez', '80345833', '946673938', 'Mz U lote 4b Barrio 2-B  (Alto Trujillo)', 'Sector 1', 'P02', 'A4', '2026-06-10', 'Con deuda', NULL),
('158', 'Luis Minchola', '18116136', '977928102', 'Mz B lote 30 Barrio 1 (Alto Trujillo)', 'Sector 1', 'P02', 'A1', '2026-06-10', 'Con deuda', NULL),
('159', 'Sarita Burgos', '47451278', '967238034', 'Mz P lote 30 Barrio 1-A (Alto Trujillo)', 'Sector 1', 'P02', 'A1', '2026-06-10', 'Con deuda', NULL),
('160', 'Sarita Burgos', '47451278', '967238034', 'Mz P lote 30 Barrio 1-A (Alto Trujillo)', 'Sector 1', 'P02', 'A1', '2026-06-10', 'Con deuda', NULL),
('161', 'Sarita Burgos', '47451278', '967238034', 'Mz P lote 30 Barrio 1-A (Alto Trujillo)', 'Sector 1', 'P02', 'A1', '2026-06-10', 'Con deuda', NULL),
('162', 'Yuly Cabrera', '45677382', '908547478', 'Mz F lote 04 Barrio 6-B (Alto Trujillo)', 'Sector 5', 'P02', 'A5', '2026-05-23', 'Con deuda', NULL),
('163', 'Eligia Otiniano', '44350492', '928195303', 'Mz G lote 03 Barrio 6-E (Alto Trujillo)', 'Sector 1', 'P02', 'A5', '2026-06-03', 'Con deuda', NULL),
('164', 'Elvis Burgos', '46020426', '922997355', 'Mz U lote 18 Barrio 6-D (Alto Trujillo)', 'Sector 1', 'P02', 'A5', '2026-06-04', 'Con deuda', NULL),
('165', 'Antonio Gimenes', '77137377', '910597472', 'Mz O lote 10 Nuevo Jerusalen  La Esperanza', 'Sector 2', 'P02', 'A2', '2026-05-15', 'Con deuda', NULL),
('166', 'Gaby Graus', '44907122', '996182422', 'Mz L lote 14 Barrio 6-E (Alto Trujillo)', 'Sector 5', 'P02', 'A5', '2026-05-26', 'Con deuda', NULL),
('167', 'Beny Cenas', '80430897', '921987718', 'Mz Y lote 7 Barrio 6-E (Alto Trujillo)', 'Sector 5', 'P02', 'A5', '2026-05-21', 'Con deuda', NULL),
('168', 'Leo Graus', '47572748', '972746694', 'Mz M lote 37 Barrio 6-E (Alto Trujillo)', 'Sector 1', 'P02', 'A5', '2026-05-28', 'Al día', NULL),
('169', 'Maria Olivares', '60495643', '900157231', 'Av los Tulipanes barrio  6-E (Alto Trujillo)', 'Sector 5', 'P02', 'A5', '2026-05-25', 'Con deuda', NULL),
('170', 'Ricardo Meza', '44550624', '901802232', 'Mz Y lote 42 Barrio 6-E (Alto Trujillo)', 'Sector 5', 'P02', 'A5', '2026-05-21', 'Con deuda', NULL),
('171', 'Talia Valtodano', '47027721', '923167306', 'Mz D lote 8 Barrio 6-E (Alto Trujillo)', 'Sector 5', 'P02', 'A5', '2026-05-13', 'Con deuda', NULL),
('172', 'Wilzon Sanches', '42661481', '956391599', 'Mz K lote 1 sector buen samaritano el Porvenir', 'Sector 1', 'P02', 'A5', '2026-05-07', 'Con deuda', NULL),
('173', 'Yino Timauri', '00292975', '929179343', 'Mz P lote 6 Barrio 6-E sector tulipanes (Alto Trujillo)', 'Sector 5', 'P02', 'A5', '2026-05-26', 'Con deuda', NULL),
('174', 'Yosmer Lasaro', '47367347', '935539419', 'Mz L lote 26 Barrio 6-E (Alto Trujillo)', 'Sector 5', 'P02', 'A5', '2026-05-20', 'Con deuda', NULL),
('175', 'Santiago Yulian', '77427018', '902362119', 'Mz S lote 33 Barrio 6-E los tulipanes (Alto Trujillo)', 'Sector 5', 'P02', 'A5', '2026-05-21', 'Con deuda', NULL),
('176', 'Paolo Sanchez', '76929278', '929846038', 'Mz G lote 38 -45 Barrio 6-E (Alto Trujillo)', 'Sector 5', 'P02', 'A5', '2026-05-25', 'Con deuda', NULL),
('177', 'Noller Reyes', '70085395', '989893802', 'Mz K lote 20 tupac amaro e etapa el porvenir', 'Sector 5', 'P02', 'A5', '2026-05-13', 'Con deuda', NULL),
('178', 'Jorge Villa', '42878957', '917041132', 'Mz M lote 7 Barrio 6-E los tulipanes (Alto Trujillo)', 'Sector 1', 'P02', 'A5', '2026-06-04', 'Con deuda', NULL),
('179', 'Roxana Orbegoso', '48874304', '910588789', 'Mz LL lote 49 Barrio 6-E (Alto Trujillo)', 'Sector 1', 'P02', 'A5', '2026-06-03', 'Con deuda', NULL),
('180', 'Cleider Tite Sanchez', '48593133', '993341405', 'Mz Y  lote 48 Barrio 6-E (Alto Trujillo)', 'Sector 5', 'P02', 'A5', '2026-05-25', 'Con deuda', NULL),
('181', 'Deysi Juares', '47393880', '908593069', 'Mz W lote 22 Barrio 6-E (Alto Trujillo)', 'Sector 5', 'P02', 'A5', '2026-05-20', 'Con deuda', NULL),
('182', 'Evelyn Villarreal', '74300508', '982690137', 'Mz LL lote 1 Barrio 1-A (Alto Trujillo)', 'Sector 1', 'P02', 'A1', '2026-06-05', 'Con deuda', NULL),
('183', 'Angy Inoñan', '76984944', '916556182', 'Nuevo Jerusalen', 'Sector 1', 'P02', 'A1', '2026-06-07', 'Con deuda', NULL),
('184', 'Ericka Lavado', '47745089', '969385752', 'Mz K lote 11 calle 22 de febrero Florencia de Mora', 'Sector 1', 'P02', 'A1', '2026-06-01', 'Con deuda', NULL),
('185', 'Segunda Polo', '19701426', '913215671', 'Mz U1 lote 01 Barrio 1-B Ampliacion las flores (Alto Trujillo)', 'Sector 1', 'P02', 'A2', '2026-06-05', 'Con deuda', NULL),
('186', 'Yery Paola', '17940186', '921257251', 'Mz A2 lote 10 Barrio 1-A (Alto Trujillo)', 'Sector 1', 'P02', 'A1', '2026-06-13', 'Con deuda', NULL),
('187', 'Any Rubio', '60798997', '902410856', 'Mz 02 lote 03 Barrio 1-A (Alto Trujillo)', 'Sector 1', 'P02', 'A1', '2026-06-13', 'Con deuda', NULL);
GO





