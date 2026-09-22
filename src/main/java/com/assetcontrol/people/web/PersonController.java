package com.assetcontrol.people.web;

import com.assetcontrol.accessories.application.AccessoryService;
import com.assetcontrol.people.application.DuplicatePersonIdentifierException;
import com.assetcontrol.people.application.DuplicatePersonUsernameException;
import com.assetcontrol.people.application.PersonProfileService;
import com.assetcontrol.people.application.PersonService;
import com.assetcontrol.people.domain.Person;
import com.assetcontrol.shared.web.InventoryPagination;
import com.assetcontrol.shared.web.InventorySort;
import com.assetcontrol.shared.web.InventorySortDirection;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
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
@RequestMapping("/people")
public class PersonController {

    private final PersonProfileService personProfileService;
    private final PersonService personService;
    private final AccessoryService accessoryService;

    public PersonController(
            PersonProfileService personProfileService,
            PersonService personService,
            AccessoryService accessoryService
    ) {
        this.personProfileService = personProfileService;
        this.personService = personService;
        this.accessoryService = accessoryService;
    }

    @GetMapping
    public String showPeople(
            @RequestParam(defaultValue = "") String query,
            @RequestParam(defaultValue = "ALPHABETICAL") InventorySort sort,
            @RequestParam(defaultValue = "ASCENDING") InventorySortDirection direction,
            @RequestParam(defaultValue = "1") int page,
            Model model
    ) {
        Page<Person> result = InventoryPagination.load(
                page,
                index -> personService.searchPage(
                        query,
                        sort == InventorySort.CHRONOLOGICAL,
                        direction == InventorySortDirection.DESCENDING,
                        index
                )
        );
        model.addAttribute("people", result.getContent());
        model.addAttribute("pagination", new InventoryPagination(result));
        model.addAttribute("query", query);
        model.addAttribute("selectedSort", sort);
        model.addAttribute("selectedDirection", direction);
        model.addAttribute("sortOptions", InventorySort.values());
        model.addAttribute("directionOptions", InventorySortDirection.values());
        return "people/index";
    }

    @GetMapping("/new")
    public String showNewPersonForm(Model model) {
        model.addAttribute("form", new PersonForm());
        return "people/new";
    }

    @PostMapping
    public String createPerson(
            @Valid @ModelAttribute("form") PersonForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            return "people/new";
        }

        try {
            var person = personService.create(form.toCreateCommand());
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Persona registrada correctamente."
            );
            return "redirect:/people/" + person.getId();
        } catch (DuplicatePersonIdentifierException exception) {
            bindingResult.rejectValue(
                    "externalId",
                    "person.externalId.duplicate",
                    exception.getMessage()
            );
        } catch (DuplicatePersonUsernameException exception) {
            bindingResult.rejectValue(
                    "username",
                    "person.username.duplicate",
                    exception.getMessage()
            );
        }

        return "people/new";
    }

    @GetMapping("/{personId}")
    public String showPersonProfile(@PathVariable Long personId, Model model) {
        model.addAttribute("profile", personProfileService.findByPersonId(personId));
        var accessoryHistory = accessoryService.historyForPerson(personId);
        model.addAttribute("accessoryHistory", accessoryHistory);
        model.addAttribute("activeAccessories", accessoryHistory.stream().filter(item -> item.isActive()).toList());

        return "people/detail";
    }

    @GetMapping("/{personId}/edit")
    public String showEditPersonForm(
            @PathVariable Long personId,
            Model model
    ) {
        model.addAttribute("personId", personId);
        model.addAttribute("form", PersonForm.from(personService.findById(personId)));
        return "people/edit";
    }

    @PostMapping("/{personId}")
    public String updatePerson(
            @PathVariable Long personId,
            @Valid @ModelAttribute("form") PersonForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("personId", personId);
            return "people/edit";
        }

        try {
            personService.update(personId, form.toUpdateCommand());
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Persona actualizada correctamente."
            );
            return "redirect:/people/" + personId;
        } catch (DuplicatePersonIdentifierException exception) {
            bindingResult.rejectValue(
                    "externalId",
                    "person.externalId.duplicate",
                    exception.getMessage()
            );
        } catch (DuplicatePersonUsernameException exception) {
            bindingResult.rejectValue(
                    "username",
                    "person.username.duplicate",
                    exception.getMessage()
            );
        }

        model.addAttribute("personId", personId);
        return "people/edit";
    }
}
