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

    private static final Logger logger = LoggerFactory.getLogger(Collaborateur.class);

    /** Clé primaire : l'identifiant métier (C001...). */
    @Id
    @Column(length = 10)
    private String identifiant;

    @Column(nullable = false, length = 50)
    private String prenom;

    @Column(nullable = false, length = 50)
    private String nom;

    @Column(nullable = false)
    private double salaire;

    /**
     * Mission 9 : relation vers Adresse.
     *
     * @ManyToOne  : PLUSIEURS collaborateurs peuvent avoir UNE même adresse.
     * cascade     : persister un collaborateur enregistre aussi son adresse si elle est nouvelle.
     * @JoinColumn : la clé étrangère est la colonne "adresse_id" de la table des collaborateurs.
     *               nullable = false : un collaborateur a toujours une adresse.
     */
    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "adresse_id", nullable = false)
    private Adresse adresse;

    /** Constructeur sans argument exigé par JPA. */
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
        double avant = salaire;
        if (pourcentage > 0) {
            salaire = salaire * (1 + pourcentage / 100);
        }
        logger.debug("Salaire : {} -> {}", avant, salaire); // détail pour l'analyse
    }

    /** Chaque sous-classe définit son propre métier (utilisé dans afficherFiche et toString). */
    public abstract String getMetier();

    /** Chaque sous-classe définit sa manière de travailler. */
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

    /** Représentation compacte sur une ligne (utilisée dans les listes). */
    @Override
    public String toString() {
        return String.format("[%s] %s %s (%s) - %.2f €",
                identifiant, prenom, nom, getMetier(), salaire);
    }
}
