package nodamushi.hl.analysis.parser;

import java.util.List;
import java.util.regex.Pattern;

import nodamushi.hl.analysis.Token;

/**
 * Parserを実装するクラスは引数なしのデフォルトコンストラクタを実装してください。<br>
 * Parsreは一回parse()を呼び出される毎に使い捨てられます。
 * @author nodamushi
 *
 */
public interface Parser{
    /**
     * defineIdentifierTokenPatternで使用する、各行から整数とパターンを抜き出す為の正規表現。<br>
     * 整数を得る場合はInteger.parseInt(matcher.group(1))を、パターンを得る場合はmathcer.group(2).trim()を用いてください。<br>
     * (※matcherはmather(String)により生成されるMatcherオブジェクト)<br><br>
     * @see Parser#defineIdentifierTokenPattern(String)
     */
    public static final Pattern DEFINEIDENTIFIER_PATTERN = Pattern.compile("^[ \t\f]*(-?[0-9]+)[ \t\f]*=(.*)");
    
    /**
     * 渡された文字列をパースし、Tokenのリストを生成します。<br>
     * その際、改行の情報を失わないで下さい。
     * @param source
     * @return
     */
    public List<Token> parse(String source);
    
    /**
     * IDENTIFIERトークンを扱わない場合は、このメソッドの実装は特に必要ありません。<br><br>
     * IDENTIFIERトークンはトークンの保持する文字列がキーワードのパターンにマッチした場合、
     * 設定された番号に変更され、マッチしなかった場合は0番（PLAIN_TOKEN)に変更するというルールがあります。<br>
     * <font color="gray">（※JFlexファイルで定義し、再コンパイルするという作業が非効率、かつ変更に弱い仕様だった為、このルールを導入しました。）</font><br><br>
     * このメソッドはそのパターンと番号の設定を行うメソッドです。<br><br>
     * 引数の文字列は<br><br>
     * 整数=パターン<br>
     * 整数=パターン<br>
     * ………<br><br>
     * という書式です。パターン部分は前後の半角空白を取り除いてから<br>
     * <b>^(パターン)$</b><br>
     * という正規表現のパターンを生成します。<br>
     * なお、<b>「整数=」から始まらない行はコメントと見なし、無視します。</b><br>
     * 各行のパターンマッチングにはDEFINEIDENTIFIER_PATTERNを利用してください。<br><br>
     * IDENTIFIERトークンの文字列がパターンにマッチしたとき、IDENTIFIERトークンは左辺の数値の番号に変更されます。<br>
     * 複数のパターンにマッチする場合は、<b>後から定義された物が優先されます。</b>
     * @param s
     */
    public void defineIdentifierTokenPattern(String s);
    
    
    /**
     * 言語名を返します
     * @return 言語名
     */
    public String language();
    
}
