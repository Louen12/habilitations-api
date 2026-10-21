package fr.louenlegac.habilitations.security;

import fr.louenlegac.habilitations.domain.ActionDemande;
import fr.louenlegac.habilitations.domain.DemandeHabilitation;
import fr.louenlegac.habilitations.domain.Role;
import fr.louenlegac.habilitations.domain.StatutDemande;
import fr.louenlegac.habilitations.domain.Utilisateur;
import org.springframework.stereotype.Component;

/**
 * Règles d'autorisation au niveau d'une demande : équivalent direct d'un Voter Symfony
 * (supports + voteOnAttribute), isolé ici pour être testé unitairement.
 */
@Component
public class DemandeVoter {

    public boolean peutConsulter(DemandeHabilitation demande, Utilisateur utilisateur) {
        return demande.appartientA(utilisateur) || utilisateur.getRole() != Role.DEMANDEUR;
    }

    public boolean peutAppliquer(DemandeHabilitation demande, ActionDemande action, Utilisateur utilisateur) {
        if (action == ActionDemande.ANNULER) {
            return demande.appartientA(utilisateur);
        }
        // Séparation des tâches : personne ne valide sa propre demande.
        if (demande.appartientA(utilisateur)) {
            return false;
        }
        Role role = utilisateur.getRole();
        return switch (action) {
            case VALIDER -> role == Role.VALIDEUR || role == Role.ADMIN;
            case REFUSER -> demande.getStatut() == StatutDemande.VALIDEE_MANAGER
                    ? role == Role.ADMIN
                    : role == Role.VALIDEUR || role == Role.ADMIN;
            case ACCORDER -> role == Role.ADMIN;
            case ANNULER -> false; // traité plus haut
        };
    }
}
