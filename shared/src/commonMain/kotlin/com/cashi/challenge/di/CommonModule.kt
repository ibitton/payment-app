package com.cashi.challenge.di

import com.cashi.challenge.data.api.PaymentApi
import com.cashi.challenge.data.api.PaymentApiClient
import com.cashi.challenge.data.repository.FirestorePaymentRepository
import com.cashi.challenge.data.repository.PaymentRepository
import com.cashi.challenge.domain.usecases.GetTransactionHistoryUseCase
import com.cashi.challenge.domain.usecases.ProcessPaymentUseCase
import com.cashi.challenge.domain.validation.PaymentValidator
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.firestore
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

/**
 * Koin DI module for the shared KMP module.
 * Provides dependencies that are common across all platforms.
 */
fun commonModule() = module {

    // HttpClient configuration
    single {
        HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        println("Ktor HttpClient: $message")
                    }
                }
                level = LogLevel.INFO
            }
        }
    }

    // Firebase Firestore
    single<FirebaseFirestore> { Firebase.firestore }

    // Validators
    singleOf(::PaymentValidator)

    // API Clients
    single<PaymentApi> { PaymentApiClient(get()) }

    // Repositories
    single<PaymentRepository> { FirestorePaymentRepository(get()) }

    // Use Cases
    factoryOf(::ProcessPaymentUseCase)
    factoryOf(::GetTransactionHistoryUseCase)
}
