package com.dievision.sinicum.server.mgnlAdapters;

import javax.jcr.Session;

public interface TaskAdapter {
    String getName();
    String getDescription();

    /**
     * Executes the install task.
     *
     * @param configSession An instance of a session to the <tt>config</tt> workspace,
     */
    void execute(Session configSession) throws TaskAdapterExecutionException;

    void setServerConfiguration(ServerConfigurationAdapter serverConfigurationAdapter);
}
