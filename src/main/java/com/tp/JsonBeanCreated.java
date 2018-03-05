package com.tp;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class JsonBeanCreated {

    StringBuffer javaBean = new StringBuffer();

    //默认class name
    String  className = "JavaBeanClass";

    List<JsonBeanCreated> childrenClass = new LinkedList<>();
    boolean isChildrenClass = false ;


    public static JsonBeanCreated createdJavaBean(JSONObject jsonObject,boolean isChildren,String className){

        JsonBeanCreated jsonBean = new JsonBeanCreated();
        jsonBean.className = className;
        jsonBean.isChildrenClass = isChildren;

        if (!isChildren){
            jsonBean.addDeafultImprot("");
        }
        jsonBean.createClass(jsonBean.className);// create start

        jsonBean.parseFeildClass(jsonObject);
        jsonBean.parseStringMethod();//创建解析方法
        jsonBean.parseMethod(jsonObject);//创建解析方法

        jsonBean.parseChildrenClass(jsonObject);

        jsonBean.endClass();
        return  jsonBean;
    }

    /**
     * 创建子类
     * @return
     */
    public void parseFeildClass(JSONObject jsonObject) {
        Iterator<String> it = jsonObject.keys();
        while (it.hasNext()){
            String key = it.next();
            Object obj = jsonObject.get(key);
            createFiled(obj.getClass().getTypeName(),key,obj);
            System.out.println(obj.getClass().getTypeName());
        }
    }

    /**
     * 创建子类
     * @return
     */
    public void parseChildrenClass(JSONObject jsonObject) {
        Iterator<String> it = jsonObject.keys();
        while (it.hasNext()){
            String key = it.next();
            Object obj = jsonObject.get(key);
            //String type = getType(obj.getClass().getTypeName());
            if (obj !=null){
                if (obj instanceof JSONObject){//普通的对象
                    childrenClass.add(JsonBeanCreated.createdJavaBean((JSONObject) obj,true,Util.createClassName(key)));
                }
                if (obj instanceof JSONArray){//复杂的对象
                    Object value  = ((JSONArray) obj).get(0);
                    String type = getType(value.getClass().getTypeName());
                    if (!isBaseType(type)){//非基础类型
                        if (value instanceof JSONObject){//普通的对象
                            childrenClass.add(JsonBeanCreated.createdJavaBean((JSONObject) value,true,Util.createClassName(key)));
                        }else{//list 数据
                            //创建flied 暂时不处理
                        }
                    }
                }


            }
        }
    }

    /**
     * 字符串传入构造方法
     * @return
     */
    public String parseStringMethod() {
        javaBean.append("public "+className+"(String json) { ");
        addLine();
        javaBean.append(" this(new JSONObject(json)); ");
        endClass();
        addLine();
        return  null;
    }

    /**
     * json 构造方法创建解析方法
     * @return
     */
    public String parseMethod(JSONObject jsonObject) {
        javaBean.append("public "+className+"(JSONObject json) { ");
        addLine();
        Iterator<String> it = jsonObject.keys();
        while (it.hasNext()) {
            String key = it.next();
            Object obj = jsonObject.get(key);
            String type = getType(obj.getClass().getTypeName());

            if (isBaseType(type)){
                if (isStringType(type)) {
                    javaBean.append("\t" + key + " = json.opt" + type + "(\"" + key + "\");");
                }else {
                    javaBean.append("\t" + key + " = json.opt" + Util.upperCase(type) + "(\"" + key + "\");");
                }
                addLine();
            }else{
//                javaBean.append("//" + key);
                if (obj instanceof JSONObject){//普通的对象
                    javaBean.append("\t" + key + " = new "+ Util.createClassName(key) +"(json.opt" + Util.upperCase(type) + "(\"" + key + "\"));");
//                    addLine();
                }
                if (obj instanceof JSONArray) {//复杂的对象
//                    javaBean.append("// array  " + key);
//                    addLine();
                   // javaBean.append("\t"+"for( ");
                    parseArrayValue((JSONArray) obj,key);
                }
                addLine();
            }
        }
        endClass();

        return  null;
    }

    /**
     * 获取解析array的值
     * @param array
     */
    public void parseArrayValue(JSONArray array,String key){
        if (array == null && array.length() ==0){
            return;
        }
        Object obj = array.get(0);
        String  type = getType(obj.getClass().getTypeName());

        ////        if (jsonArray != null && jsonArray.length() > 0) {
        ////            int len = jsonArray.length();
        ////            for (int i = 0; i < len; i++) {
        ////                result.add(new MyCouponsBean(jsonArray.optJSONObject(i)));
        ////            }
        ////        }

        String jsonArrayName = Util.underlineToCamel(key)+"Array";
        String value =String.format("JSONArray %s =  json.optJSONArray(\"%s\") ;",jsonArrayName,key);
        javaBean.append(value);
        addLine();
        javaBean.append(String.format("if (%s != null && %s.length() > 0) {",jsonArrayName,jsonArrayName));
        addLine();
        javaBean.append(String.format(" for (int i = 0; i < %s.length(); i++) {",jsonArrayName,jsonArrayName));
        addLine();

//        value = String.format(" for (Object value : json.optJSONArray(\"%s\")) { ",key);
//        javaBean.append(value);
//        addLine();
        if (isBaseType(type)){//基础类型 不需要new
            value = String.format(key +".add((%s)%s.get(i));",type,jsonArrayName);
            javaBean.append(value);
        }else{//连续嵌套 array 先不考虑
            value = String.format(key +".add(new %s((%s)%s.get(i)));",Util.createClassName(key),"JSONObject",jsonArrayName);
            javaBean.append(value);
        }
        addLine();
        javaBean.append("}");
        addLine();
        javaBean.append("}");
        addLine();
        //javaBean.append("}");

    }


    public boolean isStringType(String type) {
        switch (type){
            case "String":
                return true;
        }
        return  false;
    }

    public boolean isBaseType(String type){
        switch (type){
            case "String":
            case "int":
            case "boolean":
            case "float":
            case "double":
            case "long":
                return true;
        }
        return  false;
    }

    public String createFiled(String type , String key,Object value){

        type =  getType(type);
        if (isBaseType(type)){
            javaBean.append("public "+ type + " " + key +";");
        }else {
            if (type.equals("JSONObject")) {//普通的对象
                javaBean.append("public " + Util.createClassName(key) + " " + key + ";");
            }
            if (type.equals("JSONArray")) {//复杂的对象
                type =getJSONArrayDataClassType((JSONArray) value);
                if ("class".equals(type)) {
                    javaBean.append("public List<" + Util.createClassName(key) + "> " + key + " = new ArrayList<>();");
                }else{
                    JSONArray array = (JSONArray) value;
                    String[] arrs = array.get(0).getClass().getTypeName().split("\\.");
                    type = arrs[arrs.length-1];
                    javaBean.append("public List<" +type + "> " + key + " = new ArrayList<>();");
                }
            }
        }
       // String value = + " " +  key +";";
        addLine();
        return  null;
    }

    /**
     * 获取json 数据类型
     * @param array
     * @return
     */
    public String getJSONArrayDataClassType(JSONArray array) {
        if (array == null && array.length() ==0){
            return null;
        }
        Object obj = array.get(0);
        String  type = getType(obj.getClass().getTypeName());
        if (isBaseType(type)){//基础类型 不需要new
            return type;
        }else{//连续嵌套 array 先不考虑 对象类型
           return "class";
        }
    }

    public String getType(String type) {
        String[] arrs = type.split("\\.");
        type = arrs[arrs.length-1];
        if ("Integer".equals(type)){
            return "int";
        }
        if ("Boolean".equals(type)){
            return "boolean";
        }
        if ("Double".equals(type)){
            return "double";
        }
        if ("Long".equals(type)){
            return "long";
        }

        return  type;
    }


    //Start
    public String createClass(String name) {
        if (isChildrenClass) {
            javaBean.append(String.format("public static class %s { ",name));
        }else {
            javaBean.append(String.format("public class %s { ",name));
        }

        addLine();
        return  null;
    }

    /**
     * add default improt
     * @param name
     * @return
     */
    public void addDeafultImprot(String name) {
        javaBean.append("package com.tp;\n");
        javaBean.append("import org.json.JSONObject;\n");
        javaBean.append(" import org.json.JSONArray;\n");
        javaBean.append("import java.util.List;\n");
        javaBean.append("import java.util.ArrayList;\n");
        addLine();
    }




    //end
    public String endClass(){
        addLine();

        //添加子类
        for (JsonBeanCreated jsonBeanCreated : childrenClass){
            addLine();
            javaBean.append(jsonBeanCreated.javaBean);
            addLine();
        }

        javaBean.append("}");
        return "}";
    }

    public void addLine(){
        javaBean.append("\n");
    }
}
