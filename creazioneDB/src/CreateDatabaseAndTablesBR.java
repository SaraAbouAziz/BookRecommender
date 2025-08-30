
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * Crea il database "dbBR" e le tabelle relative.
 *
 * Uso:
 *   java -jar DBCreatorBR-1.0-jar-with-dependencies.jar <db_user> <db_password>
 *
 * Nota: il comando di DROP/CREATE database richiede privilegi adeguati (tipicamente l'utente postgres
 * o un utente con permessi CREATE DATABASE).
 */
public class CreateDatabaseAndTablesBR {
    private static Connection conn;

    // Connessione al DB di amministrazione (usata per creare/drop database)
    private static final String ADMIN_URL = "jdbc:postgresql://localhost:5432/postgres";
    private static final String DB_URL_TEMPLATE = "jdbc:postgresql://localhost:5432/%s"; // %s -> db name
    private static String user;
    private static String password;

    private static final String DB_NAME = "dbBR";
    private static final String DB_URL = String.format(DB_URL_TEMPLATE, DB_NAME);

    // SQL per creare le tabelle richieste (aggiornato per utilizzare user_id come chiave primaria)
    private static final String createUtentiRegistrati = """
            CREATE TABLE UtentiRegistrati (
              user_id       VARCHAR(50) PRIMARY KEY,
              password      TEXT NOT NULL,
              nome          VARCHAR(256) NOT NULL,
              cognome       VARCHAR(256) NOT NULL,
              codice_fiscale CHAR(16) UNIQUE,
              email         VARCHAR(256) UNIQUE NOT NULL
            );
            """;

    private static final String createLibri = """
            CREATE TABLE Libri (
              id            BIGINT PRIMARY KEY,
              titolo        VARCHAR(500) NOT NULL,
              autori        VARCHAR(500),
              anno          VARCHAR(256),
              descrizione   TEXT,
              categorie     VARCHAR(500),
              editore       VARCHAR(500),
              prezzo        VARCHAR(256)
            );
            """;

    private static final String createLibrerie = """
            CREATE TABLE Librerie (
              libreria_id   SERIAL PRIMARY KEY,
              user_id       VARCHAR(50) NOT NULL REFERENCES UtentiRegistrati(user_id) ON DELETE CASCADE,
              nome_libreria VARCHAR(256) NOT NULL,
              data_creazione TIMESTAMP DEFAULT now(),
              UNIQUE (user_id, nome_libreria)
            );
            """;

    private static final String createLibreriaLibro = """
            CREATE TABLE Libreria_Libro (
              libreria_id   INT NOT NULL,
              libro_id      BIGINT NOT NULL,
              data_inserimento TIMESTAMP DEFAULT now(),
              PRIMARY KEY (libreria_id, libro_id),
              FOREIGN KEY (libreria_id) REFERENCES Librerie(libreria_id) ON DELETE CASCADE,
              FOREIGN KEY (libro_id)     REFERENCES Libri(id)              ON DELETE RESTRICT
            );
            """;

    private static final String createValutazioniLibri = """
            CREATE TABLE ValutazioniLibri (
              user_id        VARCHAR(50) NOT NULL,
              libreria_id    INT NOT NULL,
              libro_id       BIGINT NOT NULL,
            
              stile_score      SMALLINT NOT NULL CHECK (stile_score BETWEEN 1 AND 5),
              contenuto_score  SMALLINT NOT NULL CHECK (contenuto_score BETWEEN 1 AND 5),
              gradimento_score SMALLINT NOT NULL CHECK (gradimento_score BETWEEN 1 AND 5),
              originalita_score SMALLINT NOT NULL CHECK (originalita_score BETWEEN 1 AND 5),
              qualita_score    SMALLINT NOT NULL CHECK (qualita_score BETWEEN 1 AND 5),
              voto_complessivo SMALLINT NOT NULL CHECK (voto_complessivo BETWEEN 1 AND 5),
            
              stile_note       VARCHAR(256),
              contenuto_note   VARCHAR(256),
              gradimento_note  VARCHAR(256),
              originalita_note VARCHAR(256),
              qualita_note     VARCHAR(256),
            
              data_valutazione TIMESTAMP DEFAULT now(),
            
              PRIMARY KEY (user_id, libreria_id, libro_id),
            
              FOREIGN KEY (user_id) REFERENCES UtentiRegistrati(user_id) ON DELETE CASCADE,
              FOREIGN KEY (libreria_id, libro_id) REFERENCES Libreria_Libro(libreria_id, libro_id)
            );
            """;

