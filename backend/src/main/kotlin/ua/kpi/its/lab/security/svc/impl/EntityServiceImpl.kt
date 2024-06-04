package ua.kpi.its.lab.security.svc.impl

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ua.kpi.its.lab.security.dto.SoftwareModuleResponse
import ua.kpi.its.lab.security.dto.SoftwareProductRequest
import ua.kpi.its.lab.security.dto.SoftwareProductResponse
import ua.kpi.its.lab.security.entity.SoftwareModule
import ua.kpi.its.lab.security.entity.SoftwareProduct
import ua.kpi.its.lab.security.repo.SoftwareProductRepository
import ua.kpi.its.lab.security.svc.SoftwareProductService
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.jvm.optionals.getOrElse

@Service
class SoftwareProductServiceImpl @Autowired constructor(
    private val repository: SoftwareProductRepository
) : SoftwareProductService {
    override fun create(softwareProduct: SoftwareProductRequest): SoftwareProductResponse {
        val module = softwareProduct.module
        val newModule = SoftwareModule(
            description = module.description,
            author = module.author,
            language = module.language,
            lastUpdated = this.stringToDate(module.lastUpdated),
            size = module.size,
            linesOfCode = module.linesOfCode,
            isCrossPlatform = module.isCrossPlatform
        )
        var newSoftwareProduct = SoftwareProduct(
            name = softwareProduct.name,
            developer = softwareProduct.developer,
            version = softwareProduct.version,
            releaseDate = this.stringToDate(softwareProduct.releaseDate),
            size = softwareProduct.size,
            is64bit = softwareProduct.is64bit,
            isCrossPlatform = softwareProduct.isCrossPlatform,
            module = newModule
        )
        newModule.softwareProduct = newSoftwareProduct
        newSoftwareProduct = this.repository.save(newSoftwareProduct)
        return this.softwareProductEntityToDto(newSoftwareProduct)
    }

    override fun read(): List<SoftwareProductResponse> {
        return this.repository.findAll().map(this::softwareProductEntityToDto)
    }

    override fun readById(id: Long): SoftwareProductResponse {
        val softwareProduct = this.getSoftwareProductById(id)
        return this.softwareProductEntityToDto(softwareProduct)
    }

    override fun updateById(id: Long, softwareProduct: SoftwareProductRequest): SoftwareProductResponse {
        val oldSoftwareProduct = this.getSoftwareProductById(id)

        oldSoftwareProduct.apply {
            name = softwareProduct.name
            developer = softwareProduct.developer
            version = softwareProduct.version
            releaseDate = this@SoftwareProductServiceImpl.stringToDate(softwareProduct.releaseDate)
            size = softwareProduct.size
            is64bit = softwareProduct.is64bit
            isCrossPlatform = softwareProduct.isCrossPlatform
        }
        oldSoftwareProduct.module.apply {
            description = softwareProduct.module.description
            author = softwareProduct.module.author
            language = softwareProduct.module.language
            lastUpdated = this@SoftwareProductServiceImpl.stringToDate(softwareProduct.module.lastUpdated)
            size = softwareProduct.module.size
            linesOfCode = softwareProduct.module.linesOfCode
            isCrossPlatform = softwareProduct.module.isCrossPlatform
        }
        val updatedSoftwareProduct = this.repository.save(oldSoftwareProduct)
        return this.softwareProductEntityToDto(updatedSoftwareProduct)
    }

    override fun deleteById(id: Long): SoftwareProductResponse {
        val softwareProduct = this.getSoftwareProductById(id)
        this.repository.delete(softwareProduct)
        return this.softwareProductEntityToDto(softwareProduct)
    }

    private fun getSoftwareProductById(id: Long): SoftwareProduct {
        return this.repository.findById(id).getOrElse {
            throw IllegalArgumentException("Software Product not found by id = $id")
        }
    }

    private fun softwareProductEntityToDto(softwareProduct: SoftwareProduct): SoftwareProductResponse {
        return SoftwareProductResponse(
            id = softwareProduct.id,
            name = softwareProduct.name,
            developer = softwareProduct.developer,
            version = softwareProduct.version,
            releaseDate = this.dateToString(softwareProduct.releaseDate),
            size = softwareProduct.size,
            is64bit = softwareProduct.is64bit,
            isCrossPlatform = softwareProduct.isCrossPlatform,
            module = this.softwareModuleEntityToDto(softwareProduct.module)
        )
    }

    private fun softwareModuleEntityToDto(softwareModule: SoftwareModule): SoftwareModuleResponse {
        return SoftwareModuleResponse(
            id = softwareModule.id,
            description = softwareModule.description,
            author = softwareModule.author,
            language = softwareModule.language,
            lastUpdated = this.dateToString(softwareModule.lastUpdated),
            size = softwareModule.size,
            linesOfCode = softwareModule.linesOfCode,
            isCrossPlatform = softwareModule.isCrossPlatform
        )
    }

    private fun dateToString(date: Date): String {
        val instant = date.toInstant()
        val dateTime = instant.atOffset(ZoneOffset.UTC).toLocalDateTime()
        return dateTime.format(DateTimeFormatter.ISO_DATE_TIME)
    }

    private fun stringToDate(date: String): Date {
        return try {
            val dateTime = LocalDateTime.parse(date, DateTimeFormatter.ISO_DATE_TIME)
            val instant = dateTime.toInstant(ZoneOffset.UTC)
            Date.from(instant)
        } catch (e: Exception) {
            Date() // Return current date as fallback
        }
    }
}