package com.assetcontrol.accessories.domain;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface AccessoryAssignmentRepository extends JpaRepository<AccessoryAssignment, Long> {
    @EntityGraph(attributePaths = {"accessory", "person"})
    List<AccessoryAssignment> findByReturnedAtIsNullOrderByAssignedAtDescIdDesc();
    @EntityGraph(attributePaths = {"accessory", "person"})
    List<AccessoryAssignment> findByAccessory_IdOrderByAssignedAtDescIdDesc(Long accessoryId);
    @EntityGraph(attributePaths = {"accessory", "person"})
    List<AccessoryAssignment> findByPerson_IdOrderByAssignedAtDescIdDesc(Long personId);
    @EntityGraph(attributePaths = {"accessory", "person"})
    Optional<AccessoryAssignment> findByAccessory_IdAndReturnedAtIsNull(Long accessoryId);
    @EntityGraph(attributePaths = {"accessory", "person"})
    Optional<AccessoryAssignment> findByIdAndAccessory_Id(Long id, Long accessoryId);
}
