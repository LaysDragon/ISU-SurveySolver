package com.lays.autoPost;

import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import org.dom4j.*;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.dom.DOMElement;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.dom4j.tree.DefaultElement;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Created by LaysDragon on 2016/4/15.
 */
public class SurveySolver {
    static Document SurveyDetabase;
    static String dataBasePath;

    static void init(String path){
        dataBasePath=path;
        SAXReader reader = new SAXReader();
        try {
            SurveyDetabase = reader.read(path);
        } catch (DocumentException e) {
            if(e.getCause() instanceof FileNotFoundException){
                System.out.println("找不到檔案!!\n創建新的資料庫");
                creatDetabase(path);
            }else
                e.printStackTrace();

        }

    }

    static void creatDetabase(String path){
        SurveyDetabase = DocumentHelper.createDocument();
        //SurveyDetabase.addProcessingInstruction("xml", "type=\"user-data\"");
        Element root = SurveyDetabase.addElement("Survey");
        saveDataBase(path);

    }

    static void saveDataBase(String path){
        try {
//            FileWriter fw = new FileWriter(path);
            OutputStreamWriter fw = new OutputStreamWriter(new FileOutputStream(path), StandardCharsets.UTF_8);
            OutputFormat of = OutputFormat.createPrettyPrint();
            //OutputFormat of = new OutputFormat(); // 格式化XML
            of.setIndentSize(4); // 設定 Tab 為 4 個空白
            of.setNewlines(true);// 設定 自動換行
            of.setLineSeparator("\r\n");
            of.setEncoding("UTF-8");
            XMLWriter xw = new XMLWriter(fw, of);
            SurveyDetabase.setXMLEncoding("UTF-8");
            xw.write(SurveyDetabase);
            xw.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    static void cleanerDatabase(){
        SurveyDetabase.getRootElement().clearContent();
        saveDataBase(dataBasePath);
    }

    static boolean addData(Survey.Question question){
        if(searchQuestion(question)!=null)return false;


        DefaultElement root = (DefaultElement) SurveyDetabase.getRootElement();
        DOMElement newQuestionData = new DOMElement("question");
        newQuestionData.addElement("topic").setText(question.Topic);

        for(Survey.Question.Option option: question.options){
            if(option.getType().equals("checkbox")){
                Element input = new DOMElement("checkbox");
                input.addAttribute("answer",option.answer);
                input.addAttribute("isChecked",option.isChecked()?"YES":"NO");
                if(option.getAddtionalElement()!=null){
                    input.addElement("addtionalAnswer").setText(((HtmlTextInput)option.getAddtionalElement()).getText());
                }
                newQuestionData.add(input);
                continue;
            }
            if(option.getType().equals("radio")){
//                DOMElement radios;
//                NodeList radiosList = newQuestionData.getElementsByTagName("radios");
//                if(radiosList.getLength()!=0){
//                    for(int x =0;x<radiosList.getLength();x++){
//                        if(((DOMElement)radiosList.item(x)).getAttribute("name")==option.getName()){
//                            radios=((DOMElement)radiosList.item(x));
//                            break;
//                        };
//                    }
//                }
//                if(radios==null){
//                    radios=new DOMElement("radios");
//                    radios.addAttribute("name",option.getName());
//                }

                Element input = new DOMElement("radio");
                input.addAttribute("answer",option.answer);
                input.addAttribute("isChecked",option.isChecked()?"YES":"NO");
                input.addAttribute("name", option.getName());
                if(option.getAddtionalElement()!=null){
                    input.addElement("addtionalAnswer").setText(((HtmlTextInput)option.getAddtionalElement()).getText());
                }
                newQuestionData.add(input);
                continue;
            }
        }
        root.add(newQuestionData);
        saveDataBase(dataBasePath);
        return true;
    }

    static DefaultElement searchQuestion(Survey.Question question){
        DefaultElement root = (DefaultElement) SurveyDetabase.getRootElement();
        List<Node> result = root.selectNodes("//question[topic='" + question.Topic + "']");

        questions:
        for(int x=0;x<result.size();x++){
            DefaultElement ele= (DefaultElement) result.get(x);
            for(Survey.Question.Option option: question.options){
                DefaultElement finded = (DefaultElement) ele.selectSingleNode("//"+option.getType()+"[@answer='"+option.answer+"']");
                //finded.get
                if(finded==null)continue questions;

                if(option.getAddtionalElement()!=null){
                    if(finded.selectSingleNode("//addtionalAnswer")==null){
                        continue questions;
                    }
                }

            }
            return ele;
        }
        return null;

    }

    static void doSurvey(Survey survey){
        for(Survey.Question question:survey.questions){

            DefaultElement answer = searchQuestion(question);
            if(answer!=null){
                doQuestion(question,answer);
            }else {
                askQuestion(question);
            }
        }
        main.debug.LogOutput.append("問卷完成了!!\n");
    }



    static void doQuestion(Survey.Question question , DefaultElement answer){
        for(Survey.Question.Option option: question.options){
            DefaultElement answerOption= (DefaultElement) answer.selectSingleNode("./"+option.getType()+"[@answer='"+option.answer+"']");
            option.setChecked(answerOption.attributeValue("isChecked").equals("YES"));
            Node addtional = answerOption.selectSingleNode("./addtionalAnswer");
            if(addtional !=null)((HtmlTextInput)option.getAddtionalElement()).setText(addtional.getText());
        }
    }

    static void askQuestion(Survey.Question question){

        SurveyForm ask = new SurveyForm("新的問題!");

        int count  = 0;

        GridBagConstraints grid = new GridBagConstraints();
        grid.gridx=0;
        grid.gridy=count;
        grid.anchor=GridBagConstraints.WEST;
        grid.gridwidth=2;

        count++;
        ask.add(new JLabel(question.Topic),grid);

        ButtonGroup group=new ButtonGroup();



        for(Survey.Question.Option option: question.options){

            grid = new GridBagConstraints();
            grid.gridx=0;
            grid.gridy=count;
            grid.anchor=GridBagConstraints.WEST;

            if(option.getType().equals("radio")){
                JRadioButton radio = new JRadioButton(option.answer);
                group.add(radio);
                //同步顯示網頁預設值
                radio.setSelected(option.isChecked());
                radio.addItemListener(new ItemListener() {
                    Survey.Question.Option _option;
                    public ItemListener setOption(Survey.Question.Option option){
                        //super();
                        _option=option;
                        return this;
                    }

                    @Override
                    public void itemStateChanged(ItemEvent e) {
                        _option.setChecked(e.getStateChange()==1?true:false);
                    }
                }.setOption(option));

                ask.rootPanel.add(radio,grid);
                if(option.getAddtionalElement()!=null){
                    grid = new GridBagConstraints();
                    grid.gridx=1;
                    grid.gridy=count;
                    grid.anchor=GridBagConstraints.WEST;

                    JTextField textfield = new JTextField(20);
                    //同步顯示網頁預設值
                    textfield.setText(((HtmlTextInput)option.getAddtionalElement()).getText());
                    textfield.getDocument().addDocumentListener(new DocumentListener() {
                        Survey.Question.Option _option;

                        public DocumentListener setOption(Survey.Question.Option option) {
                            //super();
                            _option = option;
                            return this;
                        }
                        @Override
                        public void insertUpdate(DocumentEvent e) {
                            change(e);
                        }

                        @Override
                        public void removeUpdate(DocumentEvent e) {
                            change(e);
                        }

                        @Override
                        public void changedUpdate(DocumentEvent e) {
                            change(e);
                        }

                        void change(DocumentEvent e){
                            try {
                                ((HtmlTextInput)_option.getAddtionalElement()).setText(e.getDocument().getText(0,e.getDocument().getLength()));
                            } catch (BadLocationException e1) {
                                e1.printStackTrace();
                            }
                        }


                    }.setOption(option));


                    ask.rootPanel.add(textfield,grid);
                }


            }

            if(option.getType().equals("checkbox")){
                JCheckBox checkbox = new JCheckBox(option.answer);
                //同步顯示網頁預設值
                checkbox.setSelected(option.isChecked());
                checkbox.addItemListener(new ItemListener() {
                    Survey.Question.Option _option;

                    public ItemListener setOption(Survey.Question.Option option) {
                        //super();
                        _option = option;
                        return this;
                    }

                    @Override
                    public void itemStateChanged(ItemEvent e) {
                        _option.setChecked(e.getStateChange() == 1 ? true : false);
                    }
                }.setOption(option));

                ask.rootPanel.add(checkbox,grid);
                if(option.getAddtionalElement()!=null){
                    grid = new GridBagConstraints();
                    grid.gridx=1;
                    grid.gridy=count;
                    grid.anchor=GridBagConstraints.WEST;

                    JTextField textfield = new JTextField(20);
                    textfield.setText(((HtmlTextInput)option.getAddtionalElement()).getText());
                    textfield.getDocument().addDocumentListener(new DocumentListener() {
                        Survey.Question.Option _option;

                        public DocumentListener setOption(Survey.Question.Option option) {
                            //super();
                            _option = option;
                            return this;
                        }
                        @Override
                        public void insertUpdate(DocumentEvent e) {
                            change(e);
                        }

                        @Override
                        public void removeUpdate(DocumentEvent e) {
                            change(e);
                        }

                        @Override
                        public void changedUpdate(DocumentEvent e) {
                            change(e);
                        }

                        void change(DocumentEvent e){
                            try {
                                ((HtmlTextInput)_option.getAddtionalElement()).setText(e.getDocument().getText(0,e.getDocument().getLength()));
                            } catch (BadLocationException e1) {
                                e1.printStackTrace();
                            }
                        }


                    }.setOption(option));


                    ask.rootPanel.add(textfield,grid);
                }



            }



            count++;
        }




        grid = new GridBagConstraints();
        grid.gridx=0;
        grid.gridy=count;
        grid.fill=GridBagConstraints.HORIZONTAL;
        //grid.gridwidth=2;

        JButton FinishBut = new JButton("確認");
        Waiter wait=new Waiter();
        FinishBut.addActionListener(wait);
        wait.setQuestion(question);
        ask.rootPanel.add(FinishBut,grid);
        ask.pack();
        wait.waitFor();
        ask.setVisible(false);
        if(!addData(question))main.debug.LogOutput.append("咦?真怪，問完資料後資料居然重複了!?\n");

        //強制重新做一次答案避免有沒被覆寫值的選項
        //doQuestion(question,searchQuestion(question));
    }


    private static class Waiter implements ActionListener
    {
        private final CountDownLatch latch = new CountDownLatch(1);

        Survey.Question _question;

        @Override
        public void actionPerformed(ActionEvent e)
        {
            boolean noCheckedEverything=true;
            for(Survey.Question.Option option: _question.options){
                if(option.isChecked()){
                    noCheckedEverything=false;
                    break;
                }

            }
            if(noCheckedEverything){
                JOptionPane.showMessageDialog(null,
                        "請勾選答案!",
                        "錯誤!",
                        JOptionPane.ERROR_MESSAGE);
                return ;
            }
            latch.countDown();
        }

        void waitFor()
        {
            try
            {
                latch.await();
            }
            catch (InterruptedException e)
            {
                Thread.currentThread().interrupt();
            }
        }

        void setQuestion( Survey.Question question){
            this._question = question;
        }
    }
}
