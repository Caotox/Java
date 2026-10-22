package fr.efrei.java;

// Quand une règle de gestion refuse l'opération demandée
public class OperationRefuseeException extends RuntimeException {
    public OperationRefuseeException(String message) {
        super(message);
    }
}