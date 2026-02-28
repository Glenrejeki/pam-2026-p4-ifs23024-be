package org.delcom.data

import kotlinx.serialization.Serializable
import org.delcom.entities.CelestialBody

@Serializable
data class CelestialBodyRequest(
    var nama: String = "",
    var deskripsi: String = "",
    var manfaat: String = "",
    var faktaMenarik: String = "",
    var pathGambar: String = "",
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "nama" to nama,
            "deskripsi" to deskripsi,
            "manfaat" to manfaat,
            "faktaMenarik" to faktaMenarik,
            "pathGambar" to pathGambar
        )
    }

    fun toEntity(): CelestialBody {
        return CelestialBody(
            nama = nama,
            deskripsi = deskripsi,
            manfaat = manfaat,
            faktaMenarik = faktaMenarik,
            pathGambar = pathGambar,
        )
    }
}