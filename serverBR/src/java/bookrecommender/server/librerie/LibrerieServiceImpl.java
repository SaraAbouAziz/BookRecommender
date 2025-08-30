package bookrecommender.server.librerie;

import bookrecommender.condivisi.librerie.LibrerieService;
import bookrecommender.condivisi.librerie.Libreria;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementazione del servizio librerie.
 * Gestisce tutte le operazioni sulle librerie personali degli utenti.
 */
public class LibrerieServiceImpl extends UnicastRemoteObject implements LibrerieService {
    
    private static final Logger logger = LogManager.getLogger(LibrerieServiceImpl.class);
    private final LibrerieDAO librerieDAO;
    
    public LibrerieServiceImpl() throws RemoteException {
        super();
        this.librerieDAO = new JdbcLibrerieDAO();
    }
    
    @Override
    public boolean creaLibreria(String userId, String nomeLibreria) throws RemoteException {
        try {
            // Validazioni
            if (userId == null || userId.trim().isEmpty()) {
                logger.warn("Tentativo di creare libreria con userId null o vuoto");
                return false;
            }
            
            if (nomeLibreria == null || nomeLibreria.trim().isEmpty()) {
                logger.warn("Tentativo di creare libreria con nome null o vuoto per utente: {}", userId);
                return false;
            }
            
            // Verifica se il nome è già esistente per questo utente
            if (librerieDAO.isNomeLibreriaEsistente(userId, nomeLibreria)) {
                logger.warn("Tentativo di creare libreria con nome già esistente: {} per utente: {}", nomeLibreria, userId);
                return false;
            }
            
            // Crea la libreria
            Libreria libreria = new Libreria(null, userId, nomeLibreria.trim(), LocalDateTime.now(), new ArrayList<>());
            Libreria libreriaCreata = librerieDAO.creaLibreria(libreria);
            
            if (libreriaCreata != null) {
                logger.info("Libreria '{}' creata con successo per l'utente: {}", nomeLibreria, userId);
            } else {
                logger.error("Fallimento nella creazione della libreria '{}' per l'utente: {}", nomeLibreria, userId);
            }
            
            return libreriaCreata != null;
            
        } catch (Exception e) {
            logger.error("Errore durante la creazione della libreria per l'utente: {}", userId, e);
            return false;
        }
    }
    
    @Override
    public boolean aggiungiLibroALibreria(String userId, String nomeLibreria, long libroId) throws RemoteException {
        try {
            // Validazioni
            if (userId == null || nomeLibreria == null) {
                logger.warn("Parametri null per aggiunta libro alla libreria");
                return false;
            }
            
            // Trova la libreria
            Libreria libreria = librerieDAO.getLibreriaByUserIdAndNome(userId, nomeLibreria);
            if (libreria == null) {
                logger.warn("Libreria '{}' non trovata per l'utente: {}", nomeLibreria, userId);
                return false;
            }
            
            // Verifica se il libro è già presente
            if (librerieDAO.isLibroInLibreria(libreria.libreriaId(), libroId)) {
                logger.info("Libro {} già presente nella libreria '{}' dell'utente: {}", libroId, nomeLibreria, userId);
                return true; // Non è un errore, il libro è già presente
            }
            
            // Aggiungi il libro
            boolean success = librerieDAO.aggiungiLibroALibreria(libreria.libreriaId(), libroId);
            
            if (success) {
                logger.info("Libro {} aggiunto alla libreria '{}' dell'utente: {}", libroId, nomeLibreria, userId);
            } else {
                logger.error("Fallimento nell'aggiunta del libro {} alla libreria '{}' dell'utente: {}", libroId, nomeLibreria, userId);
            }
            
            return success;
            
        } catch (Exception e) {
            logger.error("Errore durante l'aggiunta del libro {} alla libreria '{}' dell'utente: {}", libroId, nomeLibreria, userId, e);
            return false;
        }
    }
    
