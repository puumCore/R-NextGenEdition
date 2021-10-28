package org.movieHub._nextGenEdition._reincarnated._controller._nodes;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.movieHub._nextGenEdition._reincarnated._custom.Assistant;
import org.movieHub._nextGenEdition._reincarnated._model._object.StreamLoad;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author Mandela aka puumInc
 */

public class StreamNode extends Assistant implements Initializable {

    public static StreamLoad streamLoad;
    private StreamLoad myStreamLoad;

    @FXML
    private Label showNameTF;

    @FXML
    private Label typeLbl;

    @FXML
    private Label statusTF;

    @FXML
    void remove(ActionEvent event) {
        if (STREAM_LOADS.remove(this.myStreamLoad)) {
            VBox streamBox = get_parent_VBox(showNameTF, "streamBox");
            if (streamBox != null) {
                remove_child_from_Vbox(streamBox, showNameTF.getParent().getParent());
            }
            if (!list_is_written_to_jsonFile(STREAM_LOADS, STREAM_LOAD_JF)) {
                error_message("Incomplete!", "The updated list has not been saved").show();
            }
        } else {
            error_message("Failed!", "Unable to remove the streaming show").show();
        }
        event.consume();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.myStreamLoad = StreamNode.streamLoad;
        File file = new File(this.myStreamLoad.getShowStreamList().get(0).getValue());
        showNameTF.setText((this.myStreamLoad.getShowStreamList().size() == 1) ? file.getName() : this.myStreamLoad.getName());
        showNameTF.getTooltip().setText((this.myStreamLoad.getShowStreamList().size() == 1) ? file.getAbsolutePath() : file.getParentFile().getAbsolutePath());
        typeLbl.setText((this.myStreamLoad.getShowStreamList().size() == 1) ? "M" : "S");

        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
        scheduledExecutorService.scheduleAtFixedRate(check_show_source_status(), 1, 15, TimeUnit.SECONDS);
    }

    private Runnable check_show_source_status() {
        return () -> {
            String statusOfShowFiles = get_status_of_show_files(this.myStreamLoad);
            Platform.runLater(() -> statusTF.setText(statusOfShowFiles));
        };
    }


}
