package bookrecommender.server.consigli;

import bookrecommender.condivisi.consigli.ConsigliService;
import bookrecommender.condivisi.consigli.Consiglio;
import bookrecommender.condivisi.consigli.ConsiglioDettagliato;
import bookrecommender.condivisi.consigli.LibroConsigliato;
import bookrecommender.condivisi.libri.Libro;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Implementazione concreta del servizio RMI {@link ConsigliService}.
 * <p>
 * Questa classe agisce come "Service Layer" per la gestione dei consigli sui libri
 * sul lato server. Estende {@link UnicastRemoteObject} per essere esportabile e
 * accessibile da client remoti tramite RMI.
 * <p>
 * <b>Responsabilità principali:</b>
 * <ul>
 *     <li>Implementare la logica di business per le operazioni sui consigli, come
 *         la validazione dei dati e l'applicazione di regole (es. limite massimo di consigli).</li>
 *     <li>Coordinare le operazioni di accesso ai dati delegando al DAO appropriato ({@link ConsigliDAO}).</li>
 *     <li>Gestire le eccezioni provenienti dai layer inferiori, incapsulandole in
 *         {@link RemoteException} o lanciando eccezioni di business specifiche
 *         (es. {@link IllegalArgumentException}, {@link IllegalStateException}).</li>
 *     <li>Loggare le operazioni per scopi di monitoraggio e debug.</li>
 * </ul>
 * Questa implementazione è stateless e thread-safe.
 * @author Abou Aziz Sara Hesham Abdel Hamid 757004
 * @author Ben Mahjoub Ali 759560
 * @author Hidri Mohamed Taha 756235
 * @author Zoghbani Lilia 759652
 * @see ConsigliService
 * @see ConsigliDAO
 * @see JdbcConsigliDAO
 * @version 1.0
 */
public class ConsigliServiceImpl extends UnicastRemoteObject implements ConsigliService {

    /** Campo per il controllo della versione durante la serializzazione. */
    private static final long serialVersionUID = 1L;
    /** Logger per la registrazione degli eventi della classe. */
    private static final Logger logger = LogManager.getLogger(ConsigliServiceImpl.class);
    /** Numero massimo di consigli che un utente può dare per un singolo libro. */
    private static final int MAX_CONSIGLI = 3;
    /** Istanza del Data Access Object per interagire con la persistenza dei consigli. */
    private final ConsigliDAO consigliDAO;

    /**
     * Costruisce e inizializza il servizio di gestione dei consigli.
     * <p>
     * Il costruttore invoca il costruttore della superclasse {@link UnicastRemoteObject} per
     * esportare l'oggetto e renderlo disponibile per le chiamate remote. Inizializza
     * inoltre l'implementazione del DAO ({@link JdbcConsigliDAO}) che verrà utilizzata
     * per interagire con il database.
     *
     * @throws RemoteException se si verifica un errore durante l'esportazione dell'oggetto RMI.
     */
    public ConsigliServiceImpl() throws RemoteException {
        super();
        this.consigliDAO = new JdbcConsigliDAO();
        logger.info("ConsigliService implementation created.");
    }

