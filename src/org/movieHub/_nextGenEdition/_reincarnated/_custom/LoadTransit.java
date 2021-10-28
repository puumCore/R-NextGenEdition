package org.movieHub._nextGenEdition._reincarnated._custom;

import javafx.application.Platform;
import javafx.concurrent.Task;
import org.movieHub._nextGenEdition._reincarnated._model._enum.EntertainmentType;
import org.movieHub._nextGenEdition._reincarnated._model._object.Load;
import org.movieHub._nextGenEdition._reincarnated._model._object.Show;

import java.io.*;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

/**
 * @author edgar
 */
public class LoadTransit extends Task<String> {

    private final Assistant assistant = new Assistant();
    private final String TIME_STARTED = assistant.get_time();
    private Load load;
    private Boolean isStopped;

    public String getTimeItStarted() {
        return TIME_STARTED;
    }

    public Load getLoad() {
        return load;
    }

    public void setLoad(Load load) {
        this.load = load;
    }

    public Boolean getStopped() {
        return isStopped;
    }

    public void setStopped(Boolean stopped) {
        isStopped = stopped;
    }

    /**
     * Invoked when the Task is executed, the call method must be overridden and
     * implemented by subclasses. The call method actually performs the
     * background thread logic. Only the updateProgress, updateMessage, updateValue and
     * updateTitle methods of Task may be called from code within this method.
     * Any other interaction with the Task from the background thread will result
     * in runtime exceptions.
     *
     * @return The result of the background work, if any.
     */
    @Override
    protected String call() {
        List<Show> showList = getLoad().getShowList();
        if (showList != null) {
            for (Show show : showList) {
                if (show.getEntertainmentType().equals(EntertainmentType.MOVIE)) {
                    if (this.getStopped()) {
                        cancel(true);
                        break;
                    }
                    copy_target_file(show.getSource(), getLoad().getDestinationFolder());
                } else {
                    copy_directory_tree(show.getSource(), getLoad().getDestinationFolder(), show.getSource().getName());
                    if (this.getStopped()) {
                        cancel(true);
                        break;
                    }
                }
            }
        }
        return assistant.get_time();
    }

    private void copy_target_file(File targetFile, File destinationFolder) {
        try {
            Path copy = duplicate_file_in_another_directory(targetFile, destinationFolder.toPath().resolve(targetFile.toPath().getFileName()).toFile());
            if (copy == null || (!copy.toFile().exists())) {
                Platform.runLater(() -> assistant.warning_message("Incomplete!", String.format("%s could not be copied!", targetFile.getName())).show());
            }
        } catch (Exception e) {
            e.printStackTrace();
            new Thread(assistant.write_stack_trace(e)).start();
            Platform.runLater(() -> assistant.programmer_error(e).show());
        }
    }

    private void copy_directory_tree(File baseFolder, File destinationFolder, String nameOfShowSource) {
        Path source = baseFolder.toPath();
        Path destination = destinationFolder.toPath();
        String defaultHeader = "The following could not be copied:\n";
        StringBuilder stringBuilder = new StringBuilder(defaultHeader);
        int[] count = new int[1];
        try {
            Files.walkFileTree(source, new FileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                    if (getStopped()) {
                        return FileVisitResult.TERMINATE;
                    }
                    if (!destinationFolder.exists() || !(destinationFolder.canRead() && destinationFolder.canWrite())) {
                        return FileVisitResult.TERMINATE;
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    if (getStopped()) {
                        return FileVisitResult.TERMINATE;
                    }
                    if (attrs.isSymbolicLink()) {
                        return FileVisitResult.CONTINUE;
                    } else if (!assistant.can_be_used_by_vlc(file.toFile())) {
                        return FileVisitResult.CONTINUE;
                    }
                    Path childFilePath = source.relativize(file);
                    File copy = new File(assistant.format_path_name_to_current_os(String.format("%s\\%s\\%s", destination, nameOfShowSource, childFilePath)));
                    File origin = new File(assistant.format_path_name_to_current_os(String.format("%s\\%s", source, childFilePath)));
                    if (!the_file_has_been_copied_to_your_desired_destination(origin, copy)) {
                        ++count[0];
                        stringBuilder.append(String.format("%d. %s\n", count[0], origin.getName()));
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) {
                    if (!destinationFolder.exists() || !(destinationFolder.canRead() && destinationFolder.canWrite())) {
                        return FileVisitResult.TERMINATE;
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
                    if (getStopped()) {
                        return FileVisitResult.TERMINATE;
                    }
                    if (!destinationFolder.exists() || !(destinationFolder.canRead() && destinationFolder.canWrite())) {
                        return FileVisitResult.TERMINATE;
                    }
                    return FileVisitResult.CONTINUE;
                }

            });

            if (!stringBuilder.toString().equals(defaultHeader)) {
                Platform.runLater(() -> assistant.information_message(stringBuilder.toString()));
            }
        } catch (Exception e) {
            e.printStackTrace();
            new Thread(assistant.write_stack_trace(e)).start();
            Platform.runLater(() -> assistant.programmer_error(e).show());
        }
    }

    private boolean the_file_has_been_copied_to_your_desired_destination(File from, File to) {
        return get_path_to_the_copied_file(from, to) != null;
    }

    private Path duplicate_file_in_another_directory(File sourceFile, File duplicateFile) {
        try {
            if (duplicateFile.exists() && duplicateFile.length() == sourceFile.length()) {
                return duplicateFile.toPath();
            } else {
                Files.deleteIfExists(duplicateFile.toPath());
            }
            DataOutputStream dataOutputStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(duplicateFile)));
            DataInputStream dataInputStream = new DataInputStream(new BufferedInputStream(new FileInputStream(sourceFile)));
            int count;
            byte[] buffer = new byte[(1024 * 2)];
            while (((count = dataInputStream.read(buffer)) > 0) && !this.isStopped) {
                dataOutputStream.write(buffer, 0, count);
            }
            dataInputStream.close();
            dataOutputStream.close();
            return duplicateFile.toPath();
        } catch (IOException e) {
            e.printStackTrace();
            new Thread(assistant.write_stack_trace(e)).start();
            Platform.runLater(() -> assistant.programmer_error(e).show());
        }
        return null;
    }

    private Path get_path_to_the_copied_file(File originalFile, File duplicateFile) {
        try {
            if (!duplicateFile.getParentFile().exists()) {
                if (!duplicateFile.getParentFile().mkdirs()) {
                    Platform.runLater(() -> assistant.error_message("Failed!", "Was not able duplicateFile create " + duplicateFile.getParentFile()).show());
                    return null;
                }
            }
            if (!duplicateFile.exists()) {
                if (!duplicateFile.createNewFile()) {
                    Platform.runLater(() -> assistant.error_message("Failed!", "Was not able duplicateFile create " + duplicateFile.getAbsolutePath()).show());
                    return null;
                }
            }
            return duplicate_file_in_another_directory(originalFile, duplicateFile);
        } catch (Exception e) {
            e.printStackTrace();
            new Thread(assistant.write_stack_trace(e)).start();
            Platform.runLater(() -> assistant.programmer_error(e).show());
        }
        return null;
    }
}
