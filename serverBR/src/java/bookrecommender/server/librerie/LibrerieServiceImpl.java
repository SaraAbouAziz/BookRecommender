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
 * Implementazione concreta del servizio RMI {@link LibrerieService}.
 * <p>
 * Questa classe agisce come "Service Layer" per la gestione delle librerie personali
 * degli utenti sul lato server. Estende {@link UnicastRemoteObject} per essere
 * esportabile e accessibile da client remoti tramite RMI.
 * <p>
 * <b>Responsabilità principali:</b>
 * <ul>
 *     <li>Implementare la logica di business per le operazioni sulle librerie.</li>
 *     <li>Validare i dati ricevuti dal client (es. ID utente, nomi non vuoti).</li>
 *     <li>Coordinare le operazioni di accesso ai dati delegando al DAO appropriato ({@link LibrerieDAO}).</li>
 *     <li>Gestire le eccezioni e loggare le operazioni in modo appropriato, restituendo valori
 *         semplici (boolean, liste vuote) al client per semplificare la gestione degli errori remoti.</li>
 * </ul>
 *
 * @see LibrerieService
 * @see LibrerieDAO
 * @see JdbcLibrerieDAO
 */
public class LibrerieServiceImpl extends UnicastRemoteObject implements LibrerieService {
    
    private static final Logger logger = LogManager.getLogger(LibrerieServiceImpl.class);
    private final LibrerieDAO librerieDAO;
    
    /**
     * Costruisce e inizializza il servizio per le librerie.
     * <p>
     * Il costruttore invoca il costruttore della superclasse {@link UnicastRemoteObject} per
     * esportare l'oggetto e renderlo disponibile per le chiamate remote. Inizializza
     * inoltre l'implementazione del DAO ({@link JdbcLibrerieDAO}) che verrà utilizzata
     * per interagire con il database.
     *
     * @throws RemoteException se si verifica un errore durante l'esportazione dell'oggetto RMI.
     */
    public LibrerieServiceImpl() throws RemoteException {
        super();
        this.librerieDAO = new JdbcLibrerieDAO();
    }
    
    /**
     * {@inheritDoc}
     * <p>
     * Questa implementazione valida i parametri, verifica che non esista già una libreria
     * con lo stesso nome per lo stesso utente e, in caso di successo, delega la creazione
     * al {@link LibrerieDAO}.
     * In caso di qualsiasi errore (validazione, duplicato, eccezione dal DAO), logga l'evento
     * e restituisce {@code false}.
     */
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
    
    /**
     * {@inheritDoc}
     * <p>
     * Questa implementazione prima recupera la libreria tramite {@code userId} e {@code nomeLibreria}.
     * Se la libreria esiste, verifica se il libro è già presente. Se non lo è, delega
     * l'aggiunta al {@link LibrerieDAO}.
     * Restituisce {@code true} anche se il libro è già presente, poiché lo stato finale desiderato
     * (libro nella libreria) è comunque raggiunto.
     * In caso di errore (libreria non trovata, eccezione dal DAO), logga e restituisce {@code false}.
     */
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
    
    /**
     * {@inheritDoc}
     * <p>
     * Questa implementazione prima recupera la libreria tramite {@code userId} e {@code nomeLibreria}.
     * Se la libreria esiste e contiene il libro, delega la rimozione al {@link LibrerieDAO}.
     * Se la libreria non viene trovata o il libro non è presente, l'operazione è considerata fallita
     * e viene restituito {@code false}.
     * In caso di eccezione dal DAO, logga e restituisce {@code false}.
     */
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
    
    /**
     * {@inheritDoc}
     * <p>
     * Questa implementazione delega la ricerca delle librerie al {@link LibrerieDAO} e poi
     * mappa la lista di oggetti {@link Libreria} in una lista di stringhe contenenti solo i nomi.
     * In caso di errore, logga e restituisce una lista vuota per evitare che il client riceva un'eccezione.
     */
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
    
    /**
     * {@inheritDoc}
     * <p>
     * Questa implementazione prima recupera l'oggetto {@link Libreria} completo tramite
     * {@code userId} e {@code nomeLibreria}. Se la libreria viene trovata, restituisce la lista
     * di ID dei libri contenuta al suo interno.
     * In caso di errore o se la libreria non viene trovata, logga e restituisce una lista vuota.
     */
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
    
    /**
     * {@inheritDoc}
     * <p>
     * Questa implementazione prima recupera l'oggetto {@link Libreria} per ottenere il suo ID,
     * poi delega l'operazione di eliminazione al {@link LibrerieDAO} usando l'ID.
     * Se la libreria non viene trovata, l'operazione fallisce e restituisce {@code false}.
     */
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
    
    /**
     * {@inheritDoc}
     * <p>
     * Questa implementazione prima recupera l'oggetto {@link Libreria} per ottenere il suo ID,
     * poi delega la verifica al {@link LibrerieDAO}. Se la libreria non viene trovata,
     * restituisce {@code false}.
     */
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
    
    /**
     * {@inheritDoc}
     * <p>
     * Delega direttamente la chiamata al metodo corrispondente del {@link LibrerieDAO}.
     */
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

    /**
     * {@inheritDoc}
     * <p>
     * Questa implementazione recupera l'oggetto {@link Libreria} completo e, se trovato,
     * ne restituisce l'ID. Se la libreria non esiste, restituisce {@code -1}.
     * A differenza di altri metodi, propaga una {@link RemoteException} in caso di errore del server.
     */
    @Override
    public int getLibreriaId(String userId, String nomeLibreria) throws RemoteException {
        try {
            if (userId == null || nomeLibreria == null) {
                logger.warn("Parametri null per getLibreriaId");
                return -1;
            }
            
            Libreria libreria = librerieDAO.getLibreriaByUserIdAndNome(userId, nomeLibreria);
            
            if (libreria != null) {
                return libreria.libreriaId();
            } else {
                logger.warn("Libreria '{}' non trovata per l'utente: {}", nomeLibreria, userId);
                return -1;
            }
        } catch (Exception e) {
            logger.error("Errore durante il recupero dell'ID della libreria '{}' per l'utente: {}", nomeLibreria, userId, e);
            throw new RemoteException("Errore del server durante il recupero della libreria.", e);
        }
    }
}