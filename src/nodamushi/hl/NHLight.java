package nodamushi.hl;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.script.ScriptException;

import nodamushi.hl.analysis.Analyzer;
import nodamushi.hl.html.HTMLTemplateEngine;


/**
NHLightの基本となるクラス。<br>
基本的な使い方は

<code><pre>
NHLight nhlight = new NHLight("java","./Test.java");
String html=null;
if(nhlight.verify()){
&nbsp;&nbsp;//parseメソッドの呼び出しは以下のconvertToHTMLメソッドでも自動で行うので必要はないですが
&nbsp;&nbsp;// <<[tabsize]>>等の設定を無視してプログラム側からの設定を優先したい場合は
&nbsp;&nbsp;//以下の様にします。
&nbsp;&nbsp;//nhlight.parse();
&nbsp;&nbsp;//nhlight.setTabSize(4);//parse()の後に呼び出す。
   
&nbsp;&nbsp;HTMLTemplateEngine template = HTMLTemplateEngine.createEngin("./template.html");
&nbsp;&nbsp;//デフォルトのテンプレートで良い場合は
&nbsp;&nbsp;//HTMLTemplateEngine template = NHLight.getDefaultTemplate();
   
&nbsp;&nbsp;try{
&nbsp;&nbsp;&nbsp;&nbsp;//ファイルの読み込みに失敗したときはret=nullになります。
&nbsp;&nbsp;&nbsp;&nbsp;NHLight.Result ret=nhlight.convertToHTML(template);
 
&nbsp;&nbsp;&nbsp;&nbsp;if(ret!=null)
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;html = ret.html;
&nbsp;&nbsp;}catch( NullPointerException| ScriptException e){
&nbsp;&nbsp;&nbsp;&nbsp;e.printStackTrace();
&nbsp;&nbsp;}
}
</pre></code><br><br>
また、main関数を実行をする場合は<br><br>
java -cp juniversalchardet.jar:nhlight.jar nodamushi.hl.NHLight -l java -t template.html -o out.html Test.java<br><br>
の様に起動します。オプションの詳細について知りたい場合は--helpオプションを付けて起動してください。<br>
なお、実行にはjuniversalchardet(http://code.google.com/p/juniversalchardet/)が必要です。

@author nodamushi

 */
public class NHLight{

    private static HTMLTemplateEngine defaultEngine;
    
