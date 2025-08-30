package bookrecommender.condivisi.valutazioni;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

/**
 * Interfaccia remota per il servizio di gestione delle valutazioni dei libri,
 * progettata per essere utilizzata tramite RMI (Remote Method Invocation).
 * <p>
 * Definisce il contratto tra il client e il server per tutte le operazioni
 * relative alla creazione, lettura, aggiornamento ed eliminazione (CRUD) delle valutazioni.
 * Ogni metodo può lanciare una {@link RemoteException} per segnalare problemi
 * di comunicazione o errori avvenuti sul server durante l'esecuzione della chiamata.
 *
 * @author Abou Aziz Sara Hesham Abdel Hamid 757004
 * @author Ben Mahjoub Ali 759560
 * @author Hidri Mohamed Taha 756235
 * @author Zoghbani Lilia 759652
 * @see java.rmi.Remote
 * @see bookrecommender.server.valutazioni.ValutazioneServiceImpl
 * @see Valutazione
 * @see ValutazioneDettagliata
 * @version 1.0
 */
public interface ValutazioneService extends Remote {
    /**
     * Verifica se un libro è già stato valutato da un utente specifico.
     *
     * @param libroId L'ID del libro da controllare.
     * @param userId L'ID dell'utente da controllare.
     * @return {@code true} se esiste già una valutazione per la coppia libro-utente, {@code false} altrimenti.
     * @throws RemoteException Se si verifica un errore di comunicazione o un'eccezione sul server.
     */
    boolean isLibroGiaValutato(int libroId, String userId) throws RemoteException;

    /**
     * Salva una nuova valutazione completa per un libro.
     * <p>
     * L'implementazione di questo metodo dovrebbe verificare che il libro non sia già stato
     * valutato dallo stesso utente prima di procedere con l'inserimento.
     *
     * @param userId L'ID dell'utente che effettua la valutazione.
     * @param libroId L'ID del libro valutato.
     * @param nomeLibreria Il nome della libreria da cui è stata avviata la valutazione.
     * @param stile Punteggio (1-5) per il criterio "Stile".
     * @param stileNote Note testuali per il criterio "Stile".
     * @param contenuto Punteggio (1-5) per il criterio "Contenuto".
     * @param contenutoNote Note testuali per il criterio "Contenuto".
     * @param gradevolezza Punteggio (1-5) per il criterio "Gradevolezza".
     * @param gradevolezzaNote Note testuali per il criterio "Gradevolezza".
     * @param originalita Punteggio (1-5) per il criterio "Originalità".
     * @param originalitaNote Note testuali per il criterio "Originalità".
     * @param edizione Punteggio (1-5) per il criterio "Edizione".
     * @param edizioneNote Note testuali per il criterio "Edizione".
     * @param votoFinale Il voto complessivo calcolato.
     * @param commentoFinale Un commento finale riassuntivo.
     * @return {@code true} se la valutazione è stata salvata con successo, {@code false} altrimenti.
     * @throws RemoteException Se si verifica un errore di comunicazione o un'eccezione sul server.
     */
    boolean salvaValutazione(
            String userId,
            int libroId,
            String nomeLibreria,
            int stile, String stileNote,
            int contenuto, String contenutoNote,
            int gradevolezza, String gradevolezzaNote,
            int originalita, String originalitaNote,
            int edizione, String edizioneNote,
            double votoFinale, String commentoFinale
    ) throws RemoteException;

    /**
     * Carica tutte le valutazioni associate a un libro specifico.
     * <p>
     * Nota: questo metodo restituisce una lista di mappe generiche, probabilmente per
     * compatibilità con implementazioni precedenti. Per un uso più sicuro, si consiglia
     * di preferire metodi che restituiscono DTO specifici come {@link Valutazione}.
     *
     * @param libroId L'ID del libro di cui caricare le valutazioni.
     * @return Una lista di mappe, dove ogni mappa rappresenta una valutazione con chiavi
     *         corrispondenti ai campi della valutazione. La lista è vuota se non ci sono valutazioni.
     * @throws RemoteException Se si verifica un errore di comunicazione o un'eccezione sul server.
     */
    List<Map<String, Object>> caricaValutazioniLibro(int libroId) throws RemoteException;

    /**
     * Calcola la media aritmetica di tutti i voti complessivi per un dato libro.
     *
     * @param libroId L'ID del libro.
     * @return La media dei voti, o 0.0 se non ci sono valutazioni.
     * @throws RemoteException Se si verifica un errore di comunicazione o un'eccezione sul server.
     */
    double calcolaMediaValutazioni(int libroId) throws RemoteException;

    /**
     * Ottiene il numero totale di valutazioni ricevute da un libro.
     *
     * @param libroId L'ID del libro.
     * @return Il numero di valutazioni.
     * @throws RemoteException Se si verifica un errore di comunicazione o un'eccezione sul server.
     */
    int getNumeroValutazioni(int libroId) throws RemoteException;
    
