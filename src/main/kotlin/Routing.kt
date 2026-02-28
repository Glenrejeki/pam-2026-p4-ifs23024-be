package org.delcom

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.delcom.data.AppException
import org.delcom.data.ErrorResponse
import org.delcom.helpers.parseMessageToMap
import org.delcom.services.CelestialBodyService
import org.delcom.services.ProfileService
import org.koin.ktor.ext.inject

fun Application.configureRouting() {
    val celestialBodyService: CelestialBodyService by inject()
    val profileService: ProfileService by inject()

    install(StatusPages) {
        // Tangkap AppException
        exception<AppException> { call, cause ->
            val dataMap: Map<String, List<String>> = parseMessageToMap(cause.message)

            call.respond(
                status = HttpStatusCode.fromValue(cause.code),
                message = ErrorResponse(
                    status = "fail",
                    message = if (dataMap.isEmpty()) cause.message else "Data yang dikirimkan tidak valid!",
                    data = if (dataMap.isEmpty()) null else dataMap.toString()
                )
            )
        }

        // Tangkap semua Throwable lainnya
        exception<Throwable> { call, cause ->
            call.respond(
                status = HttpStatusCode.fromValue(500),
                message = ErrorResponse(
                    status = "error",
                    message = cause.message ?: "Unknown error",
                    data = ""
                )
            )
        }
    }

    routing {
        get("/") {
            call.respondText("API Angkasa & Tata Surya telah berjalan. Dibuat oleh Glen Rejeki Sitorus.")
        }

        // Route Celestial Bodies (Benda Langit)
        route("/celestial-bodies") {
            get {
                celestialBodyService.getAllCelestialBodies(call)
            }
            post {
                celestialBodyService.createCelestialBody(call)
            }
            get("/{id}") {
                celestialBodyService.getCelestialBodyById(call)
            }
            put("/{id}") {
                celestialBodyService.updateCelestialBody(call)
            }
            delete("/{id}") {
                celestialBodyService.deleteCelestialBody(call)
            }
            get("/{id}/image") {
                celestialBodyService.getCelestialBodyImage(call)
            }
        }

        // Route Profile
        route("/profile") {
            get {
                profileService.getProfile(call)
            }
            get("/photo") {
                profileService.getProfilePhoto(call)
            }
        }
    }
}