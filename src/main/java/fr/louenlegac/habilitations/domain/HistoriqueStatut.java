package fr.louenlegac.habilitations.domain;

import jakarta.persistence.*;

import java.time.Instant;

/** Trace d'audit : qui a changé le statut d'une demande, quand, et pourquoi. */
@Entity
@Table(name = "historique_statut")
public class HistoriqueStatut {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "demande_id")
    private DemandeHabilitation demande;

    @Enumerated(EnumType.STRING)
    @Column(name = "ancien_statut")
    private StatutDemande ancienStatut;

    @Enumerated(EnumType.STRING)
    @Column(name = "nouveau_statut", nullable = false)
    private StatutDemande nouveauStatut;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "auteur_id")
    private Utilisateur auteur;

    private String commentaire;

    @Column(name = "date_action", nullable = false)
    private Instant dateAction;

    protected HistoriqueStatut() {
    }

    HistoriqueStatut(DemandeHabilitation demande, StatutDemande ancienStatut, StatutDemande nouveauStatut,
                     Utilisateur auteur, String commentaire) {
        this.demande = demande;
        this.ancienStatut = ancienStatut;
        this.nouveauStatut = nouveauStatut;
        this.auteur = auteur;
        this.commentaire = commentaire;
        this.dateAction = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public StatutDemande getAncienStatut() {
        return ancienStatut;
    }

    public StatutDemande getNouveauStatut() {
        return nouveauStatut;
    }

    public Utilisateur getAuteur() {
        return auteur;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public Instant getDateAction() {
        return dateAction;
    }
}
