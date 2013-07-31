package nodamushi.hl.analysis;

import java.util.ArrayList;
import java.util.Collection;
//各行を表す
class Line{

    private Collection<Token> tokens;
    private Collection<String> classNames;
    private int y;
    
    
    public Line(int linenumber){
        if(linenumber<0)throw new IllegalArgumentException("y<0 :"+linenumber);
        this.y = linenumber;
        tokens = new ArrayList<>();
        classNames = new ArrayList<>();
    }
    
    @Override
    public String toString(){
        StringBuilder sb=new StringBuilder();
        for(Token t:tokens)sb.append(t.toString()).append(" String:").append(t.getString()).append("\n");
        return sb.toString();
    }
    
    public int getLineNumber(){
        return y;
    }
    
    public void addToken(Token t){
        if(tokens.contains(t))return;
        tokens.add(t);
    }
    
    public void removeToken(Token t){
        tokens.remove(t);
    }
    public Collection<Token> getTokens(){
        return new ArrayList<>(tokens);
    }
    
    public void addClassName(String classname){
        if(classname == null)return;
        classname = classname.trim();
        if(classname.isEmpty() || classNames.contains(classname))return;
        classNames.add(classname);
    }
    
    public void removeClassName(String classname){
        classNames.remove(classname);
    }
    
    
    public String getClassName(){
        StringBuilder sb=new StringBuilder();
        for(String cl:classNames){
            sb.append(cl);
            sb.append(" ");
        }
        if(sb.length()!=0)
            sb.setLength(sb.length()-1);
        return sb.toString();
    }
    
    public Collection<String> getClassNames(){
        return new ArrayList<>(classNames);
    }
}
