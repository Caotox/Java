# Journal — TP4 : Alice et Alex construisent une vraie application

> Binôme : **Antoine ROCQ - Marine EL OSTA** — Package du projet : `fr.efrei.java`

Notes : Utilisation de MAMP au lieu de XAMP : port 8889 utilisé

## Partie A — Fiabiliser l'application

### Mission 1 — Casser l'application

**Situation 1 : identifiant déjà utilisé (règle métier violée)**

- Situation : Classe HelloEfrei
-> Action : créer un collaborateur sans adresse puis afficher sa fiche
Observé : PLANTAGE, adresse.afficher() est appelé sur null

- Situation : Classe HelloEfrei  nom null
-> créer un collaborateur avec nom null puis rechercher par nom
Observé : PLANTAGE, c.getNom().toLowerCase() sur null

- Action : ajouter deux fois `C001`.
- Comportement observé : l'ajout était refusé en silence (à cause du return `return false`, l'ajout ne se fait pas, mais rien ne l'indique). L'utilisateur n'était pas mis au courant de l'échec de l'ajout, ni de sa raison.

**Situation 2 : pourcentage d'augmentation négatif (donnée incohérente)**

- Action : option 9, saisir `-10`.
- Comportement observé : le salaire ne change pas, mais l'écran affiche quand même « Salaire : X -> X », comme si l'opération avait réussi. (avec 2 salaires identiques puisqu'il n'a pas changé). Non seulement on a un message non cohérent, puisque l'opération a échoué, mais en plus, on ne donne pas à l'utilisateur l'information d'échec de la modification, ainsi que la raison de l'échec de l'opération

**À réfléchir : erreur de saisie ou règle métier violée ?**

Ce ne sont pas le même problème. Une erreur de saisie est un problème de forme, qui se gère dans l'appelant directement (HelloEfrei) tandis qu'une erreur métier est un problème de fond, qui se gère directement dans le métier (Collaborateur...) 

### Mission 2 — `CollaborateurDejaExistantException`

Chaîne mise en place :

```
règle métier → détection → exception → appelant → message utilisateur
   doublon      service      throw      HelloEfrei     println
```

- Lors de l'ajout, si on a un id en double, on throw une new Exception (le fichier qu'on vient de créer) qui va enregistré l'identifiant à l'origine de l'erreur, et mettre en place, depuis le constructeur de la classe mère RuntimeException, le message d'erreur adapté
- Ensuite, depuis HelloEfrei, qui intialise cet ajout, l'exception va être interceptée (depuis le catch) ce qui va déclencher l'affichage du message d'erreur avec les informations de la cause de l'erreur (l'information du doublon d'id, ainsi que la valeur de cet id à l'origine du problème de doublon)

### Mission 3 — Maven

- groupId : fr.efrei
- artifactId : Artifact - tp4-collaborateurs
- version : 1.0-SNAPSHOT
- 
Pourquoi `pom.xml` plutôt qu'un `.jar` copié ?

- Le `pom.xml` décrit ce dont le projet a besoin. Maven télécharge lui-même les bibliothèques et les dépendances, et permet de reproduire et reconstruire le même environnement, peu importe qui va utiliser le projet.
- Avec un `.jar` copié : le dépôt grossit avec des fichiers binaires, et il faut copier et gérer soit même les dépendances.
- Changer de version revient à modifier une ligne.

### Mission 4 — Debugger et logs

L'idée ici est de différencier l'affichage à l'utilisateur, et l'affichage au développeur. Certaines informations ne doivent pas être visibles par l'utilisateur (potentiels problème de sécurité, failles exploitables), mais le développeur a besoin d'avoir accès à ces informations. En revanche, l'utilisateur doit tout de même avoir accès à certaines informations, comme le fait d'être mis au courant de la réussite ou de l'échec d'une opération, et avoir des informations basiques sur la cause de cet échec éventuellement.

- `System.out.println` : ce que l'utilisateur doit lire (menu, résultats, messages d'erreur compréhensibles).
- `logger` : ce que le développeur doit savoir pour comprendre ce qui s'est passé.

---

## Partie B — Persister

### Mission 5 — Première entité JPA

