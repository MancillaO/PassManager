use <pass_manager>

db.createCollection("passwords");
db.createCollection("counters");
db.counters.insertOne({ _id: "passwords", sequence_value: 0 });

// Crear usuario seguro
db.getSiblingDB("admin").createUser({
    user: "manager",
    pwd: "123",
    roles: [ { 
        role: "readWrite", 
        db: "pass_manager" 
    }],
    mechanisms: ["SCRAM-SHA-256"]
});

// Ejemplo de documento:
// {
//     "service": "Netflix",
//     "username": "usuario@ejemplo.com",
//     "encrypted_password": "aGVsbG8gd29ybGQh",
//     "creation_date": ISODate("2024-01-01T00:00:00Z")
// }