/**
 * Fornisce le implementazioni lato server per la ricerca e la gestione dei libri.
 * <p>
 * Questo package contiene le classi che implementano la logica di business e
 * l'accesso ai dati per le funzionalità di ricerca nel catalogo dei libri. Segue
 * un'architettura a due livelli (Service e DAO):
 * <ul>
 *     <li>{@link bookrecommender.server.libri.CercaLibriServiceImpl}: L'implementazione
 *         concreta del servizio RMI {@link bookrecommender.condivisi.libri.CercaLibriService}.
 *         Gestisce la logica di business e coordina le operazioni di ricerca.</li>
 *     <li>{@link bookrecommender.server.libri.LibroDAO}: L'interfaccia che definisce
 *         il contratto per l'accesso ai dati (Data Access Object).</li>
 *     <li>{@link bookrecommender.server.libri.JdbcCercaLibriDAO}: L'implementazione
 *         concreta del DAO che utilizza JDBC per interagire con il database.</li>
 * </ul>
 * Il service layer è esposto ai client tramite RMI, mentre il DAO layer astrae la persistenza.
 */
package bookrecommender.server.libri;