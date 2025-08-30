package bookrecommender.server.libri;

import bookrecommender.condivisi.libri.Libro;
import bookrecommender.condivisi.libri.CercaLibriService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

/**
 * Implementazione concreta del servizio RMI {@link CercaLibriService}.
 * <p>
 * Questa classe agisce come il "Service Layer" per le operazioni di ricerca dei libri.
 * Estende {@link UnicastRemoteObject} per essere esportabile e accessibile da client remoti tramite RMI.
 * <p>
 * Le sue responsabilità principali sono:
 * <ul>
 *     <li>Implementare la logica di business per le ricerche definite nell'interfaccia {@link CercaLibriService}.</li>
 *     <li>Delegare le operazioni di accesso ai dati al DAO appropriato ({@link LibroDAO}).</li>
 *     <li>Gestire le eccezioni provenienti dal layer di persistenza, incapsulandole in {@link RemoteException}
 *         per notificarle al client remoto.</li>
 *     <li>Loggare le operazioni di ricerca per scopi di monitoraggio e debug.</li>
 * </ul>
 * Questa implementazione è stateless e thread-safe, poiché si affida a un DAO stateless che gestisce
 * le connessioni al database per ogni singola operazione.
 * @author Abou Aziz Sara Hesham Abdel Hamid 757004
 * @author Ben Mahjoub Ali 759560
 * @author Hidri Mohamed Taha 756235
 * @author Zoghbani Lilia 759652
 * @see CercaLibriService
 * @see LibroDAO
 * @see JdbcCercaLibriDAO
 * @see java.rmi.server.UnicastRemoteObject
 * @version 1.0
 */
public class CercaLibriServiceImpl extends UnicastRemoteObject implements CercaLibriService {
    private static final Logger logger = LogManager.getLogger(CercaLibriServiceImpl.class);
    private final LibroDAO libroDAO;

    /**
     * Costruisce e inizializza il servizio di ricerca libri.
     * <p>
     * Il costruttore invoca il costruttore della superclasse {@link UnicastRemoteObject} per
     * esportare l'oggetto e renderlo disponibile per le chiamate remote. Inizializza
     * inoltre l'implementazione del DAO ({@link JdbcCercaLibriDAO}) che verrà utilizzata
     * per interagire con il database.
     *
     * @throws RemoteException se si verifica un errore durante l'esportazione dell'oggetto RMI.
     */
    public CercaLibriServiceImpl() throws RemoteException {
        super();
        this.libroDAO = new JdbcCercaLibriDAO(); // DAO stateless: sicuro per concorrenza
        logger.info("CercaLibriServiceImpl inizializzato con DAO stateless");
    }

    /**
     * {@inheritDoc}
     * <p>
     * Questa implementazione delega la ricerca al metodo {@link LibroDAO#cercaLibriPerTitolo(String)}.
     * Qualsiasi eccezione sollevata dal layer di persistenza viene catturata, loggata e
     * incapsulata in una {@link RemoteException}.
     */
    @Override
    public List<Libro> cercaLibro_Per_Titolo(String titolo) throws RemoteException {
        try {
            logger.info("Ricerca libri per titolo: {}", titolo);
            List<Libro> risultati = libroDAO.cercaLibriPerTitolo(titolo); // la DAO gestisce la connection
            logger.info("Trovati {} libri per titolo '{}'", risultati.size(), titolo);
            return risultati;
        } catch (Exception e) {
            logger.error("Errore durante la ricerca per titolo: " + titolo, e);
            throw new RemoteException("Errore durante la ricerca per titolo", e);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Questa implementazione delega la ricerca al metodo {@link LibroDAO#getLibroById(int)}.
     * Qualsiasi eccezione sollevata dal layer di persistenza viene catturata, loggata e
     * incapsulata in una {@link RemoteException}.
     */
    @Override
    public Libro getTitoloLibroById(int id) throws RemoteException {
        try {
            logger.info("Ricerca libro per ID: {}", id);
            Libro libro = libroDAO.getLibroById(id);
            if (libro != null) logger.info("Libro trovato: {}", libro.titolo());
            else logger.info("Nessun libro trovato con ID: {}", id);
            return libro;
        } catch (Exception e) {
            logger.error("Errore durante la ricerca per ID: " + id, e);
            throw new RemoteException("Errore durante la ricerca per ID", e);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Questa implementazione delega la ricerca al metodo {@link LibroDAO#cercaLibriPerAutore(String)}.
     * Qualsiasi eccezione sollevata dal layer di persistenza viene catturata, loggata e
     * incapsulata in una {@link RemoteException}.
     */
    @Override
    public List<Libro> cercaLibro_Per_Autore(String autore) throws RemoteException {
        try {
            logger.info("Ricerca libri per autore: {}", autore);
            List<Libro> risultati = libroDAO.cercaLibriPerAutore(autore);
            logger.info("Trovati {} libri per autore '{}'", risultati.size(), autore);
            return risultati;
        } catch (Exception e) {
            logger.error("Errore durante la ricerca per autore: " + autore, e);
            throw new RemoteException("Errore durante la ricerca per autore", e);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Questa implementazione delega la ricerca al metodo {@link LibroDAO#cercaLibriPerAutoreEAnno(String, String)}.
     * Qualsiasi eccezione sollevata dal layer di persistenza viene catturata, loggata e
     * incapsulata in una {@link RemoteException}.
     */
    @Override
    public List<Libro> cercaLibro_Per_Autore_e_Anno(String autore, String anno) throws RemoteException {
        try {
            logger.info("Ricerca libri per autore '{}' e anno '{}'", autore, anno);
            List<Libro> risultati = libroDAO.cercaLibriPerAutoreEAnno(autore, anno);
            logger.info("Trovati {} libri per autore '{}' e anno '{}'", risultati.size(), autore, anno);
            return risultati;
        } catch (Exception e) {
            logger.error("Errore durante la ricerca per autore e anno: " + autore + ", " + anno, e);
            throw new RemoteException("Errore durante la ricerca per autore e anno", e);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Questa implementazione delega la ricerca al metodo {@link LibroDAO#cercaLibroPerId(Long)}.
     * Qualsiasi eccezione sollevata dal layer di persistenza viene catturata, loggata e
     * incapsulata in una {@link RemoteException}.
     */
    @Override
    public List<Libro> cercaLibro_Per_Id(Long id) throws RemoteException {
        try {
            logger.info("Ricerca libro per ID: {}", id);
            List<Libro> risultati = libroDAO.cercaLibroPerId(id);
            logger.info("Trovati {} libri per ID '{}'", risultati.size(), id);
            return risultati;
        } catch (Exception e) {
            logger.error("Errore durante la ricerca per ID: " + id, e);
            throw new RemoteException("Errore durante la ricerca per ID", e);
        }
    }
}
