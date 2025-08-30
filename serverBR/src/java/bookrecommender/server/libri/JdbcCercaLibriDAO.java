package bookrecommender.server.libri;

import bookrecommender.condivisi.libri.Libro;
import bookrecommender.server.utili.DBConnectionSingleton;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementazione JDBC dell'interfaccia {@link LibroDAO}.
 * DAO stateless: fornisce API "comode" (gestione interna della connection)
 * e API "transaction-aware" che accettano una Connection passata dal Service Layer.
 */
public class JdbcCercaLibriDAO implements LibroDAO {
    private static final Logger logger = LogManager.getLogger(JdbcCercaLibriDAO.class);

    private static final String QUERY_CREA_LIBRO = "INSERT INTO Libri (titolo, autori, anno, descrizione, categorie, editore, prezzo) VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING *";
    private static final String QUERY_GET_LIBRO_BY_ID = "SELECT * FROM Libri WHERE id = ?";
    private static final String QUERY_CERCA_LIBRI_PER_TITOLO = "SELECT * FROM Libri WHERE LOWER(titolo) LIKE LOWER(?) ORDER BY titolo";
    private static final String QUERY_CERCA_LIBRI_PER_AUTORE = "SELECT * FROM Libri WHERE LOWER(autori) LIKE LOWER(?) ORDER BY titolo";
    private static final String QUERY_CERCA_LIBRI_PER_AUTORE_E_ANNO = "SELECT * FROM Libri WHERE LOWER(autori) LIKE LOWER(?) AND anno = ? ORDER BY titolo";

    public JdbcCercaLibriDAO() { }

    // ----------------------------
    // API "comode" (gestiscono la Connection internamente)
    // ----------------------------

    @Override
    public Libro creaLibro(String titolo, String autore, String descrizione, String categoria, String year, String price) {
        try (Connection conn = DBConnectionSingleton.openNewConnection()) {
            return creaLibro(conn, titolo, autore, descrizione, categoria, year, price);
        } catch (SQLException e) {
            logger.error("Errore durante la creazione del libro: " + e.getMessage(), e);
            return null;
        }
    }

    @Override
    public Libro getLibroById(int id) {
        try (Connection conn = DBConnectionSingleton.openNewConnection()) {
            return getLibroById(conn, id);
        } catch (SQLException e) {
            logger.error("Errore durante la ricerca libro per ID " + id + ": " + e.getMessage(), e);
            return null;
        }
    }

    @Override
    public List<Libro> cercaLibriPerTitolo(String titolo) {
        try (Connection conn = DBConnectionSingleton.openNewConnection()) {
            return cercaLibriPerTitolo(conn, titolo);
        } catch (SQLException e) {
            logger.error("Errore durante la ricerca libri per titolo '" + titolo + "': " + e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<Libro> cercaLibriPerAutore(String autore) {
        try (Connection conn = DBConnectionSingleton.openNewConnection()) {
            return cercaLibriPerAutore(conn, autore);
        } catch (SQLException e) {
            logger.error("Errore durante la ricerca libri per autore '" + autore + "': " + e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<Libro> cercaLibriPerAutoreEAnno(String autore, String anno) {
        try (Connection conn = DBConnectionSingleton.openNewConnection()) {
            return cercaLibriPerAutoreEAnno(conn, autore, anno);
        } catch (SQLException e) {
            logger.error("Errore durante la ricerca libri per autore '" + autore + "' e anno '" + anno + "': " + e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    // ----------------------------
    // API "transaction-aware" (ricevono Connection come parametro)
    // ----------------------------

    public Libro creaLibro(Connection connection, String titolo, String autore, String descrizione, String categoria, String year, String price) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(QUERY_CREA_LIBRO)) {
            stmt.setString(1, titolo);
            stmt.setString(2, autore);
            stmt.setString(3, year);
            stmt.setString(4, descrizione);
            stmt.setString(5, categoria);
            stmt.setString(6, ""); // editore vuoto per ora
            stmt.setString(7, price);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToLibro(rs);
                }
            }
        }
        return null;
    }

    public Libro getLibroById(Connection conn, int id) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(QUERY_GET_LIBRO_BY_ID)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToLibro(rs);
                }
            }
        }
        return null;
    }

    public List<Libro> cercaLibriPerTitolo(Connection conn, String titolo) throws SQLException {
        List<Libro> libri = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(QUERY_CERCA_LIBRI_PER_TITOLO)) {
            stmt.setString(1, "%" + titolo + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    libri.add(mapResultSetToLibro(rs));
                }
            }
        }
        logger.info("Trovati {} libri per titolo '{}'", libri.size(), titolo);
        return libri;
    }

    public List<Libro> cercaLibriPerAutore(Connection conn, String autore) throws SQLException {
        List<Libro> libri = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(QUERY_CERCA_LIBRI_PER_AUTORE)) {
            stmt.setString(1, "%" + autore + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    libri.add(mapResultSetToLibro(rs));
                }
            }
        }
        logger.info("Trovati {} libri per autore '{}'", libri.size(), autore);
        return libri;
    }

    public List<Libro> cercaLibriPerAutoreEAnno(Connection conn, String autore, String anno) throws SQLException {
        List<Libro> libri = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(QUERY_CERCA_LIBRI_PER_AUTORE_E_ANNO)) {
            stmt.setString(1, "%" + autore + "%");
            stmt.setString(2, anno);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    libri.add(mapResultSetToLibro(rs));
                }
            }
        }
        logger.info("Trovati {} libri per autore '{}' e anno '{}'", libri.size(), autore, anno);
        return libri;
    }

    // ----------------------------
    // Helper
    // ----------------------------
    private Libro mapResultSetToLibro(ResultSet rs) throws SQLException {
        return new Libro(
                rs.getLong("id"),
                rs.getString("titolo"),
                rs.getString("autori"),
                rs.getString("anno"),
                rs.getString("descrizione"),
                rs.getString("categorie"),
                rs.getString("editore"),
                rs.getString("prezzo")
        );
    }
}
