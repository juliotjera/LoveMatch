package com.esmia.lovematch.dto.response;

import com.esmia.lovematch.enumeration.MotifSignalement;
import com.esmia.lovematch.enumeration.StatutSignalement;
import java.time.LocalDateTime;

// Vue de l'administration : elle voit le nom complet et l'e-mail de la personne signalée
public record SignalementResponse(Long id, MotifSignalement motif, String commentaire, StatutSignalement statut,
                                  LocalDateTime creeLe, Long auteurId, String auteurPrenom,
                                  Long cibleId, String cibleNomComplet, String cibleEmail, boolean cibleSuspendue,
                                  long signalementsContreCible, String messageSignale) {}
