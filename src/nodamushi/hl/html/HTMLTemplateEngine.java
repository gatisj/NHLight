package nodamushi.hl.html;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.script.*;

import nodamushi.hl.FullReadUtils;
import nodamushi.hl.analysis.Token;

/**
 * テンプレートファイルを読み込み、入力を整形して出力するクラス。
 * @author nodamushi
 *
 */
public class HTMLTemplateEngine{
    
    private static String JavaScriptCode=
            "function write(str){__stringbuffer.print(str);return write;}"//write method
            + "function writeln(str){__stringbuffer.println(str);return writeln;}"// writeln method
            + "function __foreach__(func){for(var i=0;i<__contents.length;i++){func(startnumber+i,__contents[i],__names[i],__subnames[i]);}}"
            + "function tag(name,clz,id){"
            + "write('<')(name);"
            + "if(clz){write(' class=\"')(clz)('\"');}"
            + "if(id){write(' id=\"')(id)('\"');}"
            + "write('>');}"
            + "function endtag(name){"
            + "write('</')(name)('>');}";
    private static ScriptEngineManager sem = new ScriptEngineManager();
    /**
     * JavaScriptのwriteで利用する為のクラスです。<br> 
     * JavaScriptEngingの為にpublicになっていますが、他クラスは利用しないで下さい。（使うメリットもないが…）
     */
    @Deprecated
    static public final class PrintBuffer{
        StringBuilder sb = new StringBuilder();
        public void print(String str){
            sb.append(str);
        }
        public void println(String str){
            sb.append(str).append("\n");
        }
    }
    
    //ただの構造体
    static private class A{
        String string;
        boolean isScript=false;
        String code;
    }

    
    
    private List<A> alist = new ArrayList<>();
    private ScriptEngine jsengine;
    private Bindings original;

    
    /**
     * 与えられたテンプレートの内容を元にエンジンを作成します。
     * @param template テンプレートの内容
     * @throws ScriptException JavaScriptのパースエラー
     */
    public HTMLTemplateEngine(String template) throws ScriptException{
        HTMLTemplateParser p = new HTMLTemplateParser();
        List<Token> tokens=p.parse(template);
//        ScriptEngineManager sem = new ScriptEngineManager();
        jsengine = sem.getEngineByName("JavaScript");
        jsengine.eval(JavaScriptCode);
        
        
        int i=0;
        for(Token t:tokens){
            switch(t.getType()){
                case HTMLTemplateFlexParser.FOREACHSCRIPT_TOKEN:
                    String v = t.getString().trim();
                    if(!v.isEmpty()){
                        A a = new A();
                        a.isScript=true;
                        a.string = "__foreach__(__func__"+i+");";
                        String code = "var __func__"+i+"=function(linenumber,tokens,classname,subclassname){"+v+"}";
                        a.code = v;
                        i++;
                        try{
                            jsengine.eval(code);
                        }catch(ScriptException e){
                            throw new ScriptException("error message:"+e.getMessage()+".\nエラーは <?foreachline\n"+v+"\n?>の評価で起こりました");
                        }
                        alist.add(a);
                    }
                    break;
                case HTMLTemplateFlexParser.SINGLESCRIPT_TOKEN:
                    v = t.getString().trim();
                    if(!v.isEmpty()){
                        A a = new A();
                        a.isScript=true;
                        a.string = "__func__"+i+"();";
                        String code = "var __func__"+i+"=function(){"+v+"}";
                        a.code= v;
                        i++;
                        try{
                            jsengine.eval(code);
                        }catch(ScriptException e){
                            throw new ScriptException("error message:"+e.getMessage()+".\nエラーは <?script\n"+v+"\n?>の評価で起こりました。");
                        }
                        alist.add(a);
                    }
                    break;
                case HTMLTemplateFlexParser.GLOBALSCRIPT_TOKEN:
                    v = t.getString().trim();
                    if(!v.isEmpty()){
                        A a = new A();
                        a.isScript=true;
                        a.string = v+";";
                        alist.add(a);
                    }
                    break;
                case HTMLTemplateFlexParser.TEXT:
                    A a = new A();
                    alist.add(a);
                    a.string = t.getString();
                    break;
            }
        }
        original=jsengine.createBindings();
        original.putAll(jsengine.getBindings(ScriptContext.ENGINE_SCOPE));
        jsengine.getBindings(ScriptContext.ENGINE_SCOPE).clear();
    }
    
    private boolean isempty(String str){
        return str==null || str.trim().isEmpty();
    }
    
