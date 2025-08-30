package bookrecommender.server.valutazioni;

import bookrecommender.condivisi.valutazioni.ValutazioneDettagliata;
import java.util.List;
import java.util.Map;

/**
 * Interfaccia DAO (Data Access Object) per la gestione della persistenza dei dati delle valutazioni.
 * <p>
 * Definisce il contratto per tutte le operazioni CRUD (Create, Read, Update, Delete)
 * e le query relative alle valutazioni dei libri. Le implementazioni di questa interfaccia
 * (es. {@link JdbcValutazioniDAO}) si occupano della logica specifica di interazione
 * con il sistema di persistenza (es. un database SQL).
 *
 * @see JdbcValutazioniDAO
 * @see ValutazioneDettagliata
 */
public interface ValutazioniDAO {
    /**
     * Verifica se un utente ha già valutato un determinato libro.
     *
     * @param libroId L'ID del libro da controllare.
     * @param userId  L'ID dell'utente da controllare.
     * @return {@code true} se esiste già una valutazione per la coppia utente-libro, {@code false} altrimenti.
     */
    boolean isLibroGiaValutato(int libroId, String userId);

    /**
     * Salva una nuova valutazione nel sistema di persistenza.
     *
     * @param userId             L'ID dell'utente che effettua la valutazione.
     * @param libroId            L'ID del libro valutato.
     * @param nomeLibreria       Il nome della libreria da cui proviene la valutazione.
     * @param stile              Punteggio per lo stile (da 1 a 5).
     * @param stileNote          Note testuali relative allo stile.
     * @param contenuto          Punteggio per il contenuto (da 1 a 5).
     * @param contenutoNote      Note testuali relative al contenuto.
     * @param gradevolezza       Punteggio per la gradevolezza (da 1 a 5).
     * @param gradevolezzaNote   Note testuali relative alla gradevolezza.
     * @param originalita        Punteggio per l'originalità (da 1 a 5).
     * @param originalitaNote    Note testuali relative all'originalità.
     * @param edizione           Punteggio per la qualità dell'edizione (da 1 a 5).
     * @param edizioneNote       Note testuali relative alla qualità dell'edizione.
     * @param votoFinale         Il voto complessivo calcolato.
     * @param commentoFinale     Un commento finale sulla valutazione.
     * @return {@code true} se il salvataggio è andato a buon fine, {@code false} altrimenti.
     */
    boolean salvaValutazione(String userId, int libroId, String nomeLibreria,
                             int stile, String stileNote,
                             int contenuto, String contenutoNote,
                             int gradevolezza, String gradevolezzaNote,
                             int originalita, String originalitaNote,
                             int edizione, String edizioneNote,
                             double votoFinale, String commentoFinale);

    /**
     * Carica tutte le valutazioni associate a un libro specifico.
     * <p>
     * Nota: questo metodo restituisce una lista di mappe generiche. Per una rappresentazione
     * più strutturata, considerare l'uso di un oggetto DTO (Data Transfer Object) specifico.
     *
     * @param libroId L'ID del libro di cui caricare le valutazioni.
     * @return Una lista di {@link Map}, dove ogni mappa rappresenta una valutazione.
     *         Le chiavi della mappa corrispondono ai campi della valutazione (es. "userId", "stile", "votoFinale").
     *         Restituisce una lista vuota se non ci sono valutazioni.
     */
    List<Map<String, Object>> caricaValutazioniLibro(int libroId);

    /**
     * Calcola la media dei voti complessivi per un libro specifico.
     *
     * @param libroId L'ID del libro.
     * @return La media dei voti, o 0.0 se non ci sono valutazioni.
     */
    double calcolaMediaValutazioni(int libroId);

    /**
     * Conta il numero totale di valutazioni ricevute da un libro.
     *
     * @param libroId L'ID del libro.
     * @return Il numero di valutazioni, o 0 se non ce ne sono.
     */
    int getNumeroValutazioni(int libroId);

