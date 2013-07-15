package nodamushi.hl.analysis;
import nodamushi.hl.analysis.Token;

%%

%class NHLightFlexParser
%unicode
%char
%line
%function parse
%type Token


%{
  public static final int
    DEFAULT_TEXT = 0,
    LINENAME  = 1,
    ADDLINENAME = 2,
    NAME = 3,
    BLOCK=4,
    CLOSE=5,
    TABSIZE=6,
    TABFIX=7;
  
  public NHLightFlexParser(String str){
    this(new java.io.StringReader(str));
    this.originalSource = str;
  }

  private String originalSource;


  private Token token(int type){
    return token(type,yychar,yylength());
  }

  private Token token(int type,int xchar,int length){
    return new Token(type,xchar,length,0,originalSource);
  }
%}



WhiteSpace = [ \t\f]*


  /*<<[]>>では不都合だから、タグの開始文字終了文字を変えたいときは、ここを変更*/
BEGIN ="<<["{WhiteSpace}
END ={WhiteSpace}"]>>"
 /* = なんて格好悪い!やめて！という場合はここを変更*/
EQ ={WhiteSpace}"="{WhiteSpace}
 /* クラス名に使える文字を限定しないで！という場合はここを変更 */
NAME=[a-zA-Z][a-zA-Z0-9\-_]*

/*  これは変えちゃダメ*/
Num=[0-9]+

%%


<YYINITIAL>{
  {BEGIN}"linename"{EQ}{NAME}{END}	{
    return token(LINENAME);
  }
  
  {BEGIN}"addlinename"{EQ}{NAME}{END}	{
    return token(ADDLINENAME);
  }
  
  {BEGIN}"name"{EQ}{NAME}{END}		{
    return token(NAME);
  }
  
  {BEGIN}"block"{EQ}{NAME}{END}		{
    return token(BLOCK);
  }
  
  {BEGIN}"/"{END}			{
    return token(CLOSE);
  }
  
  {BEGIN}"tabsize"{EQ}{Num}{END}	{
    return token(TABSIZE);
  }
  
  {BEGIN}"tabfix"{END}			{
    return token(TABFIX);
  }
  
  [^<]+|<	{
    return token(DEFAULT_TEXT);
  }
}


