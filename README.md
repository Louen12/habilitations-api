# API Habilitations — Spring Boot

API REST de gestion des **demandes d'habilitations applicatives** : un utilisateur demande un accès à une application, son manager valide, un administrateur accorde. Chaque changement de statut est tracé.

Projet réalisé pour transposer en **Java / Spring Boot** une application que j'ai conçue et développée en **Symfony** pendant mon alternance (gestion des demandes et habilitations, workflow, RBAC, traçabilité).

## Stack

- Java 21, Spring Boot 3.5
- Spring Web, Spring Data JPA (Hibernate), Bean Validation
- Spring Security + JWT (OAuth2 Resource Server, HS256)
- PostgreSQL 16, migrations Flyway
- JUnit 5, Mockito, MockMvc, AssertJ (H2 en mode PostgreSQL pour les tests)
- OpenAPI / Swagger UI (springdoc)
- Docker Compose, GitHub Actions

## Fonctionnalités

- **Workflow** : `SOUMISE → VALIDEE_MANAGER → ACCORDEE`, avec refus possible à chaque étape et annulation par le demandeur.
- **RBAC** : rôles `DEMANDEUR`, `VALIDEUR`, `ADMIN`, plus des règles par demande (un demandeur ne voit que ses demandes, personne ne valide sa propre demande, seul l'admin accorde).
- **Traçabilité** : historique complet des transitions (auteur, date, ancien/nouveau statut, commentaire). Un refus exige un commentaire.
- **Erreurs normalisées** au format RFC 7807 (`ProblemDetail`) : 400, 401, 403, 404, 409.

## Lancer le projet

```bash
docker compose up --build
```

- API : http://localhost:8080
- Swagger UI : http://localhost:8080/swagger-ui.html

En local sans Docker pour l'API : `docker compose up db` puis `mvn spring-boot:run`.

Lancer les tests : `mvn verify`

### Comptes de démo (mot de passe : `password`)

| Email | Rôle |
|---|---|
| alice@demo.fr | DEMANDEUR |
| bob@demo.fr | DEMANDEUR |
| manager@demo.fr | VALIDEUR |
| admin@demo.fr | ADMIN |

## Endpoints

| Méthode | URL | Accès |
|---|---|---|
| POST | `/api/auth/login` | public |
| GET | `/api/applications` | authentifié |
| POST | `/api/applications` | ADMIN |
| GET | `/api/demandes?statut=` | authentifié (filtré selon le rôle) |
| GET | `/api/demandes/{id}` | propriétaire, VALIDEUR, ADMIN |
| POST | `/api/demandes` | authentifié |
| POST | `/api/demandes/{id}/valider` | VALIDEUR, ADMIN |
| POST | `/api/demandes/{id}/accorder` | ADMIN |
| POST | `/api/demandes/{id}/refuser` | VALIDEUR (si soumise), ADMIN |
| POST | `/api/demandes/{id}/annuler` | propriétaire |

Exemple :

```bash
TOKEN=$(curl -s -X POST localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"alice@demo.fr","motDePasse":"password"}' | jq -r .token)

curl -s localhost:8080/api/demandes -H "Authorization: Bearer $TOKEN"
```

## Architecture

```
domain/       Entités JPA + machine à états (StatutDemande) : les règles de transition vivent dans le domaine
repository/   Spring Data JPA (requêtes dérivées, @EntityGraph contre le N+1)
service/      Cas d'usage transactionnels
security/     Config Spring Security, génération JWT, DemandeVoter (règles d'accès par demande)
web/          Contrôleurs REST + DTO (records), jamais d'entité exposée
exception/    Exceptions métier et traduction en ProblemDetail
```

## Choix techniques : équivalences Symfony → Spring

| Symfony | Spring Boot (ce projet) |
|---|---|
| Conteneur de services, autowiring | Beans Spring, injection par constructeur |
| Doctrine ORM / entités | JPA / Hibernate (`@Entity`) |
| Doctrine Migrations | Flyway (`db/migration/V1__*.sql`) |
| Repository Doctrine / QueryBuilder | Spring Data JPA (méthodes dérivées, `@EntityGraph`) |
| `security.yaml` (firewall, access_control) | `SecurityFilterChain` dans `SecurityConfig` |
| LexikJWTAuthenticationBundle | OAuth2 Resource Server + `JwtEncoder` |
| Voter | `DemandeVoter` (règles par objet, testé unitairement) |
| `#[IsGranted('ROLE_ADMIN')]` | `@PreAuthorize("hasRole('ADMIN')")` |
| Composant Workflow | Enum `StatutDemande` (machine à états) |
| Validator (`#[Assert\NotBlank]`) | Bean Validation (`@NotBlank`, `@Valid`) |
| DataFixtures | `DonneesDemo` (`ApplicationRunner`) |
| ExceptionListener | `@RestControllerAdvice` + `ProblemDetail` |
| PHPUnit / WebTestCase | JUnit 5 + Mockito / MockMvc |

## Pistes d'évolution

- Front React (liste, création, actions du workflow)
- Pagination et recherche multicritère
- Tests d'intégration sur un vrai PostgreSQL avec Testcontainers
- Notifications (mail) lors des changements de statut
