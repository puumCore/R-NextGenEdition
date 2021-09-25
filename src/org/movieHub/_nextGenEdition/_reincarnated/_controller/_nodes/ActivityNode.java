package org.movieHub._nextGenEdition._reincarnated._controller._nodes;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import org.movieHub._nextGenEdition._reincarnated._custom.Assistant;
import org.movieHub._nextGenEdition._reincarnated._model._enum.LoadStatus;
import org.movieHub._nextGenEdition._reincarnated._model._object.History;
import org.movieHub._nextGenEdition._reincarnated._model._object.Show;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * @author Mandela aka puumInc
 */

public class ActivityNode extends Assistant implements Initializable {

    public static History history;
    private History myHistory;

    @FXML
    private Label jobNameLbl;

    @FXML
    private Label timeStartLbl;

    @FXML
    private Label timeEndLbl;

    @FXML
    private Label statusLbl;

    @FXML
    void show_list_of_media(ActionEvent event) {
        get_list_alert(String.format("%s has the following shows: ", Optional.ofNullable(this.myHistory.getLoad().getName()).orElse("This stream load")), generate_a_string_from_list_of_showNames(get_file_names(this.myHistory.getLoad().getShowList().toArray(new Show[]{})))).show();
        event.consume();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.myHistory = ActivityNode.history;
        jobNameLbl.setText(Optional.ofNullable(this.myHistory.getLoad().getName()).orElse(this.myHistory.getLoadPurpose().asString()));
        timeStartLbl.setText(beautify_time(this.myHistory.getTimeWhenItStarted()));
        timeEndLbl.setText(beautify_time(Optional.ofNullable(this.myHistory.getTimeWhenItStopped()).orElse("")));
        if (!this.myHistory.getLoadStatus().equals(LoadStatus.COMPLETE)) {
            statusLbl.setStyle("-fx-font-family: 'Fira Sans Medium'; -fx-font-size: 13px; -fx-text-fill: #e42828;");
        }
        statusLbl.setText(this.myHistory.getLoadStatus().getStatus());
    }

    private String beautify_time(String time) {
        return time.replace(":", " : ");
    }

}
