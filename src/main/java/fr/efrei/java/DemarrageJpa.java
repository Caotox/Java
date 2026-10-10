package fr.efrei.java;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
// classe de test pour la mission 5.5
public class DemarrageJpa {
    public static void main(String[] args) {
        EntityManagerFactory fabrique = Persistence.createEntityManagerFactory("collaborateurs-pu");
        fabrique.close();
    }
}