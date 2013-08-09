NHLight ver0.2

プログラムコードをHTML上でシンタックスハイライトする為に作ったプログラムです。
名前はNodamushi Highlightの略です。Syntaxは消えました。相変わらず名前を付けるのが適当なのは仕様です。

プログラムソースをトークン単位に分解し、<span>要素で囲み、クラス名を指定することでハイライトします。
最終的な結果の出力はテンプレートとかで柔軟に変更できるようになっているのが特徴（だとおもう）。

Q:JavaScriptのSyntaxHighlighter使えば良いんでね？
A:なんでシンタックスハイライトの為にJavaScript有効にしないといけないんですかー。嫌ですー。
　アンチJavaScript,Flash厨なんで、極力スクリプトを動かさないで表示したい。という私の要望から作りました。


なお、文字コードの判定にjuniversalchardet（http://code.google.com/p/juniversalchardet/）を利用しています。実行、コンパイルするにはjuniversalchardet.jarへのパスを通す必要があります。


対応している言語
Java
HTML
JavaScript


　各トークンはそのトークンの種類や意味によってトークン番号が振り分けられます。このトークン番号に従って<span>のclass名が選択されます。
　予め登録されている番号と変換されるclass名、トークンの意味は以下の様になっています。
-3  text-token  テキストやhtmlなどマークアップ言語で構文上意味を持たない地の文章。
-2  *            構文エラーか解析器のバグなどで解析できないとき。(*はspanタグを生成しません)
-1  *            空白を表すトークン(*はspanタグを生成しません)
 0  plain-token  一般的なトークン
 1  block-token  ブロックを構成する{}を表すトークン（lispなど言語によっては()も対象）
 2  parenthesis-token  関数呼び出しや数式中の()を表すトークン
 3  string-token  文字列を表すトークン
 4  comment-token  コメントを表すトークン
 5  branch-token  if,for,while,switchなど処理の流れを変えるトークン
 6  define-token  define,function,class,interface,enum,varなど何かを定義するトークン
 7  type-token  intやdoubleなど型を表すトークン
 8  return-token よくある言語ではreturnを表すトークン
 9  strong-keyword-token  強い強調をするトークン
10  keyword-token  中ぐらいの強調をするトークン
11  weak-keyword-token  弱い強調をするトークン
12  number-token  数値を表すトークン
13  access-token  object.clone()などの「.」やC言語の->などアクセスを表すトークン
14  regexp-token  javascriptやperlなどの正規表現構文を表すトークン
15  javadoc-token  JavaDocを表すトークン
16  char-token  Char型の文字を表すトークン（Javaの'a'など）
17  operatoer-token +-*/など演算子
18  eos-token   文末（end of a sentence）「;」が代表的
19  annotation-token  javaでは@Override等のアノテーション
20 declaration-token  htmlなどのマークアップ言語での宣言構文
21 tag-token             html,xmlなどのタグ。<, >, </, />が該当
22 tagname-token     html,xmlなどのタグの名前。
23 attribute-token     属性名

2147483647 NEWLINE_TOKEN　改行文字を表す。ユーザーはこの番号を使ってはならない。
2147483646 IDENTIFIER　　　キーワードになり得るトークンを表す。ユーザーはこの番号を使ってはならない。

　これらのデフォルトの設定を変更したい場合は、
0:plain;1:block;2:parent;
の様に「番号:名前」を「;（セミコロン）」で区切ることで指定する。
名前が見つからないときは「token番号」になる。100番ならば、token100になる。
また、名前ではなく番号を指定することも出来る。
100:1;
この場合、100番のトークンは1番のトークンの名前に設定される。1番がさらに番号が指定されている場合は、その番号の名前を設定する。
この処理は無限ループを回避する為、100回のループまでに名前が見つからないまでには名前が設定されていない物と見なす。




ユーザー指定ハイライト
　ソースコードの特定の部分をさらに強調したいと言う場合に用いる構文。ソースコードの中に直接記入する。

<<[linename=クラス名]>>
　各行の<li>のclass名を指定したクラス名に変更する。

<<[addlinename=クラス名]>>
　各行はデフォルトでは奇数行ではoddline、偶数行ではevenlineというクラス名が与えられる。このクラス名の他にさらに「付加」したい場合はこちらを用いる。


<<[name=クラス名]>>囲う内容<<[/]>>
　囲った内容を<span>で囲う。
　入れ子関係にすることは可能である。

