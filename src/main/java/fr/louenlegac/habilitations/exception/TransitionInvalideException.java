package fr.louenlegac.habilitations.exception;

import fr.louenlegac.habilitations.domain.ActionDemande;
import fr.louenlegac.habilitations.domain.StatutDemande;

public class TransitionInvalideException extends RuntimeException {

    public TransitionInvalideException(StatutDemande statut, ActionDemande action) {
        super("Action " + action + " impossible sur une demande au statut " + statut);
    }
}
