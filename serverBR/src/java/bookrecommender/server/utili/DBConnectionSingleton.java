package bookrecommender.server.utili;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Gestisce la connessione al database utilizzando il pattern Singleton.
 * <p>
 * Questa classe fornisce un modo centralizzato per configurare e ottenere connessioni al database.
 * Offre metodi per:
 * <ul>
 *     <li>Inizializzare i parametri di connessione (URL, utente, password).</li>
 *     <li>Ottenere una nuova connessione, ideale per contesti concorrenti.</li>
 *     <li>Gestire una connessione condivisa (sconsigliato in produzione).</li>
 *     <li>Chiudere la connessione e resettare la configurazione.</li>
 * </ul>
 * L'uso di {@code volatile} garantisce che le modifiche ai parametri di connessione siano
 * visibili a tutti i thread.
 *
 * @see java.sql.Connection
 * @see java.sql.DriverManager
 */
public final class DBConnectionSingleton {
    private static volatile Connection sharedConnection;
    private static volatile String jdbcUrl;
    private static volatile String username;
    private static volatile String password;

    /**
     * Costruttore privato per impedire l'istanziazione.
     */
    private DBConnectionSingleton() { }

    /**
     * Inizializza i parametri di connessione (URL, utente, password) e, opzionalmente,
     * crea una connessione condivisa.
     * <p>
     * Questo metodo deve essere chiamato almeno una volta prima di poter ottenere connessioni.
     * La creazione della connessione condivisa è thread-safe grazie al double-checked locking.
     *
     * @param jdbcUrl  l'URL JDBC del database (es. "jdbc:postgresql://localhost:5432/mydatabase").
     * @param user     il nome utente per l'accesso al database.
     * @param pwd      la password per l'accesso al database.
     * @throws SQLException se si verifica un errore durante la creazione della connessione condivisa.
     */
    public static void initialiseConnection(String jdbcUrl, String user, String pwd) throws SQLException {
        DBConnectionSingleton.jdbcUrl = jdbcUrl;
        DBConnectionSingleton.username = user;
        DBConnectionSingleton.password = pwd;

        if (sharedConnection == null) {
            synchronized (DBConnectionSingleton.class) {
                if (sharedConnection == null) {
                    closeSharedConnectionQuietly();
                    sharedConnection = DriverManager.getConnection(jdbcUrl, user, pwd);
                }
            }
        }
    }

    /**
     * Apre e restituisce una <strong>nuova</strong> istanza di {@link Connection}.
     * <p>
     * Questo è il metodo raccomandato per ottenere una connessione in un'applicazione
     * multithread, poiché garantisce che ogni thread di lavoro operi su una connessione
     * separata, evitando problemi di concorrenza.
     *
     * @return una nuova connessione al database.
     * @throws SQLException se i parametri di connessione non sono stati prima inizializzati
     *                      tramite {@link #initialiseConnection(String, String, String)},
     *                      o se si verifica un errore di accesso al database.
     */
    public static Connection openNewConnection() throws SQLException {
        if (jdbcUrl == null) {
            throw new SQLException("Database non inizializzato (jdbcUrl == null). Chiamare initialiseConnection(...) prima.");
        }
        return DriverManager.getConnection(jdbcUrl, username, password);
    }

    /**
     * Chiude la connessione condivisa ({@code sharedConnection}), se è stata creata.
     * <p>
     * L'operazione è thread-safe e ignora eventuali {@code SQLException} che potrebbero
     * verificarsi durante la chiusura.
     */
    public static void closeSharedConnectionQuietly() {
        if (sharedConnection != null) {
            synchronized (DBConnectionSingleton.class) {
                if (sharedConnection != null) {
                    try {
                        sharedConnection.close();
                    } catch (SQLException ignored) {
                        // Ignora l'eccezione per garantire che il riferimento venga pulito
                    }
                    sharedConnection = null;
                }
            }
        }
    }

    /**
     * Esegue lo shutdown completo del gestore di connessioni.
     * <p>
     * Chiude la connessione condivisa e azzera tutti i parametri di connessione (URL, utente, password).
     * Questo metodo è utile per rilasciare le risorse in modo pulito alla terminazione dell'applicazione.
     */
    public static synchronized void shutdown() {
        closeSharedConnectionQuietly();
        jdbcUrl = null;
        username = null;
        password = null;
    }
}
