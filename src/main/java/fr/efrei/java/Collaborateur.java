package fr.efrei.java;

import jakarta.persistence.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * Classe abstraite représentant un collaborateur de l'entreprise.
 *
 * TP3 : extension du TP2 avec l'ajout d'un identifiant unique,
 * du contrat equals()/hashCode() basé sur cet identifiant,
 * et d'une méthode abstraite getMetier() pour les affichages polymorphes.
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "metier")
public abstract class Collaborateur {

    @Id
    @Column(length = 10)
    private String identifiant;

    @Column(nullable = false, length = 50)
    private String prenom;

    @Column(nullable = false, length = 50)
    private String nom;

    @Column(nullable = false)
    private double salaire;

    @Transient
    private Adresse adresse;

    private static final Logger logger = LoggerFactory.getLogger(Collaborateur.class);

    protected Collaborateur() { }
    protected Collaborateur(
            String identifiant,
            String prenom,
            String nom,
            double salaire,
            Adresse adresse) {
        this.identifiant = identifiant;
        this.prenom = prenom;
        this.nom = nom;
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

    /** Augmente le salaire du pourcentage donné. Ignoré si le pourcentage est négatif ou nul. */
    public void augmenterSalaire(double pourcentage) {
        if (pourcentage > 0) {
            salaire = salaire * (1 + pourcentage / 100);
        }
        logger.debug("Salaire : {} -> {}", getSalaire(), this.salaire); // détail pour l'analyse
    }

    /** Chaque sous-classe définit son propre métier (utilisé dans afficherFiche et toString). */
    public abstract String getMetier();

    /** Chaque sous-classe définit sa manière de travailler. */
    public abstract void travailler();

    /**
     * Affichage détaillé de la fiche du collaborateur (multiligne).
     * Les sous-classes peuvent surcharger pour ajouter leurs propres informations.
     */
    public void afficherFiche() {
        System.out.println("[" + identifiant + "] " + prenom + " " + nom);
        System.out.println("Métier  : " + getMetier());
        System.out.println("Salaire : " + salaire + " €");
        System.out.println("fr.efrei.java.Adresse :");
        adresse.afficher();
    }

    // --- Contrat equals/hashCode basé sur l'identifiant (TP3) ---

    /**
     * Deux collaborateurs sont logiquement égaux s'ils partagent le même identifiant.
     * Cela permet un comportement correct dans les collections (Set, Map, contains...).
     */
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

    /** Représentation compacte sur une ligne (utilisée dans les listes). */
    @Override
    public String toString() {
        return String.format("[%s] %s %s (%s) - %.2f €",
                identifiant, prenom, nom, getMetier(), salaire);
    }
}
