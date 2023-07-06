package com.bazaarvoice.maven.plugin.process;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.IOException;
import java.util.Stack;

public abstract class AbstractProcessMojo extends AbstractMojo {
    @Component
    protected MavenProject project;

    @Parameter (property = "exec.arguments")
    protected String[] arguments;

    @Parameter(property = "exec.workingDir")
    protected String workingDir;

    @Parameter(property = "exec.name")
    protected String name;

    @Parameter(property = "exec.healthcheckUrl")
    protected String healthcheckUrl;

    @Parameter(property = "exec.waitAfterLaunch", required = false, defaultValue = "30")
    protected int waitAfterLaunch;

    @Parameter(defaultValue = "false", property = "exec.waitForInterrupt")
    protected boolean waitForInterrupt;

    @Parameter(required = false, property = "exec.processLogFile")
    protected String processLogFile;

    @Parameter(defaultValue = "false", property = "exec.skip")
    protected boolean skip;

    @Parameter(defaultValue = "false", property = "exec.redirectErrorStream")
    protected boolean redirectErrorStream;

    @Parameter(property = "exec.stopForcibly")
    protected boolean stopForcibly;

    protected static File ensureDirectory(File dir) {
        if (!dir.mkdirs() && !dir.isDirectory()) {
            throw new RuntimeException("couldn't create directories: " + dir);
        }
        return dir;
    }

    protected void sleepUntilInterrupted() throws IOException {
        getLog().info("Hit ENTER on the console to continue the build.");

        for (;;) {
            int ch = System.in.read();
            if (ch == -1 || ch == '\n') {
                break;
            }
        }
    }

    public AbstractProcessMojo() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                Stack<ExecProcess> processesStack = CrossMojoState.getProcesses(getPluginContext());
                // Let us stop any services that are still running
                if (!processesStack.empty()) {
                    internalStopProcesses();
                }
            }
        });
    }

    protected void internalStopProcesses() {
        getLog().info("Stopping all processes ...");
        Stack<ExecProcess> processesStack = CrossMojoState.getProcesses(getPluginContext());
        while(!processesStack.isEmpty()) {
            ExecProcess execProcess = processesStack.pop();
            if (execProcess != null) {
                getLog().info("Stopping process: " + execProcess.getName());
                execProcess.destroy(stopForcibly);
                execProcess.waitFor();
                getLog().info("Stopped process: " + execProcess.getName());
            }
        }
    }
}
