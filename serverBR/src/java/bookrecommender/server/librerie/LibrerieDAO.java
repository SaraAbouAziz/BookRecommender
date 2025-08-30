package bookrecommender.server.librerie;

import bookrecommender.condivisi.librerie.Libreria;
import java.util.List;

/**
 * Interfaccia per l'accesso ai dati delle librerie nel database.
 * Definisce i metodi per le operazioni CRUD sulle librerie.
 */
public interface LibrerieDAO {
    
    /**
     * Crea una nuova libreria nel database
     * @param libreria libreria da creare
     * @return la libreria creata con l'ID assegnato
     */
    Libreria creaLibreria(Libreria libreria);
    
    /**
     * Ottiene una libreria dal database tramite ID
     * @param libreriaId ID della libreria
     * @return libreria se trovata, null altrimenti
     */
    Libreria getLibreriaById(int libreriaId);
    
    /**
     * Ottiene tutte le librerie di un utente
     * @param userId ID dell'utente
     * @return lista delle librerie dell'utente
     */
    List<Libreria> getLibrerieByUserId(String userId);
    
    /**
     * Ottiene una libreria specifica di un utente
     * @param userId ID dell'utente
     * @param nomeLibreria nome della libreria
     * @return libreria se trovata, null altrimenti
     */
    Libreria getLibreriaByUserIdAndNome(String userId, String nomeLibreria);
    
    /**
     * Aggiorna una libreria esistente
     * @param libreria libreria da aggiornare
     * @return true se l'aggiornamento è riuscito, false altrimenti
     */
    boolean aggiornaLibreria(Libreria libreria);
    
    /**
     * Elimina una libreria dal database
     * @param libreriaId ID della libreria da eliminare
     * @return true se l'eliminazione è riuscita, false altrimenti
     */
    boolean eliminaLibreria(int libreriaId);
    
    /**
     * Aggiunge un libro a una libreria
     * @param libreriaId ID della libreria
     * @param libroId ID del libro da aggiungere
     * @return true se l'aggiunta è riuscita, false altrimenti
     */
    boolean aggiungiLibroALibreria(int libreriaId, long libroId);
    
    /**
     * Rimuove un libro da una libreria
     * @param libreriaId ID della libreria
     * @param libroId ID del libro da rimuovere
     * @return true se la rimozione è riuscita, false altrimenti
     */
    boolean rimuoviLibroDaLibreria(int libreriaId, long libroId);
    
    /**
     * Verifica se un nome libreria è già esistente per un utente
     * @param userId ID dell'utente
     * @param nomeLibreria nome della libreria da verificare
     * @return true se il nome è già esistente, false altrimenti
     */
    boolean isNomeLibreriaEsistente(String userId, String nomeLibreria);
    
    /**
     * Verifica se un libro è presente in una libreria
     * @param libreriaId ID della libreria
     * @param libroId ID del libro da verificare
     * @return true se il libro è presente, false altrimenti
     */
    boolean isLibroInLibreria(int libreriaId, long libroId);
    
    /**
     * Ottiene tutti gli ID dei libri in una libreria
     * @param libreriaId ID della libreria
     * @return lista degli ID dei libri
     */
    List<Long> getLibriIdsInLibreria(int libreriaId);
}