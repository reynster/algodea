package com.bloxbean.algodea.idea.core.action;

import com.bloxbean.algodea.idea.core.action.util.ExporterUtil;
import com.bloxbean.algodea.idea.dryrun.ui.DryRunContextEntryDialog;
import com.bloxbean.algodea.idea.nodeint.model.DryRunContext;
import com.bloxbean.algodea.idea.nodeint.model.Result;
import com.bloxbean.algodea.idea.nodeint.service.LogListener;
import com.bloxbean.algodea.idea.nodeint.common.RequestMode;
import com.bloxbean.algodea.idea.util.IdeaUtil;
import com.intellij.icons.AllIcons;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.InputValidator;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.text.StringUtil;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public abstract class BaseTxnAction extends AlgoBaseAction {

    public BaseTxnAction() {
        super();
    }

    public BaseTxnAction(Icon icon) {
        super(icon);
    }

    public void exportTransaction(Project project, Module module, RequestMode requestMode, Result result, LogListener logListener) {
        if(result == null) {
            logListener.error("Export failed. Result : null");
            return;
        }
        if (result.isSuccessful()) {
            Result finalResult = result;

            ApplicationManager.getApplication().invokeLater(new Runnable() {
                @Override
                public void run() {
                    try {
                        String txnOutputFileName = Messages.showInputDialog("Enter txn file name (Without extension) : ",
                                "Export transaction", AllIcons.General.QuestionDialog, "NewTransaction", new InputValidator() {
                                    @Override
                                    public boolean checkInput(String inputString) {
                                        if(inputString != null && inputString.contains("."))
                                            return false;
                                        else
                                            return true;
                                    }

                                    @Override
                                    public boolean canClose(String inputString) {
                                        if(inputString != null && inputString.contains("."))
                                            return false;
                                        else
                                            return true;
                                    }
                                });
                        if(StringUtil.isEmpty(txnOutputFileName)) {
                            logListener.warn("Export transaction was cancelled");
                            return;
                        }

                        logListener.info(finalResult.getResponse());
                        boolean status = ExporterUtil.exportTransaction(module, finalResult.getResponse(), txnOutputFileName, logListener);
                        if(status) {
                            IdeaUtil.showNotification(project, "Export Transaction", String.format("Export transaction has been completed"),
                                    NotificationType.INFORMATION, null);
                        }
                    } catch (Exception exception) {
                        IdeaUtil.showNotification(project, "Export Transaction", String.format("Export transaction was not successful"),
                                NotificationType.INFORMATION, null);
                    }
                }
            });

        } else {
            logListener.error("Export transaction was not successful");
            IdeaUtil.showNotification(project, "Export Transaction", "Export transaction was not successful", NotificationType.ERROR, null);
        }
    }

    protected void processResult(Project project, Module module, Result result, RequestMode requestMode, LogListener logListener) {

        if(requestMode == null || requestMode.equals(RequestMode.TRANSACTION)) {
            if (result != null && result.isSuccessful()) {
                logListener.info(String.format("%s transaction executed successfully", getTxnCommand()));
                IdeaUtil.showNotification(project, getTitle(), String.format("%s was successful", getTxnCommand()), NotificationType.INFORMATION, null);
            } else {
                logListener.info(String.format("%s failed", getTxnCommand()));
                IdeaUtil.showNotification(project, getTitle(), String.format("%s failed", getTxnCommand()), NotificationType.ERROR, null);
            }
        } else if(requestMode.equals(RequestMode.EXPORT_SIGNED) || requestMode.equals(RequestMode.EXPORT_UNSIGNED)) {
            exportTransaction(project, module, requestMode, result, logListener);
        } else if(requestMode.equals(RequestMode.DRY_RUN)) {
            processDryRunResult(project, module, result, logListener);
        }
    }

    protected void processDryRunResult(Project project, Module module, Result result, LogListener logListener) {
        if(result == null) {
            logListener.error("Dry run failed. Result : " + result.getResponse());
            return;
        }
        if (result.isSuccessful()) {
            Result finalResult = result;

            ApplicationManager.getApplication().invokeLater(new Runnable() {
                @Override
                public void run() {
                    try {
                        String dryRunOutputFile = Messages.showInputDialog("Dry run file name (Without extension) : ",
                                "Dry Run Result", AllIcons.General.QuestionDialog, "NewDryRun", new InputValidator() {
                                    @Override
                                    public boolean checkInput(String inputString) {
                                        if(inputString != null && inputString.contains("."))
                                            return false;
                                        else
                                            return true;
                                    }

                                    @Override
                                    public boolean canClose(String inputString) {
                                        if(inputString != null && inputString.contains("."))
                                            return false;
                                        else
                                            return true;
                                    }
                                });
                        if(StringUtil.isEmpty(dryRunOutputFile)) {
                            //logListener.warn("Dry run export was cancelled");
                            return;
                        }

//                      logListener.info(finalResult.getResponse());
                        boolean status = ExporterUtil.exportDryRunResponse(module, finalResult.getResponse(), dryRunOutputFile, logListener);
                        if(status) {
                            IdeaUtil.showNotification(project, "Dry Run", String.format("Dry run transaction has been completed"),
                                    NotificationType.INFORMATION, null);
                        }
                    } catch (Exception exception) {
                        IdeaUtil.showNotification(project, "Dry Run export", String.format("Dry run result export was not successful"),
                                NotificationType.INFORMATION, null);
                    }
                }
            });

        } else {
            logListener.error("Dry run result export was not successful");
            IdeaUtil.showNotification(project, "Dry Run export", String.format("Dry run result export was not successful"),
                    NotificationType.INFORMATION, null);
        }
    }

    protected DryRunContext captureDryRunContext(Project project, List<Long> appIds, boolean isStatefulContract, boolean enableGeneralContextInfo, boolean enableSourceInfo) {
        DryRunContextEntryDialog dialog = new DryRunContextEntryDialog(project, appIds, isStatefulContract, enableGeneralContextInfo, enableSourceInfo);
        boolean ok = dialog.showAndGet();
        if(!ok)
            return null;

        return dialog.getDryRunContext();
    }

    protected DryRunContext captureDryRunContext(Project project, List<Long> appIds) {
        return captureDryRunContext(project, appIds, true, true, true);
    }

    protected DryRunContext.Source captureDryRunSource(Project project, Long appId, boolean isStatefulContract) {
        List<Long> appIds = new ArrayList<>();
        if(appId != null)
            appIds.add(appId);

        DryRunContextEntryDialog dialog = new DryRunContextEntryDialog(project, appIds, isStatefulContract, false, true);
        boolean ok = dialog.showAndGet();
        if(!ok)
            return null;

        return dialog.getDryRunSource();
    }

    protected abstract String getTitle();
    protected abstract String getTxnCommand();
}
