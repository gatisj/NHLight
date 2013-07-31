package nodamushi.hl.analysis;

import java.io.InputStream;
import java.util.*;

import nodamushi.hl.Element;
import nodamushi.hl.EscapeMap;
import nodamushi.hl.analysis.parser.Parser;
import nodamushi.hl.analysis.parser.TextParser;

/**
 * 各言語ごとのパーサーを生成し、パースした後、HTMLに変換する為の中間表現に変換するクラスです。
 * @author nodamushi
 *
 */
public class Analyzer{

    static private Map<String,String> ParserClassNameMap = new HashMap<>();
    static{
        InputStream in = Analyzer.class.getResourceAsStream("parser.txt");
        Scanner sc = new Scanner(in);
        while(sc.hasNext()){
            String str = sc.nextLine();
            if(str.startsWith("#")){
                continue;
            }
            String[] sp = str.split(":",2);
            if(sp.length!=2){
                continue;
            }
            String[] names = sp[0].split(",");
            String classname = sp[1].replace("{parser}", "nodamushi.hl.analysis.parser").trim();
            addParser(classname, names);
        }
        sc.close();
    }
    /**
     * 言語名に対するパーサーを登録します。
     * @param fqcn 言語をパースするParserの完全修飾クラス名。Parserを実装し、空のコンストラクタがある必要がある。
     * @param languageName 言語名。JavaScriptはjavascript,jsの二つに対応するなどのように、複数登録することが出来ます。
     * 大文字小文字は区別されません。
     * @return 登録できたかどうか
     */
    public static boolean addParser(String fqcn,String... languageName){
        if(fqcn==null||languageName==null)return false;
        try{
            Class<?> clz =Class.forName(fqcn);
            if(!Parser.class.isAssignableFrom(clz)){// !(clz object instanceof Parser)
                System.err.println(String.format("%sはParserを実装していません",
                        fqcn));
                return false;
            }
            @SuppressWarnings("unchecked")
            Class<Parser> clazz = (Class<Parser>)clz;
            clazz.getConstructor();//空のコンストラクタがあるかどうか調べる
            for(String name:languageName){
                if(name==null)continue;
                name = name.trim().toLowerCase();
                ParserClassNameMap.put(name, fqcn);
            }
            return true;
        } catch (ClassNotFoundException e) {
            System.err.println(String.format("%sというクラスをロードできませんでした。error message[%s]",
                    fqcn,e.getMessage()));
            return false;
        } catch (NoSuchMethodException e) {
            System.err.println(String.format("%sは引数なしのコンストラクタがないクラスです。error message[%s]",
                    fqcn,e.getMessage()));
            return false;
        } catch (SecurityException e) {
            System.err.println(String.format("%sというクラスはセキュリティーマネージャーの設定により"
                    + "引数なしのコンストラクタを利用できません。 error message[%s]",fqcn,e.getMessage()));
            return false;
        }
    }
    
    
    @SuppressWarnings("unchecked")
    private static Parser createParser(String language){
        if(language==null)return null;
        language = language.toLowerCase().trim();
        String cln = ParserClassNameMap.get(language);
        if(cln!=null) try {
            Class<?> cl = Class.forName(cln);
            if(Parser.class.isAssignableFrom(cl)){
                Parser p = ((Class<Parser>) cl).newInstance();
                return p;
            }else 
                System.err.println(cln+"はParserを実装していません。テキストとして処理します。");
        } catch (ClassNotFoundException e) {//ここは来ないはず
            System.err.println(String.format("%sというクラスをロードできませんでした。テキストとして処理します。error message[%s]"
                    ,cln,e.getMessage()));
        } catch (InstantiationException e) {
            System.err.println(String.format("%sは引数なしのコンストラクタがない、もしくは生成できなクラス"
                    + "です。テキストとして処理します。error message[%s]",cln,e.getMessage()));
        } catch (IllegalAccessException e) {//ここも来ないはず？
            System.err.println(String.format("%sは引数なしのコンストラクタがないクラスです。"
                    + "テキストとして処理します。error message[%s]",cln,e.getMessage()));
        }
        return new TextParser();
    }
    
    private Parser parser;
    private CharSequence originalSource;
    private String sourcecode;
    private String language;
    private Collection<Line> lines;
    private int tabsize;
    private boolean tabfixed,parseConfig,useConfig;
    private int startLineNumber;
    private List<Flag> flags=new ArrayList<>(0);
    private boolean parsed=false;
    private String identifierDefine=null;
    
    /**
     * tabsize=4,tabfixed=false,useUserConfig=true,startLineNumber=1でインスタンスを作成します
     * @param language
     * @param source
     */
    public Analyzer(String language,CharSequence source){
        this(language,source,4,false,true,true,1,null);
    }
    
