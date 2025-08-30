package bookrecommender.server.librerie;

import bookrecommender.condivisi.librerie.Libreria;
import bookrecommender.server.utili.DBConnectionSingleton;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementazione JDBC del DAO per le librerie.
 * DAO stateless: fornisce metodi "comodi" (gestione interna della Connection)
 * e metodi "transaction-aware" che accettano una Connection come parametro.
 */
public class JdbcLibrerieDAO implements LibrerieDAO {

    private static final Logger logger = LogManager.getLogger(JdbcLibrerieDAO.class);

    // Query SQL
    private static final String INSERT_LIBRERIA =
        "INSERT INTO Librerie (user_id, nome_libreria, data_creazione) VALUES (?, ?, ?) RETURNING libreria_id";

    private static final String SELECT_LIBRERIA_BY_ID =
        "SELECT * FROM Librerie WHERE libreria_id = ?";

    private static final String SELECT_LIBRERIE_BY_USER_ID =
        "SELECT * FROM Librerie WHERE user_id = ? ORDER BY nome_libreria";

    private static final String SELECT_LIBRERIA_BY_USER_AND_NOME =
        "SELECT * FROM Librerie WHERE user_id = ? AND nome_libreria = ?";

    private static final String UPDATE_LIBRERIA =
        "UPDATE Librerie SET nome_libreria = ? WHERE libreria_id = ?";

    private static final String DELETE_LIBRERIA =
        "DELETE FROM Librerie WHERE libreria_id = ?";

    private static final String INSERT_LIBRO_IN_LIBRERIA =
        "INSERT INTO Libreria_Libro (libreria_id, libro_id, data_inserimento) VALUES (?, ?, ?)";

    private static final String DELETE_LIBRO_FROM_LIBRERIA =
        "DELETE FROM Libreria_Libro WHERE libreria_id = ? AND libro_id = ?";

    private static final String SELECT_LIBRI_IN_LIBRERIA =
        "SELECT libro_id FROM Libreria_Libro WHERE libreria_id = ? ORDER BY data_inserimento";

    private static final String CHECK_LIBRO_IN_LIBRERIA =
        "SELECT COUNT(*) FROM Libreria_Libro WHERE libreria_id = ? AND libro_id = ?";

    private static final String CHECK_NOME_LIBRERIA_EXISTS =
        "SELECT COUNT(*) FROM Librerie WHERE user_id = ? AND nome_libreria = ?";

    public JdbcLibrerieDAO() { }

    // ----------------------------
    // API "comode" (gestione interna della Connection)
    // ----------------------------

    @Override
    public Libreria creaLibreria(Libreria libreria) {
        try (Connection conn = DBConnectionSingleton.openNewConnection()) {
            return creaLibreria(conn, libreria);
        } catch (SQLException e) {
            logger.error("Errore durante la creazione della libreria", e);
            return null;
        }
    }

    @Override
    public Libreria getLibreriaById(int libreriaId) {
        try (Connection conn = DBConnectionSingleton.openNewConnection()) {
            return getLibreriaById(conn, libreriaId);
        } catch (SQLException e) {
            logger.error("Errore durante il recupero della libreria con ID: {}", libreriaId, e);
            return null;
        }
    }

    @Override
    public List<Libreria> getLibrerieByUserId(String userId) {
        try (Connection conn = DBConnectionSingleton.openNewConnection()) {
            return getLibrerieByUserId(conn, userId);
        } catch (SQLException e) {
            logger.error("Errore durante il recupero delle librerie per l'utente: {}", userId, e);
            return new ArrayList<>();
        }
    }

    @Override
    public Libreria getLibreriaByUserIdAndNome(String userId, String nomeLibreria) {
        try (Connection conn = DBConnectionSingleton.openNewConnection()) {
            return getLibreriaByUserIdAndNome(conn, userId, nomeLibreria);
        } catch (SQLException e) {
            logger.error("Errore durante il recupero della libreria per utente: {} e nome: {}", userId, nomeLibreria, e);
            return null;
        }
    }

    @Override
    public boolean aggiornaLibreria(Libreria libreria) {
        try (Connection conn = DBConnectionSingleton.openNewConnection()) {
            return aggiornaLibreria(conn, libreria);
        } catch (SQLException e) {
            logger.error("Errore durante l'aggiornamento della libreria", e);
            return false;
        }
    }

    @Override
    public boolean eliminaLibreria(int libreriaId) {
        try (Connection conn = DBConnectionSingleton.openNewConnection()) {
            return eliminaLibreria(conn, libreriaId);
        } catch (SQLException e) {
            logger.error("Errore durante l'eliminazione della libreria", e);
            return false;
        }
    }

    @Override
    public boolean aggiungiLibroALibreria(int libreriaId, long libroId) {
        try (Connection conn = DBConnectionSingleton.openNewConnection()) {
            return aggiungiLibroALibreria(conn, libreriaId, libroId);
        } catch (SQLException e) {
            logger.error("Errore durante l'aggiunta del libro {} alla libreria {}", libroId, libreriaId, e);
            return false;
        }
    }

    @Override
    public boolean rimuoviLibroDaLibreria(int libreriaId, long libroId) {
        try (Connection conn = DBConnectionSingleton.openNewConnection()) {
            return rimuoviLibroDaLibreria(conn, libreriaId, libroId);
        } catch (SQLException e) {
            logger.error("Errore durante la rimozione del libro {} dalla libreria {}", libroId, libreriaId, e);
            return false;
        }
    }

