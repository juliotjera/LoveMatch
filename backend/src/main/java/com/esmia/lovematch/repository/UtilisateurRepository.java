package com.esmia.lovematch.repository;

import com.esmia.lovematch.entity.Utilisateur;
import com.esmia.lovematch.enumeration.Genre;
import com.esmia.lovematch.enumeration.GenreRecherche;
import com.esmia.lovematch.enumeration.Role;
import com.esmia.lovematch.enumeration.StatutCompte;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {

    Optional<Utilisateur> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    long countByRole(Role role);

    long countByStatut(StatutCompte statut);

    // photos, réactions, matchs, messages… partent avec les comptes (on delete cascade)
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "delete from utilisateur where demo = true", nativeQuery = true)
    int supprimerComptesDemo();

    /**
     * Premier tri en base pour les suggestions : membre actif au profil complété, genre et date de naissance
     * compatibles, jamais déjà vu (liké ou passé), aucun blocage dans un sens ou dans l'autre.
     * La distance, la tranche d'âge de l'autre et le score sont ensuite calculés en Java (CalculScore).
     */
    @Query("""
        select u from Utilisateur u
        where u.id <> :moi
          and u.role = com.esmia.lovematch.enumeration.Role.MEMBRE
          and u.statut = com.esmia.lovematch.enumeration.StatutCompte.ACTIF
          and u.bio is not null
          and u.genre in :genres
          and u.genreRecherche in :genresQuiMAcceptent
          and u.dateNaissance between :neAuPlusTot and :neAuPlusTard
          and not exists (select r from Reaction r where r.auteur.id = :moi and r.cible = u)
          and not exists (select b from Blocage b
                          where (b.auteur.id = :moi and b.cible = u) or (b.auteur = u and b.cible.id = :moi))
        """)
    List<Utilisateur> candidats(@Param("moi") Long moi,
                                @Param("genres") Collection<Genre> genres,
                                @Param("genresQuiMAcceptent") Collection<GenreRecherche> genresQuiMAcceptent,
                                @Param("neAuPlusTot") LocalDate neAuPlusTot,
                                @Param("neAuPlusTard") LocalDate neAuPlusTard);
}
