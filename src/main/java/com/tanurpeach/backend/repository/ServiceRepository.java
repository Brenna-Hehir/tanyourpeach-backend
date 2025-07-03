package com.tanurpeach.backend.repository;

import com.tanurpeach.backend.model.Service;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceRepository extends JpaRepository<Service, Long> {
}