# Journal — TP4 : Alice et Alex construisent une vraie application

> Binôme : **[À COMPLÉTER : noms]** — Package du projet : `fr.efrei.java`
>
> État d'avancement : missions 1 à 9 et 11 faites. Mission 10 : **[À COMPLÉTER]**. Challenges : non traités.
>
> Les passages marqués **[À COMPLÉTER]** sont des observations à relever nous-mêmes
> (console, phpMyAdmin, debugger).

---

## Vue d'ensemble : ce que le TP change dans l'application

| Problème de départ | Réponse apportée | Mission |
|---|---|---|
| Une erreur peut casser l'application | Exception métier précise + `try / catch` dans le menu | 1, 2 |
| Le projet dépend de la machine de celui qui l'a écrit | Maven (`pom.xml`) | 3 |
| « Ça ne marche pas » ne dit pas où est le bug | Debugger + logs SLF4J | 4 |
| Les données disparaissent à l'arrêt | Base MariaDB via JPA / Hibernate | 5 à 9 |

Organisation des classes à la fin du TP :

- `HelloEfrei` : parle à l'utilisateur (menu, saisies, messages). Ne contient pas de code JPA.
- `CollaborateurService` : parle à la base (`persist`, `find`, transactions, requêtes JPQL).
- `Collaborateur`, `Programmeur`, `Testeur`, `Adresse` : le modèle, devenu des entités JPA.
- `Annuaire` : ancienne `Map` du TP3. Conservée dans le projet mais plus utilisée par `HelloEfrei` : la base est la source de vérité.

---

## Partie A — Fiabiliser l'application

### Mission 1 — Casser l'application

**Situation 1 : identifiant déjà utilisé (règle métier violée)**

- Action : ajouter deux fois `C001`.
- Comportement observé (TP3) : l'ajout était refusé en silence (`return false`). L'appelant pouvait ne rien remarquer.
- Comportement souhaité : une exception précise et un message clair pour l'utilisateur.

**Situation 2 : pourcentage d'augmentation négatif (donnée incohérente)**

- Action : option 9, saisir `-10`.
- Comportement observé : le salaire ne change pas, mais l'écran affiche quand même « Salaire : X -> X », comme si l'opération avait réussi.
- Comportement souhaité : un message disant que le pourcentage doit être positif.
- Corrigé en fin de TP : le menu vérifie `pourcentage <= 0` avant d'appeler le service. C'est une erreur de saisie, donc traitée dans `HelloEfrei`.

**Notre erreur de départ.** Nos premiers exemples étaient `annuaire.ajouter(5)` et `annuaire.ajouter(bob)` (variable inexistante). Ce sont des erreurs de **compilation** : le programme ne démarre même pas, donc il ne « plante » pas. La mission attend des problèmes à l'**exécution**.

**À réfléchir : erreur de saisie ou règle métier violée ?**

Ce ne sont pas le même problème et ils ne se traitent pas au même endroit :

| | Erreur de saisie | Règle métier violée |
|---|---|---|
| Exemple | du texte à la place d'un nombre | un identifiant déjà utilisé |
| Nature | la donnée n'a pas la bonne forme | la donnée est bien formée mais interdite |
| Où la traiter | dans le menu (`hasNextInt()`, `hasNextDouble()`) | là où la règle est connue (`Annuaire`, `CollaborateurService`) |

### Mission 2 — `CollaborateurDejaExistantException`

Chaîne mise en place :

```
règle métier → détection → exception → appelant → message utilisateur
   doublon      service      throw      HelloEfrei     println
```

- L'exception étend `RuntimeException` et conserve l'identifiant fautif (accesseur `identifiant()`).
- `Annuaire.ajouter` et `CollaborateurService.ajouter` détectent le doublon et font `throw`.
- `HelloEfrei` intercepte **ce type précis** (pas `catch (Exception e)`) et affiche le message.

