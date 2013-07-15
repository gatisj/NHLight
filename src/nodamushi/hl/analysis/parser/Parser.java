package nodamushi.hl.analysis.parser;

import java.util.List;

import nodamushi.hl.analysis.Token;

/**
 * Parserを実装するクラスは引数なしのデフォルトコンストラクタを実装してください。
 * @author nodamushi
 *
 */
public interface Parser{
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
     * このメソッドはそのパターンと番号の設定を行うメソッドです。<br><br>
     * 引数の文字列は<br><br>
     * 整数=パターン<br>
     * 整数=パターン<br>
     * ………<br><br>
     * の様にします。パターン部分は前後の半角空白を取り除いてから<br>
     * ^(パターン)$<br>
     * という正規表現のパターンを生成します。<br>
     * IDENTIFIERトークンの文字列がパターンにマッチしたとき、IDENTIFIERトークンは左辺の数値の番号に変更されます。<br>
     * 複数のパターンにマッチする場合は、後から定義された物が優先されます。
     * @param s
     */
    public void defineIdentifierTokenPattern(String s);
}
