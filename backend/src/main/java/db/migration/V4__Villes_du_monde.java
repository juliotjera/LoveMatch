package db.migration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

/**
 * LoveMatch s'ouvre au monde : aux 34 villes malgaches d'origine s'ajoutent les villes de plus de 15 000 habitants
 * (GeoNames, licence CC BY 4.0, fichier geonames/villes.tsv.gz). Les membres existants gardent leur ville.
 * Écrit en Java plutôt qu'en SQL : le nom des contraintes uniques diffère entre PostgreSQL et H2.
 */
public class V4__Villes_du_monde extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        Connection connexion = context.getConnection();
        try (Statement st = connexion.createStatement()) {
            // plusieurs villes du monde portent le même nom (Paris en France et au Texas)
            for (String contrainte : contraintesUniques(connexion, "ville")) {
                st.execute("alter table ville drop constraint \"" + contrainte + "\"");
            }
            st.execute("alter table ville alter column nom set data type varchar(120)");
            st.execute("alter table ville alter column region set data type varchar(120)");
            st.execute("alter table ville add column pays varchar(2) default 'MG' not null");
            st.execute("alter table ville add column nom_ascii varchar(120)");
            st.execute("alter table ville add column population int default 0 not null");

            // rayon jusqu'à « partout dans le monde » ; genre « non binaire » (11 caractères)
            st.execute("alter table utilisateur drop constraint ck_utilisateur_rayon");
            st.execute("alter table utilisateur add constraint ck_utilisateur_rayon check (rayon_km between 10 and 20000)");
            st.execute("alter table utilisateur alter column genre set data type varchar(12)");
        }

        List<String[]> monde = lireGeoNames();

        // villes malgaches d'origine : nom de recherche et population tirée de GeoNames quand le nom correspond
        Map<String, Integer> populationMalgache = new HashMap<>();
        for (String[] v : monde) {
            if ("MG".equals(v[2])) {
                populationMalgache.merge(v[1], Integer.parseInt(v[6]), Math::max);
            }
        }
        Map<Long, String> origine = new HashMap<>();
        try (Statement st = connexion.createStatement(); ResultSet rs = st.executeQuery("select id, nom from ville")) {
            while (rs.next()) {
                origine.put(rs.getLong(1), rs.getString(2));
            }
        }
        try (PreparedStatement maj = connexion.prepareStatement("update ville set nom_ascii = ?, population = ? where id = ?")) {
            for (Map.Entry<Long, String> v : origine.entrySet()) {
                String ascii = normaliser(v.getValue());
                maj.setString(1, ascii);
                maj.setInt(2, populationMalgache.getOrDefault(ascii, 0));
                maj.setLong(3, v.getKey());
                maj.addBatch();
            }
            maj.executeBatch();
        }

        java.util.Set<String> dejaLa = new java.util.HashSet<>();
        for (String nom : origine.values()) {
            dejaLa.add(normaliser(nom));
        }
        try (PreparedStatement ajout = connexion.prepareStatement(
                "insert into ville (nom, region, latitude, longitude, pays, nom_ascii, population) values (?, ?, ?, ?, ?, ?, ?)")) {
            int lot = 0;
            for (String[] v : monde) {
                if ("MG".equals(v[2]) && dejaLa.contains(v[1])) {
                    continue;
                }
                ajout.setString(1, v[0]);
                ajout.setString(2, v[3]);
                ajout.setDouble(3, Double.parseDouble(v[4]));
                ajout.setDouble(4, Double.parseDouble(v[5]));
                ajout.setString(5, v[2]);
                ajout.setString(6, v[1]);
                ajout.setInt(7, Integer.parseInt(v[6]));
                ajout.addBatch();
                if (++lot % 2000 == 0) {
                    ajout.executeBatch();
                }
            }
            ajout.executeBatch();
        }

        try (Statement st = connexion.createStatement()) {
            st.execute("alter table ville alter column nom_ascii set not null");
            st.execute("create index ix_ville_nom_ascii on ville (nom_ascii)");
            st.execute("create index ix_ville_population on ville (population)");
        }
    }

    private static List<String> contraintesUniques(Connection connexion, String table) throws SQLException {
        List<String> noms = new ArrayList<>();
        try (PreparedStatement ps = connexion.prepareStatement(
                "select constraint_name from information_schema.table_constraints"
                    + " where lower(table_name) = ? and constraint_type = 'UNIQUE'")) {
            ps.setString(1, table);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    noms.add(rs.getString(1));
                }
            }
        }
        return noms;
    }

    // nom, nom ascii (minuscules), pays, région, latitude, longitude, population
    private static List<String[]> lireGeoNames() throws IOException {
        List<String[]> villes = new ArrayList<>(32_000);
        try (InputStream brut = V4__Villes_du_monde.class.getResourceAsStream("/geonames/villes.tsv.gz")) {
            if (brut == null) {
                throw new IOException("geonames/villes.tsv.gz introuvable");
            }
            BufferedReader lecteur = new BufferedReader(new InputStreamReader(new GZIPInputStream(brut), StandardCharsets.UTF_8));
            String ligne;
            while ((ligne = lecteur.readLine()) != null) {
                if (!ligne.startsWith("#")) {
                    villes.add(ligne.split("\t", -1));
                }
            }
        }
        return villes;
    }

    // « Ambositra », « Fénérive » → « ambositra », « fenerive » : la recherche ignore accents et majuscules
    public static String normaliser(String texte) {
        String sansAccents = Normalizer.normalize(texte, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return sansAccents.toLowerCase(Locale.ROOT).trim();
    }
}
