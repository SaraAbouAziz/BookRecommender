package bookrecommender.server.utenti;

import bookrecommender.condivisi.utenti.Utenti;
import bookrecommender.condivisi.utenti.UtentiService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class UtentiServiceImpl extends UnicastRemoteObject implements UtentiService {
    
    private static final Logger logger = LogManager.getLogger(UtentiServiceImpl.class);
    private final UtentiDAO utentiDAO;
    
    public UtentiServiceImpl() throws RemoteException {
        super();
        this.utentiDAO = new JdbcUtentiDAO();
        logger.info("UtentiServiceImpl inizializzato");
    }

    @Override
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
     * Valida i dati di un utente
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