    /**
     * Calcola la media delle valutazioni per il criterio "Stile" di un libro.
     *
     * @param libroId L'ID del libro.
     * @return La media del punteggio per lo stile, o 0.0 se non ci sono valutazioni.
     * @throws RemoteException Se si verifica un errore di comunicazione o un'eccezione sul server.
     */
    double calcolaMediaStile(int libroId) throws RemoteException;
    
    /**
     * Calcola la media delle valutazioni per il criterio "Contenuto" di un libro.
     *
     * @param libroId L'ID del libro.
     * @return La media del punteggio per il contenuto, o 0.0 se non ci sono valutazioni.
     * @throws RemoteException Se si verifica un errore di comunicazione o un'eccezione sul server.
     */
    double calcolaMediaContenuto(int libroId) throws RemoteException;
    
    /**
     * Calcola la media delle valutazioni per il criterio "Gradevolezza" di un libro.
     *
     * @param libroId L'ID del libro.
     * @return La media del punteggio per la gradevolezza, o 0.0 se non ci sono valutazioni.
     * @throws RemoteException Se si verifica un errore di comunicazione o un'eccezione sul server.
     */
    double calcolaMediaGradevolezza(int libroId) throws RemoteException;
    
    /**
     * Calcola la media delle valutazioni per il criterio "Originalità" di un libro.
     *
     * @param libroId L'ID del libro.
     * @return La media del punteggio per l'originalità, o 0.0 se non ci sono valutazioni.
     * @throws RemoteException Se si verifica un errore di comunicazione o un'eccezione sul server.
     */
    double calcolaMediaOriginalita(int libroId) throws RemoteException;
    
    /**
     * Calcola la media delle valutazioni per il criterio "Edizione" di un libro.
     *
     * @param libroId L'ID del libro.
     * @return La media del punteggio per l'edizione, o 0.0 se non ci sono valutazioni.
     * @throws RemoteException Se si verifica un errore di comunicazione o un'eccezione sul server.
     */
    double calcolaMediaEdizione(int libroId) throws RemoteException;

    /**
     * Recupera tutte le valutazioni effettuate da un utente, arricchite con dettagli
     * aggiuntivi (es. titolo e autore del libro) per una facile visualizzazione nell'interfaccia utente.
     *
     * @param userId L'ID dell'utente di cui recuperare le valutazioni.
     * @return Una lista di oggetti {@link ValutazioneDettagliata}. La lista è vuota se l'utente non ha valutazioni.
     * @throws RemoteException Se si verifica un errore di comunicazione o un'eccezione sul server.
     */
    List<ValutazioneDettagliata> listValutazioniDettagliateByUser(String userId) throws RemoteException;

    /**
     * Aggiorna una valutazione esistente, identificata dalla coppia utente-libro.
     *
     * @param userId L'ID dell'utente proprietario della valutazione.
     * @param libroId L'ID del libro a cui la valutazione si riferisce.
     * @param stile Nuovo punteggio (1-5) per il criterio "Stile".
     * @param stileNote Nuove note testuali per il criterio "Stile".
     * @param contenuto Nuovo punteggio (1-5) per il criterio "Contenuto".
     * @param contenutoNote Nuove note testuali per il criterio "Contenuto".
     * @param gradevolezza Nuovo punteggio (1-5) per il criterio "Gradevolezza".
     * @param gradevolezzaNote Nuove note testuali per il criterio "Gradevolezza".
     * @param originalita Nuovo punteggio (1-5) per il criterio "Originalità".
     * @param originalitaNote Nuove note testuali per il criterio "Originalità".
     * @param edizione Nuovo punteggio (1-5) per il criterio "Edizione".
     * @param edizioneNote Nuove note testuali per il criterio "Edizione".
     * @param votoFinale Il nuovo voto complessivo calcolato.
     * @param commentoFinale Il nuovo commento finale riassuntivo.
     * @return {@code true} se l'aggiornamento è andato a buon fine, {@code false} altrimenti.
     * @throws RemoteException Se si verifica un errore di comunicazione o un'eccezione sul server.
     */
    boolean aggiornaValutazione(
            String userId,
            int libroId,
            int stile, String stileNote,
            int contenuto, String contenutoNote,
            int gradevolezza, String gradevolezzaNote,
            int originalita, String originalitaNote,
            int edizione, String edizioneNote,
            double votoFinale, String commentoFinale
    ) throws RemoteException;

    /**
     * Elimina una valutazione esistente, identificata dalla coppia utente-libro.
     *
     * @param userId L'ID dell'utente proprietario della valutazione.
     * @param libroId L'ID del libro a cui la valutazione si riferisce.
     * @return {@code true} se l'eliminazione è andata a buon fine, {@code false} altrimenti.
     * @throws RemoteException Se si verifica un errore di comunicazione o un'eccezione sul server.
     */
    boolean eliminaValutazione(String userId, int libroId) throws RemoteException;
}
