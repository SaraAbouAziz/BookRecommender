package bookrecommender.server.libri;

import bookrecommender.condivisi.libri.Libro;
import bookrecommender.condivisi.libri.CercaLibriService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

/**
 * Implementazione RMI del servizio di ricerca libri.
 * Usa DAO stateless: JdbcCercaLibriDAO apre/chiude le connection per ogni operazione.
 * Thread-safe se JdbcCercaLibriDAO è stateless (come progettato).
 */
public class CercaLibriServiceImpl extends UnicastRemoteObject implements CercaLibriService {
    private static final Logger logger = LogManager.getLogger(CercaLibriServiceImpl.class);
    private final LibroDAO libroDAO;

    public CercaLibriServiceImpl() throws RemoteException {
        super();
        this.libroDAO = new JdbcCercaLibriDAO(); // DAO stateless: sicuro per concorrenza
        logger.info("CercaLibriServiceImpl inizializzato con DAO stateless");
    }

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
}
