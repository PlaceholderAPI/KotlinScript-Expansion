# KotlinScript-Expansion
Expansion for running Kotlin scripts for placeholders


#### Example Script
```kotlin
// player.kts

when (params.joinToString("").toLowerCase()) {
  "name" -> player.name
  "uuid" -> player.uniqueId
  else -> ""
}
```
