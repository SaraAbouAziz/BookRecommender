package bookrecommender.consigli;

import bookrecommender.condivisi.consigli.ConsigliService;
import bookrecommender.condivisi.consigli.ConsiglioDettagliato;
import bookrecommender.utenti.GestoreSessione;
import bookrecommender.utili.ViewsController;
import bookrecommender.utili.ParticleAnimation;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import java.net.URL;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controller per la schermata di gestione dei consigli dati dall'utente (consigli-view.fxml).
 * <p>
 * Questa classe gestisce l'interfaccia utente che permette a un utente autenticato
 * di visualizzare, modificare e cancellare i consigli che ha precedentemente fornito.
 * <p>
 * <b>Responsabilità principali:</b>
 * <ul>
 *     <li>Connettersi al servizio RMI {@link ConsigliService} per recuperare e manipolare i dati dei consigli.</li>
 *     <li>Ottenere l'ID dell'utente corrente tramite {@link GestoreSessione}.</li>
 *     <li>Caricare e visualizzare la lista dei consigli dati dall'utente in una {@link ListView}.</li>
 *     <li>Mostrare un pannello di dettaglio quando un consiglio viene selezionato, permettendo la modifica del commento.</li>
 *     <li>Gestire il salvataggio delle modifiche al commento e la cancellazione di un consiglio, con relativa conferma.</li>
 *     <li>Fornire feedback all'utente tramite un'etichetta di stato.</li>
 *     <li>Gestire la navigazione per tornare all'area privata.</li>
 *     <li>Controllare un'animazione di sfondo {@link ParticleAnimation}.</li>
 * </ul>
 *
 * @author Abou Aziz Sara Hesham Abdel Hamid 757004
 * @author Ben Mahjoub Ali 759560
 * @author Hidri Mohamed Taha 756235
 * @author Zoghbani Lilia 759652
 * @version 1.0
 */
public class ConsigliController implements Initializable {

	/** Il pannello radice della vista, usato come contenitore principale. */
	@FXML private StackPane rootPane;
	/** Il canvas per l'animazione di sfondo. */
	@FXML private Canvas backgroundCanvas;

	/** Etichetta che mostra un riepilogo, come il numero totale di consigli dati. */
	@FXML private Label subtitleLabel;
	/** La ListView che visualizza l'elenco dei consigli dati dall'utente. */
	@FXML private ListView<ConsiglioDettagliato> consigliListView;
	/** Il pannello che contiene i dettagli del consiglio selezionato e i controlli di modifica. */
	@FXML private VBox detailPanel;
	/** Etichetta che mostra i dettagli del consiglio selezionato (libri, libreria). */
	@FXML private Label selectedConsiglioInfo;
	/** Area di testo per modificare il commento del consiglio selezionato. */
	@FXML private TextArea commentEditor;
	/** Etichetta per mostrare messaggi di stato (es. successo, errore, caricamento). */
	@FXML private Label messageLabel;
	/** Pulsante per salvare le modifiche al commento. */
	@FXML private Button saveButton;
	/** Pulsante per eliminare il consiglio selezionato. */
	@FXML private Button deleteButton;
	/** Pulsante per annullare la selezione e nascondere il pannello di modifica. */
	@FXML private Button cancelButton;

	/** Il servizio RMI per interagire con la logica di business dei consigli. */
	private ConsigliService consigliService;
	/** Il consiglio attualmente selezionato nella ListView. */
	private ConsiglioDettagliato selectedConsiglio;
	/** L'ID dell'utente attualmente autenticato. */
	private String currentUserId;
	/** L'oggetto che gestisce l'animazione delle particelle in background. */
	private ParticleAnimation particleAnimation;

