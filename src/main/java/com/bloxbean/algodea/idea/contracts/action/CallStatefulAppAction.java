package com.bloxbean.algodea.idea.contracts.action;

import com.algorand.algosdk.account.Account;
import com.bloxbean.algodea.idea.nodeint.model.TxnDetailsParameters;
import com.bloxbean.algodea.idea.nodeint.service.StatefulContractService;

public class CallStatefulAppAction extends BaseStatefulAppAction {
    @Override
    public String getInputDialogTitle() {
        return "Application - Call (NoOp)";
    }

    @Override
    public String getApplicationTxnDescription() {
        return "Application - Call (NoOp)";
    }

    @Override
    public String getApplicationTxnCommand() {
        return "Call (NoOp)";
    }

    @Override
    public boolean invokeTransaction(StatefulContractService sfService, Long appId, Account fromAccount, TxnDetailsParameters txnDetailsParameters) throws Exception {
        return sfService.call(appId, fromAccount, txnDetailsParameters);
    }
}