***Journal Projet Final EL OSTA Marine et ROQC Antoine***

IL FAUDRA PENSER A INCLURE LES TABLEAUX DEMANDES DANS L'INTITULE DU TP POUR LES 2 JOURNAUX (RENDU 1 ET RENDU 2)

Nous avons commencé par la création de l'Entité Ticket. Un premier Squelette avait été fourni pour le projet. Voici donc les colonnes que nous avons décidé d'attribuer à l'entité, leurs caractéristiques, ainsi que la raison de ces choix.

- id - Cette colonne représente la clé primaire. Il n'est pas amené à changer. Nous avons décidé d'en faire un type Long qui va s'auto incrémenter (AUTO_INCREMENT ou (strategy = GenerationType.IDENTITY)) comme le Squelette conseillait d'implémenter cet attribut. L'idée d'un AUTO_INCREMENT est de ne pas avoir à gérer cette valeur, qui de toute manière ne sert qu'à identifier une Entité, et c'est également une assurance (dans une certaine mesure) qu'un id soit réellement unique, sans doublon. Evidemment, un contrôle sur la présence en base d'un Ticket avec un id déjà existant se fera préalablement dans la méthode de création d'un Ticket, pour s'assurer qu'un Ticket déjà existant ne soit pas écrasé par un autre, si pour une raison quelconque un id se retrouverait en double lors d'une création (sans aucun affichage utilisateur, puisque cette valeur ne dépend pas de lui).


- titre - Cette colonne représente le titre du Ticket. Elle ne peut pas être nulle (vérification lors de la saisie) et est de type String.


- priorite - Cette colonne représente la priorité d'un Ticket. Son type est une Enum { BASSE, NORMALE, HAUTE }, elle ne peut donc qu'être égale à une de ces 3 valeurs, et une priorité est choisie lors de la création du Ticket, c'est donc également une colonne qui ne peut pas être null.


- statut - Cette colonne représente le statut d'un Ticket. Son type est une Enum { A_FAIRE, EN_COURS, A_VALIDER, TERMINE }, elle ne peut donc qu'être égale à une de ces 4 valeurs, et le statut A_FAIRE est directement attribué lors de la création du Ticket, raison pour laquelle là encore cette colonne ne peut pas être null.


- assigne - Equivalent de la ForeignKey, correspond à l'id du collaborateur attribué au Ticket. Marine je te laisse compléter cette partie quand tu vas implémenter l'assignement d'une Collaborateur à un Ticket. 


### Création d'un Ticket

TODO

### Consultation d'un Ticket

M

### Assigner un Ticket

M

### Avancement du cycle de vie d'un Ticket

Pour mettre en place le cycle de vie d'un Ticket, il convient d'abord de rappeler les règles métiers imposée dans le cadre du TP, afin de mieux pouvoir expliquer les choix d'implémentation. 

Règle 2, cycle de vie : À faire → En cours → À valider → Terminé. Toute autre transition est refusée : pas de saut d’étape, pas de retour en arrière depuis « Terminé ».

Règle 5 : démarrer un ticket (passage « En cours ») exige qu’il soit déjà assigné.

Règle 6 : un programmeur ne peut pas avoir plus de 3 tickets « En cours » en même temps.

Règle 9 : les opérations incohérentes (ticket inexistant…) sont traitées proprement, et une opération refusée ne modifie jamais les données.

Tout d'abord, il faut commencer par la suppression du setter du statut du Ticket. L'idée est d'empêcher toute modification du statut d'un Ticket qui ne rentre pas dans le cycle de vie prévu par les méthodes de la classe Ticket. Pour résumer, seules les méthodes de la classe Ticket, qui entrent dans le cycle de vie de ce dernier, peuvent modifier le statut d'un Ticket.

Ensuite, la répartition dans le code est la suivante.

### Validation d'un Ticket

Règle 2 : un ticket « À valider » peut passer à « Terminé » ou être renvoyé « En cours ». Aucune transition n’est possible depuis « Terminé ».

Règle 7 : valider (À valider → Terminé) ou renvoyer (À valider → En cours) ne peut être fait que par un testeur, que l’on désigne lors de l’opération.

Règle 9 : ticket ou testeur inexistant, ou valideur qui n’est pas testeur : refus propre, données inchangées.

### Modification d'un Ticket

M

### Suppression d'un Ticket

M