package com.bloxbean.algorand.idea.core.action;

import com.bloxbean.algorand.idea.configuration.action.ConfigurationAction;
import com.bloxbean.algorand.idea.util.IdeaUtil;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.project.Project;

public abstract class AlgoBaseAction extends AnAction {

    public void warnDeploymentTargetNotConfigured(Project project, String actionTitle) {
        IdeaUtil.showNotification(project, actionTitle, "Algorand Node for deployment node is not configured. Click here to configure.",
                NotificationType.ERROR, ConfigurationAction.ACTION_ID);
    }
}
