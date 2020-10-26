/*
 * Copyright (c) 2020 BloxBean Project
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.bloxbean.algodea.idea.contracts.action;

import com.algorand.algosdk.account.Account;
import com.algorand.algosdk.crypto.Address;
import com.bloxbean.algodea.idea.account.model.AlgoAccount;
import com.bloxbean.algodea.idea.account.service.AccountService;
import com.bloxbean.algodea.idea.configuration.service.AlgoProjectState;
import com.bloxbean.algodea.idea.contracts.ui.CreateAppDialog;
import com.bloxbean.algodea.idea.contracts.ui.TxnDetailsEntryForm;
import com.bloxbean.algodea.idea.core.action.AlgoBaseAction;
import com.bloxbean.algodea.idea.nodeint.model.TxnDetailsParameters;
import com.bloxbean.algodea.idea.pkg.AlgoPkgJsonService;
import com.bloxbean.algodea.idea.pkg.exception.PackageJsonException;
import com.bloxbean.algodea.idea.pkg.model.AlgoPackageJson;
import com.bloxbean.algodea.idea.toolwindow.AlgoConsole;
import com.bloxbean.algodea.idea.contracts.ui.CreateAppEntryForm;
import com.bloxbean.algodea.idea.core.action.util.AlgoContractModuleHelper;
import com.bloxbean.algodea.idea.core.service.AlgoCacheService;
import com.bloxbean.algodea.idea.nodeint.exception.DeploymentTargetNotConfigured;
import com.bloxbean.algodea.idea.nodeint.service.LogListenerAdapter;
import com.bloxbean.algodea.idea.nodeint.service.StatefulContractService;
import com.bloxbean.algodea.idea.util.AlgoModuleUtils;
import com.bloxbean.algodea.idea.util.IdeaUtil;
import com.intellij.icons.AllIcons;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.progress.impl.BackgroundableProcessIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.twelvemonkeys.lang.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;

public class CreateStatefulAppAction extends AlgoBaseAction {
    private final static Logger LOG = Logger.getInstance(CreateStatefulAppAction.class);

    public CreateStatefulAppAction() {
        super(AllIcons.Actions.Install);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null)
            return;

        final Module module = LangDataKeys.MODULE.getData(e.getDataContext());
        if (module == null)
            return;

        FileDocumentManager.getInstance().saveAllDocuments();

        AlgoConsole console = AlgoConsole.getConsole(project);
        console.clearAndshow();
        try {
            StatefulContractService sfService
                    = new StatefulContractService(project, new LogListenerAdapter(console));

            AlgoProjectState projectState = AlgoProjectState.getInstance(project);
            if (projectState == null) {
                LOG.error("Project state is null");
                IdeaUtil.showNotificationWithAction(project, "Create App",
                        "Project data could not be found. Something is wrong", NotificationType.ERROR, null);
                return;
            }

            AlgoPkgJsonService pkgJsonService = AlgoPkgJsonService.getInstance(project);
            AlgoPackageJson packageJson = pkgJsonService.getPackageJson();
            if (packageJson == null || packageJson.getStatefulContractList().size() == 0) {
                IdeaUtil.showNotification(project, "Create App",
                        "No stateful contract defined in algo-package.json", NotificationType.WARNING, null);
                return;
            }

            String deploymentServerId = projectState.getState().getDeploymentServerId();

            AlgoCacheService cacheService = AlgoCacheService.getInstance(project);
            AccountService accountService = AccountService.getAccountService();

            String cacheCreatorAccount = cacheService.getSfCreatorAccount();
            AlgoAccount cacheAlgoAccount = null;
            if (!StringUtil.isEmpty(cacheCreatorAccount)) {
                cacheAlgoAccount = accountService.getAccountByAddress(cacheCreatorAccount);
            }

            CreateAppDialog createDialog = new CreateAppDialog(project, cacheAlgoAccount, cacheService.getContract());
            boolean ok = createDialog.showAndGet();
            if (!ok) {
                IdeaUtil.showNotification(project, "Create App", "Create App operation was cancelled", NotificationType.WARNING, null);
                return;
            }

            CreateAppEntryForm createForm = createDialog.getCreateForm();
            TxnDetailsEntryForm txnDetailsEntryForm = createDialog.getTxnDetailsEntryForm();

            Account account = createForm.getAccount();
            if (account == null) {
                console.showErrorMessage("Invalid or null creator account");
                console.showErrorMessage("Create App Failed");
                return;
            }

            String contractName = createForm.getContractName();
            String approvalProgramName = createForm.getApprovalProgram();
            String clearStateProgramName = createForm.getClearStateProgram();

            int globalByteslices = createForm.getGlobalByteslices();
            int globalInts = createForm.getGlobalInts();
            int localByteslices = createForm.getLocalByteslices();
            int localInts = createForm.getLocalInts();

            List<byte[]> appArgs = txnDetailsEntryForm.getArgsAsBytes();
            byte[] note = txnDetailsEntryForm.getNoteBytes();
            byte[] lease = txnDetailsEntryForm.getLeaseBytes();
            List<Address> accounts = txnDetailsEntryForm.getAccounts();
            List<Long> foreignApps = txnDetailsEntryForm.getForeignApps();
            List<Long> foreignAssets = txnDetailsEntryForm.getForeignAssets();

            TxnDetailsParameters txnDetailsParameters = new TxnDetailsParameters();
            txnDetailsParameters.setAppArgs(appArgs);
            txnDetailsParameters.setNote(note);
            txnDetailsParameters.setLease(lease);
            txnDetailsParameters.setAccounts(accounts);
            txnDetailsParameters.setForeignApps(foreignApps);
            txnDetailsParameters.setForeignAssets(foreignAssets);

            //update cache
            if(!StringUtil.isEmpty(contractName))
                cacheService.setLastContract(contractName);
            cacheService.setSfCreatorAccount(account.getAddress().toString());

            VirtualFile appProgVF = AlgoModuleUtils.getSourceVirtualFileByRelativePath(project, approvalProgramName);//VfsUtil.findRelativeFile(approvalProgramName, sourceRoot);//VfsUtil.findRelativeFile(sourceRoot, approvalProgramName);
            VirtualFile clearProgVF = AlgoModuleUtils.getSourceVirtualFileByRelativePath(project, clearStateProgramName);//VfsUtil.findRelativeFile(clearStateProgramName, sourceRoot);

            if (appProgVF == null || !appProgVF.exists()) {
                console.showErrorMessage(String.format("Approval Program doesn't exist: %s", appProgVF != null ? appProgVF.getCanonicalPath() : approvalProgramName));
                return;
            }

            if (clearProgVF == null || !clearProgVF.exists()) {
                console.showErrorMessage(String.format("Clear State Program doesn't exist: %s", clearProgVF != null ? clearProgVF.getCanonicalPath() : clearStateProgramName));
                return;
            }

            //Find relative path for source which is required to create merged source
            String relAppProgPath = AlgoModuleUtils.getRelativePathFromSourceRoot(project, appProgVF);
            String relClearStatePath = AlgoModuleUtils.getRelativePathFromSourceRoot(project, clearProgVF);

            if (StringUtil.isEmpty(relAppProgPath))
                relAppProgPath = appProgVF.getName();
            if (StringUtil.isEmpty(relClearStatePath))
                relClearStatePath = clearProgVF.getName();
            //ends

            VirtualFile moduleOutFolder = AlgoContractModuleHelper.getModuleOutputFolder(console, module);

            //Merge Approval Program if there is any variable template available
            File mergedAppProgSource = AlgoContractModuleHelper.generateMergeSourceWithVariables(project, console, moduleOutFolder, appProgVF, relAppProgPath);

            //Merge Clear Program if there is any variable template available
            File mergeClearProgSource = AlgoContractModuleHelper.generateMergeSourceWithVariables(project, console, moduleOutFolder, clearProgVF, relClearStatePath);

            String appProgSource = null;
            String clearProgSource = null;

            if (mergedAppProgSource == null) { //No VAR_TMPL_
                appProgSource = VfsUtil.loadText(appProgVF);
            } else {
                console.showInfoMessage("Variables found. Generated merged file for Approval Program can be found at : " + mergedAppProgSource.getAbsolutePath());
                appProgSource = FileUtil.loadFile(mergedAppProgSource, "UTF-8");
            }

            if (mergeClearProgSource == null) { //No VAR_TMPL
                clearProgSource = VfsUtil.loadText(clearProgVF);
                ;
            } else {
                console.showInfoMessage("Variables found. Generated merged file for Clear State Program can be found at : " + mergeClearProgSource.getAbsolutePath());
                clearProgSource = FileUtil.loadFile(mergeClearProgSource, "UTF-8");
            }

            console.showInfoMessage("Approval Program file    : " + approvalProgramName);
            console.showInfoMessage("Clear State Program file : " + clearStateProgramName);

            //Needed for nested class
            final String appProgText = appProgSource;
            final String clearProgText = clearProgSource;

            Task.Backgroundable task = new Task.Backgroundable(project, "Creating Stateful Smart Contract app") {

                @Override
                public void run(@NotNull ProgressIndicator indicator) {
                    console.showInfoMessage("Creating stateful smart contract ...");
                    Long appId = null;
                    try {
                        appId = sfService.createApp(appProgText, clearProgText, account,
                                globalByteslices, globalInts, localByteslices, localInts,
                                txnDetailsParameters);
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                    if (appId != null) {
                        LOG.info(appId + "");
                        cacheService.addAppId(deploymentServerId, contractName, String.valueOf(appId)) ;

                        console.showInfoMessage("Stateful smart contract app created with app Id : " + appId);
                        IdeaUtil.showNotification(project, "Create App", String.format("%s App Created Successfully with appId: %s", contractName, appId), NotificationType.INFORMATION, null);
                    } else {
                        console.showErrorMessage("Create App failed");
                        IdeaUtil.showNotification(project, "Create App", "Create App failed", NotificationType.ERROR, null);
                    }
                }
            };

            ProgressManager.getInstance().runProcessWithProgressAsynchronously(task, new BackgroundableProcessIndicator(task));

        } catch (DeploymentTargetNotConfigured deploymentTargetNotConfigured) {
            LOG.error(deploymentTargetNotConfigured);
            warnDeploymentTargetNotConfigured(project, "Create App");
        } catch (PackageJsonException pkex) {
            LOG.error(pkex);
            IdeaUtil.showNotification(project, "Create App",
                    "algo-package.json could not be loaded", NotificationType.ERROR, null);
        } catch (Exception ex) {
            LOG.error(ex);
            IdeaUtil.showNotification(project, "Create App", "Create App failed : " + ex.getMessage(), NotificationType.ERROR, null);
        }
    }
}
