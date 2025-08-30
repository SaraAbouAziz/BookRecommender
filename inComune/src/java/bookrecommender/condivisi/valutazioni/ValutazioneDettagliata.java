package bookrecommender.condivisi.valutazioni;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Rappresenta una valutazione con dettagli leggibili per l'interfaccia utente.
 * <p>
 * Questo record è un Data Transfer Object (DTO) "arricchito", progettato specificamente
 * per essere utilizzato nelle interfacce utente. A differenza della classe base {@link Valutazione},
 * questo oggetto include anche informazioni denormalizzate come il titolo e l'autore del libro,
 * che vengono recuperate tramite join a livello di database.
 * <p>
 * L'uso di questo DTO ottimizza la comunicazione tra client e server, poiché permette
 * di trasferire tutte le informazioni necessarie per visualizzare una valutazione in una
 * singola chiamata, senza che il client debba richiedere separatamente i dettagli del libro.
 *
 * @param userId L'identificativo dell'utente che ha effettuato la valutazione.
 * @param libroId L'identificativo del libro che è stato valutato.
 * @param titoloLibro Il titolo del libro valutato.
 * @param autoreLibro L'autore del libro valutato.
 * @param nomeLibreria Il nome della libreria dell'utente da cui è stata avviata la valutazione.
 * @param stile Punteggio (da 1 a 5) per il criterio "Stile di scrittura".
 * @param stileNote Note testuali opzionali relative allo stile.
 * @param contenuto Punteggio (da 1 a 5) per il criterio "Contenuto e trama".
 * @param contenutoNote Note testuali opzionali relative al contenuto.
 * @param gradevolezza Punteggio (da 1 a 5) per il criterio "Gradevolezza di lettura".
 * @param gradevolezzaNote Note testuali opzionali relative alla gradevolezza.
 * @param originalita Punteggio (da 1 a 5) per il criterio "Originalità e creatività".
 * @param originalitaNote Note testuali opzionali relative all'originalità.
 * @param edizione Punteggio (da 1 a 5) per il criterio "Qualità dell'edizione".
 * @param edizioneNote Note testuali opzionali relative alla qualità dell'edizione.
 * @param votoComplessivo La media calcolata dei cinque punteggi dei criteri.
 * @param commentoFinale Un commento testuale riassuntivo sulla valutazione complessiva.
 * @param dataValutazione La data e l'ora in cui la valutazione è stata registrata o aggiornata.
 *                        Può essere {@code null} se l'oggetto rappresenta una valutazione non ancora persistita.
 *
 * @see Valutazione
 * @see ValutazioneService
 * @see bookrecommender.valutazioni.ValutazioniController
 */
public record ValutazioneDettagliata(
    String userId,
    int libroId,
    String titoloLibro,
    String autoreLibro,
    String nomeLibreria,
    int stile,
    String stileNote,
    int contenuto,
    String contenutoNote,
    int gradevolezza,
    String gradevolezzaNote,
    int originalita,
    String originalitaNote,
    int edizione,
    String edizioneNote,
    double votoComplessivo,
    String commentoFinale,
    LocalDateTime dataValutazione
) implements Serializable {

    /** Campo per il controllo della versione durante la serializzazione. */
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Converte questo DTO dettagliato in un oggetto {@link Valutazione} di base.
     * <p>
     * Questo metodo è utile per "ridurre" l'oggetto alla sua forma essenziale,
     * ad esempio prima di inviarlo a un metodo di aggiornamento del backend che si aspetta il DTO fondamentale.
     *
     * @return Un'istanza di {@link Valutazione} con gli stessi dati di valutazione.
     */
    public Valutazione toValutazione() {
        return new Valutazione(userId, libroId, nomeLibreria, stile, stileNote,
                              contenuto, contenutoNote, gradevolezza, gradevolezzaNote,
                              originalita, originalitaNote, edizione, edizioneNote,
                              votoComplessivo, commentoFinale, dataValutazione);
    }

    /**
     * Restituisce una rappresentazione testuale formattata e multi-linea, ideale per la visualizzazione
     * in componenti UI come le celle di una {@link javafx.scene.control.ListView}.
     * <p>
     * Include il nome della libreria, titolo, autore, ID, voto e commento finale.
     * @return Una stringa formattata per la visualizzazione.
     */
    public String getDisplayText() {
        return String.format("📚 %s\n📖 %s (%s) [ID: %d]\n⭐ Voto: %.1f/5.0\n💬 %s",
                           nomeLibreria,
                           titoloLibro, autoreLibro, libroId,
                           votoComplessivo,
                           commentoFinale != null && !commentoFinale.trim().isEmpty() 
                               ? commentoFinale : "Nessun commento finale");
    }

    /**
     * Restituisce un riassunto compatto e su una sola riga dei punteggi per ciascuno dei cinque criteri.
     *
     * @return Una stringa che riassume i punteggi (es. "Stile: 4/5 • Contenuto: 5/5 ...").
     */
    public String getScoresSummary() {
        return String.format("Stile: %d/5 • Contenuto: %d/5 • Gradevolezza: %d/5 • Originalità: %d/5 • Edizione: %d/5",
                           stile, contenuto, gradevolezza, originalita, edizione);
    }
}