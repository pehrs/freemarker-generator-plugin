/*
 * Copyright (c) 2024. Matti Pehrs (matti@pehrs.com)
 */

package com.pehrs.intellij.freemarker.plugin;


import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.util.lang.JavaVersion;
import freemarker.template.TemplateException;
import java.io.IOException;
import org.apache.velocity.runtime.parser.ParseException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class GencodeActionTest {

//  @Test
//  public void velocityTest() {
//    VelocityContext context = new VelocityContext();
//    context.put("StringUtils", org.apache.commons.lang3.StringUtils.class);
//    context.put("classname", "ClassName");
//    context.put("fields", List.of(
//        new FieldDeclaration("java.lang.String", "name", false, false, false, false, false, true,
//            false, false),
//        new FieldDeclaration("java.math.BigDecimal", "value", false, false, false, false, false,
//            true, false, false)
//    ));
//    StringWriter writer = new StringWriter();
//    VelocityEngine engine = new VelocityEngine();
//    engine.init();
//    String template = """
//        // $classname
//        #foreach($field in $fields)
//        this.set$StringUtils.capitalize($field.name)(other.get$StringUtils.capitalize($field.name)());
//        #end
//                    """;
//    engine.evaluate(context, writer, "LOG_TAG", template);
//
//    // Assert.assertEquals("Username is matti", writer.toString());
//    System.out.println(writer.toString());
//  }

  @Test
  public void freemarkerPsiTest() throws IOException, TemplateException, ParseException {
    GencodeAction action = new GencodeAction();

    JavaVersion javaVersion = JavaVersion.current();

    PsiClass psiClass = mock(PsiClass.class);
    when(psiClass.getName()).thenReturn(TestClass.class.getName());
    PsiField[] allFields = {
        PsiUtils.getSimplePsiField("private", "java.lang.String", "stringField"),
        PsiUtils.getSimplePsiField("private", "java.lang.Boolean", "booleanField"),
        PsiUtils.getSimplePsiField("private", "java.util.List<java.lang.String>", "listOfStringsField"),
        PsiUtils.getSimplePsiField("private", "java.util.Map<java.lang.Integer, java.lang.String>", "mapIntToStrField"),
    };
    PsiField[] declaredFields = allFields;
    when(psiClass.getAllFields()).thenReturn(allFields);
    when(psiClass.getFields()).thenReturn(declaredFields);

    String templateStr = """
        public void func() {
            // ${date}
            // ${class}
            
            <#list class.getAllFields() as field>
                // (from PsiClass) field: 
                ${field.type.getCanonicalText()} ${field.name};
            </#list>
            
             <#list allFields as field>
                // (all) field: 
                ${field.type} ${field.name};
            </#list>
        }
        """;
    String mergeTemplate = """
public void mergeFrom(${class.getName()} from) {
<#list allFields as field>
    <#if field.isStatic()?string('yes','no') == 'no'>
    this.set${field.name?cap_first}(${field.name})(from.get${field.name?cap_first}());
    </#if>
</#list>
}
        """;
    String result = action.freeMarkerGenerateCode(javaVersion, psiClass, mergeTemplate);
    System.out.println(result);
  }


}