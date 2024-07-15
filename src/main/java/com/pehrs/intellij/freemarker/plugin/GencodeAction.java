/*
 * Copyright (c) 2024. Matti Pehrs (matti@pehrs.com)
 */

package com.pehrs.intellij.freemarker.plugin;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.CommonClassNames;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiType;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.lang.JavaVersion;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.stream.Collectors;
//import org.apache.velocity.VelocityContext;
//import org.apache.velocity.app.VelocityEngine;
//import org.apache.velocity.runtime.RuntimeConstants;
//import org.apache.velocity.runtime.parser.ParseException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GencodeAction extends AnAction {

  private static final Logger LOG = Logger.getInstance(GencodeAction.class);

  static Configuration fmCfg = new Configuration(Configuration.VERSION_2_3_33);
  static {
    // cfg.setDirectoryForTemplateLoading(new File("/home/matti/freemarker-templates"));
    fmCfg.setDefaultEncoding("UTF-8");
    fmCfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
    fmCfg.setLogTemplateExceptions(false);
    fmCfg.setWrapUncheckedExceptions(true);
    fmCfg.setFallbackOnNullLoopVariable(false);
    fmCfg.setSQLDateAndTimeTimeZone(TimeZone.getDefault());
  }

  @Override
  public void update(@NotNull AnActionEvent actionEvent) {
    VirtualFile projectRoot = actionEvent.getProject().getProjectFile().getParent().getParent();
    @Nullable PsiFile psiFile = actionEvent.getData(CommonDataKeys.PSI_FILE);

    // Make sure we are only enabled for Java files
    actionEvent.getPresentation().setEnabledAndVisible(psiFile instanceof PsiJavaFile);
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent event) {
    // Using the event, implement an action.
    // For example, create and show a dialog.

    Project currentProject = event.getProject();

    @Nullable PsiFile psiFile = event.getData(
        CommonDataKeys.PSI_FILE);
    // LOG.info("file: " + psiFile);

//    @Nullable PsiElement psiElement = event.getData(
//        CommonDataKeys.PSI_ELEMENT);
//    System.out.println("element: " + psiElement);

//    StringBuilder message =
//        new StringBuilder(event.getPresentation().getText() + " Selected!");
//    // If an element is selected in the editor, add info about it.
//    Navigatable selectedElement = event.getData(CommonDataKeys.NAVIGATABLE);
//    if (selectedElement != null) {
//      message.append("\nSelected Element: ").append(selectedElement);
//    }
//    String title = event.getPresentation().getDescription();
//    Messages.showMessageDialog(
//        currentProject,
//        message.toString(),
//        title,
//        Messages.getInformationIcon());
    try {
      SelectTemplateDialog templateDialog = new SelectTemplateDialog();
      if (templateDialog.showAndGet()) {
        String generatedCode = "";
        if (psiFile != null) {
          if (psiFile instanceof PsiJavaFile psiJavaFile) {
            @NotNull JavaVersion javaVersion = psiJavaFile.getLanguageLevel()
                .toJavaVersion();

            PsiClass[] classes = psiJavaFile.getClasses();
            if (classes != null && classes.length > 0) {
              String selectedTemplate = templateDialog.getSelectedTemplate();
              LOG.info("selected: " + selectedTemplate);
              if (selectedTemplate != null) {
                PsiClass klass = classes[0];
                LOG.info("CLASS " + klass.getName());
                for (PsiField field : klass.getAllFields()) {
                  LOG.info("  field: " + field.getName());
                }
                try {
                  String template = TemplateRepo.getTemplate(selectedTemplate)
                      .orElseGet(() -> null);
                  LOG.info("TEMPLATE: " + template);
                  generatedCode = freeMarkerGenerateCode(javaVersion, klass, template);
                } catch (Exception e) {
                  throw new RuntimeException(e);
                }
              }
            }

            final String codeToInsert = generatedCode;
            Editor editor = event.getData(CommonDataKeys.EDITOR);
            // Application app = ApplicationManager.getApplication();

            WriteCommandAction.runWriteCommandAction(currentProject, () -> {
              // EditorModificationUtil.insertStringAtCaret(editor, codeToInsert);
              @NotNull Document doc = editor.getDocument();
              doc.insertString(editor.getCaretModel().getOffset(), codeToInsert);
            });
          }
        }

      }
    } catch (Exception ex) {
      LOG.error(ex.getMessage(), ex);
    }
  }


  public static Map<String, Object> getClassFreemarkerModel(Class theClass) {
    Map<String, Object> model = new HashMap<>();

    model.put("theClass", theClass);
    model.put("className", theClass.getName());
    model.put("classSimpleName", theClass.getSimpleName());
    List<Map<String, String>> fields = Arrays.asList(theClass.getDeclaredFields())
        .stream().map(field -> {

          Class<?> theType = field.getType();
          String typeDecl = "" + theType.getCanonicalName();

          Type genericType = field.getGenericType();
          if (genericType instanceof ParameterizedType) {
            ParameterizedType pType = (ParameterizedType) genericType;
            String tn = pType.getRawType().getTypeName();
            String params = Arrays.stream(pType.getActualTypeArguments())
                .map(t -> t.getTypeName())
                .collect(Collectors.joining(","));
            typeDecl = "" + tn + "<" + params + ">";
          }

          return Map.of(
              "name", field.getName(),
              "type", "" + typeDecl
          );
        }).toList();
    model.put("fields", fields);

    return model;
  }

  protected String freeMarkerGenerateCode(
      @NotNull JavaVersion javaVersion, PsiClass psiClass, String templateStr)
      throws IOException, TemplateException {

    DefaultObjectWrapper objectMapper = new DefaultObjectWrapper(Configuration.VERSION_2_3_33);

    Map<String, Object> model = new HashMap<>();

    model.put("date", "" + new Date());
    model.put("class", psiClass);
    model.put("className", psiClass.getName());
    model.put("allFields",
        Arrays.stream(psiClass.getAllFields()).map(field -> FieldDeclaration.fromPsiField(field))
            .toList()
    );
    model.put("fields",
        Arrays.stream(psiClass.getFields()).map(field -> FieldDeclaration.fromPsiField(field))
            .toList()
    );
    model.put("javaVersion", javaVersion);
    model.put("javaFeatureVersion", javaVersion.feature);
    model.put("javaMinorVersion", javaVersion.minor);
    model.put("javaUpdateVersion", javaVersion.update);

    Template temp = new Template("codegen", new StringReader(templateStr), new Configuration());
    Writer out = new StringWriter();
    temp.process(model, out);

    return out.toString();
  }

