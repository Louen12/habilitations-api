package fr.louenlegac.habilitations.domain;

import fr.louenlegac.habilitations.exception.TransitionInvalideException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StatutDemandeTest {

    @ParameterizedTest(name = "{0} + {1} -> {2}")
    @CsvSource({
            "SOUMISE, VALIDER, VALIDEE_MANAGER",
            "SOUMISE, REFUSER, REFUSEE",
            "SOUMISE, ANNULER, ANNULEE",
            "VALIDEE_MANAGER, ACCORDER, ACCORDEE",
            "VALIDEE_MANAGER, REFUSER, REFUSEE"
    })
    void transitionsAutorisees(StatutDemande depart, ActionDemande action, StatutDemande attendu) {
        assertThat(depart.appliquer(action)).isEqualTo(attendu);
    }

    @ParameterizedTest(name = "{0} + {1} interdit")
    @CsvSource({
            "SOUMISE, ACCORDER",
            "VALIDEE_MANAGER, VALIDER",
            "VALIDEE_MANAGER, ANNULER"
    })
    void transitionsInterdites(StatutDemande depart, ActionDemande action) {
        assertThatThrownBy(() -> depart.appliquer(action)).isInstanceOf(TransitionInvalideException.class);
    }

    @ParameterizedTest
    @EnumSource(value = StatutDemande.class, names = {"ACCORDEE", "REFUSEE", "ANNULEE"})
    void aucunActionPossibleSurUnStatutTerminal(StatutDemande terminal) {
        assertThat(terminal.estTerminal()).isTrue();
        for (ActionDemande action : ActionDemande.values()) {
            assertThatThrownBy(() -> terminal.appliquer(action)).isInstanceOf(TransitionInvalideException.class);
        }
    }

    @Test
    void lHistoriqueTraceChaqueTransition() {
        Utilisateur alice = new Utilisateur("alice@demo.fr", "x", "Alice", Role.DEMANDEUR);
        Utilisateur manager = new Utilisateur("manager@demo.fr", "x", "Marc", Role.VALIDEUR);
        DemandeHabilitation demande = new DemandeHabilitation(alice, new Application("GED", "GED", null), "LECTURE", "Besoin");

        demande.appliquer(ActionDemande.VALIDER, manager, "OK pour moi");

        assertThat(demande.getStatut()).isEqualTo(StatutDemande.VALIDEE_MANAGER);
        assertThat(demande.getHistorique()).hasSize(2);
        HistoriqueStatut derniere = demande.getHistorique().get(1);
        assertThat(derniere.getAncienStatut()).isEqualTo(StatutDemande.SOUMISE);
        assertThat(derniere.getNouveauStatut()).isEqualTo(StatutDemande.VALIDEE_MANAGER);
        assertThat(derniere.getAuteur()).isSameAs(manager);
        assertThat(derniere.getCommentaire()).isEqualTo("OK pour moi");
    }
}
