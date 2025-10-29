# 🧪 Credenciales de Testing - Terra API

## 🔑 **Cuenta de Prueba**

```
Email: lineageiiaklas@gmail.com
Password: Holapton2!
```

---

## 🚀 **Pasos para Testing**

### **1. Iniciar API**
```bash
cd C:\terra-api\terra-api
mvn spring-boot:run
```

### **2. Login y obtener JWT**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "lineageiiaklas@gmail.com",
    "password": "Holapton2!"
  }'
```

**Respuesta esperada:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "email": "lineageiiaklas@gmail.com"
}
```

**⚠️ GUARDA EL TOKEN** para usarlo en las siguientes requests.

---

## 📦 **Testing de Items (Sin Autenticación)**

### **Obtener item por ID - Bastard Sword (ID: 69)**
```bash
curl http://localhost:8080/api/game/catalog/items/69
```

### **Buscar items por nombre**
```bash
curl "http://localhost:8080/api/game/catalog/items/search?name=sword"
```

### **Filtrar armas**
```bash
curl "http://localhost:8080/api/game/catalog/items/filter/type?type=Weapon"
```

### **Estadísticas del catálogo**
```bash
curl http://localhost:8080/api/game/catalog/items/stats
```

---

## 👤 **Testing de Personajes (Con Autenticación)**

### **Obtener personajes por email**
```bash
curl http://localhost:8080/api/game/characters/by-email?email=lineageiiaklas@gmail.com \
  -H "Authorization: Bearer TU_TOKEN_AQUI"
```

---

## 🎒 **Testing de Inventario (Con Autenticación)**

### **Obtener inventario de un personaje**

Primero necesitas el `charId` del personaje (obtenlo del endpoint anterior).

```bash
curl -X POST http://localhost:8080/api/game/storage/inventory \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer TU_TOKEN_AQUI" \
  -d '{
    "playerId": 268435459
  }'
```

---

## 🔄 **Recargar Catálogo (Admin)**

```bash
curl -X POST http://localhost:8080/api/game/catalog/items/admin/reload \
  -H "Authorization: Bearer TU_TOKEN_AQUI"
```

---

## 📊 **Verificar Logs**

Al iniciar la API, deberías ver:

```
🔄 Iniciando carga de items desde XMLs...
📂 Ruta configurada: C:/terra-api/terra-api/src/main/resources/static/items
📁 Encontrados 226+ archivos XML para procesar
✅ Archivo 00000-00099.xml procesado: 100 items
...
🎉 Carga de items completada:
   📊 Total items cargados: 15234
   🔢 ID más alto: 92399
   💾 Items en memoria: 15234
```

---

## 🐛 **Si algo falla**

### **Error 401 Unauthorized**
El token JWT expiró o es inválido. Vuelve a hacer login.

### **Item no encontrado**
Verifica que el ID del item exista en los XMLs de `src/main/resources/static/items/`

### **No se cargan items**
Verifica la ruta en `application.properties`:
```properties
l2j.items.path=C:/terra-api/terra-api/src/main/resources/static/items
```

---

## ✅ **Testing Checklist**

- [ ] API inicia correctamente
- [ ] Logs muestran carga de items exitosa
- [ ] `GET /api/game/catalog/items/69` retorna Bastard Sword
- [ ] `GET /api/game/catalog/items/stats` muestra estadísticas
- [ ] Login funciona y retorna JWT
- [ ] Obtener personajes funciona con JWT
- [ ] Inventario muestra items con nombre correcto (desde ItemTable)
- [ ] Recargar catálogo funciona

---

**Fecha:** 2025
**Puerto:** 8080
**Base URL:** http://localhost:8080

