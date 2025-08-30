package bookrecommender.utenti;

import bookrecommender.utili.TokenManager;

/**
 * Gestisce lo stato della sessione utente all'interno dell'applicazione.
 * <p>
 * Questa classe è implementata come un <b>Singleton</b> per garantire che esista
 * una sola istanza di gestione della sessione per tutta l'applicazione.
 * È il punto di riferimento centrale per conoscere lo stato di autenticazione
 * e i dettagli dell'utente attualmente loggato.
 * <p>
 * Le sue responsabilità principali sono:
 * <ul>
 *     <li>Mantenere in memoria l'ID e il nome dell'utente loggato.</li>
 *     <li>Gestire il ciclo di vita della sessione attraverso i metodi {@link #login(String, String)} e {@link #clearSession()}.</li>
 *     <li>Interagire con {@link TokenManager} per rendere la sessione persistente tra un avvio e l'altro dell'applicazione.</li>
 *     <li>Fornire un meccanismo per ripristinare una sessione da un token salvato.</li>
 * </ul>
 *
 * @author Abou Aziz Sara Hesham Abdel Hamid 757004
 * @author Ben Mahjoub Ali 759560
 * @author Hidri Mohamed Taha 756235
 * @author Zoghbani Lilia 759652
 * @see TokenManager
 * @version 1.0
 */
public class GestoreSessione {
    /** L'unica istanza della classe (Singleton). */
    private static GestoreSessione instance;
    /** L'ID dell'utente attualmente loggato. */
    private String currentUserId;
    /** Lo username dell'utente attualmente loggato. */
    private String currentUserName;
    /** Flag che indica se l'utente è autenticato. */
    private boolean isAuthenticated = false;

    /**
     * Costruttore privato per forzare l'uso del pattern Singleton.
     */
    private GestoreSessione() {}

    /**
     * Restituisce l'unica istanza della classe {@code GestoreSessione}.
     * Se l'istanza non esiste, viene creata.
     *
     * @return l'istanza Singleton di {@code GestoreSessione}.
     */
    public static GestoreSessione getInstance() {
        if (instance == null) {
            instance = new GestoreSessione();
        }
        return instance;
    }

    /**
     * Restituisce l'ID dell'utente attualmente loggato.
     *
     * @return l'ID dell'utente, o {@code null} se nessun utente è loggato.
     */
    public String getCurrentUserId() {
        return currentUserId;
    }

    /**
     * Imposta l'ID dell'utente corrente.
     * @param userId l'ID dell'utente.
     */
    public void setCurrentUserId(String userId) {
        this.currentUserId = userId;
    }

    /**
     * Restituisce lo username dell'utente attualmente loggato.
     *
     * @return lo username dell'utente, o {@code null} se nessun utente è loggato.
     */
    public String getLoggedInUser() {
        return currentUserName;
    }

    /**
     * Imposta lo username dell'utente corrente e lo marca come autenticato.
     * @param userName lo username dell'utente.
     */
    public void setLoggedInUser(String userName) {
        this.currentUserName = userName;
        this.isAuthenticated = true;
    }

    /**
     * Controlla se un utente è attualmente autenticato.
     *
     * @return {@code true} se un utente è loggato, {@code false} altrimenti.
     */
    public boolean isLoggedIn() {
        return isAuthenticated;
    }

    /**
     * Imposta lo stato di autenticazione della sessione.
     * @param authenticated il nuovo stato di autenticazione.
     */
    public void setAuthenticated(boolean authenticated) {
        this.isAuthenticated = authenticated;
    }

    /**
     * Termina la sessione corrente.
     * Azzera i dati dell'utente in memoria e comanda a {@link TokenManager}
     * di eliminare il token di sessione persistente.
     */
    public void clearSession() {
        currentUserId = null;
        currentUserName = null;
        isAuthenticated = false;
        TokenManager.deleteToken();
    }

    /**
     * Inizia una nuova sessione per un utente.
     * Imposta i dati dell'utente in memoria e comanda a {@link TokenManager}
     * di generare un token per rendere la sessione persistente.
     *
     * @param userId l'ID univoco dell'utente.
     * @param userName lo username dell'utente.
     */
    public void login(String userId, String userName) {
        currentUserId = userId;
        currentUserName = userName;
        isAuthenticated = true;
        TokenManager.generateToken(userId, userName);
    }

    /**
     * Controlla se esiste un token di sessione valido e persistente.
     * Delega il controllo a {@link TokenManager}.
     *
     * @return {@code true} se un token valido esiste, {@code false} altrimenti.
     */
    public boolean hasValidSession() {
        return TokenManager.hasValidToken();
    }

    /**
     * Tenta di caricare una sessione dal token persistente.
     * Se un token valido esiste, legge i dati dell'utente tramite {@link TokenManager}
     * e popola lo stato della sessione in memoria.
     *
     * @return {@code true} se la sessione è stata caricata con successo, {@code false} altrimenti.
     */
    public boolean loadSessionFromToken() {
        if (TokenManager.hasValidToken()) {
            currentUserId = TokenManager.getCurrentUserId();
            currentUserName = TokenManager.getCurrentUsername();
            isAuthenticated = true;
            return true;
        }
        return false;
    }
}
