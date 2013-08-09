package nodamushi.hl;

import java.util.HashMap;


/**
 * HTMLのDOMエレメントを表す簡単なクラス。<br>
 * 
 * @author nodamushi
 *
 */
public class Element extends Node{
    
    public Element(String name){
        super(name,ELEMENT_NODE);
        attrs = new HashMap<>();
    }
    
    public String getAttribute(String name){
        Attr a = getAttributeNode(name);
        return a!=null?a.getValue():"";
    }
    
    public Attr getAttributeNode(String name){
        return attrs.get(name);
    }
    
    
    public String getTagName(){
        return name;
    }
    
    public boolean hasAttribute(String name){
        return attrs.containsKey(name);
    }
    
    public void removeAttribute(String name){
        attrs.remove(name);
    }
    
    public Attr removeAttributeNode(Attr oldAttr){
        if(attrs.containsValue(oldAttr)){
            attrs.remove(oldAttr.getName());
            return oldAttr;
        }
        return null;
    }
    
    public void setAttribute(String name,String value){
        if(name==null)return;
        Attr a = new Attr(name);
        if(value!=null)
            a.setValue(value);
        setAttributeNode(a);
    }
    
    public Attr setAttributeNode(Attr newAttr){
        if(newAttr==null)return null;
        Attr old=attrs.get(newAttr.getName());
        attrs.put(newAttr.getName(), newAttr);
        return old;
    }
    
    public void setClassName(String name){
        if(name==null)return;
        setAttribute("class", name);
    }
    
    public void setID(String id){
        if(id==null)return;
        setAttribute("id", id);
    }
    
    public void addClassName(String name){
        if(name==null||name.isEmpty())return;
        Attr a = getAttributeNode("class");
        if(a==null){
            setClassName(name);
        }else{
            String value = a.getValue();
            if(value ==null || value.isEmpty())value = name;
            else value +=","+name;
            a.setValue(value);
        }
    }
    
    public void appendText(String str){
        if(str==null)return;
        Node text = Node.createTextNode(str);
        appendChild(text);
    }
    
    
}
