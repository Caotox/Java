package fr.efrei.java;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Annuaire {

    private Map<String, Collaborateur> collaborateurs = new LinkedHashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(Annuaire.class);

    public boolean ajouter(Collaborateur collaborateur) {
        if (collaborateurs.containsKey(collaborateur.getIdentifiant())) {
            logger.warn("Doublon refusé pour {}", collaborateur.getIdentifiant()); // situation inhabituelle maîtrisée
            throw new CollaborateurDejaExistantException(collaborateur.getIdentifiant());
            //return false;
        }
        collaborateurs.put(collaborateur.getIdentifiant(), collaborateur);
        logger.info("Collaborateur {} ajouté", collaborateur.getIdentifiant()); // événement normal
        return true;
    }

    public Collaborateur trouver(String identifiant) {
        return collaborateurs.get(identifiant);
    }

    public boolean supprimer(String identifiant) {
        return collaborateurs.remove(identifiant) != null;
    }

    public int taille() {
        return collaborateurs.size();
    }

    public List<Collaborateur> tous() {
        return new ArrayList<>(collaborateurs.values());
    }

    // --- Filtres ---

    public List<Programmeur> programmeurs() {
        List<Programmeur> liste = new ArrayList<>();
        for (Collaborateur c : collaborateurs.values()) {
            if (c instanceof Programmeur programmeur) {
                liste.add(programmeur);
            }
        }
        return liste;
    }

    public List<Collaborateur> nomContenant(String fragment) {
        List<Collaborateur> liste = new ArrayList<>();
        for (Collaborateur c : collaborateurs.values()) {
            if (c.getNom().toLowerCase().contains(fragment.toLowerCase())) {
                liste.add(c);
            }
        }
        return liste;
    }

    public List<Collaborateur> salaireSuperieurA(double seuil) {
        List<Collaborateur> liste = new ArrayList<>();
        for (Collaborateur c : collaborateurs.values()) {
            if (c.getSalaire() > seuil) {
                liste.add(c);
            }
        }
        return liste;
    }

    // --- Tris ---

    public List<Collaborateur> triesParNom() {
        return triesSelon(Comparator.comparing(Collaborateur::getNom));
    }

    public List<Collaborateur> triesParSalaire() {
        return triesSelon(Comparator.comparingDouble(Collaborateur::getSalaire));
    }

    public List<Collaborateur> triesParNomPuisPrenom() {
        return triesSelon(
            Comparator.comparing(Collaborateur::getNom)
                      .thenComparing(Collaborateur::getPrenom)
        );
    }

    private List<Collaborateur> triesSelon(Comparator<Collaborateur> comparateur) {
        List<Collaborateur> liste = tous();
        liste.sort(comparateur);
        return Collections.unmodifiableList(liste);
    }
}
