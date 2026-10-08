package fr.efrei.java;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "metier")
public abstract class Collaborateur {
    /*
    1 : plantage, donnée incohérente, règle métier violée ou comportement incorrect
    Collaborateur sansAdresse = new Testeur("C900", "Zoe", "Test", 30000, null);
    sansAdresse.afficherFiche();

    Annuaire test3 = new Annuaire();
    Adresse a3 = new Adresse("12 rue des Lilas", "75000", "Paris", "France");
    test3.ajouter(new Testeur("C901", "Zoe", null, 30000, a3));
    test3.nomContenant("mar");
    */
    private static final Logger logger = LoggerFactory.getLogger(Collaborateur.class);

    // Clé primaire (id)
    @Id
    @Column(length = 10)
    private String identifiant;

    @Column(nullable = false, length = 50)
    private String prenom;

    @Column(nullable = false, length = 50)
    private String nom;

    @Column(nullable = false)
    private double salaire;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "adresse_id", nullable = false)
    private Adresse adresse;

    protected Collaborateur() { }

    protected Collaborateur(
            String identifiant,
            String prenom,
            String nom,
            double salaire,
            Adresse adresse) {
        if (adresse == null) {
            logger.warn("Valeur vide refusée pour le champ adresse");
            throw new IllegalArgumentException("Le champ \"adresse\" est obligatoire.");
        }
        this.identifiant = Validation.texteObligatoire(identifiant, "identifiant");
        this.prenom = Validation.texteObligatoire(prenom, "prénom");
        this.nom = Validation.texteObligatoire(nom, "nom");
        this.salaire = salaire;
        this.adresse = adresse;
    }

    // --- Accesseurs ---

    public String getIdentifiant() { return identifiant; }
    public String getPrenom()      { return prenom; }
    public String getNom()         { return nom; }
    public double getSalaire()     { return salaire; }
    public Adresse getAdresse()    { return adresse; }

    // --- Comportements ---

    public void augmenterSalaire(double pourcentage) {
        double avant = salaire;
        if (pourcentage > 0) {
            salaire = salaire * (1 + pourcentage / 100);
        }
        logger.debug("Salaire : {} -> {}", avant, salaire); // détail pour l'analyse
    }

    public abstract String getMetier();

    public abstract void travailler();

    public void afficherFiche() {
        System.out.println("[" + identifiant + "] " + prenom + " " + nom);
        System.out.println("Métier  : " + getMetier());
        System.out.println("Salaire : " + salaire + " €");
        if (adresse != null) {
            System.out.println("Adresse :");
            adresse.afficher();
        }
    }

    // --- Contrat equals/hashCode basé sur l'identifiant (TP3) ---

    @Override
    public boolean equals(Object autre) {
        if (this == autre) return true;
        if (!(autre instanceof Collaborateur collaborateur)) return false;
        return identifiant.equals(collaborateur.identifiant);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifiant);
    }

    // meilleur affichage
    @Override
    public String toString() {
        return String.format("[%s] %s %s (%s) - %.2f €",
                identifiant, prenom, nom, getMetier(), salaire);
    }
}