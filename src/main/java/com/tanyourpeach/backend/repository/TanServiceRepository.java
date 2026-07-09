package com.tanyourpeach.backend.repository;

import com.tanyourpeach.backend.model.ServiceType;
import com.tanyourpeach.backend.model.TanService;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TanServiceRepository extends JpaRepository<TanService, Long> {

    List<TanService> findByIsActiveTrueAndServiceTypeOrderByDisplayOrderAscNameAsc(ServiceType serviceType);

    List<TanService> findAllByOrderByDisplayOrderAscNameAsc();

    Optional<TanService> findByServiceIdAndIsActiveTrue(Long serviceId);

    Optional<TanService> findBySlugAndIsActiveTrue(String slug);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndServiceIdNot(String slug, Long serviceId);
}