    /**
     * 
     * @param language 言語名。nullの場合はtextとして扱います。
     * @param source ソースコード。nullの場合は空文字として扱います。 
     * @param tabsize タブの長さ（ただし、ソースコード中に<<[tabsize]>>の設定がある場合そちらが優先されます）
     * @param tabfixed タブを固定長にするかどうか（ただし、ソースコード中に<<[tabfix]>>がある場合、そちらが優先されます）
     * @param parseConfig <<[～]>>を処理するかどうか。falseの場合、<<[～]>>は処理されず、ソースコードの一部として扱われます。
     * @param useConfig <<[～]>>で設定された設定を利用するかどうか
     * @param startLineNumber 最初の行番号の設定
     * @param identifierDefine {@link Parser#defineIdentifierTokenPattern(String)}に渡す文字列。特に設定がない場合はnullでよい。
     * @see Parser#defineIdentifierTokenPattern(String)
     */
    public Analyzer(String language,CharSequence source,
            int tabsize,
            boolean tabfixed,boolean parseConfig,boolean useConfig,int startLineNumber,String identifierDefine){
        if(language==null)language="text";
        if(source == null)source="";
        if(tabsize<0)tabsize=0;
        this.language=language;
        this.tabsize = tabsize;
        this.tabfixed = tabfixed;
        this.originalSource = source;
        this.parseConfig=parseConfig;
        this.useConfig = useConfig;
        this.startLineNumber=startLineNumber;
        this.identifierDefine=identifierDefine;
    }
    
    
    
    
    /**
     * パース処理を行います。<br>
     * ソース内のユーザー背体を有効にしている場合、タブの長さ等のプロパティが書き換わる事があります。
     */
    public void parse(){
        //<<[～]>>の処理
        if(parseConfig){
            NHLightParser p =new NHLightParser(tabsize,tabfixed, originalSource.toString());
            if(useConfig){
                this.tabsize = p.tabsize<0?0:p.tabsize;
                this.tabfixed = p.tabfix;
                this.sourcecode = p.source;
                this.flags = p.flags;
            }
        }else{
            this.sourcecode=originalSource.toString();
        }
        
        
        //ソースコードのトークン分解
        parser = createParser(language);
        if(identifierDefine!=null)
            parser.defineIdentifierTokenPattern(identifierDefine);
        language = parser.language();
        lines=toLines(parser.parse(sourcecode));
        parsed = true;
    }
    
    /**
     * parseにおいて<<[～]>>を処理するかどうかの設定
     * @param b
     */
    public void enableParseSourceConfig(boolean b){
        parseConfig=b;
    }
    
    /**
     * <<[～]>>の設定を利用するかどうか。<br>
     * パースして<<[～]>>は消したいが、設定は有効にしたくない場合に利用してください。
     * @param b
     */
    public void setUseSourceConfig(boolean b){
        useConfig=b;
    }
    
    /**
     * parseにおいて<<[～]>>を処理するかどうかの設定
     */
    public boolean isEnableUserConfig(){return parseConfig;}
    
    Collection<Line> get(){
        return lines;
    }
    
    public String getLanguageName(){
        return language;
    }
    
    public CharSequence getOriginalSource(){
    	return originalSource;
    }
    
    public String getSourceCode(){
    	return sourcecode;
    }
    
    public int getTabSize(){
    	return tabsize;
    }
    
    /**
     * {@link Parser#defineIdentifierTokenPattern(String)}に渡すルールを追加します。
     * @param rule
     * @see Parser#defineIdentifierTokenPattern(String)
     */
    public void addIdentifierDefineRule(String rule){
        if(rule==null)return;
        if(identifierDefine==null){
            identifierDefine=rule;
        }else{
            identifierDefine+="\n"+rule;
        }
    }
    
    
    /**
     * {@link Parser#defineIdentifierTokenPattern(String)}に渡すルールを置き換えます。
     * @param rule
     * @return 書き換える前の古いルール
     * @see Parser#defineIdentifierTokenPattern(String)
     */
    public String setIdentifierDefineRule(String rule){
        String old =identifierDefine;
        identifierDefine=rule;
        return old;
    }
    
    public boolean isTabFixed(){
    	return tabfixed;
    }
    
    public int getStartLineNumber(){
        return startLineNumber;
    }
    
    public void setStartLineNumber(int n){startLineNumber=n;}
    public void setTabSize(int s){
        if(s<0)s=0;
        tabsize=s;
    }
    
    public void setTabSizeFixed(boolean b){
        tabfixed=b;
    }
    
    
    Collection<Flag> getFlags(){
    	return new ArrayList<>(flags);
    }
    
    /**
     * パースした結果をHTMLに変換する為の中間表現に変換します。<br>
     * parse()が呼び出されていない場合は、このメソッドから呼び出します。
     * @param tokenTypeClassNameDefine 
     * <br>番号:クラス名[;番号:クラス名]*<br>
     * という構文で書かれた文字列から各トークンの番号に対するspanのクラス名を設定します。<br>
     * 設定することがない場合はnullでも構いません
     * @param escape 半角スペースや&lt;などの文字のエスケープ文字の定義。nullの場合デフォルトの値が使われます。
     * @return lineという名前のElementのリストを返します。<br>
     * line Elementの子要素はspanなどのHTML要素の名前からなるElementです。<br>
     * 
     */
    public List<Element> convertToElements(String tokenTypeClassNameDefine,EscapeMap escape){
        if(!parsed) parse();
        
    	TokenToElement tokentohtml= new TokenToElement(lines, tabsize, 
    			tabfixed,  flags, tokenTypeClassNameDefine,escape);
    	return tokentohtml.convertToNode();
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
