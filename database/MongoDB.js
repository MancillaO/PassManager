use <pass_manager>

db.createCollection("passwords");

db.createCollection("counters");
db.counters.insertOne({ _id: "passwords", sequence_value: 0 });

// Ejemplo de documento:
// {
//     "service": "Netflix",
//     "username": "usuario@ejemplo.com",
//     "encrypted_password": "aGVsbG8gd29ybGQh", // Debe ser cifrado en tu aplicación
//     "creation_date": ISODate("2024-01-01T00:00:00Z"),
// }