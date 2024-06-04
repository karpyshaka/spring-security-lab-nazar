package ua.kpi.its.lab.security.repo

import org.springframework.data.jpa.repository.JpaRepository
import ua.kpi.its.lab.security.entity.SoftwareProduct
import ua.kpi.its.lab.security.entity.SoftwareModule

interface SoftwareProductRepository : JpaRepository<SoftwareProduct, Long>


interface SoftwareModuleRepository : JpaRepository<SoftwareModule, Long>
