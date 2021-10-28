package org.movieHub._nextGenEdition._reincarnated._controller;

import animatefx.animation.*;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXRadioButton;
import com.jfoenix.controls.JFXTextArea;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import org.movieHub._nextGenEdition._reincarnated.Main;
import org.movieHub._nextGenEdition._reincarnated._controller._nodes.JobNode;
import org.movieHub._nextGenEdition._reincarnated._controller._nodes.OperationalDayNode;
import org.movieHub._nextGenEdition._reincarnated._controller._nodes.SelectedShowNode;
import org.movieHub._nextGenEdition._reincarnated._controller._nodes.StreamNode;
import org.movieHub._nextGenEdition._reincarnated._custom.Assistant;
import org.movieHub._nextGenEdition._reincarnated._model._enum.EntertainmentType;
import org.movieHub._nextGenEdition._reincarnated._model._enum.LoadPurpose;
import org.movieHub._nextGenEdition._reincarnated._model._enum.LoadStatus;
import org.movieHub._nextGenEdition._reincarnated._model._enum.OperatingSystem;
import org.movieHub._nextGenEdition._reincarnated._model._object.History;
import org.movieHub._nextGenEdition._reincarnated._model._object.Load;
import org.movieHub._nextGenEdition._reincarnated._model._object.Show;
import org.movieHub._nextGenEdition._reincarnated._model._object.StreamLoad;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.*;

/**
 * @author Mandela aka puumInc
 */

public class Controller extends Assistant implements Initializable {

    private File recentFolder = get_slash_for_my_os().equals(OperatingSystem.WINDOWS.getSlash()) ? null : Main.RESOURCE_PATH.getParentFile();

    @FXML
    private StackPane aboutPage;

    @FXML
    private StackPane historyPage;

    @FXML
    private VBox historyBox;

    @FXML
    private StackPane emailPage;

    @FXML
    private JFXTextField senderEmailTF;

    @FXML
    private JFXTextArea emailMessageTA;

    @FXML
    private StackPane theaterPage;

    @FXML
    private VBox streamBox;

    @FXML
    private JFXRadioButton onRB;

    @FXML
    private ToggleGroup serverStatusTG;

    @FXML
    private StackPane bridgePane;

    @FXML
    private StackPane jobsPane;

    @FXML
    private VBox jobBox;

    @FXML
    private StackPane questionPane;

    @FXML
    private HBox optionsBox;

    @FXML
    private StackPane homePane;

    @FXML
    private VBox selectedFileAndFolderBox;

    @FXML
    private VBox menuVBox;

    @FXML
    void add_dragged_files(DragEvent dragEvent) {
        try {
            if (dragEvent.getGestureSource() != dragEvent && dragEvent.getDragboard().hasFiles()) {
                /* allow for both copying and moving, whatever user chooses */
                dragEvent.acceptTransferModes(TransferMode.COPY_OR_MOVE);

                /* let the source know whether the string was successfully
                 * transferred and used
                 * */
                Dragboard dragboard = dragEvent.getDragboard();
                boolean itsComplete = dragboard.hasFiles();
                dragEvent.setDropCompleted(itsComplete);
            }

        } catch (Exception e) {
            e.printStackTrace();
            new Thread(write_stack_trace(e)).start();
            programmer_error(e).show();
        }
        dragEvent.consume();
    }

    @FXML
    void change_running_status_of_the_server(ActionEvent event) {
        if (serverStatusTG.getSelectedToggle().equals(onRB)) {
            try {
                SERVER.start(false);
                String webUrl = SERVER.get_web_url();
                String slashForMyOs = get_slash_for_my_os();
                if (slashForMyOs != null && slashForMyOs.equals(OperatingSystem.WINDOWS.getSlash())) {
                    Desktop.getDesktop().browse(URI.create(webUrl));
                } else {
                    Platform.runLater(() -> success_notification("Server has been started").show());
                }
                load_runnable_into_a_thread(start_my_ip_server_app()).start();
            } catch (Exception e) {
                e.printStackTrace();
                new Thread(write_stack_trace(e)).start();
                Platform.runLater(() -> programmer_error(e).show());
            }
        } else {
            SERVER.terminate();
            if (Main.process != null) {
                Main.process.destroy();
            }
            Platform.runLater(() -> success_notification("Server has been Stopped").show());
        }
        event.consume();
    }

