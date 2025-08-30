/**
 * Contiene le classi del client per la gestione delle valutazioni dei libri.
 * <p>
 * Questo package include i controller e la logica necessari per permettere
 * agli utenti di creare, visualizzare e modificare le proprie recensioni
 * e di consultare quelle degli altri.
 * Le classi principali sono:
 * <ul>
 *     <li>{@link bookrecommender.valutazioni.ValutazioneCompletaController}: Gestisce l'interfaccia
 *         per l'inserimento di una valutazione dettagliata di un libro.</li>
 *     <li>{@link bookrecommender.valutazioni.AllValutazioniController}: Gestisce la visualizzazione
 *         di tutte le recensioni pubbliche per un dato libro.</li>
 * </ul>
 * Queste classi comunicano con il servizio RMI {@code ValutazioneService} per
 * persistere e recuperare i dati dal server.
 */
package bookrecommender.valutazioni;