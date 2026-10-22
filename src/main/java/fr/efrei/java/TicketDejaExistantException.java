package fr.efrei.java;

public class TicketDejaExistantException extends RuntimeException {
    private final Long identifiant;

    public TicketDejaExistantException(Long identifiant) {
        super("Le ticket avec l'dentifiant " + identifiant + " existe déjà.");
        this.identifiant = identifiant;
    }

    public Long identifiant() {
        return identifiant;
    }
}
