package org.movieHub._nextGenEdition._reincarnated._api;

import j2html.tags.ContainerTag;
import org.movieHub._nextGenEdition._reincarnated._custom.Assistant;
import org.movieHub._nextGenEdition._reincarnated._model._object.ShowStream;
import org.movieHub._nextGenEdition._reincarnated._model._object.StreamLoad;
import spark.Response;
import spark.Spark;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import static j2html.TagCreator.*;
import static spark.Spark.get;

/**
 * @author Mandela aka puumInc
 */

public class ShowStreamer implements Server {

    private final transient Assistant assistant = new Assistant();
    private String httpType = "http";


    @Override
    public void start(boolean asSecure) throws Exception {
        Spark.staticFileLocation("public");
        Spark.port(3785);
        if (asSecure) {
            Spark.secure(assistant.format_path_name_to_current_os(assistant.SECURE_JKS), "bIioOCd9dU", null, null);
            this.httpType = "https";
        }
        web();
        watch();

        Spark.init();
    }

    @Override
    public void terminate() {
        Spark.stop();
    }

    @Override
    public int get_running_port() {
        return Spark.port();
    }

    @Override
    public String get_web_url() {
        return get_base_url().concat("/web");
    }

    @Override
    public String get_base_url() {
        return String.format("%s%s", get_http_url(), assistant.CONTEXT_PATH);
    }

    @Override
    public String get_http_url() {
        String url = String.format("%s://%s:%d", this.httpType, "localhost", get_running_port());
        try {
            InetAddress firstNonLoopbackAddress = assistant.get_first_nonLoopback_address(true, false);
            url = String.format("%s://%s:%d", this.httpType, firstNonLoopbackAddress.getHostAddress(), get_running_port());
        } catch (Exception e) {
            e.printStackTrace();
            new Thread(assistant.write_stack_trace(e)).start();
        }
        return url;
    }

    private void web() {
        get(String.format("%s/web", assistant.CONTEXT_PATH), (((request, response) -> {
            response.type("text/html");
            List<StreamLoad> streamLoadList = new CopyOnWriteArrayList<>();
            Assistant.STREAM_LOADS.forEach(streamLoad -> {
                StreamLoad streamLoadCopy = new StreamLoad();
                streamLoadCopy.setKey(streamLoad.getKey());
                streamLoadCopy.setName(streamLoad.getName());
                List<ShowStream> showStreamList = streamLoad.getShowStreamList().stream().filter(showStream -> {
                    File file = new File(showStream.getValue());
                    return (file.exists() && file.canRead());
                }).collect(Collectors.toList());
                if (!showStreamList.isEmpty()) {
                    streamLoadCopy.setShowStreamList(showStreamList);
                    streamLoadList.add(streamLoadCopy);
                }
            });
            return html()
                    .with(
                            head()
                                    .with(
                                            meta().withCharset("utf-8").withName("viewport").withContent("width=device-width,initial-scale=1.0"),
                                            link().withRel("shortcut icon").withType("image/x-icon").withHref(String.format("%s/favicon.ico", get_http_url())),
                                            title("Movie Hub Web")
                                    ),
                            body()
                                    .withStyle("background-color: #2a2a2a;")
                                    .with(
                                            div().with(
                                                    join(
                                                            h1("Movie Hub's ")
                                                                    .withStyle("font-family: Segoe, 'Segoe UI', 'DejaVu Sans', 'Trebuchet MS', Verdana, 'sans-serif'; color:#fae800; text-align:center;")
                                                                    .with(
                                                                            small("Web Edition")
                                                                                    .withStyle("font-weight: 600; color: #FEFEFE;")
                                                                    )
                                                    )
                                            ),
                                            div().with(
                                                    video().attr("controls=\"controls\" autoplay=\"\"  width=\"100%\" height=\"500\"")
                                                            .withName("media")
                                                            .withId("videoElement")
                                                            .with(
                                                                    source()
                                                                            .withId("videoSource")
                                                                            .withType("video/mp4")
                                                            )
                                            ),
                                            div().withStyle("background-color: #2a2a2a;").with(
                                                    p("Media Name")
                                                            .withId("mediaNameLbl")
                                                            .withStyle("font-family: Segoe, 'Segoe UI', 'DejaVu Sans', 'Trebuchet MS', Verdana, 'sans-serif'; color:#fae800; font-size: 16px;"),
                                                    p("Media Category")
                                                            .withId("mediaCategory")
                                                            .withStyle("font-family: Segoe, 'Segoe UI', 'DejaVu Sans', 'Trebuchet MS', Verdana, 'sans-serif'; color:#fefefe; font-size: 12px;")
                                            ),
                                            div()
                                                    .withStyle("padding-top: 25px; padding-bottom: 25px;")
                                                    .with(
                                                            join(
                                                                    label("Choose video from the options below")
                                                                            .withStyle("font-family: Segoe, 'Segoe UI', 'DejaVu Sans', 'Trebuchet MS', Verdana, 'sans-serif'; font-weight: 600; color:#fae800; font-style:italic;")
                                                                            .attr("for=\"select_element\""),
                                                                    select()
                                                                            .withName("select_element")
                                                                            .withId("select_element")
                                                                            .withStyle("border-color: #fae800; background-color: #2a2a2a; font-family: Segoe, 'Segoe UI', 'DejaVu Sans', 'Trebuchet MS', Verdana, 'sans-serif'; font-size: 14px; color: #FEFEFE; padding: 10px; width: 100%; margin-top: 12.5px;")
                                                                            .attr("onChange=\"show_video(event)\"")
                                                                            .with(
                                                                                    optgroup()
                                                                                            .attr("label=\"Singles\"")
                                                                                            .with(
                                                                                                    streamLoadList.stream().filter(streamLoad -> streamLoad.getShowStreamList().size() == 1).map(streamLoad -> option()
                                                                                                            .withValue(String.format("%s/show/watch/m/%s", get_base_url(), streamLoad.getShowStreamList().get(0).getKey()))
                                                                                                            .withText(streamLoad.getName())).toArray(ContainerTag[]::new)
                                                                                            )
                                                                                    , optgroup()
                                                                                            .with(
                                                                                                    streamLoadList.stream().filter(streamLoad -> streamLoad.getShowStreamList().size() > 1).map(streamLoad -> optgroup()
                                                                                                            .attr(String.format("label=\"%s\"", streamLoad.getName()))
                                                                                                            .with(
                                                                                                                    streamLoad.getShowStreamList().stream().map(showStream -> option()
                                                                                                                            .withValue(String.format("%s/show/watch/s/%s/%s", get_base_url(), streamLoad.getKey(), showStream.getKey()))
                                                                                                                            .withText(assistant.format_showName_for_streaming(new File(showStream.getValue()).getName()))).toArray(ContainerTag[]::new)
                                                                                                            )).toArray(ContainerTag[]::new)
                                                                                            )
                                                                            )
                                                            ),
                                                            script(rawHtml("var index;\n" +
                                                                    "\tfunction show_video(evt) {\n" +
                                                                    "\t\tdocument.querySelector(\"#videoElement > source\").src = evt.target.value;\n" +
                                                                    "\t\tdocument.getElementById(\"videoElement\").load();\n" +
                                                                    "\t\tvar cbx =  document.getElementById(\"select_element\");\n" +
                                                                    "\t\tindex = cbx.selectedIndex;\n" +
                                                                    "\t\tvar op = cbx.options[index];\n" +
                                                                    "\t\tdocument.getElementById(\"mediaNameLbl\").innerHTML = op.text;\n" +
                                                                    "\t\tvar optGroup = op.parentNode;\n" +
                                                                    "\t\tdocument.getElementById(\"mediaCategory\").innerHTML = optGroup.label;\n" +
                                                                    "\t\t\n" +
                                                                    "\t\tdocument.getElementById(\"videoElement\").onended = function () {\n" +
                                                                    "\t\t\tindex = (index + 1);\n" +
                                                                    "\t\t\tvar next_op = cbx.options[index];\n" +
                                                                    "\t\t\tdocument.querySelector(\"#videoElement > source\").src = next_op.value;\n" +
                                                                    "\t\t    document.getElementById(\"videoElement\").load();\n" +
                                                                    "\t\t\tdocument.getElementById(\"mediaNameLbl\").innerHTML = next_op.text;\n" +
                                                                    "\t\t\tvar next_optGroup = next_op.parentNode;\n" +
                                                                    "\t\t\tdocument.getElementById(\"mediaCategory\").innerHTML = next_optGroup.label;\n" +
                                                                    "\t\t\tdocument.getElementById(\"select_element\").options[index].selectedIndex = true;\n" +
                                                                    "\t\t};\t\n" +
                                                                    "\t}")).withType("text/javascript")
                                                    ))).render();
        })));
    }


