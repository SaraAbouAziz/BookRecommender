/**
 * Fornisce le implementazioni lato server per la gestione delle valutazioni dei libri.
 * <p>
 * Questo package contiene le classi che implementano la logica di business e
 * l'accesso ai dati per le funzionalità di creazione, recupero e aggregazione
 * delle valutazioni. Segue un'architettura a due livelli (Service e DAO):
 * <ul>
 *     <li>{@link bookrecommender.server.valutazioni.ValutazioneServiceImpl}: L'implementazione
 *         concreta del servizio RMI {@link bookrecommender.condivisi.valutazioni.ValutazioneService}.
 *         Gestisce la logica di business, la validazione e coordina le operazioni.</li>
 *     <li>{@link bookrecommender.server.valutazioni.ValutazioniDAO}: L'interfaccia che definisce
 *         il contratto per l'accesso ai dati (Data Access Object).</li>
 *     <li>{@link bookrecommender.server.valutazioni.JdbcValutazioniDAO}: L'implementazione
 *         concreta del DAO che utilizza JDBC per interagire con il database.</li>
 * </ul>
 * Il service layer è esposto ai client tramite RMI, mentre il DAO layer astrae la persistenza.
 */
package bookrecommender.server.valutazioni;