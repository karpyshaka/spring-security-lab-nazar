package ua.kpi.its.lab.security.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.oauth2.jwt.JwtClaimsSet
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.JwtEncoderParameters
import org.springframework.web.bind.annotation.*
import ua.kpi.its.lab.security.dto.SoftwareProductRequest
import ua.kpi.its.lab.security.dto.SoftwareProductResponse
import ua.kpi.its.lab.security.svc.SoftwareProductService
import java.time.Instant

@RestController
@RequestMapping("/software-products")
class SoftwareProductController @Autowired constructor(
    private val softwareProductService: SoftwareProductService
) {
    @GetMapping(path = ["", "/"])
    fun softwareProducts(): List<SoftwareProductResponse> = softwareProductService.read()

    @GetMapping("{id}")
    fun readSoftwareProduct(@PathVariable("id") id: Long): ResponseEntity<SoftwareProductResponse> {
        return wrapNotFound { softwareProductService.readById(id) }
    }

    @PostMapping(path = ["", "/"])
    fun createSoftwareProduct(@RequestBody softwareProduct: SoftwareProductRequest): SoftwareProductResponse {
        return softwareProductService.create(softwareProduct)
    }

    @PutMapping("{id}")
    fun updateSoftwareProduct(
        @PathVariable("id") id: Long,
        @RequestBody softwareProduct: SoftwareProductRequest
    ): ResponseEntity<SoftwareProductResponse> {
        return wrapNotFound { softwareProductService.updateById(id, softwareProduct) }
    }

    @DeleteMapping("{id}")
    fun deleteSoftwareProduct(@PathVariable("id") id: Long): ResponseEntity<SoftwareProductResponse> {
        return wrapNotFound { softwareProductService.deleteById(id) }
    }

    fun <T> wrapNotFound(call: () -> T): ResponseEntity<T> {
        return try {
            val result = call()
            ResponseEntity.ok(result)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.notFound().build()
        }
    }
}

@RestController
@RequestMapping("/auth")
class AuthenticationTokenController @Autowired constructor(
    private val encoder: JwtEncoder
) {
    private val authTokenExpiry: Long = 3600L // in seconds

    @PostMapping("token")
    fun token(auth: Authentication): String {
        val now = Instant.now()
        val scope = auth.authorities.joinToString(" ", transform = GrantedAuthority::getAuthority)
        val claims = JwtClaimsSet.builder()
            .issuer("self")
            .issuedAt(now)
            .expiresAt(now.plusSeconds(authTokenExpiry))
            .subject(auth.name)
            .claim("scope", scope)
            .build()
        return encoder.encode(JwtEncoderParameters.from(claims)).tokenValue
    }
}
