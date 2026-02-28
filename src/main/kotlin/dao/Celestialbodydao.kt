package org.delcom.dao

import org.delcom.tables.CelestialBodyTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import java.util.UUID

class CelestialBodyDAO(id: EntityID<UUID>) : Entity<UUID>(id) {
    companion object : EntityClass<UUID, CelestialBodyDAO>(CelestialBodyTable)

    var nama by CelestialBodyTable.nama
    var pathGambar by CelestialBodyTable.pathGambar
    var deskripsi by CelestialBodyTable.deskripsi
    var manfaat by CelestialBodyTable.manfaat
    var faktaMenarik by CelestialBodyTable.faktaMenarik
    var createdAt by CelestialBodyTable.createdAt
    var updatedAt by CelestialBodyTable.updatedAt
}