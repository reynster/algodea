package com.bloxbean.algodea.idea.stateless.model;

import com.algorand.algosdk.account.Account;
import com.algorand.algosdk.crypto.MultisigAddress;
import com.bloxbean.algodea.idea.nodeint.model.LogicSigType;

import java.util.ArrayList;
import java.util.List;

public class LogicSigParams {
    private LogicSigType type;
    private boolean isAccountDelegationType;
    private List<Account> signingAccounts;
    private MultisigAddress multisigAddress;
    private List<byte[]> args;

    public List<Account> getSigningAccounts() {
        return signingAccounts;
    }

    public void addSigningAccount(Account siginingAccount) {
        if(signingAccounts == null)
            signingAccounts = new ArrayList();

        signingAccounts.add(siginingAccount);
    }

    public void setSigningAccounts(List<Account> signingAccounts) {
        this.signingAccounts = signingAccounts;
    }

    public MultisigAddress getMultisigAddress() {
        return multisigAddress;
    }

    public void setMultisigAddress(MultisigAddress multisigAddress) {
        this.multisigAddress = multisigAddress;
    }

    public List<byte[]> getArgs() {
        return args;
    }

    public void setArgs(List<byte[]> args) {
        this.args = args;
    }

    public void addArg(byte[] arg) {
        if(args == null) args = new ArrayList<>();
        args.add(arg);
    }

    public LogicSigType getType() {
        return type;
    }

    public void setType(LogicSigType type) {
        this.type = type;
    }

    public boolean isAccountDelegationType() {
        return isAccountDelegationType;
    }

    public void setAccountDelegationType(boolean accountDelegationType) {
        isAccountDelegationType = accountDelegationType;
    }
}