    /**
     * {@inheritDoc}
     * <p>
     * Questa implementazione prima valida i parametri di input. Successivamente, applica
     * le seguenti regole di business:
     * <ul>
     *     <li>Un utente non può consigliare lo stesso libro che ha letto.</li>
     *     <li>Un utente non può superare il limite di {@value #MAX_CONSIGLI} consigli per un dato libro letto.</li>
     * </ul>
     * Se tutte le validazioni e le regole passano, delega l'operazione di aggiunta al {@link ConsigliDAO}.
     *
     * @throws IllegalArgumentException se i parametri forniti non sono validi o se si tenta di consigliare lo stesso libro.
     * @throws IllegalStateException se l'utente ha già raggiunto il numero massimo di consigli per il libro.
     * @throws RemoteException se si verifica un errore a livello di persistenza o di comunicazione.
     */
    @Override
    public void aggiungiConsiglio(String userId, int libreriaId, long libroLettoId, long libroConsigliatoId, String commento) throws RemoteException {
        logger.info("Attempting to add consiglio: userId={}, libreriaId={}, libroLettoId={}, libroConsigliatoId={}", userId, libreriaId, libroLettoId, libroConsigliatoId);

        if (userId == null || userId.isBlank() || libreriaId <= 0 || libroLettoId <= 0 || libroConsigliatoId <= 0) {
            logger.warn("Invalid arguments for aggiungiConsiglio");
            throw new IllegalArgumentException("ID utente, libreria o libro non validi.");
        }

        if (libroLettoId == libroConsigliatoId) {
            logger.warn("User {} tried to recommend the same book {}", userId, libroLettoId);
            throw new IllegalArgumentException("Non puoi consigliare lo stesso libro.");
        }

        if (getNumeroConsigliDati(userId, libroLettoId) >= MAX_CONSIGLI) {
            logger.warn("User {} reached max suggestions for book {} in library {}", userId, libroLettoId, libreriaId);
            throw new IllegalStateException("Limite massimo di " + MAX_CONSIGLI + " consigli raggiunto per questo libro.");
        }

        try {
            consigliDAO.add(userId, libreriaId, libroLettoId, libroConsigliatoId, commento);
            logger.info("Consiglio added successfully.");
        } catch (Exception e) {
            logger.error("Error while adding consiglio in service", e);
            throw new RemoteException("Errore del server durante l'aggiunta del consiglio.", e);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Questa implementazione valida i parametri e delega la chiamata al metodo
     * {@link ConsigliDAO#findConsigliati(int, long)}.
     */
    @Override
    public List<Libro> getConsigliati(int libreriaId, long libroLettoId) throws RemoteException {
        logger.debug("Getting consigliati for libreriaId={}, libroLettoId={}", libreriaId, libroLettoId);
        if (libreriaId <= 0 || libroLettoId <= 0) {
            throw new IllegalArgumentException("ID libreria o libro non validi.");
        }
        try {
            return consigliDAO.findConsigliati(libreriaId, libroLettoId);
        } catch (Exception e) {
            logger.error("Error while getting consigliati in service", e);
            throw new RemoteException("Errore del server durante il recupero dei consigli.", e);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Questa implementazione valida i parametri e delega la chiamata al metodo
     * {@link ConsigliDAO#findConsigliatiConConteggio(int, long)}.
     */
    @Override
    public List<LibroConsigliato> getConsigliatiConConteggio(int libreriaId, long libroLettoId) throws RemoteException {
        logger.debug("Getting consigliati con conteggio for libreriaId={}, libroLettoId={}", libreriaId, libroLettoId);
        if (libreriaId <= 0 || libroLettoId <= 0) {
            throw new IllegalArgumentException("ID libreria o libro non validi.");
        }
        try {
            return consigliDAO.findConsigliatiConConteggio(libreriaId, libroLettoId);
        } catch (Exception e) {
            logger.error("Error while getting consigliati con conteggio in service", e);
            throw new RemoteException("Errore del server durante il recupero dei consigli con conteggio.", e);
        }
    }

    /**
     * Conta il numero di consigli dati da un utente per un libro specifico,
     * aggregando i risultati da tutte le librerie.
     * <p>
     * Questo è un metodo di supporto utilizzato internamente per applicare le regole
     * di business, come il limite massimo di consigli.
     *
     * @param userId L'ID dell'utente.
     * @param libroLettoId L'ID del libro letto.
     * @return Il numero di consigli dati.
     * @throws RemoteException Se si verifica un errore di comunicazione o nel server.
     */
    private int getNumeroConsigliDati(String userId, long libroLettoId) throws RemoteException {
        logger.debug("Getting numero consigli dati for userId={}, libroLettoId={}", userId, libroLettoId);
        if (userId == null || userId.isBlank() || libroLettoId <= 0) {
            throw new IllegalArgumentException("ID utente o libro non validi.");
        }
        try {
            return consigliDAO.countConsigliDati(userId,  libroLettoId);
        } catch (Exception e) {
            logger.error("Error while getting numero consigli dati in service", e);
            throw new RemoteException("Errore del server durante il conteggio dei consigli.", e);
        }
    }


    /**
     * {@inheritDoc}
     * <p>
     * Questa implementazione valida il parametro e delega la chiamata al metodo
     * {@link ConsigliDAO#findConsigliatiConConteggio(long)}.
     */
     @Override
    public List<LibroConsigliato> getConsigliatiConConteggio(long libroLettoId) throws RemoteException {
        logger.debug("Getting consigliati con conteggio for libroLettoId={}", libroLettoId);
        if (libroLettoId <= 0) {
            throw new IllegalArgumentException("ID libro non valido.");
        }
        try {
            return consigliDAO.findConsigliatiConConteggio(libroLettoId);
        } catch (Exception e) {
            logger.error("Error while getting consigliati con conteggio in service", e);
            throw new RemoteException("Errore del server durante il recupero dei consigli con conteggio.", e);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Questa implementazione valida il parametro e delega la chiamata al metodo
     * {@link ConsigliDAO#findByUser(String)}.
     */
    @Override
    public List<Consiglio> listConsigliByUser(String userId) throws RemoteException {
        logger.debug("Listing consigli by userId={}", userId);
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("ID utente non valido.");
        }
        try {
            return consigliDAO.findByUser(userId);
        } catch (Exception e) {
            logger.error("Error while listing consigli by user", e);
            throw new RemoteException("Errore del server durante il recupero dei consigli dell'utente.", e);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Questa implementazione valida i parametri e delega la chiamata al metodo
     * {@link ConsigliDAO#updateCommento(String, int, long, long, String)}.
     */
    @Override
    public void updateCommento(String userId, int libreriaId, long libroLettoId, long libroConsigliatoId, String commento) throws RemoteException {
        logger.info("Updating commento for userId={}, libreriaId={}, libroLettoId={}, libroConsigliatoId={}", userId, libreriaId, libroLettoId, libroConsigliatoId);
        if (userId == null || userId.isBlank() || libreriaId <= 0 || libroLettoId <= 0 || libroConsigliatoId <= 0) {
            throw new IllegalArgumentException("Parametri non validi.");
        }
        try {
            consigliDAO.updateCommento(userId, libreriaId, libroLettoId, libroConsigliatoId, commento);
        } catch (Exception e) {
            logger.error("Error while updating commento", e);
            throw new RemoteException("Errore del server durante l'aggiornamento del commento.", e);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Questa implementazione valida i parametri e delega la chiamata al metodo
     * {@link ConsigliDAO#delete(String, int, long, long)}.
     */
    @Override
    public void deleteConsiglio(String userId, int libreriaId, long libroLettoId, long libroConsigliatoId) throws RemoteException {
        logger.info("Deleting consiglio for userId={}, libreriaId={}, libroLettoId={}, libroConsigliatoId={}", userId, libreriaId, libroLettoId, libroConsigliatoId);
        if (userId == null || userId.isBlank() || libreriaId <= 0 || libroLettoId <= 0 || libroConsigliatoId <= 0) {
            throw new IllegalArgumentException("Parametri non validi.");
        }
        try {
            consigliDAO.delete(userId, libreriaId, libroLettoId, libroConsigliatoId);
        } catch (Exception e) {
            logger.error("Error while deleting consiglio", e);
            throw new RemoteException("Errore del server durante l'eliminazione del consiglio.", e);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Questa implementazione valida il parametro e delega la chiamata al metodo
     * {@link ConsigliDAO#findDettagliatiByUser(String)}.
     */
    @Override
    public List<ConsiglioDettagliato> listConsigliDettagliatiByUser(String userId) throws RemoteException {
        logger.debug("Listing consigli dettagliati by userId={}", userId);
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("ID utente non valido.");
        }
        try {
            return consigliDAO.findDettagliatiByUser(userId);
        } catch (Exception e) {
            logger.error("Error while listing consigli dettagliati by user", e);
            throw new RemoteException("Errore del server durante il recupero dei consigli dettagliati dell'utente.", e);
        }
    }
}