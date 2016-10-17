package com.lays.autoPost;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

/**
 * Created by ISU on 2016/4/14.
 */
public class DebugWindows extends JFrame {
    public JTextArea LogOutput;
    public JTextArea htmlOutput;
    private JPanel rootPanel;

    public DebugWindows(String title) throws HeadlessException {
        super(title);

        LogOutput.setEditable(false);

        LogOutput.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                update(e);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                update(e);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                update(e);
            }
            void update(DocumentEvent e){
                LogOutput.setCaretPosition(LogOutput.getText().length());
            }
        });


        htmlOutput.setEditable(false);
        setContentPane(rootPanel);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

        pack();
        setSize(700,700);
        setLocationRelativeTo(null);
        setVisible(true);
    }


}
