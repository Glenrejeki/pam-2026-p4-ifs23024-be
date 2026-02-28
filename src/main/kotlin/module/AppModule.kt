package org.delcom.module

import org.delcom.repositories.ICelestialBodyRepository
import org.delcom.repositories.CelestialBodyRepository
import org.delcom.services.CelestialBodyService
import org.delcom.services.ProfileService
import org.koin.dsl.module

val appModule = module {
    // CelestialBody Repository
    single<ICelestialBodyRepository> {
        CelestialBodyRepository()
    }

    // CelestialBody Service
    single {
        CelestialBodyService(get())
    }

    // Profile Service
    single {
        ProfileService()
    }
}