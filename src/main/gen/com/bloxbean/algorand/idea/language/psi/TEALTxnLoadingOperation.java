// This is a generated file. Not intended for manual editing.
package com.bloxbean.algorand.idea.language.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface TEALTxnLoadingOperation extends PsiElement {

  @NotNull
  TEALTxnOpcode getTxnOpcode();

  @Nullable
  TEALTxnFieldArg getTxnFieldArg();

  @Nullable
  TEALUnsignedInteger getUnsignedInteger();

  @Nullable
  PsiElement getVarTmpl();

}