//  private String velocityGenerateCode(
//      Project project,
//      @NotNull JavaVersion javaVersion, PsiClass klass, String template) throws ParseException {
//
//    // FIXME:
//    //   without the following class loader initialization, we get this
//    //   exception when running as plugin:
//    //   org.apache.velocity.exception.VelocityException: The specified class for ResourceManager
//    //   (org.apache.velocity.runtime.resource.ResourceManagerImpl) does not
//    //   implement org.apache.velocity.runtime.resource.ResourceManager;
//    //   Velocity is not initialized correctly.
//    final Thread currentThread = Thread.currentThread();
//    final ClassLoader originalClassLoader = currentThread.getContextClassLoader();
//    final ClassLoader pluginClassLoader = currentThread.getContextClassLoader();
//
//    try {
//      currentThread.setContextClassLoader(pluginClassLoader);
//
//      VelocityEngine engine = new VelocityEngine();
//      engine.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
//      // engine.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
//      engine.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM, new MyNullLogChute());
//      // engine.setProperty(Velocity.RUNTIME_LOG_LOGSYSTEM_CLASS, MyNullLogChute.class.getName());
//
//      engine.init();
//
//      VelocityContext context = new VelocityContext();
//      context.put("StringUtils", org.apache.commons.lang3.StringUtils.class);
//      context.put("date", new Date());
//      context.put("class", klass);
//      context.put("allFields",
//          Arrays.stream(klass.getAllFields()).map(field -> FieldDeclaration.fromPsiField(field))
//              .toList()
//      );
//      context.put("fields",
//          Arrays.stream(klass.getFields()).map(field -> FieldDeclaration.fromPsiField(field))
//              .toList()
//      );
//      context.put("javaVersion", javaVersion);
//      context.put("javaFeatureVersion", javaVersion.feature);
//      context.put("javaMinorVersion", javaVersion.minor);
//      context.put("javaUpdateVersion", javaVersion.update);
//
//      @NotNull PsiClassType psiType = PsiType.getTypeByName(
//          CommonClassNames.JAVA_LANG_CLASS, project, GlobalSearchScope.EMPTY_SCOPE);
//      for (PsiField field : klass.getAllFields()) {
//        @NotNull PsiType t = field.getType();
//
//        System.out.println("is assignable from List: " +
//            field.getType().isAssignableFrom(psiType));
//
//        System.out.println("type: " + t.getCanonicalText());
//      }
//
//      StringWriter writer = new StringWriter();
////    String template = """
////        public void mergeFrom($class.getName() from) {
////        #foreach($field in $fields)
////             this.set$StringUtils.capitalize($field.getName())(from.get$StringUtils.capitalize($field.getName())());
////        #end
////        }
////            """;
//      engine.evaluate(context, writer, "LOG_TAG", template);
//
//      LOG.info("GENERATED:\n" + writer.toString());
//
//      return writer.toString();
//    } finally {
//      // FIXME: set back default class loader
//      currentThread.setContextClassLoader(originalClassLoader);
//    }
//
//  }
}
