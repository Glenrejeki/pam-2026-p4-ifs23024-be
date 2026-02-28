package org.delcom.repositories

import org.delcom.dao.CelestialBodyDAO
import org.delcom.entities.CelestialBody
import org.delcom.helpers.daoToModel
import org.delcom.helpers.suspendTransaction
import org.delcom.tables.CelestialBodyTable
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.lowerCase
import java.util.UUID

class CelestialBodyRepository : ICelestialBodyRepository {
    override suspend fun getCelestialBodies(search: String): List<CelestialBody> = suspendTransaction {
        if (search.isBlank()) {
            CelestialBodyDAO.all()
                .orderBy(CelestialBodyTable.createdAt to SortOrder.DESC)
                .limit(20)
                .map(::daoToModel)
        } else {
            val keyword = "%${search.lowercase()}%"
            CelestialBodyDAO
                .find {
                    CelestialBodyTable.nama.lowerCase() like keyword
                }
                .orderBy(CelestialBodyTable.nama to SortOrder.ASC)
                .limit(20)
                .map(::daoToModel)
        }
    }

    override suspend fun getCelestialBodyById(id: String): CelestialBody? = suspendTransaction {
        CelestialBodyDAO
            .find { (CelestialBodyTable.id eq UUID.fromString(id)) }
            .limit(1)
            .map(::daoToModel)
            .firstOrNull()
    }

    override suspend fun getCelestialBodyByName(name: String): CelestialBody? = suspendTransaction {
        CelestialBodyDAO
            .find { (CelestialBodyTable.nama eq name) }
            .limit(1)
            .map(::daoToModel)
            .firstOrNull()
    }

    override suspend fun addCelestialBody(celestialBody: CelestialBody): String = suspendTransaction {
        val dao = CelestialBodyDAO.new {
            nama = celestialBody.nama
            pathGambar = celestialBody.pathGambar
            deskripsi = celestialBody.deskripsi
            manfaat = celestialBody.manfaat
            faktaMenarik = celestialBody.faktaMenarik
            createdAt = celestialBody.createdAt
            updatedAt = celestialBody.updatedAt
        }
        dao.id.value.toString()
    }

    override suspend fun updateCelestialBody(id: String, newCelestialBody: CelestialBody): Boolean = suspendTransaction {
        val dao = CelestialBodyDAO
            .find { CelestialBodyTable.id eq UUID.fromString(id) }
            .limit(1)
            .firstOrNull()

        if (dao != null) {
            dao.nama = newCelestialBody.nama
            dao.pathGambar = newCelestialBody.pathGambar
            dao.deskripsi = newCelestialBody.deskripsi
            dao.manfaat = newCelestialBody.manfaat
            dao.faktaMenarik = newCelestialBody.faktaMenarik
            dao.updatedAt = newCelestialBody.updatedAt
            true
        } else {
            false
        }
    }

    override suspend fun removeCelestialBody(id: String): Boolean = suspendTransaction {
        val rowsDeleted = CelestialBodyTable.deleteWhere {
            CelestialBodyTable.id eq UUID.fromString(id)
        }
        rowsDeleted == 1
    }
}