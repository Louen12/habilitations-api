package fr.louenlegac.habilitations.web.dto;

import fr.louenlegac.habilitations.domain.Application;
import fr.louenlegac.habilitations.domain.DemandeHabilitation;
import fr.louenlegac.habilitations.domain.HistoriqueStatut;
import fr.louenlegac.habilitations.domain.StatutDemande;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.List;

/**
 * DTO d'entrée/sortie de l'API (records Java). On n'expose jamais les entités JPA directement,
 * comme on utiliserait des DTO + groupes de sérialisation côté Symfony.
 */
public final class Dtos {

    private Dtos() {
    }

    // ---------- Authentification ----------

    public record LoginRequest(@NotBlank @Email String email, @NotBlank String motDePasse) {
    }

    public record TokenResponse(String token, long expireDans, String role, String nomComplet) {
    }

    // ---------- Applications ----------

    public record CreerApplicationRequest(
            @NotBlank @Size(max = 50) String code,
            @NotBlank @Size(max = 255) String nom,
            @Size(max = 1000) String description) {
    }

    public record ApplicationResponse(Long id, String code, String nom, String description) {
        public static ApplicationResponse from(Application a) {
            return new ApplicationResponse(a.getId(), a.getCode(), a.getNom(), a.getDescription());
        }
    }

    // ---------- Demandes ----------

    public record CreerDemandeRequest(
            @NotNull Long applicationId,
            @NotBlank @Size(max = 50) String profil,
            @NotBlank @Size(max = 1000) String justification) {
    }

    public record ActionRequest(@Size(max = 500) String commentaire) {
    }

    public record DemandeResume(
            Long id,
            String applicationCode,
            String applicationNom,
            String demandeur,
            String profil,
            StatutDemande statut,
            Instant dateCreation,
            Instant dateMaj) {

        public static DemandeResume from(DemandeHabilitation d) {
            return new DemandeResume(d.getId(), d.getApplication().getCode(), d.getApplication().getNom(),
                    d.getDemandeur().getNomComplet(), d.getProfil(), d.getStatut(), d.getDateCreation(), d.getDateMaj());
        }
    }

    public record HistoriqueResponse(
            StatutDemande ancienStatut,
            StatutDemande nouveauStatut,
            String auteur,
            String commentaire,
            Instant date) {

        public static HistoriqueResponse from(HistoriqueStatut h) {
            return new HistoriqueResponse(h.getAncienStatut(), h.getNouveauStatut(),
                    h.getAuteur().getNomComplet(), h.getCommentaire(), h.getDateAction());
        }
    }

    public record DemandeDetail(
            Long id,
            ApplicationResponse application,
            String demandeur,
            String demandeurEmail,
            String profil,
            String justification,
            StatutDemande statut,
            Instant dateCreation,
            Instant dateMaj,
            List<HistoriqueResponse> historique) {

        public static DemandeDetail from(DemandeHabilitation d) {
            return new DemandeDetail(d.getId(), ApplicationResponse.from(d.getApplication()),
                    d.getDemandeur().getNomComplet(), d.getDemandeur().getEmail(), d.getProfil(),
                    d.getJustification(), d.getStatut(), d.getDateCreation(), d.getDateMaj(),
                    d.getHistorique().stream().map(HistoriqueResponse::from).toList());
        }
    }
}
