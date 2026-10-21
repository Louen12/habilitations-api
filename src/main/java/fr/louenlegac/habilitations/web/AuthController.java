package fr.louenlegac.habilitations.web;

import fr.louenlegac.habilitations.domain.Utilisateur;
import fr.louenlegac.habilitations.exception.RessourceIntrouvableException;
import fr.louenlegac.habilitations.repository.UtilisateurRepository;
import fr.louenlegac.habilitations.security.TokenService;
import fr.louenlegac.habilitations.web.dto.Dtos.LoginRequest;
import fr.louenlegac.habilitations.web.dto.Dtos.TokenResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentification")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UtilisateurRepository utilisateurs;
    private final TokenService tokenService;

    public AuthController(AuthenticationManager authenticationManager, UtilisateurRepository utilisateurs,
                          TokenService tokenService) {
        this.authenticationManager = authenticationManager;
        this.utilisateurs = utilisateurs;
        this.tokenService = tokenService;
    }

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest requete) {
        authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(requete.email(), requete.motDePasse()));
        Utilisateur utilisateur = utilisateurs.findByEmail(requete.email())
                .orElseThrow(() -> new RessourceIntrouvableException("Utilisateur", requete.email()));
        return new TokenResponse(tokenService.generer(utilisateur), tokenService.dureeValiditeSecondes(),
                utilisateur.getRole().name(), utilisateur.getNomComplet());
    }
}
