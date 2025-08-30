package bookrecommender.server;

import bookrecommender.condivisi.utenti.UtentiService;
import bookrecommender.condivisi.libri.CercaLibriService;
import bookrecommender.condivisi.librerie.LibrerieService;
import bookrecommender.server.utenti.UtentiServiceImpl;
import bookrecommender.server.libri.CercaLibriServiceImpl;
import bookrecommender.server.librerie.LibrerieServiceImpl;


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

public class ServerMain {
    private static final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
    private static final Logger logger = LogManager.getLogger(ServerMain.class);

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

    private static String inserimentoCredenzialiDB(String nomeCredenziale) throws IOException {
        System.out.print("Inserire " + nomeCredenziale + ": ");
        return reader.readLine();
    }

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
            
            logger.info("Servizio UtentiService registrato nel registro RMI");
            logger.info("Servizio CercaLibriService registrato nel registro RMI");
            logger.info("Servizio LibrerieService registrato nel registro RMI");
            System.out.println("Servizi RMI registrati: UtentiService, CercaLibriService, LibrerieService");
            
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
