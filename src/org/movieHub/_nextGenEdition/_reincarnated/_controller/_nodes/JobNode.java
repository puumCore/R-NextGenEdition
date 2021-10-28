package org.movieHub._nextGenEdition._reincarnated._controller._nodes;

import com.jfoenix.controls.JFXProgressBar;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.movieHub._nextGenEdition._reincarnated._custom.Assistant;
import org.movieHub._nextGenEdition._reincarnated._custom.LoadTransit;
import org.movieHub._nextGenEdition._reincarnated._model._enum.LoadPurpose;
import org.movieHub._nextGenEdition._reincarnated._model._enum.LoadStatus;
import org.movieHub._nextGenEdition._reincarnated._model._object.History;
import org.movieHub._nextGenEdition._reincarnated._model._object.Load;
import org.movieHub._nextGenEdition._reincarnated._model._object.Show;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Mandela aka puumInc
 */
public class JobNode extends Assistant implements Initializable {

    public static Load load;

    private final LoadTransit myLoadTransit = new LoadTransit();
    private final History myHistory = new History();
    private Boolean isStopped = false;
    private Load myLoad;

    @FXML
    private Label tagNameLbl;

    @FXML
    private Label sizeDifferenceLbl;

    @FXML
    private JFXProgressBar taskStatusBar;

    @FXML
    void cancel(ActionEvent event) {
        this.isStopped = true;
        event.consume();
    }

    @FXML
    void show_task_files(ActionEvent event) {
        String stringFromListOfShowNames = generate_a_string_from_list_of_showNames(get_file_names(this.myLoad.getShowList().toArray(new Show[]{})));
        get_list_alert(String.format("%s has the following shows: ", this.myLoad.getName()), stringFromListOfShowNames).show();
        event.consume();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.myLoad = JobNode.load;
        tagNameLbl.setText(this.myLoad.getName());

        taskStatusBar.setProgress(0);

        this.myLoadTransit.setStopped(this.isStopped);
        this.myLoadTransit.setLoad(this.myLoad);
        this.myLoadTransit.setOnSucceeded(event -> {
            this.myHistory.setTimeWhenItStarted(this.myLoadTransit.getTimeItStarted());
            this.myHistory.setTimeWhenItStopped(this.myLoadTransit.getValue());
            this.myHistory.setLoadStatus(LoadStatus.COMPLETE);
            load_task_into_a_thread(write_log(this.myHistory)).start();

            VBox jobBox = get_parent_VBox(tagNameLbl, "jobBox");
            if (jobBox != null) {
                remove_child_from_Vbox(jobBox, tagNameLbl.getParent().getParent().getParent());
            }

            success_notification(String.format("%s has been copied!", this.myLoad.getName())).show();
        });
        this.myLoadTransit.setOnCancelled(event -> {
            this.myHistory.setTimeWhenItStarted(this.myLoadTransit.getTimeItStarted());
            this.myHistory.setTimeWhenItStopped(get_time());
            this.myHistory.setLoadStatus(LoadStatus.INCOMPLETE);
            load_task_into_a_thread(write_log(this.myHistory)).start();
            warning_message("Halted!", String.format("%s has been triggered to STOP", this.myLoad.getName())).show();
            VBox jobBox = get_parent_VBox(tagNameLbl, "jobBox");
            if (jobBox != null) {
                remove_child_from_Vbox(jobBox, tagNameLbl.getParent().getParent().getParent());
            }
        });

        this.myLoadTransit.setOnFailed(event -> {
            this.myHistory.setTimeWhenItStarted(this.myLoadTransit.getTimeItStarted());
            this.myHistory.setTimeWhenItStopped(get_time());
            this.myHistory.setLoadStatus(LoadStatus.STOPPED);
            load_task_into_a_thread(write_log(this.myHistory)).start();
        });
        this.myLoadTransit.exceptionProperty().addListener(((observable, oldValue, newValue) -> {
            if (newValue != null) {
                this.myHistory.setTimeWhenItStarted(this.myLoadTransit.getTimeItStarted());
                this.myHistory.setTimeWhenItStopped(get_time());
                this.myHistory.setLoadStatus(LoadStatus.SYSTEM_ERROR);
                load_task_into_a_thread(write_log(this.myHistory)).start();

                Exception exception = (Exception) newValue;
                exception.printStackTrace();
                new Thread(write_stack_trace(exception)).start();
                Platform.runLater(() -> programmer_error(exception).show());
            }
        }));
        new Thread(this.myLoadTransit).start();

        this.myHistory.setDate(get_date());
        this.myHistory.setLoad(this.myLoad);
        this.myHistory.setLoadPurpose(LoadPurpose.REMOVABLE_DRIVE);

        taskStatusBar.progressProperty().unbind();
        Task<Object> copyProgress = copy_progress(this.myLoad.getDestinationFolder());
        copyProgress.setOnSucceeded(event -> {
            taskStatusBar.progressProperty().unbind();
            taskStatusBar.setProgress(0);
        });
        copyProgress.setOnCancelled(event -> {
            this.myLoadTransit.setStopped(this.isStopped);
            this.myLoadTransit.cancel(true);
        });
        taskStatusBar.progressProperty().bind(copyProgress.progressProperty());
        load_task_into_a_thread(copyProgress).start();
    }

    private Task<Object> copy_progress(File destinationFolder) {
        double loadSizeAsMB = ((this.myLoad.getSourceSize() / 1024) / 1024);
        AtomicReference<Double> bytesCopied = new AtomicReference<>(0.0);
        return new Task<Object>() {
            @Override
            protected Object call() {
                double megabytes = 0;
                while (megabytes < loadSizeAsMB) {
                    if (JobNode.this.isStopped) {
                        updateMessage("Stopping!");
                        this.cancel(true);
                        break;
                    }
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        if (JobNode.this.isStopped) {
                            updateMessage("Stopping!");
                            this.cancel(true);
                        } else {
                            bytesCopied.set(get_amount_of_load_copied(destinationFolder));
                            double kilobytes = (bytesCopied.get() / 1024);
                            megabytes = (kilobytes / 1024);
                            updateProgress(megabytes, loadSizeAsMB);
                            Platform.runLater(() -> sizeDifferenceLbl.setText(String.format("Copying %s of %s", make_bytes_more_presentable(bytesCopied.get()), make_bytes_more_presentable(JobNode.this.myLoad.getSourceSize()))));
                        }
                    }
                }
                return null;
            }
        };
    }

    private double get_amount_of_load_copied(File destinationFolder) {
        AtomicReference<Double> bytesCopied = new AtomicReference<>(0.0);
        for (Show show : this.myLoad.getShowList()) {
            File target = new File(format_path_name_to_current_os(String.format("%s\\%s", destinationFolder.getAbsolutePath(), show.getSource().getName())));
            if (target.exists()) {
                bytesCopied.getAndUpdate(v -> (v + get_actual_size_of_the_file_or_folder(target)));
            }
        }
        return bytesCopied.get();
    }

}