    /**
     * nodamushi.hlパッケージにあるdefault.htmlテンプレートを返します。
     * @return
     */
    public static HTMLTemplateEngine getDefaultTemplate(){
        synchronized (NHLight.class) {
            if(defaultEngine==null){
                InputStream in = NHLight.class.getResourceAsStream("default.html");
                if(in==null){
                    throw new RuntimeException("デフォルトのテンプレートがnodamushi.hiに保存されていません");
                }
                defaultEngine = HTMLTemplateEngine.createEngine(in, null);
            }
        }
        return defaultEngine;
    }
    
    
    /**
     * shell等からの起動用のメソッド
     * @param args
     * @throws NullPointerException
     * @throws ScriptException
     */
    public static void main(String[] args) throws NullPointerException, ScriptException{
        
        HTMLTemplateEngine template= null;
        EscapeMap escape = new EscapeMap();
        String 
        templatefile=null,templateencoding=null,lang=null,
        escapeString = null,tokenTypeClassNameDefine=null,id=null,
        classname=null,evenlinename=null,oddlinename=null,inputfile=null,
        inputencoding=null,outputfile=null,outputencoding="UTF-8",startlinenunber=null,
        tabsize=null,identifierrule=null;
        
        boolean 
        showHelp=args.length==0,toClipBoard=false,noparseuserconfig=false;
        
        
        for(int i=0,e=args.length-1;i<e;i++){
            String s = args[i];
            String next=(i+1<e)?args[i+1]:null;
            
            switch(s){
                case "-c":
                    classname=next;i++;break;
                case "-copy":
                    toClipBoard=true;break;
                case "-e":
                    evenlinename=next;i++;break;
                case "-encoding":
                    inputencoding = next;i++;break;
                case "-escape":
                    escapeString=next;i++;break;
                case "-h":case"--help":
                    showHelp=true;i=e;break;
                case "-i":
                    id=next;i++;break;
                case "-o":
                    oddlinename=next;i++;break;
                case "-out":
                    outputfile = next;i++;break;
                case "-outencoding":
                    outputencoding=next; i++;break;
                case "-t":
                    templatefile=next;i++;break;
                case "-tencoding":
                    templateencoding=next;i++;break;
                case "-tokenmap":
                    tokenTypeClassNameDefine=next;i++;break;
                case "-n":
                    startlinenunber=next;i++;break;
                case "-noparseconf":
                    noparseuserconfig=true;break;
                case "-tabsize":
                    tabsize=next;i++;break;
                case "-l":
                    lang=next;i++;break;
                case "-r":
                    if(identifierrule==null)identifierrule=next;
                    else identifierrule+="\n"+next;
                    i++;break;
            }
        }
        if(showHelp){
            System.out.println(help);
            return;
        }
        inputfile = args[args.length-1];
        NHLight nhlight;
        
        if(inputfile.equals("-Clipboad")){
            try {
                String data = getClipboardString();
                if(data ==null){
                    System.err.println("クリップボードから文字列を読み取れませんでした。");
                    return;
                }
                nhlight = new NHLight(lang, data);
            } catch (UnsupportedFlavorException | IOException e) {
                System.err.println("クリップボードから文字列を読み取れませんでした。 error message"+e.getMessage());
                return;
            }
        }else{
            if(lang==null)lang=getSuffix(inputfile);
            nhlight= new NHLight(lang, inputfile, inputencoding);
            if(!nhlight.verify()){
                System.err.println("ファイルの読み込みに失敗しました。");
                return;
            }
        }
        
        nhlight.enableParseSourceConfig(!noparseuserconfig);
        
        if(startlinenunber!=null)try{
            int i = Integer.parseInt(startlinenunber);
            nhlight.setStartLineNumber(i);
        }catch(Exception e){
            System.err.println("-nオプションの値が数値ではありません");
        }
        
        if(tabsize!=null)try{
            int i = Integer.parseInt(tabsize);
            nhlight.setTabSize(i);
        }catch(Exception e){
            System.err.println("-tabsizeオプションの値が数値ではありません");
        }
        
        if(identifierrule!=null)
            nhlight.addIdentifierDefineRule(identifierrule);
        
        nhlight.parse();
        
        if(templatefile==null){
            template=getDefaultTemplate();
        }else{
            template=HTMLTemplateEngine.createEngine(templatefile, templateencoding);
            if(template==null){
                System.err.println("テンプレートを作成することが出来なかった為終了します。");
                return;
            }
        }
        
        if(escapeString!=null){
            escape = new EscapeMap();
            String[] sp = escapeString.split("+");
            for(String s:sp){
                String[] ss = s.split("-",2);
                if(ss.length!=2)continue;
                String v = ss[1];
                switch(ss[0]){
                    case "space":
                        escape.space(v);break;
                    case "&":
                        escape.and(v);break;
                    case "dquote":
                        escape.doublequote(v);break;
                    case "<":
                        escape.lessthan(v);break;
                    case ">":
                        escape.greaterthan(v);break;
                }
            }
        }
        
        Result r = nhlight.convertToHTML(template, tokenTypeClassNameDefine, escape, 
                id, classname, evenlinename, oddlinename);
        String html = r.html;
        boolean print=outputfile==null&&!toClipBoard;
        if(print){
            System.out.println(html);
            return;
        }
        
        if(toClipBoard){
            setClipboadString(html);
        }
        
        if(outputfile!=null){
            if(outputencoding==null||!Charset.isSupported(outputencoding)){
                outputencoding="UTF-8";
            }
            try (OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(outputfile),outputencoding)){
                osw.write(html);
            } catch (IOException e) {
                System.err.println("ファイルに書き込みできませんでした。error message:"+e.getMessage());
            } 
        }
        
    }
    
    private static String getSuffix(String fileName) {
        if (fileName == null)
            return null;
        int point = fileName.lastIndexOf(".");
        if (point != -1) {
            return fileName.substring(point + 1);
        }
        return fileName;
    }
    private static String getClipboardString() throws UnsupportedFlavorException, IOException {
        Toolkit kit = Toolkit.getDefaultToolkit();
        Clipboard clip = kit.getSystemClipboard();

        return (String) clip.getData(DataFlavor.stringFlavor);
    }
    private static void setClipboadString(String str){
        try{
            Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
            StringSelection se = new StringSelection(str);
            cb.setContents(se, null);
        }catch(Exception e){
            System.err.println("クリップボードへの貼り付けに失敗しました。");
        }
    }
    
    private static final String help=
            "[options] InputFilePath\n\n"
                    + "  InputFilePath:シンタックスハイライトするソースコードのパス。\n\t"
                    + "-Clipboadという値にするとクリップボードから文字列を読み取る。\n\n"
                    + "----------Options-----------\n"
                    + "[options]=[-l programlanguage][-c containerclassname][-copy][-e classname][-encoding encoding][-escape name-escape[+name-escape]...][-h|--help][-i containerid]"
                    + "[-n startnumber][-noparseconf][-o oddclassname][-o outputfilepath][-outencoding encoding][-r rule]...[-t templatefile][-tabsize number][-tencoding encoding][-tokenmap number:name[;number:name]...]\n\n"
                    + "  -l          :言語を設定します。設定されていない場合、拡張子からの判断を試みますが、基本的には必ず設定してください。\n\n"
                    + "  -c          :テンプレートのcontainerclassnameを設定します\n"
                    + "  -copy       :クリップボードに結果をコピーします。\n"
                    + "  -e          :偶数行目のクラス名を設定します\n"
                    + "  -encoding   :読み込みファイルの文字エンコードを指定します\n"
                    + "  -escape     :エスケープ文字の設定を指定します。\n"
                    + "\t入力は　name-escape[+name-escape]...\n"
                    + "\tnameは半角スペースを表すspace,ダブルクオテーションを表すdquote,<,>,&。\n"
                    + "\tescapeは変換後の文字を表す。（+や-は使えない）\n"
                    + "\t例：-escape \"space- +&-&#38;+<-&lt;+>-&gt;+quote-&quote\"\n"
                    + "  -h,--help   :このヘルプを表示します\n"
                    + "  -i          :テンプレートのcontaineridを設定します\n"
                    + "  -n          :開始行番号を設定します。ソースコード中の設定の方が優先されます。\n"
                    + "  -noparseconf:ソースコード中に記述した設定を無視し、ソースコードの一部として扱います。\n"
                    + "  -o          :奇数行目のクラス名を設定します\n"
                    + "  -out        :結果をファイルに出力します。ファイルのパスを指定します。\n"
                    + "  -outencoding:出力ファイルの文字エンコードを指定します。デフォルトはUTF-8です。\n"
                    + "  -t          :テンプレートファイルを指定します。\n"
                    + "  -r          :Identifierトークンの置換ルール。-rオプションは複数書くことが出来ます。\n"
                    + "  -tabsize    :タブ文字の長さを設定します。ソースコード中の設定の方が優先されます。\n"
                    + "  -tencoding  :テンプレートファイルの文字エンコードを指定します\n"
                    + "  -tokenmap   :トークン番号とspanのクラス名の対応を設定します。詳しくはREADMEを参照してください。\n"
                    + "\t例:-tokenmap \"0:text;1:block;2:block;3:string;\"\n\n\n"
                    + "-----------注意--------------\n"
                    + "文字コードの判定にjuniversalchardet（http://code.google.com/p/juniversalchardet/）を利用しています。"
                    + "実行、コンパイルするにはjuniversalchardet.jarへのパスを通す必要があります。";
    
    
    /**
     * 処理した結果を返す為のコンテナ
     */
    public static final class Result{
        /**
         * 生成されたHTML文字列です
         */
        public String html;
        /**
         * Lamuriyan連携用の変数名と行番号の組み合わせです。
         */
        public Collection<Pair<String,Integer>> linenumbers=new ArrayList<>();
    }
    
    /**
     * filepathのファイルを読み込み、初期化します。
     * @param language ソースの言語 nullの場合はtextと見なします。
     * @param filepath ソースファイルのパス
     * @param charset 文字のエンコード（nullの場合は自動判別処理をします。確実ではありません）
     */
    public NHLight(String language,String filepath,String charset){
        this.language=language;
        readdata = FullReadUtils.read(filepath, charset);
        init();
    }
    
    /**
     * sourceDataをソースコードとして初期化します。
     * @param language ソースの言語 nullの場合はtextと見なします。
     * @param sourceData ソースコードの内容
     */
    public NHLight(String language,String sourceData){
        this.language = language;
        //BOM削除
        if(sourceData.charAt(0)==65279)sourceData=sourceData.substring(1);
        readdata = sourceData;
        init();
    }
    
    /**
     * urlのファイルを読み込み、初期化します。
     * @param language ソースの言語 nullの場合はtextと見なします。
     * @param url ソースファイルのURL
     * @param charset 文字のエンコード（nullの場合は自動判別処理をします。確実ではありません）
     */
    public NHLight(String language,URL url,String charset){
        this.language=language;
        readdata = FullReadUtils.read(url, charset);
        init();
    }
    
    /**
     * URLConnectionを読み込み、初期化します。
     * @param language ソースの言語 nullの場合はtextと見なします。
     * @param urlconnection ソースファイルへのURLConnection
     * @param charset 文字のエンコード（nullの場合は自動判別処理をします。確実ではありません）
     */
    public NHLight(String language,URLConnection urlconnection,String charset){
        this.language=language;
        readdata = FullReadUtils.read(urlconnection, charset);
        init();
    }
    /**
     * filepathのファイルを読み込み、初期化します。
     * @param language ソースの言語 nullの場合はtextと見なします。
     * @param filepath ソースファイルのパス
     * @param charset 文字のエンコード（nullの場合は自動判別処理をします。確実ではありません）
     */
    public NHLight(String language,File filepath,String charset){
        this.language=language;
        readdata = FullReadUtils.read(filepath, charset);
        init();
    }
    /**
     * filepathのファイルを読み込み、初期化します。
     * @param language ソースの言語 nullの場合はtextと見なします。
     * @param filepath ソースファイルのパス
     * @param charset 文字のエンコード（nullの場合は自動判別処理をします。確実ではありません）
     */
    public NHLight(String language,Path filepath,String charset){
        this.language=language;
        readdata = FullReadUtils.read(filepath, charset);
        init();
    }
    
    private String language;
    private String readdata;
    private Analyzer analyser;
    
    /**
     * このオブジェクトが正常に動作するかどうか検証する
     * @return ファイルの読み込みに失敗している場合はfalseが返ります
     */
    public boolean verify(){
        return readdata!=null;
    }
    
    
    private void init(){
        if(readdata!=null)
            analyser = new Analyzer(language, readdata);
    }
    
    /**
     * {@link Parser#defineIdentifierTokenPattern(String)}に渡すルールを追加します。
     * @param rule
     * @see Parser#defineIdentifierTokenPattern(String)
     */
    public void addIdentifierDefineRule(String rule){
        if(analyser!=null)
            analyser.addIdentifierDefineRule(rule);
    }
    
    
    /**
     * {@link Parser#defineIdentifierTokenPattern(String)}に渡すルールを置き換えます。
     * @param rule
     * @return 書き換える前の古いルール
     * @see Parser#defineIdentifierTokenPattern(String)
     */
    public String setIdentifierDefineRule(String rule){
        if(analyser==null)return null;
        return analyser.setIdentifierDefineRule(rule);
    }
    
    /**
     * ソースコードをパースします。
     */
    public void parse(){
        if(analyser!=null)analyser.parse();
    }
    
    public void setStartLineNumber(int i){
        if(analyser!=null)analyser.setStartLineNumber(i);
    }
    
    /**
     * <<[tabsize=4]>>などの<<[～]>>を設定としてパースするかどうかを変更します。<br>
     * falseの場合は<<[～]>>もソースコードの一部と見なされます。
     * @param b
     */
    public void enableParseSourceConfig(boolean b){
        if(analyser!=null)analyser.enableParseSourceConfig(b);
    }
    
    /**
     * <<[～]>>の設定を利用するかどうか。<br>
     * パースして<<[～]>>は消したいが、設定は有効にしたくない場合に利用してください。
     * @param b
     */
    public void setUseSourceConfig(boolean b){
        if(analyser!=null)analyser.setUseSourceConfig(b);;
    }
    
    /**
     * タブ文字の長さを設定します
     * @param i
     */
    public void setTabSize(int i){
        if(analyser!=null)analyser.setTabSize(i);
    }
    
    /**
     * タブ文字の長さを固定長にするかどうか
     * @param b
     */
    public void setTabSizeFixed(boolean b){
        if(analyser!=null)analyser.setTabSizeFixed(b);
    }
    
    
    /**
     * {@link NHLight#convertToHTML(HTMLTemplateEngine, String, EscapeMap, String, String, String, String)}をtemplateだけ指定し、
     * 他はデフォルトの値を用いてHTMLに変換します。
     * @see NHLight#convertToHTML(HTMLTemplateEngine, String, EscapeMap, String, String, String, String)
     * @param template HTMLのひな形。nullの場合NullPointerExceptionが発生します
     * @return HTMLの結果と、Lamuriyan連携用の行番号と変数名のペアのコレクションを入れたResultオブジェクト。
     * ただし、コンストラクタでソースコードが読み込めなかった時はnullが返ります。
     * @throws NullPointerException templateがnullの場合
     * @throws ScriptException JavaScript実行時に例外が発生した場合
     */
    public Result convertToHTML(HTMLTemplateEngine template) throws NullPointerException, ScriptException{
        return convertToHTML(template, null, null, null, null, null, null);
    }
    
    /**
     * パース結果をHTML文字列に変換します。parse()を呼び出していない場合は、先にparse()を行います。
     * @param template HTMLのひな形。nullの場合NullPointerExceptionが発生します
     * @param tokenTypeClassNameDefine <br>
     * 番号:クラス名[;番号:クラス名]*<br>
     * という構文で書かれた文字列から各トークンの番号に対するspanのクラス名を設定します。<br>
     * デフォルトの設定で良い場合はnullでも構いません<br>
     * 詳しくはREADEME.txtを参照してください。<br><br>
     * @param escape 半角スペース、&等のエスケープの設定です。デフォルトで良い場合はnullにしてください。
     * @param id 最も外側にくるHTML要素のidです。設定がない場合はnullでもよいです。<br>
     * テンプレート側ではcontaineridとして利用されます<br><br>
     * @param classname 最も外側にくるHTML要素のclass名です。設定がない場合はnullでもよいです。<br>
     * テンプレート側ではcontainerclassnameとして利用されます。<br><br>
     * @param evenlinename 偶数行に指定するクラス名です。nullの場合はevenlineと扱われます。
     * @param oddlinename 奇数行に指定するクラス名です。nullの場合はoddlineと扱われます。
     * @return HTMLの結果と、Lamuriyan連携用に行番号と変数名のペアのコレクションを入れたResultオブジェクト。
     * ただし、コンストラクタでソースコードが読み込めなかった時はnullが返ります。
     * @throws NullPointerException templateがnullの場合
     * @throws ScriptException JavaScript実行時に例外が発生した場合
     */
    public Result convertToHTML(HTMLTemplateEngine template,String tokenTypeClassNameDefine,EscapeMap escape,
            String id,String classname,String evenlinename,String oddlinename) throws NullPointerException,ScriptException{
        if(analyser==null)return null;
        if(template==null)throw new NullPointerException("engine is null.");
        List<Element> elements = analyser.convertToElements(tokenTypeClassNameDefine, escape);
        
        int startlinenumber = analyser.getStartLineNumber();
        String language=analyser.getLanguageName();
        String[] contents = new String[elements.size()],
                clname=new String[elements.size()],subname=new String[elements.size()];
        
        Result r=new Result();
        int i=0;
        for(Element el:elements){
           contents[i]=el.innerHTML();
           String s =  el.getAttribute("class");
           clname[i] =s==null?"":s;
           s=el.getAttribute("sub-class");
           subname[i] = s==null?"":s;
           
           String n = el.getAttribute("linenumberflag");
           if(!n.isEmpty()){
               r.linenumbers.add(new Pair<String, Integer>(n, startlinenumber+i));
           }
           i++;
        }
        
        r.html=template.run(contents, clname, subname, startlinenumber, id, classname,language, evenlinename, oddlinename);
        
        
        return r;
    }
    
    
}

