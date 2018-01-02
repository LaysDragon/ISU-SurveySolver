package com.lays.autoPost;

import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.html.*;

import javax.swing.*;
import java.io.IOException;
import java.net.URL;
import java.util.function.Consumer;

/**
 * Created by ISU on 2016/4/14.
 */
public class main {
    static DebugWindows debug;

    public static void main(String[] args) {
        SurveySolver.init("dataBase.xml");
        SurveySolver.saveDataBase("dataBase.xml");
        //return;



        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        } catch (Exception e) {
            e.printStackTrace();
        }

        mainWindow.getInstance().setVisible(true);
    }



    public static void start(String userid,String password,boolean redoSurvey) {
        debug = new DebugWindows("DebugWindows");

        WebClient client = new WebClient();
        client.getOptions().setUseInsecureSSL(true);
        class myAlertHandler implements AlertHandler {
            boolean enabled=true;

            public void setEnabled(boolean enabled){
                this.enabled = enabled;
            }

            @Override
            public void handleAlert(Page page, final String message) {
                if(!enabled)return;

                new Thread() {
                    @Override
                    public void run() {
                        JOptionPane.showMessageDialog(null,
                                message,
                                "提醒",
                                JOptionPane.INFORMATION_MESSAGE);
                    }

                }.start();
            }
        };

        myAlertHandler alerthandler = new myAlertHandler();
        client.setAlertHandler(alerthandler);

        client.getOptions().setThrowExceptionOnScriptError(false);
        try {
            //===從主頁登入
            HtmlPage  page = (HtmlPage) client.getPage("https://netreg.isu.edu.tw/Wapp/Wap_indexmain2.asp");
            debug.htmlOutput.setText( page.asText());

            //HtmlForm loginForm = page.getFormByName("wapmain");
            //loginForm.setActionAttribute();
            ((HtmlTextInput) page.getElementsByName("logon_id").get(0)).setText(userid);
            ((HtmlPasswordInput)page.getElementsByName("txtpasswd").get(0)).setText(password);
            page = (HtmlPage) ((HtmlButton) page.getElementsByName("submit1").get(0)).click();
            if(page.getEnclosingWindow().getName().equals("popup")){
                ((TopLevelWindow)page.getEnclosingWindow()).close();
                page= (HtmlPage) client.getCurrentWindow().getEnclosedPage();
            }

            //debug.htmlOutput.setText( page.asText());

            //驗證是否登入成功
            if(page.asText().contains("輸入密碼輸入不正確")){
                JOptionPane.showMessageDialog(null,
                        "輸入密碼輸入不正確",
                        "錯誤",
                        JOptionPane.ERROR_MESSAGE);
                debug.setVisible(false);
                return;
            }else if(page.asText().contains("資料不正確 ,無法登入 !!")){
                JOptionPane.showMessageDialog(null,
                        "資料不正確 ,無法登入 !!\n",
                        "錯誤",
                        JOptionPane.ERROR_MESSAGE);
                debug.setVisible(false);
                return;
            }
            alerthandler.setEnabled(false);


            //===進入問卷頁面
            FrameWindow mainFrame = page.getFrameByName("main");
            //HtmlPage mainFramePage= (HtmlPage)mainFrame.getEnclosedPage();
            debug.htmlOutput.setText( ((HtmlPage)mainFrame.getEnclosedPage()).asText());
            HtmlPage SurveyPage = (HtmlPage) client.getPage(client.getCurrentWindow(),"main",new WebRequest(new URL("http://netreg.isu.edu.tw/wapp/wap_13/wap_130100.asp")));
            debug.htmlOutput.setText( SurveyPage.asText());
            for(HtmlTableRow rows : ((HtmlTable) SurveyPage.getElementsByTagName("table").get(2)).getRows()){
                DomNodeList<HtmlElement> inputs = rows.getElementsByTagName("input");
                if(inputs.size()==0)continue;
                debug.LogOutput.append(" == ");
                debug.LogOutput.append(" 課程代號:"+rows.getCell(0).asText()+"\t");
                debug.LogOutput.append(" 課程名稱:"+rows.getCell(1).asText()+"\t");
                debug.LogOutput.append(" 開課教師:"+rows.getCell(2).asText()+"\t");
                if(((HtmlHiddenInput)inputs.get(2)).getValueAttribute().equals("Redo_SURVEY")) {
                    debug.LogOutput.append(" [問卷表狀況:　已完成] ==\n");
                    if(redoSurvey){
                        debug.LogOutput.append("照要求重做問卷...\n");
                    }else {
                        continue;
                    }
                }else{
                    debug.LogOutput.append(" [問卷表狀況:尚未完成]");
                    debug.LogOutput.append(" == \n");
                }


                debug.LogOutput.append("嘗試做問卷...\n");
                page = ((HtmlSubmitInput) inputs.get(5)).click();
                debug.htmlOutput.setText( page.asText());
                Survey testSurvey = new Survey(page);
                //SurveySolver.addData(testSurvey.questions.get(1));
                SurveySolver.doSurvey(testSurvey);
                page = testSurvey.SubmitEle.click();
                debug.htmlOutput.setText( page.asText());
                if(page.asXml().contains("非常抱歉,"))debug.LogOutput.append("問卷作答失敗了QAQ!\n");
                else debug.LogOutput.append("問卷作答成功!!!\n");
                //testSurvey.questions.size();
                //break;
            }

            //client.getOptions().setJavaScriptEnabled(false);
            //HtmlPage  page = (HtmlPage) client.getPage("http://netreg.isu.edu.tw/wapp/wap_13/wap_130100.asp");
            debug.setVisible(false);
            JOptionPane.showMessageDialog(null,
                    "恭喜，已完成!!",
                    "完成!",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (IOException e) {
            e.printStackTrace();
        }catch ( FailingHttpStatusCodeException e ) {
            e.printStackTrace();
        }


    }

}
