-----------------------------------------------

NHLight ver 0.1

author:nodamushi
License:New BSD License

----------------------------------------------

　HTML上でプログラムソースコードを静的（JavaScript等を使わず）にシンタックスハイライトをする為のJavaプログラムです。
　主にnodamushi作のLamuriyanで利用する為に作られました。

***対応言語*************

Java
JavaScript
HTML



***インストール*********

適当なディレクトリに展開してください

http://code.google.com/p/juniversalchardet/
よりjuniversalchardetをダウンロードし、JavaのCLASSPATHが通ってるディレクトリか、同じディレクトリに保存してください



***ファイル構成*********

nhlight
├─javadoc :NHLightのJavaDocを格納したディレクトリ
├─testhtml:デザインの確認用HTMLファイルを入れたディレクトリ
├─nhlight.bat :WindowsでNHLightを簡単に実行する為のbatファイル
├─nhlight.jar :NHLightをまとめたJarファイル
├─nhlight_typeol.css  :nhlight用のCSS。醜いので自作推奨 (-_-)
├─nhlight_typeol.scss :nhlight_typeol.cssの元となったSassファイル
└─read.me :このファイルです



***実行方法*************

実行にはJavaRuntimeが必要です。jre7以上のバージョンが必要です。

java -cp "nhlight.jar;juniversalchardetの保存場所" nodamushi.hl.NHLight [Options] Inputfile

　オプションについては--helpオプションを付けて実行してください。Unix系の場合は-cpの区切り文字は;ではなく:です。
　長くて面倒くさいので、Windows用にnhlight.batファイルも用意してあります。そちらを使えば
nhlight [Option] Inputfile
　で起動できます。juniversalchardet-1.0.3.jarが同一フォルダに保存されていることを前提に書いてあるので、必要に応じて書き換えてください。



***CSSについて**********

　デフォルトのテンプレートを使うのであれば、nhlight_typeol.cssを利用できます。

利用できますが、

彩色センスのかけらもない私が適当に作ったので、かなり酷い出来です。はっきり言って使わない方が良いです。

　というわけで、自作してください。
　nhlight_typeol.cssはSassを利用して生成されたコードです。元ファイルはnhlight_typeol.scssで、こちらの編集をおすすめします。コメントからどういうトークンに対応するのかは類推してください。一応頑張ってコメント書きました。
　SassのインストールはRubyが必要です。Rubyをインストールした後「gem install sass」でインストールできます。詳しくは　http://sass-lang.com/download.html
　格好いいのできたらnodamushiにください。



***もっと細かくキーワードの操作とかしたい*********

　一応、制御することが出来るように設計してありますが、説明するのが面倒くさいっす。誰かが必要だ、という声をあげてくれたら書くっす。



***対応言語を増やせ*******

　手伝え！



***更新履歴*************

ver0.1[13/8/1] とりあえず、公開
