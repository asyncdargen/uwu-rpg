package ru.dargen.rpg.util

import ru.dargen.accessors.Accessors
import ru.starfarm.core.database.query.DatabaseRowType
import java.sql.ResultSet

fun injectMySQLFix() {
    Accessors.invokeFieldAccessor<String>(DatabaseRowType::class.java, "format")
        .setValue(DatabaseRowType.VAR_CHAR, "VARCHAR(255)")
}

fun ResultSet.getBool(column: String) = getObject(column) == true || getObject(column) == 1

fun ResultSet.forEach(block: ResultSet.() -> Unit) = use {
    while (next()) block(this)
}

fun <T> ResultSet.getPair(column: String, mapper: (String) -> T) = getString(column)
    ?.split('-', limit = 2)
    ?.map(mapper)
    ?.let { it[0] to it[1] }

fun ResultSet.getItemStack(column: String) = getString(column).asItemStack

fun ResultSet.getMap(
    column: String,
    entriesMapper: (String) -> List<String> = String::lines,
    entryResolver: (String) -> Pair<String, String> = { it.split(':', limit = 2).map(String::trim).let { it[0].lowercase() to it[1] } }
) = getString(column)?.let(entriesMapper)?.associate(entryResolver) ?: hashMapOf()
