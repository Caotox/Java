package fr.efrei.java;

import fr.efrei.java.demo.DonneesDemo;
import fr.efrei.java.enums.Priorite;
import fr.efrei.java.enums.Statut;
import fr.efrei.java.exceptions.CollaborateurDejaExistantException;
import fr.efrei.java.exceptions.OperationRefuseeException;
import fr.efrei.java.exceptions.TicketDejaExistantException;
import fr.efrei.java.model.*;
import fr.efrei.java.service.CollaborateurService;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.util.List;
import java.util.Scanner;

// Point d'entrée : menu console qui délègue tout le travail au service
public class HelloEfrei {

    public static void main(String[] args) {

        // Une seule fabrique pour toute l'application (coûteuse à créer)
        EntityManagerFactory fabrique = Persistence.createEntityManagerFactory("collaborateurs-pu");
        CollaborateurService service = new CollaborateurService(fabrique);

        Scanner scanner = new Scanner(System.in);
        int choix = -1;

        // Menu principal : 4 sections, chacune ouvre son propre sous-menu
        while (choix != 0) {
            System.out.println();
            System.out.println("=========== EFREIForge ===========");
            System.out.println(" 1. Collaborateurs");
            System.out.println(" 2. Tickets");
            System.out.println(" 3. Avancement des tickets");
            System.out.println(" 4. Outils");
            System.out.println(" 0. Quitter");
            choix = lireChoix(scanner);

            switch (choix) {
                case 0 -> System.out.println("À bientôt !");
                case 1 -> menuCollaborateurs(scanner, service);
                case 2 -> menuTickets(scanner, service);
                case 3 -> menuAvancement(scanner, service);
                case 4 -> menuOutils(scanner, service);
                default -> System.out.println("Option invalide. Choisissez entre 0 et 4.");
            }
        }

        scanner.close();
        fabrique.close(); // on ferme la fabrique en quittant
    }

    // --- Menus ---

