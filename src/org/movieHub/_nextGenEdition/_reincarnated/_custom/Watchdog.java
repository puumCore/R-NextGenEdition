package org.movieHub._nextGenEdition._reincarnated._custom;

import animatefx.animation.Shake;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;
import org.movieHub._nextGenEdition._reincarnated.Main;
import org.movieHub._nextGenEdition._reincarnated._api.Server;
import org.movieHub._nextGenEdition._reincarnated._api.ShowStreamer;
import org.movieHub._nextGenEdition._reincarnated._model._enum.OperatingSystem;
import org.movieHub._nextGenEdition._reincarnated._model._object.History;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;

/**
 * @author Mandela aka puumInc
 */

public abstract class Watchdog {

    protected static final Server SERVER = new ShowStreamer();
    public final String SECURE_JKS = Main.RESOURCE_PATH.getAbsolutePath().concat("\\_config\\_secure\\nextGenEdition");
    protected final String STREAM_LOAD_JF = Main.RESOURCE_PATH.getAbsolutePath().concat("\\_config\\streamLoads.json");
    private final String PATH_TO_INFO_FOLDER = Main.RESOURCE_PATH.getAbsolutePath().concat("\\_watchDog\\_log");
    protected final String HISTORY_JF = PATH_TO_INFO_FOLDER.concat("\\_advanced\\history.json");

    @SuppressWarnings("SameParameterValue")
    protected final String get_text_from_dynamic_textInput_dialog(String title, String header, String contextOfInput, String defaultValue) {
        TextInputDialog textInputDialog = new TextInputDialog(defaultValue);
        textInputDialog.initOwner(Main.stage);
        textInputDialog.setTitle(title);
        textInputDialog.setHeaderText(header);
        textInputDialog.setContentText(contextOfInput.concat(": "));
        Optional<String> userChoice = textInputDialog.showAndWait();
        return userChoice.orElse(null);
    }

    protected final Task<Object> write_log(String loggableInfo) {
        return new Task<Object>() {
            @Override
            protected Object call() {
                write_a_basic_activity(loggableInfo);
                return null;
            }
        };
    }

    protected final Task<Object> write_log(History history) {
        return new Task<Object>() {
            @Override
            protected Object call() {
                log_history(history);
                return null;
            }
        };
    }

