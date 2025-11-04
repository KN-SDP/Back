package com.knusdp.SmartLedger.repository;

import com.knusdp.SmartLedger.entity.AccountBook;
import com.knusdp.SmartLedger.entity.AccountCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<AccountCategory, Long> {


}
