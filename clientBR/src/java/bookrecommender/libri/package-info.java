/**
 * Contiene le classi del client per la gestione delle funzionalità legate ai libri.
 * <p>
 * Questo package raggruppa i controller che gestiscono l'interazione dell'utente
 * con il catalogo dei libri. Le responsabilità principali includono:
 * <ul>
 *     <li>{@link bookrecommender.libri.CercaLibriController}: Fornisce l'interfaccia e la logica
 *         per la ricerca di libri nel database.</li>
 *     <li>{@link bookrecommender.libri.DettagliLibroController}: Gestisce la visualizzazione
 *         delle informazioni dettagliate di un singolo libro, incluse le valutazioni medie
 *         e i consigli degli altri utenti.</li>
 * </ul>
 * Le classi di questo package interagiscono con i servizi RMI
 * {@code CercaLibriService}, {@code ValutazioneService} e {@code ConsigliService}
 * per recuperare i dati dal server.
 */
package bookrecommender.libri;