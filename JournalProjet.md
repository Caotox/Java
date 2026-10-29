# ***Journal Projet Final EL OSTA Marine et ROQC Antoine***

## 1. Démarche
Pour ce projet, nous sommes partis de notre application du TP4. Notre Base de Données est donc nommée ```efrei_tp4```. Nous l'avons donc fait évoluer sans la réécrire.
Pour bien se partager le travail, nous avons crée un dépôt Git, et avons identifier les besoins de l'application.
Nous avons pus ainsi se repartir les tâches de manière equitable : 
- Travail commun : Pour partir de la même base et pouvoir avancer paralèlle, nous avons créé l'Entité `Ticket` et la méthode de création de ticket ensemble.
- Marine : 
  - Méthodes de consultation de ticket : Consulter tous les tickets, Consulter les tickets d'un programmeur, consulter les ticket en fonction de leur Statut.
  - Méthode pour assigner un ticket (Avec validation que le Collaborateur est bien un Programmeur)
  - Méthode pour modifier le titre d'un ticket
  - Méthode pour modifier la priorité d'un ticket
- Antoine : 
  - L'avancement du cycle de vie d'un ticket (Avec Validation a chaque fois que le ticket peut passer d'un cycle à l'autre et qu'un programmeur ne peut pas avoir plus de 3 ticket avec un statut EN_COURS)
  - Validation d'un ticket (Avec validation que c'est bien un Testeur qui chqnge l'état a ``VALIDER``)//Renvoie du ticket a `EN_COURS` dans le cas où il n'est pas validé.
  - Suppression d'un ticket

Nous nous organisions avec des pushs réguliers de notre travail avec des messages de commit explicit afin de savoir ce qui a été fait.

**Éléments du TP4 réutilisés :**

- Hiérarchie Collaborateur → Programmeur / Testeur, dont le polymorphisme sert aux règles métier.
- `Validation.texteObligatoire` pour refuser les textes vides.
- Le service JPA : `EntityManagerFactory` unique, transactions, requêtes JPQL à paramètres nommés.
- Les exceptions métier.
- La console (menu + Scanner ) et le chargement des données de démonstration.

**Ce qui est nouveau :**  L'entité `Ticket` (squelette fourni, complété), les enums `Priorite` et `Statut` , les règles du cycle de vie, les requêtes de consultation, la liste des tickets à répartir et
les options de menu 14 à 25.

### Pour charger les données nécéssaires aux tests, veuillez choisir l'option 11 du menu.

## 2. Implémentation : 
Nous avons commencé par la création de l'Entité Ticket. Un premier Squelette avait été fourni pour le projet. Voici donc les colonnes que nous avons décidé d'attribuer à l'entité, leurs caractéristiques, ainsi que la raison de ces choix.

- id - Cette colonne représente la clé primaire. Il n'est pas amené à changer. Nous avons décidé d'en faire un type Long qui va s'auto incrémenter (AUTO_INCREMENT ou (strategy = GenerationType.IDENTITY)) comme le Squelette conseillait d'implémenter cet attribut. L'idée d'un AUTO_INCREMENT est de ne pas avoir à gérer cette valeur, qui de toute manière ne sert qu'à identifier une Entité, et c'est également une assurance (dans une certaine mesure) qu'un id soit réellement unique, sans doublon.


- titre - Cette colonne représente le titre du Ticket. Elle ne peut pas être nulle (vérification lors de la saisie) et est de type String.


- priorite - Cette colonne représente la priorité d'un Ticket. Son type est une Enum { BASSE, NORMALE, HAUTE }, elle ne peut donc qu'être égale à une de ces 3 valeurs, et une priorité est choisie lors de la création du Ticket, c'est donc également une colonne qui ne peut pas être null.


- statut - Cette colonne représente le statut d'un Ticket. Son type est une Enum { A_FAIRE, EN_COURS, A_VALIDER, TERMINE }, elle ne peut donc qu'être égale à une de ces 4 valeurs, et le statut A_FAIRE est directement attribué lors de la création du Ticket, raison pour laquelle là encore cette colonne ne peut pas être null.


- assigne - Equivalent de la ForeignKey, correspond à l'id du collaborateur attribué au Ticket. C’est un ``@ManyToOne`` unidirectionnel vers Collaborateur, et la colonne ``assigne_id`` contient l’identifiant du collaborateur, par exemple ``C001``.


### Création d'un Ticket

À la création, le constructeur de `Ticket` valide le titre avec `Validation.texteObligatoire`
(refus du vide et des espaces) et la priorité (obligatoire). Le ticket est créé au statut
`A_FAIRE`, sans responsable. Le service l'enregistre ensuite en base dans une transaction.

### Consultation d'un Ticket

L'utilisateur a trois façon de consulter un ticket : 
- Consulter tous les tickets
- Consulter les Tickets asignés à un Programmeur précis
- Consulter les tickets en fonction du statut choisi
Elles s'appuient sur le même principe que les requêtes du TP4 (`salaireSuperieurA`, `programmeurs`) : requêtes JPQL avec paramètres nommés, résultats triés par numéro, lecture sans transaction.

Pour les tickets d'un collaborateur, le service vérifie d'abord que le collaborateur existe (sinon l'opération est refusée avec un message clair). Quand aucun ticket ne correspond, un message explicatif apparaît.

### Assigner un Ticket
L'assignation est faite dans une seule transaction : le ticket et le collaborateur sont chargés avec le même `EntityManager`, ce qui permet à JPA d'enregistrer la modification du ticket au commit (comme l'augmentation de salaire du TP4). L'association `assigne` est un `@ManyToOne` unidirectionnel, sans cascade : la colonne `assigne_id` contient
l'identifiant du collaborateur, qui doit déjà être en base.
Pour assigner un ticket, il faut dans un premier de vérifier que le responsable est bien un Programmeur. Il faut également vérifier que le statut du ticket est ``A_FAIRE``.
Pour chacunes de ces vérifications, un exception est lancée et la transaction est annulée : l'opération refusée ne modifie pas les données.

### Avancement du cycle de vie d'un Ticket

Pour mettre en place le cycle de vie d'un Ticket, il convient d'abord de rappeler les règles métiers imposée dans le cadre du TP, afin de mieux pouvoir expliquer les choix d'implémentation. 

Règle 2, cycle de vie : À faire → En cours → À valider → Terminé. Toute autre transition est refusée : pas de saut d’étape, pas de retour en arrière depuis « Terminé ».

Règle 5 : démarrer un ticket (passage « En cours ») exige qu’il soit déjà assigné.

Règle 6 : un programmeur ne peut pas avoir plus de 3 tickets « En cours » en même temps.

Règle 9 : les opérations incohérentes (ticket inexistant…) sont traitées proprement, et une opération refusée ne modifie jamais les données.

Tout d'abord, il faut commencer par la suppression du setter du statut du Ticket. L'idée est d'empêcher toute modification du statut d'un Ticket qui ne rentre pas dans le cycle de vie prévu par les méthodes de la classe Ticket. Pour résumer, seules les méthodes de la classe Ticket, qui entrent dans le cycle de vie de ce dernier, peuvent modifier le statut d'un Ticket. Un acteur n'est précisé que pour la validation. Ainsi, nous avons fait le choix de ne pas contrôler la nature du Collaborateur lors de la mise à jour du statut d'un Ticket (de A_FAIRE -> EN_COURS -> A_VALIDER). Cette décision se justifie par le fait que dans le monde de l'entreprise, un Testeur peut parfois être amené à faire progresser un Ticket de EN_COURS à A_VALIDER, lors d'un daily par exemple.

Ensuite, la répartition dans le code est la suivante.

La classe Ticket possède les méthodes de changement du statut d'un ticket, dans le respect du cycle de vie de ce dernier. Ces méthodes vont se charger de la vérification du statut (Exemple : Vérification qu'un ticket était A_FAIRE avant de le faire passer à EN_COURS), voire même de la vérification de la nature du Collaborateur avant le passage d'un Ticket de A_VALIDER à TERMINE (s'assurer qu'il s'agit bien d'un Testeur). 

Ces méthodes vont être appelées par la Classe CollaborateurService.

### Validation d'un Ticket

Règle 2 : un ticket « À valider » peut passer à « Terminé » ou être renvoyé « En cours ». Aucune transition n’est possible depuis « Terminé ».

Règle 7 : valider (À valider → Terminé) ou renvoyer (À valider → En cours) ne peut être fait que par un testeur, que l’on désigne lors de l’opération.

Règle 9 : ticket ou testeur inexistant, ou valideur qui n’est pas testeur : refus propre, données inchangées.

Valider ou renvoyer un ticket nécessite de désigner un testeur, passé en paramètre. Le service charge le ticket et le collaborateur avec le même `EntityManager`, puis `Ticket.valider` /
`Ticket.renvoyer` contrôlent que le collaborateur peut valider (méthode polymorphe `peutValiderUnTicket()`, `false` par défaut dans `Collaborateur`, redéfinie à `true` dans
`Testeur`), puis que le ticket est `A_VALIDER`. Un ticket ou un collaborateur inexistant, un valideur non testeur ou un mauvais statut sont refusés par une exception : la transaction est
annulée et les données restent inchangées.
Si un ticket doit être renvoyé au statut ``EN_COURS``, on vérifie d'abord que le programmeur à qui le ticket sera réassigner n'a pas déjà 3 ticket au statut ``EN_COURS``. Si c'est le cas, une exception est lancée. Si non, on modifie le statut.

### Modification d'un Ticket

Pour mettre en place la modification du titre ou de la priorité d'un Ticket, il convient d'abord de rappeler la règle métier imposée dans le cadre du TP, afin de mieux pouvoir expliquer les choix d'implémentation.
règle 8 : Le titre et la priorité peuvent être modifiés tant que le ticket n'est pas `TERMINE`. 

Les règles sont portées par la classe `Ticket` (`modifierTitre`, `modifierPriorite`), et le service se contente de charger le ticket, d'appeler la méthode puis de laisser JPA enregistrer la modification au commit, comme
pour l'augmentation de salaire du TP4. Le nouveau titre passe par `Validation.texteObligatoire` (refus du vide et des espaces, comme à la création) et la nouvelle priorité ne peut pas être nulle. Si le ticket n'existe pas ou est terminé,
l'opération est refusée et rien n'est modifié.

### Suppression d'un Ticket

Un ticket ne peut être supprimé que s'il est encore `A_FAIRE`, afin de conserver l'historique du travail commencé (règle 8). `Ticket.verifierSuppressionPossible()` porte la règle, et le service appelle ensuite `em.remove(ticket)` dans la même
transaction. 
Un ticket inexistant ou déjà commencé est refusé avec un message clair.

## 3. Décisions Techniques
### Décision 1 — Les règles métier vivent dans `Ticket`, le service orchestre

- Le **setter du statut est supprimé** : seules les méthodes de `Ticket` (`assigner`, `demarrer`, `soumettre`, `valider`, `renvoyer`) font évoluer le statut, donc aucune transition hors cycle de vie n'est possible.
- Les règles qui dépendent du métier utilisent le **polymorphisme** : `Collaborateur.isProgrammeur()` (`false` par défaut, `true` dans `Programmeur`) et `peutValiderUnTicket()` (`false` par défaut, `true` dans `Testeur`). Pas de `instanceof` ni de test sur un texte.
- Le service charge l'entité, appelle la méthode du ticket, puis laisse JPA enregistrer au commit (comme l'augmentation de salaire du TP4).
- **Pourquoi :** les règles sont au même endroit, impossibles à contourner depuis le menu, et le service reste court. Nous ne contrôlons pas le métier de celui qui fait passer un ticket de « En cours » à « À valider » : en entreprise, un testeur peut le faire lors d'un point quotidien. Seule la validation exige un testeur (règle 7).
- Le nombre de tickets « En cours » d'un programmeur est compté par une requête JPQL (`count`) puis transmis à `demarrer` / `renvoyer`, qui appliquent la limite de 3 tickets `EN_COURS`.

### Décision 2 — Une opération = une transaction, avec un seul `EntityManager`

- Chaque opération (assigner, démarrer, valider, supprimer…) charge **le ticket et le collaborateur avec le même `EntityManager`**, dans une seule transaction. L'association `assigne` est un `@ManyToOne` unidirectionnel **sans cascade** ; le collaborateur doit déjà exister en base.
- Tout refus lève une `OperationRefuseeException` : la transaction est **annulée (rollback)**, une opération refusée ne modifie jamais les données (règle 9).
- La console attrape ces exceptions et affiche un message clair.
- **Pourquoi :** cohérence garantie, et mécanisme déjà vu au TP4.

## 4. Etat du Projet
### Ce qui fonctionne
La création, consultation, assignation, démarrage, soumission, validation/renvoie, modification du titre, modification de la priorité, suppresion d'un ticket, persistance.

### Ce qui reste incomplet pour le Jalon 1 : 
La liste des tickets à répartir et les bonus (tableau de bord, réassignation en bloc...)

## 5. Autocontrôle du jalon 1

| N° | Autocontrôle | Résultat attendu | Constat |
|----|--------------|------------------|---------|
| J1 | Créer deux tickets de priorités différentes, puis consulter les tickets. | Ils apparaissent « À faire », sans responsable. | OK |
| J2 | Assigner un ticket à un programmeur, puis afficher les tickets de ce programmeur. | Le ticket apparaît dans sa liste. | OK |
| J3 | Arrêter puis relancer l'application, consulter les tickets. | Tickets, statuts et assignations retrouvés à l'identique. | OK |

## 6. Tableau de recette

| N° | Scénario | Résultat attendu | Constaté |
|----|----------|------------------|----------|
| 1 | Créer un ticket sans titre (vide ou espaces), puis un ticket valide. | Le premier est refusé ; le second est créé « À faire », sans responsable. | **OK** : Lors de la création du ticket sans titre les messages d'erreur `Données invalides : Le champ "titre" est obligatoire.` `[main] WARN fr.efrei.java.Validation - Valeur vide refusée pour le champ titre` s'affichent et le ticket n'est pas ajouter à la BDD.<br>Lors de l'ajout du ticket valide nous avons ces messages : `[main] INFO fr.efrei.java.CollaborateurService - Ticket 11 enregistré en base` `Ticket ajouté avec succès.` Et on voit bien le ticket dans la base. |
| 2 | Assigner un ticket à un programmeur, puis un autre ticket à un testeur. | Le premier est accepté ; le second est refusé. | **OK** : Lorsqu'on assigne un ticket a un Programmeur (C017 pour le ticket 11) nous avons le message : `Ticket assigné avec succès`, et la base est mise à jour.<br>Lorsqu'on assigne un ticket à un Testeur (C014 pour le ticket 2) nous avons le message : `Refusé : Nina n'est pas programmeur : seul un programmeur peut se voir assigner un ticket` `[main] WARN fr.efrei.java.CollaborateurService - Transaction annulée (rollback) : Nina n'est pas programmeur : seul un programmeur peut se voir assigner un ticket`, et la base reste la même. |
| 3 | Démarrer un ticket non assigné, puis un ticket assigné. | Refus pour le premier ; le second passe « En cours ». | **OK** : Si on tente de démarrer le ticket 1 (non assigné) nous avons ce message dans la console : `Refusé : Le ticket #1 doit être assigné avant d'être démarré.` `[main] WARN fr.efrei.java.CollaborateurService - Transaction annulée (rollback) : Le ticket #1 doit être assigné avant d'être démarré.` La base reste inchangée.<br>Si C017 veut démarrer le ticket 11, la console affiche ce message : `Ticket démarré : #11  BASSE  Test Du Tableau de Recettes  - EN_COURS - Quentin Martinez [C017]` `[main] INFO fr.efrei.java.CollaborateurService - Ticket 11 démarré`. Le statut est mis à jour dans la base. |
| 4 | Faire passer « En cours » un 4e ticket d'un programmeur qui en a déjà 3 (ex. ticket n°4 d'Alice). | Refus ; le ticket reste « À faire ». | **OK** : Si Alice C001 veut démarrer le ticket 4 : `Refusé : Alice a déjà 3 tickets en cours.` `[main] WARN fr.efrei.java.CollaborateurService - Transaction annulée (rollback) : Alice a déjà 3 tickets en cours.` La base reste inchangée. |
| 5 | Tenter de passer un ticket directement de « À faire » ou « En cours » à « Terminé ». | Refus, statut inchangé. | **OK** : Le ticket 4 a un statut A_FAIRE. Si on essaie de le faire passer directement a TERMINER on aura : `Refusé : Impossible de valider le ticket #4 : il est A_FAIRE (il devrait être A_VALIDER).` `[main] WARN fr.efrei.java.CollaborateurService - Transaction annulée (rollback) : Impossible de valider le ticket #4 : il est A_FAIRE (il devrait être A_VALIDER).` |
| 6 | Soumettre un ticket à validation, tenter de le valider avec un programmeur, puis avec un testeur ; renvoyer un autre ticket « À valider » en cours. | Refus avec le programmeur ; « Terminé » avec le testeur ; le ticket renvoyé repasse « En cours ». | **OK** :<br>**Avec Programmeur** : `Refusé : Alice n'est pas testeur : seul un testeur peut valider ou renvoyer un ticket.` `[main] WARN fr.efrei.java.CollaborateurService - Transaction annulée (rollback) : Alice n'est pas testeur : seul un testeur peut valider ou renvoyer un ticket.`<br>**Avec Testeur** : `[main] INFO fr.efrei.java.CollaborateurService - Ticket 11 validé par C014` `Ticket validé : #11  BASSE  Test Du Tableau de Recettes  - TERMINE - Quentin Martinez [C017].`<br>**Renvoyer EN_COURS** : `[main] INFO fr.efrei.java.CollaborateurService - Ticket 9 renvoyé en cours par C014` `Ticket renvoyé : #9  HAUTE  Corriger le bug de paiement  - EN_COURS - Clara Bernard [C003].`<br>Lors des transactions réussies, la BDD est mise à jour avec les nouveaux statuts. |
| 7 | Sur un ticket « Terminé » : modifier le titre, le réassigner. Supprimer un ticket « En cours », puis un ticket « À faire ». | Modification, réassignation et première suppression refusées ; la seconde suppression est acceptée. | **OK** :<br>**Modification du titre d'un ticket terminé** : `Modification refusée: Impossible de modifier le ticket #11 car son statut est déjà à 'terminé'.`<br>**Suppression d'un ticket EN_COURS** : `Refusé : Le ticket #9 est EN_COURS : seul un ticket A_FAIRE peut être supprimé.` `[main] WARN fr.efrei.java.CollaborateurService - Transaction annulée (rollback) : Le ticket #9 est EN_COURS : seul un ticket A_FAIRE peut être supprimé.`<br>**Suppression d'un ticket A_FAIRE** : `Ticket #3 supprimé.` `[main] INFO fr.efrei.java.CollaborateurService - Ticket 3 supprimé` |
| 8 | Arrêter puis relancer l'application ; utiliser un ticket ou un collaborateur inexistant ; saisir du texte quand un nombre est attendu. | Tickets, statuts et assignations retrouvés ; message clair, l'application ne plante pas. | **OK** : Les données sont restaurées, `Saisie invalide. Veuillez entrer un nombre`, et l'application ne plante pas. |
| 9 | Afficher les tickets à répartir. | Non assignés, HAUTE → BASSE puis par numéro ; message clair si aucun. | **À FAIRE** |
## 7. README — Lancer et essayer l'application

