import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe per la lettura diretta di file CSV e popolamento del database.
 * Sostituisce le classi Book e BookReader per operazioni di caricamento dati.
 * 
 * @author Taha
 * @author Sara
 */
public class LeggiFileCSV {

    /**
     * Legge un file CSV e popola direttamente la tabella Libri nel database.
     * 
     * @param filePath Percorso del file CSV da leggere
     * @param connection Connessione al database
     * @return Numero di libri inseriti con successo
     */
    public static int popolaDatabaseDaCSV(String filePath, Connection connection) {
        int libriInseriti = 0;
        
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean primaRiga = true; // Per saltare l'header se presente
            
            // Preparo la query SQL una sola volta
            String sql = "INSERT INTO Libri (id, titolo, autori, anno, descrizione, categorie, editore, prezzo) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                
                while ((line = br.readLine()) != null) {
                    // Salto la prima riga se sembra un header
                    if (primaRiga && (line.toLowerCase().contains("id") || line.toLowerCase().contains("titolo"))) {
                        primaRiga = false;
                        continue;
                    }
                    
                    String[] fields = splitLine(line);
                    
                    if (fields.length == 9) {
                        try {
                            // Estraggo i campi direttamente
                            int id = Integer.parseInt(fields[0].trim());
                            String titolo = fields[1].trim();
                            String autori = fields[2].trim();
                            String anno = fields[8].trim(); // Anno di pubblicazione
                            String descrizione = fields[3].trim();
                            String categorie = fields[4].trim();
                            String editore = fields[5].trim();
                            String prezzo = fields[6].trim();
                            
                            // Imposto i parametri della query
                            ps.setInt(1, id);
                            ps.setString(2, titolo);
                            ps.setString(3, autori);
                            ps.setString(4, anno);
                            ps.setString(5, descrizione);
                            ps.setString(6, categorie);
                            ps.setString(7, editore);
                            ps.setString(8, prezzo);
                            
                            ps.addBatch(); // Aggiungo al batch
                            libriInseriti++;
                            
                        } catch (NumberFormatException e) {
                            System.err.println("Errore nel parsing dell'ID alla riga: " + line);
                        }
                    } else {
                        System.err.println("Riga con numero di campi errato (" + fields.length + "): " + line);
                    }
                }
                
                // Eseguo tutti gli insert in batch
                ps.executeBatch();
                System.out.println("Inseriti " + libriInseriti + " libri nel database.");
                
            } catch (SQLException e) {
                System.err.println("Errore durante l'inserimento nel database: " + e.getMessage());
                e.printStackTrace();
            }
            
        } catch (IOException e) {
            System.err.println("Errore nella lettura del file CSV: " + e.getMessage());
            e.printStackTrace();
        }
        
        return libriInseriti;
    }

    /**
     * Divide una riga di testo in un array di stringhe, tenendo conto delle virgole all'interno delle virgolette.
     * 
     * @param line La riga di testo da dividere
     * @return Un array di stringhe contenente i campi estratti
     */
    private static String[] splitLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean virgolette = false;

        for (char c : line.toCharArray()) {
            if (c == '"') {
                virgolette = !virgolette;
            } else if (c == ',' && !virgolette) {
                fields.add(field.toString());
                field.setLength(0);
            } else {
                field.append(c);
            }
        }
        fields.add(field.toString()); // Aggiunge l'ultimo campo
        
        return fields.toArray(new String[0]);
    }

    
}
