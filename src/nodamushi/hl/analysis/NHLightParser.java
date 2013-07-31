package nodamushi.hl.analysis;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static nodamushi.hl.analysis.NHLightFlexParser.*;

//<<[]>>の読み取り
class NHLightParser{
    private static final String SPLIT_SYMBOL="=";//=以外にしたいときはここを書き換え。
    private static final String END_TAG_STRING ="]>>";//タグの終端文字を変更したい場合はここを書き換え
    private static final String CLASS_NAME = "[a-zA-Z][a-zA-Z0-9\\-_]*";//クラス名に使える文字を変えたい場合はここを変更
    
    private static final Pattern NUMBER_PATTERN = Pattern.compile("[0-9]+");
    private static final Pattern CLASSNAME_PATTERN = Pattern.compile(
            SPLIT_SYMBOL+"[ \t\f]*("+CLASS_NAME+")[ \t\f]*"+END_TAG_STRING);
    
    private ArrayList<Token> tokens=new ArrayList<>();
    private Token before = new Token(-100, 0, 0, 0, "");//ダミー
    
    int tabsize;
    boolean tabfix;
    String source ="";
    List<Flag> flags;
    
    public NHLightParser(int defaultTabSize,boolean tabfixed,String str){
        tabsize = defaultTabSize;
        tabfix=tabfixed;
        NHLightFlexParser p = new NHLightFlexParser(str);
        try {
            Token t;
            while((t=p.parse())!=null){
                if(t.getType()==0){
                    if(before.getType()==0){
//                        boolean debag=
                                before.add(t);
                        
//                        if(!debag){
//                            System.err.println("err");
//                        }
                        
                        continue;
                    }
                }
                tokens.add(t);
                before=t;
                
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        ArrayList<Flag> alle = new ArrayList<>();
        flags=alle;
        ArrayDeque<Flag> evs = new ArrayDeque<>();
        StringBuilder sb = new StringBuilder();
        
        for(Token t:tokens){
            int type=t.getType();
            String value=t.getString();
            switch(type){
                case DEFAULT_TEXT:
                    sb.append(value);
                    break;
                    
                case TABSIZE:{
                    Matcher m = NUMBER_PATTERN.matcher(value);
                    if(m.find()){
                        String num = m.group();
                        try{
                            tabsize =Integer.parseInt(num);
                        }catch (NumberFormatException e) {
                        }
                    }
                }break;
                
                case TABFIX:
                    tabfix = true;
                    break;
                    
                case CLOSE:{
                    if(evs.isEmpty())break;
                    Flag e = evs.pop();
                    int tt=e.type==Flag.NAME_SPAN?Flag.CLOSE_NAME_SPAN:Flag.CLOSE_BLOCK_SPAN;
                    Flag ee = new Flag(sb.length(), tt, null);
                    alle.add(ee);
                }break;
                
                default:{
                    Flag e,target = null;
                    int pos = sb.length();
                    Matcher m=CLASSNAME_PATTERN.matcher(value);
                    String name=null;
                    if(m.find()){
                        name = m.group(1);
                    }
                    
                    e = new Flag(pos, type, name,target);
                    alle.add(e);
                    
                    switch(type){//if type>=NAMEでいいんだけど、拡張したときの為
                        case NAME:
                        case BLOCK:
                            evs.push(e);
                    }
                }
            }//end switch
        }//end for
        
        source = sb.toString();
    }

}
