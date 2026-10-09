package fr.efrei.java;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

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
        this.langagePrefere = Validation.texteObligatoire(langagePrefere, "langage préféré");
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