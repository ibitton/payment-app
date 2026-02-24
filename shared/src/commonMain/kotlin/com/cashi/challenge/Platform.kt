package com.cashi.challenge

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform