package com.deadzone.license.repository;

import com.deadzone.license.entity.License;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface LicenseRepository extends JpaRepository<License, Long> {

    Optional<License> findByLicenseKey(String licenseKey);

    Optional<License> findByLicenseKeyAndActiveTrue(String licenseKey);

    List<License> findByActiveTrueOrderByCreatedAtDesc();

    List<License> findByExpirationDateBeforeAndActiveTrue(LocalDateTime date);

    @Query("SELECT l FROM License l WHERE l.active = true AND " +
           "(l.expirationDate IS NULL OR l.expirationDate >= :currentDate)")
    List<License> findActiveLicenses(@Param("currentDate") LocalDateTime currentDate);

    @Query("SELECT COUNT(l) FROM License l WHERE l.active = true")
    long countActiveLicenses();

    boolean existsByLicenseKey(String licenseKey);
}
