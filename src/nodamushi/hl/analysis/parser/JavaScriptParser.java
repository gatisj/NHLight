package nodamushi.hl.analysis.parser;

import nodamushi.hl.analysis.parser.flex.JavaScriptFlexParser;

public class JavaScriptParser extends FlexParser{
    
    private static final String keywordDefine;
    static{
        keywordDefine = readPackageTextFile("javascriptparser.txt");
    }

    @Override
    protected AutoGeneratedParser createParser(String source){
        return new JavaScriptFlexParser(source);
    }

    @Override
    protected String defaultKeywordDefine(){
        return keywordDefine;
    }
    
    
    
}