    @Override
    public boolean isNomeLibreriaEsistente(String userId, String nomeLibreria) {
        try (Connection conn = DBConnectionSingleton.openNewConnection()) {
            return isNomeLibreriaEsistente(conn, userId, nomeLibreria);
        } catch (SQLException e) {
            logger.error("Errore durante la verifica del nome libreria esistente", e);
            return false;
        }
    }

    @Override
    public boolean isLibroInLibreria(int libreriaId, long libroId) {
        try (Connection conn = DBConnectionSingleton.openNewConnection()) {
            return isLibroInLibreria(conn, libreriaId, libroId);
        } catch (SQLException e) {
            logger.error("Errore durante la verifica del libro nella libreria", e);
            return false;
        }
    }

    @Override
    public List<Long> getLibriIdsInLibreria(int libreriaId) {
        try (Connection conn = DBConnectionSingleton.openNewConnection()) {
            return getLibriIdsInLibreria(conn, libreriaId);
        } catch (SQLException e) {
            logger.error("Errore durante il recupero dei libri nella libreria: {}", libreriaId, e);
            return new ArrayList<>();
        }
    }

    // ----------------------------
    // API "transaction-aware" (accettano Connection come parametro)
    // ----------------------------

    public Libreria creaLibreria(Connection conn, Libreria libreria) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(INSERT_LIBRERIA)) {
            stmt.setString(1, libreria.userId());
            stmt.setString(2, libreria.nomeLibreria());
            stmt.setTimestamp(3, Timestamp.valueOf(
                    libreria.dataCreazione() != null ? libreria.dataCreazione() : LocalDateTime.now()
            ));

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int libreriaId = rs.getInt(1);
                    logger.info("Libreria creata con ID: {}", libreriaId);
                    return new Libreria(libreriaId, libreria.userId(), libreria.nomeLibreria(), libreria.dataCreazione(), libreria.libriIds());
                }
            }
        }
        return null;
    }

    public Libreria getLibreriaById(Connection conn, int libreriaId) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(SELECT_LIBRERIA_BY_ID)) {
            stmt.setInt(1, libreriaId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToLibreria(conn, rs);
                }
            }
        }
        return null;
    }

    public List<Libreria> getLibrerieByUserId(Connection conn, String userId) throws SQLException {
        List<Libreria> librerie = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(SELECT_LIBRERIE_BY_USER_ID)) {
            stmt.setString(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    librerie.add(mapResultSetToLibreria(conn, rs));
                }
            }
        }
        return librerie;
    }

    public Libreria getLibreriaByUserIdAndNome(Connection conn, String userId, String nomeLibreria) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(SELECT_LIBRERIA_BY_USER_AND_NOME)) {
            stmt.setString(1, userId);
            stmt.setString(2, nomeLibreria);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToLibreria(conn, rs);
                }
            }
        }
        return null;
    }

    public boolean aggiornaLibreria(Connection conn, Libreria libreria) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(UPDATE_LIBRERIA)) {
            stmt.setString(1, libreria.nomeLibreria());
            stmt.setInt(2, libreria.libreriaId());
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Libreria aggiornata con ID: {}", libreria.libreriaId());
                return true;
            }
        }
        return false;
    }

    public boolean eliminaLibreria(Connection conn, int libreriaId) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(DELETE_LIBRERIA)) {
            stmt.setInt(1, libreriaId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        }
    }

    public boolean aggiungiLibroALibreria(Connection conn, int libreriaId, long libroId) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(INSERT_LIBRO_IN_LIBRERIA)) {
            stmt.setInt(1, libreriaId);
            stmt.setLong(2, libroId);
            stmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Libro {} aggiunto alla libreria {}", libroId, libreriaId);
                return true;
            }
        }
        return false;
    }

    public boolean rimuoviLibroDaLibreria(Connection conn, int libreriaId, long libroId) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(DELETE_LIBRO_FROM_LIBRERIA)) {
            stmt.setInt(1, libreriaId);
            stmt.setLong(2, libroId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        }
    }

    public boolean isNomeLibreriaEsistente(Connection conn, String userId, String nomeLibreria) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(CHECK_NOME_LIBRERIA_EXISTS)) {
            stmt.setString(1, userId);
            stmt.setString(2, nomeLibreria);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    public boolean isLibroInLibreria(Connection conn, int libreriaId, long libroId) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(CHECK_LIBRO_IN_LIBRERIA)) {
            stmt.setInt(1, libreriaId);
            stmt.setLong(2, libroId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    public List<Long> getLibriIdsInLibreria(Connection conn, int libreriaId) throws SQLException {
        List<Long> libriIds = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(SELECT_LIBRI_IN_LIBRERIA)) {
            stmt.setInt(1, libreriaId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    libriIds.add(rs.getLong("libro_id"));
                }
            }
        }
        return libriIds;
    }

    // ----------------------------
    // Helper: mapping ResultSet -> Libreria usando la Connection corrente
    // ----------------------------
    private Libreria mapResultSetToLibreria(Connection conn, ResultSet rs) throws SQLException {
        int libreriaId = rs.getInt("libreria_id");
        String userId = rs.getString("user_id");
        String nomeLibreria = rs.getString("nome_libreria");
        Timestamp ts = rs.getTimestamp("data_creazione");
        LocalDateTime dataCreazione = ts != null ? ts.toLocalDateTime() : LocalDateTime.now();

        // Recupera gli ID dei libri usando la stessa Connection (evita apertura di nuove connection)
        List<Long> libriIds = getLibriIdsInLibreria(conn, libreriaId);

        return new Libreria(libreriaId, userId, nomeLibreria, dataCreazione, libriIds);
    }
}