    private void write_a_basic_activity(String info) {
        BufferedWriter bw = null;
        try {
            File log = new File(format_path_name_to_current_os(PATH_TO_INFO_FOLDER.concat("\\_basic\\".concat(gate_date_for_file_name()).concat(".txt"))));
            if (!log.exists()) {
                Files.write(log.toPath(), String.format("This is a newly created file [ %s ]\n\n", time_stamp()).getBytes(StandardCharsets.UTF_8), StandardOpenOption.WRITE, StandardOpenOption.CREATE_NEW);
            }
            if (log.canRead() && log.canWrite()) {
                FileWriter fw = new FileWriter(log, true);
                bw = new BufferedWriter(fw);
                bw.write("\n" + info);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            programmer_error(ex).show();
        } finally {
            try {
                if (bw != null) {
                    bw.close();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                programmer_error(ex).show();
            }
        }
    }

    private void log_history(History history) {
        try {
            final File file = new File(format_path_name_to_current_os(HISTORY_JF));
            if (file.exists()) {
                final JsonArray jsonArray = get_jsonArray_from_file(file.getAbsolutePath());
                jsonArray.add(new Gson().toJsonTree(history, History.class));
                FileWriter fileWriter = new FileWriter(file);
                fileWriter.write(new Gson().toJson(jsonArray));
                fileWriter.close();
            } else {
                FileWriter fileWriter = new FileWriter(file);
                final JsonArray jsonArray = new JsonArray();
                jsonArray.add(new Gson().toJsonTree(history, History.class));
                fileWriter.write(new Gson().toJson(jsonArray));
                fileWriter.close();
            }

        } catch (IOException ex) {
            ex.printStackTrace();
            programmer_error(ex).show();
        }
    }

    protected final JsonArray get_jsonArray_from_file(String pathToJsonFile) {
        final JsonArray jsonArray = new JsonArray();
        try {
            final BufferedReader bufferedReader = new BufferedReader(new FileReader(format_path_name_to_current_os(pathToJsonFile)));
            jsonArray.addAll(new Gson().fromJson(bufferedReader, JsonArray.class));
            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
            new Thread(write_stack_trace(e)).start();
        }
        return jsonArray;
    }

    protected final <T> boolean list_is_written_to_jsonFile(List<T> list, String pathToJsonFile) {
        try {
            File file = new File(format_path_name_to_current_os(pathToJsonFile));
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(new Gson().toJson(list));
            fileWriter.close();
            fileWriter.close();
            return true;
        } catch (IOException ex) {
            ex.printStackTrace();
            Platform.runLater(() -> programmer_error(ex).show());
        }
        return false;
    }

    public final Runnable write_stack_trace(Exception exception) {
        return () -> {
            BufferedWriter bw = null;
            try {
                final String PATH_TO_ERROR_FOLDER = Main.RESOURCE_PATH.getAbsolutePath().concat("\\_watchDog\\_error\\");
                File log = new File(format_path_name_to_current_os(PATH_TO_ERROR_FOLDER.concat(gate_date_for_file_name().concat(" stackTrace_log.txt"))));
                if (!log.exists()) {
                    Files.write(log.toPath(), String.format("This is a newly created file [ %s ]\n\n", time_stamp()).getBytes(StandardCharsets.UTF_8), StandardOpenOption.WRITE, StandardOpenOption.CREATE_NEW);
                }
                if (log.canWrite() & log.canRead()) {
                    FileWriter fw = new FileWriter(log, true);
                    bw = new BufferedWriter(fw);
                    StringWriter stringWriter = new StringWriter();
                    PrintWriter printWriter = new PrintWriter(stringWriter);
                    exception.printStackTrace(printWriter);
                    String exceptionText = stringWriter.toString();
                    bw.write("\n ##################################################################################################"
                            + " \n " + time_stamp()
                            + "\n " + exceptionText
                            + "\n\n");
                }
            } catch (IOException ex) {
                ex.printStackTrace();
                programmer_error(ex).show();
            } finally {
                try {
                    if (bw != null) {
                        bw.close();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    programmer_error(ex).show();
                }
            }
        };
    }

    public final Alert programmer_error(Exception exception) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.initOwner(Main.stage);
        alert.setTitle("WATCH DOG");
        alert.setHeaderText("ERROR TYPE : " + exception.getClass());
        alert.setContentText("This dialog is a detailed explanation of the error that has occurred");
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        exception.printStackTrace(printWriter);
        String exceptionText = stringWriter.toString();
        Label label = new Label("The exception stacktrace was: ");
        TextArea textArea = new TextArea(exceptionText);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        VBox vBox = new VBox();
        vBox.getChildren().add(label);
        vBox.getChildren().add(textArea);
        alert.getDialogPane().setExpandableContent(vBox);
        return alert;
    }

    private String gate_date_for_file_name() {
        return get_date().replaceAll("-", " ");
    }

    protected final String time_stamp() {
        return get_date() + " at " + get_time();
    }

    protected final String get_time() {
        return new SimpleDateFormat("HH:mm:ss:SSS").format(Calendar.getInstance().getTime());
    }

    protected final String get_date() {
        return new SimpleDateFormat("dd-MMM-yyyy").format(Calendar.getInstance().getTime());
    }

    protected final void information_message(String message) {
        warning_message("Information", "\n".concat(message))
                .hideAfter(Duration.seconds(12))
                .graphic(null)
                .position(Pos.BOTTOM_RIGHT)
                .show();
    }

    public final String format_path_name_to_current_os(String myPath) {
        String myOperatingSystemSlash = get_slash_for_my_os();
        if (myOperatingSystemSlash != null) {
            if (!myOperatingSystemSlash.equals(OperatingSystem.WINDOWS.getSlash())) {
                myPath = myPath.replace(OperatingSystem.WINDOWS.getSlash(), myOperatingSystemSlash);
            }
        }
        return myPath;
    }

    protected final String get_slash_for_my_os() {
        String OS = System.getProperty("os.name").toLowerCase();
        return Arrays.stream(OperatingSystem.values()).filter(operatingSystem -> OS.contains(operatingSystem.getOs())).findFirst().orElse(OperatingSystem.NIX).getSlash();
    }

    protected final Notifications success_notification(String about) {
        return Notifications.create()
                .title("Success")
                .text(about)
                .position(Pos.TOP_LEFT)
                .hideAfter(Duration.seconds(5))
                .graphic(new ImageView(new Image("/_images/_icon/icons8_Ok_48px.png")));
    }

    protected final Notifications error_message(String title, String text) {
        Image image = new Image("/_images/_icon/icons8_Close_Window_48px.png");
        return Notifications.create()
                .title(title)
                .text(text)
                .graphic(new ImageView(image))
                .hideAfter(Duration.seconds(8))
                .position(Pos.TOP_RIGHT);
    }

    protected final Notifications warning_message(String title, String text) {
        Image image = new Image("/_images/_icon/icons8_Error_48px.png");
        return Notifications.create()
                .title(title)
                .text(text)
                .graphic(new ImageView(image))
                .hideAfter(Duration.seconds(8))
                .position(Pos.TOP_RIGHT);
    }

    protected final Notifications empty_and_null_pointer_message(Node node) {
        Image image = new Image("/_images/_icon/icons8_Error_48px.png");
        return Notifications.create()
                .title("Something is Missing")
                .text("Click Here to trace this Error.")
                .graphic(new ImageView(image))
                .hideAfter(Duration.seconds(8))
                .position(Pos.TOP_CENTER)
                .onAction(event -> {
                    new Shake(node).play();
                    node.requestFocus();
                });
    }

}
