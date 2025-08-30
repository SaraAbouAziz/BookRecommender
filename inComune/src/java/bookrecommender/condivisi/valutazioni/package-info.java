/**
 * Fornisce le classi e le interfacce condivise per la gestione delle valutazioni dei libri.
 * <p>
 * Questo package definisce il contratto di comunicazione tra client e server per tutte
 * le operazioni relative alle recensioni dei libri, come l'inserimento, la modifica,
 * l'eliminazione e il recupero delle valutazioni.
 * Le classi principali includono:
 * <ul>
 *     <li>{@link bookrecommender.condivisi.valutazioni.ValutazioneService}: L'interfaccia RMI
 *         che espone i metodi per la gestione remota delle valutazioni.</li>
 *     <li>{@link bookrecommender.condivisi.valutazioni.Valutazione}: Un record che rappresenta
 *         una valutazione di base, usato per il trasferimento dati.</li>
 *     <li>{@link bookrecommender.condivisi.valutazioni.ValutazioneDettagliata}: Un DTO arricchito
 *         con i punteggi specifici (stile, contenuto, etc.) e le note testuali,
 *         ideale per le interfacce di modifica e visualizzazione dettagliata.</li>
 * </ul>
 * Tutte le classi di dati sono serializzabili per il trasferimento tramite RMI.
 */
package bookrecommender.condivisi.valutazioni;