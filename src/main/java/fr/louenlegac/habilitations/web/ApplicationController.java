package fr.louenlegac.habilitations.web;

import fr.louenlegac.habilitations.service.ApplicationService;
import fr.louenlegac.habilitations.web.dto.Dtos.ApplicationResponse;
import fr.louenlegac.habilitations.web.dto.Dtos.CreerApplicationRequest;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/applications")
@Tag(name = "Applications")
public class ApplicationController {

    private final ApplicationService service;

    public ApplicationController(ApplicationService service) {
        this.service = service;
    }

    @GetMapping
    public List<ApplicationResponse> lister() {
        return service.lister();
    }

    /** Équivalent de #[IsGranted('ROLE_ADMIN')] en Symfony. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public ApplicationResponse creer(@Valid @RequestBody CreerApplicationRequest requete) {
        return service.creer(requete);
    }
}