    /**
     * Calcola la media dei punteggi per la categoria "stile" per un libro specifico.
     *
     * @param libroId L'ID del libro.
     * @return La media dei punteggi di stile, o 0.0 se non ci sono valutazioni.
     */
    double calcolaMediaStile(int libroId);

    /**
     * Calcola la media dei punteggi per la categoria "contenuto" per un libro specifico.
     *
     * @param libroId L'ID del libro.
     * @return La media dei punteggi di contenuto, o 0.0 se non ci sono valutazioni.
     */
    double calcolaMediaContenuto(int libroId);

    /**
     * Calcola la media dei punteggi per la categoria "gradevolezza" per un libro specifico.
     *
     * @param libroId L'ID del libro.
     * @return La media dei punteggi di gradevolezza, o 0.0 se non ci sono valutazioni.
     */
    double calcolaMediaGradevolezza(int libroId);

    /**
     * Calcola la media dei punteggi per la categoria "originalità" per un libro specifico.
     *
     * @param libroId L'ID del libro.
     * @return La media dei punteggi di originalità, o 0.0 se non ci sono valutazioni.
     */
    double calcolaMediaOriginalita(int libroId);

    /**
     * Calcola la media dei punteggi per la categoria "edizione" per un libro specifico.
     *
     * @param libroId L'ID del libro.
     * @return La media dei punteggi di edizione, o 0.0 se non ci sono valutazioni.
     */
    double calcolaMediaEdizione(int libroId);

    /**
     * Trova tutte le valutazioni effettuate da un utente specifico, arricchite con dettagli
     * leggibili come il titolo del libro e il nome della libreria.
     *
     * @param userId L'ID dell'utente di cui recuperare le valutazioni.
     * @return Una lista di oggetti {@link ValutazioneDettagliata}. Restituisce una lista vuota se l'utente non ha valutazioni.
     */
    List<ValutazioneDettagliata> findValutazioniDettagliateByUser(String userId);

    /**
     * Aggiorna una valutazione esistente identificata dalla coppia utente-libro.
     *
     * @param userId         L'ID dell'utente che ha effettuato la valutazione.
     * @param libroId        L'ID del libro la cui valutazione deve essere aggiornata.
     * @param stile          Nuovo punteggio per lo stile.
     * @param stileNote      Nuove note per lo stile.
     * @param contenuto      Nuovo punteggio per il contenuto.
     * @param contenutoNote  Nuove note per il contenuto.
     * @param gradevolezza   Nuovo punteggio per la gradevolezza.
     * @param gradevolezzaNote Nuove note per la gradevolezza.
     * @param originalita    Nuovo punteggio per l'originalità.
     * @param originalitaNote Nuove note per l'originalità.
     * @param edizione       Nuovo punteggio per la qualità dell'edizione.
     * @param edizioneNote   Nuove note per la qualità dell'edizione.
     * @param votoFinale     Nuovo voto complessivo.
     * @param commentoFinale Nuovo commento finale.
     * @return {@code true} se l'aggiornamento ha avuto successo (almeno una riga modificata), {@code false} altrimenti.
     */
    boolean aggiornaValutazione(String userId, int libroId, int stile, String stileNote,
                               int contenuto, String contenutoNote, int gradevolezza, String gradevolezzaNote,
                               int originalita, String originalitaNote, int edizione, String edizioneNote,
                               double votoFinale, String commentoFinale);

    /**
     * Elimina una valutazione esistente, identificata dalla coppia utente-libro.
     *
     * @param userId  L'ID dell'utente che ha effettuato la valutazione.
     * @param libroId L'ID del libro la cui valutazione deve essere eliminata.
     * @return {@code true} se l'eliminazione ha avuto successo (almeno una riga eliminata), {@code false} altrimenti.
     */
    boolean eliminaValutazione(String userId, int libroId);
}
