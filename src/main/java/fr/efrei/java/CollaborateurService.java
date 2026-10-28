package fr.efrei.java;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class CollaborateurService {

    private static final Logger logger = LoggerFactory.getLogger(CollaborateurService.class);

    private final EntityManagerFactory fabrique;

    public CollaborateurService(EntityManagerFactory fabrique) {
        this.fabrique = fabrique;
    }

    private <T> T transactionAvecResultat(Function<EntityManager, T> travail) {
        try (EntityManager em = fabrique.createEntityManager()) {
            EntityTransaction transaction = em.getTransaction();
            transaction.begin();
            try {
                T resultat = travail.apply(em);
                transaction.commit();
                return resultat;
            } catch (RuntimeException e) {
                if (transaction.isActive()) {
                    transaction.rollback();
                    logger.warn("Transaction annulée (rollback) : {}", e.getMessage());
                }
                throw e;
            }
        }
    }

    private void dansUneTransaction(Consumer<EntityManager> travail) {
        transactionAvecResultat(em -> {
            travail.accept(em);
            return null;
        });
    }

//    public void ajouterUnCollaborateur(Collaborateur collaborateur) {
//        // try (...) : l'EntityManager est fermé automatiquement à la fin du bloc
//        try (EntityManager em = fabrique.createEntityManager()) {
//
//            // Règle métier de la mission 2 : on vérifie le doublon avant d'écrire
//            Collaborateur dejaPresent = em.find(Collaborateur.class, collaborateur.getIdentifiant());
//            if (dejaPresent != null) {
//                logger.warn("Doublon refusé pour {}", collaborateur.getIdentifiant());
//                throw new CollaborateurDejaExistantException(collaborateur.getIdentifiant());
//            }
//
//            // Une écriture se fait toujours dans une transaction
//            EntityTransaction transaction = em.getTransaction();
//            transaction.begin();
//            try {
//                em.persist(collaborateur);
//                transaction.commit();
//                logger.info("Collaborateur {} enregistré en base", collaborateur.getIdentifiant());
//            } catch (RuntimeException e) {
//                transaction.rollback();
//                logger.error("Échec de l'enregistrement de {}", collaborateur.getIdentifiant(), e);
//                throw e;
//            }
//        }
//    }
    public void ajouterUnCollaborateur(Collaborateur collaborateur) {
        // Règle métier du doublon : on vérifie avant d'écrire
        if (trouverUnCollaborateur(collaborateur.getIdentifiant()) != null) {
            logger.warn("Doublon refusé pour {}", collaborateur.getIdentifiant());
            throw new CollaborateurDejaExistantException(collaborateur.getIdentifiant());
        }
        dansUneTransaction(em -> em.persist(collaborateur));
        logger.info("Collaborateur {} enregistré en base", collaborateur.getIdentifiant());
    }

    public Collaborateur trouverUnCollaborateur(String identifiant) {
        try (EntityManager em = fabrique.createEntityManager()) {
            return chercherParIdentifiant(em, identifiant);
        }
    }

    private Collaborateur chercherParIdentifiant(EntityManager em, String identifiant) {
        List<Collaborateur> resultat = em.createQuery(
                        "select c from Collaborateur c where c.identifiant = :identifiant",
                        Collaborateur.class)
                .setParameter("identifiant", identifiant) // jamais de concaténation avec la saisie
                .getResultList();
        return resultat.isEmpty() ? null : resultat.get(0);
    }

//    public int initialiserLesCollaborateurs(List<Collaborateur> collaborateurs) {
//        int ajoutes = 0;
//        try (EntityManager em = fabrique.createEntityManager()) {
//            EntityTransaction transaction = em.getTransaction();
//            transaction.begin();
//            try {
//                for (Collaborateur c : collaborateurs) {
//                    if (em.find(Collaborateur.class, c.getIdentifiant()) == null) {
//                        em.persist(c);
//                        ajoutes++;
//                        logger.info("Donnée de démonstration {} enregistrée", c.getIdentifiant());
//                    } else {
//                        logger.debug("Donnée de démonstration {} déjà présente, ignorée", c.getIdentifiant());
//                    }
//                }
//                transaction.commit();
//                return ajoutes;
//            } catch (RuntimeException e) {
//                transaction.rollback();
//                logger.error("Échec de l'initialisation des données de démonstration", e);
//                throw e;
//            }
//        }
//    }

    public int initialiserLesCollaborateurs(List<Collaborateur> collaborateurs) {
        return transactionAvecResultat(em -> {
            int ajoutes = 0;
            for (Collaborateur c : collaborateurs) {
                if (chercherParIdentifiant(em, c.getIdentifiant()) == null) {
                    em.persist(c);
                    ajoutes++;
                    logger.info("Donnée de démonstration {} enregistrée", c.getIdentifiant());
                } else {
                    logger.debug("Donnée de démonstration {} déjà présente, ignorée", c.getIdentifiant());
                }
            }
            return ajoutes;
        });
    }

//    public Collaborateur augmenterSalaire(String identifiant, double pourcentage) {
//        try (EntityManager em = fabrique.createEntityManager()) {
//            EntityTransaction transaction = em.getTransaction();
//            transaction.begin();
//            try {
//                Collaborateur collaborateur = em.find(Collaborateur.class, identifiant);
//                if (collaborateur == null) {
//                    transaction.rollback();
//                    return null;
//                }
//
//                // 7.3 : commenter
//                collaborateur.augmenterSalaire(pourcentage);
//
//                // 7.4
//                // throw new RuntimeException("Panne simulée (7.4)");
//
//                transaction.commit();
//                return collaborateur;
//            } catch (RuntimeException e) {
//                transaction.rollback();
//                throw e;
//            }
//        }
//    }

    public Collaborateur augmenterSalaire(String identifiant, double pourcentage) {
        return transactionAvecResultat(em -> {
            Collaborateur collaborateur = chercherParIdentifiant(em, identifiant);
            if (collaborateur == null) {
                return null;
            }

            // 7.3 : commenter
            collaborateur.augmenterSalaire(pourcentage);

            // 7.4
            // throw new RuntimeException("Panne simulée (7.4)");

            return collaborateur;
        });
    }

    public void ajouterDeux(Collaborateur premier, Collaborateur second, boolean provoquerPanne) {
        dansUneTransaction(em -> {
            em.persist(premier);
            logger.info("Test rollback : {} persisté (pas encore validé)", premier.getIdentifiant());
            if (provoquerPanne) {
                throw new IllegalStateException("Panne simulée entre les deux ajouts");
            }
            em.persist(second);
            logger.info("Test rollback : {} persisté (pas encore validé)", second.getIdentifiant());
        });
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

    public List<Collaborateur> collaborateursTriesParId() {
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

    public List<Programmeur> programmeursDuLangage(String langage) {
        try (EntityManager em = fabrique.createEntityManager()) {
            return em.createQuery(
                            "select p from Programmeur p "
                                    + "where lower(p.langagePrefere) = :langage "
                                    + "order by p.nom, p.prenom",
                            Programmeur.class)
                    .setParameter("langage", langage.trim().toLowerCase())
                    .getResultList();
        }
    }

    public Ticket trouverUnTicket(Long identifiant) {
        try (EntityManager em = fabrique.createEntityManager()) {
            return chercherParIdentifiant(em, identifiant);
        }
    }

    private Ticket chercherParIdentifiant(EntityManager em, Long identifiant) {
        List<Ticket> resultat = em.createQuery(
                        "select t from Ticket t where t.id = :identifiant",
                        Ticket.class)
                .setParameter("identifiant", identifiant) // jamais de concaténation avec la saisie
                .getResultList();
        return resultat.isEmpty() ? null : resultat.get(0);
    }

    public void creerTicket(Ticket ticket){
        if (trouverUnTicket(ticket.getId()) != null) {
            logger.warn("Doublon refusé pour {}", ticket.getId());
            throw new TicketDejaExistantException(ticket.getId());
        }
        dansUneTransaction(em -> em.persist(ticket));
        logger.info("Ticket {} enregistré en base", ticket.getId());
    }

    public List<Ticket> consulterAllTickets() {
        try (EntityManager em = fabrique.createEntityManager()) {
            return em.createQuery(
                            "select t from Ticket t order by t.id",
                            Ticket.class)
                    .getResultList();
        }
    }

    public List<Ticket> consulterTicketByProgrammeur(String identifiantProgrammeur) {
        if (identifiantProgrammeur == null || identifiantProgrammeur.isBlank()) {
            throw new IllegalArgumentException("L'identifiant du programmeur est obligatoire");
        }
        try (EntityManager em = fabrique.createEntityManager()) {
            collaborateurExistant(em, identifiantProgrammeur);

            return em.createQuery(
                            "select t from Ticket t "
                                    + "where t.assigne.identifiant = :identifiant "
                                    + "order by t.id",
                            Ticket.class)
                    .setParameter("identifiant", identifiantProgrammeur)
                    .getResultList();
        }
    }

    public List<Ticket> consulterTicketByStatut(Statut statut) {
        try (EntityManager em = fabrique.createEntityManager()) {
            return em.createQuery(
                            "select t from Ticket t where t.statut = :statut order by t.id",
                            Ticket.class)
                    .setParameter("statut", statut)
                    .getResultList();
        }
    }

    public Ticket demarrerTicket(Long idTicket) {
        return transactionAvecResultat(em -> {
            Ticket ticket = ticketExistant(em, idTicket);
            long enCours = 0;
            if (ticket.getAssigne() != null) {
                enCours = compterTicketsEnCours(em, ticket.getAssigne());
            }
            ticket.demarrer(enCours);
            logger.info("Ticket {} démarré", idTicket);
            return ticket;
        });
    }

    public Ticket soumettreTicket(Long idTicket) {
        return transactionAvecResultat(em -> {
            Ticket ticket = ticketExistant(em, idTicket);
            ticket.soumettre();
            logger.info("Ticket {} soumis à validation", idTicket);
            return ticket;
        });
    }

    public Ticket validerTicket(Long idTicket, String identifiantTesteur) {
        return transactionAvecResultat(em -> {
            Ticket ticket = ticketExistant(em, idTicket);
            Collaborateur valideur = collaborateurExistant(em, identifiantTesteur);
            ticket.valider(valideur);
            logger.info("Ticket {} validé par {}", idTicket, identifiantTesteur);
            return ticket;
        });
    }

    public Ticket renvoyerTicket(Long idTicket, String identifiantTesteur) {
        return transactionAvecResultat(em -> {
            Ticket ticket = ticketExistant(em, idTicket);
            Collaborateur valideur = collaborateurExistant(em, identifiantTesteur);
            ticket.renvoyer(valideur);
            logger.info("Ticket {} renvoyé en cours par {}", idTicket, identifiantTesteur);
            return ticket;
        });
    }

    public void supprimerTicket(Long idTicket) {
        dansUneTransaction(em -> {
            Ticket ticket = ticketExistant(em, idTicket);
            ticket.verifierSuppressionPossible();
            em.remove(ticket);
            logger.info("Ticket {} supprimé", idTicket);
        });
    }

    private Ticket ticketExistant(EntityManager em, Long idTicket) {
        Ticket ticket = em.find(Ticket.class, idTicket);
        if (ticket == null) {
            throw new OperationRefuseeException("Ticket introuvable : #" + idTicket);
        }
        return ticket;
    }

    private Collaborateur collaborateurExistant(EntityManager em, String identifiant) {
        Collaborateur collaborateur = chercherParIdentifiant(em, identifiant);
        if (collaborateur == null) {
            throw new OperationRefuseeException("Collaborateur introuvable : " + identifiant);
        }
        return collaborateur;
    }

    private long compterTicketsEnCours(EntityManager em, Collaborateur collaborateur) {
        return em.createQuery(
                        "select count(t) from Ticket t "
                                + "where t.assigne = :collaborateur and t.statut = :statut",
                        Long.class)
                .setParameter("collaborateur", collaborateur)
                .setParameter("statut", Statut.EN_COURS)
                .getSingleResult();
    }

    private long compterTicketsActifs(EntityManager em, Collaborateur collaborateur) {
        return em.createQuery(
                        "select count(t) from Ticket t "
                                + "where t.assigne = :collaborateur and t.statut <> :termine",
                        Long.class)
                .setParameter("collaborateur", collaborateur)
                .setParameter("termine", Statut.TERMINE)
                .getSingleResult();
    }

    public void assignerTicket(Long idTicket, String identifiantCollaborateur) {
        transactionAvecResultat(em -> {
            Ticket ticket = ticketExistant(em, idTicket);
            Collaborateur responsable = collaborateurExistant(em, identifiantCollaborateur);
            ticket.assigner(responsable);
            return ticket;
        });
    }

    public Ticket modifierTitreTicket(Long idTicket, String nouveauTitre) {
        return transactionAvecResultat(em -> {
            Ticket ticket = ticketExistant(em, idTicket);
            ticket.modifierTitre(nouveauTitre);
            logger.info("Ticket {} : titre modifié", idTicket);
            return ticket;
        });
    }

    public Ticket modifierPrioriteTicket(Long idTicket, Priorite nouvellePriorite) {
        return transactionAvecResultat(em -> {
            Ticket ticket = ticketExistant(em, idTicket);
            ticket.modifierPriorite(nouvellePriorite);
            logger.info("Ticket {} : priorité modifiée en {}", idTicket, nouvellePriorite);
            return ticket;
        });
    }
}
