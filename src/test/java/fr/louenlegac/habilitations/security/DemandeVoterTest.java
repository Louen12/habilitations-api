package fr.louenlegac.habilitations.security;

import fr.louenlegac.habilitations.domain.ActionDemande;
import fr.louenlegac.habilitations.domain.Application;
import fr.louenlegac.habilitations.domain.DemandeHabilitation;
import fr.louenlegac.habilitations.domain.Role;
import fr.louenlegac.habilitations.domain.Utilisateur;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DemandeVoterTest {

    private final DemandeVoter voter = new DemandeVoter();

    private final Utilisateur alice = new Utilisateur("alice@demo.fr", "x", "Alice", Role.DEMANDEUR);
    private final Utilisateur bob = new Utilisateur("bob@demo.fr", "x", "Bob", Role.DEMANDEUR);
    private final Utilisateur manager = new Utilisateur("manager@demo.fr", "x", "Marc", Role.VALIDEUR);
    private final Utilisateur admin = new Utilisateur("admin@demo.fr", "x", "Anne", Role.ADMIN);

    private DemandeHabilitation demandeDe(Utilisateur demandeur) {
        return new DemandeHabilitation(demandeur, new Application("GED", "GED", null), "LECTURE", "Besoin");
    }

    @Test
    void unDemandeurNeVoitQueSesDemandes() {
        DemandeHabilitation demande = demandeDe(alice);
        assertThat(voter.peutConsulter(demande, alice)).isTrue();
        assertThat(voter.peutConsulter(demande, bob)).isFalse();
        assertThat(voter.peutConsulter(demande, manager)).isTrue();
    }

    @Test
    void seulLeProprietairePeutAnnuler() {
        DemandeHabilitation demande = demandeDe(alice);
        assertThat(voter.peutAppliquer(demande, ActionDemande.ANNULER, alice)).isTrue();
        assertThat(voter.peutAppliquer(demande, ActionDemande.ANNULER, admin)).isFalse();
    }

    @Test
    void separationDesTaches_onNeValidePasSaPropreDemande() {
        DemandeHabilitation demandeDuManager = demandeDe(manager);
        assertThat(voter.peutAppliquer(demandeDuManager, ActionDemande.VALIDER, manager)).isFalse();
    }

    @Test
    void validationManagerPuisAccordAdmin() {
        DemandeHabilitation demande = demandeDe(alice);
        assertThat(voter.peutAppliquer(demande, ActionDemande.VALIDER, alice)).isFalse();
        assertThat(voter.peutAppliquer(demande, ActionDemande.VALIDER, manager)).isTrue();
        assertThat(voter.peutAppliquer(demande, ActionDemande.ACCORDER, manager)).isFalse();
        assertThat(voter.peutAppliquer(demande, ActionDemande.ACCORDER, admin)).isTrue();
    }

    @Test
    void apresValidationManagerSeulLAdminPeutRefuser() {
        DemandeHabilitation demande = demandeDe(alice);
        demande.appliquer(ActionDemande.VALIDER, manager, null);

        assertThat(voter.peutAppliquer(demande, ActionDemande.REFUSER, manager)).isFalse();
        assertThat(voter.peutAppliquer(demande, ActionDemande.REFUSER, admin)).isTrue();
    }
}
