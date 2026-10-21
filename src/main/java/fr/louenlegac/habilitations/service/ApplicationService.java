package fr.louenlegac.habilitations.service;

import fr.louenlegac.habilitations.domain.Application;
import fr.louenlegac.habilitations.exception.RegleMetierException;
import fr.louenlegac.habilitations.repository.ApplicationRepository;
import fr.louenlegac.habilitations.web.dto.Dtos.ApplicationResponse;
import fr.louenlegac.habilitations.web.dto.Dtos.CreerApplicationRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ApplicationService {

    private final ApplicationRepository applications;

    public ApplicationService(ApplicationRepository applications) {
        this.applications = applications;
    }

    public List<ApplicationResponse> lister() {
        return applications.findAllByOrderByNomAsc().stream().map(ApplicationResponse::from).toList();
    }

    @Transactional
    public ApplicationResponse creer(CreerApplicationRequest requete) {
        if (applications.existsByCode(requete.code())) {
            throw new RegleMetierException("Une application avec le code " + requete.code() + " existe déjà");
        }
        Application application = new Application(requete.code(), requete.nom(), requete.description());
        return ApplicationResponse.from(applications.save(application));
    }
}