Pourquoi c'est mieux qu'un `println` dans l'annuaire :

- la classe qui connaît la règle ne décide pas de l'affichage (console aujourd'hui, autre chose demain) ;
- l'appelant ne peut pas ignorer le problème par accident, contrairement à un `return false` ;
- un `catch` précis ne masque pas d'autres erreurs inattendues.

**Difficultés rencontrées**

- `annuaire.ajouter(c)` était resté commenté dans `HelloEfrei` : l'annuaire était vide et le `catch` ne servait jamais. La règle était codée mais invérifiable.
- L'accesseur s'appelait `getIdentifiant()` alors que le TP demande `identifiant()`.
- Le menu n'avait aucune option d'ajout : impossible de tester un doublon à la main. Nous avons ajouté l'option 10 (ajouter un testeur).

**Vérification** : `DonneesDemo` contient volontairement `C001` deux fois. Au démarrage, le message de doublon s'affiche une fois et l'application continue. **[À COMPLÉTER : confirmer, et tester l'option 10 deux fois avec le même identifiant]**

### Mission 3 — Maven

Le projet a la structure Maven : `src/main/java`, `src/main/resources`, `pom.xml`.

**[À COMPLÉTER : `groupId`, `artifactId`, version Java choisie (`maven.compiler.release`), résultat de `Lifecycle → compile`]**

**À réfléchir : pourquoi `pom.xml` plutôt qu'un `.jar` copié ?**

- Le `pom.xml` **décrit** ce dont le projet a besoin (nom + version). Maven télécharge lui-même les bibliothèques, y compris celles dont elles dépendent.
- Un camarade qui récupère le code n'a rien à installer à la main : il ouvre le projet, Maven reconstruit le même environnement.
- Avec un `.jar` copié : on ne sait plus quelle version c'est, il faut penser à copier aussi ses dépendances, et le dépôt grossit avec des fichiers binaires.
- Changer de version revient à modifier une ligne.

À retenir : après chaque modification du `pom.xml`, cliquer sur **Load Maven Changes**, sinon IntelliJ ne voit pas les nouvelles dépendances.

### Mission 4 — Debugger et logs

**Debugger.** **[À COMPLÉTER : cas choisi, ligne du breakpoint, variables observées, première ligne où la valeur devient fausse]**

Idée à retenir : le bug est à la **première ligne où une valeur devient fausse**, pas à l'endroit où il se manifeste.

**Logs.** Principe : message utilisateur ≠ information de diagnostic.

- `System.out.println` : ce que l'utilisateur doit lire (menu, résultats, messages d'erreur compréhensibles).
- `logger` : ce que le développeur doit savoir pour comprendre ce qui s'est passé.

| Niveau | Usage | Exemple dans notre code |
|---|---|---|
| `info` | événement normal | « Collaborateur C021 enregistré en base » |
| `warn` | situation inhabituelle mais maîtrisée | « Doublon refusé pour C001 » |
| `debug` | détail pour l'analyse | « Salaire : 42000 -> 44100 » |
| `error` | échec, avec l'exception | « Échec de l'enregistrement de C021 » |

Par défaut, `slf4j-simple` n'affiche que `INFO` et au-dessus. Pour voir `DEBUG` : option JVM `-Dorg.slf4j.simpleLogger.defaultLogLevel=debug`.

**Bug trouvé dans nos logs.** Dans `augmenterSalaire`, nous avions écrit :

```java
logger.debug("Salaire : {} -> {}", getSalaire(), this.salaire);
```

Le log était placé **après** la modification : les deux valeurs étaient donc identiques. Correction : mémoriser `double avant = salaire;` en début de méthode. Un log faux est pire que pas de log, il oriente le diagnostic dans la mauvaise direction.

---

## Partie B — Persister

### Mission 5 — Première entité JPA

**Vocabulaire**

