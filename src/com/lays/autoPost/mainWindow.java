package com.lays.autoPost;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by LaysDragon on 2016/6/16.
 */
public class mainWindow extends JFrame {
    static mainWindow instance;



    private JTextField txtf_userid;
    private JPasswordField pass_password;
    private JButton btn_Start;
    private JButton btn_cleanDatabase;
    private JCheckBox chk_redoSurvey;
    private JButton logButton;
    private JPanel rootpanel;

    static mainWindow getInstance(){
        if(instance==null){
            return new mainWindow();
        }else{
            return instance;
        }

    }

    private mainWindow(){
        super("自動填問卷 V1.2");

        setContentPane(rootpanel);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        pack();

        setLocationRelativeTo(null);
        btn_Start.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new Thread()
                {
                    public void run() {
                        main.start(txtf_userid.getText().trim(), String.valueOf(pass_password.getPassword()).trim(), chk_redoSurvey.isSelected());
                    }
                }.start();

            }
        });

        logButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                main.debug.setVisible(true);
            }
        });

        btn_cleanDatabase.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int result = JOptionPane.showConfirmDialog(null, "確定要清除資料庫嗎?\n清除後至少需要重新填一次問題!", "確定要刪除嗎?", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (result == JOptionPane.YES_OPTION) {
                    SurveySolver.cleanerDatabase();
                }
            }

        });

    }
}