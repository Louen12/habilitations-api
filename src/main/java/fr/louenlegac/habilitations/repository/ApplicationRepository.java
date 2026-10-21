package fr.louenlegac.habilitations.repository;

import fr.louenlegac.habilitations.domain.Application;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

    boolean existsByCode(String code);

    List<Application> findAllByOrderByNomAsc();
}