- **JPA** : une spécification (des annotations et des interfaces, package `jakarta.persistence`).
- **Hibernate** : l'implémentation qui fait réellement le travail.
- **Pilote JDBC** (`mariadb-java-client`) : ce qui permet à Java de parler à MariaDB.
- **`persistence.xml`** : la configuration (adresse de la base, utilisateur, liste des entités).

**Objet Java, entité JPA, ligne d'une table : quelle différence ?**

| | Où ça vit | Durée de vie |
|---|---|---|
| Objet Java | en mémoire | disparaît à l'arrêt du programme |
| Ligne d'une table | dans la base, sur disque | survit à l'arrêt |
| Entité JPA | une **classe** annotée `@Entity` | c'est le lien entre les deux : elle dit à Hibernate comment transformer un objet en ligne et inversement |

Un objet d'une classe entité n'est pas forcément en base : il ne le devient qu'après `persist` + `commit`.

**Annotations utilisées**

| Annotation | Rôle |
|---|---|
| `@Entity` | cette classe correspond à une table |
| `@Id` | cet attribut est la clé primaire (ici l'identifiant métier `C001`) |
| `@Column(nullable = false, length = 50)` | contraintes sur la colonne |
| `@Transient` | attribut **non** enregistré (notre `adresse`, en attendant la mission 9) |
| `@Inheritance`, `@DiscriminatorColumn`, `@DiscriminatorValue` | gestion de la hiérarchie (voir ci-dessous) |

Chaque entité a aussi un constructeur sans argument `protected` : Hibernate en a besoin pour créer l'objet avant de le remplir quand il relit une ligne.

**Choix : une seule table pour toute la hiérarchie (`SINGLE_TABLE`)**

- Une table `Collaborateur` contient les programmeurs **et** les testeurs.
- Une colonne `metier` (le discriminant) vaut `PROGRAMMEUR` ou `TESTEUR` et indique quelle classe Java recréer.
- La colonne `langagePrefere` est vide (`NULL`) pour les testeurs.

Pourquoi ce choix : chercher « tous les collaborateurs dont le salaire dépasse un seuil » (mission 8) se fait par une seule requête sur une seule table, sans jointure. Avec une table par classe, il faudrait interroger plusieurs tables et réunir les résultats.

Contrainte acceptée : les colonnes propres à une sous-classe ne peuvent pas être `nullable = false`, puisqu'elles sont vides pour les autres sous-classes.

**`hbm2ddl.auto = update`** : Hibernate crée ou complète les tables au démarrage, sans rien supprimer. Pratique en TP, à ne pas utiliser en production (`none` ou `validate`).

**Difficulté rencontrée** : le `persistence.xml` du sujet liste `fr.efrei.collaborateurs.modele.Collaborateur`. Il faut l'adapter à notre package et lister les **trois** classes (`Collaborateur`, `Programmeur`, `Testeur`).

**Premier contact (`DemarrageJpa`)** : **[À COMPLÉTER : le `create table` vu dans la console, les colonnes vues dans phpMyAdmin]**

**MySQL arrêté dans XAMPP** : **[À COMPLÉTER : message observé]**

Ce que nous attendons : une longue trace d'erreur technique (connexion refusée). Elle est utile au **développeur**. L'**utilisateur**, lui, devrait seulement lire quelque chose comme « La base de données est indisponible ». Notre application n'affiche pas encore ce message (voir « Non réalisé »).

### Mission 6 — `persist` et `find`

**Notions clés**

- **`EntityManagerFactory`** (la fabrique) : coûteuse à créer. Une seule pour toute l'application, créée au début de `main` et fermée en quittant.
- **`EntityManager`** : léger. Un par opération, ouvert puis fermé (`try (...)` le ferme automatiquement).
- **Écriture** : toujours dans une transaction.
- **Lecture** : `em.find(Collaborateur.class, "C001")`, pas besoin de transaction.

Le motif d'une écriture :

```java
transaction.begin();
try {
    em.persist(alice);
    transaction.commit();      // les modifications partent en base
} catch (RuntimeException e) {
    transaction.rollback();    // en cas d'erreur, on annule tout
    throw e;
}
```

**Choix de conception** : tout le code JPA est dans `CollaborateurService`, qui reçoit la fabrique dans son constructeur. `HelloEfrei` ne connaît que `service.ajouter(...)` et `service.trouver(...)`.

**Que retourne `find` pour `C999` ?** `null`, sans lever d'exception. C'est donc à l'appelant de tester le résultat, comme avec `Map.get` au TP3. **[À COMPLÉTER : confirmer avec l'option 8]**

