package fr.efrei.java;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.util.List;
import java.util.Scanner;

public class HelloEfrei {

    public static void main(String[] args) {

        // Une seule fabrique pour toute l'application (elle est coûteuse à créer)
        EntityManagerFactory fabrique = Persistence.createEntityManagerFactory("collaborateurs-pu");
        CollaborateurService service = new CollaborateurService(fabrique);

        // Données de démonstration : seuls les collaborateurs absents de la base sont ajoutés
        service.initialiser(DonneesDemo.creerCollaborateurs());

        Scanner scanner = new Scanner(System.in);
        int choix = -1;

        while (choix != 0) {
            afficherMenu();

            if (scanner.hasNextInt()) {
                choix = scanner.nextInt();
                scanner.nextLine(); // consommer le saut de ligne résiduel

                switch (choix) {
                    case 0 -> System.out.println("À bientôt !");

                    case 1 -> afficherListe(service.tous());

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
                        Collaborateur trouve = service.trouver(id);
                        if (trouve != null) {
                            System.out.println();
                            trouve.afficherFiche();
                        } else {
                            System.out.println("Collaborateur introuvable : " + id);
                        }
                    }

                    case 9 -> augmenterSalaire(scanner, service);

                    case 10 -> ajouterTesteur(scanner, service);

                    default -> System.out.println("Option invalide. Choisissez entre 0 et 10.");
                }

            } else {
                System.out.println("Saisie invalide. Veuillez entrer un nombre.");
                scanner.nextLine();
            }
        }

        scanner.close();
        fabrique.close(); // on ferme la fabrique en quittant
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
        System.out.println("10. Ajouter un testeur");
        System.out.println("0. Quitter");
        System.out.print("Votre choix : ");
    }

    private static void augmenterSalaire(Scanner scanner, CollaborateurService service) {
        System.out.print("Identifiant du collaborateur : ");
        String id = scanner.nextLine();

        Collaborateur avant = service.trouver(id);
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

        // 7.3 : décommenter la ligne suivante. L'EntityManager est déjà fermé,
        //       "apres" est donc une entité détachée : la base ne changera pas.
        // apres.augmenterSalaire(pourcentage);

        System.out.printf("Salaire : %.2f € -> %.2f €%n", avant.getSalaire(), apres.getSalaire());
    }

    private static void ajouterTesteur(Scanner scanner, CollaborateurService service) {
        System.out.print("Identifiant (ex. C021) : ");
        String id = scanner.nextLine();
        System.out.print("Prénom : ");
        String prenom = scanner.nextLine();
        System.out.print("Nom : ");
        String nom = scanner.nextLine();

        System.out.print("Salaire (€) : ");
        if (!scanner.hasNextDouble()) {
            System.out.println("Saisie invalide.");
            scanner.nextLine();
            return;
        }
        double salaire = scanner.nextDouble();
        scanner.nextLine();

        // Mission 9 : un collaborateur a toujours une adresse
        System.out.print("Rue : ");
        String rue = scanner.nextLine();
        System.out.print("Code postal : ");
        String codePostal = scanner.nextLine();
        System.out.print("Ville : ");
        String ville = scanner.nextLine();
        System.out.print("Pays : ");
        String pays = scanner.nextLine();

        Adresse adresse = new Adresse(rue, codePostal, ville, pays);
        Testeur testeur = new Testeur(id, prenom, nom, salaire, adresse);

        try {
            service.ajouter(testeur); // l'adresse est enregistrée en même temps (cascade)
            System.out.println("Collaborateur " + id + " enregistré.");
        } catch (CollaborateurDejaExistantException e) {
            System.out.println("Impossible : l'identifiant " + e.identifiant() + " est déjà utilisé.");
        }
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
}
