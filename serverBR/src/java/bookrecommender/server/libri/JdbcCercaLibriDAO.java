package bookrecommender.server.libri;

import bookrecommender.condivisi.libri.Libro;
import bookrecommender.server.utili.DBConnectionSingleton;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementazione JDBC (Java Database Connectivity) dell'interfaccia {@link LibroDAO}.
 * <p>
 * Questa classe è responsabile della persistenza e del recupero degli oggetti {@link Libro}
 * da un database relazionale. Implementa tutte le operazioni di accesso ai dati definite
 * nel contratto {@link LibroDAO}.
 * <p>
 * La classe è progettata per essere <b>stateless</b> e offre due tipi di API:
 * <ul>
 *     <li><b>API "convenience"</b>: implementano direttamente i metodi dell'interfaccia
 *         {@code LibroDAO}. Gestiscono autonomamente l'apertura e la chiusura della
 *         connessione al database per ogni singola operazione, utilizzando un
 *         {@code try-with-resources} su {@link DBConnectionSingleton#openNewConnection()}.
 *         Sono semplici da usare ma non adatte a transazioni complesse.</li>
 *     <li><b>API "transaction-aware"</b>: sono metodi pubblici aggiuntivi che accettano
 *         un'istanza di {@link Connection} come parametro. Questo permette a un livello
 *         superiore (es. un Service Layer) di controllare il ciclo di vita della transazione
 *         (commit/rollback) quando più operazioni DAO devono essere eseguite atomicamente.</li>
 * </ul>
 * @author Abou Aziz Sara Hesham Abdel Hamid 757004
 * @author Ben Mahjoub Ali 759560
 * @author Hidri Mohamed Taha 756235
 * @author Zoghbani Lilia 759652
 * @see LibroDAO
 * @see Libro
 * @see DBConnectionSingleton
 * @version 1.0
 */
public class JdbcCercaLibriDAO implements LibroDAO {
    private static final Logger logger = LogManager.getLogger(JdbcCercaLibriDAO.class);

    // == Costanti per le query SQL ==
    private static final String QUERY_CREA_LIBRO = "INSERT INTO Libri (titolo, autori, anno, descrizione, categorie, editore, prezzo) VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING *";
    private static final String QUERY_GET_LIBRO_BY_ID = "SELECT * FROM Libri WHERE id = ?";
    private static final String QUERY_CERCA_LIBRI_PER_TITOLO = "SELECT * FROM Libri WHERE LOWER(titolo) LIKE LOWER(?) ORDER BY titolo";
    private static final String QUERY_CERCA_LIBRI_PER_AUTORE = "SELECT * FROM Libri WHERE LOWER(autori) LIKE LOWER(?) ORDER BY titolo";
    private static final String QUERY_CERCA_LIBRI_PER_AUTORE_E_ANNO = "SELECT * FROM Libri WHERE LOWER(autori) LIKE LOWER(?) AND anno = ? ORDER BY titolo";
    private static final String QUERY_CERCA_LIBRO_PER_ID = "SELECT * FROM Libri WHERE id = ?";

    /**
     * Costruttore di default.
     * Non esegue alcuna operazione speciale.
     */
    public JdbcCercaLibriDAO() { }

   

    /**
     * {@inheritDoc}
     * <p>
     * Questa implementazione gestisce autonomamente la connessione al database.
     * In caso di errore, logga l'eccezione e restituisce {@code null}.
     */
    @Override
    public Libro creaLibro(String titolo, String autore, String descrizione, String categoria, String year, String price) {
        try (Connection conn = DBConnectionSingleton.openNewConnection()) {
            return creaLibro(conn, titolo, autore, descrizione, categoria, year, price);
        } catch (SQLException e) {
            logger.error("Errore durante la creazione del libro: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Questa implementazione gestisce autonomamente la connessione al database.
     * In caso di errore, logga l'eccezione e restituisce {@code null}.
     */
    @Override
    public Libro getLibroById(int id) {
        try (Connection conn = DBConnectionSingleton.openNewConnection()) {
            return getLibroById(conn, id);
        } catch (SQLException e) {
            logger.error("Errore durante la ricerca libro per ID " + id + ": " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Questa implementazione gestisce autonomamente la connessione al database.
     * In caso di errore, logga l'eccezione e restituisce una lista vuota.
     */
    @Override
    public List<Libro> cercaLibriPerTitolo(String titolo) {
        try (Connection conn = DBConnectionSingleton.openNewConnection()) {
            return cercaLibriPerTitolo(conn, titolo);
        } catch (SQLException e) {
            logger.error("Errore durante la ricerca libri per titolo '" + titolo + "': " + e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Questa implementazione gestisce autonomamente la connessione al database.
     * In caso di errore, logga l'eccezione e restituisce una lista vuota.
     */
    @Override
    public List<Libro> cercaLibriPerAutore(String autore) {
        try (Connection conn = DBConnectionSingleton.openNewConnection()) {
            return cercaLibriPerAutore(conn, autore);
        } catch (SQLException e) {
            logger.error("Errore durante la ricerca libri per autore '" + autore + "': " + e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Questa implementazione gestisce autonomamente la connessione al database.
     * In caso di errore, logga l'eccezione e restituisce una lista vuota.
     */
    @Override
    public List<Libro> cercaLibriPerAutoreEAnno(String autore, String anno) {
        try (Connection conn = DBConnectionSingleton.openNewConnection()) {
            return cercaLibriPerAutoreEAnno(conn, autore, anno);
        } catch (SQLException e) {
            logger.error("Errore durante la ricerca libri per autore '" + autore + "' e anno '" + anno + "': " + e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Questa implementazione gestisce autonomamente la connessione al database.
     * In caso di errore, logga l'eccezione e restituisce una lista vuota.
     */
    @Override
    public List<Libro> cercaLibroPerId(Long id) {
        try (Connection conn = DBConnectionSingleton.openNewConnection()) {
            return cercaLibroPerId(conn, id);
        } catch (SQLException e) {
            logger.error("Errore durante la ricerca libro per ID " + id + ": " + e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    // ----------------------------
    // API "transaction-aware" (ricevono Connection come parametro)
    // ----------------------------

    /**
     * Crea un nuovo libro nel database utilizzando una connessione esistente.
     * <p>
     * Questo metodo è progettato per essere eseguito all'interno di una transazione
     * gestita da un livello superiore. Non gestisce commit o rollback.
     *
     * @param connection la connessione al database da utilizzare.
     * @param titolo il titolo del libro.
     * @param autore l'autore del libro.
     * @param descrizione la descrizione del libro.
     * @param categoria la categoria del libro.
     * @param year l'anno di pubblicazione.
     * @param price il prezzo del libro.
     * @return l'oggetto {@link Libro} appena creato, o {@code null} se la creazione fallisce.
     * @throws SQLException se si verifica un errore di accesso al database.
     */
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

    /**
     * Recupera un libro tramite ID utilizzando una connessione esistente.
     * <p>
     * Questo metodo è progettato per essere eseguito all'interno di una transazione
     * gestita da un livello superiore.
     *
     * @param conn la connessione al database da utilizzare.
     * @param id l'ID del libro da recuperare.
     * @return l'oggetto {@link Libro} corrispondente, o {@code null} se non trovato.
     * @throws SQLException se si verifica un errore di accesso al database.
     */
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

    /**
     * Cerca libri per titolo utilizzando una connessione esistente.
     * La ricerca è case-insensitive e parziale (contiene).
     * <p>
     * Questo metodo è progettato per essere eseguito all'interno di una transazione.
     *
     * @param conn la connessione al database da utilizzare.
     * @param titolo il testo da cercare nel titolo.
     * @return una {@link List} di {@link Libro} corrispondenti.
     * @throws SQLException se si verifica un errore di accesso al database.
     */
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

    /**
     * Cerca libri per autore utilizzando una connessione esistente.
     * La ricerca è case-insensitive e parziale (contiene).
     * <p>
     * Questo metodo è progettato per essere eseguito all'interno di una transazione.
     *
     * @param conn la connessione al database da utilizzare.
     * @param autore il testo da cercare nel nome dell'autore.
     * @return una {@link List} di {@link Libro} corrispondenti.
     * @throws SQLException se si verifica un errore di accesso al database.
     */
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

    /**
     * Cerca libri per autore e anno utilizzando una connessione esistente.
     * La ricerca sull'autore è case-insensitive e parziale, quella sull'anno è esatta.
     * <p>
     * Questo metodo è progettato per essere eseguito all'interno di una transazione.
     *
     * @param conn la connessione al database da utilizzare.
     * @param autore il testo da cercare nel nome dell'autore.
     * @param anno l'anno esatto di pubblicazione.
     * @return una {@link List} di {@link Libro} corrispondenti.
     * @throws SQLException se si verifica un errore di accesso al database.
     */
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

    /**
     * Cerca un libro per ID e lo restituisce in una lista, utilizzando una connessione esistente.
     * <p>
     * Questo metodo è progettato per essere eseguito all'interno di una transazione.
     *
     * @param conn la connessione al database da utilizzare.
     * @param id l'ID del libro da recuperare.
     * @return una {@link List} contenente il {@link Libro} trovato, o una lista vuota.
     * @throws SQLException se si verifica un errore di accesso al database.
     */
    public List<Libro> cercaLibroPerId(Connection conn, Long id) throws SQLException {
        List<Libro> libri = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(QUERY_CERCA_LIBRO_PER_ID)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    libri.add(mapResultSetToLibro(rs));
                }
            }
        }
        logger.info("Trovati {} libri per ID '{}'", libri.size(), id);
        return libri;
    }

   
    /**
     * Metodo helper per mappare una riga di un {@link ResultSet} a un oggetto {@link Libro}.
     *
     * @param rs il ResultSet posizionato sulla riga da mappare.
     * @return un nuovo oggetto {@link Libro} popolato con i dati della riga corrente.
     * @throws SQLException se si verifica un errore durante la lettura dei dati dal ResultSet
     *                      (es. nomi di colonna non validi).
     */
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
