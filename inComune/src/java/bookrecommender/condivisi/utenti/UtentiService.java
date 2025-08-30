package bookrecommender.condivisi.utenti;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interfaccia del servizio RMI per la gestione degli utenti.
 * <p>
 * Definisce le operazioni remote che il client può invocare sul server per
 * gestire le funzionalità legate agli utenti, come l'autenticazione, la
 * registrazione e il recupero di informazioni.
 * <p>
 * Questa interfaccia è il contratto tra il client e il server per tutto ciò
 * che riguarda la gestione degli account utente.
 *
 * @see bookrecommender.server.utenti.UtentiServiceImpl
 * @see Utenti
 */
public interface UtentiService extends Remote {
    
    /**
     * Autentica un utente in base a un identificatore e una password.
     * <p>
     * L'identificatore può essere lo username ({@code userID}), l'indirizzo email
     * o il codice fiscale dell'utente. Il metodo verifica la corrispondenza
     * dell'identificatore e della password con i dati presenti nel database.
     *
     * @param username L'identificatore dell'utente (username, email o codice fiscale).
     * @param password La password in chiaro dell'utente.
     * @return {@code true} se le credenziali sono corrette e l'utente viene
     *         autenticato con successo, {@code false} altrimenti (utente non trovato
     *         o password errata).
     * @throws RemoteException se si verifica un errore di comunicazione RMI durante l'operazione.
     */
    boolean authenticateUser(String username, String password) throws RemoteException;
    
    /**
     * Registra un nuovo utente nel sistema.
     * <p>
     * Il metodo tenta di salvare i dati del nuovo utente nel database.
     * La registrazione fallisce se lo username ({@code userID}), l'email o il
     * codice fiscale forniti nell'oggetto {@link Utenti} sono già stati
     * registrati da un altro utente.
     *
     * @param utente L'oggetto {@link Utenti} contenente tutti i dati del nuovo
     *               utente da registrare.
     * @return {@code true} se la registrazione viene completata con successo,
     *         {@code false} se l'utente esiste già (in base a username, email o
     *         codice fiscale) o se i dati forniti non sono validi.
     * @throws RemoteException se si verifica un errore di comunicazione RMI durante l'operazione.
     */
    boolean registerUser(Utenti utente) throws RemoteException;
    
    /**
     * Verifica se un utente con un dato identificatore (username, email o codice fiscale)
     * è già presente nel sistema.
     * <p>
     * Questo metodo è utile per la validazione in tempo reale, ad esempio per
     * controllare se uno username è disponibile durante la registrazione.
     *
     * @param username L'identificatore (username, email o codice fiscale) da verificare.
     * @return {@code true} se un utente con l'identificatore specificato esiste già,
     *         {@code false} altrimenti.
     * @throws RemoteException se si verifica un errore di comunicazione RMI durante l'operazione.
     */
    boolean isUsernameExists(String username) throws RemoteException;
    
    /**
     * Recupera i dati completi di un utente dal sistema tramite il suo identificatore.
     * <p>
     * L'identificatore può essere lo username ({@code userID}), l'indirizzo email
     * o il codice fiscale. Questo metodo è tipicamente chiamato dopo un'autenticazione
     * andata a buon fine per ottenere tutti i dettagli dell'utente e avviare la sessione.
     *
     * @param username L'identificatore (username, email o codice fiscale) dell'utente da recuperare.
     * @return Un oggetto {@link Utenti} contenente i dati dell'utente se trovato,
     *         altrimenti {@code null}.
     * @throws RemoteException se si verifica un errore di comunicazione RMI durante l'operazione.
     */
    Utenti getUserByUsername(String username) throws RemoteException;
}
