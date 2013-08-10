package nodamushi.hl.analysis.parser;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import nodamushi.hl.Pair;
import nodamushi.hl.analysis.Token;
import nodamushi.hl.analysis.TokenTypePreDefine;

/**
 * JFlexで自動生成したパーサーを利用するパーサーの基本となるクラス。<br>
 * 実際に構文をパースするのは、JFlexが生成するAutoGeneratedParserを実装したクラスが受け持ち、このパーサーは
 * AutoGeneratedParserにデータを渡し、出力結果を整形する処理を行う。<br><br>
 * このクラスを継承するときはcreateParserでJFlexが生成したクラスのオブジェクト作成を行うメソッドを実装する。<br><br><br>
 */
public abstract class FlexParser implements TokenTypePreDefine,Parser{
    
    private Token lastAdd;
    private ArrayList<Token> tokens = new ArrayList<>(0);
    private List<Pair<Pattern,Integer>> keywordList = new ArrayList<>();
    private boolean ignoreNewLine_LastAddToken=true;
    protected boolean ignore_newline=false;
    
    /**Identifierトークンが変更されるデフォルトのトークン番号を定義します。<br>
     * 初期値はPLAIN_TOKENに設定されています。<br>
     * IdentifierがPLAIN_TOKEN以外の値にデフォルトで設定したい場合はこの値を変更することで可能になります。
     * */
    protected int defaultIdentifierTokenType=TokenTypePreDefine.PLAIN_TOKEN;
    
    protected FlexParser(){
        defineIdentifierTokenPattern(defaultKeywordDefine());
    }
    
    /**
     * getLastAddTokenで改行トークンを無視するかどうかの設定。
     * @param b
     */
    protected void setIgnoreNewLineLastToken(boolean b){
        ignoreNewLine_LastAddToken = b;
    }
    /**
     * getLastAddTokenで改行トークンを無視するかどうかの設定。
     */
    protected boolean isIgnoreNewLineLastToken(){
        return ignoreNewLine_LastAddToken;
    }
    /**
     * 最後に追加したトークンを返します。
     * @return 最後に追加したトークン
     */
    protected Token getLastAddToken(){return lastAdd;}
    
    /**
     * 渡されたトークンをその並びのまま保持します。
     * @param ts
     */
    protected void addTokens(Collection<Token> ts){
        for(Token t:ts)addToken(t);
    }
    
    /**
     * 現在保持しているトークン列のクローンを返します。<br>
     * コレクションオブジェクトはクローンですが、
     * 各トークンはクローンではないので、内容を書き換えると反映されます。
     * @return
     */
    protected Collection<Token> getAllTokens(){
        return new ArrayList<>(tokens);
    }
    
    @Override
    public void defineIdentifierTokenPattern(String s){
        if(s==null)return;
        @SuppressWarnings("resource")
        Scanner sc = new Scanner(s);
        while(sc.hasNext()){
            String str = sc.nextLine();
            Matcher m = DEFINEIDENTIFIER_PATTERN.matcher(str);
            if(m.find()){
                String num = m.group(1);
                int n = Integer.parseInt(num);
                String pattern = m.group(2).trim();
                if(pattern.isEmpty())continue;
                try{
                    Pattern pat = Pattern.compile("^("+pattern+")$");
                    Pair<Pattern,Integer> p = new Pair<Pattern, Integer>(pat, n);
                    keywordList.add(p);
                }catch(PatternSyntaxException e){//Pattern.compile error
                    e.printStackTrace();
                }
            }
        }//end while
    }
    
    
    private int identifierTokenNumber(Token t){
        String value = t.getString();
        for(int i=keywordList.size()-1;i>=0;i--){
            Pair<Pattern, Integer> p = keywordList.get(i);
            Matcher m = p.getA().matcher(value);
            if(m.find()){
                return p.getB();
            }
        }
        return defaultIdentifierTokenType;
    }
    