    private void watch() {
        get(String.format("/%s/show/watch/m/:loadId", assistant.CONTEXT_PATH), ((request, response) -> {
            String loadId = request.params(":loadId");
            Assistant.STREAM_LOADS.stream()
                    .filter(streamLoad -> ((streamLoad.getKey() == null) && streamLoad.getShowStreamList().get(0).getKey().equals(loadId)))
                    .findFirst()
                    .ifPresent(load -> {
                        response.type("video/mp4");
                        stream_to_client(load.getShowStreamList().get(0).getValue(), response);
                    });
            return HttpURLConnection.HTTP_NOT_FOUND;
        }));

        get(String.format("/%s/show/watch/s/:loadId/:fileId", assistant.CONTEXT_PATH), ((request, response) -> {
            String loadId = request.params(":loadId");
            String fileId = request.params(":fileId");
            Assistant.STREAM_LOADS.stream().filter(streamLoad -> ((streamLoad.getShowStreamList().size() > 1) && streamLoad.getKey().equals(loadId))).findAny()
                    .flatMap(streamLoad -> streamLoad.getShowStreamList().stream().filter(showStream -> showStream.getKey().equals(fileId)).findAny())
                    .ifPresent(showStream -> {
                        response.type("video/mp4");
                        stream_to_client(showStream.getValue(), response);
                    });
            return HttpURLConnection.HTTP_NO_CONTENT;
        }));
    }

    private void stream_to_client(String filePath, Response response) {
        File file = new File(filePath);
        if (file.exists()) {
            response.raw().setContentLengthLong(file.length());
            try {
                DataOutputStream dataOutputStream = new DataOutputStream(new BufferedOutputStream(response.raw().getOutputStream()));
                DataInputStream dataInputStream = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
                int count;
                byte[] buffer = new byte[8192];
                while ((count = dataInputStream.read(buffer)) > 0) {
                    dataOutputStream.write(buffer, 0, count);
                }
                dataInputStream.close();
                dataOutputStream.close();
            } catch (Exception exception) {
                exception.printStackTrace();
                new Thread(assistant.write_stack_trace(exception)).start();
            }
        }
    }


}
