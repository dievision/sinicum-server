package com.dievision.sinicum.server.magnolia;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.magnolia.module.DefaultModuleVersionHandler;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.Task;

import com.dievision.sinicum.server.mgnlAdapters.TaskAdapter;

public class VersionHandler extends DefaultModuleVersionHandler {
    private static final Logger logger = LoggerFactory.getLogger(VersionHandler.class);

    @Override
    protected List<Task> getExtraInstallTasks(InstallContext installContext) {
        List<Task> taskList = new ArrayList<Task>();
        InstallTaskDefinitions installTaskDefinitions = new InstallTaskDefinitions();
        for (Class<? extends TaskAdapter> adapterClass
                : installTaskDefinitions.getExtraInstallTasks()) {
            try {
                taskList.add(new TaskAdapterInstallerTask(adapterClass));
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (InstantiationException e) {
                throw new RuntimeException(e);
            }
        }
        taskList.add(new SinicumRestServletInstallTask());
        return taskList;
    }
}
