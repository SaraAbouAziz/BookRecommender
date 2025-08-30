/**
 * Fornisce classi di utilità e componenti trasversali per l'applicazione server.
 * <p>
 * Questo package contiene classi di supporto che non appartengono a un dominio
 * di business specifico ma sono utilizzate da vari componenti del server.
 * Le responsabilità principali includono:
 * <ul>
 *     <li>{@link bookrecommender.server.utili.DBConnectionSingleton}: Un singleton per
 *         la gestione centralizzata della connessione al database.</li>
 *     <li>{@link bookrecommender.server.utili.DBUtil}: Una classe di utilità per
 *         la gestione e la stampa dettagliata delle {@code SQLException}.</li>
 * </ul>
 */
package bookrecommender.server.utili;