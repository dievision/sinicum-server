package com.dievision.sinicum.server.magnolia;

import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.Task;
import info.magnolia.module.delta.TaskExecutionException;

import com.dievision.sinicum.server.mgnlAdapters.ServerConfigurationAdapter5;
import com.dievision.sinicum.server.mgnlAdapters.TaskAdapter;
import com.dievision.sinicum.server.mgnlAdapters.TaskAdapterExecutionException;

public class TaskAdapterInstallerTask implements Task {
    final TaskAdapter taskAdapter;
    private static final Logger logger = LoggerFactory.getLogger(TaskAdapterInstallerTask.class);

    public TaskAdapterInstallerTask(Class<? extends TaskAdapter> taskAdapterClass)
        throws IllegalAccessException, InstantiationException {
        this.taskAdapter = taskAdapterClass.newInstance();
        taskAdapter.setServerConfiguration(new ServerConfigurationAdapter5());
    }

    @Override
    public String getName() {
        return taskAdapter.getName();
    }

    @Override
    public String getDescription() {
        return taskAdapter.getDescription();
    }

    @Override
    public void execute(InstallContext installContext) throws TaskExecutionException {
        try {
            taskAdapter.execute(installContext.getConfigJCRSession());
        } catch (TaskAdapterExecutionException e) {
            throw new TaskExecutionException(e.toString(), e);
        } catch (RepositoryException e) {
            throw new TaskExecutionException(e.toString(), e);
        }
    }
}