- On va indiquer au Hibernate de surveiller : Collaborateur, Programmeur et Testeur (@Entity) (il faudra aussi penser à l'ajout d'adresse par la suite)
- Ici, la clé primaire (@Id) sera l'identifiant
- Au niveau des contraintes, on choisit de ne pas permettre une valeur null pour chacune des colonnes de la table, car ce n'est pas pertinent dans notre cas (aucune information ne devrait être null) -> @Column(nullable = false, length = 50)
- On met un @Transiant sur l'adresse, car elle n'est pas gérée pour l'instant

- Il va donc falloir également créer des constructeurs vides pour chaque classe (si ils n'existaient pas déjà) car le Hibernate va d'abord créer des objets vides, puis y injecter des informations


**Notre choix : une seule table pour toute la hiérarchie (`SINGLE_TABLE`)**

- Les 2 choix étaient possible et pertinents, mais nous avons fait le choix d'une seule table, car Programmeur et Testeur sont chacun des Collaborateurs. Les inclure dans la même table permet donc de ne pas faire de jointure, et de respecter la règle métier initiale. Il faudra alors mettre en place une règle permettant de les différencier, car Programmeur, par exemple, a un attribut langagePréféré tandis qu'un testeur n'a pas cet attribut. D'autres cas similaires pourraient être ajoutés dans le futur, avec par exemple un nouvel attribut dans Testeur qui ne serait pas partagé par Programmeur. Il est donc nécessaire de les différencier en base, même si ils appartiennent à la même table. 
- Nous avons donc ajouté une colonne métier, où on renseignera si le Collaborateur est un Programmeur et un Testeur. Cela permet non seulement de gérer les différences entre les 2 classes, mais aussi de gérer la création des objets, en indiquant si Hibernate doit faire new Programmeur ou new Testeur. Le discriminant vaut donc 'PROGRAMMEUR' ou 'TESTEUR'
- Ainsi, contrairement aux autres colonnes de la table, la colonne langagePrefere doit pouvoir valoir null, car elle n'existe pas pour les testeurs (Testeur)
- Ce découpage empêche de multiplier les appels, pour chercher tous les Collaborateurs donc le salaire dépasse un seuil par exemple, où il aurait alors fallu un double appel, ou une jointure


**`hbm2ddl.auto = update`** : Hibernate crée ou complète les tables au démarrage, sans rien supprimer. 

- Il faudra donc adapter `persistence.xml` et lister les trois classes (`Collaborateur`, `Programmeur`, `Testeur`).

**Premier contact (`DemarrageJpa`)** : Le démarrage de l'app se fait depuis DEmarrageJpa, qui va créer l'EntityManager (qui va alors être appelé pour chaque opération)

### Mission 6 — `persist` et `find`

- Ici, il s'agit de créer l'EntityManager, qu'on va utiliser dans les transactions (begin et close)

- Tout le code JPA est dans `CollaborateurService`, qui reçoit la fabrique dans son constructeur. Cela permet de séparer les responsabilités entre les différentes couches de l'app, et surtout de centraliser la gestion Jpa. Toute opération partira du même endroit. `HelloEfrei` ne connaît donc que `service.ajouter(...)` et `service.trouver(...)` ce qui est pertinent avec l'organisation de notre projet, puisqu'il est le point d'entrée de notre app, ce n'est pas sa responsabilité de gérer la Jpa

**Que retourne `find` pour `C999` ?** : `null` sans lever d'exception. C'est donc à l'appelant de gérer la possibilité d'un null lors d'un find, dans le cas où il n'existe pas de collaborateur pour un id donné.

- Avant le `persist`, le service fait un `find` : si le collaborateur existe déjà, il lève `CollaborateurDejaExistantException`.
- Ce choix s'illustre par le fait que la clé primaire ne suffit pas car elle protège les données (la base refusera toujours le doublon), mais pas l'utilisateur. Sans notre vérification, il recevrait une erreur technique d'Hibernate au moment du `commit`, incompréhensible pour lui. Notre exception métier donne un message clair, adapté à son cas d'usage.

**Preuve que les données survivent** : au démarrage, on n'enregistre un collaborateur de démonstration que si `service.trouver(...)` renvoie `null` (donc si le collaborateur n'existe pas pour un id donné). Au premier lancement, 20 `insert` apparaissent. Au deuxième, aucun.
- Toutefois, nous avons fait évoluer le modèle, en placant l'ajout des données depuis DonnesDemo dans le menu. En effet, si cette opération se fait à chaque initialisation, cela cause une opération supplémentaire parfois inutile mais couteuse, mais surtout imaginons qu'un Collaborateur change d'id (ce qui n'est pas censé arriver, on ne devrait pas pouvoir changer un id, mais imaginons que ce soit possible dans d'éventuelles évolutions de notre projet), le .find() renverra alors null, ce qui aura pour effet d'ajouter à nouveau le Colaborateur à la base (avec l'id avant la modification). -> Par exemple, si on change un id de C001 à C100, lorsqu'on relancera le programme, puisque le .find() renvoie null pour C001, il va recréer ce Collaborateur
- Egalement, nous avons ajouté une option d'ajout de Collaborateur dans le menu. L'utilisateur peut choisir chaque champs (création de Programmeur ou de Testeur, puis valeur associée à chaque colonne) et le collaborateur sera ajouté en base.

**À réfléchir : que vaut une clé comme `C001` ?**

1. « C + 3 chiffres » donne 1000 identifiants au maximum (`C000` à `C999`). `C1000` rentre techniquement dans la colonne (longueur 10), mais casse le format, et le tri devient faux puisque c'est du texte : `"C1000"` est classé avant `"C200"`.
2. La clé est potentiellement amenée à changer, ce choix devrait alors se répértorier sur l'ensemble de la base. Une clé primaire ne devrait pas, ou presque jamais être amenée à changer.
3. Une clé technique : un nombre généré automatiquement par la base, sans signification pour les humains, donc sans raison de changer, sans limite de format et sans conflit entre deux utilisateurs. `C001` resterait un simple attribut, avec une contrainte d'unicité. Jpa permet de générer ce type de clé.
- Pour la suite du TP, nous avons donc choisi de mettre en place cette option. Cela sera notamment le cas pour la table Adresse, et sa clé primaire (la table Adresse n'a pas de colonne unique, contrairmenet à Collaborateur qui avait déjà id)

- Comme adresse est Transiant, il faut vérifier si adresse == null, sinon notre projet plantait (NullException) lors d'un afficherFiche()
- 
### Mission 7 — Une augmentation sans écrire `UPDATE`

**Où est le SQL `UPDATE` ?**
C'est Hibernate qui le génère au `commit`. Au moment du `find`, il garde une copie de l'état d'Alice tel qu'il était en base (avec le persist) et au `commit`, il compare l'objet avec cette copie, voit que `salaire` a changé et envoie l'`update` -> dirty checking

**Qu'est-ce qu'une entité gérée ?**
Un objet que l'`EntityManager` surveille. Un objet obtenu par `find` ou passé à `persist` est géré tant que cet `EntityManager` est ouvert (begin) : toute modification sera reportée en base au prochain `commit`. Un objet créé par `new`, ou dont l'`EntityManager` est fermé (close), n'est pas géré.

**Quel rôle joue le contexte de persistance ?**
C'est la mémoire de l'`EntityManager` : la liste des entités gérées avec leur état d'origine. Il sert à détecter les modifications, et éviter les requêtes inutiles (puisque seul les objets qui ont besoin d'opérations en base restent en mémoire)
Il disparaît à la fermeture de l'`EntityManager`.

**Pourquoi la transaction est-elle importante ?**
Elle rend l'opération « tout ou rien ». Rien ne part en base avant le `commit`. Si une erreur survient avant, le `rollback` annule tout et la base reste dans son état d'origine. C'est une meilleure gestion des données, soit toutes les opérations passent, soit elles échouent toutes, ce qui empêche de se retrouver dans un état où une table a été update mais pas l'autre, ce qui pourrait compliquer les opérations futures.

**Variante 7.3 — entité détachée**

- L'appel à `augmenterSalaire` est fait dans `HelloEfrei`, après le retour du service (l'`EntityManager` est donc fermé).
- L'écran affiche le salaire augmenté, mais aucun `update` dans la console et la base ne change pas.
- L'objet existe toujours en Java, mais plus personne ne le surveille. On modifie une simple copie en mémoire.
- Pourtant l'affichage laisse croire que tout a fonctionné.

