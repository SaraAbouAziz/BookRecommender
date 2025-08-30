package bookrecommender.server.utenti;

import bookrecommender.condivisi.utenti.Utenti;
import bookrecommender.server.utili.DBConnectionSingleton;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementazione JDBC dell'interfaccia {@link UtentiDAO}.
 * <p>
 * Questa classe gestisce la persistenza degli oggetti {@link Utenti} su un
 * database relazionale tramite JDBC. È responsabile di tutte le operazioni
 * CRUD (Create, Read, Update, Delete) per l'entità utente.
 * <p>
 * Offre due tipi di API:
 * <ul>
 *     <li><b>API "convenience"</b>: implementano direttamente i metodi dell'interfaccia
 *         {@code UtentiDAO}. Gestiscono autonomamente l'apertura e la chiusura della
 *         connessione al database per ogni singola operazione. Sono semplici da usare
 *         ma non adatte a transazioni che coinvolgono più operazioni DAO.</li>
 *     <li><b>API "transaction-aware"</b>: sono metodi pubblici aggiuntivi che accettano
 *         un'istanza di {@link Connection} come parametro. Questo permette al chiamante
 *         (tipicamente un Service Layer) di controllare il ciclo di vita della transazione
 *         (commit/rollback) quando più operazioni devono essere eseguite in modo atomico.</li>
 * </ul>
 * @author Abou Aziz Sara Hesham Abdel Hamid 757004
 * @author Ben Mahjoub Ali 759560
 * @author Hidri Mohamed Taha 756235
 * @author Zoghbani Lilia 759652
 * @see UtentiDAO
 * @see DBConnectionSingleton
 * @version 1.0
 */
public class JdbcUtentiDAO implements UtentiDAO {

    private static final Logger logger = LogManager.getLogger(JdbcUtentiDAO.class);

    // == Costanti per le query SQL ==
    private static final String QUERY_SAVE =
        "INSERT INTO UtentiRegistrati (user_id, password, nome, cognome, codice_fiscale, email) " +
        "VALUES (?, ?, ?, ?, ?, ?)";
    private static final String QUERY_FIND_BY_USERNAME =
        "SELECT user_id, password, nome, cognome, codice_fiscale, email FROM UtentiRegistrati WHERE user_id = ? OR email = ? OR codice_fiscale = ?";
    private static final String QUERY_UPDATE =
        "UPDATE UtentiRegistrati SET password = ?, nome = ?, cognome = ?, codice_fiscale = ?, email = ? WHERE user_id = ?";
    private static final String QUERY_DELETE =
        "DELETE FROM UtentiRegistrati WHERE user_id = ?";
    private static final String QUERY_FIND_ALL =
        "SELECT user_id, password, nome, cognome, codice_fiscale, email FROM UtentiRegistrati";

    // -------------------------------------------------
    // API "comode" (implementazione di UtentiDAO)
    // -------------------------------------------------

