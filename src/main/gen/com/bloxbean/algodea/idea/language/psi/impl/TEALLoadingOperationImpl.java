// This is a generated file. Not intended for manual editing.
package com.bloxbean.algodea.idea.language.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static com.bloxbean.algodea.idea.language.psi.TEALTypes.*;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.bloxbean.algodea.idea.language.psi.*;

public class TEALLoadingOperationImpl extends ASTWrapperPsiElement implements TEALLoadingOperation {

  public TEALLoadingOperationImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull TEALVisitor visitor) {
    visitor.visitLoadingOperation(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof TEALVisitor) accept((TEALVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public TEALArgOperation getArgOperation() {
    return findChildByClass(TEALArgOperation.class);
  }

  @Override
  @Nullable
  public TEALBytecOperation getBytecOperation() {
    return findChildByClass(TEALBytecOperation.class);
  }

  @Override
  @Nullable
  public TEALBytecblockOperation getBytecblockOperation() {
    return findChildByClass(TEALBytecblockOperation.class);
  }

  @Override
  @Nullable
  public TEALGlobalOperation getGlobalOperation() {
    return findChildByClass(TEALGlobalOperation.class);
  }

  @Override
  @Nullable
  public TEALGtxnLoadingOperation getGtxnLoadingOperation() {
    return findChildByClass(TEALGtxnLoadingOperation.class);
  }

  @Override
  @Nullable
  public TEALGtxnaLoadingOperation getGtxnaLoadingOperation() {
    return findChildByClass(TEALGtxnaLoadingOperation.class);
  }

  @Override
  @Nullable
  public TEALIntcOperation getIntcOperation() {
    return findChildByClass(TEALIntcOperation.class);
  }

  @Override
  @Nullable
  public TEALIntcblockOperation getIntcblockOperation() {
    return findChildByClass(TEALIntcblockOperation.class);
  }

  @Override
  @Nullable
  public TEALLoadOperation getLoadOperation() {
    return findChildByClass(TEALLoadOperation.class);
  }

  @Override
  @Nullable
  public TEALStoreOperation getStoreOperation() {
    return findChildByClass(TEALStoreOperation.class);
  }

  @Override
  @Nullable
  public TEALTxnLoadingOperation getTxnLoadingOperation() {
    return findChildByClass(TEALTxnLoadingOperation.class);
  }

  @Override
  @Nullable
  public TEALTxnaLoadingOperation getTxnaLoadingOperation() {
    return findChildByClass(TEALTxnaLoadingOperation.class);
  }

}
