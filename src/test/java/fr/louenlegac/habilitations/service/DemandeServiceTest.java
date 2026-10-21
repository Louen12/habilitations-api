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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DemandeServiceTest {

    @Mock
    private DemandeRepository demandes;
    @Mock
    private ApplicationRepository applications;
    @Mock
    private UtilisateurRepository utilisateurs;

    private DemandeService service;

    private final Utilisateur alice = new Utilisateur("alice@demo.fr", "x", "Alice", Role.DEMANDEUR);
    private final Utilisateur manager = new Utilisateur("manager@demo.fr", "x", "Marc", Role.VALIDEUR);
    private final Application ged = new Application("GED", "GED", null);

    @BeforeEach
    void setUp() {
        service = new DemandeService(demandes, applications, utilisateurs, new DemandeVoter());
    }

    @Test
    void leManagerValideUneDemandeSoumise() {
        DemandeHabilitation demande = new DemandeHabilitation(alice, ged, "LECTURE", "Besoin");
        when(utilisateurs.findByEmail("manager@demo.fr")).thenReturn(Optional.of(manager));
        when(demandes.findById(1L)).thenReturn(Optional.of(demande));

        DemandeDetail resultat = service.appliquer(1L, ActionDemande.VALIDER, "OK", "manager@demo.fr");

        assertThat(resultat.statut()).isEqualTo(StatutDemande.VALIDEE_MANAGER);
        assertThat(resultat.historique()).hasSize(2);
    }

    @Test
    void unDemandeurNePeutPasValider() {
        DemandeHabilitation demande = new DemandeHabilitation(alice, ged, "LECTURE", "Besoin");
        when(utilisateurs.findByEmail("alice@demo.fr")).thenReturn(Optional.of(alice));
        when(demandes.findById(1L)).thenReturn(Optional.of(demande));

        assertThatThrownBy(() -> service.appliquer(1L, ActionDemande.VALIDER, null, "alice@demo.fr"))
                .isInstanceOf(AccessDeniedException.class);
        assertThat(demande.getStatut()).isEqualTo(StatutDemande.SOUMISE);
    }

    @Test
    void unRefusExigeUnCommentaire() {
        DemandeHabilitation demande = new DemandeHabilitation(alice, ged, "LECTURE", "Besoin");
        when(utilisateurs.findByEmail("manager@demo.fr")).thenReturn(Optional.of(manager));
        when(demandes.findById(1L)).thenReturn(Optional.of(demande));

        assertThatThrownBy(() -> service.appliquer(1L, ActionDemande.REFUSER, "  ", "manager@demo.fr"))
                .isInstanceOf(RegleMetierException.class);
        assertThat(demande.getStatut()).isEqualTo(StatutDemande.SOUMISE);
    }

    @Test
    void creationSurApplicationInconnue() {
        when(utilisateurs.findByEmail("alice@demo.fr")).thenReturn(Optional.of(alice));
        when(applications.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.creer(new CreerDemandeRequest(99L, "LECTURE", "Besoin"), "alice@demo.fr"))
                .isInstanceOf(RessourceIntrouvableException.class);
        verify(demandes, never()).save(any());
    }

    @Test
    void unDemandeurNeListeQueSesDemandes() {
        when(utilisateurs.findByEmail("alice@demo.fr")).thenReturn(Optional.of(alice));
        when(demandes.findByDemandeurOrderByDateCreationDesc(alice)).thenReturn(List.of());

        service.lister("alice@demo.fr", null);

        verify(demandes).findByDemandeurOrderByDateCreationDesc(alice);
        verify(demandes, never()).findAllByOrderByDateCreationDesc();
    }
}
