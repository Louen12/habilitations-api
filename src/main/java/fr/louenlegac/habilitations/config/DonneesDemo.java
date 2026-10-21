package fr.louenlegac.habilitations.config;

import fr.louenlegac.habilitations.domain.Application;
import fr.louenlegac.habilitations.domain.DemandeHabilitation;
import fr.louenlegac.habilitations.domain.Role;
import fr.louenlegac.habilitations.domain.Utilisateur;
import fr.louenlegac.habilitations.repository.ApplicationRepository;
import fr.louenlegac.habilitations.repository.DemandeRepository;
import fr.louenlegac.habilitations.repository.UtilisateurRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Jeu de données de démonstration (équivalent des DataFixtures Doctrine).
 * Mot de passe de tous les comptes : "password".
 */
@Component
@ConditionalOnProperty(name = "app.demo-data", havingValue = "true", matchIfMissing = true)
public class DonneesDemo implements ApplicationRunner {

    static final String MOT_DE_PASSE = "password";

    private final UtilisateurRepository utilisateurs;
    private final ApplicationRepository applications;
    private final DemandeRepository demandes;
    private final PasswordEncoder encoder;

    public DonneesDemo(UtilisateurRepository utilisateurs, ApplicationRepository applications,
                       DemandeRepository demandes, PasswordEncoder encoder) {
        this.utilisateurs = utilisateurs;
        this.applications = applications;
        this.demandes = demandes;
        this.encoder = encoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (utilisateurs.count() > 0) {
            return;
        }
        String hash = encoder.encode(MOT_DE_PASSE);
        Utilisateur alice = utilisateurs.save(new Utilisateur("alice@demo.fr", hash, "Alice Martin", Role.DEMANDEUR));
        utilisateurs.save(new Utilisateur("bob@demo.fr", hash, "Bob Durand", Role.DEMANDEUR));
        utilisateurs.save(new Utilisateur("manager@demo.fr", hash, "Marc Le Goff", Role.VALIDEUR));
        utilisateurs.save(new Utilisateur("admin@demo.fr", hash, "Anne Admin", Role.ADMIN));

        Application ged = applications.save(new Application("GED", "Gestion électronique de documents", "Stockage et partage des documents"));
        applications.save(new Application("PAIE", "Logiciel de paie", "Bulletins et déclarations sociales"));
        applications.save(new Application("CRM", "Relation client", "Suivi des contacts et opportunités"));

        demandes.save(new DemandeHabilitation(alice, ged, "LECTURE", "Consultation des procédures du service"));
    }
}
