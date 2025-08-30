/**
 * Fornisce le classi e le interfacce condivise per la gestione degli utenti.
 * <p>
 * Questo package contiene le definizioni dei dati e dei servizi remoti che
 * costituiscono il contratto tra client e server per le funzionalità di
 * autenticazione, registrazione e gestione degli account utente.
 * Le classi principali sono:
 * <ul>
 *     <li>{@link bookrecommender.condivisi.utenti.UtentiService}: L'interfaccia RMI che
 *         definisce i metodi remoti per l'autenticazione e la registrazione.</li>
 *     <li>{@link bookrecommender.condivisi.utenti.Utenti}: Il Data Transfer Object (DTO),
 *         implementato come record, che incapsula i dati anagrafici e le credenziali
 *         di un utente.</li>
 * </ul>
 * Le classi sono serializzabili per essere trasferite tramite RMI.
 */
package bookrecommender.condivisi.utenti;