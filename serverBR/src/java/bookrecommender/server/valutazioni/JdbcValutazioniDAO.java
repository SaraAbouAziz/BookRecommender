package bookrecommender.server.valutazioni;

import bookrecommender.condivisi.valutazioni.ValutazioneDettagliata;
import bookrecommender.server.utili.DBConnectionSingleton;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Implementazione JDBC (Java Database Connectivity) dell'interfaccia {@link ValutazioniDAO}.
 * <p>
 * Questa classe è responsabile della persistenza e del recupero degli oggetti
 * relativi alle valutazioni (come {@link ValutazioneDettagliata}) da un database
 * relazionale. Implementa tutte le operazioni di accesso ai dati definite nel
 * contratto {@link ValutazioniDAO}, interagendo principalmente con la tabella
 * {@code ValutazioniLibri}.
 * <p>
 * Ogni metodo gestisce autonomamente la connessione al database tramite il singleton
 * {@link DBConnectionSingleton}, aprendo e chiudendo una connessione per ogni
 * operazione. La gestione degli errori è semplificata: le eccezioni SQL vengono
 * catturate e gestite internamente, restituendo valori di default (es. {@code false},
 * {@code null}, o collezioni vuote) per indicare un fallimento.
 *
 * @author Abou Aziz Sara Hesham Abdel Hamid 757004
 * @author Ben Mahjoub Ali 759560
 * @author Hidri Mohamed Taha 756235
 * @author Zoghbani Lilia 759652
 * @see ValutazioniDAO
 * @see ValutazioneDettagliata
 * @see DBConnectionSingleton
 * @version 1.0
 */
public class JdbcValutazioniDAO implements ValutazioniDAO {

    /** {@inheritDoc} */
    @Override
    public boolean isLibroGiaValutato(int libroId, String userId) {
        String sql = "SELECT 1 FROM ValutazioniLibri WHERE user_id = ? AND libro_id = ? LIMIT 1";
        try (Connection c = DBConnectionSingleton.openNewConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, userId);
            ps.setInt(2, libroId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (Exception e) {
            return false;
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean salvaValutazione(String userId, int libroId, String nomeLibreria, int stile, String stileNote, int contenuto, String contenutoNote, int gradevolezza, String gradevolezzaNote, int originalita, String originalitaNote, int edizione, String edizioneNote, double votoFinale, String commentoFinale) {
        Integer libreriaId = ensureLibreriaId(userId, nomeLibreria);
        if (libreriaId == null) {
            return false;
        }
        String sql = "INSERT INTO ValutazioniLibri (user_id, libreria_id, libro_id, stile_score, contenuto_score, gradimento_score, originalita_score, qualita_score, voto_complessivo, stile_note, contenuto_note, gradimento_note, originalita_note, qualita_note, commento_finale) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection c = DBConnectionSingleton.openNewConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, userId);
            ps.setInt(2, libreriaId);
            ps.setInt(3, libroId);
            ps.setInt(4, stile);
            ps.setInt(5, contenuto);
            ps.setInt(6, gradevolezza);
            ps.setInt(7, originalita);
            ps.setInt(8, edizione);
            ps.setDouble(9, votoFinale);
            ps.setString(10, stileNote);
            ps.setString(11, contenutoNote);
            ps.setString(12, gradevolezzaNote);
            ps.setString(13, originalitaNote);
            ps.setString(14, edizioneNote);
            ps.setString(15, commentoFinale);
            return ps.executeUpdate() == 1;
        } catch (Exception e) {
            return false;
        }
    }

    /** {@inheritDoc} */
    @Override
    public List<Map<String, Object>> caricaValutazioniLibro(int libroId) {
        String sql = "SELECT user_id, libro_id, stile_score, stile_note, contenuto_score, contenuto_note, gradimento_score, gradimento_note, originalita_score, originalita_note, qualita_score, qualita_note, voto_complessivo, commento_finale FROM ValutazioniLibri WHERE libro_id = ?";
        List<Map<String, Object>> out = new ArrayList<>();
        try (Connection c = DBConnectionSingleton.openNewConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, libroId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> v = new HashMap<>();
                    v.put("userId", rs.getString("user_id"));
                    v.put("libroId", rs.getInt("libro_id"));
                    v.put("stile", rs.getInt("stile_score"));
                    v.put("stileNote", rs.getString("stile_note"));
                    v.put("contenuto", rs.getInt("contenuto_score"));
                    v.put("contenutoNote", rs.getString("contenuto_note"));
                    v.put("gradevolezza", rs.getInt("gradimento_score"));
                    v.put("gradevolezzaNote", rs.getString("gradimento_note"));
                    v.put("originalita", rs.getInt("originalita_score"));
                    v.put("originalitaNote", rs.getString("originalita_note"));
                    v.put("edizione", rs.getInt("qualita_score"));
                    v.put("edizioneNote", rs.getString("qualita_note"));
                    v.put("votoFinale", rs.getDouble("voto_complessivo"));
                    v.put("commentoFinale", rs.getString("commento_finale"));
                    out.add(v);
                }
            }
        } catch (Exception e) {
            // return empty
        }
        return out;
    }

