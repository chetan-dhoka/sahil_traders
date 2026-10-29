package com.sahil.billing.repository;

import com.sahil.billing.entity.FirmSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FirmSettingsRepository extends JpaRepository<FirmSettings, Long> {
}