    @FXML
    void check_files_being_dragged(DragEvent dragEvent) {
        try {
            Dragboard dragboard = dragEvent.getDragboard();
            if (!dragboard.hasFiles()) {
                dragEvent.consume();
                return;
            }
            List<File> dragBoardFiles = dragboard.getFiles();
            if (dragBoardFiles != null) {
                List<File> fileList = new ArrayList<>();
                List<File> folderList = new ArrayList<>();
                for (File draggedFileOrFolder : dragBoardFiles) {
                    if (draggedFileOrFolder.isFile()) {
                        if (can_be_used_by_vlc(draggedFileOrFolder)) {
                            fileList.add(draggedFileOrFolder);
                        }
                    } else if (draggedFileOrFolder.isDirectory()) {
                        if (!the_file_or_folder_has_zero_bytes(draggedFileOrFolder)) {
                            folderList.add(draggedFileOrFolder);
                        }
                    }
                }
                fileList.addAll(folderList);
                handle_selected_shows(false, fileList.toArray(new File[]{}));
                dragboard.clear();
            }
        } catch (Exception e) {
            e.printStackTrace();
            new Thread(write_stack_trace(e)).start();
            programmer_error(e).show();
        }
        dragEvent.consume();
    }

    @FXML
    void choose_file(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(this.recentFolder);
        String[] extensions = EXTENSIONS_SUPPORTED_BY_VLC.replace(":", ":*").split(":");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Video", extensions));
        List<File> fileList = fileChooser.showOpenMultipleDialog(Main.stage);
        if (fileList != null) {
            handle_selected_shows(true, fileList.toArray(new File[]{}));
        }
        event.consume();
    }

