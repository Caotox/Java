***Journal TP-4 EL OSTA Marine et ROQC Antoine***\
**Partie A — Fiabiliser l’application**\
*1. Commencez par casser votre application*
- Situation 1 : Classe HelloEfrei ligne 22. (type utilisé différent du type attendu)
- Situation 2 : Classe HelloEfrei ligne 23. (type correct, mais objet inexistant)


*2.3 try / catch*\
Le menu n'a pas d'option d'ajout de collaborateur, donc nous avons ajouté le try/catch directement dans HelloEfrei.
L'idée est, pour chaque instance dans DonneesDemo, de catch l'exception levée lors d'une potentielle erreur causée par un identifiant identique dans les données de test, qui a été throw diretement dans la méthode d'ajout de l'annuaire.
Pour résumer : on boucle sur les DonneesDemo pour créer une Map de Collaborateur, la méthode d'ajout est appelée depuis annuaire, qui throw une exception quand un id est identique, l'exception sera catch dans HelloEfrei, lors de la boucle d'ajout depuis DonneesDemo, et on affiche un message d'erreur à l'utilisateur adapté depuis les valeurs stockées dans notre classe d'exception.
Pour tester, il suffit d'ajouter depuis DonneesDemo un collaborateur avec un identifiant déjà existant.

*3*
groupId : org.example
artifactId : tp4-collaborateurs
version : 1.0-SNAPSHOT

REPONDRE A LA QUESTION

Inclure Caputre

*4*

Inclure Screen

Pour l'ajout de logs :
- logger.info : ajouté lors d'un ajout de collaborateur
- logger.warn : ajouté lors d'un id doublon détécté
- logger.debug : ajouté  lors de la modification du salaire pour analyse
- logger.error : 

*5*

