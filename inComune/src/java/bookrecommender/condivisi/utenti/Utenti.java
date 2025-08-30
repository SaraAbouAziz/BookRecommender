package bookrecommender.condivisi.utenti;

import java.io.Serial;
import java.io.Serializable;

/**
 * Rappresenta un utente del sistema.
 * <p>
 * Questo record è un Data Transfer Object (DTO) che incapsula tutte le
 * informazioni relative a un utente. Viene utilizzato per trasferire i dati
 * tra il client e il server, ad esempio durante la registrazione di un nuovo
 * utente o per recuperare i dettagli di un utente esistente.
 * <p>
 * Essendo immutabile, garantisce che i dati dell'utente non vengano modificati
 * accidentalmente dopo la creazione dell'oggetto.
 *
 * @author Abou Aziz Sara Hesham Abdel Hamid 757004
 * @author Ben Mahjoub Ali 759560
 * @author Hidri Mohamed Taha 756235
 * @author Zoghbani Lilia 759652
 * @param nome Il nome  dell'utente.
 * @param cognome Il cognome dell'utente.
 * @param codFiscale Il codice fiscale dell'utente (deve essere univoco).
 * @param email L'indirizzo email dell'utente (deve essere univoco).
 * @param userID Lo username scelto dall'utente per l'accesso (deve essere univoco).
 * @param password La password scelta dall'utente 
 * @version 1.0
 */
public record Utenti(String nome, String cognome, String codFiscale, String email, String userID, String password) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
}
