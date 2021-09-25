package org.movieHub._nextGenEdition._reincarnated._controller._nodes;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.movieHub._nextGenEdition._reincarnated._custom.Assistant;
import org.movieHub._nextGenEdition._reincarnated._model._enum.EntertainmentType;
import org.movieHub._nextGenEdition._reincarnated._model._object.Show;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * @author Mandela aka puumInc
 */
public class SelectedShowNode extends Assistant implements Initializable {

    public static Show show;
    public static int index;
    private Show myShow;

    @FXML
    private Label showNameTF;

    @FXML
    private Label typeLbl;

    @FXML
    private Label sizeTF;

    @FXML
    private void remove(ActionEvent event) {
        if (SHOW_SET.remove(this.myShow)) {
            Node me = showNameTF.getParent().getParent();
            VBox vBox = (VBox) me.getParent();
            VBox.clearConstraints(me);
            vBox.getChildren().remove(me);
        } else {
            error_message("Failed!", String.format("Could not remove %s", this.myShow.getSource().getName())).show();
        }
        event.consume();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.myShow = SelectedShowNode.show;
        showNameTF.setText(String.format("%d. %s", SelectedShowNode.index, this.myShow.getSource().getName()));
        showNameTF.getTooltip().setText(this.myShow.getSource().getAbsolutePath());

        typeLbl.setText(String.format("%s", this.myShow.getEntertainmentType().getSymbol()));
        sizeTF.setText(make_bytes_more_presentable(get_size_of_the_show_sources(this.myShow.getSource())));
        if (this.myShow.getEntertainmentType().equals(EntertainmentType.SERIES)) {
            sizeTF.getTooltip().setText(get_details_about_number_of_seasons_and_episodes_of_a_series(this.myShow.getSource()));
        }
    }

}
