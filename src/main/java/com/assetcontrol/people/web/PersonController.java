package com.assetcontrol.people.web;

import com.assetcontrol.people.application.PersonProfileService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/people")
public class PersonController {

    private final PersonProfileService personProfileService;

    public PersonController(PersonProfileService personProfileService) {
        this.personProfileService = personProfileService;
    }

    @GetMapping("/{personId}")
    public String showPersonProfile(@PathVariable Long personId, Model model) {
        model.addAttribute("profile", personProfileService.findByPersonId(personId));

        return "people/detail";
    }
}