    @FXML
    void choose_folder(ActionEvent event) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setInitialDirectory(this.recentFolder);
        File folder = directoryChooser.showDialog(Main.stage);
        if (folder != null) {
            if (the_file_or_folder_has_zero_bytes(folder)) {
                warning_message("Halted!", "The selected folder does not have desired shows").show();
            } else {
                handle_selected_shows(true, folder);
            }
        }
        event.consume();
    }

    @FXML
    void continue_with_selected_shows(ActionEvent event) {
        if (SHOW_SET.isEmpty() && jobBox.getChildren().isEmpty()) {
            warning_message("Halted!", "Select at-least one show to continue!").show();
        } else {
            show_target_pane(bridgePane);
        }
        event.consume();
    }

    @FXML
    void go_back_to_home_page(ActionEvent event) {
        show_home_page(event);
        event.consume();
    }

    @FXML
    void go_back_to_select_an_action(ActionEvent event) {
        show_desired_subPane(questionPane, jobsPane, questionPane);
        event.consume();
    }

    @FXML
    void make_selected_shows_streamable(ActionEvent event) {
        if (SHOW_SET.isEmpty()) {
            warning_message("Halted!", "Select at-least another new show to continue!").show();
        } else {
            Load load = new Load();
            load.setShowList(new ArrayList<>(SHOW_SET));
            load.setSourceSize(SHOW_SET.stream().mapToDouble(value -> get_size_of_the_show_sources(value.getSource())).sum());

            History history = new History();
            history.setDate(get_date());
            history.setTimeWhenItStarted(get_time());
            history.setLoad(load);
            history.setLoadPurpose(LoadPurpose.STREAMING);

            if (list_is_written_to_jsonFile(createStreamLoad(), STREAM_LOAD_JF)) {
                history.setLoadStatus(LoadStatus.COMPLETE);

                load_task_into_a_thread(display_streaming_shows()).start();

                reset_home();

                success_notification("The shows are now streamable!").show();
                show_home_page(event);
            } else {
                error_message("Failed!", "Show streams have not being saved").show();
                history.setLoadStatus(LoadStatus.INCOMPLETE);
            }

            load_task_into_a_thread(write_log(history)).start();
        }
        event.consume();
    }

    @FXML
    void reload_history(ActionEvent event) {
        load_runnable_into_a_thread(display_most_recent_history(create_history_list(get_jsonArray_from_file(HISTORY_JF)))).start();
        event.consume();
    }

    @FXML
    void save_copy_to_removableDrive(ActionEvent event) {
        if (!SHOW_SET.isEmpty()) {
            String textFromDynamicTextInputDialog = get_text_from_dynamic_textInput_dialog("Load Tagging", "You are required to name the tag to continue", "Enter Tag name", null);
            if (textFromDynamicTextInputDialog == null) {
                error_message("Failed!", "This load is nameless, please retry").show();
            } else {
                double loadSize = SHOW_SET.stream().mapToDouble(value -> get_size_of_the_show_sources(value.getSource())).sum();
                if (the_user_wants_to_start_copying(textFromDynamicTextInputDialog, make_bytes_more_presentable(loadSize))) {
                    DirectoryChooser directoryChooser = new DirectoryChooser();
                    directoryChooser.setInitialDirectory(this.recentFolder);
                    directoryChooser.setTitle("Choose the destination folder");
                    File destinationFolder = directoryChooser.showDialog(Main.stage);
                    if (destinationFolder != null) {
                        if (destinationFolder.canRead() && destinationFolder.canWrite()) {
                            Load load = new Load();
                            load.setName(textFromDynamicTextInputDialog);
                            load.setDestinationFolder(destinationFolder);
                            load.setShowList(new ArrayList<>(SHOW_SET));
                            load.setSourceSize(loadSize);

                            show_desired_subPane(jobsPane, questionPane, jobsPane);

                            try {
                                JobNode.load = load;
                                Node node = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/_fxml/jobNode.fxml")));
                                Platform.runLater(() -> {
                                    jobBox.getChildren().add(node);
                                    new SlideInUp(node).setDelay(Duration.seconds(0.6)).play();
                                });

                                reset_home();

                            } catch (Exception e) {
                                e.printStackTrace();
                                new Thread(write_stack_trace(e)).start();
                                programmer_error(e).show();
                            } finally {
                                JobNode.load = null;
                            }
                        } else {
                            error_message("Failed!", "The destination folder can not be witten to nor read, please choose a different directory").show();
                        }
                    } else {
                        error_message("Failed!", "No destination folder has been chosen, please retry").show();
                    }
                } else {
                    warning_message("Halted!", "The user has put this load on hold").show();
                }
            }
        } else if (jobBox.getChildren().isEmpty()) {
            warning_message("Halted!", "Select at-least another new show to continue!").show();
        } else {
            show_desired_subPane(jobsPane, questionPane, jobsPane);
        }
        event.consume();
    }

    @FXML
    void send_mail_to_developer(ActionEvent event) {
        if (senderEmailTF.getText().trim().length() == 0 || senderEmailTF.getText() == null) {
            empty_and_null_pointer_message(senderEmailTF).show();
            event.consume();
            return;
        }
        if (!email_is_in_correct_format(senderEmailTF.getText().trim())) {
            error_message("Bad email!", "Kindly ensure that the email you have provided is in the correct formant").show();
            event.consume();
            return;
        }
        if (emailMessageTA.getText().trim().length() == 0 || emailMessageTA.getText() == null) {
            empty_and_null_pointer_message(emailMessageTA).show();
            event.consume();
            return;
        }
        information_message("Please wait...");
        final Task<Boolean> task = send_email(senderEmailTF.getText().trim(), emailMessageTA.getText().trim());
        task.setOnSucceeded(event1 -> {
            if (task.getValue()) {
                senderEmailTF.clear();
                emailMessageTA.clear();
                success_notification("Message has been Sent").show();
            } else {
                error_message("Failed!", "Your message was NOT sent").show();
            }
        });
        task.setOnFailed(event1 -> error_message("Failed!", "Your message was NOT sent").show());
        load_task_into_a_thread(task).start();
        event.consume();
    }

    @FXML
    void show_about_page(ActionEvent event) {
        hide_menu();
        show_target_pane(aboutPage);
        event.consume();
    }

    @FXML
    void show_email_page(ActionEvent event) {
        hide_menu();
        show_target_pane(emailPage);
        event.consume();
    }

    @FXML
    void show_history_page(ActionEvent event) {
        hide_menu();
        show_target_pane(historyPage);
        event.consume();
    }

    @FXML
    void show_home_page(ActionEvent event) {
        hide_menu();
        show_target_pane(homePane);
        event.consume();
    }

    @FXML
    void show_menu_options(ActionEvent event) {
        show_menu();
        event.consume();
    }

    @FXML
    void show_theater_page(ActionEvent event) {
        hide_menu();
        show_target_pane(theaterPage);
        event.consume();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        build_skeleton().run();
        load_task_into_a_thread(breath_of_life()).start();
    }

    private Task<Object> breath_of_life() {
        return new Task<Object>() {
            @Override
            protected Object call() {
                load_task_into_a_thread(display_streaming_shows()).start();
                reload_history(new ActionEvent());
                return null;
            }
        };
    }

    private Runnable build_skeleton() {
        return () -> {
            optionsBox.getChildren().stream().filter(node -> node instanceof JFXButton).map(node -> (JFXButton) node).forEach(jfxButton -> {
                Pulse pulse = new Pulse(jfxButton);
                jfxButton.setOnMouseEntered(event -> pulse.play());
                jfxButton.setOnMouseExited(event -> pulse.stop());
            });

            hide_menu();

            senderEmailTF.textProperty().addListener(((observable, oldValue, newValue) -> {
                if (email_is_in_correct_format(newValue)) {
                    senderEmailTF.setStyle("-fx-text-fill : rgb(255, 255, 255); -jfx-unfocus-color :  rgb(255, 255, 255); -jfx-focus-color : linear-gradient(#FFB900, #F0D801);");
                } else {
                    senderEmailTF.setStyle("-fx-text-fill : rgb(241, 58, 58); -jfx-unfocus-color :  rgb(255, 255, 255); -jfx-focus-color : linear-gradient(#FFB900, #F0D801);");
                }
            }));
        };
    }

    private Runnable display_most_recent_history(Map<LocalDate, List<History>> localDateListMap) {
        return () -> {
            if (!localDateListMap.isEmpty()) {
                clear_tabular_display(historyBox);
            }

            int count = 7;
            for (Map.Entry<LocalDate, List<History>> entry : localDateListMap.entrySet()) {
                if (count == 0) {
                    break;
                }
                List<History> historyList = entry.getValue();
                try {
                    OperationalDayNode.histories = historyList;
                    Node node = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/_fxml/operationalDayNode.fxml")));
                    Platform.runLater(() -> historyBox.getChildren().add(node));
                } catch (IOException e) {
                    e.printStackTrace();
                    new Thread(write_stack_trace(e)).start();
                    Platform.runLater(() -> programmer_error(e).show());
                    break;
                }
                --count;
            }
            OperationalDayNode.histories = null;
        };
    }

    private Map<LocalDate, List<History>> create_history_list(JsonArray historyAsJsonArray) {
        HashMap<LocalDate, List<History>> unorderedMap = new HashMap<>();
        historyAsJsonArray.forEach(jsonElement -> {
            History history = new Gson().fromJson(jsonElement, History.class);
            LocalDate localDate = LocalDate.parse(history.getDate(), DateTimeFormatter.ofPattern("dd-MMM-yyyy"));
            if (unorderedMap.containsKey(localDate)) {
                unorderedMap.get(localDate).add(history);
            } else {
                List<History> historyList = new ArrayList<>();
                historyList.add(history);
                unorderedMap.put(localDate, historyList);
            }
        });
        return new TreeMap<>(unorderedMap).descendingMap();
    }

    private Task<Object> display_streaming_shows() {
        return new Task<Object>() {
            @Override
            protected Object call() throws IOException {
                STREAM_LOADS.clear();
                clear_tabular_display(streamBox);

                STREAM_LOADS.addAll(get_streamLoads_from_file());
                for (StreamLoad streamLoad : STREAM_LOADS) {
                    try {
                        display_streaming_load(streamLoad);
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
                return null;
            }
        };
    }

    private void reset_home() {
        SHOW_SET.clear();
        this.recentFolder = get_slash_for_my_os().equals(OperatingSystem.WINDOWS.getSlash()) ? null : Main.RESOURCE_PATH.getParentFile();

        clear_tabular_display(selectedFileAndFolderBox);
    }

    private void handle_selected_shows(boolean showNotification, File... files) {
        if (files != null) {
            String[] defaultHeader = new String[]{"The following have been accepted:\n\n", "The following have been rejected:\n\n", "The following are unreadable:\n\n"};
            StringBuilder acceptedFiles = new StringBuilder(defaultHeader[0]);
            StringBuilder rejectedFiles = new StringBuilder(defaultHeader[1]);
            StringBuilder unreadableFiles = new StringBuilder(defaultHeader[2]);
            int[] count = new int[3];
            for (File file : files) {
                Show show = new Show();
                show.setSource(file);
                show.setEntertainmentType(file.isFile() ? EntertainmentType.MOVIE : EntertainmentType.SERIES);
                if (file.canRead()) {
                    if (SHOW_SET.add(show)) {
                        ++count[0];
                        acceptedFiles.append(String.format("%d. %s\n", count[0], file.getName()));
                        try {
                            display_selected_show(show);
                        } catch (Exception e) {
                            e.printStackTrace();
                            new Thread(write_stack_trace(e)).start();
                            Platform.runLater(() -> programmer_error(e).show());
                            break;
                        }
                    } else {
                        ++count[1];
                        rejectedFiles.append(String.format("%d. %s\n", count[1], file.getName()));
                    }
                } else {
                    ++count[2];
                    unreadableFiles.append(String.format("%d. %s\n", count[2], file.getName()));
                }
                this.recentFolder = file.getParentFile();
            }
            if (showNotification) {
                acceptedFiles.trimToSize();
                rejectedFiles.trimToSize();
                unreadableFiles.trimToSize();
                if (!acceptedFiles.toString().equals(defaultHeader[0])) {
                    Platform.runLater(() -> information_message(acceptedFiles.toString()));
                }
                if (!rejectedFiles.toString().equals(defaultHeader[1])) {
                    Platform.runLater(() -> information_message(rejectedFiles.toString()));
                }
                if (!unreadableFiles.toString().equals(defaultHeader[2])) {
                    Platform.runLater(() -> information_message(unreadableFiles.toString()));
                }
            }
        }
    }

    private void display_streaming_load(StreamLoad streamLoad) throws IOException {
        StreamNode.streamLoad = streamLoad;
        Node node = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/_fxml/streamNode.fxml")));
        Platform.runLater(() -> {
            streamBox.getChildren().add(node);
            new SlideInRight(node).play();
        });
        StreamNode.streamLoad = null;
    }

    private void display_selected_show(Show show) throws IOException {
        SelectedShowNode.index = SHOW_SET.size();
        SelectedShowNode.show = show;
        Node node = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/_fxml/selectedShowNode.fxml")));
        Platform.runLater(() -> {
            selectedFileAndFolderBox.getChildren().add(node);
            new SlideInRight(node).play();
        });
        SelectedShowNode.show = null;
        SelectedShowNode.index = 0;
    }

    private Task<Boolean> send_email(String receiverEmail, String text) {
        return new Task<Boolean>() {
            @Override
            protected Boolean call() {
                if (send_automatic_reply_to_user(receiverEmail)) {
                    inform_developer(receiverEmail, text);
                    return true;
                }
                return false;
            }
        };
    }

    private void show_desired_subPane(Node desiredPane, Node... nodes) {
        for (Node node : nodes) {
            if (node.getOpacity() > 0) {
                new FadeOut(node).play();
            }
        }
        if (desiredPane.getOpacity() < 1) {
            desiredPane.toFront();
            new FadeIn(desiredPane).setDelay(Duration.seconds(0.6)).play();
        }
    }

    private void show_target_pane(StackPane stackPane) {
        hide_the_current_pane(stackPane);
        if (stackPane.getOpacity() < 1) {
            stackPane.toFront();
            new FadeInRight(stackPane).play();
        }
    }

    private void hide_the_current_pane(StackPane excludedPane) {
        StackPane[] stackPanes = new StackPane[]{homePane, bridgePane, theaterPage, emailPage, historyPage, aboutPage};
        for (StackPane stackPane : stackPanes) {
            if (!stackPane.equals(excludedPane)) {
                if (stackPane.getOpacity() > 0) {
                    new FadeOutLeft(stackPane).play();
                }
            }
        }
    }

    private void hide_menu() {
        if (menuVBox.getOpacity() > 0) {
            menuVBox.toBack();
            new FadeOutRight(menuVBox).play();
        }
    }

    private void show_menu() {
        if (menuVBox.getOpacity() < 1) {
            menuVBox.toFront();
            new FadeInRight(menuVBox).play();
        } else {
            hide_menu();
        }
    }

}