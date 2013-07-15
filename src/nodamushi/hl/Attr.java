package nodamushi.hl;

/**
 * HTMLのDOMエレメントの属性を表す単純なクラス。
 * @author nodamushi
 *
 */
public class Attr extends Node{

    private boolean isId,isClass,specified = false;
    
    
    public Attr(String name) throws NullPointerException{
        super(name,ATTRIBUTE_NODE);
        String l = name.toLowerCase();
        isId="id".equals(l);
        isClass = "class".equals(l);
    }
    
    
    public String getName(){
        return name;
    }
    
    public String getValue(){
        return value.toString();
    }
    
    public void setValue(String value){
        super.setNodeValue(value);
        specified = true;
    }
    
    public boolean isId(){
        return isId;
    }
    
    public boolean isClass(){
        return isClass;
    }
    
    public boolean getSpecified(){
        return specified;
    }
    
    public Element getOwnerElement(){
        return (Element)parent;
    }
    
    @Override
    protected void setParent(Node parent){
        if(parent instanceof Element){
            super.setParent(parent);
        }
    }
    
}
