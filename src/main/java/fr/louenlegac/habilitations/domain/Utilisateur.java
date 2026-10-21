package fr.louenlegac.habilitations.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "utilisateur")
public class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "mot_de_passe", nullable = false)
    private String motDePasse;

    @Column(name = "nom_complet", nullable = false)
    private String nomComplet;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    protected Utilisateur() {
    }

    public Utilisateur(String email, String motDePasse, String nomComplet, Role role) {
        this.email = email;
        this.motDePasse = motDePasse;
        this.nomComplet = nomComplet;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getMotDePasse() {
        return motDePasse;
    }

    public String getNomComplet() {
        return nomComplet;
    }

    public Role getRole() {
        return role;
    }
}
