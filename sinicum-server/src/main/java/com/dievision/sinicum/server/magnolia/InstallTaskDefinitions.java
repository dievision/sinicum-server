package com.dievision.sinicum.server.magnolia;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dievision.sinicum.server.mgnlAdapters.TaskAdapter;

/**
 *
 */
public class InstallTaskDefinitions {
    private static final Logger logger = LoggerFactory.getLogger(InstallTaskDefinitions.class);

    protected List<Class<? extends TaskAdapter>> getExtraInstallTasks() {
        List<Class<? extends TaskAdapter>> taskList = new ArrayList<Class<? extends TaskAdapter>>();
        taskList.add(AllowMultipleHttpMethodsTask.class);
        taskList.add(SinicumProxyFilterInstallTask.class);
        taskList.add(SinicumTemplateRendererInstallTask.class);
        return taskList;
    }
}