    private static final String createConsigliLibri = """
            CREATE TABLE ConsigliLibri (
              user_id             VARCHAR(50) NOT NULL,
              libreria_id         INT NOT NULL,
              libro_letto_id      BIGINT NOT NULL,
              libro_consigliato_id BIGINT NOT NULL,
              commento            VARCHAR(256),
              data_consiglio      TIMESTAMP DEFAULT now(),
            
              PRIMARY KEY (user_id, libreria_id, libro_letto_id, libro_consigliato_id),
            
              FOREIGN KEY (user_id) REFERENCES UtentiRegistrati(user_id) ON DELETE CASCADE,
              FOREIGN KEY (libreria_id, libro_letto_id) REFERENCES Libreria_Libro(libreria_id, libro_id),
              FOREIGN KEY (libro_consigliato_id) REFERENCES Libri(id)
            );
            """;

   
    public static void main(String[] args) {
        String LIBRI_FILE = "Libri.dati.csv";
      
        if (args.length != 2) {
            System.out.println("Utilizzo: java -jar DBCreatorBR-1.0-jar-with-dependencies.jar <user> <password>");
            System.exit(1);
        }
        user = args[0];
        password = args[1];

        // Verifica preliminare della struttura del CSV
        System.out.println("Verifica struttura del file CSV...");
        

        createDatabase();
        createTables();
        populateLibriFromCSV(LIBRI_FILE);
    }

    /**
     * Crea (o ricrea) il database dbBR.
     */
    public static void createDatabase() {
        try {
            // Connessione al DB "postgres" per operazioni di amministrazione (DROP/CREATE DATABASE)
            conn = DBConnectionSingleton.initialiseConnectionAndGet(ADMIN_URL, user, password);
        } catch (SQLException e) {
            System.out.println("Errore nella connessione amministrativa al server PostgreSQL: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        String dropDbSql = "DROP DATABASE IF EXISTS \"" + DB_NAME + "\"";
        String createDbSql = "CREATE DATABASE \"" + DB_NAME + "\"";

        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(dropDbSql);
            System.out.println("Database '" + DB_NAME + "' eliminato, se esistente.");

            stmt.executeUpdate(createDbSql);
            System.out.println("Database '" + DB_NAME + "' creato con successo.");

        } catch (SQLException e) {
            System.out.println("Errore nella creazione del database: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBConnectionSingleton.closeConnectionQuietly();
        }
    }

    /**
     * Crea le tabelle all'interno di dbBR (ricrea se esistono).
     */
    public static void createTables() {
        try {
            conn = DBConnectionSingleton.initialiseConnectionAndGet(DB_URL, user, password);
        } catch (SQLException e) {
            System.out.println("Errore nella connessione al database specifico: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        System.out.println("Connessione riuscita al database " + DB_NAME + "!");

        // Drop in ordine generico (CASCADE si occupa delle dipendenze, ma è buona pratica eseguire comunque)
        String dropTables = """
                DROP TABLE IF EXISTS ConsigliLibri CASCADE;
                DROP TABLE IF EXISTS ValutazioniLibri CASCADE;
                DROP TABLE IF EXISTS Libreria_Libro CASCADE;
                DROP TABLE IF EXISTS Librerie CASCADE;
                DROP TABLE IF EXISTS Libri CASCADE;
                DROP TABLE IF EXISTS UtentiRegistrati CASCADE;
                """;

        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(dropTables);
            System.out.println("Tabelle eliminate, se esistenti.");

            // Crea tabelle nell'ordine corretto per le FK
            stmt.executeUpdate(createUtentiRegistrati);
            stmt.executeUpdate(createLibri);
            stmt.executeUpdate(createLibrerie);
            stmt.executeUpdate(createLibreriaLibro);
            stmt.executeUpdate(createValutazioniLibri);
            stmt.executeUpdate(createConsigliLibri);

            System.out.println("Tabelle create con successo nel database " + DB_NAME + "!");
        } catch (SQLException e) {
            System.out.println("Errore nella creazione delle tabelle: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBConnectionSingleton.closeConnectionQuietly();
        }
    }

    /**
     * Semplice singleton per gestire la connessione JDBC.
     * Lo mettiamo nello stesso file per comodità; se hai già una classe simile usa quella.
     */
    static class DBConnectionSingleton {
        private static Connection connection;

        /**
         * Inizializza e restituisce una connessione (non mantiene la connessione se già chiusa).
         */
        public static Connection initialiseConnectionAndGet(String jdbcUrl, String user, String password) throws SQLException {
            closeConnectionQuietly();
            connection = DriverManager.getConnection(jdbcUrl, user, password);
            return connection;
        }

        public static Connection getConnection() {
            return connection;
        }

        public static void closeConnectionQuietly() {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException ignored) { }
                connection = null;
            }
        }

    }

    public static void populateLibriFromCSV(String csvPath) {
        try {
            conn = DBConnectionSingleton.initialiseConnectionAndGet(DB_URL, user, password);
        } catch (SQLException e) {
            System.out.println("Errore nella connessione al database specifico: " + e.getMessage());
            e.printStackTrace();
            return;
        }
    
        // Utilizzo la nuova classe LeggiFileCSV per popolare direttamente il database
        int libriInseriti = LeggiFileCSV.popolaDatabaseDaCSV(csvPath, conn);
        
        if (libriInseriti > 0) {
            System.out.println("Tabella Libri popolata correttamente da CSV. Inseriti " + libriInseriti + " libri.");
        } else {
            System.out.println("Nessun libro è stato inserito nel database.");
        }
    }

}
