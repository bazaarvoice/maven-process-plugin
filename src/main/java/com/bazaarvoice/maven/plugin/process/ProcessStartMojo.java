package com.bazaarvoice.maven.plugin.process;

import com.google.common.base.Joiner;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

import java.io.File;

@Mojo (name = "start", defaultPhase = LifecyclePhase.PRE_INTEGRATION_TEST)
public class ProcessStartMojo extends AbstractProcessMojo {

    @Override
    public void execute()
            throws MojoExecutionException, MojoFailureException {
        if (skip) {
            getLog().info("Skipping " + name + " due to configuration skip=true");
            return;
        }
        for (String arg : arguments) {
            getLog().info("arg: " + arg);
        }
        getLog().info("Full command line: " + Joiner.on(" ").join(arguments));
        try {
            startProcess();
            if (waitForInterrupt) {
                sleepUntilInterrupted();
            }
        } catch (Exception e) {
            getLog().error(e);
        }
    }

    private void startProcess() {
        final ExecProcess exec = new ExecProcess(name);
        if (null != processLogFile) {
            exec.setProcessLogFile(new File(processLogFile));
        }
        getLog().info("Starting process: " + exec.getName());
        exec.execute(processWorkingDirectory(), getLog(), arguments);
        CrossMojoState.addProcess(exec, getPluginContext());
        ProcessHealthCondition.waitSecondsUntilHealthy(healthcheckUrl, waitAfterLaunch);
        getLog().info("Started process: " + exec.getName());
    }

    private File processWorkingDirectory() {
        if (workingDir == null) {
            return ensureDirectory(new File(project.getBuild().getDirectory()));
        }

        // try to check if directory is absolute
        // https://github.com/bazaarvoice/maven-process-plugin/issues/11
        File potentialWorkingDir = new File(workingDir);
        if (potentialWorkingDir.isAbsolute() && potentialWorkingDir.exists() && potentialWorkingDir.isDirectory()) {
            return potentialWorkingDir;
        }
        return ensureDirectory(new File(project.getBuild().getDirectory(), workingDir));
    }

}
