package bookrecommender.condivisi.consigli;

import bookrecommender.condivisi.libri.Libro;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * Interfaccia remota per il servizio di gestione dei consigli sui libri,
 * progettata per essere utilizzata tramite RMI (Remote Method Invocation).
 * <p>
 * Definisce il contratto tra il client e il server per tutte le operazioni
 * relative alla creazione, lettura, aggiornamento e cancellazione (CRUD) dei consigli.
 * Ogni metodo può lanciare una {@link RemoteException} per segnalare problemi
 * di comunicazione o errori avvenuti sul server durante l'esecuzione della chiamata.
 *
 * @see java.rmi.Remote
 * @see bookrecommender.server.consigli.ConsigliServiceImpl
 */
public interface ConsigliService extends Remote {

    /**
     * Nome pubblico con cui questo servizio viene registrato e cercato nel registro RMI.
     * I client utilizzeranno questa costante per ottenere un riferimento all'oggetto remoto.
     */
    String NAME = "ConsigliService";

    /**
     * Aggiunge un nuovo consiglio nel sistema.
     * <p>
     * L'implementazione di questo metodo dovrebbe applicare regole di business, come:
     * <ul>
     *     <li>Impedire a un utente di consigliare un libro che ha già consigliato per lo stesso libro letto.</li>
     *     <li>Impedire a un utente di consigliare lo stesso libro che ha letto.</li>
     *     <li>Applicare un limite massimo di consigli che un utente può dare per un singolo libro letto.</li>
     * </ul>
     *
     * @param userId L'ID dell'utente che fornisce il consiglio.
     * @param libreriaId L'ID della libreria in cui si trova il libro letto.
     * @param libroLettoId L'ID del libro letto per cui si sta dando il consiglio.
     * @param libroConsigliatoId L'ID del libro che viene consigliato.
     * @param commento Un commento testuale opzionale associato al consiglio.
     * @throws RemoteException Se si verifica un errore di comunicazione o un'eccezione sul server.
     * @throws IllegalArgumentException Se i parametri forniti non sono validi (es. ID negativi, utente nullo).
     * @throws IllegalStateException Se una regola di business viene violata (es. limite massimo di consigli raggiunto).
     */
    void aggiungiConsiglio(String userId, int libreriaId, long libroLettoId, long libroConsigliatoId, String commento) throws RemoteException;

    /**
     * Recupera una lista di libri che sono stati consigliati da altri utenti
     * per un dato libro letto all'interno di una specifica libreria.
     *
     * @param libreriaId L'ID della libreria di contesto.
     * @param libroLettoId L'ID del libro per cui si cercano i consigli.
     * @return Una {@link List} di oggetti {@link Libro} consigliati. La lista può essere vuota se non ci sono consigli.
     * @throws RemoteException Se si verifica un errore di comunicazione o un'eccezione sul server.
     */
    List<Libro> getConsigliati(int libreriaId, long libroLettoId) throws RemoteException;

    /**
     * Recupera una lista di libri consigliati per un dato libro letto in una specifica libreria,
     * arricchita con il conteggio di quante volte ogni libro è stato consigliato.
     *
     * @param libreriaId L'ID della libreria di contesto.
     * @param libroLettoId L'ID del libro per cui si cercano i consigli.
     * @return Una {@link List} di {@link LibroConsigliato}, ordinata in modo decrescente per numero di consigli.
     *         La lista può essere vuota.
     * @throws RemoteException Se si verifica un errore di comunicazione o un'eccezione sul server.
     */
    List<LibroConsigliato> getConsigliatiConConteggio(int libreriaId, long libroLettoId) throws RemoteException;

    /**
     * Recupera una lista di libri consigliati per un dato libro letto, aggregando i dati
     * da tutte le librerie. Ogni libro consigliato è arricchito con il conteggio totale
     * di quante volte è stato suggerito.
     *
     * @param libroLettoId L'ID del libro per cui si cercano i consigli.
     * @return Una {@link List} di {@link LibroConsigliato}, ordinata in modo decrescente per numero di consigli.
     *         La lista può essere vuota.
     * @throws RemoteException Se si verifica un errore di comunicazione o un'eccezione sul server.
     */
    List<LibroConsigliato> getConsigliatiConConteggio(long libroLettoId) throws RemoteException;

    /**
     * Recupera tutti i consigli (in forma base) che un dato utente ha fornito.
     *
     * @param userId L'ID dell'utente di cui recuperare i consigli.
     * @return Una {@link List} di oggetti {@link Consiglio}. La lista può essere vuota.
     * @throws RemoteException Se si verifica un errore di comunicazione o un'eccezione sul server.
     */
    List<Consiglio> listConsigliByUser(String userId) throws RemoteException;

    /**
     * Aggiorna il commento di un consiglio esistente. Il consiglio da modificare
     * è identificato univocamente dalla sua chiave primaria composta.
     *
     * @param userId L'ID dell'utente che ha dato il consiglio.
     * @param libreriaId L'ID della libreria del consiglio.
     * @param libroLettoId L'ID del libro letto del consiglio.
     * @param libroConsigliatoId L'ID del libro consigliato.
     * @param commento Il nuovo testo del commento. Può essere vuoto o nullo per rimuovere il commento.
     * @throws RemoteException Se si verifica un errore di comunicazione o un'eccezione sul server.
     */
    void updateCommento(String userId, int libreriaId, long libroLettoId, long libroConsigliatoId, String commento) throws RemoteException;

    /**
     * Elimina un consiglio esistente dal sistema. Il consiglio da eliminare
     * è identificato univocamente dalla sua chiave primaria composta.
     *
     * @param userId L'ID dell'utente che ha dato il consiglio.
     * @param libreriaId L'ID della libreria del consiglio.
     * @param libroLettoId L'ID del libro letto del consiglio.
     * @param libroConsigliatoId L'ID del libro consigliato.
     * @throws RemoteException Se si verifica un errore di comunicazione o un'eccezione sul server.
     */
    void deleteConsiglio(String userId, int libreriaId, long libroLettoId, long libroConsigliatoId) throws RemoteException;

    /**
     * Recupera tutti i consigli che un dato utente ha fornito, arricchiti con
     * dettagli testuali (come titoli dei libri e nomi delle librerie) per una
     * facile visualizzazione nell'interfaccia utente.
     *
     * @param userId L'ID dell'utente di cui recuperare i consigli dettagliati.
     * @return Una {@link List} di oggetti {@link ConsiglioDettagliato}. La lista può essere vuota.
     * @throws RemoteException Se si verifica un errore di comunicazione o un'eccezione sul server.
     */
    List<ConsiglioDettagliato> listConsigliDettagliatiByUser(String userId) throws RemoteException;
}