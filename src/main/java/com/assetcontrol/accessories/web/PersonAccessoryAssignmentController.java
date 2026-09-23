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
@RequestMapping("/people/{personId}/accessories")
public class PersonAccessoryAssignmentController {

    private final AccessoryService accessories;
    private final PersonService people;

    public PersonAccessoryAssignmentController(AccessoryService accessories, PersonService people) {
        this.accessories = accessories;
        this.people = people;
    }

    @GetMapping("/assign")
    public String showForm(@PathVariable Long personId,
                           @RequestParam(defaultValue = "") String query,
                           @RequestParam(required = false) AccessoryType type,
                           Model model) {
        model.addAttribute("form", new PersonAccessoryForm());
        prepareOptions(personId, query, type, model);
        return "accessories/assign-person";
    }

    @PostMapping("/assign")
    public String assign(@PathVariable Long personId,
                         @RequestParam(defaultValue = "") String query,
                         @RequestParam(required = false) AccessoryType type,
                         @Valid @ModelAttribute("form") PersonAccessoryForm form,
                         BindingResult errors,
                         Model model,
                         RedirectAttributes redirect) {
        if (errors.hasErrors()) {
            prepareOptions(personId, query, type, model);
            return "accessories/assign-person";
        }
        try {
            accessories.assign(form.getAccessoryId(), personId, form.getAssignedAt(), form.getAssignedBy());
        } catch (IllegalArgumentException | IllegalStateException | DataIntegrityViolationException exception) {
            errors.reject("accessory.unavailable", "Ese accesorio ya no está disponible. Selecciona otro del stock.");
            prepareOptions(personId, query, type, model);
            return "accessories/assign-person";
        }
        redirect.addFlashAttribute("successMessage", "Accesorio asignado a la persona.");
        return "redirect:/people/" + personId + "#accesorios";
    }

    private void prepareOptions(Long personId, String query, AccessoryType type, Model model) {
        model.addAttribute("person", people.findById(personId));
        var matches = accessories.inventory(query, type, false).items();
        model.addAttribute("stock", matches.stream().limit(50).toList());
        model.addAttribute("stockCount", matches.size());
        model.addAttribute("query", query);
        model.addAttribute("type", type);
        model.addAttribute("types", AccessoryType.values());
    }
}
