package fr.louenlegac.habilitations.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Traduit les exceptions en réponses RFC 7807 (ProblemDetail).
 * Équivalent d'un ExceptionListener / kernel.exception en Symfony.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(RessourceIntrouvableException.class)
    ProblemDetail introuvable(RessourceIntrouvableException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(TransitionInvalideException.class)
    ProblemDetail transitionInvalide(TransitionInvalideException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(RegleMetierException.class)
    ProblemDetail regleMetier(RegleMetierException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    ProblemDetail accesRefuse(AccessDeniedException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, "Action non autorisée pour cet utilisateur");
    }

    @ExceptionHandler(AuthenticationException.class)
    ProblemDetail authentification(AuthenticationException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "Identifiants invalides");
    }
}
