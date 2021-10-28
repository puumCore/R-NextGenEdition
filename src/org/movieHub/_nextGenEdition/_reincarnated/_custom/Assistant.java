package org.movieHub._nextGenEdition._reincarnated._custom;

import animatefx.animation.SlideOutLeft;
import animatefx.animation.SlideOutRight;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.movieHub._nextGenEdition._reincarnated.Main;
import org.movieHub._nextGenEdition._reincarnated._model._enum.EntertainmentType;
import org.movieHub._nextGenEdition._reincarnated._model._enum.OperatingSystem;
import org.movieHub._nextGenEdition._reincarnated._model._object.Show;
import org.movieHub._nextGenEdition._reincarnated._model._object.ShowStream;
import org.movieHub._nextGenEdition._reincarnated._model._object.StreamLoad;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Mandela aka puumInc
 */

public class Assistant extends Email {

    public static final List<StreamLoad> STREAM_LOADS = new CopyOnWriteArrayList<>();
    protected static final Set<Show> SHOW_SET = new HashSet<>();
    public final String CONTEXT_PATH = "/movieHub/v2/nxtG";
    protected final String EXTENSIONS_FOR_MOVIES_ONLY = ".m4v:.mpeg1:.mpeg2:.flv:.mkv:.mov:.mpeg4:.vob:.avi:.mpeg:.m4a:.3gp:.mp4:.m4p";
    protected final String EXTENSIONS_SUPPORTED_BY_VLC = ".asx:.dts:.gxf:.m2v:.m3u:.m4v:.mpeg1:.mpeg2:.mts:.mxf:.pls:.divx:.dv:.flv:.m1v:.m2ts:.mkv:.mov:.mpeg4:.ts:.vlc:.vob:.3g2:.avi:.mpeg:.mpg:.m4a:.3gp:.srt:.wmv:.asf:.mp4:.m4p";

    protected final void clear_tabular_display(VBox vBox) {
        CopyOnWriteArrayList<Node> nodeCopyOnWriteArrayList = new CopyOnWriteArrayList<>(vBox.getChildren());
        nodeCopyOnWriteArrayList.forEach(node -> {
                    new SlideOutRight(node).play();
                    VBox.clearConstraints(node);
                    Platform.runLater(() -> vBox.getChildren().remove(node));
                }
        );
    }

    protected final String get_status_of_show_files(StreamLoad streamLoad) {
        int size = streamLoad.getShowStreamList().size();
        AtomicInteger showCount = new AtomicInteger(0);
        for (ShowStream showStream : streamLoad.getShowStreamList()) {
            File file = new File(showStream.getValue());
            if (file.exists() && file.canRead()) {
                showCount.incrementAndGet();
            }
        }
        if (showCount.get() < size) {
            int difference = size - showCount.get();
            return (difference == size) ? (size == 1) ? "Missing" : "All are missing" : String.format("%d out of %d are missing", difference, size);
        } else {
            return "";
        }
    }

    protected final List<StreamLoad> createStreamLoad() {
        List<String> streamKeys = get_stream_keys();
        List<StreamLoad> streamLoadList = new ArrayList<>(STREAM_LOADS);
        SHOW_SET.forEach(show -> {
            StreamLoad streamLoad = new StreamLoad();
            streamLoad.setName(format_showName_for_streaming(show.getSource().getName()));
            streamLoad.setKey(show.getEntertainmentType().equals(EntertainmentType.MOVIE) ? null : get_unique_key_for_list(streamKeys));
            streamLoad.setShowStreamList(create_then_get_new_show_streams(show.getSource(), streamKeys));

            streamLoadList.add(streamLoad);
        });
        return streamLoadList;
    }

    private List<ShowStream> create_then_get_new_show_streams(File fileSource, List<String> keys) {
        List<ShowStream> showStreamList = new ArrayList<>();
        String keyForList = get_unique_key_for_list(keys);
        keys.add(keyForList);
        if (fileSource.isFile()) {
            ShowStream showStream = new ShowStream();
            showStream.setKey(keyForList);
            showStream.setValue(fileSource.getAbsolutePath());

            showStreamList.add(showStream);
        } else if (fileSource.isDirectory()) {
            File[] listFiles = fileSource.listFiles();
            if (listFiles != null) {
                for (File file : listFiles) {
                    List<ShowStream> thenGetNewShowStreams = create_then_get_new_show_streams(file, keys);
                    if (thenGetNewShowStreams != null) {
                        showStreamList.addAll(thenGetNewShowStreams);
                    }
                }
            }
        }
        return showStreamList.isEmpty() ? null : showStreamList;
    }


