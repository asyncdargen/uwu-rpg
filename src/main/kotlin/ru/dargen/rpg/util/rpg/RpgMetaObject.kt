package ru.dargen.rpg.util.rpg

interface RpgMetaObject {

    val metadata: MutableMap<String, Any?>

    fun hasMetaValue(key: String) = key.lowercase() in metadata

    fun getMetaValue(key: String) = metadata[key.lowercase()]

}

inline fun <reified T> RpgMetaObject.getMetaValue(key: String, mapper: String.() -> T): T? {
    val value = getMetaValue(key)
    return if (value == null || value is T) value as T?
    else mapper(value as String).apply { metadata[key] = this }
}