### Prérequis
- JDK 21, Maven 3.9+
- Un serveur MariaDB/MySQL démarré, via **MAMP**, **WAMP** (ou XAMPP). Le pilote JDBC est fourni par Maven.

### Paramètres de connexion selon l'environnement
Les paramètres sont dans `src/main/resources/META-INF/persistence.xml` (propriétés `jakarta.persistence.jdbc.url` et `jakarta.persistence.jdbc.password`). **Adapter port et mot de passe selon votre machine** :

| Environnement | URL | Utilisateur | Mot de passe |
|---------------|-----|-------------|--------------|
| **MAMP** | `jdbc:mariadb://localhost:8889/efrei_tp4` | `root` | `root` |
| **WAMP** | `jdbc:mariadb://localhost:3306/efrei_tp4` | `root` | *(vide)* |
| XAMPP | `jdbc:mariadb://localhost:3306/efrei_tp4` | `root` | *(vide)* |

> Le port MAMP (8889) est celui de notre configuration ; si votre MAMP utilise un autre port, il s'affiche sur sa page d'accueil. Sous WAMP, le port peut être 3307 si un autre MySQL est déjà installé. Dans tous les cas, **l'URL doit pointer vers le port de votre serveur**.
> Le fichier fourni est réglé pour **WAMP/XAMPP** : `jdbc:mariadb://localhost:3306/efrei_tp4`, utilisateur `root`, mot de passe vide. Pour MAMP, remplacer le port par 8889 et le mot de passe par `root` (voir le tableau ci-dessus).

### Préparer la base
1. Démarrer MAMP ou WAMP.
2. Créer la base, au choix :
    - en ligne de commande : `mariadb -u root -p < sql/efrei_tp4.sql` (sous MAMP, ajouter `-P 8889 -h 127.0.0.1`) ;
    - ou via **phpMyAdmin** (lien sur la page d'accueil de MAMP/WAMP) : importer `sql/efrei_tp4.sql`, ou créer à la main une base `efrei_tp4`.
3. Les tables sont créées par Hibernate au premier lancement ; les collaborateurs du TP4 déjà présents ne sont pas touchés.

### Démarrer
```
mvn compile exec:java
```
(ou lancer `HelloEfrei` depuis IntelliJ).

### A Faire pour gérer les tickets
1. Menu **11** : charger les données de démonstration (collaborateurs et tickets ; rejouable sans doublon).
2. Menu **15** : consulter tous les tickets (10 tickets, 4 statuts).
3. Faire les tests nécéssaires pour vérifier le bon fonctionnement de l'application

### A savoir : 
Si la table Tickets contient au minimum 1 ticket aucune donnée des donées de démonstration ne sera affichée dans la base
