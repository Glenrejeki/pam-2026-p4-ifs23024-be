package org.delcom.services

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import org.delcom.data.AppException
import org.delcom.data.CelestialBodyRequest
import org.delcom.data.DataResponse
import org.delcom.helpers.ValidatorHelper
import org.delcom.repositories.ICelestialBodyRepository
import java.io.File
import java.util.*

class CelestialBodyService(private val celestialBodyRepository: ICelestialBodyRepository) {

    // Mengambil semua data benda langit
    suspend fun getAllCelestialBodies(call: ApplicationCall) {
        val search = call.request.queryParameters["search"] ?: ""

        val celestialBodies = celestialBodyRepository.getCelestialBodies(search)

        val response = DataResponse(
            "success",
            "Berhasil mengambil daftar benda langit",
            mapOf(Pair("celestialBodies", celestialBodies))
        )
        call.respond(response)
    }

    // Mengambil data benda langit berdasarkan id
    suspend fun getCelestialBodyById(call: ApplicationCall) {
        val id = call.parameters["id"]
            ?: throw AppException(400, "ID benda langit tidak boleh kosong!")

        val celestialBody = celestialBodyRepository.getCelestialBodyById(id)
            ?: throw AppException(404, "Data benda langit tidak tersedia!")

        val response = DataResponse(
            "success",
            "Berhasil mengambil data benda langit",
            mapOf(Pair("celestialBody", celestialBody))
        )
        call.respond(response)
    }

    // Ambil data request
    private suspend fun getCelestialBodyRequest(call: ApplicationCall): CelestialBodyRequest {
        val celestialBodyReq = CelestialBodyRequest()

        val multipartData = call.receiveMultipart(formFieldLimit = 1024 * 1024 * 5)
        multipartData.forEachPart { part ->
            when (part) {
                is PartData.FormItem -> {
                    when (part.name) {
                        "nama" -> celestialBodyReq.nama = part.value.trim()
                        "deskripsi" -> celestialBodyReq.deskripsi = part.value
                        "manfaat" -> celestialBodyReq.manfaat = part.value
                        "faktaMenarik" -> celestialBodyReq.faktaMenarik = part.value
                    }
                }

                is PartData.FileItem -> {
                    val ext = part.originalFileName
                        ?.substringAfterLast('.', "")
                        ?.let { if (it.isNotEmpty()) ".$it" else "" }
                        ?: ""

                    val fileName = UUID.randomUUID().toString() + ext
                    val filePath = "uploads/celestial-bodies/$fileName"

                    val file = File(filePath)
                    file.parentFile.mkdirs()

                    part.provider().copyAndClose(file.writeChannel())
                    celestialBodyReq.pathGambar = filePath
                }

                else -> {}
            }

            part.dispose()
        }

        return celestialBodyReq
    }

    // Validasi request data dari pengguna
    private fun validateCelestialBodyRequest(celestialBodyReq: CelestialBodyRequest) {
        val validatorHelper = ValidatorHelper(celestialBodyReq.toMap())
        validatorHelper.required("nama", "Nama tidak boleh kosong")
        validatorHelper.required("deskripsi", "Deskripsi tidak boleh kosong")
        validatorHelper.required("manfaat", "Manfaat tidak boleh kosong")
        validatorHelper.required("faktaMenarik", "Fakta Menarik tidak boleh kosong")
        validatorHelper.required("pathGambar", "Gambar tidak boleh kosong")
        validatorHelper.validate()

        val file = File(celestialBodyReq.pathGambar)
        if (!file.exists()) {
            throw AppException(400, "Gambar benda langit gagal diupload!")
        }
    }

    // Menambahkan data benda langit
    suspend fun createCelestialBody(call: ApplicationCall) {
        val celestialBodyReq = getCelestialBodyRequest(call)

        validateCelestialBodyRequest(celestialBodyReq)

        val existCelestialBody = celestialBodyRepository.getCelestialBodyByName(celestialBodyReq.nama)
        if (existCelestialBody != null) {
            val tmpFile = File(celestialBodyReq.pathGambar)
            if (tmpFile.exists()) tmpFile.delete()
            throw AppException(409, "Benda langit dengan nama ini sudah terdaftar!")
        }

        val celestialBodyId = celestialBodyRepository.addCelestialBody(
            celestialBodyReq.toEntity()
        )

        val response = DataResponse(
            "success",
            "Berhasil menambahkan data benda langit",
            mapOf(Pair("celestialBodyId", celestialBodyId))
        )
        call.respond(response)
    }

    // Mengubah data benda langit
    suspend fun updateCelestialBody(call: ApplicationCall) {
        val id = call.parameters["id"]
            ?: throw AppException(400, "ID benda langit tidak boleh kosong!")

        val oldCelestialBody = celestialBodyRepository.getCelestialBodyById(id)
            ?: throw AppException(404, "Data benda langit tidak tersedia!")

        val celestialBodyReq = getCelestialBodyRequest(call)

        if (celestialBodyReq.pathGambar.isEmpty()) {
            celestialBodyReq.pathGambar = oldCelestialBody.pathGambar
        }

        validateCelestialBodyRequest(celestialBodyReq)

        if (celestialBodyReq.nama != oldCelestialBody.nama) {
            val existCelestialBody = celestialBodyRepository.getCelestialBodyByName(celestialBodyReq.nama)
            if (existCelestialBody != null) {
                val tmpFile = File(celestialBodyReq.pathGambar)
                if (tmpFile.exists()) tmpFile.delete()
                throw AppException(409, "Benda langit dengan nama ini sudah terdaftar!")
            }
        }

        if (celestialBodyReq.pathGambar != oldCelestialBody.pathGambar) {
            val oldFile = File(oldCelestialBody.pathGambar)
            if (oldFile.exists()) oldFile.delete()
        }

        val isUpdated = celestialBodyRepository.updateCelestialBody(
            id, celestialBodyReq.toEntity()
        )
        if (!isUpdated) {
            throw AppException(400, "Gagal memperbarui data benda langit!")
        }

        val response = DataResponse(
            "success",
            "Berhasil mengubah data benda langit",
            null
        )
        call.respond(response)
    }

    // Menghapus data benda langit
    suspend fun deleteCelestialBody(call: ApplicationCall) {
        val id = call.parameters["id"]
            ?: throw AppException(400, "ID benda langit tidak boleh kosong!")

        val oldCelestialBody = celestialBodyRepository.getCelestialBodyById(id)
            ?: throw AppException(404, "Data benda langit tidak tersedia!")

        val oldFile = File(oldCelestialBody.pathGambar)

        val isDeleted = celestialBodyRepository.removeCelestialBody(id)
        if (!isDeleted) {
            throw AppException(400, "Gagal menghapus data benda langit!")
        }

        if (oldFile.exists()) oldFile.delete()

        val response = DataResponse(
            "success",
            "Berhasil menghapus data benda langit",
            null
        )
        call.respond(response)
    }

    // Mengambil gambar benda langit
    suspend fun getCelestialBodyImage(call: ApplicationCall) {
        val id = call.parameters["id"]
            ?: return call.respond(HttpStatusCode.BadRequest)

        val celestialBody = celestialBodyRepository.getCelestialBodyById(id)
            ?: return call.respond(HttpStatusCode.NotFound)

        val file = File(celestialBody.pathGambar)

        if (!file.exists()) {
            return call.respond(HttpStatusCode.NotFound)
        }

        call.respondFile(file)
    }
}