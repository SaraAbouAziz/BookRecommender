package bookrecommender.server.utenti;

import bookrecommender.condivisi.utenti.Utenti;
import bookrecommender.condivisi.utenti.UtentiService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * Implementazione concreta del servizio RMI {@link UtentiService}.
 * <p>
 * Questa classe agisce come il "Service Layer" per la gestione degli utenti sul lato server.
 * Estende {@link UnicastRemoteObject} per essere esportabile e accessibile da client remoti tramite RMI.
 * <p>
 * Le sue responsabilità principali sono:
 * <ul>
 *     <li>Implementare la logica di business per le operazioni sugli utenti, come l'autenticazione e la registrazione.</li>
 *     <li>Validare i dati ricevuti dal client prima di interagire con lo strato di persistenza.</li>
 *     <li>Coordinare le operazioni di accesso ai dati delegando al DAO appropriato ({@link UtentiDAO}).</li>
 *     <li>Gestire le eccezioni e loggare le operazioni in modo appropriato.</li>
 * </ul>
 *
 * @see UtentiService
 * @see UtentiDAO
 * @see JdbcUtentiDAO
 */
public class UtentiServiceImpl extends UnicastRemoteObject implements UtentiService {
    
    private static final Logger logger = LogManager.getLogger(UtentiServiceImpl.class);
    private final UtentiDAO utentiDAO;
    
    /**
     * Costruisce e inizializza il servizio utenti.
     * <p>
     * Il costruttore invoca il costruttore della superclasse {@link UnicastRemoteObject} per
     * esportare l'oggetto e renderlo disponibile per le chiamate remote. Inizializza
     * inoltre l'implementazione del DAO ({@link JdbcUtentiDAO}) che verrà utilizzata
     * per interagire con il database.
     *
     * @throws RemoteException se si verifica un errore durante l'esportazione dell'oggetto RMI.
     */
    public UtentiServiceImpl() throws RemoteException {
        super();
        this.utentiDAO = new JdbcUtentiDAO();
        logger.info("UtentiServiceImpl inizializzato");
    }

    @Override
    /**
     * {@inheritDoc}
     * <p>
     * Questa implementazione recupera l'utente tramite il suo identificatore (username, email o CF)
     * utilizzando il {@link UtentiDAO}. Se l'utente viene trovato, confronta la password fornita
     * con quella memorizzata nel database.
     * <b>Attenzione:</b> Attualmente esegue un confronto in chiaro delle password.
     *
     * @throws RemoteException se si verifica un errore a livello di persistenza o di comunicazione.
     */
    public boolean authenticateUser(String username, String password) throws RemoteException {
        try {
            logger.debug("Tentativo di autenticazione per utente: " + username);
            
            if (username == null || password == null || username.trim().isEmpty() || password.isEmpty()) {
                logger.warn("Tentativo di autenticazione con credenziali vuote");
                return false;
            }
            
            Utenti utente = utentiDAO.findByUsername(username.trim());
            if (utente == null) {
                logger.warn("Utente non trovato: " + username);
                return false;
            }
            
            boolean authenticated = utente.password().equals(password);
            if (authenticated) {
                logger.info("Autenticazione riuscita per utente: " + username);
            } else {
                logger.warn("Password errata per utente: " + username);
            }
            
            return authenticated;
            
        } catch (Exception e) {
            logger.error("Errore durante l'autenticazione dell'utente: " + username, e);
            throw new RemoteException("Errore durante l'autenticazione", e);
        }
    }

