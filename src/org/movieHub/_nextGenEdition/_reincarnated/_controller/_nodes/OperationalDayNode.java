package org.movieHub._nextGenEdition._reincarnated._controller._nodes;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.movieHub._nextGenEdition._reincarnated._custom.Assistant;
import org.movieHub._nextGenEdition._reincarnated._model._object.History;

import java.io.IOException;
import java.net.URL;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * @author Mandela aka puumInc
 */

public class OperationalDayNode extends Assistant implements Initializable {

    public static List<History> histories;

    @FXML
    private Label dateLbl;

    @FXML
    private VBox activitiesBox;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        List<History> historyList = OperationalDayNode.histories;
        dateLbl.setText(historyList.get(0).getDate());
        show_some_activities_for_context_day(get_sorted_activities_for_context_day(historyList));
    }

    private void show_some_activities_for_context_day(Map<LocalTime, List<History>> localTimeListMap) {
        int count = (localTimeListMap.size() >= 10) ? (localTimeListMap.size() / 2) : localTimeListMap.size();
        shell:
        for (Map.Entry<LocalTime, List<History>> entry : localTimeListMap.entrySet()) {
            if (count == 0) {
                break;
            }
            List<History> historyList = entry.getValue();
            for (History history : historyList) {
                try {
                    ActivityNode.history = history;
                    Node node = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/_fxml/activityNode.fxml")));
                    Platform.runLater(() -> activitiesBox.getChildren().add(node));
                } catch (IOException e) {
                    e.printStackTrace();
                    new Thread(write_stack_trace(e)).start();
                    Platform.runLater(() -> programmer_error(e).show());
                    break shell;
                } finally {
                    ActivityNode.history = null;
                }
            }
            --count;
        }
    }

    private Map<LocalTime, List<History>> get_sorted_activities_for_context_day(List<History> historyList) {
        HashMap<LocalTime, List<History>> unorderedMap = new HashMap<>();
        historyList.forEach(history -> {
            LocalTime localTime = LocalTime.parse(history.getTimeWhenItStarted(), DateTimeFormatter.ofPattern("HH:mm:ss:SSS"));
            if (unorderedMap.containsKey(localTime)) {
                unorderedMap.get(localTime).add(history);
            } else {
                List<History> historyArrayList = new ArrayList<>();
                historyArrayList.add(history);
                unorderedMap.put(localTime, historyArrayList);
            }
        });
        return new TreeMap<>(unorderedMap).descendingMap();
    }

}
