package fr.louenlegac.habilitations.domain;

import fr.louenlegac.habilitations.exception.TransitionInvalideException;

/**
 * Machine à états du workflow de demande d'habilitation.
 *
 * <pre>
 * SOUMISE ──VALIDER──▶ VALIDEE_MANAGER ──ACCORDER──▶ ACCORDEE
 *    │                        │
 *    ├──REFUSER──▶ REFUSEE ◀──┘ REFUSER
 *    └──ANNULER──▶ ANNULEE
 * </pre>
 *
 * Équivalent du composant Workflow de Symfony, mais porté directement par le domaine.
 */
public enum StatutDemande {
    SOUMISE,
    VALIDEE_MANAGER,
    ACCORDEE,
    REFUSEE,
    ANNULEE;

    public StatutDemande appliquer(ActionDemande action) {
        return switch (this) {
            case SOUMISE -> switch (action) {
                case VALIDER -> VALIDEE_MANAGER;
                case REFUSER -> REFUSEE;
                case ANNULER -> ANNULEE;
                case ACCORDER -> throw new TransitionInvalideException(this, action);
            };
            case VALIDEE_MANAGER -> switch (action) {
                case ACCORDER -> ACCORDEE;
                case REFUSER -> REFUSEE;
                case VALIDER, ANNULER -> throw new TransitionInvalideException(this, action);
            };
            case ACCORDEE, REFUSEE, ANNULEE -> throw new TransitionInvalideException(this, action);
        };
    }

    public boolean estTerminal() {
        return this == ACCORDEE || this == REFUSEE || this == ANNULEE;
    }
}
