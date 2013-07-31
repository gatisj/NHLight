package nodamushi.hl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * HTMLのDOMノードを表す単純なクラス。<br>
 * とりあえず必要そうなのをてきとーに実装しただけなので、正確さ等は求めていません。
 * @author nodamushi
 *
 */
public class Node{

    public static Node createTextNode(String text){
        Node n = new Node("textnode",TEXT_NODE);
        n.setNodeValue(text);
        return n;
    }
    
    public static Element createElement(String tagname){
        Element e = new Element(tagname);
        return e;
    }
    
    public static final short ELEMENT_NODE=1;
    public static final short ATTRIBUTE_NODE=2;
    public static final short TEXT_NODE=3;
    
    protected Node parent;
    protected String name;
    protected short nodetype;
    protected CharSequence value="";
    protected List<Node> children=new ArrayList<>();
    protected Map<String, Attr> attrs;
    
    
    protected void setParent(Node parent){
        this.parent = parent;
    }
    
    public Node(String name,short nodetype) throws NullPointerException{
        this.name = name;
        this.nodetype = nodetype;
    }
    
    public void setNodeValue(CharSequence value){
        if(value==null)value = "";
        this.value = value;
    }
    
    public String getNodeValue(){
        return value.toString();
    }
    
    public String getNodeName(){
        return name;
    }
    
    public boolean hasAttributes(){
        return attrs!=null&&!attrs.isEmpty();
    }
    
    public boolean hasChildNodes(){
        return !children.isEmpty();
    }
    
    
    
    public Map<String,Attr> getAttributes(){
        return attrs;
    }
    
    public List<Node> getChildNodes(){
        return children;
    }
    
    protected void setParentNode(Node n){
        parent = n;
    }
    
    public Node getParentNode(){
        return parent;
    }
    
    public Node getFirstChild(){
        return hasChildNodes()? children.get(0):null;
    }
    
    public Node getLastChild(){
        return hasChildNodes()? children.get(children.size()-1):null;
    }
    
    private void add_postprocess(Node n){
        n.setParent(this);
    }
    
    public Node appendChild(Node newChild) throws NullPointerException{
        if(children.contains(newChild))children.remove(newChild);
        children.add(newChild);
        add_postprocess(newChild);
        return newChild;
    }
    
    public void removeChild(Node oldChild) {
    	if(children.contains(oldChild))
    		children.remove(oldChild);
    	
    }
    
    public Node getPreviousSibling(){
        return parent!=null?parent.getPreviousNode(this):null;
    }
    
    public Node getNextSibling(){
        return parent!=null?parent.getNextNode(this):null;
    }
    
    Node getNextNode(Node n){
        if(children.contains(n)){
            int index = children.indexOf(n)+1;
            return children.size()==index?null:children.get(index);
        }
        return null;
    }
    
    Node getPreviousNode(Node n){
        if(children.contains(n)){
            int index = children.indexOf(n)-1;
            return -1==index?null:children.get(index);
        }
        return null;
    }
    
    public Node insertBefore(Node newChild,Node refChild){
        if(children.contains(refChild)){
            int index = children.indexOf(refChild)+1;
            children.add(index, newChild);
            add_postprocess(newChild);
            return newChild;
        }
        return null;
    }
    
    public Node replaceChild(Node newChild,Node oldChild){
        if(children.contains(oldChild)){
            int index = children.indexOf(oldChild);
            children.remove(index);
            children.add(index, newChild);
            add_postprocess(newChild);
            return newChild;
        }
        return null;
    }
    
    public String getTextContent(){
        switch(nodetype){
            case TEXT_NODE:
                return value.toString();
            case ELEMENT_NODE:
            case ATTRIBUTE_NODE:
                StringBuilder sb = new StringBuilder();
                for(Node n:children){
                    sb.append(n.getTextContent());
                }
                return sb.toString();
        }
        return null;
    }
    
    
    
    public StringBuilder toHTML(StringBuilder sb){
    	if(nodetype==TEXT_NODE){
    		sb.append(value);
    		return sb;
    	}
    	sb.append("<").append(name);
    	if(hasAttributes()){
    		for(String name:attrs.keySet()){
    			Attr a = attrs.get(name);
    			String value = a.getValue();
    			sb.append(" ").append(name).append("=\"")
    			.append(value).append("\"");
    		}
    	}
    	sb.append(">");
    	
    	innerHTML(sb);
    	
    	sb.append("</").append(name).append(">");
    	return sb;
    }
    
    
    public StringBuilder innerHTML(StringBuilder sb){
        for(Node n:children){
            n.toHTML(sb);
        }
        return sb;
    }
    
    public String innerHTML(){
        return innerHTML(new StringBuilder()).toString();
    }
    
    public String toHTML(){
    	return toHTML(new StringBuilder()).toString();
    }

    public void setNodeName(String name){
        this.name = name;
    }
    
}
