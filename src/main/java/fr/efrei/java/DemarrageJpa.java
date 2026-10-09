package fr.efrei.java;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class DemarrageJpa {
    public static void main(String[] args) {
        EntityManagerFactory fabrique = Persistence.createEntityManagerFactory("collaborateurs-pu");
        fabrique.close();
    }
}