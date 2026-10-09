INSERT INTO productos (codigo, nombre, categoria, precio, existencia) VALUES ('S001', 'Sándwich de pollo', 'SANDWICH', 25.00, 10);
INSERT INTO productos (codigo, nombre, categoria, precio, existencia) VALUES ('S002', 'Sándwich de jamón', 'SANDWICH', 22.00, 20);
INSERT INTO productos (codigo, nombre, categoria, precio, existencia) VALUES ('S003', 'Sándwich de pavo', 'SANDWICH', 27.00, 4);
INSERT INTO productos (codigo, nombre, categoria, precio, existencia) VALUES ('S004', 'Sándwich vegetariano', 'SANDWICH', 20.00, 0);
INSERT INTO productos (codigo, nombre, categoria, precio, existencia) VALUES ('B001', 'Gaseosa', 'BEBIDA', 8.00, 20);
INSERT INTO productos (codigo, nombre, categoria, precio, existencia) VALUES ('B002', 'Jugo de naranja', 'BEBIDA', 10.00, 15);
INSERT INTO productos (codigo, nombre, categoria, precio, existencia) VALUES ('B003', 'Agua pura', 'BEBIDA', 6.00, 5);
INSERT INTO productos (codigo, nombre, categoria, precio, existencia) VALUES ('A001', 'Papas', 'ACOMPANAMIENTO', 10.00, 15);
INSERT INTO productos (codigo, nombre, categoria, precio, existencia) VALUES ('A002', 'Aros de cebolla', 'ACOMPANAMIENTO', 12.00, 12);
INSERT INTO productos (codigo, nombre, categoria, precio, existencia) VALUES ('A003', 'Ensalada', 'ACOMPANAMIENTO', 9.00, 3);

INSERT INTO movimientos_inventario (id_producto, tipo, cantidad, existencia_anterior, existencia_nueva, referencia)
SELECT id_producto, 'ABASTECIMIENTO', existencia, 0, existencia, 'INVENTARIO INICIAL'
FROM productos
WHERE existencia > 0;

INSERT INTO menus (codigo, nombre, precio, id_sandwich, id_bebida, id_acompanamiento)
VALUES ('M001', 'Menú completo de pollo', 37.00,
        (SELECT id_producto FROM productos WHERE codigo = 'S001'),
        (SELECT id_producto FROM productos WHERE codigo = 'B001'),
        (SELECT id_producto FROM productos WHERE codigo = 'A001'));
INSERT INTO menus (codigo, nombre, precio, id_sandwich, id_bebida, id_acompanamiento)
VALUES ('M002', 'Menú completo de jamón', 34.00,
        (SELECT id_producto FROM productos WHERE codigo = 'S002'),
        (SELECT id_producto FROM productos WHERE codigo = 'B001'),
        (SELECT id_producto FROM productos WHERE codigo = 'A001'));
INSERT INTO menus (codigo, nombre, precio, id_sandwich, id_bebida, id_acompanamiento)
VALUES ('M003', 'Menú completo de pavo', 40.00,
        (SELECT id_producto FROM productos WHERE codigo = 'S003'),
        (SELECT id_producto FROM productos WHERE codigo = 'B002'),
        (SELECT id_producto FROM productos WHERE codigo = 'A002'));

INSERT INTO recompensas (nombre, puntos_necesarios, tipo, id_producto)
VALUES ('Bebida gratuita', 10, 'PRODUCTO', (SELECT id_producto FROM productos WHERE codigo = 'B001'));
INSERT INTO recompensas (nombre, puntos_necesarios, tipo, id_producto)
VALUES ('Acompañamiento gratuito', 15, 'PRODUCTO', (SELECT id_producto FROM productos WHERE codigo = 'A001'));
INSERT INTO recompensas (nombre, puntos_necesarios, tipo, id_producto)
VALUES ('Sándwich individual gratuito', 30, 'PRODUCTO', (SELECT id_producto FROM productos WHERE codigo = 'S001'));
INSERT INTO recompensas (nombre, puntos_necesarios, tipo, id_menu)
VALUES ('Menú completo gratuito', 50, 'MENU', (SELECT id_menu FROM menus WHERE codigo = 'M001'));

INSERT INTO clientes (tipo_identificacion, identificacion, nombre, telefono, correo)
VALUES ('NIT', '1234567-8', 'Juan Pérez', '55551234', 'juan.perez@correo.com');
INSERT INTO clientes (tipo_identificacion, identificacion, nombre, telefono, correo)
VALUES ('DPI', '2547896541201', 'María López', '55559876', 'maria.lopez@correo.com');
INSERT INTO clientes (tipo_identificacion, identificacion, nombre, telefono, correo)
VALUES ('NIT', '9876543-2', 'Carlos Gómez', '44443210', NULL);
INSERT INTO clientes (tipo_identificacion, identificacion, nombre, telefono, correo)
VALUES ('DPI', '3012456780101', 'Ana Martínez', NULL, 'ana.martinez@correo.com');
INSERT INTO clientes (tipo_identificacion, identificacion, nombre, telefono, correo, activo)
VALUES ('NIT', '5551112-3', 'Pedro Ramírez', '33334444', NULL, 'N');

COMMIT;
