package com.lays.autoPost;

import com.gargoylesoftware.htmlunit.html.*;

import java.util.Vector;

/**
 * Created by LaysDragon on 2016/4/14.
 */
public class Survey {
    Vector<Question> questions = new Vector<Question>();
    HtmlSubmitInput SubmitEle;
    HtmlPage surveyPage;
    public Survey(HtmlPage page) {
        surveyPage=page;
        ParseSurvey();


    }

    private void ParseSurvey(){
        HtmlTable table= (HtmlTable) (surveyPage.getElementsByTagName("table").get(0));
        rowFor:
        for(HtmlTableRow row : table.getRows()) {
            DomNodeList<HtmlElement> inputs = row.getElementsByTagName("input");
            if (inputs.size() == 0) continue;
            inputRow:{
                for (HtmlElement input : inputs) {
                    String type = ((HtmlInput) input).getTypeAttribute();
                    switch(type){
                        case "submit":
                            SubmitEle = (HtmlSubmitInput) input;
                            continue rowFor;
                        case "checkbox":
                        case "radio":
                        case "text":
                        case "input":
                            break inputRow;
                        default:
                            continue rowFor;
                    }
                }
            }

            try {
                questions.add(new Question(row.getCell(1)));
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }
    }


    public class Question {
        Vector<Option> options = new Vector<Option>();
        String Topic="";
        public Question(HtmlTableCell tableCell) throws Exception {
            DomNodeList<DomNode> nodes = ((DomNode) tableCell).getChildNodes();
            HtmlElement findedEle=null;
            String findedAnswer="";
            for (DomNode node:nodes){


                if(node instanceof HtmlRadioButtonInput) {
                    if(Topic.equals("") && !findedAnswer.equals("")){
                        Topic=findedAnswer;
                        findedAnswer="";
                    }
                    if(findedEle!=null){
                        if(findedAnswer.equals(""))
                            throw new Exception("重複出現的input?!ˊ真怪!");
                        else{
                            options.add(new Option((HtmlInput) findedEle, findedAnswer));
                            findedEle=null;
                            findedAnswer = "";
                        }
                    }
                    findedEle = (HtmlElement) node;
                    continue;
                }
                if(node instanceof HtmlCheckBoxInput) {
                    if(Topic.equals("") && !findedAnswer.equals("")){
                        Topic=findedAnswer;
                        findedAnswer="";
                    }
                    if(findedEle!=null){
                        if(findedAnswer.equals(""))
                            throw new Exception("重複出現的input?!ˊ真怪!");
                        else{
                            options.add(new Option((HtmlInput) findedEle, findedAnswer));
                            findedEle=null;
                            findedAnswer = "";
                        }
                    }
                    findedEle = (HtmlElement) node;
                    continue;
                }

                if(node instanceof DomText) {
                    if(!findedAnswer.equals("")){
                        System.out.println("出現第二textNode，串起來用吧!");
                    }

                    String text = ((DomText) node).asText().trim().trim().replaceAll("^　+|　+$", "").trim();
                    if(text.equals("")) continue;

                    if(findedEle==null && !Topic.equals("")){
                        throw new Exception("還沒出現input就出現選項答案了，真怪!");
                    }
                    findedAnswer += text;
                    continue;
                }

                if(node instanceof HtmlTextInput) {
                    if(findedEle!=null){
                        if(findedAnswer.equals(""))
                            throw new Exception("重複出現的input?!ˊ真怪!");
                        else{
                            options.add(new Option((HtmlInput) findedEle, findedAnswer));
                            findedEle=null;
                            findedAnswer = "";
                        }
                    }
                    findedEle = (HtmlElement) node;
                    options.get(options.size()-1).setAddtionalElement((HtmlInput) findedEle);
                    findedEle=null;
                    continue;
                }

            }
            if(findedEle!=null && !findedAnswer.equals("") ){
                options.add(new Option((HtmlInput) findedEle, findedAnswer));
            }
        }

        public class Option {
            final String answer;
            HtmlInput htmlElement;

            public HtmlInput getAddtionalElement() {
                return addtionalElement;
            }
            public void setAddtionalElement(HtmlInput addtionalElement) {
                this.addtionalElement = this.addtionalElement==null?addtionalElement:this.addtionalElement;
            }
            private HtmlInput addtionalElement;

            public Option(HtmlInput ele, String answer) {
                this.htmlElement = ele;
                this.answer=answer.trim();
            }

            public void setChecked(boolean value){
                switch (htmlElement.getTypeAttribute()){
                    case "checkbox":
                        ((HtmlCheckBoxInput)htmlElement).setChecked(value);
                        break;
                    case "radio":
                        ((HtmlRadioButtonInput)htmlElement).setChecked(value);
                        break;

                }
            }

            public boolean isChecked(){
                switch (htmlElement.getTypeAttribute()){
                    case "checkbox":
                        return ((HtmlCheckBoxInput)htmlElement).isChecked();
                    case "radio":
                        return ((HtmlRadioButtonInput)htmlElement).isChecked();
                    default:
                        return htmlElement.isChecked();
                }
            }
            public String getType(){
                return htmlElement.getTypeAttribute();
            }


            public String getName(){
                return htmlElement.getNameAttribute();
            }

            public String getID(){
                return htmlElement.getId();
            }

        }
    }
}
