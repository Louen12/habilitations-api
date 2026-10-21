package fr.louenlegac.habilitations.service;

import fr.louenlegac.habilitations.domain.ActionDemande;
import fr.louenlegac.habilitations.domain.Application;
import fr.louenlegac.habilitations.domain.DemandeHabilitation;
import fr.louenlegac.habilitations.domain.Role;
import fr.louenlegac.habilitations.domain.StatutDemande;
import fr.louenlegac.habilitations.domain.Utilisateur;
import fr.louenlegac.habilitations.exception.RegleMetierException;
import fr.louenlegac.habilitations.exception.RessourceIntrouvableException;
import fr.louenlegac.habilitations.repository.ApplicationRepository;
import fr.louenlegac.habilitations.repository.DemandeRepository;
import fr.louenlegac.habilitations.repository.UtilisateurRepository;
import fr.louenlegac.habilitations.security.DemandeVoter;
import fr.louenlegac.habilitations.web.dto.Dtos.CreerDemandeRequest;
import fr.louenlegac.habilitations.web.dto.Dtos.DemandeDetail;
import fr.louenlegac.habilitations.web.dto.Dtos.DemandeResume;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Cas d'usage autour des demandes d'habilitation.
 * Le service orchestre (chargement, autorisation, persistance) ; les règles de transition
 * restent dans le domaine ({@link StatutDemande}) et les règles d'accès dans {@link DemandeVoter}.
 */
@Service
@Transactional(readOnly = true)
public class DemandeService {

    private final DemandeRepository demandes;
    private final ApplicationRepository applications;
    private final UtilisateurRepository utilisateurs;
    private final DemandeVoter voter;

    public DemandeService(DemandeRepository demandes, ApplicationRepository applications,
                          UtilisateurRepository utilisateurs, DemandeVoter voter) {
        this.demandes = demandes;
        this.applications = applications;
        this.utilisateurs = utilisateurs;
        this.voter = voter;
    }

    /** Un demandeur ne voit que ses demandes ; valideurs et admins voient tout. */
    public List<DemandeResume> lister(String email, StatutDemande statut) {
        Utilisateur utilisateur = utilisateur(email);
        List<DemandeHabilitation> resultat;
        if (utilisateur.getRole() == Role.DEMANDEUR) {
            resultat = statut == null
                    ? demandes.findByDemandeurOrderByDateCreationDesc(utilisateur)
                    : demandes.findByDemandeurAndStatutOrderByDateCreationDesc(utilisateur, statut);
        } else {
            resultat = statut == null
                    ? demandes.findAllByOrderByDateCreationDesc()
                    : demandes.findByStatutOrderByDateCreationDesc(statut);
        }
        return resultat.stream().map(DemandeResume::from).toList();
    }

    public DemandeDetail consulter(Long id, String email) {
        Utilisateur utilisateur = utilisateur(email);
        DemandeHabilitation demande = demande(id);
        if (!voter.peutConsulter(demande, utilisateur)) {
            throw new AccessDeniedException("Consultation refusée");
        }
        return DemandeDetail.from(demande);
    }

    @Transactional
    public DemandeDetail creer(CreerDemandeRequest requete, String email) {
        Utilisateur demandeur = utilisateur(email);
        Application application = applications.findById(requete.applicationId())
                .orElseThrow(() -> new RessourceIntrouvableException("Application", requete.applicationId()));
        DemandeHabilitation demande = new DemandeHabilitation(demandeur, application,
                requete.profil(), requete.justification());
        return DemandeDetail.from(demandes.save(demande));
    }

    @Transactional
    public DemandeDetail appliquer(Long id, ActionDemande action, String commentaire, String email) {
        Utilisateur utilisateur = utilisateur(email);
        DemandeHabilitation demande = demande(id);

        if (!voter.peutAppliquer(demande, action, utilisateur)) {
            throw new AccessDeniedException("Action " + action + " refusée");
        }
        if (action == ActionDemande.REFUSER && (commentaire == null || commentaire.isBlank())) {
            throw new RegleMetierException("Un commentaire est obligatoire pour refuser une demande");
        }

        demande.appliquer(action, utilisateur, commentaire);
        // Pas de save() explicite : l'entité est managée, le dirty checking flush en fin de transaction.
        return DemandeDetail.from(demande);
    }

    private Utilisateur utilisateur(String email) {
        return utilisateurs.findByEmail(email)
                .orElseThrow(() -> new RessourceIntrouvableException("Utilisateur", email));
    }

    private DemandeHabilitation demande(Long id) {
        return demandes.findById(id)
                .orElseThrow(() -> new RessourceIntrouvableException("Demande", id));
    }
}
