package com.assetcontrol.computers.application;

import com.assetcontrol.computers.domain.Computer;
import com.assetcontrol.computers.domain.ComputerRepository;
import com.assetcontrol.computers.domain.ComputerStatus;
import com.assetcontrol.computers.domain.ComputerType;
import com.assetcontrol.sites.application.SiteService;
import com.assetcontrol.sites.domain.Site;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@Transactional(readOnly = true)
public class ComputerService {

    private final ComputerRepository computerRepository;
    private final SiteService siteService;

    public ComputerService(
            ComputerRepository computerRepository,
            SiteService siteService
    ) {
        this.computerRepository = computerRepository;
        this.siteService = siteService;
    }

    public List<Computer> search(
            String query,
            ComputerStatus status,
            ComputerType type,
            Long siteId
    ) {
        String normalizedQuery = query == null ? "" : query.trim();

        return computerRepository.search(normalizedQuery, status, type, siteId);
    }

    public Computer findById(Long id) {
        return computerRepository.findDetailedById(id)
                .orElseThrow(() -> new ComputerNotFoundException(id));
    }

    @Transactional
    public Computer create(CreateComputerCommand command) {
        String asset = normalizeIdentifier(command.asset());
        String host = normalizeIdentifier(command.host());

        if (computerRepository.existsByAssetIgnoreCase(asset)) {
            throw new DuplicateComputerFieldException("asset");
        }

        if (computerRepository.existsByHostIgnoreCase(host)) {
            throw new DuplicateComputerFieldException("host");
        }

        Site site = siteService.resolve(command.siteId(), command.newSiteName());

        Computer computer = new Computer(
                asset,
                host,
                command.type(),
                normalizeRequired(command.brand()),
                normalizeRequired(command.model()),
                normalizeRequired(command.serialNumber()),
                normalizeOptional(command.chargerSerialNumber()),
                normalizeOptional(command.operatingSystem()),
                command.status(),
                site,
                normalizeOptional(command.observations())
        );

        return computerRepository.save(computer);
    }

    private String normalizeIdentifier(String value) {
        return value.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeRequired(String value) {
        return value.trim();
    }

    private String normalizeOptional(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    @Transactional
    public Computer update(Long id, UpdateComputerCommand command) {
        Computer computer = computerRepository.findById(id)
                .orElseThrow(() -> new ComputerNotFoundException(id));

        String asset = normalizeIdentifier(command.asset());
        String host = normalizeIdentifier(command.host());

        if (computerRepository.existsByAssetIgnoreCaseAndIdNot(asset, id)) {
            throw new DuplicateComputerFieldException("asset");
        }

        if (computerRepository.existsByHostIgnoreCaseAndIdNot(host, id)) {
            throw new DuplicateComputerFieldException("host");
        }

        Site site = siteService.resolve(command.siteId(), command.newSiteName());

        applyStatusTransition(computer, command.status());
        computer.updateDetails(
                asset,
                host,
                command.type(),
                normalizeRequired(command.brand()),
                normalizeRequired(command.model()),
                normalizeRequired(command.serialNumber()),
                normalizeOptional(command.chargerSerialNumber()),
                normalizeOptional(command.operatingSystem()),
                site,
                normalizeOptional(command.observations())
        );

        return computer;
    }

    private void applyStatusTransition(Computer computer, ComputerStatus requestedStatus) {
        if (requestedStatus == null || requestedStatus == computer.getStatus()) {
            return;
        }

        if (requestedStatus == ComputerStatus.AVAILABLE
                || requestedStatus == ComputerStatus.RETIRED) {
            computer.changeStatus(requestedStatus);
            return;
        }

        throw new IllegalArgumentException(
                "Asignado y Préstamo solo se establecen mediante una asignación."
        );
    }
}
