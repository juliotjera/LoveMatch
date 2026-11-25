package com.esmia.lovematch.enumeration;

// Genre(s) que le membre souhaite rencontrer ; « TOUS » inclut les personnes non binaires
public enum GenreRecherche {
    HOMMES,
    FEMMES,
    TOUS;

    public boolean accepte(Genre genre) {
        return this == TOUS || (this == HOMMES && genre == Genre.HOMME) || (this == FEMMES && genre == Genre.FEMME);
    }
}
