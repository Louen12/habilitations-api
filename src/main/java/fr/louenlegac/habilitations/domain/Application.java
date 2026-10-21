package fr.louenlegac.habilitations.domain;

import jakarta.persistence.*;

/** Application métier sur laquelle on peut demander une habilitation. */
@Entity
@Table(name = "application")
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String nom;

    private String description;

    protected Application() {
    }

    public Application(String code, String nom, String description) {
        this.code = code;
        this.nom = nom;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getNom() {
        return nom;
    }

    public String getDescription() {
        return description;
    }
}
