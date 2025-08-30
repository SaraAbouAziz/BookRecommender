package bookrecommender.server.libri;

import bookrecommender.condivisi.libri.Libro;
import java.util.List;

/**
 * Interfaccia che definisce il contratto per le operazioni di accesso ai dati (DAO)
 * relative ai libri.
 * <p>
 * Questa interfaccia astrae le operazioni di persistenza per l'entità {@link Libro},
 * seguendo il pattern Data Access Object (DAO). Fornisce metodi per la creazione,
 * il recupero e la ricerca di libri, nascondendo i dettagli implementativi
 * dello strato di persistenza.
 *
 * @see Libro
 * @see JdbcCercaLibriDAO
 */
public interface LibroDAO {
    
    /**
     * Crea un nuovo libro nel database con i dettagli forniti.
     *
     * @param titolo      il titolo del libro.
     * @param autore      l'autore del libro.
     * @param descrizione la descrizione o trama del libro.
     * @param categoria   la categoria o genere del libro.
     * @param year        l'anno di pubblicazione del libro.
     * @param price       il prezzo del libro.
     * @return l'oggetto {@link Libro} appena creato, comprensivo dell'ID generato dal database,
     *         o {@code null} se la creazione non è riuscita.
     */
    Libro creaLibro(String titolo, String autore, String descrizione, String categoria, String year, String price);

    /**
     * Recupera un libro dal database tramite il suo identificativo univoco (ID).
     *
     * @param id l'ID univoco del libro da recuperare.
     * @return l'oggetto {@link Libro} corrispondente all'ID,
     *         o {@code null} se non viene trovato alcun libro con tale ID.
     */
    Libro getLibroById(int id);
    
    /**
     * Cerca libri nel database il cui titolo contiene la stringa fornita (match parziale).
     * La ricerca è case-insensitive.
     *
     * @param titolo la stringa da cercare all'interno del titolo dei libri.
     * @return una {@link List} di oggetti {@link Libro} che corrispondono al criterio.
     *         Se nessun libro corrisponde, restituisce una lista vuota.
     */
    List<Libro> cercaLibriPerTitolo(String titolo);
    
    /**
     * Cerca libri nel database il cui nome dell'autore contiene la stringa fornita (match parziale).
     * La ricerca è case-insensitive.
     *
     * @param autore la stringa da cercare all'interno del nome dell'autore.
     * @return una {@link List} di oggetti {@link Libro} che corrispondono al criterio.
     *         Se nessun libro corrisponde, restituisce una lista vuota.
     */
    List<Libro> cercaLibriPerAutore(String autore);
    
    /**
     * Cerca libri nel database combinando autore (match parziale, case-insensitive) e anno di pubblicazione (match esatto).
     *
     * @param autore la stringa da cercare all'interno del nome dell'autore.
     * @param anno   l'anno di pubblicazione esatto da cercare.
     * @return una {@link List} di oggetti {@link Libro} che corrispondono ai criteri.
     *         Se nessun libro corrisponde, restituisce una lista vuota.
     */
    List<Libro> cercaLibriPerAutoreEAnno(String autore, String anno);

    /**
     * Cerca un libro nel database tramite il suo identificativo univoco (ID).
     *
     * @param id l'ID univoco del libro da recuperare.
     * @return una {@link List} di oggetti {@link Libro} che corrispondono al criterio (dovrebbe essere al massimo uno).
     *         Se nessun libro corrisponde, restituisce una lista vuota.
     */
    List<Libro> cercaLibroPerId(Long id);
   
}