<<[name=a]>>public<<[/]>> class Test{

の様に囲った場合
<span class="keywordm-token"><span class="a">public</span></span>  ………
となる。


　ここで注意するべきは、囲った内容が複数のトークンにまたがっている場合である。

public TestObject method(int x,int y){
とあったとき、理由は分からないが、中途半端にObjectからintのnまでをspanで囲いたいとする

　つまり、
public Test<<[name=a]>>Object method(in<<[/]>>t x,int y){
のように記述した場合である。

　このとき
<span>public</span> <span>Test<span class="a">Object</span></span><span class="a">&nbsp;</span><span><span class="a">method</span></span><span><span class="a">(</span></span><span><span class="a">in</span>t</span>………
という風に変換される。つまり、各トークンを囲うspanの中に新たにaというクラス名を持つspanが生成される。これはHTMLの性質上どうすることも出来ないと思われる。

　トークンのspanの外側を囲いたい場合は
<<[block=クラス名]>>囲う内容<<[/]>>
とする。ただし、先ほどのようにトークンの中途半端な位置から書こうと言うことは出来ない。必ずトークンの開始位置と終了位置に書かなくてはならない。




タブ文字の扱いについて。
　タブ文字はデフォルトでは4文字分の空白に変換される。これはタブ文字が必ず4文字の空白に変換されるというわけではなく、タブ文字の次の文字が4*n+1番目になるということである。英数字と\と~と半角カナ文字を1文字幅、それ以外は全部2文字幅と見なして空白の数は調節する。
　タブの幅を変えたい場合はソースコード中に
<<[tabsize=2]>>
と記述する。
　記述位置はどこでもよいが基本的に一番最初に書いておくとよい。NHLightは最初の空行の連続と最後の空行の連続は無視する。

　また、タブ幅を可変でなくする（常に決まった数の空白にする）には
<<[tabfix]>>
と記述する。


　なお、これら
<<[linename=クラス名]>>
<<[addlinename=クラス名]>>
<<[name=クラス名]>>
<<[block=クラス名]>>
<<[/]>>
<<[tabsize=長さ]>>
<<[tabfix]>>

を
<<[linename
=
クラス名
]>>
の様に複数行にわたって記述することは「できない」

　また、linenameやaddlinename等をl inenameやadd line nameの様にスペースで区切ることも許されない。（前後にスペースを入れることは許可される。）
　クラス名に使うことが出来る文字は英数字と-（ハイフン）と_(アンダーバー)である。それ以外の文字があった場合は構文とみなさない。（ただし、クラス名の後に空白を入れることは出来る。クラス名の中に空白を入れることは出来ない。）
長さで使うことが出来るのは0から9の数字のみである。（16の様に二文字になることは許される。）数字がない場合やそれ以外の文字が見つかった場合も構文と見なされない。





*************************************************************

出力結果のHTMLを整形するテンプレートについて

　テンプレートはJavaScriptで記述します。DOMオブジェクト（の様な物）を生成し、一つのElementを返します。
　ElementはDOMっぽいものを簡易的に扱えるように定義したnodamushi.hl.Elementであり、完全なDOMオブジェクトではないので注意してください。
　細かいことは、nodamushi.hl.default.jsを読む方が理解早いかも。


利用可能な変数：
　startNumber:行の開始番号
　oddLineName:奇数行のclass名
　evenLineName:偶数行のclass名
　lines:パース処理によって得られた各行のDOMが格納された配列
　id:一番外側のDOMに設定するid。空文字の場合は何も付加しないこと
　classname:一番外側のDOMに追加するclass名。空文字の場合は何も追加しないこと
　language:言語名


利用可能な関数：
　createElement(name):
　　名前がnameであるElementを生成します。

　foreach(func):
　　各行の処理についてfuncを呼び出します。

　　funcについて。
　　引数 (i,linenumber,innerDOM,className,subClassName,label)
　　　i:数値。現在のループ回数 0～行数-1　
　　　linenumber:数値。行番号
　　　innerDOM:Element。ソースコードの現在の行をパースして生成されたDOM
　　　className:string
　　　　nodamushiが作ったテンプレートのルールにおいて、「このclassNameが空文字でない場合、この値をclass名に使い、そうでない場合は偶数行奇数行に対応するclass名を行のクラス名とする。」と定義してある。
　　　subClassName:string
　　　　nodamushiが作ったテンプレートのルールにおいて、「subClassNameが空文字でない場合、行のclass名に追加する名前」と定義してある。半角スペース区切りになっている。
　　　label:string
　　　　Lamuriyanとの連携用。この文字列が空文字でない場合、行に「Lamuriyan-Label」という名前の属性にlabelの値を設定する。


　getOddOrEvenLineName(number):
　　numberが奇数の時oddLineNameを、偶数の時evenLineNameを返す関数

　print: 標準出力に文字を出力する。

Elementについて
　ものすごく適当に作ったDOMライクな自作Javaクラスです。本物のDOMと同じようには扱えません。
　生成：createElement関数を使う

　属性の設定。element.setAttribute(name,value)を使う

　属性を得る。element.getAttribute(name)を使う。返ってくる値はjava.util.Stringであり、JavaScriptのstringではないので注意。

　子の追加。element.appendChild(Element)を使う。

　文字列の追加。element.appendText(string)を使う
　
　classの設定。element.setClass(value)　ただのsetAttributeの簡略用関数
　idの設定。　element.setID(value)　　ただのsetAttributeの簡略洋館数
　


**************************************************************
TODO
文字として定義されていない値は?に置換したいのだが、Character.isDefinedではうまくいかないみたいです。
