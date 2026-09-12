package com.assetcontrol.sites.application;

import com.assetcontrol.sites.domain.Site;
import com.assetcontrol.sites.domain.SiteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class SiteService {

    private final SiteRepository siteRepository;

    public SiteService(SiteRepository siteRepository) {
        this.siteRepository = siteRepository;
    }

    public List<Site> findAllActive() {
        return siteRepository.findByActiveTrueOrderByNameAsc();
    }

    public Site findActiveById(Long id) {
        return siteRepository.findByIdAndActiveTrue(id)
                .orElseThrow(SiteNotFoundException::new);
    }

    @Transactional
    public Site resolve(Long siteId, String newSiteName) {
        if (siteId != null) {
            return findActiveById(siteId);
        }

        String normalizedName = normalize(newSiteName);

        if (normalizedName.isBlank()) {
            throw new SiteRequiredException();
        }

        return siteRepository.findByNameIgnoreCase(normalizedName)
                .map(site -> {
                    site.activate();
                    return site;
                })
                .orElseGet(() -> siteRepository.save(new Site(normalizedName)));
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }

        return value.trim().replaceAll("\\s+", " ");
    }
}