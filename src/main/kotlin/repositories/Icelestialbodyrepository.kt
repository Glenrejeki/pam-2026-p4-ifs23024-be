package org.delcom.repositories

import org.delcom.entities.CelestialBody

interface ICelestialBodyRepository {
    suspend fun getCelestialBodies(search: String): List<CelestialBody>
    suspend fun getCelestialBodyById(id: String): CelestialBody?
    suspend fun getCelestialBodyByName(name: String): CelestialBody?
    suspend fun addCelestialBody(celestialBody: CelestialBody): String
    suspend fun updateCelestialBody(id: String, newCelestialBody: CelestialBody): Boolean
    suspend fun removeCelestialBody(id: String): Boolean
}