    // Lit le choix d'un menu. On lit la ligne entière puis on la convertit :
    // évite le piège de nextInt() qui laisse le retour à la ligne dans le Scanner.
    // Renvoie -1 si ce n'est pas un nombre, ce qui tombe dans « option invalide ».
    private static int lireChoix(Scanner scanner) {
        System.out.print("Votre choix : ");
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    // Chaque sous-menu boucle jusqu'à « 0. Retour » : on peut enchaîner
    // plusieurs actions de la même section sans repasser par l'accueil.
    private static void menuCollaborateurs(Scanner scanner, CollaborateurService service) {
        int choix = -1;
        while (choix != 0) {
            System.out.println();
            System.out.println("--------- Collaborateurs ---------");
            System.out.println(" 1. Lister tous (par identifiant)");
            System.out.println(" 2. Trier par nom");
            System.out.println(" 3. Trier par nom puis prénom");
            System.out.println(" 4. Trier par salaire");
            System.out.println(" 5. Rechercher par nom");
            System.out.println(" 6. Filtrer par salaire minimum");
            System.out.println(" 7. Lister les programmeurs");
            System.out.println(" 8. Programmeurs d'un langage");
            System.out.println(" 9. Fiche d'un collaborateur");
            System.out.println("10. Ajouter un collaborateur");
            System.out.println("11. Augmenter un salaire");
            System.out.println(" 0. Retour");
            choix = lireChoix(scanner);

            switch (choix) {
                case 0 -> { } // retour au menu principal
                case 1 -> afficherListe(service.collaborateursTriesParId());
                case 2 -> afficherListe(service.triesParNom());
                case 3 -> afficherListe(service.triesParNomPuisPrenom());
                case 4 -> afficherListe(service.triesParSalaire());
                case 5 -> {
                    System.out.print("Fragment de nom : ");
                    String fragment = scanner.nextLine();
                    afficherListe(service.nomContenant(fragment));
                }
                case 6 -> {
                    System.out.print("Salaire minimum (€) : ");
                    if (scanner.hasNextDouble()) {
                        double seuil = scanner.nextDouble();
                        scanner.nextLine();
                        afficherListe(service.salaireSuperieurA(seuil));
                    } else {
                        System.out.println("Saisie invalide.");
                        scanner.nextLine();
                    }
                }
                case 7 -> afficherListe(service.programmeurs());
                case 8 -> {
                    System.out.print("Langage : ");
                    String langage = scanner.nextLine();
                    afficherListe(service.programmeursDuLangage(langage));
                }
                case 9 -> {
                    System.out.print("Identifiant du collaborateur : ");
                    String id = scanner.nextLine();
                    Collaborateur trouve = service.trouverUnCollaborateur(id);
                    if (trouve != null) {
                        System.out.println();
                        trouve.afficherFiche();
                    } else {
                        System.out.println("Collaborateur introuvable : " + id);
                    }
                }
                case 10 -> ajouterCollaborateur(scanner, service);
                case 11 -> augmenterSalaire(scanner, service);
                default -> System.out.println("Option invalide. Choisissez entre 0 et 11.");
            }
        }
    }

    private static void menuTickets(Scanner scanner, CollaborateurService service) {
        int choix = -1;
        while (choix != 0) {
            System.out.println();
            System.out.println("------------ Tickets -------------");
            System.out.println(" 1. Lister tous les tickets");
            System.out.println(" 2. Tickets d'un programmeur");
            System.out.println(" 3. Tickets par statut");
            System.out.println(" 4. Tickets à répartir (non assignés)");
            System.out.println(" 5. Créer un ticket");
            System.out.println(" 6. Modifier le titre");
            System.out.println(" 7. Modifier la priorité");
            System.out.println(" 8. Supprimer un ticket");
            System.out.println(" 9. Assigner à un programmeur");
            System.out.println("10. Réassigner les tickets d'un programmeur qui part");
            System.out.println(" 0. Retour");
            choix = lireChoix(scanner);

            switch (choix) {
                case 0 -> { }
                case 1 -> consulterAllTicket(service);
                case 2 -> consulterTicketByProgrammeur(scanner, service);
                case 3 -> consulterTicketByStatut(scanner, service);
                case 4 -> repartirTickets(service);
                case 5 -> creerTicket(scanner, service);
                case 6 -> modifierTitreTicket(scanner, service);
                case 7 -> modifierPrioriteTicket(scanner, service);
                case 8 -> supprimerTicket(scanner, service);
                case 9 -> assignerTicket(scanner, service);
                case 10 -> reAssignerTickets(scanner, service);
                default -> System.out.println("Option invalide. Choisissez entre 0 et 10.");
            }
        }
    }

    private static void menuAvancement(Scanner scanner, CollaborateurService service) {
        int choix = -1;
        while (choix != 0) {
            System.out.println();
            System.out.println("----- Avancement des tickets -----");
            System.out.println(" 1. Tableau de bord");
            System.out.println(" 2. Démarrer         (À faire  -> En cours)");
            System.out.println(" 3. Soumettre        (En cours -> À valider)");
            System.out.println(" 4. Valider/renvoyer (testeur)");
            System.out.println(" 0. Retour");
            choix = lireChoix(scanner);

            switch (choix) {
                case 0 -> { }
                case 1 -> tableauDeBord(service);
                case 2 -> demarrerTicket(scanner, service);
                case 3 -> soumettreTicket(scanner, service);
                case 4 -> validerOuRenvoyerTicket(scanner, service);
                default -> System.out.println("Option invalide. Choisissez entre 0 et 4.");
            }
        }
    }

    private static void menuOutils(Scanner scanner, CollaborateurService service) {
        int choix = -1;
        while (choix != 0) {
            System.out.println();
            System.out.println("------------- Outils -------------");
            System.out.println(" 1. Charger les données de démonstration");
            System.out.println(" 2. Tester le rollback d'une transaction");
            System.out.println(" 0. Retour");
            choix = lireChoix(scanner);

            switch (choix) {
                case 0 -> { }
                case 1 -> {
                    // Seuls les collaborateurs et tickets absents de la base sont ajoutés
                    int ajoutes = service.initialiserLesCollaborateurs(DonneesDemo.creerCollaborateurs());
                    int initialiserLesTickets = service.initialiserLesTickets(DonneesDemo.creerTickets());
                    System.out.println(ajoutes + " collaborateur(s) de démonstration ajouté(s).");
                    System.out.println(initialiserLesTickets + " ticket(s) de démonstration ajouté(s).");
                }
                case 2 -> testerRollback(service);
                default -> System.out.println("Option invalide. Choisissez entre 0 et 2.");
            }
        }
    }

    // --- Collaborateurs ---

    private static void afficherListe(List<? extends Collaborateur> liste) {
        if (liste.isEmpty()) {
            System.out.println("Aucun collaborateur trouvé.");
        } else {
            System.out.println(liste.size() + " collaborateur(s) :");
            for (Collaborateur c : liste) {
                System.out.println("  " + c);
            }
        }
    }

    private static void augmenterSalaire(Scanner scanner, CollaborateurService service) {
        System.out.print("Identifiant du collaborateur : ");
        String id = scanner.nextLine();

        // On garde l'ancien salaire pour afficher le avant/après
        Collaborateur avant = service.trouverUnCollaborateur(id);
        if (avant == null) {
            System.out.println("Collaborateur introuvable : " + id);
            return;
        }

        System.out.print("Pourcentage d'augmentation : ");
        if (!scanner.hasNextDouble()) {
            System.out.println("Saisie invalide.");
            scanner.nextLine();
            return;
        }
        double pourcentage = scanner.nextDouble();
        scanner.nextLine();

        if (pourcentage <= 0) {
            System.out.println("Le pourcentage doit être strictement positif.");
            return;
        }

        Collaborateur apres = service.augmenterSalaire(id, pourcentage);

        // 7.3
        // apres.augmenterSalaire(pourcentage);

        System.out.println("Salaire : " + avant.getSalaire() + " € -> " + apres.getSalaire() + " €");
    }

    private static void ajouterCollaborateur(Scanner scanner, CollaborateurService service) {
        System.out.print("Type (P = programmeur, T = testeur) : ");
        String type = scanner.nextLine().trim().toUpperCase();
        if (!type.equals("P") && !type.equals("T")) {
            System.out.println("Type invalide.");
            return;
        }

        System.out.print("Identifiant : ");
        String id = scanner.nextLine().trim();
        System.out.print("Prénom : ");
        String prenom = scanner.nextLine().trim();
        System.out.print("Nom : ");
        String nom = scanner.nextLine().trim();

        System.out.print("Salaire : ");
        double salaire;
        try {
            // accepte aussi la virgule française (42000,50)
            salaire = Double.parseDouble(scanner.nextLine().trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            System.out.println("Salaire invalide : entrez un nombre.");
            return;
        }

        // Mission 9 : un collaborateur a toujours une adresse, saisie par l'utilisateur
        System.out.print("Rue : ");
        String rue = scanner.nextLine().trim();
        System.out.print("Code postal : ");
        String codePostal = scanner.nextLine().trim();
        System.out.print("Ville : ");
        String ville = scanner.nextLine().trim();
        System.out.print("Pays : ");
        String pays = scanner.nextLine().trim();

        try {
            // inséré en base par la cascade, en même temps que le collaborateur
            Adresse adresse = new Adresse(rue, codePostal, ville, pays);

            Collaborateur c;
            if (type.equals("P")) {
                System.out.print("Langage préféré : ");
                String langage = scanner.nextLine().trim();
                c = new Programmeur(id, prenom, nom, salaire, adresse, langage);
            } else {
                c = new Testeur(id, prenom, nom, salaire, adresse);
            }
            service.ajouterUnCollaborateur(c);
            System.out.println("Collaborateur " + id + " ajouté.");
        } catch (CollaborateurDejaExistantException e) {
            System.out.println("Impossible : l'identifiant " + e.identifiant() + " est déjà utilisé.");
        } catch (IllegalArgumentException e) {
            // levée par Validation quand un champ obligatoire est vide
            System.out.println("Données invalides : " + e.getMessage());
        }
    }

    // --- Tickets : consulter ---

    private static void consulterAllTicket(CollaborateurService service) {
        List<Ticket> tickets = service.consulterAllTickets();
        if (tickets.isEmpty()) {
            System.out.println("Aucun ticket trouvé");
        } else {
            tickets.forEach(System.out::println);
        }
    }

    private static void consulterTicketByProgrammeur(Scanner scanner, CollaborateurService service) {
        System.out.println("ID du Programmeur : ");
        String id = scanner.nextLine();
        try {
            List<Ticket> ticketsByProgrammeur = service.consulterTicketByProgrammeur(id);
            if (ticketsByProgrammeur.isEmpty()) {
                System.out.println("Aucun ticket trouvé.");
            } else {
                ticketsByProgrammeur.forEach(System.out::println);
            }
        } catch (OperationRefuseeException | IllegalArgumentException e) {
            System.out.println("Refusé : " + e.getMessage());
        }
    }

    private static void consulterTicketByStatut(Scanner scanner, CollaborateurService service) {
        System.out.println("Voir les tickets qui ont pour statut : ");
        System.out.println("1. A FAIRE");
        System.out.println("2. EN COURS");
        System.out.println("3. A VALIDER");
        System.out.println("4. TERMINE");
        int choixStatut;
        try {
            choixStatut = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Saisie invalide : entrez un nombre.");
            return;
        }
        Statut statut = switch (choixStatut) {
            case 1 -> Statut.A_FAIRE;
            case 2 -> Statut.EN_COURS;
            case 3 -> Statut.A_VALIDER;
            case 4 -> Statut.TERMINE;
            default -> null; // choix hors liste
        };
        if (statut == null) {
            System.out.println("Statut invalide : choisissez 1, 2, 3 ou 4.");
            return;
        }

        List<Ticket> tickets = service.consulterTicketByStatut(statut);
        if (tickets.isEmpty()) {
            System.out.println("Aucun ticket trouvé.");
        } else {
            tickets.forEach(System.out::println);
        }
    }

    private static void repartirTickets(CollaborateurService service) {
        List<Ticket> tickets = service.repartirTickets();
        if (tickets.isEmpty()) {
            System.out.println("Aucun ticket à prendre en charge");
        } else {
            System.out.println("Tickets à répartir : " + tickets.size());
            tickets.forEach(System.out::println);
        }
    }

    private static void tableauDeBord(CollaborateurService service) {
        // 1) Nombre de tickets par statut
        List<Ticket> ticketsEnCours = service.consulterTicketByStatut(Statut.EN_COURS);
        List<Ticket> ticketsAFaire = service.consulterTicketByStatut(Statut.A_FAIRE);
        List<Ticket> ticketsAValider = service.consulterTicketByStatut(Statut.A_VALIDER);
        List<Ticket> ticketsTermines = service.consulterTicketByStatut(Statut.TERMINE);
        System.out.println("-------------------------");
        System.out.println("| Tickets a faire   | " + ticketsAFaire.size() + " |");
        System.out.println("| Tickets en cours  | " + ticketsEnCours.size() + " |");
        System.out.println("| Tickets a valider | " + ticketsAValider.size() + " |");
        System.out.println("| Tickets termines  | " + ticketsTermines.size() + " |");
        System.out.println("-------------------------");

        // 2) Charge de chaque programmeur = nombre de tickets EN_COURS
        List<Programmeur> programmeurs = service.programmeurs();
        for (Programmeur programmeur : programmeurs) {
            List<Ticket> ticketParProgrammeur = service.consulterTicketByProgrammeur(programmeur.getIdentifiant());
            int chargeProgrammeur = 0;
            for (Ticket ticket : ticketParProgrammeur) {
                if (ticket.getStatut() == Statut.EN_COURS) {
                    chargeProgrammeur++;
                }
            }
            System.out.println("| Charge du Programmeur | " + programmeur.getPrenom() + " | " + chargeProgrammeur + " | ");
        }
    }

    // --- Tickets : gérer ---

    // Lit un numéro de ticket ; renvoie null si la saisie n'est pas un nombre
    private static Long lireNumeroTicket(Scanner scanner) {
        System.out.print("Numéro du ticket : ");
        try {
            return Long.parseLong(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Numéro invalide : entrez un nombre.");
            return null;
        }
    }

    private static void creerTicket(Scanner scanner, CollaborateurService service) {
        System.out.print("Titre du Ticket : ");
        String titre = scanner.nextLine().trim();
        System.out.print("Priorité (1 : BASSE, 2 : NORMALE, 3 : HAUTE) : ");
        int priorite;
        try {
            priorite = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Saisie invalide : entrez un nombre.");
            return;
        }

        Priorite prioriteTicket = switch (priorite) {
            case 1 -> Priorite.BASSE;
            case 2 -> Priorite.NORMALE;
            case 3 -> Priorite.HAUTE;
            default -> null;
        };
        if (prioriteTicket == null) {
            System.out.println("Priorité invalide : choisissez 1, 2 ou 3.");
            return;
        }
        try {
            Ticket ticket = new Ticket(titre, prioriteTicket);
            service.creerTicket(ticket);
            System.out.println("Ticket ajouté avec succès.");
        } catch (TicketDejaExistantException e) {
            System.out.println("Impossible : le ticket " + e.identifiant() + " est déjà créé.");
        } catch (IllegalArgumentException e) {
            System.out.println("Données invalides : " + e.getMessage());
        }
    }

    private static void modifierTitreTicket(Scanner scanner, CollaborateurService service) {
        System.out.println("Quel ticket voulez-vous modifier ?");
        Long id = lireNumeroTicket(scanner);
        if (id == null) return;
        System.out.println("Par quel titre voulez-vous le remplacer ?");
        String nouveauTitre = scanner.nextLine();
        try {
            service.modifierTitreTicket(id, nouveauTitre);
            System.out.println("Titre modifié avec succès !");
        } catch (OperationRefuseeException | IllegalArgumentException e) {
            System.out.println("Modification refusée : " + e.getMessage());
        }
    }

    private static void modifierPrioriteTicket(Scanner scanner, CollaborateurService service) {
        System.out.println("Quel ticket voulez-vous modifier ?");
        Long id = lireNumeroTicket(scanner);
        if (id == null) return;

        System.out.println("Quelle est la nouvelle priorité ?");
        System.out.println("1. BASSE");
        System.out.println("2. NORMALE");
        System.out.println("3. HAUTE");

        int choixPriorite;
        try {
            choixPriorite = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Saisie invalide : entrez un nombre.");
            return;
        }

        Priorite nouvellePriorite = switch (choixPriorite) {
            case 1 -> Priorite.BASSE;
            case 2 -> Priorite.NORMALE;
            case 3 -> Priorite.HAUTE;
            default -> null;
        };
        if (nouvellePriorite == null) {
            System.out.println("Priorité invalide : choisissez 1, 2 ou 3.");
            return;
        }

        try {
            Ticket ticket = service.modifierPrioriteTicket(id, nouvellePriorite);
            System.out.println("Priorité modifiée : " + ticket);
        } catch (OperationRefuseeException | IllegalArgumentException e) {
            System.out.println("Refusé : " + e.getMessage());
        }
    }

    private static void supprimerTicket(Scanner scanner, CollaborateurService service) {
        Long id = lireNumeroTicket(scanner);
        if (id == null) return;
        try {
            service.supprimerTicket(id);
            System.out.println("Ticket #" + id + " supprimé.");
        } catch (OperationRefuseeException e) {
            System.out.println("Refusé : " + e.getMessage());
        }
    }

    private static void assignerTicket(Scanner scanner, CollaborateurService service) {
        System.out.println("Quel ticket voulez-vous assigner ?");
        Long id = lireNumeroTicket(scanner);
        if (id == null) return;
        System.out.println("À quel programmeur voulez-vous assigner le ticket #" + id + " ?");
        String idProgrammeur = scanner.nextLine().trim();
        try {
            service.assignerTicket(id, idProgrammeur);
            System.out.println("Ticket assigné avec succès");
        } catch (OperationRefuseeException | IllegalArgumentException e) {
            System.out.println("Refusé : " + e.getMessage());
        }
    }

    // Seuls les tickets « À faire » sont transférés (ceux déjà démarrés restent au programmeur source)
    private static void reAssignerTickets(Scanner scanner, CollaborateurService service) {
        System.out.println("Identifiant du programmeur qui quitte le projet : ");
        String source = scanner.nextLine().trim();
        System.out.println("Identifiant du programmeur qui reprend ses tickets : ");
        String cible = scanner.nextLine().trim();
        try {
            int nbTickets = service.reassignerTicketsAFaire(source, cible);
            System.out.println(nbTickets == 0
                    ? "Aucun ticket « À faire » à réassigner pour " + source + "."
                    : nbTickets + " ticket(s) réassigné(s) à " + cible + ".");
        } catch (OperationRefuseeException e) {
            System.out.println("Refusé : " + e.getMessage());
        }
    }

    // --- Tickets : faire avancer ---

    private static void demarrerTicket(Scanner scanner, CollaborateurService service) {
        Long id = lireNumeroTicket(scanner);
        if (id == null) return;
        try {
            System.out.println("Ticket démarré : " + service.demarrerTicket(id));
        } catch (OperationRefuseeException e) {
            System.out.println("Refusé : " + e.getMessage());
        }
    }

    private static void soumettreTicket(Scanner scanner, CollaborateurService service) {
        Long id = lireNumeroTicket(scanner);
        if (id == null) return;
        try {
            System.out.println("Ticket soumis : " + service.soumettreTicket(id));
        } catch (OperationRefuseeException e) {
            System.out.println("Refusé : " + e.getMessage());
        }
    }

    private static void validerOuRenvoyerTicket(Scanner scanner, CollaborateurService service) {
        Long id = lireNumeroTicket(scanner);
        if (id == null) return;
        System.out.print("Identifiant du testeur : ");
        String testeur = scanner.nextLine().trim();
        System.out.print("V = valider, R = renvoyer en cours : ");
        String action = scanner.nextLine().trim().toUpperCase();
        try {
            if (action.equals("V")) {
                System.out.println("Ticket validé : " + service.validerTicket(id, testeur));
            } else if (action.equals("R")) {
                System.out.println("Ticket renvoyé : " + service.renvoyerTicket(id, testeur));
            } else {
                System.out.println("Action invalide : tapez V ou R.");
            }
        } catch (OperationRefuseeException e) {
            System.out.println("Refusé : " + e.getMessage());
        }
    }

    // --- Outils ---

    // Challenge A : une panne entre deux ajouts doit annuler les deux (rollback)
    private static void testerRollback(CollaborateurService service) {
        Adresse adresse = new Adresse("1 rue du Test", "75000", "Paris", "France");
        Collaborateur premier = new Testeur("C901", "Test", "Un", 30000, adresse);
        Collaborateur second = new Testeur("C902", "Test", "Deux", 30000, adresse);

        try {
            service.ajouterDeux(premier, second, true);
        } catch (IllegalStateException e) {
            System.out.println("Échec simulé : " + e.getMessage());
        }

        // Si le rollback a fonctionné, aucun des deux n'est en base
        boolean baseInchangee = service.trouverUnCollaborateur("C901") == null
                && service.trouverUnCollaborateur("C902") == null;
        System.out.println(baseInchangee
                ? "Rollback OK : aucun des deux collaborateurs n'est en base."
                : "PROBLÈME : un collaborateur a été enregistré malgré la panne !");
    }
}