    private List<String> get_stream_keys() {
        List<String> stringList = new ArrayList<>();
        Assistant.STREAM_LOADS.forEach(showStream -> {
            stringList.add(showStream.getKey());
            stringList.addAll(showStream.getShowStreamList().stream().map(ShowStream::getKey).collect(Collectors.toList()));
        });
        return stringList;
    }

    protected final List<StreamLoad> get_streamLoads_from_file() {
        JsonArray jsonArrayFromFile = get_jsonArray_from_file(STREAM_LOAD_JF);
        List<StreamLoad> streamLoadList = new ArrayList<>();
        Gson gson = new Gson();
        jsonArrayFromFile.forEach(jsonElement -> streamLoadList.add(gson.fromJson(jsonElement, StreamLoad.class)));
        return streamLoadList;
    }

    protected final boolean the_user_wants_to_start_copying(String tagName, String loadSize) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.initOwner(Main.stage);
        alert.setTitle(tagName);
        alert.setHeaderText("This load is ".concat(loadSize));
        alert.setContentText("Click \"Continue\" to start copying!");
        ButtonType continueBtn = new ButtonType("Continue");
        ButtonType cancelBtn = new ButtonType("Cancel");
        alert.getButtonTypes().clear();
        alert.getButtonTypes().addAll(continueBtn, cancelBtn);
        Optional<ButtonType> result = alert.showAndWait();
        return result.map(buttonType -> buttonType.equals(continueBtn)).orElse(false);
    }

    protected final Thread load_task_into_a_thread(Task<?> task) {
        task.exceptionProperty().addListener(((observable, oldValue, newValue) -> {
            if (newValue != null) {
                Exception exception = (Exception) newValue;
                exception.printStackTrace();
                new Thread(write_stack_trace(exception)).start();
                Platform.runLater(() -> programmer_error(exception).show());
            }
        }));
        return new Thread(task);
    }

    protected final Thread load_runnable_into_a_thread(Runnable runnable) {
        Task<?> task = new Task<Object>() {
            @Override
            protected Object call() {
                runnable.run();
                return null;
            }
        };
        return load_task_into_a_thread(task);
    }


    protected final void remove_child_from_Vbox(VBox vBox, Node targetNode) {
        CopyOnWriteArrayList<Node> copyOnWriteArrayList = new CopyOnWriteArrayList<>(vBox.getChildren());
        for (Node child : copyOnWriteArrayList) {
            if (child.equals(targetNode)) {
                VBox.clearConstraints(child);
                new SlideOutLeft(child).play();
                vBox.getChildren().remove(child);
                break;
            }
        }
    }

    protected final VBox get_parent_VBox(Node node, String parentName) {
        Node parentNode = node.getParent();
        if (parentNode != null) {
            if (parentNode instanceof VBox) {
                if (parentNode.getId() != null) {
                    if (parentNode.getId().equals(parentName)) {
                        return (VBox) parentNode;
                    }
                }
            }
            return get_parent_VBox(parentNode, parentName);
        }
        return null;
    }

    protected final boolean email_is_in_correct_format(String param) {
        return Pattern.matches("^[\\w!#$%&’*+/=?`{|}~^-]+(?:\\.[\\w!#$%&’*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$", param);
    }

    protected final Runnable start_my_ip_server_app() {
        return () -> {
            try {
                String pathToAppInLinux = "/opt/Address/Address";
                ProcessBuilder processBuilder = new ProcessBuilder();
                processBuilder.command(format_path_name_to_current_os(get_slash_for_my_os().equals(OperatingSystem.WINDOWS.getSlash()) ? String.format("%s\\_support\\Address\\Address.exe", Main.RESOURCE_PATH.getParentFile().getAbsolutePath()) : ((new File(pathToAppInLinux)).exists() ? pathToAppInLinux : "echo Address app not installed")));
                processBuilder.redirectErrorStream(true);
                Main.process = processBuilder.start();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(Main.process.getInputStream()));
                String someText;
                while (true) {
                    someText = bufferedReader.readLine();
                    if (someText == null) {
                        break;
                    }
                    new Thread(write_log(String.format("cmd:// attempting to start ip address provider.\n%s\n", someText))).start();
                }
            } catch (IOException e) {
                e.printStackTrace();
                new Thread(write_stack_trace(e)).start();
                Platform.runLater(() -> programmer_error(e).show());
            }
        };
    }

    public final InetAddress get_first_nonLoopback_address(boolean preferIpv4, boolean preferIPv6) throws SocketException, UnknownHostException {
        InetAddress result = InetAddress.getLocalHost();
        Enumeration<NetworkInterface> networkInterfaceEnumeration = NetworkInterface.getNetworkInterfaces();
        while (networkInterfaceEnumeration.hasMoreElements()) {
            NetworkInterface networkInterface = networkInterfaceEnumeration.nextElement();
            for (Enumeration<InetAddress> inetAddressEnumeration = networkInterface.getInetAddresses(); inetAddressEnumeration.hasMoreElements(); ) {
                InetAddress inetAddress = inetAddressEnumeration.nextElement();
                if (!inetAddress.isLoopbackAddress()) {
                    if (inetAddress instanceof Inet4Address) {
                        if (preferIPv6) {
                            continue;
                        }
                        result = inetAddress;
                        break;
                    }
                    if (inetAddress instanceof Inet6Address) {
                        if (preferIpv4) {
                            continue;
                        }
                        result = inetAddress;
                        break;
                    }
                }
            }
            if (result != null) break;
        }
        return result;
    }

    protected final boolean the_file_or_folder_has_zero_bytes(File fileOrFolder) {
        return get_size_of_the_show_sources(fileOrFolder) == 0;
    }

    protected final List<File> get_sub_folders(File parentFolder) {
        List<File> folderList = new ArrayList<>();
        if (parentFolder.isDirectory()) {
            if (!the_file_or_folder_has_zero_bytes(parentFolder)) {
                for (File childFileOrFolder : Objects.requireNonNull(parentFolder.listFiles())) {
                    if (childFileOrFolder.isDirectory()) {
                        if (the_file_or_folder_has_zero_bytes(childFileOrFolder)) {
                            return get_sub_folders(childFileOrFolder);
                        } else {
                            folderList.add(childFileOrFolder);
                        }
                    }
                }
            }
        }
        return folderList;
    }

    protected final boolean can_be_used_by_vlc(File file) {
        for (String string : EXTENSIONS_SUPPORTED_BY_VLC.split(":")) {
            if (file.getName().endsWith(string) || file.getName().endsWith(string.toUpperCase())) {
                if (file.canRead()) {
                    return true;
                }
            }
        }
        return false;
    }

    protected final String get_details_about_number_of_seasons_and_episodes_of_a_series(File folder) {
        int seasons = 1, episodes = get_number_of_valid_episodes(folder);

        List<File> subFolders = get_sub_folders(folder);
        if (!subFolders.isEmpty()) {
            seasons = subFolders.size();
            episodes = subFolders.stream().flatMapToInt(file -> IntStream.of(get_number_of_valid_episodes(file))).sum();
        }
        return String.format("%d Seasons & %d Episodes", seasons, episodes);
    }

    private boolean is_an_episode(File file) {
        return Arrays.stream(EXTENSIONS_FOR_MOVIES_ONLY.split(":")).filter(extensions -> StringUtils.endsWithIgnoreCase(file.getName(), extensions)).findFirst().orElse(null) != null;
    }

    private int get_number_of_valid_episodes(File file) {
        int result = 0;
        if (file.isDirectory()) {
            final File[] fileList = file.listFiles();
            if (fileList != null) {
                for (File file1 : fileList) {
                    if (file1.isFile()) {
                        if (is_an_episode(file1)) {
                            ++result;
                        }
                    } else if (file1.isDirectory()) {
                        result += get_number_of_valid_episodes(file1);
                    }
                }
            }
        }
        return result;
    }

    protected final double get_size_of_the_show_sources(File file) {
        double bytes = 0;
        if (file.isFile() && file.canRead() && can_be_used_by_vlc(file)) {
            bytes += file.length();
        } else if (file.isDirectory() && file.canRead()) {
            final File[] fileList = file.listFiles();
            if (fileList != null) {
                bytes += Arrays.stream(fileList).mapToDouble(this::get_size_of_the_show_sources).sum();
            }
        }
        return bytes;
    }

    protected final double get_actual_size_of_the_file_or_folder(File file) {
        double bytes = 0;
        if (file.isFile()) {
            bytes += file.length();
        } else if (file.isDirectory()) {
            final File[] fileList = file.listFiles();
            if (fileList != null) {
                bytes += Arrays.stream(fileList).mapToDouble(this::get_actual_size_of_the_file_or_folder).sum();
            }
        }
        return bytes;
    }

    protected final String make_bytes_more_presentable(double bytes) {
        final double kilobytes = (bytes / 1024);
        final double megabytes = (kilobytes / 1024);
        final double gigabytes = (megabytes / 1024);
        final double terabytes = (gigabytes / 1024);
        final double petabytes = (terabytes / 1024);
        final double exabytes = (petabytes / 1024);
        final double zettabytes = (exabytes / 1024);
        final double yottabytes = (zettabytes / 1024);

        String result;
        if (((int) yottabytes) > 0) {
            result = String.format("%,.2f", yottabytes).concat(" YB");
            return result;
        }
        if (((int) zettabytes) > 0) {
            result = String.format("%,.2f", zettabytes).concat(" ZB");
            return result;
        }
        if (((int) exabytes) > 0) {
            result = String.format("%,.2f", exabytes).concat(" EB");
            return result;
        }
        if (((int) petabytes) > 0) {
            result = String.format("%,.2f", petabytes).concat(" PB");
            return result;
        }
        if (((int) terabytes) > 0) {
            result = String.format("%,.2f", terabytes).concat(" TB");
            return result;
        }
        if (((int) gigabytes) > 0) {
            result = String.format("%,.2f", gigabytes).concat(" GB");
            return result;
        }
        if (((int) megabytes) > 0) {
            result = String.format("%,.2f", megabytes).concat(" MB");
            return result;
        }
        if (((int) kilobytes) > 0) {
            result = String.format("%,.2f", kilobytes).concat(" KB");
            return result;
        }
        result = String.format("%,.0f", bytes).concat(" Bytes");
        return result;
    }

    public final String format_showName_for_streaming(String fileName) {
        String result = fileName;
        for (String extension : EXTENSIONS_FOR_MOVIES_ONLY.split(":")) {
            result = StringUtils.replace(fileName, extension, "", 1);
            if (!result.equals(fileName)) {
                break;
            }
        }
        return result;
    }

    protected final String get_unique_key_for_list(List<String> stringList) {
        String newKey = RandomStringUtils.randomAlphanumeric(8);
        if (stringList.isEmpty()) {
            return newKey;
        } else if (stringList.contains(newKey)) {
            return get_unique_key_for_list(stringList);
        } else {
            return newKey;
        }
    }

    protected final List<String> get_file_names(Show... shows) {
        final List<String> stringList = new ArrayList<>();
        for (Show show : shows) {
            stringList.addAll(get_list_of_file_names_from_the_given_file_type(show.getSource()));
        }
        return stringList;
    }

    protected final List<String> get_list_of_file_names_from_the_given_file_type(File file) {
        final List<String> stringList = new ArrayList<>();
        if (file.exists()) {
            if (file.isDirectory()) {
                final File[] files = file.listFiles();
                if (files != null) {
                    for (File file1 : files) {
                        stringList.addAll(get_list_of_file_names_from_the_given_file_type(file1));
                    }
                }
            } else {
                if (file.isFile()) {
                    stringList.add(file.getName());
                }
            }
        } else {
            stringList.add("NOT FOUND >> ".concat(file.getAbsolutePath()));
        }
        return stringList;
    }

    protected final String generate_a_string_from_list_of_showNames(List<String> showNames) {
        int index = 1;
        StringBuilder line = new StringBuilder();
        for (String name : showNames) {
            final String str = String.format("%d . %s \n", index, name);
            if (line.length() == 0) {
                line = new StringBuilder(str);
            } else {
                line.append(str);
            }
            ++index;
        }
        return line.toString();
    }

    protected final Alert get_list_alert(String header, String text) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.initOwner(Main.stage);
        alert.setTitle("Movie hub");
        alert.setHeaderText(header);
        alert.setContentText("Click below to view the list");
        TextArea textArea = new TextArea(text);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        alert.getDialogPane().setExpandableContent(textArea);
        return alert;
    }

}
