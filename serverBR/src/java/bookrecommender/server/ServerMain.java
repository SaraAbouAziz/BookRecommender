package bookrecommender.server;

import bookrecommender.condivisi.utenti.UtentiService;
import bookrecommender.condivisi.valutazioni.ValutazioneService;
import bookrecommender.condivisi.libri.CercaLibriService;
import bookrecommender.condivisi.librerie.LibrerieService;
import bookrecommender.condivisi.consigli.ConsigliService;
import bookrecommender.server.utenti.UtentiServiceImpl;
import bookrecommender.server.valutazioni.ValutazioneServiceImpl;
import bookrecommender.server.libri.CercaLibriServiceImpl;
import bookrecommender.server.librerie.LibrerieServiceImpl;
import bookrecommender.server.consigli.ConsigliServiceImpl;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Classe principale del server BookRecommender.
 * <p>
 * Questa classe rappresenta il punto di ingresso (entry point) per l'applicazione server.
 * Le sue responsabilità principali sono:
 * <ul>
 *     <li>Avviare il server e gestire il suo ciclo di vita.</li>
 *     <li>Gestire la configurazione iniziale, inclusa la connessione al database PostgreSQL,
 *         richiedendo le credenziali all'utente in modo interattivo.</li>
 *     <li>Creare e configurare il registro RMI (Remote Method Invocation).</li>
 *     <li>Istanziare e registrare (bind/rebind) tutte le implementazioni dei servizi remoti
 *         ({@link UtentiService}, {@link CercaLibriService}, etc.) nel registro RMI,
 *         rendendole accessibili ai client remoti.</li>
 *     <li>Mantenere il server attivo per accettare le chiamate dai client.</li>
 * </ul>
 * L'applicazione rimane in esecuzione fino a quando non viene terminata manualmente.
 *
 * @see bookrecommender.server.utili.DBConnectionSingleton
 * @see java.rmi.registry.Registry
 */
public class ServerMain {
    /**
     * Reader per l'input da console, utilizzato per acquisire le credenziali del database.
     */
    private static final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
    /**
     * Logger per la registrazione degli eventi e degli errori del server.
     */
    private static final Logger logger = LogManager.getLogger(ServerMain.class);

    /**
     * Metodo principale (entry point) che avvia il server.
     * <p>
     * Esegue la sequenza di avvio:
     * <ol>
     *     <li>Inizializza la connessione al database tramite {@link #createDBConnection()}.</li>
     *     <li>Crea il registro RMI e vi registra i servizi tramite {@link #createRMIRegistry()}.</li>
     * </ol>
     * Una volta avviato, il server rimane in attesa di richieste fino alla sua
     * terminazione manuale (es. tramite Ctrl+C). In caso di errore critico
     * durante l'avvio, il programma termina con un codice di errore.
     * </p>
     *
     * @param args argomenti passati dalla riga di comando (attualmente non utilizzati).
     */
    public static void main(String[] args) {
        logger.info("Avvio del server BookRecommender...");
        
        try {
            createDBConnection();
            createRMIRegistry();
            logger.info("Server avviato con successo. In attesa di richieste...");
            
            // Mantieni il server attivo
            System.out.println("Server avviato. In attesa di richieste...");
            System.out.println("Premi Ctrl+C per terminare il server.");
            
        } catch (Exception e) {
            logger.error("Errore critico durante l\'avvio del server", e);
            System.err.println("Errore critico durante l\'avvio del server: " + e.getMessage());
            System.exit(1);
        }
    }

    /**
     * Gestisce la creazione della connessione al database PostgreSQL.
     * <p>
     * Richiede interattivamente all'utente le credenziali (URL, username, password)
     * tramite la console. Tenta di stabilire la connessione utilizzando il singleton
     * {@link bookrecommender.server.utili.DBConnectionSingleton}.
     * In caso di fallimento della connessione (es. credenziali errate),
     * il metodo entra in un ciclo che permette all'utente di ritentare l'inserimento
     * delle credenziali fino al successo della connessione.
     * Una volta stabilita la connessione, chiude il {@code BufferedReader} usato per l'input.
     * </p>
     */
    private static void createDBConnection() {
        boolean connessioneFallita = true;

        while (connessioneFallita) {
            try {
                System.out.println("Configurazione connessione database PostgreSQL...");
                String url = inserimentoCredenzialiDB("URL del database (es. jdbc:postgresql://localhost:5432/dbBR)");
                String user = inserimentoCredenzialiDB("Username PostgreSQL");
                String password = inserimentoCredenzialiDB("Password PostgreSQL");
                
                // Utilizza la classe di connessione esistente
                bookrecommender.server.utili.DBConnectionSingleton.initialiseConnection(url, user, password);
                try (Connection conn = bookrecommender.server.utili.DBConnectionSingleton.openNewConnection()) {
                    logger.info("Connesso al database: " + conn.getCatalog());
                    System.out.println("Connesso al database " + conn.getCatalog());
                }
                connessioneFallita = false;
                
            } catch (IOException e) {
                logger.error("Errore di I/O durante l\'inserimento delle credenziali del database.", e);
                return;
            } catch (SQLException e) {
                logger.error("Errore SQL durante la connessione al database", e);
                System.out.println("\nCreazione della connessione al database fallita. Verificare le credenziali inserite.\n");
                bookrecommender.server.utili.DBUtil.printSQLException(e);
            } catch (Exception e) {
                logger.error("Errore imprevisto durante la connessione al database", e);
                System.out.println("\nErrore imprevisto durante la connessione al database.\n");
            }
        }

        try {
            reader.close();
        } catch (IOException e) {
            logger.error("Errore di I/O durante la chiusura del buffered reader.");
        }
    }

