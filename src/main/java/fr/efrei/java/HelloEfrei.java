package fr.efrei.java;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.util.List;
import java.util.Scanner;

public class HelloEfrei {

    public static void main(String[] args) {

        // Une seule fabrique pour toute l'application
        EntityManagerFactory fabrique = Persistence.createEntityManagerFactory("collaborateurs-pu");
        CollaborateurService service = new CollaborateurService(fabrique);

        Scanner scanner = new Scanner(System.in);
        int choix = -1;

        while (choix != 0) {
            afficherMenu();

            if (scanner.hasNextInt()) {
                choix = scanner.nextInt();
                scanner.nextLine();

                switch (choix) {
                    case 0 -> System.out.println("À bientôt !");

                    case 1 -> afficherListe(service.collaborateursTriesParId());

                    case 2 -> {
                        System.out.print("Fragment de nom : ");
                        String fragment = scanner.nextLine();
                        afficherListe(service.nomContenant(fragment));
                    }

                    case 3 -> {
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

                    case 4 -> afficherListe(service.programmeurs());

                    case 5 -> afficherListe(service.triesParNom());

                    case 6 -> afficherListe(service.triesParSalaire());

                    case 7 -> afficherListe(service.triesParNomPuisPrenom());

                    case 8 -> {
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

                    case 9 -> augmenterSalaire(scanner, service);

                    case 10 -> ajouterCollaborateur(scanner, service);

                    case 11 -> {
                        // Seuls les collaborateurs absents de la base sont ajoutés
                        int ajoutes = service.initialiserLesCollaborateurs(DonneesDemo.creerCollaborateurs());
                        System.out.println(ajoutes + " collaborateur(s) de démonstration ajouté(s).");
                    }
                    case 12 -> {
                        System.out.print("Langage : ");
                        String langage = scanner.nextLine();
                        afficherListe(service.programmeursDuLangage(langage));
                    }

                    case 13 -> testerRollback(service);
                    case 14 -> creerTicket(scanner, service);
                    case 15 -> consulterAllTicket(service);
                    case 16 -> consulterTicketByProgrammeur(scanner, service);
                    case 17 -> consulterTicketByStatut(scanner, service);
                    case 18 -> assignerTicket(scanner, service);
                    case 19 -> demarrerTicket(scanner, service);
                    case 20 -> soumettreTicket(scanner, service);
                    case 21 -> validerOuRenvoyerTicket(scanner, service);
                    case 22 -> supprimerTicket(scanner, service);
                    case 23 -> modifierTitreTicket(scanner, service);
                    case 24 -> modifierPrioriteTicket(scanner, service);
                    default -> System.out.println("Option invalide. Choisissez entre 0 et 13.");
                }

            } else {
                System.out.println("Saisie invalide. Veuillez entrer un nombre.");
                scanner.nextLine();
            }
        }

        scanner.close();
        fabrique.close(); // on ferme la fabrique en quittant
    }

    private static void consulterAllTicket(CollaborateurService service) {
        List<Ticket> tickets = service.consulterAllTickets();
        if (tickets.isEmpty()) {
            System.out.println("Aucun ticket trouvé");
        } else {
            tickets.forEach(System.out::println);
        }
    }

    private static void testerRollback(CollaborateurService service) {
        Adresse adresse = new Adresse("1 rue du Test", "75000", "Paris", "France");
        Collaborateur premier = new Testeur("C901", "Test", "Un", 30000, adresse);
        Collaborateur second = new Testeur("C902", "Test", "Deux", 30000, adresse);

        try {
            service.ajouterDeux(premier, second, true);
        } catch (IllegalStateException e) {
            System.out.println("Échec simulé : " + e.getMessage());
        }

        boolean baseInchangee = service.trouverUnCollaborateur("C901") == null
                && service.trouverUnCollaborateur("C902") == null;
        System.out.println(baseInchangee
                ? "Rollback OK : aucun des deux collaborateurs n'est en base."
                : "PROBLÈME : un collaborateur a été enregistré malgré la panne !");
    }

    // --- Méthodes statiques utilitaires ---

    private static void afficherMenu() {
        System.out.println();
        System.out.println("=== Annuaire des collaborateurs (EFREI) ===");
        System.out.println("1. Afficher tous les collaborateurs");
        System.out.println("2. Rechercher par nom");
        System.out.println("3. Filtrer par salaire minimum");
        System.out.println("4. Afficher les programmeurs");
        System.out.println("5. Trier par nom");
        System.out.println("6. Trier par salaire");
        System.out.println("7. Trier par nom puis prénom");
        System.out.println("8. Afficher la fiche d'un collaborateur");
        System.out.println("9. Augmenter le salaire d'un collaborateur");
        System.out.println("10. Ajouter un collaborateur (Programmeur ou Testeur au choix depuis la saisie)");
        System.out.println("11. Charger les données de démonstration");
        System.out.println("12. Afficher les programmeurs d'un langage");
        System.out.println("13. Tester le rollback d'une transaction (challenge A)");
        System.out.println("14. Crée un ticket");
        System.out.println("15. Consulter tous les tickets");
        System.out.println("16. Consulter les tickets d'un programmeur");
        System.out.println("17. Consulter les tickets en fonction de leur statut");
        System.out.println("18. Assigner un ticket à un programmeur");
        System.out.println("19. Démarrer un ticket");
        System.out.println("20. Soumettre un ticket à validation");
        System.out.println("21. Valider ou renvoyer un ticket (testeur)");
        System.out.println("22. Supprimer un ticket");
        System.out.println("23. Modifier le titre d'une ticket");
        System.out.println("24. Modifier la priorite d'une ticket");
        System.out.println("0. Quitter");
        System.out.print("Votre choix : ");
    }

    private static void augmenterSalaire(Scanner scanner, CollaborateurService service) {
        System.out.print("Identifiant du collaborateur : ");
        String id = scanner.nextLine();

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
            System.out.println("Données invalides : " + e.getMessage());
        }
    }
    private static void creerTicket(Scanner scanner, CollaborateurService service) {
        System.out.print("Titre du Ticket : ");
        String titre = scanner.nextLine().trim();
        System.out.print("Priorite (1 : BASSE, 2 : NORMALE, 3: HAUTE) : ");
        int priorite;
        try {
            priorite = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Saisie invalide : entrez un nombre.");
            return;
        }

        Priorite prioriteTicket = switch (priorite){
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
            System.out.println("Impossible : le ticket " + e.identifiant() + " est déjà crée.");
        } catch (IllegalArgumentException e) {
            System.out.println("Données invalides : " + e.getMessage());
        }

    }
    private static Long lireNumeroTicket(Scanner scanner) {
        System.out.print("Numéro du ticket : ");
        try {
            return Long.parseLong(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Numéro invalide : entrez un nombre.");
            return null;
        }
    }

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

    private static void consulterTicketByProgrammeur(Scanner scanner,  CollaborateurService service) {
        System.out.println("ID du Programmeur : ");
        String id = scanner.nextLine();
        if (id == null) return;
        try {
            List <Ticket> ticketsByProgrammeur = service.consulterTicketByProgrammeur(id);
            if (ticketsByProgrammeur.isEmpty()) {
                System.out.println("Aucun ticket trouvé.");
            } else {
                ticketsByProgrammeur.forEach(System.out::println);
            }
        } catch (OperationRefuseeException e) {
            System.out.println("Refusé : " + e.getMessage());
        }
    }

    private static void consulterTicketByStatut(Scanner scanner,  CollaborateurService service) {
        System.out.println("Voir les tickets qui ont pour statut : ");
        System.out.println("1. A FAIRE");
        System.out.println("2. EN COURS");
        System.out.println("3. A VALIDER");
        System.out.println("4. TERMINER");
        int choixStatut;
        try {
            choixStatut = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Saisie invalide : entrez un nombre.");
            return;
        }
        List<Ticket> tickets = switch (choixStatut) {
            case 1 -> service.consulterTicketByStatut(Statut.A_FAIRE);
            case 2 -> service.consulterTicketByStatut(Statut.EN_COURS);
            case 3 -> service.consulterTicketByStatut(Statut.A_VALIDER);
            case 4 -> service.consulterTicketByStatut(Statut.TERMINE);
            default -> service.consulterAllTickets();
        };
        if (tickets == null) {
            System.out.println("Statut invalide : choisissez 1, 2 ou 3.");
            return;
        }

        if (tickets.isEmpty()) {
            System.out.println("Aucun ticket trouvé.");
        } else {
            tickets.forEach(System.out::println);
        }
    }

    public static void assignerTicket(Scanner scanner, CollaborateurService service) {
        System.out.println("Quel ticket voulez vous assigner ?");
        Long id = lireNumeroTicket(scanner);
        if (id == null) return;
        System.out.println("A quel programmeur voulez-vous assigner le ticket d'id : " + id);
        String idProgrammeur = scanner.nextLine();
        if (idProgrammeur == null) return;
        try {
            service.assignerTicket(id, idProgrammeur);
            System.out.println("Ticket assigné avec succès");
        } catch (OperationRefuseeException e) {
            System.out.println("Refusé : " + e.getMessage());
        }
    }

    private static void modifierTitreTicket(Scanner scanner, CollaborateurService service) {
        System.out.println("Quel ticket voulez vous modifier ?");
        Long id = lireNumeroTicket(scanner);
        if (id == null) return;
        System.out.println("Par quel titre voulez-vous le remplacer");
        String nouveauTitre = scanner.nextLine();
        if (nouveauTitre == null) return;
        try {
            service.modifierTitreTicket(id, nouveauTitre);
            System.out.println("Titre modifié avec succès ! ");
        } catch (OperationRefuseeException e) {
            System.out.println("Modification refusée: " + e.getMessage());
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
}