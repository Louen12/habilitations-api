package fr.louenlegac.habilitations.security;

import fr.louenlegac.habilitations.domain.Utilisateur;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

/** Génère les JWT signés HS256 (équivalent de LexikJWTAuthenticationBundle). */
@Service
public class TokenService {

    private final JwtEncoder encoder;
    private final JwtProperties properties;

    public TokenService(JwtEncoder encoder, JwtProperties properties) {
        this.encoder = encoder;
        this.properties = properties;
    }

    public String generer(Utilisateur utilisateur) {
        Instant maintenant = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("habilitations-api")
                .issuedAt(maintenant)
                .expiresAt(maintenant.plus(properties.ttl()))
                .subject(utilisateur.getEmail())
                .claim("roles", List.of(utilisateur.getRole().name()))
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    public long dureeValiditeSecondes() {
        return properties.ttl().toSeconds();
    }
}
