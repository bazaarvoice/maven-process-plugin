package com.bazaarvoice.maven.plugin.process;

import java.util.Map;
import java.util.Stack;

public class CrossMojoState {
    private static final String PROCESSES = "processes";

    @SuppressWarnings("unchecked")
    public static void addProcess(ExecProcess process, Map pluginContext) {
        getProcesses(pluginContext).push(process);
    }

    @SuppressWarnings("unchecked")
    public static Stack<ExecProcess> getProcesses(Map pluginContext) {
    	if(pluginContext == null) return null;
    	
        Stack<ExecProcess> processes = (Stack<ExecProcess>) pluginContext.get(PROCESSES);
        if (processes == null) {
            processes = new Stack<ExecProcess>();
            pluginContext.put(PROCESSES, processes);
        }
        return processes;
    }
}
