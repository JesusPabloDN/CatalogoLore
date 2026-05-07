-- Sistema Gestor de Catálogos Digitales
-- Script DDL - Base de Datos Local SQLite
-- Autor: Jesús Pablo Damián Nava
-- Materia: Ingeniería de Software - 6° Semestre S6A
-- Institución: Instituto Tecnológico de Chilpancingo
--
-- Notas técnicas SQLite:
--   · No existe tipo BOOLEAN nativo → se usa INTEGER (0=false, 1=true)
--   · No existe AUTO_INCREMENT     → se usa AUTOINCREMENT en INTEGER PRIMARY KEY
--   · Las llaves compuestas se declaran a nivel de tabla, no de columna
--   · PRAGMA foreign_keys debe activarse en cada conexión
--   · Todos los CREATE usan IF NOT EXISTS para ejecución idempotente


-- Activa la verificación de llaves foráneas; SQLite la desactiva por defecto.
-- Debe ejecutarse en cada nueva conexión para garantizar integridad referencial.
PRAGMA foreign_keys = ON;


-- Almacena los datos generales del negocio que aparecerán en la portada del PDF.
-- Siempre contendrá un único registro (id_datos = 1).
CREATE TABLE IF NOT EXISTS DATOS_CATALOGO (
    id_datos          INTEGER PRIMARY KEY,
    nombre_negocio    TEXT    NOT NULL,
    descripcion       TEXT,
    telefono_contacto TEXT
);

-- Semilla inicial: inserta el registro de configuración solo si aún no existe.
INSERT OR IGNORE INTO DATOS_CATALOGO (id_datos, nombre_negocio, descripcion, telefono_contacto)
VALUES (1, 'Mi Negocio', '', '');


-- Clasifica los productos del catálogo por tipo (ej. Belleza, Perfumería).
-- El nombre es único y no debe superar 30 caracteres (validación en Java).
CREATE TABLE IF NOT EXISTS CATEGORIA (
    id_categoria INTEGER PRIMARY KEY AUTOINCREMENT,
    fk_datos     INTEGER NOT NULL,
    nombre       TEXT    NOT NULL UNIQUE,
    FOREIGN KEY (fk_datos) REFERENCES DATOS_CATALOGO(id_datos)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
);


-- Registra los productos artesanales del catálogo.
-- · disponibilidad: controla si el producto aparece en el PDF (1=visible, 0=oculto).
-- · stock_terminado: unidades listas para venta, parte en cero.
-- · ruta_imagen: ruta absoluta a la imagen local del producto.
CREATE TABLE IF NOT EXISTS PRODUCTO (
    id_producto     INTEGER PRIMARY KEY AUTOINCREMENT,
    fk_categoria    INTEGER NOT NULL,
    nombre          TEXT    NOT NULL,
    beneficios      TEXT,
    precio_actual   REAL    NOT NULL CHECK(precio_actual >= 0),
    ruta_imagen     TEXT,
    disponibilidad  INTEGER NOT NULL DEFAULT 1 CHECK(disponibilidad IN (0, 1)),
    stock_terminado INTEGER NOT NULL DEFAULT 0 CHECK(stock_terminado >= 0),
    FOREIGN KEY (fk_categoria) REFERENCES CATEGORIA(id_categoria)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
);


-- Controla la materia prima disponible en inventario.
-- stock_actual admite decimales para manejar medidas como ml o gr.
CREATE TABLE IF NOT EXISTS INSUMO (
    id_insumo     INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre        TEXT    NOT NULL UNIQUE,
    stock_actual  REAL    NOT NULL DEFAULT 0 CHECK(stock_actual >= 0),
    unidad_medida TEXT    NOT NULL  -- ej. "ml", "gr", "piezas", "litros"
);


-- Relación N:M entre PRODUCTO e INSUMO.
-- Define qué insumos y en qué cantidad se requieren para fabricar una unidad del producto.
-- La llave primaria es compuesta: (fk_producto, fk_insumo).
CREATE TABLE IF NOT EXISTS PRODUCTO_INSUMO (
    fk_producto        INTEGER NOT NULL,
    fk_insumo          INTEGER NOT NULL,
    cantidad_necesaria REAL    NOT NULL CHECK(cantidad_necesaria > 0),
    PRIMARY KEY (fk_producto, fk_insumo),
    FOREIGN KEY (fk_producto) REFERENCES PRODUCTO(id_producto)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    FOREIGN KEY (fk_insumo) REFERENCES INSUMO(id_insumo)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);


-- Registro de clientes frecuentes del negocio.
-- El teléfono debe tener exactamente 10 dígitos (validación en Java).
CREATE TABLE IF NOT EXISTS CLIENTE (
    id_cliente        INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre_completo   TEXT    NOT NULL,
    telefono          TEXT    NOT NULL,
    direccion_entrega TEXT
);


-- Registro de órdenes de venta.
-- · fecha_pedido se almacena como texto en formato ISO 8601 (YYYY-MM-DD).
-- · estatus solo admite 'Pendiente' o 'Entregado'.
CREATE TABLE IF NOT EXISTS PEDIDO (
    id_pedido    INTEGER PRIMARY KEY AUTOINCREMENT,
    fk_cliente   INTEGER NOT NULL,
    fecha_pedido TEXT    NOT NULL,
    estatus      TEXT    NOT NULL DEFAULT 'Pendiente'
                         CHECK(estatus IN ('Pendiente', 'Entregado')),
    FOREIGN KEY (fk_cliente) REFERENCES CLIENTE(id_cliente)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
);


-- Relación N:M entre PEDIDO y PRODUCTO; detalla qué productos lleva cada orden.
-- · precio_unitario se captura al crear el pedido para preservar el precio histórico;
--   no se ve afectado si el precio del producto cambia después.
-- La llave primaria es compuesta: (fk_pedido, fk_producto).
CREATE TABLE IF NOT EXISTS DETALLE_PEDIDO (
    fk_pedido       INTEGER NOT NULL,
    fk_producto     INTEGER NOT NULL,
    cantidad        INTEGER NOT NULL CHECK(cantidad > 0),
    precio_unitario REAL    NOT NULL CHECK(precio_unitario >= 0),
    PRIMARY KEY (fk_pedido, fk_producto),
    FOREIGN KEY (fk_pedido) REFERENCES PEDIDO(id_pedido)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    FOREIGN KEY (fk_producto) REFERENCES PRODUCTO(id_producto)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
);


-- Índices para acelerar las consultas más frecuentes de la aplicación.

-- Agiliza el listado de productos agrupados por categoría (vista del catálogo).
CREATE INDEX IF NOT EXISTS idx_producto_categoria
    ON PRODUCTO(fk_categoria);

-- Agiliza el filtrado de productos disponibles al generar el PDF.
CREATE INDEX IF NOT EXISTS idx_producto_disponibilidad
    ON PRODUCTO(disponibilidad);

-- Agiliza la búsqueda del historial de pedidos de un cliente.
CREATE INDEX IF NOT EXISTS idx_pedido_cliente
    ON PEDIDO(fk_cliente);

-- Agiliza la separación entre pedidos pendientes y entregados.
CREATE INDEX IF NOT EXISTS idx_pedido_estatus
    ON PEDIDO(estatus);


-- Fin del script DDL.
-- Idempotente: puede ejecutarse varias veces sin alterar datos existentes.
