package org.movieHub._nextGenEdition._reincarnated._api;

/**
 * @author edgar
 */
public interface Server {

    void start(boolean asSecure) throws Exception;

    void terminate();

    int get_running_port();

    String get_web_url();

    String get_base_url();

    String get_http_url();

}
