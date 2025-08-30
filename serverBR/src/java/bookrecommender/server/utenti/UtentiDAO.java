package bookrecommender.server.utenti;

import bookrecommender.condivisi.utenti.Utenti;

/**
 * Interfaccia che definisce il contratto per le operazioni di accesso ai dati (DAO)
 * relative agli utenti.
 * <p>
 * Questa interfaccia astrae le operazioni di persistenza per l'entità {@link Utenti},
 * seguendo il pattern Data Access Object (DAO). In questo modo, il resto dell'applicazione
 * può interagire con i dati degli utenti senza conoscere i dettagli implementativi
 * dello strato di persistenza (es. JDBC, JPA, etc.).
 * @author Abou Aziz Sara Hesham Abdel Hamid 757004
 * @author Ben Mahjoub Ali 759560
 * @author Hidri Mohamed Taha 756235
 * @author Zoghbani Lilia 759652
 * @see Utenti
 * @see JdbcUtentiDAO
 * @version 1.0
 */
public interface UtentiDAO {
    
    /**
     * Salva un nuovo utente nel database.
     * Questo metodo è responsabile dell'inserimento di un nuovo record utente
     * nella tabella di persistenza.
     *
     * @param utente L'oggetto {@link Utenti} contenente i dati dell'utente da salvare.
     *               L'oggetto dovrebbe contenere tutti i campi necessari per la creazione di un nuovo utente.
     * @return {@code true} se il salvataggio dell'utente è avvenuto con successo,
     *         {@code false} altrimenti (es. in caso di errore di connessione al DB,
     *         violazione di vincoli di integrità, o utente già esistente se la logica lo prevede).
     */
    boolean save(Utenti utente);
    
    /**
     * Trova un utente nel database tramite il suo username.
     * Questo metodo recupera un singolo record utente basandosi sull'identificativo
     * univoco dell'utente (username).
     *
     * @param username Lo username dell'utente da cercare. Questo campo è utilizzato
     *                 come criterio di ricerca principale.
     * @return L'oggetto {@link Utenti} corrispondente allo username specificato se trovato;
     *         {@code null} se nessun utente con lo username fornito è presente nel database.
     */
    Utenti findByUsername(String username);
    
    /**
     * Aggiorna i dati di un utente esistente nel database.
     * Questo metodo è utilizzato per modificare i campi di un utente già presente,
     * identificato tipicamente dal suo ID o username.
     *
     * @param utente L'oggetto {@link Utenti} contenente i dati aggiornati dell'utente.
     *               L'oggetto deve includere l'identificativo dell'utente (es. userID)
     *               per permettere l'individuazione del record da aggiornare.
     * @return {@code true} se l'aggiornamento dell'utente è avvenuto con successo (almeno una riga modificata),
     *         {@code false} altrimenti (es. utente non trovato, errore di connessione al DB, ecc.).
     */
    boolean update(Utenti utente);
    
    /**
     * Elimina un utente dal database tramite il suo username.
     * Questo metodo rimuove un record utente dalla tabella di persistenza.
     *
     * @param username Lo username dell'utente da eliminare. Questo campo è utilizzato
     *                 per identificare univocamente l'utente da rimuovere.
     * @return {@code true} se l'eliminazione dell'utente è avvenuta con successo (almeno una riga eliminata),
     *         {@code false} altrimenti (es. utente non trovato, errore di connessione al DB, ecc.).
     */
    boolean delete(String username);
}
