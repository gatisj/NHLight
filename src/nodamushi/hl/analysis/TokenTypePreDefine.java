package nodamushi.hl.analysis;
/**
 * トークンのタイプは数値により指定され、-1～99までは前もって予約されている。<br>
 * 0以下のタイプのトークンは同じタイプのトークンが連続する限り結合されるという特徴を持つ。<br>
 * 独自拡張する場合は-2以下か100以上の数値を使う。ただし、Integer.MAX_VALUEは改行を表すトークンに当てられている。<br><br>
 * @author nodamushi
 *
 */
public interface TokenTypePreDefine{
    
    public static final String DEFAULT_TOKEN_MAP=
            "-3:text-token;"+
            "-2:*;" +
            "-1:*;" +
            "0:plain-token;" +
            "1:block-token;" +
            "2:parenthesis-token;" +
            "3:string-token;" +
            "4:comment-token;" +
            "5:branch-token;" +
            "6:define-token;" +
            "7:type-token;" +
            "8:return-token;" +
            "9:strong-keyword-token;" +
            "10:keyword-token;" +
            "11:week-keyword-token;" +
            "12:number-token;" +
            "13:access-token;" +
            "14:regexp-token;" +
            "15:javadoc-token;" +
            "16:char-token;"+
            "17:operator-token;" +
            "18:eos-token;" +
            "19:annotation-token;" +
            "20:declaration-token;" +
            "21:tag-token;" +
            "22:tagname-token;" +
            "23:attribute-token;"+
            "24:command-token;"
            ;
    
    
    
    /**改行トークンを表す番号。使用禁止*/
    public static final int NEWLINE_TOKEN = Integer.MAX_VALUE;
    /**キーワードトークンになり得るトークン。
     * 一時的な番号で、キーワードにマッチしない場合はPLAIN_TOKENに変更される。
     * 使用禁止。*/
    public static final int IDENTIFIER =Integer.MAX_VALUE-1;
    
    public static final int TEXT = -3;
    /** なんかよーわからん時。エラー対象*/
    public static final int UNKNOWN = -2;
    /**空白トークンを表す番号*/
    public static final int SPACE_TOKEN = -1;
    /**地の文のトークンを表す番号*/
    public static final int PLAIN_TOKEN = 0;
    /**ブロックを構成する{}を表すトークン番号　
     * （言語によっては()や&lt;>も対象）*/
    public static final int BLOCK_TOKEN = 1;
    /**関数呼び出しや数式中の()を表すトークン番号*/
    public static final int PARENTHESIS_TOKEN = 2;
    /**文字列を表すトークン番号*/
    public static final int STRING_TOKEN = 3;
    /**コメントトークンを表す番号*/
    public static final int COMMENT_TOKEN = 4;
    /**if,for,break,while,switchなど処理の流れを変えるトークンを表す番号*/ 
    public static final int BRANCH_TOKEN = 5;
    /**define,function,class,interface,enum,varなど定義を行うトークン番号*/
    public static final int DEFINE_TOKEN = 6;    
    /**intやdoubleなど方を表すトークン番号。<br><br>
     * 本当はDate date = new Date();の最初のDateも対応したいが………*/
    public static final int TYPE_TOKEN = 7;
    /**returnトークンの番号*/
    public static final int RETURN_TOKEN = 8;
    /**強強調するキーワードを表す番号*/
    public static final int KEYWORD_STRONG_TOKEN = 9;
    /**中強調をするキーワードを表す番号*/
    public static final int KEYWORD_MIDIUM_TOKEN = 10;
    /**弱強調をするキーワードを表す番号*/
    public static final int KEYWORD_WEAK_TOKEN = 11;
    /**数値を表す番号*/
    public static final int NUMBER_TOKEN=12;
    /**object.clone()などの「.」やCの->などの様なアクセスを表すトークンの番号*/
    public static final int ACCESS_TOKEN=13;
    /**JavaScriptやPerlなどにおける正規表現構文を表すトークン番号*/
    public static final int REGULAR_EXPRESSION_TOKEN = 14;
    /**JavaDocを表すトークン番号*/
    public static final int JAVADOC_TOKEN = 15;
    /**Char型の文字を表すトークン(Javaだったら'a'など)*/
    public static final int CHARSTRING_TOKEN=16;
    /**+-*などの演算子*/
    public static final int OPERATOR_TOKEN=17;
    /**「;」に代表される文末文字*/
    public static final int EOS_TOKEN=18;
    /** アノテーションを表すトークン。主にJavaとか。*/
    public static final int ANNOTATION_TOKEN=19;
    /**&lt;!DOCTYPE>宣言などmarkup declarationを表す*/
    public static final int MARKUP_DECLARATION_TOKEN=20;
    /**マークアップ言語おけるタグの開始文字と終了文字。<br>
     * XML,HTMLでは&lt; &gt;がそれに当たる。*/
    public static final int MARKUP_TAG_TOKEN=21;
    /**
     * マークアップ言語におけるタグの名前
     */
    public static final int MARKUP_TAG_NAME_TOKEN=22;
    /**マークアップ言語における属性名*/
    public static final int MARKUP_TAG_ATTRIBUTE_TOKEN=23;
    /**
     * マークアップ言語におけるコマンドを表すトークン。<br>
     * 主にTeXの\command が対象
     */
    public static final int MARKUP_COMMAND_TOKEN=24;
}
