package com.knusdp.SmartLedger.repository;

import com.knusdp.SmartLedger.entity.AccountBook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LedgerRepository extends JpaRepository<AccountBook, Long> {

}
