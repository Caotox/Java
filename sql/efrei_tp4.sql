-- =====================================================================
-- Usage : mariadb -u root -p < sql/efrei_tp4.sql
--
-- Ce script ne supprime rien : il crée seulement la base si elle n'existe pas.
-- Les tables (Adresse, Collaborateur, ticket) sont créées par Hibernate au
-- premier lancement (hibernate.hbm2ddl.auto = update).
-- Le jeu de données (collaborateurs + tickets) est chargé depuis
-- l'application : menu, option 11 « Charger les données de démonstration ».
-- =====================================================================
CREATE DATABASE IF NOT EXISTS efrei_tp4
    CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;