    @Override
    /**
     * {@inheritDoc}
     * <p>
     * Questa implementazione prima valida i dati dell'utente tramite {@link #isValidUser(Utenti)}.
     * Successivamente, verifica che l'identificatore (userID, email, CF) non sia già in uso.
     * Se tutti i controlli passano, delega il salvataggio al {@link UtentiDAO}.
     *
     * @throws RemoteException se si verifica un errore a livello di persistenza o di comunicazione.
     */
    public boolean registerUser(Utenti utente) throws RemoteException {
        try {
            logger.debug("Tentativo di registrazione per utente: " + utente.userID());
            
            if (utente == null) {
                logger.warn("Tentativo di registrazione con utente null");
                return false;
            }
            
            // Validazione dati utente
            if (!isValidUser(utente)) {
                logger.warn("Dati utente non validi per la registrazione: " + utente.userID());
                return false;
            }
            
            // Verifica se l'username esiste già
            if (utentiDAO.findByUsername(utente.userID()) != null) {
                logger.warn("Username già esistente: " + utente.userID());
                return false;
            }
            
            // Salvataggio utente
            boolean saved = utentiDAO.save(utente);
            if (saved) {
                logger.info("Utente registrato con successo: " + utente.userID());
            } else {
                logger.error("Errore nel salvataggio dell'utente: " + utente.userID());
            }
            
            return saved;
            
        } catch (Exception e) {
            logger.error("Errore durante la registrazione dell'utente: " + utente.userID(), e);
            throw new RemoteException("Errore durante la registrazione", e);
        }
    }

    @Override
    /**
     * {@inheritDoc}
     * <p>
     * Questa implementazione delega la ricerca al metodo {@link UtentiDAO#findByUsername(String)}
     * e restituisce {@code true} se l'oggetto restituito non è nullo.
     * @throws RemoteException se si verifica un errore a livello di persistenza o di comunicazione.
     */
    public boolean isUsernameExists(String username) throws RemoteException {
        try {
            logger.debug("Verifica esistenza username: " + username);
            
            if (username == null || username.trim().isEmpty()) {
                return false;
            }
            
            Utenti utente = utentiDAO.findByUsername(username.trim());
            boolean exists = utente != null;
            
            logger.debug("Username " + username + " esiste: " + exists);
            return exists;
            
        } catch (Exception e) {
            logger.error("Errore durante la verifica dell'username: " + username, e);
            throw new RemoteException("Errore durante la verifica username", e);
        }
    }

    @Override
    /**
     * {@inheritDoc}
     * <p>
     * Questa implementazione delega semplicemente la chiamata al metodo {@link UtentiDAO#findByUsername(String)}.
     * @throws RemoteException se si verifica un errore a livello di persistenza o di comunicazione.
     */
    public Utenti getUserByUsername(String username) throws RemoteException {
        try {
            logger.debug("Richiesta utente per username: " + username);
            
            if (username == null || username.trim().isEmpty()) {
                return null;
            }
            
            Utenti utente = utentiDAO.findByUsername(username.trim());
            
            if (utente != null) {
                logger.debug("Utente trovato: " + username);
            } else {
                logger.debug("Utente non trovato: " + username);
            }
            
            return utente;
            
        } catch (Exception e) {
            logger.error("Errore durante il recupero dell'utente: " + username, e);
            throw new RemoteException("Errore durante il recupero utente", e);
        }
    }
    
    /**
     * Esegue una validazione di base sui dati di un oggetto {@link Utenti}.
     * <p>
     * Controlla che i campi principali non siano nulli o vuoti e che rispettino
     * alcuni vincoli di base (es. lunghezza minima per la password, lunghezza fissa
     * per il codice fiscale, presenza della '@' nell'email).
     *
     * @param utente L'oggetto {@link Utenti} da validare.
     * @return {@code true} se i dati dell'utente sono considerati validi, {@code false} altrimenti.
     */
    private boolean isValidUser(Utenti utente) {
        return utente.nome() != null && !utente.nome().trim().isEmpty() &&
               utente.cognome() != null && !utente.cognome().trim().isEmpty() &&
               utente.codFiscale() != null && utente.codFiscale().trim().length() == 16 &&
               utente.email() != null && utente.email().trim().contains("@") &&
               utente.userID() != null && !utente.userID().trim().isEmpty() &&
               utente.password() != null && utente.password().length() >= 6;
    }
}
