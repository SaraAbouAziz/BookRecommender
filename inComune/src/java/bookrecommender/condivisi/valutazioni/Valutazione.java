package bookrecommender.condivisi.valutazioni;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Rappresenta una valutazione completa di un libro da parte di un utente.
 * <p>
 * Questo record è un Data Transfer Object (DTO) immutabile, progettato per
 * trasportare i dati di una valutazione dettagliata tra i vari layer
 * dell'applicazione, in particolare tra il client e il server tramite RMI.
 * Contiene i punteggi e le note per cinque criteri specifici, oltre a un
 * voto complessivo e un commento finale.
 * <p>
 * La sua chiave primaria logica è la combinazione di {@code userId} e {@code libroId}.
 *
 * @param userId L'identificativo dell'utente che ha effettuato la valutazione.
 * @param libroId L'identificativo del libro che è stato valutato.
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
 * @see ValutazioneService
 * @see ValutazioneDettagliata
 */
public record Valutazione(
    String userId,
    int libroId,
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
     * Costruttore di convenienza per creare un'istanza di {@code Valutazione}
     * che rappresenta una valutazione non ancora persistita. La data della valutazione
     * viene inizializzata a {@code null}, poiché si assume che verrà generata
     * dal database al momento dell'inserimento.
     *
     * @param userId L'ID dell'utente che effettua la valutazione.
     * @param libroId L'ID del libro valutato.
     * @param nomeLibreria Il nome della libreria di riferimento.
     * @param stile Il punteggio per lo stile.
     * @param stileNote Le note per lo stile.
     * @param contenuto Il punteggio per il contenuto.
     * @param contenutoNote Le note per il contenuto.
     * @param gradevolezza Il punteggio per la gradevolezza.
     * @param gradevolezzaNote Le note per la gradevolezza.
     * @param originalita Il punteggio per l'originalità.
     * @param originalitaNote Le note per l'originalità.
     * @param edizione Il punteggio per l'edizione.
     * @param edizioneNote Le note per l'edizione.
     * @param votoComplessivo Il voto complessivo calcolato.
     * @param commentoFinale Il commento finale.
     */
    public Valutazione(String userId, int libroId, String nomeLibreria,
                      int stile, String stileNote, int contenuto, String contenutoNote,
                      int gradevolezza, String gradevolezzaNote, int originalita, String originalitaNote,
                      int edizione, String edizioneNote, double votoComplessivo, String commentoFinale) {
        this(userId, libroId, nomeLibreria, stile, stileNote, contenuto, contenutoNote,
             gradevolezza, gradevolezzaNote, originalita, originalitaNote, edizione, edizioneNote,
             votoComplessivo, commentoFinale, null);
    }
}