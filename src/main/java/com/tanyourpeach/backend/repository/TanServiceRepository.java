package com.tanyourpeach.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tanyourpeach.backend.model.TanService;

public interface TanServiceRepository extends JpaRepository<TanService, Long> {
}