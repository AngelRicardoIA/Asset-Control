package com.assetcontrol.accessories.application;

import com.assetcontrol.accessories.domain.Accessory;
import com.assetcontrol.accessories.domain.AccessoryAssignment;
import com.assetcontrol.accessories.domain.AccessoryAssignmentRepository;
import com.assetcontrol.accessories.domain.AccessoryRepository;
import com.assetcontrol.accessories.domain.AccessoryType;
import com.assetcontrol.people.application.PersonNotFoundException;
import com.assetcontrol.people.domain.PersonRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class AccessoryService {
    private final AccessoryRepository accessories;
    private final AccessoryAssignmentRepository assignments;
    private final PersonRepository people;

    public AccessoryService(AccessoryRepository accessories, AccessoryAssignmentRepository assignments,
                            PersonRepository people) {
        this.accessories = accessories;
        this.assignments = assignments;
        this.people = people;
    }

    public Accessory find(Long id) {
        return accessories.findById(id).orElseThrow(() -> new AccessoryNotFoundException(id));
    }

    public AccessoryAssignment active(Long id) {
        return assignments.findByAccessory_IdAndReturnedAtIsNull(id).orElse(null);
    }

    public List<AccessoryAssignment> history(Long id) {
        return assignments.findByAccessory_IdOrderByAssignedAtDescIdDesc(id);
    }

    public List<AccessoryAssignment> historyForPerson(Long personId) {
        return assignments.findByPerson_IdOrderByAssignedAtDescIdDesc(personId);
    }

    public AccessoryInventory inventory(String query, AccessoryType type, boolean assigned) {
        Map<Long, AccessoryAssignment> active = new LinkedHashMap<>();
        assignments.findByReturnedAtIsNullOrderByAssignedAtDescIdDesc().forEach(item ->
                active.put(item.getAccessory().getId(), item));
        List<AccessorySnapshot> all = accessories.findAllByOrderByTypeAscBrandAscModelAscIdAsc().stream()
                .map(item -> new AccessorySnapshot(item, active.get(item.getId()))).toList();
        Map<String, AccessoryStockGroup> counts = new LinkedHashMap<>();
        for (AccessorySnapshot item : all) {
            if (item.assigned()) continue;
            var accessory = item.accessory();
            String key = accessory.getType().name();
            counts.compute(key, (unused, old) -> new AccessoryStockGroup(accessory.getType(),
                    old == null || old.brand().equalsIgnoreCase(accessory.getBrand())
                            ? accessory.getBrand() : "Varias marcas",
                    old == null ? 1 : old.quantity() + 1));
        }
        String term = query == null ? "" : query.strip().toLowerCase(Locale.ROOT);
        List<AccessorySnapshot> items = all.stream()
                .filter(item -> item.assigned() == assigned)
                .filter(item -> type == null || item.accessory().getType() == type)
                .filter(item -> term.isEmpty() || searchable(item).contains(term)).toList();
        return new AccessoryInventory(items, new ArrayList<>(counts.values()), all.size() - active.size(), active.size());
    }

    private String searchable(AccessorySnapshot item) {
        Accessory accessory = item.accessory();
        String person = item.assignment() == null ? "" : item.assignment().getPerson().getUsername() + " "
                + item.assignment().getPerson().getFullName();
        return (accessory.getType().getLabel() + " " + accessory.getBrand() + " " + accessory.getModel() + " "
                + (accessory.getAsset() == null ? "" : accessory.getAsset()) + " "
                + (accessory.getSerialNumber() == null ? "" : accessory.getSerialNumber()) + " " + person)
                .toLowerCase(Locale.ROOT);
    }

    @Transactional
    public Accessory createStock(AccessoryType type, String brand, String model, String serial, String asset, int quantity) {
        if (quantity < 1 || quantity > 200) throw new IllegalArgumentException("La cantidad debe estar entre 1 y 200.");
        if (quantity > 1 && (optional(serial) != null || optional(asset) != null)) {
            throw new IllegalArgumentException("Para registrar varias unidades, deja vacíos serie y asset y completa cada ficha después.");
        }
        Accessory first = null;
        for (int i = 0; i < quantity; i++) {
            Accessory saved = save(null, type, brand, model, serial, asset);
            if (first == null) first = saved;
        }
        return first;
    }

    @Transactional
    public Accessory save(Long id, AccessoryType type, String brand, String model, String serial, String asset) {
        if (type == null) throw new IllegalArgumentException("Selecciona el tipo de accesorio.");
        String normalizedBrand = required(brand, "marca");
        String normalizedModel = required(model, "modelo");
        String normalizedSerial = optional(serial);
        String normalizedAsset = optional(asset);
        if (normalizedAsset != null && (id == null ? accessories.existsByAssetIgnoreCase(normalizedAsset)
                : accessories.existsByAssetIgnoreCaseAndIdNot(normalizedAsset, id))) {
            throw new IllegalArgumentException("Ya existe un accesorio con ese asset.");
        }
        if (normalizedSerial != null && (id == null ? accessories.existsBySerialNumberIgnoreCase(normalizedSerial)
                : accessories.existsBySerialNumberIgnoreCaseAndIdNot(normalizedSerial, id))) {
            throw new IllegalArgumentException("Ya existe un accesorio con ese número de serie.");
        }
        Accessory accessory = id == null
                ? new Accessory(type, normalizedBrand, normalizedModel, normalizedSerial, normalizedAsset)
                : find(id);
        accessory.update(type, normalizedBrand, normalizedModel, normalizedSerial, normalizedAsset);
        return accessories.save(accessory);
    }

    @Transactional
    public void assign(Long accessoryId, Long personId, LocalDate date, String assignedBy) {
        Accessory accessory = find(accessoryId);
        if (active(accessoryId) != null) throw new IllegalStateException("Primero devuelve este accesorio.");
        if (date == null || date.isAfter(LocalDate.now())) throw new IllegalArgumentException("La fecha de asignación no puede ser futura.");
        var person = people.findById(personId).orElseThrow(() -> new PersonNotFoundException(personId));
        assignments.saveAndFlush(new AccessoryAssignment(accessory, person, date, required(assignedBy, "responsable")));
    }

    @Transactional
    public void returnToStock(Long accessoryId, Long assignmentId, LocalDate date, String receivedBy, String notes) {
        AccessoryAssignment assignment = assignments.findByIdAndAccessory_Id(assignmentId, accessoryId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró la asignación."));
        if (date == null || date.isAfter(LocalDate.now())) throw new IllegalArgumentException("La fecha de devolución no puede ser futura.");
        assignment.returnToStock(date, required(receivedBy, "persona que recibe"), optional(notes));
    }

    private String required(String value, String label) {
        String result = optional(value);
        if (result == null) throw new IllegalArgumentException("Indica " + label + ".");
        return result;
    }

    private String optional(String value) {
        return value == null || value.isBlank() ? null : value.strip().replaceAll("\\s+", " ");
    }
}
