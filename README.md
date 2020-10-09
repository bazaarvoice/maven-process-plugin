[![Build Status](https://travis-ci.org/bazaarvoice/maven-process-plugin.svg?branch=master)](https://travis-ci.org/bazaarvoice/maven-process-plugin)

process-exec-maven-plugin
========================

Improve end-to-end integration testing with maven. Process Executor Plugin allows you to to start multiple processes in pre-integration-test phase in order, and then stops all the processes in post-integration-test phase, in reverse order. 

## Goals
* __start__ - Pre-Integration-test phase. Starts a given process in the pre-integration-test phase. Requires one execution per process.
* __stop-all__ - Post-Integration-test phase. Stops all processes that are started in the pre-integration-test phase, in reverse order. Requires only one execution for all processes. 

## Arguments
* __arguments__: Command line arguments as you would provide when starting a process in your terminal. So, for example to run something like this
    ```bash
    java -jar drop-wizard-app.jar server config.yaml
    ```

    set arguments as:

    ```
    <arguments>
        <argument>java</argument>
        <argument>-jar</argument>
        <argument>drop-wizard-app.jar</argument>
        <argument>server</argument>
        <argument>config.yaml</argument>
    </arguments>
    ```
* __name__: Give a name to the process to start.
* __workingDir__: Give a working directory for your process to start in. Could be same as name. If not provided, the build directory is used.
* __waitForInterrupt__: Optional. Setting this value to true will pause your build after starting every process to give you a chance to manually play with your system. Default is false.
* __healthcheckUrl__: Recommended, but optional. You should provide a healthcheck url, so the plugin waits until the healthchecks are all green for your process. If not provided, the plugin waits for `waitAfterLaunch` seconds before moving on.
* __waitAfterLaunch__: Optional. This specifies the maximum time in seconds to wait after launching the process. If healthcheckUrl is specified, then it will move on as soon as the health checks pass. Default is 30 seconds.
* __processLogFile__: Optional. Specifying a log file will redirect the process output to the specified file. Recommended as this will avoid cluttering your build's log with the log of external proccesses.
* __stopForcibly__: Optional. Setting this value to true will enforce process termination

*NOTE:* As of 0.7, killing the maven process (using Ctrl+C or kill <pid> command) will stop all the processes started by the plugin.
## POM example:
The latest version is 0.7.

    <build>
        <plugins>
            <plugin>
                <groupId>com.bazaarvoice.maven.plugins</groupId>
                <artifactId>process-exec-maven-plugin</artifactId>
                <version>0.7</version>
                <executions>
                    <!--Start process 1, eg., a dropwizard app dependency-->
                    <execution>
                        <id>switchboard-process</id>
                        <phase>pre-integration-test</phase>
                        <goals>
                            <goal>start</goal>
                        </goals>
                        <configuration>
                            <name>Switchboard2</name>
                            <workingDir>switchboard2</workingDir>
                            <waitForInterrupt>false</waitForInterrupt>
                            <healthcheckUrl>http://localhost:8381/healthcheck</healthcheckUrl>
                            <arguments>
                                <argument>java</argument>
                                <argument>-jar</argument>
                                <argument>${basedir}/../../app/target/switchboard-${project.version}.jar</argument>
                                <argument>server</argument>
                                <argument>${basedir}/bin/switchboard.yaml</argument>
                            </arguments>
                        </configuration>
                    </execution>
                    <!--Start process 2, eg., another dropwizard app dependency-->
                    <execution>
                        <id>emodb-shovel-process</id>
                        <phase>pre-integration-test</phase>
                        <goals>
                            <goal>start</goal>
                        </goals>
                        <configuration>
                            <name>emodb-shovel</name>
                            <workingDir>shovel</workingDir>
                            <waitForInterrupt>false</waitForInterrupt>
                            <healthcheckUrl>http://localhost:8181/healthcheck</healthcheckUrl>
                            <arguments>
                                <argument>java</argument>
                                <argument>-jar</argument>
                                <argument>${basedir}/../../app/target/emodb-shovel-app-${project.version}.jar</argument>
                                <argument>server</argument>
                                <argument>${basedir}/bin/config-local-dc.yaml</argument>
                            </arguments>
                        </configuration>
                    </execution>
                    <!--Stop all processes in reverse order-->
                    <execution>
                        <id>stop-all</id>
                        <phase>post-integration-test</phase>
                        <goals>
                            <goal>stop-all</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
       
    
