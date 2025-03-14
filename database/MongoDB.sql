use pass_manager

db.createCollection("passwords");
db.createCollection("counters");
db.counters.insertOne({ _id: "passwords", sequence_value: 0 });

db.createUser({user: "user", pwd: "123",
   roles: [{role: "readWrite", db: "pass_manager"}]
})

/*
PASOS
1. mongod.cfg:
   Modificar linea bindIp = 127.0.0.1 a bindIp = 0.0.0.0

2. Habilitar autenticación
   Añadir: 
   security:
      authorization: enabled 
 */