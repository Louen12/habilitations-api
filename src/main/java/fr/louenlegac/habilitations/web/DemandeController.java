package fr.louenlegac.habilitations.web;

import fr.louenlegac.habilitations.domain.ActionDemande;
import fr.louenlegac.habilitations.domain.StatutDemande;
import fr.louenlegac.habilitations.service.DemandeService;
import fr.louenlegac.habilitations.web.dto.Dtos.ActionRequest;
import fr.louenlegac.habilitations.web.dto.Dtos.CreerDemandeRequest;
import fr.louenlegac.habilitations.web.dto.Dtos.DemandeDetail;
import fr.louenlegac.habilitations.web.dto.Dtos.DemandeResume;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/api/demandes")
@Tag(name = "Demandes d'habilitation")
public class DemandeController {

    private final DemandeService service;

    public DemandeController(DemandeService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Liste des demandes (les siennes pour un demandeur, toutes pour un valideur/admin)")
    public List<DemandeResume> lister(@RequestParam(required = false) StatutDemande statut,
                                      @AuthenticationPrincipal Jwt jwt) {
        return service.lister(jwt.getSubject(), statut);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Détail d'une demande avec son historique")
    public DemandeDetail consulter(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return service.consulter(id, jwt.getSubject());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Créer une demande (statut initial : SOUMISE)")
    public DemandeDetail creer(@Valid @RequestBody CreerDemandeRequest requete, @AuthenticationPrincipal Jwt jwt) {
        return service.creer(requete, jwt.getSubject());
    }

    @PostMapping("/{id}/{action:valider|accorder|refuser|annuler}")
    @Operation(summary = "Faire avancer le workflow (valider, accorder, refuser, annuler)")
    public DemandeDetail appliquer(@PathVariable Long id,
                                   @PathVariable String action,
                                   @Valid @RequestBody(required = false) ActionRequest requete,
                                   @AuthenticationPrincipal Jwt jwt) {
        ActionDemande actionDemande = ActionDemande.valueOf(action.toUpperCase(Locale.ROOT));
        String commentaire = requete == null ? null : requete.commentaire();
        return service.appliquer(id, actionDemande, commentaire, jwt.getSubject());
    }
}
