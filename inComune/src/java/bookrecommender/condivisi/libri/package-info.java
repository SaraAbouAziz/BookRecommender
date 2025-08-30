/**
 * Fornisce le classi e le interfacce condivise per la gestione dei libri.
 * <p>
 * Questo package contiene le definizioni dei dati e dei servizi remoti relativi
 * al catalogo dei libri. È il punto di riferimento per la comunicazione tra client
 * e server quando si tratta di cercare o recuperare informazioni sui libri.
 * Le classi principali sono:
 * <ul>
 *     <li>{@link bookrecommender.condivisi.libri.CercaLibriService}: L'interfaccia RMI
 *         che definisce le operazioni di ricerca disponibili per i client.</li>
 *     <li>{@link bookrecommender.condivisi.libri.Libro}: Il Data Transfer Object (DTO),
 *         implementato come record, che rappresenta un libro e tutte le sue
 *         informazioni anagrafiche.</li>
 * </ul>
 * Le classi in questo package sono progettate per essere serializzabili e utilizzate
 * in un'architettura distribuita.
 */
package bookrecommender.condivisi.libri;
