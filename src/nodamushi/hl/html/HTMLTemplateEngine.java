package nodamushi.hl.html;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;

import javax.script.*;

import nodamushi.hl.Element;
import nodamushi.hl.FullReadUtils;

public class HTMLTemplateEngine{

    public static final String LabelAttrName="Lamuriyan-Label";
    //実行時に必要な変数
    //startNumber:行の開始番号
    //oddLineName:偶数行のクラス名
    //evenLineName:奇数行のクラス名
    //lines: 各行のDocument-FragmentのDOMが格納された配列
    //linage: lines.length
    //containerid: 一番外側のDOMのid
    //containerclassname: 一番外側のDOMのclass名
    //langage:言語名
    private static final String JavaScriptCode=
            "importClass(Packages.nodamushi.hl.Element);"
            + "var labelAttrName='"+LabelAttrName+"';"
            + ""
            + "function createElement(name){"
            +   "name=name+'';"
            +   "if(name){return new Element(name);}"
            +   "else {return null;}"
            + "}"
            + "function getOddOrEvenLineName(number){return (number%2==1)?oddLineName:evenLineName;}"
          //funcの引数(loop,lineNumber,lineDOM,lineClassName,lineSubClassName,lamuriyanLabel)
          //戻り値でloopの次へのステップサイズを変更できる。
          //ただし、0を返したら無視される。何も返さない場合は通常の1幅として扱う。
            + "function foreach(func){"
            +   "for(var i=0,linage=lines.length;i<linage;i++){"
            +     "var e = lines[i];"
            +     "var cn = e.getAttribute('class')+'';"
            +     "var sub= e.getAttribute('sub-class')+'';"
            +     "var label =e.getAttribute('linenumberflag')+'';"
            +     "var skip=func(i,i+startNumber,e,cn,sub,label);"
            +     "if(!isNaN(skip)&& skip!=0 ){i+=skip-1;}"
            +   "}"
            + "}"
            
            ;
    private static final String RUNCODE="__$main$__();";
    private static ScriptEngineManager sem = new ScriptEngineManager();
    
    private ScriptEngine jsengine;
    private Bindings original;
    private CompiledScript compiled;
    
    public HTMLTemplateEngine(String template) throws ScriptException{
        jsengine = sem.getEngineByName("JavaScript");
        jsengine.eval(JavaScriptCode);
        jsengine.eval("var __$main$__=function(){"+template+";};");
        original=jsengine.createBindings();
        original.putAll(jsengine.getBindings(ScriptContext.ENGINE_SCOPE));
        jsengine.getBindings(ScriptContext.ENGINE_SCOPE).clear();
        if(jsengine instanceof Compilable){
            compiled = ((Compilable)jsengine).compile(RUNCODE);
        }
    }
    private boolean isempty(String str){
        return str==null || str.trim().isEmpty();
    }
    public Element run(int startNumber,String oddLineName,String evenLineName,Element[] lines,
            String containerid,String containerclassname,String language) throws ScriptException{
        if(lines==null)lines = new Element[0];
        Bindings bind = jsengine.getBindings(ScriptContext.ENGINE_SCOPE);
        bind.putAll(original);
        
        
        bind.put("startNumber", startNumber);
        bind.put("evenLineName",isempty(evenLineName)?"evenline":evenLineName);
        bind.put("oddLineName",isempty(oddLineName)?"oddline":oddLineName);
        bind.put("lines",lines);
        bind.put("id",containerid);
        bind.put("classname",containerclassname);
        bind.put("language",isempty(language)?"text":language);
        
        Object o;
        
        if(containerid!=null)o= compiled.eval(bind);
        else o=jsengine.eval(RUNCODE);
        
        bind.clear();
        
        if(o ==null || !(o instanceof Element)){
            throw new ScriptException("テンプレートの結果がnodamushi.hl.Elementではありません。　result:"+o);
        }
        
        return (Element)o;
    }
    
    
    
    
//ファクトリメソッド
    
    /**
     * ファイルを読み込んでTemplateEngine2を生成します
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
     * ファイルを読み込んでTemplateEngine2を生成します
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
     * ファイルを読み込んでTemplateEngine2を生成します
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
     * InputStreamを読み込んでTemplateEngine2を生成します
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
