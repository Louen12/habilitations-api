package fr.louenlegac.habilitations.repository;

import fr.louenlegac.habilitations.domain.DemandeHabilitation;
import fr.louenlegac.habilitations.domain.StatutDemande;
import fr.louenlegac.habilitations.domain.Utilisateur;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Les @EntityGraph chargent demandeur et application en une seule requête
 * (évite le problème N+1, équivalent d'un addSelect/join dans un QueryBuilder Doctrine).
 */
public interface DemandeRepository extends JpaRepository<DemandeHabilitation, Long> {

    @EntityGraph(attributePaths = {"demandeur", "application"})
    List<DemandeHabilitation> findAllByOrderByDateCreationDesc();

    @EntityGraph(attributePaths = {"demandeur", "application"})
    List<DemandeHabilitation> findByStatutOrderByDateCreationDesc(StatutDemande statut);

    @EntityGraph(attributePaths = {"demandeur", "application"})
    List<DemandeHabilitation> findByDemandeurOrderByDateCreationDesc(Utilisateur demandeur);

    @EntityGraph(attributePaths = {"demandeur", "application"})
    List<DemandeHabilitation> findByDemandeurAndStatutOrderByDateCreationDesc(Utilisateur demandeur, StatutDemande statut);
}