**La règle du doublon avec la base.** Avant le `persist`, le service fait un `find` : si le collaborateur existe déjà, il lève `CollaborateurDejaExistantException`.

La clé primaire ne suffit-elle pas ? Elle protège les **données** (la base refusera toujours le doublon), mais pas l'**utilisateur** : sans notre vérification, il recevrait une erreur technique d'Hibernate au moment du `commit`, incompréhensible. Notre exception métier donne un message clair.

**Preuve que les données survivent** : au démarrage, on n'enregistre un collaborateur de démonstration que si `service.trouver(...)` renvoie `null`. Au premier lancement, 20 `insert` apparaissent. Au deuxième, aucun. **[À COMPLÉTER : confirmer]**

**À réfléchir : que vaut une clé comme `C001` ?**

1. **Capacité.** « C + 3 chiffres » donne 1000 identifiants au maximum (`C000` à `C999`). `C1000` rentre techniquement dans la colonne (longueur 10), mais casse le format, et le tri devient faux puisque c'est du texte : `"C1000"` est classé avant `"C200"`.
2. **Qui décide du prochain identifiant ?** Aujourd'hui, l'utilisateur qui le tape. Si deux personnes créent `C007` en même temps, les deux vérifications `find` répondent « libre », les deux `persist` partent, et le second `commit` échoue sur la clé primaire. Notre vérification ne protège donc pas complètement : il reste une erreur technique possible.
3. **Corriger `C01` en `C001`.** Une clé primaire est recopiée dans toutes les tables qui la référencent (l'adresse, à la mission 9). Corriger une faute de frappe oblige à modifier toutes ces lignes en même temps. Une clé primaire ne devrait jamais avoir besoin de changer.
4. **Deux collaborateurs différents avec le même identifiant.** Avec notre service : refus immédiat par l'exception métier, avant toute écriture. Sans cette vérification : l'erreur n'arrive qu'au `commit`, quand la base refuse l'`insert`. **[À COMPLÉTER si testé]**
5. **Une clé plus « propre ».** Une clé **technique** : un nombre généré automatiquement par la base, sans signification pour les humains, donc sans raison de changer, sans limite de format et sans conflit entre deux utilisateurs. `C001` resterait un simple attribut, avec une contrainte d'unicité. (À reprendre aux missions 9 et 10 et au challenge B.)

**Difficulté rencontrée** : l'adresse est `@Transient`, donc un collaborateur relu depuis la base a `adresse == null`. `afficherFiche()` plantait avec un `NullPointerException`. Correction : `if (adresse != null)`. En corrigeant, nous avions laissé l'ancien appel `adresse.afficher();` après le `if`, ce qui faisait toujours planter.

### Mission 7 — Une augmentation sans écrire `UPDATE`

Le code de `CollaborateurService.augmenterSalaire` :

```java
transaction.begin();
Collaborateur collaborateur = em.find(Collaborateur.class, identifiant);
collaborateur.augmenterSalaire(pourcentage);   // simple méthode métier
transaction.commit();                          // l'UPDATE part ici
```

Aucun `persist`, aucun SQL.

**Où est le SQL `UPDATE` ?**
C'est Hibernate qui le génère au `commit`. Au moment du `find`, il garde une copie de l'état d'Alice tel qu'il était en base. Au `commit`, il compare l'objet avec cette copie, voit que `salaire` a changé et envoie l'`update`. Ce mécanisme s'appelle le *dirty checking*. **[À COMPLÉTER : coller l'`update` vu dans la console]**