    /**
     * {@inheritDoc}
     * <p>
     * Questa implementazione gestisce autonomamente la connessione al database.
     * Apre una nuova connessione tramite {@link DBConnectionSingleton}, esegue l'operazione
     * delegando al metodo {@link #save(Connection, Utenti)}, e chiude la connessione
     * automaticamente grazie al costrutto {@code try-with-resources}.
     * <p>
     * In caso di {@link SQLException} o altre eccezioni, l'errore viene loggato
     * e il metodo restituisce {@code false}, indicando il fallimento dell'operazione.
     */
    @Override
    public boolean save(Utenti utente) {
        try (Connection conn = DBConnectionSingleton.openNewConnection()) {
            return save(conn, utente);
        } catch (SQLException e) {
            logger.error("Errore SQL durante il salvataggio dell'utente: {}", utente.userID(), e);
            return false;
        } catch (Exception e) {
            logger.error("Errore imprevisto durante il salvataggio dell'utente: " + utente.userID(), e);
            return false;
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Questa implementazione gestisce autonomamente la connessione al database.
     * Apre una nuova connessione, esegue la ricerca delegando al metodo
     * {@link #findByUsername(Connection, String)}, e chiude la connessione.
     * <p>
     * In caso di {@link SQLException} o altre eccezioni, l'errore viene loggato
     * e il metodo restituisce {@code null}, indicando che l'utente non è stato trovato
     * o che si è verificato un problema.
     */
    @Override
    public Utenti findByUsername(String username) {
        try (Connection conn = DBConnectionSingleton.openNewConnection()) {
            return findByUsername(conn, username);
        } catch (SQLException e) {
            logger.error("Errore SQL durante la ricerca dell'utente: {}", username, e);
            return null;
        } catch (Exception e) {
            logger.error("Errore imprevisto durante la ricerca dell'utente: " + username, e);
            return null;
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Questa implementazione gestisce autonomamente la connessione al database.
     * Apre una nuova connessione, esegue l'aggiornamento delegando al metodo
     * {@link #update(Connection, Utenti)}, e chiude la connessione.
     * <p>
     * In caso di {@link SQLException} o altre eccezioni, l'errore viene loggato
     * e il metodo restituisce {@code false}, indicando il fallimento dell'operazione.
     */
    @Override
    public boolean update(Utenti utente) {
        try (Connection conn = DBConnectionSingleton.openNewConnection()) {
            return update(conn, utente);
        } catch (SQLException e) {
            logger.error("Errore SQL durante l'aggiornamento dell'utente: {}", utente.userID(), e);
            return false;
        } catch (Exception e) {
            logger.error("Errore imprevisto durante l'aggiornamento dell'utente: " + utente.userID(), e);
            return false;
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Questa implementazione gestisce autonomamente la connessione al database.
     * Apre una nuova connessione, esegue l'eliminazione delegando al metodo
     * {@link #delete(Connection, String)}, e chiude la connessione.
     * <p>
     * In caso di {@link SQLException} o altre eccezioni, l'errore viene loggato
     * e il metodo restituisce {@code false}, indicando il fallimento dell'operazione.
     */
    @Override
    public boolean delete(String username) {
        try (Connection conn = DBConnectionSingleton.openNewConnection()) {
            return delete(conn, username);
        } catch (SQLException e) {
            logger.error("Errore SQL durante l'eliminazione dell'utente: {}", username, e);
            return false;
        } catch (Exception e) {
            logger.error("Errore imprevisto durante l'eliminazione dell'utente: " + username, e);
            return false;
        }
    }

    /**
     * Recupera tutti gli utenti registrati dal database.
     * <p>
     * Questo metodo gestisce autonomamente la connessione al database.
     * Apre una nuova connessione, esegue la query delegando al metodo {@link #findAll(Connection)}, e la chiude.
     * In caso di errore, logga e restituisce una lista vuota.
     *
     * @return una {@link List} di {@link Utenti}; la lista è vuota se non ci sono utenti o in caso di errore.
     */
    public List<Utenti> findAll() {
        try (Connection conn = DBConnectionSingleton.openNewConnection()) {
            return findAll(conn);
        } catch (SQLException e) {
            logger.error("Errore SQL durante il recupero di tutti gli utenti", e);
            return new ArrayList<>();
        } catch (Exception e) {
            logger.error("Errore imprevisto durante il recupero di tutti gli utenti", e);
            return new ArrayList<>();
        }
    }

    

    /**
     * Salva un nuovo utente nel database utilizzando una connessione esistente.
     * <p>
     * Questo metodo è progettato per essere eseguito all'interno di una transazione
     * più ampia gestita da un livello di servizio superiore. Non gestisce
     * commit o rollback, lasciando tale responsabilità al chiamante.
     *
     * @param conn   la connessione al database da utilizzare, che deve essere valida e aperta.
     * @param utente l'oggetto {@link Utenti} da salvare, contenente tutti i dati necessari.
     * @return {@code true} se l'inserimento ha avuto successo (almeno una riga modificata), {@code false} altrimenti.
     * @throws SQLException se si verifica un errore di accesso al database.
     */
    public boolean save(Connection conn, Utenti utente) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(QUERY_SAVE)) {
            stmt.setString(1, utente.userID());
            stmt.setString(2, utente.password());
            stmt.setString(3, utente.nome());
            stmt.setString(4, utente.cognome());
            stmt.setString(5, utente.codFiscale());
            stmt.setString(6, utente.email());

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        }
    }

    /**
     * Trova un utente tramite username, email o codice fiscale utilizzando una connessione esistente.
     * <p>
     * Questo metodo è progettato per essere eseguito all'interno di una transazione
     * gestita dal chiamante.
     *
     * @param conn     la connessione al database da utilizzare, che deve essere valida e aperta.
     * @param username lo username, l'email o il codice fiscale da cercare.
     * @return un oggetto {@link Utenti} se un utente corrispondente viene trovato, altrimenti {@code null}.
     * @throws SQLException se si verifica un errore di accesso al database durante l'esecuzione della query.
     */
    public Utenti findByUsername(Connection conn, String username) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(QUERY_FIND_BY_USERNAME)) {
            stmt.setString(1, username);
            stmt.setString(2, username);
            stmt.setString(3, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Utenti(
                        rs.getString("nome"),
                        rs.getString("cognome"),
                        rs.getString("codice_fiscale"),
                        rs.getString("email"),
                        rs.getString("user_id"),
                        rs.getString("password")
                    );
                } else {
                    return null;
                }
            }
        }
    }

    /**
     * Aggiorna i dati di un utente esistente utilizzando una connessione esistente.
     * <p>
     * Questo metodo è progettato per essere eseguito all'interno di una transazione
     * gestita dal chiamante. L'utente da aggiornare viene identificato tramite il suo {@code userID}.
     *
     * @param conn   la connessione al database da utilizzare, che deve essere valida e aperta.
     * @param utente l'oggetto {@link Utenti} con i dati aggiornati.
     * @return {@code true} se l'aggiornamento ha avuto successo (almeno una riga modificata), {@code false} altrimenti.
     * @throws SQLException se si verifica un errore di accesso al database durante l'esecuzione della query.
     */
    public boolean update(Connection conn, Utenti utente) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(QUERY_UPDATE)) {
            stmt.setString(1, utente.password());
            stmt.setString(2, utente.nome());
            stmt.setString(3, utente.cognome());
            stmt.setString(4, utente.codFiscale());
            stmt.setString(5, utente.email());
            stmt.setString(6, utente.userID());

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        }
    }

    /**
     * Elimina un utente dal database utilizzando una connessione esistente.
     * <p>
     * Questo metodo è progettato per essere eseguito all'interno di una transazione
     * gestita dal chiamante.
     *
     * @param conn     la connessione al database da utilizzare, che deve essere valida e aperta.
     * @param username lo username dell'utente da eliminare.
     * @return {@code true} se l'eliminazione ha avuto successo (almeno una riga modificata), {@code false} altrimenti.
     * @throws SQLException se si verifica un errore di accesso al database durante l'esecuzione della query.
     */
    public boolean delete(Connection conn, String username) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(QUERY_DELETE)) {
            stmt.setString(1, username);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        }
    }

    /**
     * Recupera tutti gli utenti registrati utilizzando una connessione esistente.
     * <p>
     * Questo metodo è progettato per essere eseguito all'interno di una transazione
     * gestita dal chiamante.
     *
     * @param conn la connessione al database da utilizzare, che deve essere valida e aperta.
     * @return una {@link List} di {@link Utenti}; la lista è vuota se non ci sono utenti.
     * @throws SQLException se si verifica un errore di accesso al database durante l'esecuzione della query.
     */
    public List<Utenti> findAll(Connection conn) throws SQLException {
        List<Utenti> utenti = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(QUERY_FIND_ALL);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Utenti utente = new Utenti(
                    rs.getString("nome"),
                    rs.getString("cognome"),
                    rs.getString("codice_fiscale"),
                    rs.getString("email"),
                    rs.getString("user_id"),
                    rs.getString("password")
                );
                utenti.add(utente);
            }
        }
        logger.debug("Trovati {} utenti nel database", utenti.size());
        return utenti;
    }
}
