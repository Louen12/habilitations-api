package fr.louenlegac.habilitations.domain;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Agrégat principal : une demande d'accès d'un utilisateur à une application.
 * Les changements de statut passent obligatoirement par {@link #appliquer},
 * qui garantit la cohérence du workflow et alimente l'historique (traçabilité).
 */
@Entity
@Table(name = "demande_habilitation")
public class DemandeHabilitation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "demandeur_id")
    private Utilisateur demandeur;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "application_id")
    private Application application;

    @Column(nullable = false)
    private String profil;

    @Column(nullable = false)
    private String justification;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutDemande statut;

    @Column(name = "date_creation", nullable = false)
    private Instant dateCreation;

    @Column(name = "date_maj", nullable = false)
    private Instant dateMaj;

    @OneToMany(mappedBy = "demande", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("dateAction ASC")
    private List<HistoriqueStatut> historique = new ArrayList<>();

    protected DemandeHabilitation() {
    }

    public DemandeHabilitation(Utilisateur demandeur, Application application, String profil, String justification) {
        this.demandeur = demandeur;
        this.application = application;
        this.profil = profil;
        this.justification = justification;
        this.statut = StatutDemande.SOUMISE;
        this.dateCreation = Instant.now();
        this.dateMaj = this.dateCreation;
        this.historique.add(new HistoriqueStatut(this, null, StatutDemande.SOUMISE, demandeur, "Création de la demande"));
    }

    /** Applique une action du workflow et trace le changement de statut. */
    public void appliquer(ActionDemande action, Utilisateur auteur, String commentaire) {
        StatutDemande ancien = this.statut;
        this.statut = ancien.appliquer(action);
        this.dateMaj = Instant.now();
        this.historique.add(new HistoriqueStatut(this, ancien, this.statut, auteur, commentaire));
    }

    public boolean appartientA(Utilisateur utilisateur) {
        return demandeur.getEmail().equals(utilisateur.getEmail());
    }

    public Long getId() {
        return id;
    }

    public Utilisateur getDemandeur() {
        return demandeur;
    }

    public Application getApplication() {
        return application;
    }

    public String getProfil() {
        return profil;
    }

    public String getJustification() {
        return justification;
    }

    public StatutDemande getStatut() {
        return statut;
    }

    public Instant getDateCreation() {
        return dateCreation;
    }

    public Instant getDateMaj() {
        return dateMaj;
    }

    public List<HistoriqueStatut> getHistorique() {
        return Collections.unmodifiableList(historique);
    }
}
