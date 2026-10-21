package fr.louenlegac.habilitations.web;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests d'intégration de bout en bout : HTTP → sécurité JWT → service → JPA → H2 (migrations Flyway).
 * Chaque test est rollbacké grâce à @Transactional. Utilise les comptes de {@code DonneesDemo}.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class DemandeWorkflowIntegrationTest {

    @Autowired
    private MockMvc mvc;

    private String token(String email) throws Exception {
        String reponse = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "%s", "motDePasse": "password"}
                                """.formatted(email)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return "Bearer " + JsonPath.read(reponse, "$.token");
    }

    private long applicationGedId(String auth) throws Exception {
        String reponse = mvc.perform(get("/api/applications").header("Authorization", auth))
                .andReturn().getResponse().getContentAsString();
        List<Integer> ids = JsonPath.read(reponse, "$[?(@.code == 'GED')].id");
        return ids.get(0).longValue();
    }

    private long creerDemande(String auth) throws Exception {
        String reponse = mvc.perform(post("/api/demandes")
                        .header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"applicationId": %d, "profil": "ECRITURE", "justification": "Nouvelle mission"}
                                """.formatted(applicationGedId(auth))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statut").value("SOUMISE"))
                .andReturn().getResponse().getContentAsString();
        return ((Integer) JsonPath.read(reponse, "$.id")).longValue();
    }

    @Test
    void sansTokenL_apiRepond401() throws Exception {
        mvc.perform(get("/api/demandes")).andExpect(status().isUnauthorized());
    }

    @Test
    void mauvaisMotDePasseRepond401() throws Exception {
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"alice@demo.fr\", \"motDePasse\": \"faux\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void workflowCompletSoumiseValideeAccordee() throws Exception {
        String alice = token("alice@demo.fr");
        long id = creerDemande(alice);

        mvc.perform(post("/api/demandes/{id}/valider", id).header("Authorization", token("manager@demo.fr")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("VALIDEE_MANAGER"));

        mvc.perform(post("/api/demandes/{id}/accorder", id)
                        .header("Authorization", token("admin@demo.fr"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"commentaire\": \"Droits ouverts\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("ACCORDEE"));

        mvc.perform(get("/api/demandes/{id}", id).header("Authorization", alice))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.historique", hasSize(3)))
                .andExpect(jsonPath("$.historique[2].auteur").value("Anne Admin"))
                .andExpect(jsonPath("$.historique[2].commentaire").value("Droits ouverts"));
    }

    @Test
    void unDemandeurNePeutPasValider() throws Exception {
        String alice = token("alice@demo.fr");
        long id = creerDemande(alice);

        mvc.perform(post("/api/demandes/{id}/valider", id).header("Authorization", token("bob@demo.fr")))
                .andExpect(status().isForbidden());
    }

    @Test
    void unDemandeurNeVoitPasLesDemandesDesAutres() throws Exception {
        long id = creerDemande(token("alice@demo.fr"));

        mvc.perform(get("/api/demandes/{id}", id).header("Authorization", token("bob@demo.fr")))
                .andExpect(status().isForbidden());
    }

    @Test
    void accorderUneDemandeNonValideeRepond409() throws Exception {
        long id = creerDemande(token("alice@demo.fr"));

        mvc.perform(post("/api/demandes/{id}/accorder", id).header("Authorization", token("admin@demo.fr")))
                .andExpect(status().isConflict());
    }

    @Test
    void refuserSansCommentaireRepond400() throws Exception {
        long id = creerDemande(token("alice@demo.fr"));

        mvc.perform(post("/api/demandes/{id}/refuser", id).header("Authorization", token("manager@demo.fr")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void seulUnAdminPeutCreerUneApplication() throws Exception {
        String corps = "{\"code\": \"ERP\", \"nom\": \"ERP\", \"description\": \"Progiciel\"}";

        mvc.perform(post("/api/applications").header("Authorization", token("manager@demo.fr"))
                        .contentType(MediaType.APPLICATION_JSON).content(corps))
                .andExpect(status().isForbidden());

        mvc.perform(post("/api/applications").header("Authorization", token("admin@demo.fr"))
                        .contentType(MediaType.APPLICATION_JSON).content(corps))
                .andExpect(status().isCreated());
    }
}
