package com.tanurpeach.backend.repository;

import com.tanurpeach.backend.model.TanService;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TanServiceRepository extends JpaRepository<TanService, Long> {
}