    /**
     * 引数を元にテンプレートからHTMLを生成します。
     * @param linecontents 各行のHTML。テンプレートではtokensになる
     * @param linenames 各行のクラス名。テンプレートではclassnameになる
     * @param subclassname 各行のサブクラス名。テンプレートではsubclassnameになる
     * @param startnumber 開始行番号
     * @param id テンプレートではcontainerid
     * @param classname テンプレートではcontainerclassname
     * @param langageName 言語名
     * @param evenlinename 偶数行のクラス名
     * @param oddlinename 奇数行のクラス名
     * @return 変換結果
     * @throws IllegalArgumentException linecontents,linenames,subclassnameの配列長が一致していない
     * @throws ScriptException JavaScriptを実行中に何らかの例外が起こった
     * @throws NullPointerException linecontents,linenames,subclassnameがnull
     */
    public String run(String[] linecontents,String[] linenames,String[] subclassname,
            int startnumber,String id,String classname,String langageName,
            String evenlinename,String oddlinename)
            throws IllegalArgumentException, ScriptException,NullPointerException{
        int length = linecontents.length;
        if(length!=linenames.length||length!=subclassname.length)throw new IllegalArgumentException("linecontents,linenames,subclassnameの配列長が一致していない");
        PrintBuffer pb = new PrintBuffer();
        Bindings bind = jsengine.getBindings(ScriptContext.ENGINE_SCOPE);
        bind.putAll(original);
        
        
        bind.put("__contents",linecontents);
        bind.put("__names",linenames);
        bind.put("__subnames",subclassname);
        bind.put("startnumber",startnumber);
        bind.put("containerid",id==null?"":id);
        bind.put("containerclassname",classname==null?"":classname);
        bind.put("langage",isempty(langageName)?"text":langageName);
        bind.put("linecounts",length);
        bind.put("evenlinename",isempty(evenlinename)?"evenline":evenlinename);
        bind.put("oddlinename",isempty(oddlinename)?"oddline":oddlinename);
        bind.put("__stringbuffer", pb);
        
        
        for(A a:alist){
            if(a.isScript){
                try{
                    jsengine.eval(a.string);
                }catch(ScriptException e){
                    throw new ScriptException("error message:"+e.getMessage()+".\nエラーは \n"+a.code+"\nの実行で起こりました。");
                }
            }else{
                pb.print(a.string);
            }
        }
        bind.clear();
        
        return pb.sb.toString();
    }
    
    
 
    
    
    
    
    
    //ファクトリメソッド
    
    /**
     * ファイルを読み込んでHTMLTemplateEngineを生成します
     * @param filepath ファイルパス
     * @param charset 文字コード（nullの場合は自動判別します。確実ではありません）
     * @return ファイル読み込みに失敗したり、スクリプトエラーが起こった場合はnull
     */
    public static HTMLTemplateEngine createEngine(String filepath,String charset){
        String c = FullReadUtils.read(filepath, charset);
        if(c!=null){
            try {
                return new HTMLTemplateEngine(c);
            } catch (ScriptException e) {
                System.err.println("JavaScript実行例外が発生しました。 error message"+e.getMessage());
                e.printStackTrace();
            }
        }else System.err.println(filepath+"を読み込むことが出来ませんでした。");
        return null;
    }

    /**
     * ファイルを読み込んでHTMLTemplateEngineを生成します
     * @param filepath ファイルパス
     * @param charset 文字コード（nullの場合は自動判別します。確実ではありません）
     * @return ファイル読み込みに失敗したり、スクリプトエラーが起こった場合はnull
     */
    public static HTMLTemplateEngine createEngine(File filepath,String charset){
        String c = FullReadUtils.read(filepath, charset);
        if(c!=null){
            try {
                return new HTMLTemplateEngine(c);
            } catch (ScriptException e) {
                System.err.println("JavaScript実行例外が発生しました。 error message"+e.getMessage());
            }
        }else System.err.println(filepath+"を読み込むことが出来ませんでした。");
        return null;
    }

    /**
     * ファイルを読み込んでHTMLTemplateEngineを生成します
     * @param filepath ファイルパス
     * @param charset 文字コード（nullの場合は自動判別します。確実ではありません）
     * @return ファイル読み込みに失敗したり、スクリプトエラーが起こった場合はnull
     */
    public static HTMLTemplateEngine createEngine(Path filepath,String charset){
        String c = FullReadUtils.read(filepath, charset);
        if(c!=null){
            try {
                return new HTMLTemplateEngine(c);
            } catch (ScriptException e) {
                System.err.println("JavaScript実行例外が発生しました。 error message"+e.getMessage());
            }
        }else System.err.println(filepath+"を読み込むことが出来ませんでした。");
        return null;
    }

    /**
     * InputStreamを読み込んでHTMLTemplateEngineを生成します
     * @param in テンプレートの内容のInputStream
     * @param charset 文字コード（nullの場合は自動判別します。確実ではありません）
     * @return 読み込みに失敗したり、スクリプトエラーが起こった場合はnull
     */
    public static HTMLTemplateEngine createEngine(InputStream in,String charset){
        String c = FullReadUtils.read(in, charset);
        if(c!=null){
            try {
                return new HTMLTemplateEngine(c);
            } catch (ScriptException e) {
                System.err.println("JavaScript実行例外が発生しました。 error message"+e.getMessage());
            }
        }else System.err.println(in+"を読み込むことが出来ませんでした。");
        return null;
    }
}
