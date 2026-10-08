package fr.efrei.java;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Validation {

    private static final Logger logger = LoggerFactory.getLogger(Validation.class);

    private Validation() { }

    public static String texteObligatoire(String valeur, String champ) {
        if (valeur == null || valeur.isBlank() || valeur.isEmpty()) {
            logger.warn("Valeur vide refusée pour le champ {}", champ);
            throw new IllegalArgumentException("Le champ \"" + champ + "\" est obligatoire.");
        }
        return valeur.trim();
    }
}