    /**
     * 渡されたトークンを保持します。<br>
     * 負値のトークン番号が連続する際にはトークンが結合されます。
     * @return
     */
    protected void addToken(Token t){
        if(t==null){
            return;
        }
        
        if(t.getType() <=0) JOIN:{//結合するかどうかの判定と結合処理
            if(lastAdd==null)break JOIN;
            if(lastAdd.getType() != t.getType())break JOIN;
            if(!ignore_newline&&lastAdd.getLineNumber() != t.getLineNumber())break JOIN;
            if(lastAdd.add(t))return;
        }
        tokens.add(t);
        if(ignoreNewLine_LastAddToken&&t.getType()!=NEWLINE_TOKEN)//改行は無視
            lastAdd=t;
    }
    
    @Override
    /**
     * 渡された文字列をパースします。<br>
     * 
     * なお、基本的にtemplate.jflexを元にしたJFlexで作ったパーサーを利用する事を前提として作られているので、
     * このメソッドで定義されている処理の流れとは異なるパース処理をしたい場合は、
     * オーバーライドしてください。
     * @param source
     * @return
     */
    public List<Token> parse(String source){
        tokens = new ArrayList<>();
        AutoGeneratedParser parser = createParser(source);
        while(true){
            Collection<Token> ts =parser.parse();
            if(ts.isEmpty())break;//空なら終了
            for(Token t:ts){
                appendParsedToken(t);
            }
        }
        
        parseEndHook(tokens);
        
        //indentifierを変更する
        for(Token t:tokens){
            if(t.getType()==TokenTypePreDefine.IDENTIFIER){
                int n = identifierTokenNumber(t);
                t.setType(n);
            }
        }
        
        postProcess(tokens);
        return tokens;
    }
    /**実際に文字列をトークン分割するFlexなどで作ったパーサーを生成します*/
    abstract protected AutoGeneratedParser createParser(String source);
    
    /**
     * FlexParserが出力した結果に対し、必要な処理を行います。<br>
     * デフォルト実装では渡されたTokenをaddToken(Token)メソッドで追加するだけですが、
     * 特殊な理由でTokenの内容を書き換えたり、あるトークンは追加せず無視するなどする場合は、それに相当する処理を記述してください。<br>
     * ここに渡されたトークンは同じ負値の番号のトークンが連続する可能性があります。トークンの結合はaddToken(Token)メソッドで行われます。
     * また、この時点でIDENTIFIERのトークン番号は変更されていません。
     */
    protected void appendParsedToken(Token t){addToken(t);}
    
    /**
     * IDENTIFIERトークンの中でキーワードとなる文字列のパターンを定義した文字列を返します。<br>
     * このメソッドはコンストラクタから呼ばれ、最初のパターンの設定が行われます。このメソッドの返値を変更することで、
     * デフォルトの値を設定できます。<br><br>
     * @see FlexParser#defineIdentifierTokenPattern(String)
     * @return
     */
    protected String defaultKeywordDefine(){return null;}

    /**
     * nhlight.analysis.parserパッケージ内のUTF-8テキストファイルを全部読み込んで返します。
     * @param filename ファイル名
     * @return ファイルの内容。ファイルが存在しない場合はnull
     */
    protected static String readPackageTextFile(String filename){
        InputStream in = FlexParser.class.getResourceAsStream(filename);
        if(in!=null){
            try(Reader reader=new BufferedReader(new InputStreamReader(in,"UTF-8"))){
                char[] buf = new char[1024];
                int read=0;
                StringBuilder sb = new StringBuilder();
                while((read=reader.read(buf))>=0){
                    sb.append(buf,0,read);
                }
                return sb.toString();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
    
    /**
     * パースが終わり、IDENTIFIERトークンの番号が変更された後のトークン列に何らかの処理を加えたい場合はこのメソッドをオーバーライドしてください。<br>
     * 渡されたコレクションの内容を書き換えると、このパーサーが保持する内容が書き換えられます。
     */
    protected void postProcess(List<Token> tokens){}
    
    /**
     * パースが終わった後のトークン列に何らかの処理を加えたい場合はこのメソッドをオーバーライドしてください。<br>
     * 渡されたコレクションの内容を書き換えると、このパーサーが保持する内容が書き換えられます。<br>
     * この段階ではTokenのタイプがIDENTIFIERのトークンの番号は書き換えられていません。
     * @param tokens
     */
    protected void parseEndHook(List<Token> tokens){}
}
