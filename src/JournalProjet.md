***Journal Projet Final EL OSTA Marine et ROQC Antoine***

IL FAUDRA PENSER A INCLURE LES TABLEAUX DEMANDES DANS L'INTITULE DU TP POUR LES 2 JOURNAUX (RENDU 1 ET RENDU 2)

Pour ce projet, nous sommes partis sur la base de notre rendu du tp4. Notre Base de Données est donc nommée ```efrei_tp4```

Nous avons commencé par la création de l'Entité Ticket. Un premier Squelette avait été fourni pour le projet. Voici donc les colonnes que nous avons décidé d'attribuer à l'entité, leurs caractéristiques, ainsi que la raison de ces choix.

- id - Cette colonne représente la clé primaire. Il n'est pas amené à changer. Nous avons décidé d'en faire un type Long qui va s'auto incrémenter (AUTO_INCREMENT ou (strategy = GenerationType.IDENTITY)) comme le Squelette conseillait d'implémenter cet attribut. L'idée d'un AUTO_INCREMENT est de ne pas avoir à gérer cette valeur, qui de toute manière ne sert qu'à identifier une Entité, et c'est également une assurance (dans une certaine mesure) qu'un id soit réellement unique, sans doublon. Evidemment, un contrôle sur la présence en base d'un Ticket avec un id déjà existant se fera préalablement dans la méthode de création d'un Ticket, pour s'assurer qu'un Ticket déjà existant ne soit pas écrasé par un autre, si pour une raison quelconque un id se retrouverait en double lors d'une création (sans aucun affichage utilisateur, puisque cette valeur ne dépend pas de lui).


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

## Autocontrôle du jalon 1

| N° | Autocontrôle | Résultat attendu | Constat |
|----|--------------|------------------|---------|
| J1 | Créer deux tickets de priorités différentes, puis consulter les tickets. | Ils apparaissent « À faire », sans responsable. | |
| J2 | Assigner un ticket à un programmeur, puis afficher les tickets de ce programmeur. | Le ticket apparaît dans sa liste. | |
| J3 | Arrêter puis relancer l'application, consulter les tickets. | Tickets, statuts et assignations retrouvés à l'identique. | |