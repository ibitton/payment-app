package com.cashi.challenge.di

import com.cashi.challenge.ui.viewmodel.PaymentViewModel
import com.cashi.challenge.ui.viewmodel.TransactionHistoryViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

fun viewModelModule() = module {
    factoryOf(::PaymentViewModel)
    factoryOf(::TransactionHistoryViewModel)
}
