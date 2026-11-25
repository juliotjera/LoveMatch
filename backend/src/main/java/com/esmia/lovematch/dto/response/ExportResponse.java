package com.esmia.lovematch.dto.response;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Toutes les données d'un membre, dans un format lisible et réutilisable (droit à la portabilité, RGPD article 20).
 * Des autres membres, seul le prénom apparaît.
 */
public record ExportResponse(
    LocalDateTime genereLe,
    MoiResponse profil,
    List<PhotoExport> photos,
    List<ReactionExport> reactions,
    List<MatchExport> matchs,
    List<BlocageExport> blocages,
    List<SignalementExport> signalements
) {

    public record PhotoExport(int ordre, String typeMime, String contenuBase64) {}

    public record ReactionExport(String prenom, String decision, LocalDateTime le) {}

    public record MessageExport(boolean deMoi, String contenu, LocalDateTime envoyeLe, LocalDateTime luLe) {}

    public record MatchExport(String avec, LocalDateTime depuis, LocalDateTime fermeLe, List<MessageExport> messages) {}

    public record BlocageExport(String prenom) {}

    public record SignalementExport(String prenom, String motif, String commentaire, String statut, LocalDateTime le) {}
}
