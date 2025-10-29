# 📊 Plan: Sistema de Item Details con Enchant y Special Abilities

## 🎯 **Objetivo:**

Crear un endpoint que muestre detalles completos de un item específico (instancia), incluyendo:
- ✅ Stats base del template
- ✅ Bonus por enchant (+0, +22, etc.)
- ✅ Special abilities (augments, runas, ensoul)

---

## 📦 **Estructura de Datos:**

### **1. Entidad Inmutable: ItemSpecialAbility**

```java
@Entity
@Table(name = "item_special_abilities")
@Immutable  // Solo lectura
public class ItemSpecialAbility {
    @Id
    @Column(name = "objectId")
    private int objectId;
    
    @Column(name = "type")
    private int type;  // 1=Augment, 2=Ensoul, 3=Rune, etc.
    
    @Column(name = "optionId")
    private int optionId;
    
    @Column(name = "position")
    private int position;
    
    // Solo getters, sin setters
}
```

### **2. Repository (Solo lectura)**

```java
@Repository
public interface ItemSpecialAbilityRepository extends JpaRepository<ItemSpecialAbility, Integer> {
    List<ItemSpecialAbility> findByObjectId(int objectId);
}
```

### **3. Lógica de Enchant (del core)**

Copiar funciones simplificadas:

```java
// En game/l2j/util/EnchantCalculator.java
public class EnchantCalculator {
    
    // Bonus de P.Atk por enchant (armas)
    public static int calcPAtkBonus(String grade, int enchantLevel) {
        // D-grade: +2 por nivel hasta +3, +3 después
        // C-grade: +3 por nivel hasta +3, +4 después
        // Etc...
    }
    
    // Bonus de P.Def por enchant (armaduras)
    public static int calcPDefBonus(String bodyPart, int enchantLevel) {
        // Chest/Full armor: mayor bonus
        // Helmet/Gloves/Boots: menor bonus
    }
}
```

### **4. Endpoint:**

```java
GET /api/game/items/{objectId}/details

Response:
{
  "objectId": 268501010,
  "itemId": 69,
  "name": "Bastard Sword",
  "enchantLevel": 22,
  "type": "Weapon",
  "grade": "D",
  
  // Stats base del template
  "baseStats": {
    "pAtk": 51,
    "mAtk": 32,
    "weight": 1510
  },
  
  // Bonus por enchant
  "enchantBonus": {
    "pAtk": 66,  // +3 por nivel desde +4
    "description": "+22: +66 P.Atk"
  },
  
  // Stats totales
  "totalStats": {
    "pAtk": 117  // 51 + 66
  },
  
  // Habilidades especiales (de la BD)
  "specialAbilities": [
    {
      "type": 1,
      "typeName": "Augment",
      "optionId": 13656,
      "name": "Empower +4%",
      "description": "Increases skill power by 4%"
    }
  ]
}
```

---

## 🏗️ **Archivos a Crear:**

### **Backend (API):**

```
game/
├── entities/
│   └── ItemSpecialAbility.java        ← Entidad inmutable
├── repositories/
│   └── ItemSpecialAbilityRepository.java
├── l2j/
│   ├── data/
│   │   └── OptionsData.java           ← Carga options.xml (opcional)
│   └── util/
│       └── EnchantCalculator.java     ← Lógica de enchant
├── dto/
│   └── ItemDetailsDTO.java            ← Response completo
├── services/
│   └── ItemDetailsService.java        ← Lógica de negocio
└── controllers/
    └── ItemDetailsController.java     ← Endpoint REST
```

---

## 📋 **Pasos de Implementación:**

### **Paso 1:** Crear entidad ItemSpecialAbility (inmutable)
### **Paso 2:** Crear repository (solo lectura)
### **Paso 3:** Crear EnchantCalculator (lógica simplificada del core)
### **Paso 4:** Crear ItemDetailsDTO
### **Paso 5:** Crear ItemDetailsService
### **Paso 6:** Crear ItemDetailsController con endpoint
### **Paso 7:** (Opcional) Cargar nombres de options desde XML

---

## 🔧 **Lógica de Enchant Simplificada:**

### **Armas:**

| Grade | Enchant Level | Bonus P.Atk |
|-------|---------------|-------------|
| D     | +1 a +3       | +2 por nivel |
| D     | +4+           | +3 por nivel |
| C     | +1 a +3       | +3 por nivel |
| C     | +4+           | +4 por nivel |
| B     | +1 a +3       | +4 por nivel |
| B     | +4+           | +6 por nivel |
| A/S   | +1 a +3       | +5/+6 por nivel |
| A/S   | +4+           | +8/+10 por nivel |

### **Armaduras (P.Def):**

| Body Part | Bonus por nivel |
|-----------|-----------------|
| Chest/Full | +1 |
| Legs | +1 |
| Helmet | +0.5 |
| Gloves/Boots | +0.5 |

---

## ❓ **Decisiones:**

1. **¿Cargar nombres de options desde XML?**
   - SÍ: Más completo, muestra "Empower +4%" en vez de "Option 13656"
   - NO: Solo mostrar optionId, frontend puede tener tabla hardcoded

2. **¿Endpoint público o autenticado?**
   - Público: Cualquiera puede ver stats de items
   - Autenticado: Solo el dueño puede ver detalles

3. **¿Simplificar cálculo de enchant?**
   - SÍ: Fórmula aproximada, más simple
   - NO: Copiar código exacto del core (más complejo)

---

## 🎯 **Recomendación:**

**Empezar simple e iterar:**

1. **Primera versión:** Stats base + enchant bonus (sin special abilities)
2. **Segunda versión:** Agregar special abilities con optionId
3. **Tercera versión:** Agregar nombres de abilities (desde XML o hardcoded)

---

**¿Te parece bien este plan?**
**¿Con qué versión empezamos?**