	/**
	 * Metodo chiamato da FXMLLoader all'inizializzazione del controller.
	 * <p>
	 * Esegue le seguenti operazioni in sequenza:
	 * <ol>
	 *     <li>Inizializza la connessione al servizio RMI ({@link #setupServices()}).</li>
	 *     <li>Configura e avvia l'animazione di sfondo ({@link #setupParticleAnimation()}).</li>
	 *     <li>Imposta la factory per personalizzare la visualizzazione delle celle nella ListView ({@link #setupListCellFactory()}).</li>
	 *     <li>Carica i consigli dell'utente dal servizio ({@link #loadConsigli()}).</li>
	 *     <li>Imposta il listener per gestire la selezione di un elemento nella lista ({@link #setupSelectionListener()}).</li>
	 *     <li>Resetta il pannello di modifica allo stato iniziale ({@link #resetEditor()}).</li>
	 * </ol>
	 *
	 * @param location L'URL della risorsa FXML, non utilizzato direttamente.
	 * @param resources Il ResourceBundle, non utilizzato direttamente.
	 */
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		setupServices();
		setupParticleAnimation();
		setupListCellFactory();
		loadConsigli();
		setupSelectionListener();
		resetEditor();
	}

	/**
	 * Inizializza il servizio RMI {@link ConsigliService} e recupera l'ID dell'utente corrente.
	 * Esegue un lookup sul registro RMI all'indirizzo {@code localhost:1099}.
	 * In caso di errore, imposta un messaggio di stato nella {@code messageLabel}.
	 */
	private void setupServices() {
		try {
			Registry registry = LocateRegistry.getRegistry("localhost", 1099);
			consigliService = (ConsigliService) registry.lookup(ConsigliService.NAME);
			currentUserId = GestoreSessione.getInstance().getCurrentUserId();
		} catch (Exception e) {
			messageLabel.setText("Errore collegamento servizio consigli");
		}
	}

	/**
	 * Inizializza e avvia l'animazione di sfondo {@link ParticleAnimation}.
	 * Aggiunge anche un listener per fermare l'animazione alla chiusura della finestra,
	 * prevenendo così memory leak.
	 */
	private void setupParticleAnimation() {
		if (rootPane != null && backgroundCanvas != null) {
			particleAnimation = new ParticleAnimation(backgroundCanvas);
			particleAnimation.start();

			// Stop animation when window closes to prevent memory leaks
			backgroundCanvas.sceneProperty().addListener((obs, oldScene, newScene) -> {
				if (newScene != null) {
					newScene.windowProperty().addListener((obsWindow, oldWindow, newWindow) -> {
						if (newWindow != null) {
							newWindow.setOnCloseRequest(event -> {
								if (particleAnimation != null) {
									particleAnimation.stop();
								}
							});
						}
					});
				}
			});
		}
	}

	/**
	 * Configura la {@link ListCell} della {@code consigliListView} per visualizzare
	 * correttamente ogni oggetto {@link ConsiglioDettagliato}.
	 * Utilizza il metodo {@code getDisplayText()} dell'oggetto per ottenere la stringa da mostrare.
	 */
	private void setupListCellFactory() {
		Callback<ListView<ConsiglioDettagliato>, ListCell<ConsiglioDettagliato>> factory = lv -> new ListCell<ConsiglioDettagliato>() {
			@Override
			protected void updateItem(ConsiglioDettagliato item, boolean empty) {
				super.updateItem(item, empty);
				if (empty || item == null) {
					setText(null);
				} else {
					setText(item.getDisplayText());
				}
			}
		};
		consigliListView.setCellFactory(factory);
	}

	/**
	 * Imposta un listener sulla proprietà di selezione della {@code consigliListView}.
	 * Quando un elemento viene selezionato, chiama {@link #onSelectConsiglio(ConsiglioDettagliato)}; altrimenti, resetta l'editor.
	 */
	private void setupSelectionListener() {
		consigliListView.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
			if (newV != null) {
				onSelectConsiglio(newV);
			} else {
				resetEditor();
			}
		});
	}

	/**
	 * Gestisce la logica da eseguire quando un consiglio viene selezionato dalla lista.
	 * Aggiorna lo stato interno, popola il pannello di dettaglio con le informazioni
	 * del consiglio e lo rende visibile.
	 *
	 * @param c Il {@link ConsiglioDettagliato} selezionato.
	 */
	private void onSelectConsiglio(ConsiglioDettagliato c) {
		selectedConsiglio = c;
		selectedConsiglioInfo.setText(String.format(
			"Libreria: %s\nLibro letto: %s (%s)\nLibro consigliato: %s (%s)",
			c.nomeLibreria(),
			c.titoloLibroLetto(), c.autoreLibroLetto(),
			c.titoloLibroConsigliato(), c.autoreLibroConsigliato()
		));
		commentEditor.setText(c.commento() != null ? c.commento() : "");
		messageLabel.setText("");
		
		// Mostra il pannello di dettaglio
		detailPanel.setVisible(true);
		detailPanel.setManaged(true);
	}

	/**
	 * Resetta il pannello di modifica al suo stato iniziale.
	 * Cancella la selezione, pulisce i campi di testo e nasconde il pannello.
	 */
	private void resetEditor() {
		selectedConsiglio = null;
		selectedConsiglioInfo.setText("");
		commentEditor.clear();
		
		// Nascondi il pannello di dettaglio
		detailPanel.setVisible(false);
		detailPanel.setManaged(false);
		
		messageLabel.setText("");
	}

	/**
	 * Carica la lista dei consigli dati dall'utente corrente tramite il servizio RMI.
	 * Aggiorna la {@code consigliListView} e le etichette di stato con i risultati.
	 * Gestisce gli errori di caricamento mostrando un messaggio all'utente.
	 */
	private void loadConsigli() {
		messageLabel.setText("Caricamento in corso...");
		try {
			if (consigliService == null || currentUserId == null) {
				messageLabel.setText("Servizio non disponibile");
				return;
			}
			List<ConsiglioDettagliato> miei = consigliService.listConsigliDettagliatiByUser(currentUserId);
			consigliListView.getItems().setAll(miei);
			subtitleLabel.setText("Hai dato " + miei.size() + " consigli");
			messageLabel.setText(miei.isEmpty() ? "Nessun consiglio trovato. Inizia a consigliare libri dalle tue librerie!" : "");
		} catch (Exception ex) {
			messageLabel.setText("Errore nel caricamento consigli: " + ex.getMessage());
		}
	}

	/**
	 * Gestisce l'evento di click sul pulsante "Salva".
	 * Recupera il nuovo commento dall'editor, invoca il metodo {@code updateCommento} del servizio RMI
	 * e, in caso di successo, aggiorna l'elemento corrispondente nella {@code consigliListView}
	 * e mostra un messaggio di conferma.
	 */
	@FXML
	private void handleSave() {
		if (selectedConsiglio == null || consigliService == null) {
			return;
		}
		try {
			String nuovoCommento = commentEditor.getText() != null ? commentEditor.getText().trim() : "";
			consigliService.updateCommento(
				selectedConsiglio.userId(),
				selectedConsiglio.libreriaId(),
				selectedConsiglio.libroLettoId(),
				selectedConsiglio.libroConsigliatoId(),
				nuovoCommento
			);
			messageLabel.setText("✅ Commento aggiornato con successo!");
			
			// Aggiorna l'elemento nella lista
			int idx = consigliListView.getItems().indexOf(selectedConsiglio);
			if (idx >= 0) {
				ConsiglioDettagliato aggiornato = new ConsiglioDettagliato(
					selectedConsiglio.userId(),
					selectedConsiglio.libreriaId(),
					selectedConsiglio.nomeLibreria(),
					selectedConsiglio.libroLettoId(),
					selectedConsiglio.titoloLibroLetto(),
					selectedConsiglio.autoreLibroLetto(),
					selectedConsiglio.libroConsigliatoId(),
					selectedConsiglio.titoloLibroConsigliato(),
					selectedConsiglio.autoreLibroConsigliato(),
					nuovoCommento,
					selectedConsiglio.dataConsiglio()
				);
				consigliListView.getItems().set(idx, aggiornato);
				selectedConsiglio = aggiornato;
			}
			consigliListView.refresh();
		} catch (Exception ex) {
			messageLabel.setText("❌ Errore durante l'aggiornamento: " + ex.getMessage());
		}
	}

	/**
	 * Gestisce l'evento di click sul pulsante "Elimina".
	 * Mostra un dialogo di conferma all'utente. Se l'utente conferma, invoca il metodo
	 * {@code deleteConsiglio} del servizio RMI e, in caso di successo, rimuove l'elemento
	 * dalla {@code consigliListView} e resetta l'editor.
	 */
	@FXML
	private void handleDelete() {
		ConsiglioDettagliato sel = selectedConsiglio;
		if (sel == null || consigliService == null) {
			return;
		}
		
		// Conferma eliminazione
		Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
		alert.setTitle("Conferma eliminazione");
		alert.setHeaderText("Eliminare questo consiglio?");
		alert.setContentText(String.format("Stai per eliminare il consiglio:\n\n" +
			"📚 %s\n📖 %s → 💡 %s\n\nQuesta azione non può essere annullata.",
			sel.nomeLibreria(),
			sel.titoloLibroLetto(),
			sel.titoloLibroConsigliato()));
		
		Optional<ButtonType> result = alert.showAndWait();
		if (result.isPresent() && result.get() == ButtonType.OK) {
			try {
				consigliService.deleteConsiglio(
					sel.userId(),
					sel.libreriaId(),
					sel.libroLettoId(),
					sel.libroConsigliatoId()
				);
				consigliListView.getItems().remove(sel);
				resetEditor();
				messageLabel.setText("🗑 Consiglio eliminato con successo");
				subtitleLabel.setText("Hai dato " + consigliListView.getItems().size() + " consigli");
			} catch (Exception ex) {
				messageLabel.setText("❌ Errore durante l'eliminazione: " + ex.getMessage());
			}
		}
	}

	/**
	 * Gestisce l'evento di click sul pulsante "Annulla".
	 * Deseleziona l'elemento corrente nella lista e resetta il pannello di modifica.
	 */
	@FXML
	private void handleCancel() {
		consigliListView.getSelectionModel().clearSelection();
		resetEditor();
		messageLabel.setText("");
	}

	/**
	 * Gestisce l'evento di click sul pulsante "Indietro".
	 * Ferma l'animazione di sfondo e naviga all'area privata tramite {@link ViewsController}.
	 */
	@FXML
	private void handleBack() {
		if (particleAnimation != null) {
			particleAnimation.stop();
		}
		ViewsController.mostraAreaPrivata();
	}
}