**Variante 7.4 — échec avant le `commit`**

- Avec un `throw new RuntimeException(...)` juste avant `transaction.commit()`, l'erreur est remontée, le salaire en base est inchangé.
- L'objet Java a été modifié, mais le `commit` n'a jamais eu lieu et le `catch` a fait un `rollback`.

(Les deux variantes sont laissées en commentaire dans le code)

### Mission 8 — JPQL : retrouver autrement que par identifiant

`find` ne sait chercher que par clé primaire. Pour tout le reste (salaire, nom, tri), il faut une requête.

**JPQL travaille-t-il sur tables/colonnes ou sur entités/attributs ?**

Sur les entités et leurs attributs.

**Type réel des objets.** Une requête sur `Collaborateur` renvoie des `Programmeur` et des `Testeur`, pas des « collaborateurs génériques » : Hibernate lit la colonne `metier` et crée l'objet de la bonne classe. On le voit dans les listes, où `getMetier()` affiche le bon métier pour chacun.

- Pour l'option « Afficher les programmeurs », il suffit d'interroger la sous-classe : `select p from Programmeur p`. Hibernate ajoute lui-même `where metier = 'PROGRAMMEUR'`.

- Nous avons changé le fonctionnement du projet : l'ajout depuis Annuaire ne se fait pas par une boucle Java lors de l'initialisation de l'app, mais se fait en base avec une boucle sur DonneesDemo depuis CollaborateurService (appelé lorsque l'utilisateur le souhaite, dans le menu de HelloEfrei)

