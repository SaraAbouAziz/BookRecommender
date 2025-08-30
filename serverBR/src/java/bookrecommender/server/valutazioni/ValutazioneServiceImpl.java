package bookrecommender.server.valutazioni;

import bookrecommender.condivisi.valutazioni.ValutazioneService;
import bookrecommender.condivisi.valutazioni.ValutazioneDettagliata;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

/**
 * Implementazione del servizio RMI {@link ValutazioneService} per la gestione delle valutazioni dei libri.
 * <p>
 * Questa classe agisce come service layer, esponendo le funzionalità di valutazione ai client remoti.
 * Estende {@link UnicastRemoteObject} per essere esportabile come oggetto remoto RMI e implementa
 * l'interfaccia {@code ValutazioneService} che definisce il contratto del servizio.
 * <p>
 * La logica di business e l'accesso ai dati sono delegati a un'istanza di {@link ValutazioniDAO},
 * che astrae la persistenza dei dati (in questo caso, tramite JDBC con {@link JdbcValutazioniDAO}).
 * <p>
 * Ogni metodo del servizio gestisce le eccezioni provenienti dal layer di persistenza,
 * loggandole e incapsulandole in una {@link RemoteException} per notificare il client
 * di un errore avvenuto sul server, come richiesto dal pattern RMI.
 *
 * @see ValutazioneService
 * @see ValutazioniDAO
 * @see JdbcValutazioniDAO
 * @see java.rmi.server.UnicastRemoteObject
 * @see java.rmi.RemoteException
 */
public class ValutazioneServiceImpl extends UnicastRemoteObject implements ValutazioneService {

    private static final Logger logger = LogManager.getLogger(ValutazioneServiceImpl.class);
    private final ValutazioniDAO valutazioniDAO = new JdbcValutazioniDAO();

    /**
     * Costruttore che inizializza l'oggetto remoto.
     * <p>
     * Chiama il costruttore della superclasse {@link UnicastRemoteObject} per esportare
     * l'oggetto e renderlo disponibile per le chiamate remote.
     *
     * @throws RemoteException se si verifica un errore durante l'esportazione dell'oggetto remoto.
     */
    public ValutazioneServiceImpl() throws RemoteException {
        super();
        logger.info("ValutazioneServiceImpl inizializzato");
    }

