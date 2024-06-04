package ua.kpi.its.lab.security.dto

import kotlinx.serialization.Serializable

@Serializable
data class SoftwareProductRequest(
    var name: String,
    var developer: String,
    var version: String,
    var releaseDate: String,
    var size: Double,
    var is64bit: Boolean,
    var isCrossPlatform: Boolean,
    var module: SoftwareModuleRequest
)
@Serializable

data class SoftwareProductResponse(
    var id: Long,
    var name: String,
    var developer: String,
    var version: String,
    var releaseDate: String,
    var size: Double,
    var is64bit: Boolean,
    var isCrossPlatform: Boolean,
    var module: SoftwareModuleResponse
)
@Serializable

data class SoftwareModuleRequest(
    var description: String,
    var author: String,
    var language: String,
    var lastUpdated: String,
    var size: Double,
    var linesOfCode: Int,
    var isCrossPlatform: Boolean
)
@Serializable

data class SoftwareModuleResponse(
    var id: Long,
    var description: String,
    var author: String,
    var language: String,
    var lastUpdated: String,
    var size: Double,
    var linesOfCode: Int,
    var isCrossPlatform: Boolean
)
