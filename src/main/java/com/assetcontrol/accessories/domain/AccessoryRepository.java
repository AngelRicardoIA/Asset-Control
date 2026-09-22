package com.assetcontrol.accessories.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AccessoryRepository extends JpaRepository<Accessory, Long> {
    List<Accessory> findAllByOrderByTypeAscBrandAscModelAscIdAsc();
    boolean existsByAssetIgnoreCase(String asset);
    boolean existsByAssetIgnoreCaseAndIdNot(String asset, Long id);
    boolean existsBySerialNumberIgnoreCase(String serialNumber);
    boolean existsBySerialNumberIgnoreCaseAndIdNot(String serialNumber, Long id);
}