**Qu'est-ce qu'une entité gérée ?**
Un objet que l'`EntityManager` surveille. Un objet obtenu par `find` ou passé à `persist` est géré tant que cet `EntityManager` est ouvert : toute modification sera reportée en base au prochain `commit`. Un objet créé par `new`, ou dont l'`EntityManager` est fermé, n'est pas géré.

**Quel rôle joue le contexte de persistance ?**
C'est la « mémoire » de l'`EntityManager` : la liste des entités gérées avec leur état d'origine. Il sert à :

- détecter les modifications (c'est ce qui rend l'`UPDATE` automatique) ;
- garantir qu'un identifiant correspond à un seul objet Java dans cet `EntityManager` ;
- éviter des requêtes inutiles (un deuxième `find` du même identifiant ne retourne pas en base).

Il disparaît à la fermeture de l'`EntityManager`.

**Pourquoi la transaction est-elle importante ?**
Elle rend l'opération « tout ou rien ». Rien ne part en base avant le `commit`. Si une erreur survient avant, le `rollback` annule tout et la base reste dans son état d'origine.

**Variante 7.3 — entité détachée**

- Manipulation : l'appel à `augmenterSalaire` est fait dans `HelloEfrei`, après le retour du service (l'`EntityManager` est donc fermé).
- Résultat attendu : l'écran affiche le salaire augmenté, mais aucun `update` dans la console et la base ne change pas.
- Explication : l'objet existe toujours en Java, mais plus personne ne le surveille. On modifie une simple copie en mémoire.
- Piège : l'affichage laisse croire que tout a fonctionné.
- **[À COMPLÉTER : observation réelle]**

**Variante 7.4 — échec avant le `commit`**

- Manipulation : un `throw new RuntimeException(...)` juste avant `transaction.commit()`.
- Résultat attendu : l'application s'arrête avec l'erreur, le salaire en base est inchangé.
- Explication : l'objet Java a été modifié, mais le `commit` n'a jamais eu lieu et le `catch` a fait un `rollback`.
- **[À COMPLÉTER : observation réelle]**

Les deux variantes sont laissées en commentaire dans le code, repérées par `7.3` et `7.4`.

### Mission 8 — JPQL : retrouver autrement que par identifiant

`find` ne sait chercher que par clé primaire. Pour tout le reste (salaire, nom, tri), il faut une requête.

```java
em.createQuery("select c from Collaborateur c where c.salaire > :seuil", Collaborateur.class)
  .setParameter("seuil", seuil)
  .getResultList();
```

**JPQL travaille-t-il sur tables/colonnes ou sur entités/attributs ?**

Sur les **entités et leurs attributs**.

| | SQL | JPQL |
|---|---|---|
| Requête | `SELECT * FROM collaborateur WHERE salaire > 45000` | `select c from Collaborateur c where c.salaire > :seuil` |
| `Collaborateur` désigne | une table | une classe Java (majuscule comprise) |
| `salaire` désigne | une colonne | un attribut Java |
| Résultat | des lignes et des colonnes | une `List<Collaborateur>` d'objets prêts à l'emploi |

Hibernate traduit ensuite le JPQL en SQL (visible dans la console grâce à `show_sql`). Conséquence pratique : si on renomme une colonne avec `@Column(name = ...)`, le JPQL ne change pas.

**Pourquoi `:seuil` et `setParameter` ?**
Il ne faut jamais construire la requête en collant la saisie de l'utilisateur dans le texte (`"... where c.nom = '" + saisie + "'"`). L'utilisateur pourrait taper un morceau de requête et modifier son sens : c'est l'**injection SQL**. Avec un paramètre, la saisie reste une simple valeur, jamais du code.

