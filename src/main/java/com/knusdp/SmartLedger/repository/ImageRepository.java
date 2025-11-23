package com.knusdp.SmartLedger.repository;

import com.knusdp.SmartLedger.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImageRepository extends JpaRepository<Image, Integer> {
}

