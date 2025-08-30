package bookrecommender.valutazioni;

import bookrecommender.condivisi.libri.Libro;
import bookrecommender.condivisi.valutazioni.ValutazioneService;
import bookrecommender.utili.ParticleAnimation;
import bookrecommender.utili.ViewsController;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class AllValutazioniController {

    @FXML private Canvas backgroundCanvas;
    @FXML private Label titoloLibroLabel;
    @FXML private VBox valutazioniContainer;

    private Libro libroCorrente;
    private ValutazioneService valutazioneService;
    private ParticleAnimation particleAnimation;
       private ViewsController.Provenienza provenienza;

    @FXML
    public void initialize() {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            valutazioneService = (ValutazioneService) registry.lookup("ValutazioneService");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Errore di Connessione", "Impossibile connettersi al servizio di valutazione.");
        }

        if (backgroundCanvas != null) {
            particleAnimation = new ParticleAnimation(backgroundCanvas);
            particleAnimation.start();
        }
    }

    public void setLibro(Libro libro) {
        this.libroCorrente = libro;
        titoloLibroLabel.setText("Recensioni per: " + libro.titolo());
        loadValutazioni();
    }

    private void loadValutazioni() {
        valutazioniContainer.getChildren().clear();
        if (valutazioneService == null || libroCorrente == null) {
            return;
        }

        try {
            List<Map<String, Object>> valutazioni = valutazioneService.caricaValutazioniLibro(libroCorrente.id().intValue());

            if (valutazioni.isEmpty()) {
                Label noReviewsLabel = new Label("Nessuna recensione dettagliata trovata per questo libro.");
                noReviewsLabel.getStyleClass().add("info-text");
                valutazioniContainer.getChildren().add(noReviewsLabel);
                return;
            }

            for (Map<String, Object> v : valutazioni) {
                valutazioniContainer.getChildren().add(createValutazioneNode(v));
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Errore", "Impossibile caricare le valutazioni: " + e.getMessage());
        }
    }

    private Node createValutazioneNode(Map<String, Object> v) {
        VBox panel = new VBox(15);
        panel.getStyleClass().add("library-panel");
        panel.setPadding(new Insets(25, 30, 25, 30));
        panel.setMaxWidth(800);

        Label userLabel = new Label("Recensione di: " + v.get("userId"));
        userLabel.getStyleClass().add("section-title");

        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(10);

        int stile = ((Number) v.get("stile")).intValue();
        int contenuto = ((Number) v.get("contenuto")).intValue();
        int gradevolezza = ((Number) v.get("gradevolezza")).intValue();
        int originalita = ((Number) v.get("originalita")).intValue();
        int edizione = ((Number) v.get("edizione")).intValue();

        grid.add(new Label("Stile: " + stile + "/5 - " + v.get("stileNote")), 0, 0);
        grid.add(new Label("Contenuto: " + contenuto + "/5 - " + v.get("contenutoNote")), 0, 1);
        grid.add(new Label("Gradevolezza: " + gradevolezza + "/5 - " + v.get("gradevolezzaNote")), 0, 2);
        grid.add(new Label("Originalità: " + originalita + "/5 - " + v.get("originalitaNote")), 0, 3);
        grid.add(new Label("Edizione: " + edizione + "/5 - " + v.get("edizioneNote")), 0, 4);

        double votoFinale = (stile + contenuto + gradevolezza + originalita + edizione) / 5.0;

        Label finaleLabel = new Label(String.format("Voto Finale: %.1f/5.0", votoFinale));
        finaleLabel.getStyleClass().add("book-title");
        Label commentoFinaleLabel = new Label((String) v.get("commentoFinale"));
        commentoFinaleLabel.getStyleClass().add("info-text");
        commentoFinaleLabel.setWrapText(true);

        panel.getChildren().addAll(userLabel, new Separator(), grid, new Separator(), finaleLabel, commentoFinaleLabel);
        return panel;
    }


      public void setProvenienza(ViewsController.Provenienza provenienza) {
        this.provenienza = provenienza;
    }



    @FXML
    private void onBack() {
        if (particleAnimation != null) {
            particleAnimation.stop();
        }
         try {
            if (provenienza == ViewsController.Provenienza.LIBRERIE) {
                ViewsController.mostraLibrerie();
            } else {
                ViewsController.mostraCercaLibri();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
