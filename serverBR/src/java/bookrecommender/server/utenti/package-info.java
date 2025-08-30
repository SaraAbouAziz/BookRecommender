/**
 * Fornisce le implementazioni lato server per la gestione degli utenti.
 * <p>
 * Questo package contiene le classi che implementano la logica di business e
 * l'accesso ai dati per le funzionalità di autenticazione e registrazione degli utenti.
 * Segue un'architettura a due livelli (Service e DAO):
 * <ul>
 *     <li>{@link bookrecommender.server.utenti.UtentiServiceImpl}: L'implementazione
 *         concreta del servizio RMI {@link bookrecommender.condivisi.utenti.UtentiService}.
 *         Gestisce la logica di business, la validazione e coordina le operazioni.</li>
 *     <li>{@link bookrecommender.server.utenti.UtentiDAO}: L'interfaccia che definisce
 *         il contratto per l'accesso ai dati (Data Access Object).</li>
 *     <li>{@link bookrecommender.server.utenti.JdbcUtentiDAO}: L'implementazione
 *         concreta del DAO che utilizza JDBC per interagire con il database.</li>
 * </ul>
 * Il service layer è esposto ai client tramite RMI, mentre il DAO layer astrae la persistenza.
 */
package bookrecommender.server.utenti;