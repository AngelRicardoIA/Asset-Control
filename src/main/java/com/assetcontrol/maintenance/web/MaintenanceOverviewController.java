package com.assetcontrol.maintenance.web;

import com.assetcontrol.maintenance.application.MaintenanceDueStatus;
import com.assetcontrol.maintenance.application.MaintenanceOverviewService;
import com.assetcontrol.maintenance.application.MaintenancePlanningService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Locale;

@Controller
@RequestMapping("/maintenance")
public class MaintenanceOverviewController {
    private final MaintenanceOverviewService overview;
    private final MaintenancePlanningService planning;

    public MaintenanceOverviewController(MaintenanceOverviewService overview, MaintenancePlanningService planning) {
        this.overview = overview;
        this.planning = planning;
    }

    @GetMapping
    public String index(@RequestParam(defaultValue = "") String query,
                        @RequestParam(required = false) MaintenanceDueStatus status,
                        @RequestParam(defaultValue = "1") int page, Model model) {
        var all = overview.overview();
        var filtered = all.computers().stream()
                .filter(item -> status == null || item.status() == status)
                .filter(item -> query.isBlank() || (item.computer().getHost() + " " + item.computer().getAsset()
                        + " " + item.computer().getBrand() + " " + item.computer().getModel())
                        .toLowerCase(Locale.ROOT).contains(query.strip().toLowerCase(Locale.ROOT)))
                .toList();
        int pages = Math.max(1, (filtered.size() + 19) / 20);
        int currentPage = Math.max(1, Math.min(page, pages));
        model.addAttribute("items", filtered.subList((currentPage - 1) * 20, Math.min(currentPage * 20, filtered.size())));
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("totalPages", pages);
        model.addAttribute("visiblePages", java.util.stream.IntStream.rangeClosed(Math.max(1, currentPage - 2),
                Math.min(pages, currentPage + 2)).boxed().toList());
        model.addAttribute("total", filtered.size());
        model.addAttribute("overview", all);
        model.addAttribute("query", query);
        model.addAttribute("status", status);
        model.addAttribute("statuses", MaintenanceDueStatus.values());
        model.addAttribute("policy", overview.policy());
        return "maintenance/index";
    }

    @GetMapping("/settings")
    public String settings(Model model) {
        var policy = overview.policy();
        var form = new MaintenancePolicyForm();
        form.setIntervalMonths(policy.getIntervalMonths());
        form.setWarningDays(policy.getWarningDays());
        model.addAttribute("form", form);
        return "maintenance/settings";
    }

    @PostMapping("/settings")
    public String saveSettings(@Valid @ModelAttribute("form") MaintenancePolicyForm form, BindingResult result,
                               RedirectAttributes redirect) {
        if (result.hasErrors()) return "maintenance/settings";
        planning.updatePolicy(form.getIntervalMonths(), form.getWarningDays());
        redirect.addFlashAttribute("successMessage", "Frecuencia de revisión actualizada.");
        return "redirect:/maintenance";
    }

    @GetMapping("/computers/{computerId}/schedule")
    public String schedule(@PathVariable Long computerId, Model model) {
        var due = overview.findByComputerId(computerId);
        var form = new MaintenanceScheduleForm();
        form.setNextDueAt(due.nextDueAt());
        model.addAttribute("form", form);
        model.addAttribute("due", due);
        return "maintenance/schedule";
    }

    @PostMapping("/computers/{computerId}/schedule")
    public String saveSchedule(@PathVariable Long computerId,
                               @Valid @ModelAttribute("form") MaintenanceScheduleForm form,
                               BindingResult result, Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) {
            model.addAttribute("due", overview.findByComputerId(computerId));
            return "maintenance/schedule";
        }
        planning.schedule(computerId, form.getNextDueAt(), form.getReason(), form.getRecordedBy());
        redirect.addFlashAttribute("successMessage", "Próxima revisión programada.");
        return "redirect:/computers/" + computerId + "#mantenimiento";
    }
}
