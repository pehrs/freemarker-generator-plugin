/*
 * Copyright (c) 2024. Matti Pehrs (matti@pehrs.com)
 */

package com.pehrs.intellij.freemarker.plugin;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public abstract class TemplateRepo {

  static final String home = System.getenv("HOME");

  static final Path configPath = Path.of(home, ".config", "freemarker-generator-plugin");
  static final Path templatesPath = Path.of(configPath.toFile().getAbsolutePath());

  static final String freeMarkerSampleTemplate = """
      public void mergeFrom(${class.getName()} from) {
      <#list allFields as field>
          <#if field.isStatic?string('yes','no') == 'no'>
          this.set${field.name?cap_first}(from.get${field.name?cap_first}());
          </#if>
      </#list>
      }
""";

  public static List<File> getTemplateFiles() {

    File templatesDir = templatesPath.toFile();
    // Make sure path exists
    if (!templatesDir.exists()) {
      templatesDir.mkdirs();
    }

    File[] templateFiles = templatesDir.listFiles(new FileFilter() {
      @Override
      public boolean accept(File file) {
        return file.getName().endsWith(".fm");
      }
    });

    if (templateFiles.length == 0) {
      // Add a sample template
      Path samplePath = Paths.get(templatesDir.getAbsolutePath(), "mergeFrom.fm");
      try (BufferedWriter writer = new BufferedWriter(new FileWriter(samplePath.toFile(), false))) {
        writer.write(freeMarkerSampleTemplate);
        writer.newLine();
      } catch (IOException e) {
        e.printStackTrace();
      }
      templateFiles = new File[]{
          samplePath.toFile()
      };
    }

    return Arrays.asList(templateFiles);
  }

  public static List<String> getTemplateNames() {
    return getTemplateFiles().stream()
        .map(file -> file.getName().replace(".fm", ""))
        .toList();
  }

  public static Optional<String> getTemplate(String name) {
    Path templatePath = Paths.get(templatesPath.toFile().getAbsolutePath(), name + ".fm");
    if (templatePath.toFile().exists()) {
      try {
        String content = new String(Files.readAllBytes(templatePath), StandardCharsets.UTF_8);
        return Optional.of(content);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    } else {
      return Optional.empty();
    }
  }
}
