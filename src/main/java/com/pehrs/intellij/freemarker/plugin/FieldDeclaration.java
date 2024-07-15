/*
 * Copyright (c) 2024. Matti Pehrs (matti@pehrs.com)
 */

package com.pehrs.intellij.freemarker.plugin;

import com.intellij.psi.PsiField;
import com.intellij.psi.PsiModifierList;
import com.intellij.psi.impl.java.stubs.JavaStubElementTypes;
import com.intellij.psi.impl.source.PsiFieldImpl;
import org.jetbrains.annotations.Nullable;

public class FieldDeclaration {

  private String type;
  private String name;

  private boolean isStatic;

  private boolean isCollection;
  private boolean isSet;
  private boolean isList;

  private boolean isArray;

  private boolean isPublic;
  private boolean isPrivate;
  private boolean isProtected;

  public FieldDeclaration(String type, String name, boolean isStatic, boolean isCollection,
      boolean isSet, boolean isList, boolean isArray, boolean isPublic, boolean isPrivate,
      boolean isProtected) {
    this.type = type;
    this.name = name;
    this.isStatic = isStatic;
    this.isCollection = isCollection;
    this.isSet = isSet;
    this.isList = isList;
    this.isArray = isArray;
    this.isPublic = isPublic;
    this.isPrivate = isPrivate;
    this.isProtected = isProtected;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public boolean isStatic() {
    return isStatic;
  }

  public void setStatic(boolean aStatic) {
    isStatic = aStatic;
  }

  public boolean isCollection() {
    return isCollection;
  }

  public void setCollection(boolean collection) {
    isCollection = collection;
  }

  public boolean isSet() {
    return isSet;
  }

  public void setSet(boolean set) {
    isSet = set;
  }

  public boolean isList() {
    return isList;
  }

  public void setList(boolean list) {
    isList = list;
  }

  public boolean isArray() {
    return isArray;
  }

  public void setArray(boolean array) {
    isArray = array;
  }

  public boolean isPublic() {
    return isPublic;
  }

  public void setPublic(boolean aPublic) {
    isPublic = aPublic;
  }

  public boolean isPrivate() {
    return isPrivate;
  }

  public void setPrivate(boolean aPrivate) {
    isPrivate = aPrivate;
  }

  public boolean isProtected() {
    return isProtected;
  }

  public void setProtected(boolean aProtected) {
    isProtected = aProtected;
  }

  public static FieldDeclaration fromPsiField(PsiField psiField) {
    String name = psiField.getName();
    String type = psiField.getType().getCanonicalText();

    if (psiField instanceof PsiFieldImpl psiFieldImpl) {
      @Nullable PsiModifierList mods =
          psiFieldImpl.getStubOrPsiChild(JavaStubElementTypes.MODIFIER_LIST);

      boolean isStatic = mods.hasModifierProperty("static");
      boolean isPublic = mods.hasModifierProperty("public");
      boolean isPrivate = mods.hasModifierProperty("private");
      boolean isProtected = mods.hasModifierProperty("protected");

      boolean isArray = psiFieldImpl.getType().getArrayDimensions() > 0;

      boolean isList = type.startsWith("java.util.List");
      boolean isSet = type.startsWith("java.util.Set");
      boolean isCollection = isList || isSet;

      return new FieldDeclaration(type, name, isStatic, isCollection, isSet, isList, isArray, isPublic, isPrivate, isProtected);
    } else {
      throw new IllegalStateException("Not supported PsiField: " + psiField.getClass().getName());
    }
  }

}
