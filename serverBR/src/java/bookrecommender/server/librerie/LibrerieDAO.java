package bookrecommender.server.librerie;

import bookrecommender.condivisi.librerie.Libreria;
import java.util.List;

/**
 * Interfaccia che definisce il contratto per le operazioni di accesso ai dati (DAO)
 * relative alle librerie personali degli utenti.
 * <p>
 * Questa interfaccia astrae le operazioni di persistenza per l'entità {@link Libreria},
 * seguendo il pattern Data Access Object (DAO).
 *
 * @see JdbcLibrerieDAO
 * @see Libreria
 */
public interface LibrerieDAO {
    
    /**
     * Crea una nuova libreria nel database.
     * @param libreria L'oggetto {@link Libreria} da creare (l'ID sarà ignorato e generato dal DB).
     * @return La {@link Libreria} creata, completa dell'ID assegnato dal database, o {@code null} se la creazione fallisce.
     */
    Libreria creaLibreria(Libreria libreria);
    
    /**
     * Recupera una libreria dal database tramite il suo ID.
     * @param libreriaId L'ID della libreria da recuperare.
     * @return La {@link Libreria} corrispondente se trovata, altrimenti {@code null}.
     */
    Libreria getLibreriaById(int libreriaId);
    
    /**
     * Recupera tutte le librerie appartenenti a un utente specifico.
     * @param userId L'ID dell'utente.
     * @return Una lista di {@link Libreria} dell'utente. Restituisce una lista vuota se l'utente non ha librerie.
     */
    List<Libreria> getLibrerieByUserId(String userId);
    
    /**
     * Recupera una libreria specifica di un utente, identificata dal nome.
     * @param userId L'ID dell'utente.
     * @param nomeLibreria Il nome della libreria da cercare.
     * @return La {@link Libreria} corrispondente se trovata, altrimenti {@code null}.
     */
    Libreria getLibreriaByUserIdAndNome(String userId, String nomeLibreria);
    
    /**
     * Aggiorna i dati di una libreria esistente (es. il nome).
     * @param libreria L'oggetto {@link Libreria} con i dati aggiornati. L'ID viene usato per identificare la riga da aggiornare.
     * @return {@code true} se l'aggiornamento ha avuto successo, {@code false} altrimenti.
     */
    boolean aggiornaLibreria(Libreria libreria);
    
    /**
     * Elimina una libreria dal database tramite il suo ID.
     * @param libreriaId L'ID della libreria da eliminare.
     * @return {@code true} se l'eliminazione ha avuto successo, {@code false} altrimenti.
     */
    boolean eliminaLibreria(int libreriaId);
    
    /**
     * Associa un libro a una libreria nella tabella di giunzione.
     * @param libreriaId L'ID della libreria.
     * @param libroId L'ID del libro da aggiungere.
     * @return {@code true} se l'aggiunta ha avuto successo, {@code false} altrimenti.
     */
    boolean aggiungiLibroALibreria(int libreriaId, long libroId);
    
    /**
     * Rimuove l'associazione di un libro da una libreria.
     * @param libreriaId L'ID della libreria.
     * @param libroId L'ID del libro da rimuovere.
     * @return {@code true} se la rimozione ha avuto successo, {@code false} altrimenti.
     */
    boolean rimuoviLibroDaLibreria(int libreriaId, long libroId);
    
    /**
     * Verifica se un dato nome di libreria è già in uso da parte di un utente.
     * @param userId L'ID dell'utente.
     * @param nomeLibreria Il nome della libreria da verificare.
     * @return {@code true} se il nome è già esistente per quell'utente, {@code false} altrimenti.
     */
    boolean isNomeLibreriaEsistente(String userId, String nomeLibreria);
    
    /**
     * Verifica se un libro è già associato a una libreria.
     * @param libreriaId L'ID della libreria.
     * @param libroId L'ID del libro da verificare.
     * @return {@code true} se il libro è già presente nella libreria, {@code false} altrimenti.
     */
    boolean isLibroInLibreria(int libreriaId, long libroId);
    
    /**
     * Recupera la lista di tutti gli ID dei libri contenuti in una specifica libreria.
     * @param libreriaId L'ID della libreria.
     * @return Una lista di {@link Long} rappresentanti gli ID dei libri.
     */
    List<Long> getLibriIdsInLibreria(int libreriaId);
}