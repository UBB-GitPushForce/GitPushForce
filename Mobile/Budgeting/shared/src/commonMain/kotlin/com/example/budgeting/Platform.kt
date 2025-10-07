package com.example.budgeting

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform