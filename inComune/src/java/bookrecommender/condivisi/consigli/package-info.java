/**
 * Fornisce le classi e le interfacce condivise per la gestione dei consigli sui libri.
 * <p>
 * Questo package contiene le definizioni dei dati (DTO) e dei servizi remoti (RMI)
 * relativi alla funzionalità di raccomandazione dei libri. Le classi qui definite
 * sono utilizzate per la comunicazione tra il client e il server.
 * Le classi principali includono:
 * <ul>
 *     <li>{@link bookrecommender.condivisi.consigli.ConsigliService}: L'interfaccia RMI
 *         che definisce le operazioni per aggiungere, recuperare, aggiornare ed eliminare
 *         i consigli.</li>
 *     <li>{@link bookrecommender.condivisi.consigli.Consiglio}: Il DTO che rappresenta
 *         un singolo consiglio dato da un utente.</li>
 *     <li>{@link bookrecommender.condivisi.consigli.LibroConsigliato}: Un DTO utilizzato
 *         per aggregare i dati, mostrando un libro e il numero di volte che è stato
 *         consigliato.</li>
 *     <li>{@link bookrecommender.condivisi.consigli.ConsiglioDettagliato}: Un DTO che
 *         arricchisce un consiglio con informazioni aggiuntive (come i titoli dei libri)
 *         per una visualizzazione più chiara lato client.</li>
 * </ul>
 * Tutte le classi sono serializzabili per consentirne il trasferimento tramite RMI.
 */
package bookrecommender.condivisi.consigli;