**Recherche sur le nom** : `lower(c.nom) like :motif`, avec la valeur `"%" + fragment.toLowerCase() + "%"`. `%` signifie « n'importe quelle suite de caractères », et `lower` des deux côtés ignore la casse.

**Type réel des objets.** Une requête sur `Collaborateur` renvoie des `Programmeur` et des `Testeur`, pas des « collaborateurs génériques » : Hibernate lit la colonne `metier` et crée l'objet de la bonne classe. On le voit dans les listes, où `getMetier()` affiche le bon métier pour chacun (polymorphisme). **[À COMPLÉTER : confirmer à l'option 1]**

Pour l'option « Afficher les programmeurs », il suffit d'interroger la sous-classe : `select p from Programmeur p`. Hibernate ajoute lui-même `where metier = 'PROGRAMMEUR'`.

C'est ici que le choix `SINGLE_TABLE` de la mission 5 se justifie : une seule table à lire, aucune jointure entre classes.

**Choix** : nous avons aussi passé en JPQL la liste complète et les tris (`order by`). `HelloEfrei` n'utilise donc plus du tout l'`Annuaire` en mémoire, ce qui supprime l'incohérence de la mission 7 (deux sources de données).

Ce que cela change par rapport au TP3 : le filtre et le tri sont faits **par la base**, pas par une boucle Java sur toute la liste. Avec 20 collaborateurs c'est invisible, avec 100 000 on ne charge que les lignes utiles.

### Mission 9 — Persister la relation avec `Adresse`

**1. Besoin métier**
Un collaborateur a une adresse. Dans nos données de démonstration, il n'y a que 3 adresses (Paris, Villejuif, Lyon) pour 20 collaborateurs.

**2. Cardinalité**

