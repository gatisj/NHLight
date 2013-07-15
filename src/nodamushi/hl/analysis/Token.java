package nodamushi.hl.analysis;



/**
 * 構文解析により生成されるトークンを表すクラス。<br>
 * トークンのタイプは数値により指定され、-1～99までは前もって定義されている。<br>
 * 0以下のタイプのトークンは同じタイプのトークンが連続する限り結合されるという特徴を持つ。<br>
 * 独自拡張する場合は-2以下か100以上の数値を使う。ただし、Integer.MAX_VALUEは改行を表すトークンに当てられている。<br><br>
 * 予め定義されている数値についてはTokenTypePreDefineの各定義を参照のこと
 * @author nodamushi
 * @see TokenTypePreDefine
 *
 */
public class Token implements Cloneable{
    
    private int type,xchar,length,yline;
    final private String source;
    
    public Token(int type,int startposition,int length,int linenumber,String originalString){
        if(startposition < 0)throw new IllegalArgumentException("xchar < 0  :"+startposition);
        if(length < 0)throw new IllegalArgumentException("length < 0  :"+length);
//        if(linenumber < 0)throw new IllegalArgumentException("yline < 0  :"+linenumber);
        if(originalString == null)throw new NullPointerException("originalString is null!");
        
        int ll = originalString.length();
        if(ll < startposition || ll < startposition+length)throw new IllegalArgumentException(String.format(
                "out of string range:xchar[%d],length[%d],originalString length[%d]", startposition,length,originalString.length()));
        
        
        this.type = type;
        this.xchar = startposition;
        this.length = length;
        this.yline = linenumber;
        this.source = originalString;
    }
    
    
    public void setLineNumber(int n){
        yline = n;
    }
    
    /**
     * このトークンのタイプを表す番号を返します。
     * @return
     */
    public int getType(){
        return type;
    }
    
    /**
     * このトークンのタイプを表す番号を設定します。
     * @param i
     */
    public void setType(int i){
        type = i;
    }
    
    /**
     * このトークンが元となる文字列のどの位置から開始されるかを返します。
     * @return 開始位置
     * @see Token#length()
     * @see Token#getString()
     */
    public int startPosition(){
        return xchar;
    }
    
    /**
     * このトークンの文字列長を返します。
     * @return 文字列長
     */
    public int length(){
        return length;
    }
    
    /**
     * このトークンが何行目に位置するかを返します。<br>
     * 行数は0番目から始まります。
     * @return
     */
    public int getLineNumber(){
        return yline;
    }
    
    /**
     * このトークンの内容の文字列を返します。
     * @return トークンの内容の文字列
     * @see Token#length()
     * @see Token#startPosition()
     * @see Token#getOriginalString()
     */
    public String getString(){
        return source.substring(xchar,xchar+length);
    }
    
    /**
     * このトークンとtをひとまとめにします。<br>
     * tのstartPositionとこのトークンのstartPosition+lengthが一致しない場合や、
     * line numberが一致しない場合、
     * originalStringが同じで無い場合はfalseが返り何もされません。<br>
     * 一致する場合は、このトークンの長さにtのトークンの長さが追加され、trueを返します。<br><br>
     * なお、二つのトークンのタイプは一致している必要はありません。
     * @param t ひとまとめにするトークン。
     * @return 結合に成功したかどうか
     */
    public boolean add(Token t){
        if(t.source.equals(source)
                &&t.yline == yline && t.xchar == xchar+length){
            length += t.length;
            return true;
        }else return false;
    }
    
    
    /**
     * このトークンの元となる文字列を返します。<br>
     * この文字列に対してstartPosition()からlength()の長さ切り出した文字列が
     * このトークンが表す文字列になります。
     * @return 元となる文字列
     */
    public String getOriginalString(){return source;}
    
    /**
     * addと違い新たなトークンを生成します。<br>
     * このトークンのクローンを作成した後に、クローンに対してaddを呼び出します。
     * @param t 結合するトークン
     * @return 結合したトークン
     * @see Token#add(Token)
     */
    public Token join(Token t){
        Token me = clone();
        if(me.add(t))return me;
        return null;
    }
    
    @Override
    public Token clone(){
        return new Token(type, xchar, length, yline, source);
    }
    
    @Override
    public String toString(){
        return String.format("Token\n type:%d\n" +
        		" start position: %d\n" +
        		" length: %d\n line:%d\nvalue:{%s}",type,xchar,length,yline,getString());
    }
}
