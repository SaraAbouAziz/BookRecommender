package bookrecommender.condivisi.libri;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
/**
 * Interfaccia remota per il servizio di ricerca dei libri.
 * <p>
 * Definisce il contratto per le operazioni di ricerca che possono essere invocate
 * da un client remoto tramite RMI. Questa interfaccia astrae le modalità di
 * ricerca dei libri nel catalogo dell'applicazione, come la ricerca per titolo,
 * autore, o identificativo univoco.
 * <p>
 * Tutte le operazioni definite possono lanciare una {@link RemoteException} per
 * segnalare problemi di comunicazione o errori durante l'esecuzione remota.
 *
 * @author Abou Aziz Sara Hesham Abdel Hamid 757004
 * @author Ben Mahjoub Ali 759560
 * @author Hidri Mohamed Taha 756235
 * @author Zoghbani Lilia 759652
 * @see java.rmi.Remote
 * @see java.rmi.RemoteException
 * @see Libro
 * @version 1.0
 */
public interface CercaLibriService extends Remote {

    /**
     * Nome pubblico utilizzato per registrare e cercare questo servizio
     * nel registro RMI.
     */
    String NAME = "CercaLibriService";

    /**
     * Cerca i libri il cui titolo contiene la stringa specificata.
     * La ricerca è tipicamente case-insensitive e basata su corrispondenze parziali.
     *
     * @param titolo Il testo da cercare nel titolo dei libri.
     * @return Una {@link List} di oggetti {@link Libro} che corrispondono al criterio.
     *         Restituisce una lista vuota se nessun libro viene trovato.
     * @throws RemoteException Se si verifica un errore di comunicazione durante la chiamata remota.
     */
    List<Libro> cercaLibro_Per_Titolo(String titolo) throws RemoteException;

    /**
     * Cerca i libri il cui nome dell'autore contiene la stringa specificata.
     * La ricerca è tipicamente case-insensitive e basata su corrispondenze parziali.
     *
     * @param autore Il testo da cercare nel nome dell'autore.
     * @return Una {@link List} di oggetti {@link Libro} che corrispondono al criterio.
     *         Restituisce una lista vuota se nessun libro viene trovato.
     * @throws RemoteException Se si verifica un errore di comunicazione durante la chiamata remota.
     */
    List<Libro> cercaLibro_Per_Autore(String autore) throws RemoteException;

    /**
     * Cerca i libri che corrispondono sia al nome dell'autore (ricerca parziale)
     * sia all'anno di pubblicazione esatto.
     *
     * @param Autore La stringa da cercare nel nome dell'autore.
     * @param Anno   L'anno di pubblicazione esatto.
     * @return Una {@link List} di oggetti {@link Libro} che soddisfano entrambi i criteri.
     *         Restituisce una lista vuota se nessun libro viene trovato.
     * @throws RemoteException Se si verifica un errore di comunicazione durante la chiamata remota.
     */
    List<Libro> cercaLibro_Per_Autore_e_Anno(String Autore, String Anno) throws RemoteException;    

    /**
     * Recupera un singolo libro tramite il suo identificativo univoco (ID).
     *
     * @param id L'ID univoco del libro da recuperare.
     * @return L'oggetto {@link Libro} corrispondente all'ID specificato, o {@code null} se non trovato.
     * @throws RemoteException Se si verifica un errore di comunicazione durante la chiamata remota.
     */
    Libro getTitoloLibroById(int id) throws RemoteException;

    /**
     * Cerca un libro tramite il suo identificativo univoco (ID) e lo restituisce in una lista.
     *
     * @param id L'ID univoco del libro da cercare.
     * @return Una {@link List} contenente il singolo {@link Libro} se trovato, altrimenti una lista vuota.
     * @throws RemoteException Se si verifica un errore di comunicazione durante la chiamata remota.
     */
    List<Libro> cercaLibro_Per_Id(Long id) throws RemoteException;
   
}

