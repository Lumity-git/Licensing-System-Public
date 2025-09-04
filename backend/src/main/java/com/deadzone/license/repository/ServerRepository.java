package com.deadzone.license.repository;

import com.deadzone.license.entity.License;
import com.deadzone.license.entity.ServerInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ServerRepository extends JpaRepository<ServerInstance, Long> {

    List<ServerInstance> findByLicenseId(Long licenseId);

    List<ServerInstance> findByLicenseIdOrderByLastSeenDesc(Long licenseId);

    Optional<ServerInstance> findByLicenseIdAndServerIp(Long licenseId, String serverIp);

    Optional<ServerInstance> findByLicenseAndServerIp(License license, String serverIp);

    // New method to find by server IP regardless of license
    Optional<ServerInstance> findFirstByServerIpOrderByLastSeenDesc(String serverIp);

    long countByLicenseId(Long licenseId);

    List<ServerInstance> findByLastSeenBefore(LocalDateTime cutoffTime);

    List<ServerInstance> findByLastSeenAfter(LocalDateTime cutoffTime);

    // New methods for enhanced tracking
    List<ServerInstance> findByLicenseStatusOrderByLastSeenDesc(ServerInstance.LicenseStatus licenseStatus);

    List<ServerInstance> findByPluginStatusOrderByLastSeenDesc(ServerInstance.PluginStatus pluginStatus);

    @Query("SELECT s FROM ServerInstance s WHERE s.licenseStatus = :status AND s.lastSeen > :cutoffTime")
    List<ServerInstance> findActiveServersByLicenseStatus(@Param("status") ServerInstance.LicenseStatus status, @Param("cutoffTime") LocalDateTime cutoffTime);

    @Query("SELECT s FROM ServerInstance s ORDER BY s.lastSeen DESC")
    List<ServerInstance> findAllOrderByLastSeenDesc();

    long countByLastSeenAfter(LocalDateTime cutoffTime);

    @Query("SELECT s FROM ServerInstance s WHERE s.license.licenseKey = :licenseKey")
    List<ServerInstance> findByLicenseKey(@Param("licenseKey") String licenseKey);

    @Query("SELECT s FROM ServerInstance s WHERE s.serverIp = :ip ORDER BY s.lastSeen DESC")
    List<ServerInstance> findByServerIp(@Param("ip") String ip);

    @Query("SELECT COUNT(DISTINCT s.serverIp) FROM ServerInstance s WHERE s.license.id = :licenseId")
    long countDistinctIpsByLicenseId(@Param("licenseId") Long licenseId);

    @Query("SELECT s FROM ServerInstance s WHERE s.lastSeen >= :since ORDER BY s.lastSeen DESC")
    List<ServerInstance> findRecentlyActive(@Param("since") LocalDateTime since);

    boolean existsByLicenseIdAndServerIp(Long licenseId, String serverIp);
}