    /**
     * Legge una stringa di input dalla console per l'inserimento delle credenziali del database.
     * <p>
     * Metodo di utilità che mostra un prompt all'utente e legge la riga di testo inserita.
     * </p>
     *
     * @param nomeCredenziale il messaggio da mostrare all'utente per indicare quale credenziale inserire
     *                        (es. "Username PostgreSQL").
     * @return la stringa inserita dall'utente.
     * @throws IOException se si verifica un errore di I/O durante la lettura dalla console.
     */
    private static String inserimentoCredenzialiDB(String nomeCredenziale) throws IOException {
        System.out.print("Inserire " + nomeCredenziale + ": ");
        return reader.readLine();
    }

    /**
     * Crea o si collega a un registro RMI esistente e vi registra i servizi dell'applicazione.
     * <p>
     * Tenta di creare un nuovo registro RMI sulla porta 1099. Se la porta è già in uso,
     * si collega al registro esistente per permettere il riavvio del server senza dover
     * terminare il registro manualmente.
     * Successivamente, istanzia e registra i seguenti servizi, rendendoli disponibili ai client:
     * <ul>
     *     <li>{@link UtentiService} con il nome "UtentiService"</li>
     *     <li>{@link CercaLibriService} con il nome "CercaLibriService"</li>
     *     <li>{@link LibrerieService} con il nome "LibrerieService"</li>
     *     <li>{@link ValutazioneService} con il nome "ValutazioneService"</li>
     *     <li>{@link ConsigliService} con il nome definito in {@link ConsigliService#NAME}</li>
     * </ul>
     * </p>
     * @throws RuntimeException se non è possibile creare o registrare i servizi RMI a causa di
     *         un errore di comunicazione remota o un altro problema imprevisto.
     * @see UtentiServiceImpl
     * @see CercaLibriServiceImpl
     * @see LibrerieServiceImpl
     * @see ValutazioneServiceImpl
     * @see ConsigliServiceImpl
     */
    private static void createRMIRegistry() {
        try {
            // Crea (o riusa) il registro RMI sulla porta 1099
            Registry reg;
            try {
                reg = LocateRegistry.createRegistry(1099);
                logger.info("Registro RMI creato sulla porta 1099");
            } catch (java.rmi.server.ExportException e) {
                logger.warn("Registro RMI gia' attivo sulla porta 1099, uso quello esistente");
                reg = LocateRegistry.getRegistry(1099);
                // Verifica che risponda
                reg.list();
            }
            
            // Crea e registra il servizio UtentiService
            UtentiService utentiService = new UtentiServiceImpl();
            reg.rebind("UtentiService", utentiService);
            
            // Crea e registra il servizio CercaLibriService
            CercaLibriService cercaLibriService = new CercaLibriServiceImpl();
            reg.rebind("CercaLibriService", cercaLibriService);

            // Crea e registra il servizio LibrerieService
            LibrerieService librerieService = new LibrerieServiceImpl();
            reg.rebind("LibrerieService", librerieService);

             // Crea e registra il servizio ValutazioneService
            ValutazioneService valutazioneService = new ValutazioneServiceImpl();
            reg.rebind("ValutazioneService", valutazioneService);

            // Crea e registra il servizio ConsigliService
            ConsigliService consigliService = new ConsigliServiceImpl();
            reg.rebind(ConsigliService.NAME, consigliService);
            
            logger.info("Servizio UtentiService registrato nel registro RMI");
            logger.info("Servizio CercaLibriService registrato nel registro RMI");
            logger.info("Servizio LibrerieService registrato nel registro RMI");
            logger.info("Servizio ValutazioneService registrato nel registro RMI");
            logger.info("Servizio ConsigliService registrato nel registro RMI");


            System.out.println("Servizi RMI registrati: UtentiService, CercaLibriService, LibrerieService, ValutazioneService, ConsigliService");
            
        } catch (RemoteException e) {
            logger.error("Errore durante la creazione del registro RMI.", e);
            System.err.println("Errore durante la creazione del registro RMI: " + e.getMessage());
            throw new RuntimeException("Impossibile creare il registro RMI", e);
        } catch (Exception e) {
            logger.error("Errore imprevisto durante la creazione del registro RMI.", e);
            System.err.println("Errore imprevisto durante la creazione del registro RMI: " + e.getMessage());
            throw new RuntimeException("Errore imprevisto durante la creazione del registro RMI", e);
        }
    }
}
