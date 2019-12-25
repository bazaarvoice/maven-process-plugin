package com.bazaarvoice.maven.plugin.process;

import org.apache.maven.plugins.annotations.Parameter;

import java.net.URL;
import java.util.Properties;

public class HealthcheckUrl {
    private URL url;
    private Properties headers;

    public URL getUrl() {
        return url;
    }

    public Properties getHeaders() {
        return headers;
    }

    public void setUrl(URL url) {
        this.url = url;
    }
}