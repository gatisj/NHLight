package nodamushi.hl.analysis.parser;

import nodamushi.hl.analysis.Token;
import nodamushi.hl.analysis.parser.flex.JavaFlexParser;

public class JavaParser extends FlexParser{

    private final static String KeywordDefine;
    static{
        KeywordDefine = FlexParser.readPackageTextFile("javaparser.txt");
    }
    
    
    @Override
    protected String defaultKeywordDefine(){
        return KeywordDefine;
    }
    
    @Override
    protected AutoGeneratedParser createParser(String source){
        return new JavaFlexParser(source);
    }

    @Override
    protected void appendParsedToken(Token t){
        // JavaParser.class の様なclassは単なるキーワードとして扱う
        if(t.getType()==DEFINE_TOKEN && "class".equals(t.getString())){
            Token before = getLastAddToken();
            if(before!=null && before.getType()==ACCESS_TOKEN){
                t.setType(KEYWORD_MIDIUM_TOKEN);
            }
        }else if(t.getType()==OPERATOR_TOKEN ){
            //import java.lang.*;の*はワイルドカードであって演算子ではない。
            if("*".equals(t.getString())){
                Token before = getLastAddToken();
                if(before !=null && before.getType()==ACCESS_TOKEN){
                    t.setType(PLAIN_TOKEN);
                }
            }
        }
        addToken(t);
    }

}
