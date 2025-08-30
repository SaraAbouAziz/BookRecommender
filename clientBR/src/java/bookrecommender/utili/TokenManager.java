package bookrecommender.utili;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

/**
 * Utility class per la gestione della persistenza della sessione su file.
 * <p>
 * Questa classe è responsabile della creazione, lettura e cancellazione di un
 * file "token" locale ({@code session.token}) che memorizza le credenziali
 * di base dell'utente (ID e username) per consentire la funzionalità
 * "ricordami" tra le sessioni dell'applicazione.
 * <p>
 * Agisce come livello di persistenza per {@link bookrecommender.utenti.GestoreSessione} e non dovrebbe
 * essere utilizzata direttamente da altre parti dell'applicazione.
 *
 * @author Abou Aziz Sara Hesham Abdel Hamid 757004
 * @author Ben Mahjoub Ali 759560
 * @author Hidri Mohamed Taha 756235
 * @author Kahri Mohamed Ameur 754773
 * @author Zoghbani Lilia 759652
 * @see bookrecommender.utenti.GestoreSessione
 * @version 1.0
 * @since 1.0
 */
public class TokenManager {
    /** Nome del file utilizzato per memorizzare il token di sessione. */
    private static final String TOKEN_FILE = "session.token";

    /**
     * Elimina il file del token di sessione, se esiste.
     * Questo metodo viene tipicamente chiamato durante il logout.
     */
    public static void deleteToken() {
        File file = new File(TOKEN_FILE);
        if (file.exists()) {
            file.delete();
        }
    }

    /**
     * Genera e salva un nuovo file token con l'ID e lo username dell'utente.
     * Il file conterrà l'ID utente sulla prima riga e lo username sulla seconda.
     *
     * @param userId l'ID dell'utente da salvare.
     * @param userName lo username dell'utente da salvare.
     */
    public static void generateToken(String userId, String userName) {
        try (FileWriter writer = new FileWriter(TOKEN_FILE)) {
            writer.write(userId + "\n");
            writer.write(userName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Controlla se un file token di sessione valido esiste.
     *
     * @return {@code true} se il file {@code session.token} esiste, {@code false} altrimenti.
     */
    public static boolean hasValidToken() {
        return new File(TOKEN_FILE).exists();
    }

    /**
     * Legge e restituisce l'ID utente dal file token.
     *
     * @return l'ID utente letto dal file, o {@code null} se il file non esiste o si verifica un errore.
     */
    public static String getCurrentUserId() {
        if (hasValidToken()) {
            try (Scanner scanner = new Scanner(new File(TOKEN_FILE))) {
                if (scanner.hasNextLine()) {
                    return scanner.nextLine();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * Legge e restituisce lo username dal file token.
     *
     * @return lo username letto dal file, o {@code null} se il file non esiste o si verifica un errore.
     */
    public static String getCurrentUsername() {
        if (hasValidToken()) {
            try (Scanner scanner = new Scanner(new File(TOKEN_FILE))) {
                if (scanner.hasNextLine()) {
                    scanner.nextLine(); // Skip userId
                }
                if (scanner.hasNextLine()) {
                    return scanner.nextLine();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