    /** {@inheritDoc} */
    @Override
    public double calcolaMediaValutazioni(int libroId) {
        String sql = "SELECT AVG(voto_complessivo) AS media FROM ValutazioniLibri WHERE libro_id = ?";
        try (Connection c = DBConnectionSingleton.openNewConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, libroId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("media");
                }
            }
        } catch (Exception e) {
            // ignore
        }
        return 0.0;
    }

    /** {@inheritDoc} */
    @Override
    public int getNumeroValutazioni(int libroId) {
        String sql = "SELECT COUNT(*) AS n FROM ValutazioniLibri WHERE libro_id = ?";
        try (Connection c = DBConnectionSingleton.openNewConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, libroId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("n");
                }
            }
        } catch (Exception e) {
            // ignore
        }
        return 0;
    }
    
    /** {@inheritDoc} */
    @Override
    public double calcolaMediaStile(int libroId) {
        String sql = "SELECT AVG(stile_score) AS media FROM ValutazioniLibri WHERE libro_id = ?";
        try (Connection c = DBConnectionSingleton.openNewConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, libroId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("media");
                }
            }
        } catch (Exception e) {
            // ignore
        }
        return 0.0;
    }
    
    /** {@inheritDoc} */
    @Override
    public double calcolaMediaContenuto(int libroId) {
        String sql = "SELECT AVG(contenuto_score) AS media FROM ValutazioniLibri WHERE libro_id = ?";
        try (Connection c = DBConnectionSingleton.openNewConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, libroId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("media");
                }
            }
        } catch (Exception e) {
            // ignore
        }
        return 0.0;
    }
    
    /** {@inheritDoc} */
    @Override
    public double calcolaMediaGradevolezza(int libroId) {
        String sql = "SELECT AVG(gradimento_score) AS media FROM ValutazioniLibri WHERE libro_id = ?";
        try (Connection c = DBConnectionSingleton.openNewConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, libroId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("media");
                }
            }
        } catch (Exception e) {
            // ignore
        }
        return 0.0;
    }
    
    /** {@inheritDoc} */
    @Override
    public double calcolaMediaOriginalita(int libroId) {
        String sql = "SELECT AVG(originalita_score) AS media FROM ValutazioniLibri WHERE libro_id = ?";
        try (Connection c = DBConnectionSingleton.openNewConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, libroId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("media");
                }
            }
        } catch (Exception e) {
            // ignore
        }
        return 0.0;
    }
    
    /** {@inheritDoc} */
    @Override
    public double calcolaMediaEdizione(int libroId) {
        String sql = "SELECT AVG(qualita_score) AS media FROM ValutazioniLibri WHERE libro_id = ?";
        try (Connection c = DBConnectionSingleton.openNewConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, libroId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("media");
                }
            }
        } catch (Exception e) {
            // ignore
        }
        return 0.0;
    }
    
    /**
     * Assicura che esista una libreria per l'utente con il nome specificato e ne restituisce l'ID.
     * <p>
     * Questo metodo implementa una logica "get-or-create". Prima tenta di recuperare
     * l'ID di una libreria esistente. Se non la trova, ne crea una nuova e restituisce
     * l'ID appena generato. Questa operazione non è atomica e potrebbe non essere sicura
     * in ambienti ad alta concorrenza.
     *
     * @param userId L'ID dell'utente proprietario della libreria.
     * @param nomeLibreria Il nome della libreria.
     * @return L'ID della libreria (esistente o appena creata), o {@code null} in caso di errore.
     */
    private Integer ensureLibreriaId(String userId, String nomeLibreria) {
        String selectSql = "SELECT libreria_id FROM Librerie WHERE user_id = ? AND nome_libreria = ?";
        try (Connection c = DBConnectionSingleton.openNewConnection()) {
            try (PreparedStatement ps = c.prepareStatement(selectSql)) {
                ps.setString(1, userId);
                ps.setString(2, nomeLibreria);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }

            String insertSql = "INSERT INTO Librerie (user_id, nome_libreria) VALUES (?, ?) RETURNING libreria_id";
            try (PreparedStatement psIns = c.prepareStatement(insertSql)) {
                psIns.setString(1, userId);
                psIns.setString(2, nomeLibreria);
                try (ResultSet rs = psIns.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public List<ValutazioneDettagliata> findValutazioniDettagliateByUser(String userId) {
        String sql = """
            SELECT v.user_id, v.libro_id, l.titolo, l.autori, lib.nome_libreria,
                   v.stile_score, v.stile_note, v.contenuto_score, v.contenuto_note,
                   v.gradimento_score, v.gradimento_note, v.originalita_score, v.originalita_note,
                   v.qualita_score, v.qualita_note, v.voto_complessivo, v.commento_finale
            FROM ValutazioniLibri v
            JOIN Libri l ON v.libro_id = l.id
            JOIN Librerie lib ON v.libreria_id = lib.libreria_id
            WHERE v.user_id = ?
            ORDER BY l.titolo
            """;

        List<ValutazioneDettagliata> result = new ArrayList<>();
        try (Connection c = DBConnectionSingleton.openNewConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(new ValutazioneDettagliata(
                        rs.getString("user_id"),
                        rs.getInt("libro_id"),
                        rs.getString("titolo"),
                        rs.getString("autori"),
                        rs.getString("nome_libreria"),
                        rs.getInt("stile_score"),
                        rs.getString("stile_note"),
                        rs.getInt("contenuto_score"),
                        rs.getString("contenuto_note"),
                        rs.getInt("gradimento_score"),
                        rs.getString("gradimento_note"),
                        rs.getInt("originalita_score"),
                        rs.getString("originalita_note"),
                        rs.getInt("qualita_score"),
                        rs.getString("qualita_note"),
                        rs.getDouble("voto_complessivo"),
                        rs.getString("commento_finale"),
                        null // dataValutazione se presente nel DB
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public boolean aggiornaValutazione(String userId, int libroId, int stile, String stileNote,
                                      int contenuto, String contenutoNote, int gradevolezza, String gradevolezzaNote,
                                      int originalita, String originalitaNote, int edizione, String edizioneNote,
                                      double votoFinale, String commentoFinale) {
        String sql = """
            UPDATE ValutazioniLibri SET
                stile_score = ?, stile_note = ?,
                contenuto_score = ?, contenuto_note = ?,
                gradimento_score = ?, gradimento_note = ?,
                originalita_score = ?, originalita_note = ?,
                qualita_score = ?, qualita_note = ?,
                voto_complessivo = ?, commento_finale = ?
            WHERE user_id = ? AND libro_id = ?
            """;

        try (Connection c = DBConnectionSingleton.openNewConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, stile);
            ps.setString(2, stileNote);
            ps.setInt(3, contenuto);
            ps.setString(4, contenutoNote);
            ps.setInt(5, gradevolezza);
            ps.setString(6, gradevolezzaNote);
            ps.setInt(7, originalita);
            ps.setString(8, originalitaNote);
            ps.setInt(9, edizione);
            ps.setString(10, edizioneNote);
            ps.setDouble(11, votoFinale);
            ps.setString(12, commentoFinale);
            ps.setString(13, userId);
            ps.setInt(14, libroId);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean eliminaValutazione(String userId, int libroId) {
        String sql = "DELETE FROM ValutazioniLibri WHERE user_id = ? AND libro_id = ?";
        try (Connection c = DBConnectionSingleton.openNewConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, userId);
            ps.setInt(2, libroId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
