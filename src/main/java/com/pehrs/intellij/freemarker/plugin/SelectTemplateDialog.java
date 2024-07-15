/*
 * Copyright (c) 2024. Matti Pehrs (matti@pehrs.com)
 */

package com.pehrs.intellij.freemarker.plugin;

import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBList;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SelectTemplateDialog extends DialogWrapper {

  JBList list;

  public SelectTemplateDialog() {
    super(true); // use current window as parent
    setTitle("Select template");
    init();

  }
  @NotNull
  @Override
  protected Action[] createActions() {
    super.createDefaultActions();
    // return right hand side action buttons
    return new Action[] { };
  }

  @NotNull
  protected Action[] createLeftSideActions() {
    // return left hand side action buttons
    return new Action[] {  };
  }

  public String getSelectedTemplate() {
    return (String)this.list.getSelectedValue();
  }

  @Nullable
  @Override
  protected JComponent createCenterPanel() {
    JPanel dialogPanel = new JPanel(new BorderLayout());

    List<String> templates = TemplateRepo.getTemplateNames();

    list = new JBList(templates);
    list.setMinimumSize(new Dimension(200, 100));
    list.setSelectedIndex(0);
    list.setEmptyText("No templates configured.");
    dialogPanel.add(list, BorderLayout.CENTER);

    list.addKeyListener(new KeyAdapter() {
      @Override
      public void keyTyped(KeyEvent e) {
        if(e.getKeyChar() == '\n') {
          close(OK_EXIT_CODE, true);
        }
        super.keyTyped(e);
      }
    });

    return dialogPanel;
  }

}