    @Override
    public boolean rimuoviLibroDaLibreria(String userId, String nomeLibreria, long libroId) throws RemoteException {
        try {
            // Validazioni
            if (userId == null || nomeLibreria == null) {
                logger.warn("Parametri null per rimozione libro dalla libreria");
                return false;
            }
            
            // Trova la libreria
            Libreria libreria = librerieDAO.getLibreriaByUserIdAndNome(userId, nomeLibreria);
            if (libreria == null) {
                logger.warn("Libreria '{}' non trovata per l'utente: {}", nomeLibreria, userId);
                return false;
            }
            
            // Verifica se il libro è presente
            if (!librerieDAO.isLibroInLibreria(libreria.libreriaId(), libroId)) {
                logger.warn("Libro {} non presente nella libreria '{}' dell'utente: {}", libroId, nomeLibreria, userId);
                return false;
            }
            
            // Rimuovi il libro
            boolean success = librerieDAO.rimuoviLibroDaLibreria(libreria.libreriaId(), libroId);
            
            if (success) {
                logger.info("Libro {} rimosso dalla libreria '{}' dell'utente: {}", libroId, nomeLibreria, userId);
            } else {
                logger.error("Fallimento nella rimozione del libro {} dalla libreria '{}' dell'utente: {}", libroId, nomeLibreria, userId);
            }
            
            return success;
            
        } catch (Exception e) {
            logger.error("Errore durante la rimozione del libro {} dalla libreria '{}' dell'utente: {}", libroId, nomeLibreria, userId, e);
            return false;
        }
    }
    
    @Override
    public List<String> getLibrerieUtente(String userId) throws RemoteException {
        try {
            if (userId == null || userId.trim().isEmpty()) {
                logger.warn("Tentativo di ottenere librerie con userId null o vuoto");
                return List.of();
            }
            
            List<Libreria> librerie = librerieDAO.getLibrerieByUserId(userId);
            List<String> nomiLibrerie = librerie.stream()
                    .map(Libreria::nomeLibreria)
                    .toList();
            
            logger.info("Recuperate {} librerie per l'utente: {}", nomiLibrerie.size(), userId);
            return nomiLibrerie;
            
        } catch (Exception e) {
            logger.error("Errore durante il recupero delle librerie per l'utente: {}", userId, e);
            return List.of();
        }
    }
    
    @Override
    public List<Long> getLibriInLibreria(String userId, String nomeLibreria) throws RemoteException {
        try {
            if (userId == null || nomeLibreria == null) {
                logger.warn("Parametri null per recupero libri in libreria");
                return List.of();
            }
            
            Libreria libreria = librerieDAO.getLibreriaByUserIdAndNome(userId, nomeLibreria);
            if (libreria == null) {
                logger.warn("Libreria '{}' non trovata per l'utente: {}", nomeLibreria, userId);
                return List.of();
            }
            
            List<Long> libriIds = libreria.libriIds();
            logger.info("Recuperati {} libri dalla libreria '{}' dell'utente: {}", libriIds.size(), nomeLibreria, userId);
            return libriIds;
            
        } catch (Exception e) {
            logger.error("Errore durante il recupero dei libri dalla libreria '{}' dell'utente: {}", nomeLibreria, userId, e);
            return List.of();
        }
    }
    
    @Override
    public boolean eliminaLibreria(String userId, String nomeLibreria) throws RemoteException {
        try {
            if (userId == null || nomeLibreria == null) {
                logger.warn("Parametri null per eliminazione libreria");
                return false;
            }
            
            Libreria libreria = librerieDAO.getLibreriaByUserIdAndNome(userId, nomeLibreria);
            if (libreria == null) {
                logger.warn("Libreria '{}' non trovata per l'utente: {}", nomeLibreria, userId);
                return false;
            }
            
            boolean success = librerieDAO.eliminaLibreria(libreria.libreriaId());
            
            if (success) {
                logger.info("Libreria '{}' eliminata con successo per l'utente: {}", nomeLibreria, userId);
            } else {
                logger.error("Fallimento nell'eliminazione della libreria '{}' per l'utente: {}", nomeLibreria, userId);
            }
            
            return success;
            
        } catch (Exception e) {
            logger.error("Errore durante l'eliminazione della libreria '{}' per l'utente: {}", nomeLibreria, userId, e);
            return false;
        }
    }
    
    @Override
    public boolean isLibroInLibreria(String userId, String nomeLibreria, long libroId) throws RemoteException {
        try {
            if (userId == null || nomeLibreria == null) {
                return false;
            }
            
            Libreria libreria = librerieDAO.getLibreriaByUserIdAndNome(userId, nomeLibreria);
            if (libreria == null) {
                return false;
            }
            
            return librerieDAO.isLibroInLibreria(libreria.libreriaId(), libroId);
            
        } catch (Exception e) {
            logger.error("Errore durante la verifica del libro nella libreria", e);
            return false;
        }
    }
    
    @Override
    public boolean isNomeLibreriaEsistente(String userId, String nomeLibreria) throws RemoteException {
        try {
            if (userId == null || nomeLibreria == null) {
                return false;
            }
            
            return librerieDAO.isNomeLibreriaEsistente(userId, nomeLibreria);
            
        } catch (Exception e) {
            logger.error("Errore durante la verifica del nome libreria esistente", e);
            return false;
        }
    }
}