package com.assetcontrol.people.web;

import com.assetcontrol.people.application.DuplicatePersonIdentifierException;
import com.assetcontrol.people.application.DuplicatePersonUsernameException;
import com.assetcontrol.people.application.PersonProfileService;
import com.assetcontrol.people.application.PersonService;
import com.assetcontrol.people.domain.Person;
import com.assetcontrol.shared.web.InventoryOrdering;
import com.assetcontrol.shared.web.InventorySort;
import com.assetcontrol.shared.web.InventorySortDirection;
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

import java.util.Comparator;

@Controller
@RequestMapping("/people")
public class PersonController {

    private static final Comparator<Person> PERSON_ALPHABETICAL_ORDER = Comparator
            .comparing(Person::getFullName, String.CASE_INSENSITIVE_ORDER)
            .thenComparing(Person::getUsername, String.CASE_INSENSITIVE_ORDER)
            .thenComparing(Person::getId);
    private static final Comparator<Person> PERSON_CHRONOLOGICAL_ORDER = Comparator
            .comparing(Person::getCreatedAt)
            .thenComparing(Person::getId);

    private final PersonProfileService personProfileService;
    private final PersonService personService;

    public PersonController(
            PersonProfileService personProfileService,
            PersonService personService
    ) {
        this.personProfileService = personProfileService;
        this.personService = personService;
    }

    @GetMapping
    public String showPeople(
            @RequestParam(defaultValue = "") String query,
            @RequestParam(defaultValue = "ALPHABETICAL") InventorySort sort,
            @RequestParam(defaultValue = "ASCENDING") InventorySortDirection direction,
            Model model
    ) {
        model.addAttribute(
                "people",
                InventoryOrdering.apply(
                        personService.search(query),
                        PERSON_ALPHABETICAL_ORDER,
                        PERSON_CHRONOLOGICAL_ORDER,
                        sort,
                        direction
                )
        );
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
