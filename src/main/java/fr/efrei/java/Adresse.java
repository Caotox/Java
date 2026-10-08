package fr.efrei.java;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Adresse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String rue;

    @Column(nullable = false, length = 10)
    private String codePostal;

    @Column(nullable = false, length = 50)
    private String ville;

    @Column(nullable = false, length = 50)
    private String pays;

    protected Adresse() { }

    public Adresse(String rue, String codePostal, String ville, String pays) {
        this.rue = Validation.texteObligatoire(rue, "rue");
        this.codePostal = Validation.texteObligatoire(codePostal, "code postal");
        this.ville = Validation.texteObligatoire(ville, "ville");
        this.pays = Validation.texteObligatoire(pays, "pays");
    }

    public Long getId()           { return id; }
    public String getRue()        { return rue; }
    public String getCodePostal() { return codePostal; }
    public String getVille()      { return ville; }
    public String getPays()       { return pays; }

    public void afficher() {
        System.out.println(rue);
        System.out.println(codePostal + " " + ville);
        System.out.println(pays);
    }

    @Override
    public String toString() {
        return rue + ", " + codePostal + " " + ville + " (" + pays + ")";
    }
}