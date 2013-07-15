package nodamushi.hl.analysis;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import nodamushi.hl.analysis.parser.Parser;

public class TokenAnalyzer{

    
    public static void main(String[] args) throws IOException{
        Path p = Paths.get("testcode\\test.html");
        Scanner s = new Scanner(p);
        StringBuilder sb = new StringBuilder();
        while(s.hasNext()){
            sb.append(s.nextLine()).append("\n");
        }
        TokenAnalyzer ta =new TokenAnalyzer("html", sb.toString());
        TokenToHTML tth = ta.createTokentToHTMLObject("");
        System.out.println(tth.convertTypeOl().toHTML());
//        Node n = tth.convertToNode();
//        System.out.println(n.toHTML());
    }
    static private Map<String,String> ParserClassNameMap = new HashMap<>();
    static{
        InputStream in = TokenAnalyzer.class.getResourceAsStream("parser.txt");
        Scanner sc = new Scanner(in);
        while(sc.hasNext()){
            String str = sc.nextLine();
            String[] sp = str.split(":");
            String classname = sp[1].replace("{parser}", "nhlight.analysis.parser").trim();
            
            try{
                Class<?> clz =Class.forName(classname);
                if(!Parser.class.isAssignableFrom(clz)){// !(clz object instanceof Parser)
                    continue;
                }
                @SuppressWarnings("unchecked")
                Class<Parser> clazz = (Class<Parser>)clz;
                clazz.getConstructor();//空のコンストラクタがあるかどうか調べる
            }catch(Exception e){
                System.err.println("error:"+str);
                e.printStackTrace();
                continue;
            }
            
            String[] names = sp[0].split(",");
            for(String name:names){
                name = name.trim().toLowerCase();
                ParserClassNameMap.put(name, classname);
            }
            
        }
    }
    
    
    private static Parser createParser(String language){
        if(language==null)return null;
        language = language.toLowerCase().trim();
        String cln = ParserClassNameMap.get(language);
        if(cln!=null){
            try{
                @SuppressWarnings("unchecked")
                Class<Parser> clz = (Class<Parser>) Class.forName(cln);
                Parser p = clz.newInstance();
                return p;
            }catch(Exception e){
                return null;//TODO Text用のを返す様にする。
            }
        }
//        //TODO
//        switch(language){
//            case "java":
//                return new JavaParser();
//            case "javascript":
//            case "js":
//                return new JavaScriptParser();
//                
//        }
        
        return null;
    }
    
    private Parser parser;
    private String originalSource;
    private String sourcecode;
    private Collection<Line> lines;
    private int tabsize;
    private boolean tabfixed;
    private int startLineNumber=1;
    private List<Event> events=new ArrayList<>(0);
    
    
    
    public TokenAnalyzer(String language,String source){
        this(language,source,4,false,true);
    }
    
    public TokenAnalyzer(String language,String source,
            int tabsize,
            boolean tabfixed,boolean useUserConifg){
        if(language==null)throw new NullPointerException("language is null!");
        if(source == null)throw new NullPointerException("source is null!");
        
        this.tabsize = tabsize;
        this.tabfixed = tabfixed;
        this.originalSource = source;
       
        
        //コンストラクタはここまで。
        //<<[～]>>の処理
        if(useUserConifg){
            NHLightParser p =new NHLightParser(tabsize, source);
            this.tabsize = p.tabsize;
            this.tabfixed = p.tabfix;
            this.sourcecode = p.source;
            this.events = p.events;
        }else{
            this.sourcecode=source;
        }
        
        
        //ソースコードのトークン分解
        parser = createParser(language);
        lines=toLines(parser.parse(sourcecode));
        
        
    }
   
    public Collection<Line> get(){
        return lines;
    }
    
    
    
    public String getOriginalSource(){
    	return originalSource;
    }
    
    public String getSourceCode(){
    	return sourcecode;
    }
    
    public int getTabSize(){
    	return tabsize;
    }
    
    public boolean iSTabFixed(){
    	return tabfixed;
    }
    
    public Collection<Event> getEvents(){
    	return new ArrayList<>(events);
    }
    
    
    public TokenToHTML createTokentToHTMLObject(String config){
    	TokenToHTML tokentohtml= new TokenToHTML(lines, tabsize, 
    			tabfixed, startLineNumber, events, config);
    	return tokentohtml;
    }


    private List<Line> toLines(List<Token> tokens){
        ArrayList<Line> lines = new ArrayList<>();
        int l=0;
        Line line = new Line(l);
        lines.add(line);
        for(Token t:tokens){
            if(t.getType() == TokenTypePreDefine.NEWLINE_TOKEN){
                l++;
                line = new Line(l);
                lines.add(line);
                continue;
            }
            line.addToken(t);
        }
        return lines;
    }
    
    
    
    
}
