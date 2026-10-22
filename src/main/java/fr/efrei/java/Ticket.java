package fr.efrei.java;

import jakarta.persistence.*;

@Entity
@Table(name = "ticket")
public class Ticket {

    public static final int MAX_TICKETS_EN_COURS = 3;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titre;

    @Enumerated(EnumType.STRING)
    private Priorite priorite;

    @Enumerated(EnumType.STRING)
    private Statut statut;

    @ManyToOne
    @JoinColumn(name = "assigne_id")
    private Collaborateur assigne;

    protected Ticket() { }

    public Ticket(String titre, Priorite priorite) {
        this.titre = Validation.texteObligatoire(titre, "titre"); // refuse null, "" et "   "
        if (priorite == null) {
            throw new IllegalArgumentException("La priorité est obligatoire.");
        }
        this.priorite = priorite;
        this.statut = Statut.A_FAIRE;   // règle 1 : créé « À faire », sans responsable
    }

    public Long getId()               { return id; }
    public String getTitre()          { return titre; }
    public Priorite getPriorite()     { return priorite; }
    public Statut getStatut()         { return statut; }
    public Collaborateur getAssigne() { return assigne; }

    public void setTitre(String titre)          { this.titre = titre; }
    public void setPriorite(Priorite priorite)  { this.priorite = priorite; }

    public void demarrer(long ticketsEnCoursDuResponsable) {
        verifierStatut(Statut.A_FAIRE, "démarrer");
        if (assigne == null) {
            throw new OperationRefuseeException(
                    "Le ticket #" + id + " doit être assigné avant d'être démarré.");
        }
        if (ticketsEnCoursDuResponsable >= MAX_TICKETS_EN_COURS) {
            throw new OperationRefuseeException(assigne.getPrenom() + " a déjà "
                    + MAX_TICKETS_EN_COURS + " tickets en cours.");
        }
        statut = Statut.EN_COURS;
    }

    public void soumettre() {
        verifierStatut(Statut.EN_COURS, "soumettre à validation");
        statut = Statut.A_VALIDER;
    }

    public void valider(Collaborateur valideur) {
        verifierValideur(valideur);
        verifierStatut(Statut.A_VALIDER, "valider");
        statut = Statut.TERMINE;
    }

    public void renvoyer(Collaborateur valideur) {
        verifierValideur(valideur);
        verifierStatut(Statut.A_VALIDER, "renvoyer en cours");
        statut = Statut.EN_COURS;
    }

    public void verifierSuppressionPossible() {
        if (statut != Statut.A_FAIRE) {
            throw new OperationRefuseeException("Le ticket #" + id + " est " + statut
                    + " : seul un ticket A_FAIRE peut être supprimé.");
        }
    }


    private void verifierStatut(Statut attendu, String action) {
        if (statut != attendu) {
            throw new OperationRefuseeException("Impossible de " + action + " le ticket #" + id
                    + " : il est " + statut + " (il devrait être " + attendu + ").");
        }
    }

    private void verifierValideur(Collaborateur valideur) {
        if (!valideur.peutValiderUnTicket()) {
            throw new OperationRefuseeException(valideur.getPrenom()
                    + " n'est pas testeur : seul un testeur peut valider ou renvoyer un ticket.");
        }
    }

    @Override
    public String toString() {
        String responsable = (assigne == null)
                ? "non assigné"
                : assigne.getPrenom() + " " + assigne.getNom() + " [" + assigne.getIdentifiant() + "]";
        return "#" + id + "  " + priorite + "  " + titre + "  - " + statut + " - " + responsable;
    }
}