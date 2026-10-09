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

    public void ajouter(Collaborateur collaborateur) {
        // try (...) : l'EntityManager est fermé automatiquement à la fin du bloc
        try (EntityManager em = fabrique.createEntityManager()) {

            // Règle métier de la mission 2 : on vérifie le doublon avant d'écrire
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

    public int initialiser(List<Collaborateur> collaborateurs) {
        int ajoutes = 0;
        try (EntityManager em = fabrique.createEntityManager()) {
            EntityTransaction transaction = em.getTransaction();
            transaction.begin();
            try {
                for (Collaborateur c : collaborateurs) {
                    if (em.find(Collaborateur.class, c.getIdentifiant()) == null) {
                        em.persist(c);
                        ajoutes++;
                        logger.info("Donnée de démonstration {} enregistrée", c.getIdentifiant());
                    } else {
                        logger.debug("Donnée de démonstration {} déjà présente, ignorée", c.getIdentifiant());
                    }
                }
                transaction.commit();
                return ajoutes;
            } catch (RuntimeException e) {
                transaction.rollback();
                logger.error("Échec de l'initialisation des données de démonstration", e);
                throw e;
            }
        }
    }

    public Collaborateur augmenterSalaire(String identifiant, double pourcentage) {
        try (EntityManager em = fabrique.createEntityManager()) {
            EntityTransaction transaction = em.getTransaction();
            transaction.begin();
            try {
                Collaborateur collaborateur = em.find(Collaborateur.class, identifiant);
                if (collaborateur == null) {
                    transaction.rollback();
                    return null;
                }

                // 7.3 : commenter
                collaborateur.augmenterSalaire(pourcentage);

                // 7.4
                // throw new RuntimeException("Panne simulée (7.4)");

                transaction.commit();
                return collaborateur;
            } catch (RuntimeException e) {
                transaction.rollback();
                throw e;
            }
        }
    }

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

    public List<Programmeur> programmeurs() {
        try (EntityManager em = fabrique.createEntityManager()) {
            return em.createQuery(
                            "select p from Programmeur p order by p.identifiant",
                            Programmeur.class)
                    .getResultList();
        }
    }

    private List<Collaborateur> lister(String jpql) {
        try (EntityManager em = fabrique.createEntityManager()) {
            return em.createQuery(jpql, Collaborateur.class).getResultList();
        }
    }
}
