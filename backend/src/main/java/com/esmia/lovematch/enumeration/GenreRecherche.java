package com.esmia.lovematch.enumeration;

// Genre(s) que le membre souhaite rencontrer (sujet d'origine : homme, femme, les deux)
public enum GenreRecherche {
    HOMMES,
    FEMMES,
    TOUS;

    public boolean accepte(Genre genre) {
        return this == TOUS || (this == HOMMES && genre == Genre.HOMME) || (this == FEMMES && genre == Genre.FEMME);
    }
}
