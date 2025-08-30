
/**
 * Fornisce le implementazioni lato server per la gestione dei consigli sui libri.
 * <p>
 * Questo package contiene le classi che implementano la logica di business e
 * l'accesso ai dati per le funzionalità di raccomandazione. Segue un'architettura
 * a due livelli (Service e DAO):
 * <ul>
 *     <li>{@link bookrecommender.server.consigli.ConsigliServiceImpl}: L'implementazione
 *         concreta del servizio RMI {@link bookrecommender.condivisi.consigli.ConsigliService}.
 *         Gestisce la logica di business, la validazione e coordina le operazioni.</li>
 *     <li>{@link bookrecommender.server.consigli.ConsigliDAO}: L'interfaccia che definisce
 *         il contratto per l'accesso ai dati (Data Access Object).</li>
 *     <li>{@link bookrecommender.server.consigli.JdbcConsigliDAO}: L'implementazione
 *         concreta del DAO che utilizza JDBC per interagire con il database.</li>
 * </ul>
 * Il service layer è esposto ai client tramite RMI, mentre il DAO layer astrae la persistenza.
 */
package bookrecommender.server.consigli;
