package nodamushi.hl;
/**
 * HTMLに変換する際、エスケープすべき文字の設定。<br>
 * 「 (半角スペース),",&,&lt;,>」<br>
 * をどのようにエスケープするかを設定します。<br>
 * 半角スペースは&amp;nbsp;に変換したくないなどの特殊な事情がなければ、NHLightはデフォルトの
 *設定を利用するのでこのクラスを触る必要はありません。<br><br>
 * デフォルトの値は
 * <ul>
 * <li>半角スペース: &amp;nbsp;</li>
 * <li>&lt;: &amp;lt;</li>
 * <li>>: &amp;gt;</li>
 * <li>": &amp;quot;</li>
 * <li>&: &amp;amp;</li>
 * </ul>
 * @author nodamushi
 *
 */
public class EscapeMap{
    
    private String
    space,lt,gt,amp,dquote;
    
    public static final String 
    DEFAULT_SPACE="&nbsp;",
    DEFAULT_LESSTHAN="&lt;",
    DEFAULT_GREATERTHAN="&gt;",
    DEFAULT_AND="&amp;",
    DEFAULT_DOUBLEQUOTE="&quot;";
    
    /**
     * デフォルトの設定でインスタンスを作成します。
     */
    public EscapeMap(){
        this(null, null, null, null, null);
    }
    
    /**
     * 各文字のエスケープ文字を定義してインスタンスを作成します。引数がnullの場合はデフォルトの文字が設定されます。
     * @param space
     * @param lessthan
     * @param greaterthan
     * @param and
     * @param doublequote
     */
    public EscapeMap(String space,
            String lessthan,String greaterthan,
            String and,String doublequote){
        space(space);
        lessthan(lessthan);
        greaterthan(greaterthan);
        and(and);
        doublequote(doublequote);
    }
    
    /**
     * spaceのエスケープ文字を定義します。
     * @param string nullの場合は&amp;nbsp;が設定されます
     */
    public void space(String string){
        if(string==null)string=DEFAULT_SPACE;
        space =string;
    }
    public String space(){
        return space;
    }
    
    /**
     * &lt;のエスケープ文字を定義します。
     * @param string nullの場合は&amp;lt;が設定されます
     */
    public void lessthan(String string){
        if(string==null)string=DEFAULT_LESSTHAN;
        lt=string;
    }
    
    public String lessthan(){
        return lt;
    }
    /**
     * >のエスケープ文字を定義します。
     * @param string nullの場合は&amp;gt;が設定されます
     */
    public void greaterthan(String string){
        if(string==null)string=DEFAULT_GREATERTHAN;
        gt=string;
    }
    
    public String greaterthan(){
        return gt;
    }
    /**
     * &amp;のエスケープ文字を定義します。
     * @param string nullの場合は&amp;amp;が設定されます
     */
    public void and(String string){
        if(string==null)string=DEFAULT_AND;
        amp = string;
    }
    
    public String and(){
        return amp;
    }
    /**
     * &quot;のエスケープ文字を定義します。
     * @param string nullの場合は&amp;quot;が設定されます
     */
    public void doublequote(String string){
        if(string==null)string=DEFAULT_DOUBLEQUOTE;
        dquote=string;
    }
    public String doublequote(){
        return dquote;
    }
}
