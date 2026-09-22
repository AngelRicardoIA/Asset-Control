package com.assetcontrol.accessories.web;

import com.assetcontrol.accessories.application.AccessoryService;
import com.assetcontrol.accessories.domain.AccessoryType;
import com.assetcontrol.people.application.PersonService;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
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

@Controller
@RequestMapping("/accessories")
public class AccessoryController {
    private final AccessoryService accessories;
    private final PersonService people;

    public AccessoryController(AccessoryService accessories, PersonService people) {
        this.accessories = accessories;
        this.people = people;
    }

    @GetMapping
    public String index(@RequestParam(defaultValue = "") String query,
                        @RequestParam(required = false) AccessoryType type,
                        @RequestParam(defaultValue = "STOCK") String view,
                        @RequestParam(defaultValue = "1") int page, Model model) {
        boolean assigned = "ASSIGNED".equals(view);
        var inventory = accessories.inventory(query, type, assigned);
        int totalPages = Math.max(1, (inventory.items().size() + 11) / 12);
        int current = Math.max(1, Math.min(page, totalPages));
        model.addAttribute("items", inventory.items().subList((current - 1) * 12,
                Math.min(current * 12, inventory.items().size())));
        model.addAttribute("inventory", inventory);
        model.addAttribute("page", current);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("pages", java.util.stream.IntStream.rangeClosed(Math.max(1, current - 2),
                Math.min(totalPages, current + 2)).boxed().toList());
        model.addAttribute("query", query);
        model.addAttribute("type", type);
        model.addAttribute("view", assigned ? "ASSIGNED" : "STOCK");
        model.addAttribute("types", AccessoryType.values());
        return "accessories/index";
    }

    @GetMapping("/new")
    public String newAccessory(Model model) {
        model.addAttribute("form", new AccessoryForm());
        model.addAttribute("types", AccessoryType.values());
        model.addAttribute("editing", false);
        return "accessories/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("form") AccessoryForm form, BindingResult result,
                         Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) return formError(null, model);
        try {
            var accessory = accessories.createStock(form.getType(), form.getBrand(), form.getModel(),
                    form.getSerialNumber(), form.getAsset(), form.getQuantity());
            redirect.addFlashAttribute("successMessage", form.getQuantity() == 1
                    ? "Accesorio agregado al stock." : form.getQuantity() + " accesorios agregados al stock.");
            return form.getQuantity() == 1 ? "redirect:/accessories/" + accessory.getId()
                    : "redirect:/accessories";
        } catch (IllegalArgumentException exception) {
            result.reject("accessory.invalid", exception.getMessage());
            return formError(null, model);
        } catch (DataIntegrityViolationException exception) {
            result.reject("accessory.duplicate", "Asset o número de serie ya registrado.");
            return formError(null, model);
        }
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("accessory", accessories.find(id));
        model.addAttribute("activeAssignment", accessories.active(id));
        model.addAttribute("history", accessories.history(id));
        return "accessories/detail";
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        model.addAttribute("form", AccessoryForm.from(accessories.find(id)));
        model.addAttribute("types", AccessoryType.values());
        model.addAttribute("editing", true);
        model.addAttribute("accessoryId", id);
        return "accessories/form";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("form") AccessoryForm form,
                         BindingResult result, Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) return formError(id, model);
        try {
            accessories.save(id, form.getType(), form.getBrand(), form.getModel(),
                    form.getSerialNumber(), form.getAsset());
            redirect.addFlashAttribute("successMessage", "Datos del accesorio actualizados.");
            return "redirect:/accessories/" + id;
        } catch (IllegalArgumentException exception) {
            result.reject("accessory.invalid", exception.getMessage());
            return formError(id, model);
        } catch (DataIntegrityViolationException exception) {
            result.reject("accessory.duplicate", "Asset o número de serie ya registrado.");
            return formError(id, model);
        }
    }

    private String formError(Long id, Model model) {
        model.addAttribute("types", AccessoryType.values());
        model.addAttribute("editing", id != null);
        model.addAttribute("accessoryId", id);
        return "accessories/form";
    }

    @GetMapping("/{id}/assign")
    public String assignment(@PathVariable Long id, Model model) {
        if (accessories.active(id) != null) return "redirect:/accessories/" + id;
        model.addAttribute("accessory", accessories.find(id));
        model.addAttribute("people", people.findAll());
        model.addAttribute("form", new AccessoryAssignForm());
        return "accessories/assign";
    }

    @PostMapping("/{id}/assign")
    public String assign(@PathVariable Long id, @Valid @ModelAttribute("form") AccessoryAssignForm form,
                         BindingResult result, Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) return assignmentError(id, model);
        try {
            accessories.assign(id, form.getPersonId(), form.getAssignedAt(), form.getAssignedBy());
            redirect.addFlashAttribute("successMessage", "Accesorio asignado.");
            return "redirect:/accessories/" + id;
        } catch (IllegalArgumentException | IllegalStateException | DataIntegrityViolationException exception) {
            result.reject("assignment.invalid", "No se pudo asignar. Verifica la persona y que siga en stock.");
            return assignmentError(id, model);
        }
    }

    private String assignmentError(Long id, Model model) {
        model.addAttribute("accessory", accessories.find(id));
        model.addAttribute("people", people.findAll());
        return "accessories/assign";
    }

    @GetMapping("/{id}/return")
    public String returnForm(@PathVariable Long id, Model model) {
        var active = accessories.active(id);
        if (active == null) return "redirect:/accessories/" + id;
        model.addAttribute("accessory", accessories.find(id));
        model.addAttribute("assignment", active);
        model.addAttribute("form", new AccessoryReturnForm());
        return "accessories/return";
    }

    @PostMapping("/{id}/return")
    public String returnAccessory(@PathVariable Long id,
                                  @Valid @ModelAttribute("form") AccessoryReturnForm form,
                                  BindingResult result, Model model, RedirectAttributes redirect) {
        var active = accessories.active(id);
        if (active == null) return "redirect:/accessories/" + id;
        if (result.hasErrors() || form.getReturnedAt().isBefore(active.getAssignedAt())) {
            if (!result.hasErrors()) result.rejectValue("returnedAt", "return.date", "La fecha debe ser posterior a la asignación.");
            model.addAttribute("accessory", accessories.find(id));
            model.addAttribute("assignment", active);
            return "accessories/return";
        }
        accessories.returnToStock(id, active.getId(), form.getReturnedAt(), form.getReceivedBy(), form.getNotes());
        redirect.addFlashAttribute("successMessage", "Accesorio devuelto al stock.");
        return "redirect:/accessories/" + id;
    }
}
