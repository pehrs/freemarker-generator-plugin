/*
 * Copyright (c) 2024. Matti Pehrs (matti@pehrs.com)
 */

package com.pehrs.intellij.freemarker.plugin;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.intellij.psi.PsiModifierList;
import com.intellij.psi.PsiType;
import com.intellij.psi.impl.java.stubs.JavaStubElementTypes;
import com.intellij.psi.impl.source.PsiFieldImpl;
import org.jetbrains.annotations.NotNull;

public class PsiUtils {

  @NotNull
  static PsiFieldImpl getSimplePsiField(String accessor, String javaType,
      String fieldName) {
    PsiFieldImpl stringField = mock(PsiFieldImpl.class);
    when(stringField.getName()).thenReturn(fieldName);

    PsiModifierList stringFieldMods = mock(PsiModifierList.class);
    when(stringFieldMods.hasModifierProperty("static")).thenReturn(accessor.contains("static"));
    when(stringFieldMods.hasModifierProperty("public")).thenReturn(accessor.contains("public"));
    when(stringFieldMods.hasModifierProperty("private")).thenReturn(accessor.contains("private"));
    when(stringFieldMods.hasModifierProperty("protected")).thenReturn(
        accessor.contains("protected"));
    when(stringField.getStubOrPsiChild(JavaStubElementTypes.MODIFIER_LIST)).thenReturn(
        stringFieldMods);

    PsiType stringType = mock(PsiType.class);
    when(stringType.getCanonicalText()).thenReturn(javaType);
    when(stringType.getArrayDimensions()).thenReturn(0);
    when(stringField.getType()).thenReturn(stringType);
    return stringField;
  }
}
