package fr.louenlegac.habilitations.domain;

public enum Role {
    /** Peut créer et annuler ses propres demandes. */
    DEMANDEUR,
    /** Manager : premier niveau de validation. */
    VALIDEUR,
    /** Administrateur des habilitations : validation finale. */
    ADMIN
}
