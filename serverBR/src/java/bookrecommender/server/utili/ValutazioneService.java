package bookrecommender.server.utili;

import java.io.*;
import java.util.*;

/**
 * Servizio per la gestione delle valutazioni dei libri
 * Implementa la logica di business per il salvataggio e recupero delle valutazioni
 */
public class ValutazioneService {
    
    private static final String VALUTAZIONI_FILE = "ValutazioniLibri.dati.csv";
    private static final String SEPARATOR = ";";
    
    /**
     * Verifica se un libro è già stato valutato da un utente
     */
    public static boolean isLibroGiaValutato(int libroId, String userId) {
        try {
            File file = new File(VALUTAZIONI_FILE);
            if (!file.exists()) {
                return false;
            }
            
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(SEPARATOR);
                    if (parts.length >= 2) {
                        String fileUserId = parts[0];
                        int fileLibroId = Integer.parseInt(parts[1]);
                        
                        if (fileUserId.equals(userId) && fileLibroId == libroId) {
                            return true;
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Errore nel verificare se il libro è già valutato: " + e.getMessage());
        }
        return false;
    }
    
    /**
     * Salva una valutazione completa di un libro
     */
    public static boolean salvaValutazione(String userId, int libroId, 
                                         int stile, String stileNote,
                                         int contenuto, String contenutoNote,
                                         int gradevolezza, String gradevolezzaNote,
                                         int originalita, String originalitaNote,
                                         int edizione, String edizioneNote,
                                         double votoFinale, String commentoFinale) {
        try {
            // Validazione dei dati
            if (!isValutazioneValida(stile, contenuto, gradevolezza, originalita, edizione)) {
                return false;
            }
            
            // Verifica se già valutato
            if (isLibroGiaValutato(libroId, userId)) {
                return false;
            }
            
            // Prepara la riga CSV
            String line = String.join(SEPARATOR,
                userId,
                String.valueOf(libroId),
                String.valueOf(stile),
                escapeCsv(stileNote),
                String.valueOf(contenuto),
                escapeCsv(contenutoNote),
                String.valueOf(gradevolezza),
                escapeCsv(gradevolezzaNote),
                String.valueOf(originalita),
                escapeCsv(originalitaNote),
                String.valueOf(edizione),
                escapeCsv(edizioneNote),
                String.format("%.1f", votoFinale),
                escapeCsv(commentoFinale)
            );
            
            // Salva nel file
            try (FileWriter writer = new FileWriter(VALUTAZIONI_FILE, true);
                 BufferedWriter bw = new BufferedWriter(writer);
                 PrintWriter out = new PrintWriter(bw)) {
                out.println(line);
            }
            
            return true;
            
        } catch (Exception e) {
            System.err.println("Errore nel salvare la valutazione: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Carica tutte le valutazioni di un libro
     */
    public static List<Map<String, Object>> caricaValutazioniLibro(int libroId) {
        List<Map<String, Object>> valutazioni = new ArrayList<>();
        
        try {
            File file = new File(VALUTAZIONI_FILE);
            if (!file.exists()) {
                return valutazioni;
            }
            
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(SEPARATOR);
                    if (parts.length >= 14) {
                        int fileLibroId = Integer.parseInt(parts[1]);
                        
                        if (fileLibroId == libroId) {
                            Map<String, Object> valutazione = new HashMap<>();
                            valutazione.put("userId", parts[0]);
                            valutazione.put("libroId", fileLibroId);
                            valutazione.put("stile", Integer.parseInt(parts[2]));
                            valutazione.put("stileNote", unescapeCsv(parts[3]));
                            valutazione.put("contenuto", Integer.parseInt(parts[4]));
                            valutazione.put("contenutoNote", unescapeCsv(parts[5]));
                            valutazione.put("gradevolezza", Integer.parseInt(parts[6]));
                            valutazione.put("gradevolezzaNote", unescapeCsv(parts[7]));
                            valutazione.put("originalita", Integer.parseInt(parts[8]));
                            valutazione.put("originalitaNote", unescapeCsv(parts[9]));
                            valutazione.put("edizione", Integer.parseInt(parts[10]));
                            valutazione.put("edizioneNote", unescapeCsv(parts[11]));
                            valutazione.put("votoFinale", Double.parseDouble(parts[12]));
                            valutazione.put("commentoFinale", unescapeCsv(parts[13]));
                            
                            valutazioni.add(valutazione);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Errore nel caricare le valutazioni: " + e.getMessage());
        }
        
        return valutazioni;
    }
    
    /**
     * Calcola la media delle valutazioni per un libro
     */
    public static double calcolaMediaValutazioni(int libroId) {
        List<Map<String, Object>> valutazioni = caricaValutazioniLibro(libroId);
        
        if (valutazioni.isEmpty()) {
            return 0.0;
        }
        
        double somma = 0.0;
        for (Map<String, Object> valutazione : valutazioni) {
            somma += (Double) valutazione.get("votoFinale");
        }
        
        return somma / valutazioni.size();
    }
    
    /**
     * Ottiene il numero di valutazioni per un libro
     */
    public static int getNumeroValutazioni(int libroId) {
        return caricaValutazioniLibro(libroId).size();
    }
    
    /**
     * Valida i dati di una valutazione
     */
    private static boolean isValutazioneValida(int stile, int contenuto, int gradevolezza, int originalita, int edizione) {
        return stile >= 1 && stile <= 5 &&
               contenuto >= 1 && contenuto <= 5 &&
               gradevolezza >= 1 && gradevolezza <= 5 &&
               originalita >= 1 && originalita <= 5 &&
               edizione >= 1 && edizione <= 5;
    }
    
    /**
     * Escape dei caratteri speciali per CSV
     */
    private static String escapeCsv(String text) {
        if (text == null) return "";
        return text.replace(";", ",").replace("\n", " ").replace("\r", "");
    }
    
    /**
     * Unescape dei caratteri speciali dal CSV
     */
    private static String unescapeCsv(String text) {
        if (text == null) return "";
        return text.replace(",", ";");
    }
}

