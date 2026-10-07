package fr.efrei.java;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 * fr.efrei.java.Testeur : collaborateur spécialisé dans les tests logiciels.
 *
 * Reprise du TP2 avec ajout de l'identifiant (délégué à fr.efrei.java.Collaborateur).
 */
@Entity
@DiscriminatorValue("TESTEUR")
public class Testeur extends Collaborateur {

    public Testeur(
            String identifiant,
            String prenom,
            String nom,
            double salaire,
            Adresse adresse) {
        super(identifiant, prenom, nom, salaire, adresse);
    }

    protected Testeur() {

    }

    @Override
    public String getMetier() {
        return "fr.efrei.java.Testeur";
    }

    @Override
    public void travailler() {
        System.out.println(getPrenom() + " exécute une campagne de tests.");
    }
}