- Un collaborateur a-t-il une ou plusieurs adresses ? **Une seule**, et elle est obligatoire.
- Deux collaborateurs peuvent-ils partager la même ? **Oui** (c'est le cas dans `DonneesDemo`).
- Une adresse existe-t-elle sans collaborateur ? **Oui**, c'est possible : si tous les collaborateurs d'une adresse partent, l'adresse peut rester.

Donc : **plusieurs** collaborateurs → **une** adresse.

**3. Représentation en base**
La clé étrangère est dans la table des **collaborateurs** : une colonne `adresse_id` qui contient l'`id` de l'adresse.

Pourquoi de ce côté ? Une colonne ne contient qu'une valeur. Un collaborateur a une seule adresse, donc une colonne lui suffit. À l'inverse, une adresse a plusieurs collaborateurs : il faudrait plusieurs valeurs dans une même case. La clé étrangère se place toujours du côté « plusieurs ».

**[À COMPLÉTER : vérifier dans phpMyAdmin que `adresse_id` est bien dans la table des collaborateurs]**

**4. Mapping JPA**

```java
@ManyToOne(cascade = CascadeType.PERSIST)
@JoinColumn(name = "adresse_id", nullable = false)
private Adresse adresse;
```

| Élément | Signification |
|---|---|
| `@ManyToOne` | plusieurs collaborateurs pour une adresse |
| `@JoinColumn(name = "adresse_id")` | nom de la colonne de clé étrangère |
| `nullable = false` | un collaborateur a toujours une adresse |
| `cascade = CascadeType.PERSIST` | persister un collaborateur enregistre aussi son adresse si elle est nouvelle |

Pourquoi pas `@OneToOne` ? Il signifierait « une adresse appartient à un seul collaborateur » : Bob ne pourrait pas partager l'adresse d'Alice, ce qui contredit nos données.

Pourquoi seulement `PERSIST` dans le cascade ? Si on avait aussi cascadé la suppression, supprimer un collaborateur supprimerait une adresse encore utilisée par d'autres.

**`Adresse` devient une entité** : `@Entity`, constructeur `protected` sans argument, et un identifiant.

```java
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;
```

Une adresse n'a pas d'identifiant métier. `IDENTITY` demande à la base de fabriquer le numéro (colonne auto-incrémentée). Tant que l'adresse n'est pas enregistrée, `id` vaut `null`.

**Comparaison des deux clés** (suite de la réflexion de la mission 6) :

| | `Collaborateur` : `C001` | `Adresse` : `id` généré |
|---|---|---|
| Qui la choisit | l'utilisateur | la base |
| Risque de doublon | oui, à vérifier nous-mêmes | non |
| Limite de format | 1000 valeurs | pratiquement aucune |
| Peut devoir être corrigée | oui (faute de frappe) | non, elle n'a pas de sens |
| Lisible par un humain | oui | non |

La clé générée est la plus facile à faire vivre. La clé `C001` nous a obligés à écrire une vérification de doublon et une exception.

**Difficultés et contraintes**

- **Table à vider.** La colonne `adresse_id` est obligatoire. Avec `hbm2ddl.auto=update`, Hibernate ne peut pas l'ajouter à une table qui contient déjà des lignes (elles n'auraient pas de valeur). Il a fallu vider la table des collaborateurs dans phpMyAdmin avant de relancer. **[À COMPLÉTER : message d'erreur vu si la table n'était pas vidée]**
- **Adresses partagées et données de démonstration.** `DonneesDemo` réutilise le même objet `Adresse` pour plusieurs collaborateurs. Si on enregistre chaque collaborateur avec son propre `EntityManager`, l'adresse est enregistrée avec le premier, puis elle est **détachée** quand cet `EntityManager` se ferme. Pour le collaborateur suivant, le cascade `PERSIST` tombe sur une adresse qui a déjà un `id` mais n'est pas gérée, et Hibernate refuse (« detached entity passed to persist »). Solution retenue : `CollaborateurService.initialiser` enregistre toutes les données de démonstration avec **un seul** `EntityManager` et **une seule** transaction. L'adresse reste gérée, elle est insérée une fois et réutilisée.
- **Relecture de la fiche.** Avec `@ManyToOne`, l'adresse est chargée en même temps que le collaborateur. On peut donc l'afficher après la fermeture de l'`EntityManager` (option 8).

---

## Partie C — Prendre du recul

### Mission 10 — Entité générée par IntelliJ (BDD → Objet)

**[À COMPLÉTER : cette mission se fait dans IntelliJ Ultimate, à partir de la table `collaborateur_demo`.]**

Grille de comparaison à remplir, avec pour chaque ligne la question du sujet : *est-ce que je comprends cette décision, et est-ce que je souhaite la conserver ?*

| Point à comparer | Notre entité manuelle | Entité générée | Je garde ? Pourquoi ? |
|---|---|---|---|
| Clé primaire (type, `@GeneratedValue` ?) | `String identifiant`, pas de génération | | |
| Type Java de `salaire` | `double` | | |
| Colonne nullable | `@Column(nullable = false)` sur les champs obligatoires | | |
| Noms de classe et d'attributs | choisis par nous | | |
| Présence de `@Column` / `@Table` | `@Column` seulement pour les contraintes | | |
| Constructeurs | un constructeur complet + un `protected` vide | | |
| Setters | aucun (seulement `augmenterSalaire`) | | |
| Colonne `metier` | hiérarchie `Programmeur` / `Testeur` | | |

Idées pour l'analyse (à confirmer avec le code réellement généré) :

- L'outil ne voit que la **structure** de la table. Il ne peut pas deviner une intention métier : une hiérarchie de classes, une règle comme « le salaire ne change que par une augmentation ».
- Des setters générés sur tous les attributs permettraient de modifier un salaire ou un identifiant sans passer par les méthodes métier.
- Un type objet (`Double`) accepte `null`, un type primitif (`double`) non : le choix dépend de si la colonne peut être vide.

