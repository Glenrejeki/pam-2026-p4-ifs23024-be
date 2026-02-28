package org.delcom.helpers

import kotlinx.coroutines.Dispatchers
import org.delcom.dao.CelestialBodyDAO
import org.delcom.entities.CelestialBody
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

suspend fun <T> suspendTransaction(block: Transaction.() -> T): T =
    newSuspendedTransaction(Dispatchers.IO, statement = block)

fun daoToModel(dao: CelestialBodyDAO) = CelestialBody(
    dao.id.value.toString(),
    dao.nama,
    dao.pathGambar,
    dao.deskripsi,
    dao.manfaat,
    dao.faktaMenarik,
    dao.createdAt,
    dao.updatedAt
)