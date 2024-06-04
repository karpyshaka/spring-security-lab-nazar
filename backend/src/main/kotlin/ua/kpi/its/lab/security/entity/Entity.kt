package ua.kpi.its.lab.security.entity

import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "software_products")
class SoftwareProduct(
    @Column
    var name: String,

    @Column
    var developer: String,

    @Column
    var version: String,

    @Column
    var releaseDate: Date,

    @Column
    var size: Double,

    @Column
    var is64bit: Boolean,

    @Column
    var isCrossPlatform: Boolean,

    @OneToOne(cascade = [CascadeType.ALL])
    @JoinColumn(name = "module_id", referencedColumnName = "id")
    var module: SoftwareModule
) : Comparable<SoftwareProduct> {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Long = -1

    override fun compareTo(other: SoftwareProduct): Int {
        val equal = this.name == other.name && this.releaseDate.time == other.releaseDate.time
        return if (equal) 0 else 1
    }

    override fun toString(): String {
        return "SoftwareProduct(name=$name, releaseDate=$releaseDate, module=$module)"
    }
}

@Entity
@Table(name = "software_modules")
class SoftwareModule(
    @Column
    var description: String,

    @Column
    var author: String,

    @Column
    var language: String,

    @Column
    var lastUpdated: Date,

    @Column
    var size: Double,

    @Column
    var linesOfCode: Int,

    @Column
    var isCrossPlatform: Boolean,

    @OneToOne(mappedBy = "module")
    var softwareProduct: SoftwareProduct? = null
) : Comparable<SoftwareModule> {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Long = -1

    override fun compareTo(other: SoftwareModule): Int {
        val equal = this.description == other.description && this.size == other.size
        return if (equal) 0 else 1
    }

    override fun toString(): String {
        return "SoftwareModule(description=$description, size=$size)"
    }
}
