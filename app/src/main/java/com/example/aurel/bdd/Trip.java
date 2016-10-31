package com.example.aurel.bdd;


public class Trip {
    private long id;
    private String titre;

    public Trip(long id, String titre) {
        this.id = id;
        this.titre = titre;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getTitre() { return  titre; }
    public void setTitre(String titre) { this.titre = titre; }
}