Conclusion attendue : générer fait gagner du temps, mais seulement si on sait relire et corriger ce qui est produit. D'où l'ordre du TP : comprendre d'abord, automatiser ensuite.

### Mission 11 — Vérification finale

| Point à vérifier | Où, dans notre projet |
|---|---|
| Le projet se construit avec Maven | `pom.xml`, `Lifecycle → compile` **[À COMPLÉTER : confirmer]** |
| Une règle métier est protégée par une exception précise | `CollaborateurDejaExistantException`, levée dans `CollaborateurService.ajouter` |
| Les exceptions ne sont pas avalées | chaque `catch` affiche un message utilisateur, ou fait `rollback` puis `throw e` |
| Diagnostic avec le debugger | voir mission 4 **[À COMPLÉTER]** |
| Les logs restent techniques | `logger` dans `Collaborateur` et `CollaborateurService`, aucun dans `HelloEfrei` |
| Une entité est persistée puis retrouvée | options 10 puis 8, après redémarrage |
| Une modification d'entité gérée est synchronisée | option 9, `update` visible dans la console |
| Une requête JPQL fonctionne | options 1 à 7 |
| La relation avec `Adresse` est comprise | mission 9 : `@ManyToOne`, clé étrangère `adresse_id` |
| Les limites d'une clé comme `C001` | « À réfléchir » de la mission 6 et tableau de la mission 9 |
| `HelloEfrei` ne concentre pas toute la logique | il ne contient ni règle métier ni code JPA |

---

## Bilan

### Ce que nous retenons

- Une règle métier se signale par une exception précise, levée là où la règle est connue et interceptée là où l'on parle à l'utilisateur.
- Un message pour l'utilisateur et une information de diagnostic sont deux choses différentes (`println` / `logger`).
- Avec JPA, on manipule des objets et Hibernate écrit le SQL. Mais cela ne fonctionne que pour une entité **gérée**, dans une **transaction**.
- Une clé primaire qui a un sens pour les humains pose des problèmes de format, de concurrence et de correction.
- JPQL interroge des classes et des attributs, pas des tables et des colonnes, et les saisies passent toujours par des paramètres.
- Une relation se décide dans l'ordre : besoin métier, cardinalité, emplacement de la clé étrangère, puis annotation.

### Difficultés rencontrées (résumé)

- Confusion entre erreur de compilation et erreur d'exécution (mission 1).
- Appel `annuaire.ajouter(c)` resté commenté : règle impossible à vérifier (mission 2).
- Log placé après la modification, donc faux (mission 4).
- Nom de package à adapter dans `persistence.xml`, et les trois classes à lister (mission 5).
- `NullPointerException` sur l'adresse d'un collaborateur relu depuis la base (mission 6).
- Un renommage automatique d'IntelliJ avait inséré `fr.efrei.java.` dans des textes affichés (« fr.efrei.java.Collaborateur introuvable »).
- **[À COMPLÉTER : autres difficultés, messages d'erreur rencontrés, temps passé]**

### Limites connues et non réalisé

- **Base indisponible** : pas de message compréhensible pour l'utilisateur, seulement la trace technique.
- **Deux créations simultanées du même identifiant** : la vérification `find` puis `persist` ne suffit pas, une erreur technique reste possible.
- **Adresses en double.** L'option 10 crée toujours une nouvelle adresse, même si une adresse identique existe déjà en base. Il faudrait proposer de choisir une adresse existante.
- **Saisies vides** : un identifiant, un nom ou une rue vides sont acceptés par l'option 10.
- L'option 10 ne permet d'ajouter qu'un testeur, pas un programmeur.
- La clé primaire de `Collaborateur` reste `C001` : le passage à une clé technique (challenge B) n'est pas fait.
- Pas de suppression ni de modification d'un collaborateur autre que le salaire.
- Mission 10 : **[À COMPLÉTER]**. Challenges : non traités.
