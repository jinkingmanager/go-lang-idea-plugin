/*
 * Copyright 2013-2014 Sergey Ignatov, Alexander Zolotov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.goide.psi.impl;

import com.goide.GoLanguage;
import com.goide.psi.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.impl.PsiParserFacadeImpl;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("ConstantConditions")
public class GoElementFactory {
  private GoElementFactory() {
  }

  @NotNull
  private static GoFile createFileFromText(@NotNull Project project, @NotNull String text) {
    return (GoFile)PsiFileFactory.getInstance(project).createFileFromText("a.go", GoLanguage.INSTANCE, text);
  }

  @NotNull
  public static PsiElement createIdentifierFromText(@NotNull Project project, String text) {
    GoFile file = createFileFromText(project, "package " + text);
    return file.getPackage().getIdentifier();
  }

  @NotNull
  public static PsiElement createVarDefinitionFromText(@NotNull Project project, String text) {
    GoFile file = createFileFromText(project, "package p; var " + text + " = 1");
    return file.getVars().get(0);
  }

  @NotNull
  public static GoImportDeclaration createImportDeclaration(@NotNull Project project, @NotNull String importString,
                                                            @Nullable String alias, boolean withParens) {
    importString = GoPsiImplUtil.isQuotedImportString(importString) ? importString : StringUtil.wrapWithDoubleQuote(importString);
    alias = alias != null ? alias + " " : "";
    GoFile file = withParens
                  ? createFileFromText(project, "package main\nimport (\n" + alias + importString + "\n)")
                  : createFileFromText(project, "package main\nimport " + alias + importString);
    return PsiTreeUtil.findChildOfType(file, GoImportDeclaration.class);
  }

  @NotNull
  public static GoImportSpec createImportSpec(@NotNull Project project, @NotNull String importString, @Nullable String alias) {
    GoImportDeclaration importDeclaration = createImportDeclaration(project, importString, alias, true);
    return ContainerUtil.getFirstItem(importDeclaration.getImportSpecList());
  }

  @NotNull
  public static GoImportString createImportString(@NotNull Project project, @NotNull String importString) {
    GoImportSpec importSpec = createImportSpec(project, importString, null);
    return importSpec.getImportString();
  }

  @NotNull
  public static PsiElement createNewLine(@NotNull Project project) {
    return PsiParserFacadeImpl.SERVICE.getInstance(project).createWhiteSpaceFromText("\n");
  }

  @NotNull
  public static GoPackageClause createPackageClause(@NotNull Project project, @NotNull String name) {
    return createFileFromText(project, "package " + name).getPackage();
  }

  @NotNull
  public static GoBlock createBlock(@NotNull Project project) {
    return createFileFromText(project, "package a; func t() {\n}").getFunctions().get(0).getBlock();
  }

  @NotNull
  public static GoExpression createExpressionFromText(@NotNull Project project, String text) {
    GoFile file = createFileFromText(project, "package p; var a = " + text);
    return PsiTreeUtil.findChildOfType(file.getVars().get(0), GoExpression.class);
  }
}