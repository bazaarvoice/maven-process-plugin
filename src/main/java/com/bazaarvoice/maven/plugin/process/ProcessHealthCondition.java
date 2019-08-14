package com.bazaarvoice.maven.plugin.process;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;

public class ProcessHealthCondition {
    private static final int SECONDS_BETWEEN_CHECKS = 1;

    private ProcessHealthCondition() {}

    public static void waitSecondsUntilHealthy(HealthCheckConfig healthcheck, int timeoutInSeconds) {
        if (healthcheck == null || healthcheck.getUrl() == null) {
            // Wait for timeout seconds to let the process come up
            sleep(timeoutInSeconds);
            return;
        }
        final long start = System.currentTimeMillis();
        final URL url = url(healthcheck.getUrl());
        while ((System.currentTimeMillis() - start) / 1000 < timeoutInSeconds) {
            internalSleep();
            if (isHealthy(healthcheck, url)) {
                return; // success!!!
            }
        }
        throw new RuntimeException("Process was not healthy even after " + timeoutInSeconds + " seconds");
    }

    private static boolean isHealthy(HealthCheckConfig healthcheck, URL url) {
        InputStream in = null;
        try {
            URLConnection openConnection = url.openConnection();
            if (healthcheck.basicAuth != null) {
                String encoding = Base64.getEncoder().encodeToString((healthcheck.getBasicAuth().getUsername() + ":" + healthcheck.getBasicAuth().getPassword()).getBytes());
                openConnection.setRequestProperty("Authorization", "Basic " + encoding);
            }
            final HttpURLConnection http = (HttpURLConnection) openConnection;

            http.setRequestMethod("GET");
            http.connect();
            in = http.getInputStream();
            int responseCode = http.getResponseCode();
            if (responseCode != healthcheck.getStatus())
                return false;
            if (healthcheck.bodyMatchExpression != null) {
                Pattern pattern = Pattern.compile(healthcheck.bodyMatchExpression);
                List<String> readLines = IOUtils.readLines(in);
                String fullbody = readLines.stream().collect(Collectors.joining("\n"));
                Matcher matcher = pattern.matcher(fullbody);
                if (!matcher.find())
                    return false;
            }
            return true;
        } catch (ProtocolException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            return false;
        } finally {
            closeQuietly(in);
        }
    }

    private static URL url(String spec) {
        try {
            return new URL(spec);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private static void internalSleep() {
        try {
            Thread.sleep(SECONDS_BETWEEN_CHECKS * 1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static void sleep(int seconds) {
        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static void closeQuietly(Closeable out) {
        try {
            if (out != null) {
                out.close();
            }
        } catch (Exception e) {/**/}
    }
}
