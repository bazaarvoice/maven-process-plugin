package com.bazaarvoice.maven.plugin.process;

import com.google.common.collect.Lists;
import org.apache.maven.plugin.logging.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class ExecProcess {
    private Process process = null;
    private final List<StdoutRedirector> redirectors = Lists.newArrayList();
    private File processLogFile = null;
    private boolean redirectErrorStream;
    private final String name;
    final ProcessBuilder pb = new ProcessBuilder();

    public ExecProcess(String name) {
        this.name = name;
    }

    public void setProcessLogFile(File emoLogFile) {
        this.processLogFile = emoLogFile;
    }

    public void setRedirectErrorStream(boolean isRedirect) {
        this.redirectErrorStream  = isRedirect;
    }

    public String getName() {
        return name;
    }

    public void execute(File workingDirectory, Log log, String... args) {
        final ProcessBuilder pb = new ProcessBuilder();

        if(redirectErrorStream) {
            pb.redirectErrorStream(true);
            pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        }

        log.info("Using working directory for this process: " + workingDirectory);
        pb.directory(workingDirectory);
        pb.command(args);
        try {
            process = pb.start();
            pumpOutputToLog(process, log);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void pumpOutputToLog(Process process, Log mavenLog) {
        if (processLogFile == null) {
            // pump to maven log
            redirectors.add(new StdoutRedirector(new InputStreamReader(process.getInputStream()), new MavenLogOutputStream(mavenLog, MavenLogOutputStream.INFO)));
            redirectors.add(new StdoutRedirector(new InputStreamReader(process.getErrorStream()), new MavenLogOutputStream(mavenLog, MavenLogOutputStream.ERROR)));
        } else {
            // pump to file log
            if (!processLogFile.getParentFile().isDirectory() && !processLogFile.getParentFile().mkdir()) {
                throw new IllegalStateException("Could not find or create directory containing " + processLogFile.getPath());
            }

            final FileOutputStream out = openFileOutputStream(processLogFile);
            redirectors.add(new StdoutRedirector(new InputStreamReader(process.getInputStream()), out));
            redirectors.add(new StdoutRedirector(new InputStreamReader(process.getErrorStream()), out));
        }
        // start all
        for (StdoutRedirector redirector : redirectors) {
            redirector.start();
        }
    }

    private static FileOutputStream openFileOutputStream(File file) {
        final FileOutputStream out;
        try {
            out = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("couldn't create or open file '" + file + "'", e);
        }
        return out;
    }

    public void destroy(boolean forcibly) {
        for (StdoutRedirector redirector : redirectors) {
            redirector.stopIt();
        }
        if (forcibly) {
            process.destroyForcibly();
        } else {
            process.destroy();
        }
    }

    public void waitFor() {
        try {
            process.waitFor();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