### Mission 9 — Persister la relation avec `Adresse`

**1. Besoin métier**
Un collaborateur a une adresse.

**2. Cardinalité**

- Un collaborateur a-t-il une ou plusieurs adresses ? Une seule, et elle est obligatoire.
- Deux collaborateurs peuvent-ils partager la même ? Oui (c'est le cas dans `DonneesDemo`).
- Une adresse existe-t-elle sans collaborateur ? Oui, c'est possible : si tous les collaborateurs d'une adresse partent, l'adresse peut rester.

Donc : plusieurs collaborateurs → une adresse.

**3. Représentation en base**
La clé étrangère est dans la table des collaborateurs : une colonne `adresse_id` qui contient l'`id` de l'adresse. (qu'on gèrera comme explique précédemment)

**4. Mapping JPA**

```java
@ManyToOne(cascade = CascadeType.PERSIST)
@JoinColumn(name = "adresse_id", nullable = false)
private Adresse adresse;
```
| `@ManyToOne` | plusieurs collaborateurs pour une adresse |
| `@JoinColumn(name = "adresse_id")` | nom de la colonne de clé étrangère |
| `nullable = false` | un collaborateur a toujours une adresse |
| `cascade = CascadeType.PERSIST` | persister un collaborateur enregistre aussi son adresse si elle est nouvelle |

- On ne cascade que le persist, car si on avait aussi cascadé la suppression, supprimer un collaborateur supprimerait une adresse encore utilisée par d'autres.

- `Adresse` devient alors une entité (comme prévu et anticipé initialement) : `@Entity` + constructeur sans argument et ajout de l'id pour la clé étrangère en base

- Par ailleurs, l'id est auto incrémenté et gérer par Jpa donc avant l'ajout en base (donc l'ajout d'un Collaborateur qui aurait cette adresse), un id vaut null

---

## Partie C — Prendre du recul

### Mission 10 — Entité générée par IntelliJ (BDD → Objet)

- Ici, il s'agit de faire le chemin inverse : au lieu d'écrire l'entité puis de laisser Hibernate créer la table, on part de la table `collaborateur_demo` et on laisse IntelliJ écrire l'entité (`CollaborateurDemo`, dans le package `fr.efrei.java.genere`)
- La classe générée n'est pas ajoutée à `persistence.xml`, car elle sert uniquement à la comparaison avec notre `Collaborateur`
- Notre version d'IntelliJ ne proposait pas « Generate Persistence Mapping », nous sommes donc passés par « JPA Entities from DB »

**Décision 1 : la clé primaire**

- L'outil garde `id_collaborateur` comme clé primaire, en `String`, sans `@GeneratedValue`
- Je pense qu'une valeur comme `C001` a été interprétée comme ayant une signification métier, la base ne peut pas la fabriquer elle-même, ou la modifier, car elle n'a pas le contexte métier. C'est pourquoi l'outil ne met donc pas de génération automatique selon moi.
- C'est le même choix que dans notre `Collaborateur`. On le conserve pour ce TP, mais avec les limites vues en mission 6 (une clé technique serait plus propre, comme pour `Adresse`)

**Décision 2 : le type de `salaire`**

- L'outil génère `Double` (l'objet), alors que nous avons écrit `double` (le primitif)
- `Double` peut valoir `null`, `double` non. Or la colonne est `not null` en base (et l'outil le sait, puisqu'il écrit `nullable = false` juste au-dessus)
- On ne conserve pas ce choix : un salaire est obligatoire, le primitif est donc plus cohérent, et il évite un `null` qui ferait planter `augmenterSalaire`

**Décision 3 : le nommage, `@Column` et `@Table`**

- L'outil convertit les noms de la base (`id_collaborateur`, `langage_prefere`, `collaborateur_demo`) en noms Java (`idCollaborateur`, `langagePrefere`, `CollaborateurDemo`)
- Comme les noms ne correspondent plus, il écrit `@Column(name = "...")` sur chaque attribut, même quand le nom est identique (`prenom`, `nom`), et un `@Table(name = ..., schema = "efrei_tp4")` sur la classe
- Dans ce projet, nous n'avons pas précisé les noms : nos attributs portent le même nom que les colonnes. Le choix de l'outil semble tout de même pertinent (bonnes pratiques).

**Décision 4 : constructeurs et setters**

- L'outil ne génère aucun constructeur, mais un getter et un setter pour chaque attribut, y compris `setIdCollaborateur` (on peut donc changer la clé primaire d'un objet Collaborateur). Selon moi, c'est pertinent du point de vue de certaines décisions prises tout le long du projet (basculer l'initialisation dans le menu pour éviter une recréation), mais d'un côté purement logique, il vaut mieux éviter de modifier la clé primaire.
- Notre `Collaborateur` : un constructeur complet qui valide les champs, un constructeur vide `protected` réservé à Hibernate, aucun setter, et une méthode métier (`augmenterSalaire`) pour la modification prévue dans le menu
- C'est une évolution intéressante, si notre projet prévoie d'ajouter des méthodes de modification de certains champs. Selon moi, il faudrait limiter les setters aux champs qui peuvent être amenés à changer, et ne pas en faire pour d'autre (cela me semble moins pertinent de modifier le Nom par exemple, l'ajout d'un setter semble alors peu pertinent)

**Décision 5 : la colonne `metier`**

- Pour l'outil, `metier` est une simple colonne `String`. Il génère une seule classe, et `langagePrefere` existe pour tout le monde, même pour un testeur
- De notre côté, cette colonne est le discriminant (`@DiscriminatorColumn`) d'une hiérarchie avec `Programmeur` et `Testeur`
- L'outil lit la structure de la table, il ne peut pas deviner que cette colonne sert à distinguer deux classes. On ne le conserve pas, car on perdrait le polymorphisme (`getMetier()`, `travailler()`, et colonne langagePrefere non présent selon le type de Collaborateur)

**Bilan de la comparaison**

- L'outil reproduit fidèlement la structure de la table : les types, les longueurs (`length = 40` pour `metier` et `langage_prefere`) et les colonnes obligatoires
- En revanche, il ne connaît pas le sens métier : pas d'héritage, pas de règle de validation, pas de `equals` / `hashCode`, pas de méthode métier
- C'est donc un bon point de départ pour éviter de recopier les colonnes à la main, mais le résultat doit être relu et adapté. D'où l'intérêt d'avoir écrit l'entité à la main en premier : on sait quoi corriger