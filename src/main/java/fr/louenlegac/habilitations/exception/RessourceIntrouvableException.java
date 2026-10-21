package fr.louenlegac.habilitations.exception;

public class RessourceIntrouvableException extends RuntimeException {

    public RessourceIntrouvableException(String ressource, Object id) {
        super(ressource + " introuvable : " + id);
    }
}
