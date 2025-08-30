/**
 * Fornisce le classi per la gestione delle funzionalità relative agli utenti
 * nell'applicazione client.
 * <p>
 * Questo package raggruppa tutti i controller e le classi di logica che
 * gestiscono l'interazione dell'utente con il sistema per quanto riguarda
 * l'account e la sessione. Le responsabilità principali includono:
 * <ul>
 *     <li>{@link bookrecommender.utenti.LoginController}: Gestione del processo di login.</li>
 *     <li>{@link bookrecommender.utenti.RegistrazioneController}: Gestione del processo di registrazione di un nuovo utente.</li>
 *     <li>{@link bookrecommender.utenti.DashboardController}: Controller per l'area privata dell'utente, che funge da hub centrale.</li>
 *     <li>{@link bookrecommender.utenti.GestoreSessione}: Singleton per la gestione dello stato della sessione utente (login, logout, dati utente).</li>
 * </ul>
 * Le classi in questo package interagiscono strettamente con il servizio RMI
 * {@link bookrecommender.condivisi.utenti.UtentiService} per comunicare con il server
 * e con {@link bookrecommender.utili.ViewsController} per la navigazione tra le viste.
 */
package bookrecommender.utenti;
