use <pass_manager>

// Crear colección (no necesario en MongoDB, pero opcional)
db.createCollection("passwords");

// Ejemplo de documento:
// {
//     "service": "Netflix",
//     "username": "usuario@ejemplo.com",
//     "encrypted_password": "aGVsbG8gd29ybGQh", // Debe ser cifrado en tu aplicación
//     "creation_date": ISODate("2024-01-01T00:00:00Z"),
// }