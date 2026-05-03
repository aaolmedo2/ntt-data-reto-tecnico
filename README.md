# Reto Técnico NTT DATA

Breve guía rápida para levantar y probar la solución con Docker.

## NOTA

La mayoria de campos esta basado en Ecuador, ejemplo: los numeros telefonicos son de 10 digitos, la identificacion de igual manera son de 10 digitos, el numero de cuenta son de 10 digitos, espero y sea intuitivo el uso.

- Reglas de negocio para consulta de movimientos y reporte de no más de 30 días.

- Reporte en Excel

## Autor

- Nombre: ANGELO OLMEDO CAMACHO - olmedo.angelo68@gmail.com

## Arranque rápido

1. Crear un fichero `.env` en la raíz del proyecto con estas variables (ejemplo):

```
POSTGRES_USER=postgres
POSTGRES_PASSWORD=postgres
```

2. Levantar toda la solución (compila imágenes y arranca contenedores):

```
docker-compose up --build
```

3. Servicios disponibles (por defecto):

- Customer service: http://localhost:8081
- Account service: http://localhost:8082

Bases de datos:

- customer-db: puerto `5433`
- account-db: puerto `5434`

Para detener y eliminar contenedores y redes creadas:

```
docker-compose down
```

## Base de datos

Si quieres poblar la base de datos localmente, puedes ejecutar `BaseDatos.sql`

## Colección Postman

https://www.postman.com/ad7777-2734/workspace/ntt-data

## Notas útiles

- Variables de entorno: `POSTGRES_USER` y `POSTGRES_PASSWORD` se pasan a los contenedores desde el `.env`.
- Perfiles Spring: las imágenes activan `SPRING_PROFILES_ACTIVE=prod` por defecto; modifica si es necesario.
- Ver logs en tiempo real:

```
docker-compose logs -f account-service
```

- Para reconstruir solo un servicio:

```
docker-compose build account-service
docker-compose up -d account-service
```
