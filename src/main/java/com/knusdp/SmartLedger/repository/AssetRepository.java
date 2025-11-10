package com.knusdp.SmartLedger.repository;

import com.knusdp.SmartLedger.entity.Asset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssetRepository extends JpaRepository<Asset, Long> {
    List<Asset> findByMemberId(Long userId);
    List<Asset> findAllByMember_Id(Long userId);

}
