package fr.efrei.java;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class CollaborateurService {

    private static final Logger logger = LoggerFactory.getLogger(CollaborateurService.class);

    private final EntityManagerFactory fabrique;

    public CollaborateurService(EntityManagerFactory fabrique) {
        this.fabrique = fabrique;
    }

    // =========================================================================
    // Mission 6 — persist et find
    // =========================================================================

    public void ajouter(Collaborateur collaborateur) {
        // try (...) : l'EntityManager est fermé automatiquement à la fin du bloc
        try (EntityManager em = fabrique.createEntityManager()) {

            // Règle métier de la mission 2 : on vérifie le doublon AVANT d'écrire
            Collaborateur dejaPresent = em.find(Collaborateur.class, collaborateur.getIdentifiant());
            if (dejaPresent != null) {
                logger.warn("Doublon refusé pour {}", collaborateur.getIdentifiant());
                throw new CollaborateurDejaExistantException(collaborateur.getIdentifiant());
            }

            // Une écriture se fait toujours dans une transaction
            EntityTransaction transaction = em.getTransaction();
            transaction.begin();
            try {
                em.persist(collaborateur);
                transaction.commit();
                logger.info("Collaborateur {} enregistré en base", collaborateur.getIdentifiant());
            } catch (RuntimeException e) {
                transaction.rollback();
                logger.error("Échec de l'enregistrement de {}", collaborateur.getIdentifiant(), e);
                throw e;
            }
        }
    }

    public Collaborateur trouver(String identifiant) {
        try (EntityManager em = fabrique.createEntityManager()) {
            return em.find(Collaborateur.class, identifiant);
        }
    }

    public void initialiser(List<Collaborateur> collaborateurs) {
        try (EntityManager em = fabrique.createEntityManager()) {
            EntityTransaction transaction = em.getTransaction();
            transaction.begin();
            try {
                for (Collaborateur c : collaborateurs) {
                    if (em.find(Collaborateur.class, c.getIdentifiant()) == null) {
                        em.persist(c);
                        logger.info("Donnée de démonstration {} enregistrée", c.getIdentifiant());
                    } else {
                        logger.debug("Donnée de démonstration {} déjà présente, ignorée", c.getIdentifiant());
                    }
                }
                transaction.commit();
            } catch (RuntimeException e) {
                transaction.rollback();
                logger.error("Échec de l'initialisation des données de démonstration", e);
                throw e;
            }
        }
    }

    // =========================================================================
    // Mission 7 — modifier une entité gérée (pas d'UPDATE écrit à la main)
    // =========================================================================

    public Collaborateur augmenterSalaire(String identifiant, double pourcentage) {
        try (EntityManager em = fabrique.createEntityManager()) {
            EntityTransaction transaction = em.getTransaction();
            transaction.begin();
            try {
                // find dans la transaction : l'objet retourné est une entité GÉRÉE
                Collaborateur collaborateur = em.find(Collaborateur.class, identifiant);
                if (collaborateur == null) {
                    transaction.rollback();
                    return null;
                }

                // Simple méthode métier : ni persist, ni SQL
                // 7.3 : commenter la ligne suivante (et décommenter la ligne 7.3 dans HelloEfrei)
                collaborateur.augmenterSalaire(pourcentage);

                // 7.4 : décommenter la ligne suivante pour faire échouer l'opération avant le commit
                // throw new RuntimeException("Panne simulée (7.4)");

                transaction.commit(); // c'est ici que Hibernate envoie l'UPDATE
                return collaborateur;
            } catch (RuntimeException e) {
                transaction.rollback();
                throw e;
            }
        }
    }

    // =========================================================================
    // Mission 8 — requêtes JPQL
    // Dans une requête JPQL, "Collaborateur", "salaire", "nom"... sont des noms
    // de CLASSE et d'ATTRIBUTS Java, pas des noms de table ou de colonne.
    // =========================================================================

    public List<Collaborateur> salaireSuperieurA(double seuil) {
        try (EntityManager em = fabrique.createEntityManager()) {
            return em.createQuery(
                            "select c from Collaborateur c where c.salaire > :seuil order by c.salaire",
                            Collaborateur.class)
                    .setParameter("seuil", seuil) // jamais de concaténation avec la saisie
                    .getResultList();
        }
    }

    public List<Collaborateur> nomContenant(String fragment) {
        try (EntityManager em = fabrique.createEntityManager()) {
            return em.createQuery(
                            "select c from Collaborateur c where lower(c.nom) like :motif order by c.nom",
                            Collaborateur.class)
                    .setParameter("motif", "%" + fragment.toLowerCase() + "%")
                    .getResultList();
        }
    }

    public List<Collaborateur> tous() {
        return lister("select c from Collaborateur c order by c.identifiant");
    }

    public List<Collaborateur> triesParNom() {
        return lister("select c from Collaborateur c order by c.nom");
    }

    public List<Collaborateur> triesParSalaire() {
        return lister("select c from Collaborateur c order by c.salaire");
    }

    public List<Collaborateur> triesParNomPuisPrenom() {
        return lister("select c from Collaborateur c order by c.nom, c.prenom");
    }

    /**
     * Uniquement les programmeurs : il suffit d'interroger la sous-classe.
     * Hibernate ajoute lui-même le filtre sur la colonne "metier".
     */
    public List<Programmeur> programmeurs() {
        try (EntityManager em = fabrique.createEntityManager()) {
            return em.createQuery(
                            "select p from Programmeur p order by p.identifiant",
                            Programmeur.class)
                    .getResultList();
        }
    }

    /** Méthode privée mutualisée : exécute une requête JPQL sans paramètre. */
    private List<Collaborateur> lister(String jpql) {
        try (EntityManager em = fabrique.createEntityManager()) {
            return em.createQuery(jpql, Collaborateur.class).getResultList();
        }
    }
}
