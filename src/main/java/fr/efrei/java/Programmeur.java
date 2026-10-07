package fr.efrei.java;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 * fr.efrei.java.Programmeur : collaborateur spécialisé dans le développement logiciel.
 * Implémente fr.efrei.java.Formateur car un programmeur peut animer des formations internes.
 *
 * Reprise du TP2 avec ajout de l'identifiant (délégué à fr.efrei.java.Collaborateur).
 */
@Entity
@DiscriminatorValue("PROGRAMMEUR")
public class Programmeur extends Collaborateur implements Formateur {

    @Column(length = 30)
    private String langagePrefere;

    public Programmeur(
            String identifiant,
            String prenom,
            String nom,
            double salaire,
            Adresse adresse,
            String langagePrefere) {
        super(identifiant, prenom, nom, salaire, adresse);
        this.langagePrefere = langagePrefere;
    }

    protected Programmeur() {

    }

    public String getLangagePrefere() { return langagePrefere; }

    @Override
    public String getMetier() {
        return "fr.efrei.java.Programmeur (" + langagePrefere + ")";
    }

    @Override
    public void travailler() {
        System.out.println(getPrenom() + " développe une fonctionnalité en " + langagePrefere + ".");
    }

    @Override
    public void former() {
        System.out.println(getPrenom() + " anime une formation interne.");
    }
}
