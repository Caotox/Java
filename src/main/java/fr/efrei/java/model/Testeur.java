package fr.efrei.java.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

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
        return "fr.efrei.java.model.Testeur";
    }

    @Override
    public void travailler() {
        System.out.println(getPrenom() + " exécute une campagne de tests.");
    }
    // En revanche, un Testeur peut valider un ticket
    @Override
    public boolean peutValiderUnTicket() {
        return true;
    }
}
