package bookrecommender.server.consigli;

import bookrecommender.condivisi.consigli.Consiglio;
import bookrecommender.condivisi.consigli.LibroConsigliato;
import bookrecommender.condivisi.libri.Libro;
import java.util.List;

/**
 * Interfaccia che definisce il contratto per le operazioni di accesso ai dati (DAO)
 * relative ai consigli sui libri.
 * <p>
 * Questa interfaccia astrae le operazioni di persistenza per l'entità {@link Consiglio},
 * seguendo il pattern Data Access Object (DAO). Le implementazioni di questa interfaccia
 * si occuperanno della comunicazione con lo strato di persistenza (es. un database tramite JDBC).
 * @author Abou Aziz Sara Hesham Abdel Hamid 757004
 * @author Ben Mahjoub Ali 759560
 * @author Hidri Mohamed Taha 756235
 * @author Zoghbani Lilia 759652
 * @see JdbcConsigliDAO
 * @see Consiglio
 * @version 1.0
 */
public interface ConsigliDAO {

    /**
     * Aggiunge un nuovo consiglio nel database.
     *
     * @param userId L'ID dell'utente che fornisce il consiglio.
     * @param libreriaId L'ID della libreria in cui si trova il libro letto.
     * @param libroLettoId L'ID del libro letto per cui si sta dando il consiglio.
     * @param libroConsigliatoId L'ID del libro che viene consigliato.
     * @param commento Un commento testuale opzionale associato al consiglio.
     */
    void add(String userId, int libreriaId, long libroLettoId, long libroConsigliatoId, String commento);

    /**
     * Recupera una lista di libri che sono stati consigliati da altri utenti
     * per un dato libro letto all'interno di una specifica libreria.
     *
     * @param libreriaId L'ID della libreria di contesto.
     * @param libroLettoId L'ID del libro per cui si cercano i consigli.
     * @return Una {@link List} di oggetti {@link Libro} consigliati. La lista può essere vuota se non ci sono consigli.
     */
    List<Libro> findConsigliati(int libreriaId, long libroLettoId);

    /**
     * Conta il numero totale di consigli dati da un utente per un libro specifico,
     * indipendentemente dalla libreria.
     *
     * @param userId L'ID dell'utente che ha dato i consigli.
     * @param libroLettoId L'ID del libro letto per cui contare i consigli.
     * @return Il numero di consigli dati dall'utente per quel libro.
     */
    int countConsigliDati(String userId,  long libroLettoId);

    /**
     * Recupera una lista di libri consigliati per un dato libro letto in una specifica libreria,
     * arricchita con il conteggio di quante volte ogni libro è stato consigliato.
     *
     * @param libreriaId L'ID della libreria di contesto.
     * @param libroLettoId L'ID del libro per cui si cercano i consigli.
     * @return Una {@link List} di {@link LibroConsigliato}, ordinata in modo decrescente per numero di consigli.
     */
    List<LibroConsigliato> findConsigliatiConConteggio(int libreriaId, long libroLettoId);

    /**
     * Recupera una lista di libri consigliati per un dato libro letto, aggregando i dati
     * da tutte le librerie. Ogni libro consigliato è arricchito con il conteggio totale
     * di quante volte è stato suggerito.
     *
     * @param libroLettoId L'ID del libro per cui si cercano i consigli.
     * @return Una {@link List} di {@link LibroConsigliato}, ordinata in modo decrescente per numero di consigli.
     */
    List<LibroConsigliato> findConsigliatiConConteggio(long libroLettoId);

    /**
     * Recupera tutti i consigli (in forma base) che un dato utente ha fornito.
     *
     * @param userId L'ID dell'utente di cui recuperare i consigli.
     * @return Una {@link List} di oggetti {@link Consiglio}. La lista può essere vuota.
     */
    List<Consiglio> findByUser(String userId);

    /**
     * Aggiorna il commento di un consiglio esistente. Il consiglio da modificare
     * è identificato univocamente dalla sua chiave primaria composta.
     *
     * @param userId L'ID dell'utente che ha dato il consiglio.
     * @param libreriaId L'ID della libreria del consiglio.
     * @param libroLettoId L'ID del libro letto del consiglio.
     * @param libroConsigliatoId L'ID del libro consigliato.
     * @param commento Il nuovo testo del commento. Può essere vuoto o nullo per rimuovere il commento.
     */
    void updateCommento(String userId, int libreriaId, long libroLettoId, long libroConsigliatoId, String commento);

    /**
     * Elimina un consiglio esistente dal sistema. Il consiglio da eliminare
     * è identificato univocamente dalla sua chiave primaria composta.
     *
     * @param userId L'ID dell'utente che ha dato il consiglio.
     * @param libreriaId L'ID della libreria del consiglio.
     * @param libroLettoId L'ID del libro letto del consiglio.
     * @param libroConsigliatoId L'ID del libro consigliato.
     */
    void delete(String userId, int libreriaId, long libroLettoId, long libroConsigliatoId);

    /**
     * Recupera tutti i consigli che un dato utente ha fornito, arricchiti con
     * dettagli testuali (come titoli dei libri e nomi delle librerie) per una
     * facile visualizzazione nell'interfaccia utente.
     *
     * @param userId L'ID dell'utente di cui recuperare i consigli dettagliati.
     * @return Una {@link List} di oggetti {@link bookrecommender.condivisi.consigli.ConsiglioDettagliato}. La lista può essere vuota.
     */
    List<bookrecommender.condivisi.consigli.ConsiglioDettagliato> findDettagliatiByUser(String userId);
}