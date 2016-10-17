package com.lays.autoPost;

import javax.swing.*;
import java.awt.*;

/**
 * Created by LaysDragon on 2016/4/15.
 */
public class SurveyForm  extends JFrame {
    public JPanel rootPanel;

    public SurveyForm(String title) throws HeadlessException {
        super(title);


        setContentPane(rootPanel);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setResizable(false);
        pack();
        setLocationRelativeTo(null);
        //setSize(1000,1000);
        setVisible(true);


    }
}