    /**
     * {@inheritDoc}
     * <p>
     * Delega la verifica al {@link ValutazioniDAO}.
     */
    @Override
    public boolean isLibroGiaValutato(int libroId, String userId) throws RemoteException {
        try {
            return valutazioniDAO.isLibroGiaValutato(libroId, userId);
        } catch (Exception e) {
            logger.error("Errore nel verificare valutazione esistente", e);
            throw new RemoteException("Errore verifica valutazione", e);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Prima di salvare, questo metodo esegue due controlli di validazione:
     * <ol>
     *     <li>Verifica che i punteggi (stile, contenuto, ecc.) siano nel range valido [1, 5].</li>
     *     <li>Verifica che l'utente non abbia già valutato lo stesso libro.</li>
     * </ol>
     * Se i controlli falliscono, restituisce {@code false}. Altrimenti, delega il salvataggio al {@link ValutazioniDAO}.
     */
    @Override
    public boolean salvaValutazione(String userId, int libroId, String nomeLibreria, int stile, String stileNote, int contenuto, String contenutoNote, int gradevolezza, String gradevolezzaNote, int originalita, String originalitaNote, int edizione, String edizioneNote, double votoFinale, String commentoFinale) throws RemoteException {
        try {
            if (!isValutazioneValida(stile, contenuto, gradevolezza, originalita, edizione)) {
                logger.warn("Tentativo di salvare una valutazione con punteggi non validi per userId={}, libroId={}", userId, libroId);
                return false;
            }
            if (isLibroGiaValutato(libroId, userId)) {
                logger.warn("Tentativo di salvare una valutazione duplicata per userId={}, libroId={}", userId, libroId);
                return false;
            }
            return valutazioniDAO.salvaValutazione(userId, libroId, nomeLibreria, stile, stileNote, contenuto, contenutoNote, gradevolezza, gradevolezzaNote, originalita, originalitaNote, edizione, edizioneNote, votoFinale, commentoFinale);
        } catch (Exception e) {
            logger.error("Errore nel salvataggio valutazione", e);
            throw new RemoteException("Errore salvataggio valutazione", e);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Delega il caricamento delle valutazioni al {@link ValutazioniDAO}.
     */
    @Override
    public List<Map<String, Object>> caricaValutazioniLibro(int libroId) throws RemoteException {
        try {
            return valutazioniDAO.caricaValutazioniLibro(libroId);
        } catch (Exception e) {
            logger.error("Errore nel caricamento valutazioni", e);
            throw new RemoteException("Errore caricamento valutazioni", e);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Delega il calcolo al {@link ValutazioniDAO}.
     */
    @Override
    public double calcolaMediaValutazioni(int libroId) throws RemoteException {
        try {
            return valutazioniDAO.calcolaMediaValutazioni(libroId);
        } catch (Exception e) {
            logger.error("Errore calcolo media valutazioni", e);
            throw new RemoteException("Errore calcolo media", e);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Delega il conteggio al {@link ValutazioniDAO}.
     */
    @Override
    public int getNumeroValutazioni(int libroId) throws RemoteException {
        try {
            return valutazioniDAO.getNumeroValutazioni(libroId);
        } catch (Exception e) {
            logger.error("Errore conteggio valutazioni", e);
            throw new RemoteException("Errore conteggio", e);
        }
    }
    
    /**
     * {@inheritDoc}
     * <p>
     * Delega il calcolo al {@link ValutazioniDAO}.
     */
    @Override
    public double calcolaMediaStile(int libroId) throws RemoteException {
        try {
            return valutazioniDAO.calcolaMediaStile(libroId);
        } catch (Exception e) {
            logger.error("Errore calcolo media stile", e);
            throw new RemoteException("Errore calcolo media stile", e);
        }
    }
    
    /**
     * {@inheritDoc}
     * <p>
     * Delega il calcolo al {@link ValutazioniDAO}.
     */
    @Override
    public double calcolaMediaContenuto(int libroId) throws RemoteException {
        try {
            return valutazioniDAO.calcolaMediaContenuto(libroId);
        } catch (Exception e) {
            logger.error("Errore calcolo media contenuto", e);
            throw new RemoteException("Errore calcolo media contenuto", e);
        }
    }
    
    /**
     * {@inheritDoc}
     * <p>
     * Delega il calcolo al {@link ValutazioniDAO}.
     */
    @Override
    public double calcolaMediaGradevolezza(int libroId) throws RemoteException {
        try {
            return valutazioniDAO.calcolaMediaGradevolezza(libroId);
        } catch (Exception e) {
            logger.error("Errore calcolo media gradevolezza", e);
            throw new RemoteException("Errore calcolo media gradevolezza", e);
        }
    }
    
    /**
     * {@inheritDoc}
     * <p>
     * Delega il calcolo al {@link ValutazioniDAO}.
     */
    @Override
    public double calcolaMediaOriginalita(int libroId) throws RemoteException {
        try {
            return valutazioniDAO.calcolaMediaOriginalita(libroId);
        } catch (Exception e) {
            logger.error("Errore calcolo media originalità", e);
            throw new RemoteException("Errore calcolo media originalità", e);
        }
    }
    
    /**
     * {@inheritDoc}
     * <p>
     * Delega il calcolo al {@link ValutazioniDAO}.
     */
    @Override
    public double calcolaMediaEdizione(int libroId) throws RemoteException {
        try {
            return valutazioniDAO.calcolaMediaEdizione(libroId);
        } catch (Exception e) {
            logger.error("Errore calcolo media edizione", e);
            throw new RemoteException("Errore calcolo media edizione", e);
        }
    }

    /**
     * Metodo di utilità privato per validare i punteggi di una valutazione.
     * Verifica che ogni punteggio sia compreso nell'intervallo [1, 5].
     *
     * @param stile Punteggio per lo stile.
     * @param contenuto Punteggio per il contenuto.
     * @param gradevolezza Punteggio per la gradevolezza.
     * @param originalita Punteggio per l'originalità.
     * @param edizione Punteggio per la qualità dell'edizione.
     * @return {@code true} se tutti i punteggi sono validi, {@code false} altrimenti.
     */
    private boolean isValutazioneValida(int stile, int contenuto, int gradevolezza, int originalita, int edizione) {
        return stile >= 1 && stile <= 5 &&
                contenuto >= 1 && contenuto <= 5 &&
                gradevolezza >= 1 && gradevolezza <= 5 &&
                originalita >= 1 && originalita <= 5 &&
                edizione >= 1 && edizione <= 5;
    }

    // CSV helpers rimossi: ora si usa il DB via DAO

    /**
     * {@inheritDoc}
     * <p>
     * Esegue un controllo preliminare per assicurarsi che l'ID utente non sia nullo o vuoto,
     * dopodiché delega il recupero dei dati al {@link ValutazioniDAO}.
     *
     * @throws IllegalArgumentException se l'ID utente fornito non è valido.
     */
    @Override
    public List<ValutazioneDettagliata> listValutazioniDettagliateByUser(String userId) throws RemoteException {
        logger.debug("Listing valutazioni dettagliate by userId={}", userId);
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("ID utente non valido.");
        }
        try {
            return valutazioniDAO.findValutazioniDettagliateByUser(userId);
        } catch (Exception e) {
            logger.error("Error while listing valutazioni dettagliate by user", e);
            throw new RemoteException("Errore del server durante il recupero delle valutazioni dell'utente.", e);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Esegue controlli di validazione sui parametri di input (ID utente, ID libro e punteggi)
     * prima di delegare l'operazione di aggiornamento al {@link ValutazioniDAO}.
     *
     * @throws IllegalArgumentException se i parametri di input non sono validi o se i punteggi
     *         sono fuori dal range consentito [1, 5].
     */
    @Override
    public boolean aggiornaValutazione(String userId, int libroId, int stile, String stileNote,
                                      int contenuto, String contenutoNote, int gradevolezza, String gradevolezzaNote,
                                      int originalita, String originalitaNote, int edizione, String edizioneNote,
                                      double votoFinale, String commentoFinale) throws RemoteException {
        logger.info("Updating valutazione for userId={}, libroId={}", userId, libroId);
        
        if (userId == null || userId.isBlank() || libroId <= 0) {
            throw new IllegalArgumentException("Parametri non validi.");
        }
        
        if (!isValutazioneValida(stile, contenuto, gradevolezza, originalita, edizione)) {
            throw new IllegalArgumentException("I punteggi devono essere compresi tra 1 e 5.");
        }
        
        try {
            return valutazioniDAO.aggiornaValutazione(userId, libroId, stile, stileNote, contenuto, contenutoNote,
                                         gradevolezza, gradevolezzaNote, originalita, originalitaNote,
                                         edizione, edizioneNote, votoFinale, commentoFinale);
        } catch (Exception e) {
            logger.error("Error while updating valutazione", e);
            throw new RemoteException("Errore del server durante l'aggiornamento della valutazione.", e);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Esegue un controllo di validazione sui parametri di input prima di delegare
     * l'operazione di eliminazione al {@link ValutazioniDAO}.
     *
     * @throws IllegalArgumentException se i parametri di input non sono validi.
     */
    @Override
    public boolean eliminaValutazione(String userId, int libroId) throws RemoteException {
        logger.info("Deleting valutazione for userId={}, libroId={}", userId, libroId);
        
        if (userId == null || userId.isBlank() || libroId <= 0) {
            throw new IllegalArgumentException("Parametri non validi.");
        }
        
        try {
            return valutazioniDAO.eliminaValutazione(userId, libroId);
        } catch (Exception e) {
            logger.error("Error while deleting valutazione", e);
            throw new RemoteException("Errore del server durante l'eliminazione della valutazione.", e);
        }
    }
}
