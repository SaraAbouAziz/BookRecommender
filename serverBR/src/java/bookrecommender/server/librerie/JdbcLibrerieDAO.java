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
 * Implementazione JDBC (Java Database Connectivity) dell'interfaccia {@link LibrerieDAO}.
 * <p>
 * Questa classe gestisce la persistenza degli oggetti {@link Libreria} su un
 * database relazionale tramite JDBC. È responsabile di tutte le operazioni
 * CRUD (Create, Read, Update, Delete) per le entità {@code Libreria} e le
 * relative associazioni con i libri.
 * <p>
 * La classe è progettata per essere <b>stateless</b> e offre due tipi di API:
 * <ul>
 *     <li><b>API "convenience"</b>: implementano direttamente i metodi dell'interfaccia
 *         {@code LibrerieDAO}. Gestiscono autonomamente l'apertura e la chiusura della
 *         connessione al database per ogni singola operazione, utilizzando un
 *         {@code try-with-resources} su {@link DBConnectionSingleton#openNewConnection()}.
 *         Sono semplici da usare ma non adatte a transazioni complesse che coinvolgono
 *         più operazioni DAO.</li>
 *     <li><b>API "transaction-aware"</b>: sono metodi pubblici aggiuntivi che accettano
 *         un'istanza di {@link Connection} come parametro. Questo permette a un livello
 *         superiore (es. un Service Layer) di controllare il ciclo di vita della transazione
 *         (commit/rollback) quando più operazioni DAO devono essere eseguite atomicamente.</li>
 * </ul>
 *
 * @see LibrerieDAO
 * @see Libreria
 * @see DBConnectionSingleton
 */
public class JdbcLibrerieDAO implements LibrerieDAO {

    private static final Logger logger = LogManager.getLogger(JdbcLibrerieDAO.class);

    // == Costanti per le query SQL ==
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

    /**
     * Costruttore di default.
     * Non esegue alcuna operazione speciale, in quanto la classe è stateless.
     */
    public JdbcLibrerieDAO() { }

    // ----------------------------
    // API "comode" (gestione interna della Connection)
    // ----------------------------

    /**
     * {@inheritDoc}
     * <p>
     * Questa implementazione gestisce autonomamente la connessione al database.
     * Apre una nuova connessione, esegue l'operazione delegando al metodo
     * {@link #creaLibreria(Connection, Libreria)}, e chiude la connessione.
     * In caso di errore, logga l'eccezione e restituisce {@code null}.
     */
    @Override
    public Libreria creaLibreria(Libreria libreria) {
        try (Connection conn = DBConnectionSingleton.openNewConnection()) {
            return creaLibreria(conn, libreria);
        } catch (SQLException e) {
            logger.error("Errore durante la creazione della libreria", e);
            return null;
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Questa implementazione gestisce autonomamente la connessione al database.
     * Apre una nuova connessione, esegue la ricerca delegando al metodo
     * {@link #getLibreriaById(Connection, int)}, e chiude la connessione.
     * In caso di errore, logga l'eccezione e restituisce {@code null}.
     */
    @Override
    public Libreria getLibreriaById(int libreriaId) {
        try (Connection conn = DBConnectionSingleton.openNewConnection()) {
            return getLibreriaById(conn, libreriaId);
        } catch (SQLException e) {
            logger.error("Errore durante il recupero della libreria con ID: {}", libreriaId, e);
            return null;
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Questa implementazione gestisce autonomamente la connessione al database.
     * Apre una nuova connessione, esegue la ricerca delegando al metodo
     * {@link #getLibrerieByUserId(Connection, String)}, e chiude la connessione.
     * In caso di errore, logga l'eccezione e restituisce una lista vuota.
     */
    @Override
    public List<Libreria> getLibrerieByUserId(String userId) {
        try (Connection conn = DBConnectionSingleton.openNewConnection()) {
            return getLibrerieByUserId(conn, userId);
        } catch (SQLException e) {
            logger.error("Errore durante il recupero delle librerie per l'utente: {}", userId, e);
            return new ArrayList<>();
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Questa implementazione gestisce autonomamente la connessione al database.
     * Apre una nuova connessione, esegue la ricerca delegando al metodo
     * {@link #getLibreriaByUserIdAndNome(Connection, String, String)}, e chiude la connessione.
     * In caso di errore, logga l'eccezione e restituisce {@code null}.
     */
    @Override
    public Libreria getLibreriaByUserIdAndNome(String userId, String nomeLibreria) {
        try (Connection conn = DBConnectionSingleton.openNewConnection()) {
            return getLibreriaByUserIdAndNome(conn, userId, nomeLibreria);
        } catch (SQLException e) {
            logger.error("Errore durante il recupero della libreria per utente: {} e nome: {}", userId, nomeLibreria, e);
            return null;
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Questa implementazione gestisce autonomamente la connessione al database.
     * Apre una nuova connessione, esegue l'aggiornamento delegando al metodo
     * {@link #aggiornaLibreria(Connection, Libreria)}, e chiude la connessione.
     * In caso di errore, logga l'eccezione e restituisce {@code false}.
     */
    @Override
    public boolean aggiornaLibreria(Libreria libreria) {
        try (Connection conn = DBConnectionSingleton.openNewConnection()) {
            return aggiornaLibreria(conn, libreria);
        } catch (SQLException e) {
            logger.error("Errore durante l'aggiornamento della libreria", e);
            return false;
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Questa implementazione gestisce autonomamente la connessione al database.
     * Apre una nuova connessione, esegue l'eliminazione delegando al metodo
     * {@link #eliminaLibreria(Connection, int)}, e chiude la connessione.
     * In caso di errore, logga l'eccezione e restituisce {@code false}.
     */
    @Override
    public boolean eliminaLibreria(int libreriaId) {
        try (Connection conn = DBConnectionSingleton.openNewConnection()) {
            return eliminaLibreria(conn, libreriaId);
        } catch (SQLException e) {
            logger.error("Errore durante l'eliminazione della libreria", e);
            return false;
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Questa implementazione gestisce autonomamente la connessione al database.
     * Apre una nuova connessione, esegue l'operazione delegando al metodo
     * {@link #aggiungiLibroALibreria(Connection, int, long)}, e chiude la connessione.
     * In caso di errore, logga l'eccezione e restituisce {@code false}.
     */
    @Override
    public boolean aggiungiLibroALibreria(int libreriaId, long libroId) {
        try (Connection conn = DBConnectionSingleton.openNewConnection()) {
            return aggiungiLibroALibreria(conn, libreriaId, libroId);
        } catch (SQLException e) {
            logger.error("Errore durante l'aggiunta del libro {} alla libreria {}", libroId, libreriaId, e);
            return false;
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Questa implementazione gestisce autonomamente la connessione al database.
     * Apre una nuova connessione, esegue l'operazione delegando al metodo
     * {@link #rimuoviLibroDaLibreria(Connection, int, long)}, e chiude la connessione.
     * In caso di errore, logga l'eccezione e restituisce {@code false}.
     */
    @Override
    public boolean rimuoviLibroDaLibreria(int libreriaId, long libroId) {
        try (Connection conn = DBConnectionSingleton.openNewConnection()) {
            return rimuoviLibroDaLibreria(conn, libreriaId, libroId);
        } catch (SQLException e) {
            logger.error("Errore durante la rimozione del libro {} dalla libreria {}", libroId, libreriaId, e);
            return false;
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Questa implementazione gestisce autonomamente la connessione al database.
     * Apre una nuova connessione, esegue la verifica delegando al metodo
     * {@link #isNomeLibreriaEsistente(Connection, String, String)}, e chiude la connessione.
     * In caso di errore, logga l'eccezione e restituisce {@code false}.
     */
    @Override
    public boolean isNomeLibreriaEsistente(String userId, String nomeLibreria) {
        try (Connection conn = DBConnectionSingleton.openNewConnection()) {
            return isNomeLibreriaEsistente(conn, userId, nomeLibreria);
        } catch (SQLException e) {
            logger.error("Errore durante la verifica del nome libreria esistente", e);
            return false;
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Questa implementazione gestisce autonomamente la connessione al database.
     * Apre una nuova connessione, esegue la verifica delegando al metodo
     * {@link #isLibroInLibreria(Connection, int, long)}, e chiude la connessione.
     * In caso di errore, logga l'eccezione e restituisce {@code false}.
     */
    @Override
    public boolean isLibroInLibreria(int libreriaId, long libroId) {
        try (Connection conn = DBConnectionSingleton.openNewConnection()) {
            return isLibroInLibreria(conn, libreriaId, libroId);
        } catch (SQLException e) {
            logger.error("Errore durante la verifica del libro nella libreria", e);
            return false;
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Questa implementazione gestisce autonomamente la connessione al database.
     * Apre una nuova connessione, esegue la ricerca delegando al metodo
     * {@link #getLibriIdsInLibreria(Connection, int)}, e chiude la connessione.
     * In caso di errore, logga l'eccezione e restituisce una lista vuota.
     */
    @Override
    public List<Long> getLibriIdsInLibreria(int libreriaId) {
        try (Connection conn = DBConnectionSingleton.openNewConnection()) {
            return getLibriIdsInLibreria(conn, libreriaId);
        } catch (SQLException e) {
            logger.error("Errore durante il recupero dei libri nella libreria: {}", libreriaId, e);
            return new ArrayList<>();
        }
    }

    // -------------------------------------------------
    // API "transaction-aware" (ricevono Connection)
    // -------------------------------------------------

    /**
     * Crea una nuova libreria nel database utilizzando una connessione esistente.
     * <p>
     * Questo metodo è progettato per essere eseguito all'interno di una transazione
     * gestita da un livello superiore. Non gestisce commit o rollback.
     *
     * @param conn la connessione al database da utilizzare, che deve essere valida e aperta.
     * @param libreria l'oggetto {@link Libreria} da salvare. Vengono utilizzati i campi
     *                 {@code userId}, {@code nomeLibreria} e {@code dataCreazione}.
     * @return una nuova istanza di {@link Libreria} completa dell'ID generato dal database,
     *         o {@code null} se la creazione fallisce.
     * @throws SQLException se si verifica un errore di accesso al database.
     */
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
                    // Restituisce un nuovo oggetto Libreria con l'ID popolato
                    return new Libreria(libreriaId, libreria.userId(), libreria.nomeLibreria(), libreria.dataCreazione(), libreria.libriIds());
                }
            }
        }
        return null;
    }

    /**
     * Recupera una libreria tramite ID utilizzando una connessione esistente.
     * <p>
     * Questo metodo è progettato per essere eseguito all'interno di una transazione.
     *
     * @param conn la connessione al database da utilizzare.
     * @param libreriaId l'ID della libreria da recuperare.
     * @return l'oggetto {@link Libreria} corrispondente, completo della lista di ID dei libri,
     *         o {@code null} se non trovato.
     * @throws SQLException se si verifica un errore di accesso al database.
     */
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

    /**
     * Recupera tutte le librerie di un utente utilizzando una connessione esistente.
     * <p>
     * Questo metodo è progettato per essere eseguito all'interno di una transazione.
     *
     * @param conn la connessione al database da utilizzare.
     * @param userId l'ID dell'utente di cui recuperare le librerie.
     * @return una {@link List} di {@link Libreria}, ognuna completa della lista di ID dei libri.
     *         Restituisce una lista vuota se l'utente non ha librerie.
     * @throws SQLException se si verifica un errore di accesso al database.
     */
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

    /**
     * Recupera una specifica libreria di un utente tramite il suo nome, utilizzando una connessione esistente.
     * <p>
     * Questo metodo è progettato per essere eseguito all'interno di una transazione.
     *
     * @param conn la connessione al database da utilizzare.
     * @param userId l'ID dell'utente proprietario.
     * @param nomeLibreria il nome della libreria da cercare.
     * @return l'oggetto {@link Libreria} corrispondente, completo della lista di ID dei libri,
     *         o {@code null} se non trovato.
     * @throws SQLException se si verifica un errore di accesso al database.
     */
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

    /**
     * Aggiorna i dati di una libreria (es. il nome) utilizzando una connessione esistente.
     * <p>
     * Questo metodo è progettato per essere eseguito all'interno di una transazione.
     *
     * @param conn la connessione al database da utilizzare.
     * @param libreria l'oggetto {@link Libreria} con i dati aggiornati. L'ID viene usato per
     *                 identificare la riga da aggiornare.
     * @return {@code true} se l'aggiornamento ha avuto successo (almeno una riga modificata),
     *         {@code false} altrimenti.
     * @throws SQLException se si verifica un errore di accesso al database.
     */
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

    /**
     * Elimina una libreria dal database tramite il suo ID, utilizzando una connessione esistente.
     * <p>
     * L'eliminazione è a cascata sulla tabella di giunzione {@code Libreria_Libro} grazie
     * ai vincoli del database ({@code ON DELETE CASCADE}).
     * Questo metodo è progettato per essere eseguito all'interno di una transazione.
     *
     * @param conn la connessione al database da utilizzare.
     * @param libreriaId l'ID della libreria da eliminare.
     * @return {@code true} se l'eliminazione ha avuto successo (almeno una riga modificata),
     *         {@code false} altrimenti.
     * @throws SQLException se si verifica un errore di accesso al database.
     */
    public boolean eliminaLibreria(Connection conn, int libreriaId) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(DELETE_LIBRERIA)) {
            stmt.setInt(1, libreriaId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        }
    }

    /**
     * Associa un libro a una libreria nella tabella di giunzione, utilizzando una connessione esistente.
     * <p>
     * Questo metodo è progettato per essere eseguito all'interno di una transazione.
     *
     * @param conn la connessione al database da utilizzare.
     * @param libreriaId l'ID della libreria.
     * @param libroId l'ID del libro da aggiungere.
     * @return {@code true} se l'inserimento ha avuto successo (almeno una riga modificata),
     *         {@code false} altrimenti.
     * @throws SQLException se si verifica un errore di accesso al database, ad esempio per
     *                      violazione di vincoli di chiave primaria (libro già presente).
     */
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

    /**
     * Rimuove l'associazione di un libro da una libreria, utilizzando una connessione esistente.
     * <p>
     * Questo metodo è progettato per essere eseguito all'interno di una transazione.
     *
     * @param conn la connessione al database da utilizzare.
     * @param libreriaId l'ID della libreria.
     * @param libroId l'ID del libro da rimuovere.
     * @return {@code true} se la rimozione ha avuto successo (almeno una riga modificata),
     *         {@code false} altrimenti.
     * @throws SQLException se si verifica un errore di accesso al database.
     */
    public boolean rimuoviLibroDaLibreria(Connection conn, int libreriaId, long libroId) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(DELETE_LIBRO_FROM_LIBRERIA)) {
            stmt.setInt(1, libreriaId);
            stmt.setLong(2, libroId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        }
    }

    /**
     * Verifica se un dato nome di libreria è già in uso da parte di un utente,
     * utilizzando una connessione esistente.
     * <p>
     * Questo metodo è progettato per essere eseguito all'interno di una transazione.
     *
     * @param conn la connessione al database da utilizzare.
     * @param userId l'ID dell'utente.
     * @param nomeLibreria il nome della libreria da verificare.
     * @return {@code true} se il nome è già esistente per quell'utente, {@code false} altrimenti.
     * @throws SQLException se si verifica un errore di accesso al database.
     */
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

    /**
     * Verifica se un libro è già associato a una libreria, utilizzando una connessione esistente.
     * <p>
     * Questo metodo è progettato per essere eseguito all'interno di una transazione.
     *
     * @param conn la connessione al database da utilizzare.
     * @param libreriaId l'ID della libreria.
     * @param libroId l'ID del libro da verificare.
     * @return {@code true} se il libro è già presente nella libreria, {@code false} altrimenti.
     * @throws SQLException se si verifica un errore di accesso al database.
     */
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

    /**
     * Recupera la lista di tutti gli ID dei libri contenuti in una specifica libreria,
     * utilizzando una connessione esistente.
     * <p>
     * Questo metodo è progettato per essere eseguito all'interno di una transazione.
     *
     * @param conn la connessione al database da utilizzare.
     * @param libreriaId l'ID della libreria.
     * @return una {@link List} di {@link Long} rappresentanti gli ID dei libri.
     * @throws SQLException se si verifica un errore di accesso al database.
     */
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

    // -------------------------------------------------
    // Metodo helper per il mapping
    // -------------------------------------------------

    /**
     * Metodo helper per mappare una riga di un {@link ResultSet} a un oggetto {@link Libreria}.
     * <p>
     * Questo metodo costruisce un oggetto {@code Libreria} completo, recuperando non solo
     * i dati dalla tabella {@code Librerie}, ma anche la lista associata degli ID dei libri
     * dalla tabella {@code Libreria_Libro}. Per fare ciò, esegue una query aggiuntiva
     * utilizzando la stessa connessione passata come parametro.
     *
     * @param conn la connessione al database da utilizzare per recuperare gli ID dei libri.
     * @param rs il ResultSet posizionato sulla riga della libreria da mappare.
     * @return un nuovo oggetto {@link Libreria} popolato con i dati della riga corrente e la lista degli ID dei libri.
     * @throws SQLException se si verifica un errore durante la lettura dei dati dal ResultSet
     *                      o durante la query per recuperare gli ID dei libri.
     */
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