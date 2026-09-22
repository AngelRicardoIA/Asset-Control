package com.assetcontrol.phones.application;

import com.assetcontrol.phones.domain.Phone;
import com.assetcontrol.phones.domain.PhoneLine;
import com.assetcontrol.phones.domain.PhoneLineRepository;
import com.assetcontrol.phones.domain.PhoneRepository;
import com.assetcontrol.phones.domain.PhoneStatus;
import com.assetcontrol.sites.application.SiteService;
import com.assetcontrol.sites.domain.Site;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@Transactional
public class PhoneService {

    private final PhoneRepository phoneRepository;
    private final PhoneLineRepository phoneLineRepository;
    private final SiteService siteService;

    public PhoneService(
            PhoneRepository phoneRepository,
            PhoneLineRepository phoneLineRepository,
            SiteService siteService
    ) {
        this.phoneRepository = phoneRepository;
        this.phoneLineRepository = phoneLineRepository;
        this.siteService = siteService;
    }

    @Transactional(readOnly = true)
    public List<Phone> findAll() {
        return phoneRepository.findAllWithDetails();
    }

    @Transactional(readOnly = true)
    public Page<Phone> searchPage(
            String query,
            PhoneStatus status,
            Long siteId,
            boolean chronological,
            boolean descending,
            int page
    ) {
        return phoneRepository.searchPage(
                query == null ? "" : query.trim(),
                status,
                siteId,
                chronological,
                descending,
                PageRequest.of(page, 10)
        );
    }

    @Transactional(readOnly = true)
    public Phone findById(Long phoneId) {
        return phoneRepository.findByIdWithDetails(phoneId)
                .orElseThrow(() -> new PhoneNotFoundException(phoneId));
    }

    @Transactional(readOnly = true)
    public List<PhoneLine> findAvailablePhoneLines() {
        return phoneLineRepository.findAllAvailableForPhone();
    }

    @Transactional(readOnly = true)
    public List<PhoneLine> findSelectablePhoneLines(Long phoneId) {
        return phoneLineRepository.findAllSelectableForPhone(phoneId);
    }

    public Phone register(RegisterPhoneCommand command) {
        String imei = normalizeRequired(command.imei());

        if (phoneRepository.existsByImeiIgnoreCase(imei)) {
            throw new IllegalArgumentException("Ya existe un teléfono con ese IMEI.");
        }

        Site site = siteService.resolve(command.siteId(), command.newSiteName());
        PhoneLine phoneLine = resolvePhoneLine(
                command.phoneLineId(),
                command.newLineNumber(),
                command.newLineCarrier(),
                null
        );

        Phone phone = new Phone(
                imei,
                normalizeRequired(command.brand()),
                normalizeRequired(command.model()),
                PhoneStatus.AVAILABLE,
                site,
                phoneLine,
                normalizeOptional(command.observations())
        );

        return phoneRepository.save(phone);
    }

    public Phone update(Long phoneId, UpdatePhoneCommand command) {
        Phone phone = findById(phoneId);
        String imei = normalizeRequired(command.imei());

        if (phoneRepository.existsByImeiIgnoreCaseAndIdNot(imei, phoneId)) {
            throw new IllegalArgumentException("Ya existe un teléfono con ese IMEI.");
        }

        Site site = siteService.resolve(command.siteId(), command.newSiteName());
        PhoneLine phoneLine = resolvePhoneLine(
                command.phoneLineId(),
                command.newLineNumber(),
                command.newLineCarrier(),
                phoneId
        );

        phone.updateDetails(
                imei,
                normalizeRequired(command.brand()),
                normalizeRequired(command.model()),
                site,
                phoneLine,
                normalizeOptional(command.observations())
        );

        applyStatusTransition(phone, command.status());

        return phone;
    }

    private PhoneLine resolvePhoneLine(
            Long phoneLineId,
            String newLineNumber,
            String newLineCarrier,
            Long currentPhoneId
    ) {
        String normalizedNewLineNumber = normalizeOptional(newLineNumber);
        String normalizedNewLineCarrier = normalizeOptional(newLineCarrier);

        if (phoneLineId != null && normalizedNewLineNumber != null) {
            throw new IllegalArgumentException(
                    "Elige una línea existente o registra una nueva, no ambas."
            );
        }

        if (phoneLineId == null && normalizedNewLineNumber == null) {
            if (normalizedNewLineCarrier != null) {
                throw new IllegalArgumentException(
                        "Captura el número de la línea antes de indicar su proveedor."
                );
            }

            return null;
        }

        if (phoneLineId != null) {
            boolean usedByAnotherPhone = currentPhoneId == null
                    ? phoneRepository.existsByPhoneLineId(phoneLineId)
                    : phoneRepository.existsByPhoneLineIdAndIdNot(phoneLineId, currentPhoneId);

            if (usedByAnotherPhone) {
                throw new IllegalArgumentException(
                        "La línea seleccionada ya está ligada a otro teléfono."
                );
            }

            return phoneLineRepository.findById(phoneLineId)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "La línea seleccionada no existe."
                    ));
        }

        String lineNumber = normalizeLineNumber(normalizedNewLineNumber);

        if (phoneLineRepository.existsByNumberIgnoreCase(lineNumber)) {
            throw new IllegalArgumentException("Ya existe una línea con ese número.");
        }

        return phoneLineRepository.save(
                new PhoneLine(lineNumber, normalizedNewLineCarrier)
        );
    }

    private void applyStatusTransition(Phone phone, PhoneStatus requestedStatus) {
        if (requestedStatus == null || requestedStatus == phone.getStatus()) {
            return;
        }

        if (requestedStatus == PhoneStatus.AVAILABLE
                || requestedStatus == PhoneStatus.RETIRED) {
            phone.changeStatus(requestedStatus);
            return;
        }

        throw new IllegalArgumentException(
                "Asignado y Préstamo solo se establecen mediante una asignación."
        );
    }

    private String normalizeRequired(String value) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalArgumentException("Completa todos los campos obligatorios.");
        }

        return value.trim();
    }

    private String normalizeOptional(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String normalizeLineNumber(String value) {
        return normalizeRequired(value).replaceAll("[\\s()-]", "");
    }
}
