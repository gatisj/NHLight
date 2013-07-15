/* template ver 1.0 */
package nodamushi.hl.analysis.parser.flex;
import nodamushi.hl.analysis.Token;
import nodamushi.hl.analysis.parser.AutoGeneratedParser;

%%


%class JavaFlexParser
%implements AutoGeneratedParser
%unicode
%char
%line
%function _parse
%type void
%public


%{

  public JavaFlexParser(String str){
    this(new java.io.StringReader(str));
    this.originalSource = str;
  }

  private String originalSource;

  private java.util.ArrayList<Token> tokens = new java.util.ArrayList<>();

  @Override
  public java.util.Collection<Token> parse(){
    tokens.clear();
    try{
        _parse();
    }catch(Exception e){e.printStackTrace();}
    return tokens;
  }


  private void push(Token t){
    if(t!=null)
      tokens.add(t);
  }
  private Token token(int type){
    return token(type,yychar,yylength());
  }

  private Token token(int type,int xchar,int length){
    return new Token(type,xchar,length,yyline,originalSource);
  }
  

  private int tokenStartPosition=0;
  private int tokenLength=0;
  private boolean nowLong=false;

  private void longTokenInit(){
    tokenStartPosition = yychar;
    tokenLength=0;
    nowLong=true;
  }

  private void longTokenInit_by_NewLine(){
    tokenStartPosition = yychar+yylength();
    tokenLength=0;
    nowLong=true;
  }
  
  private void addLongToken(){
    tokenLength += yylength();
  }
  
  private Token longToken(int type){
    nowLong=false;
    if(tokenLength==0)return null;
    return token(type,tokenStartPosition,tokenLength);
  }

%}

%eofval{
  if(nowLong){
    switch(yystate()){
    case STRING:
      push(longToken(STRING_TOKEN));
      break;
    case COMMENTS:
      push(longToken(COMMENT_TOKEN));
      break;
    case JAVADOC:
      push(longToken(JAVADOC_TOKEN));
      break;
    }
  }
  return;
%eofval}

LineTerminator =\r|\n|\r\n
InputCharacter =[^\r\n]
WhiteSpace = [ \t\f]+



PlainSymbol = ,|:
  


TraditionalComment = "/*"
EndOfLineComment = "//"{InputCharacter}*
JavaDoc = "/**"
CommentEnd = "*/"
CommentContent = ([^*\n\r]|\*+[^*/\r\n])*

Identifier=[:jletter:][:jletterdigit:]*



EqOperatorSymbol= \+|-|\*|\/|\%|\!|\<|\>|\=|&|\||\^|&&|\|\||\>\>|\>\>\>|\<\<|\~
OperatorSymbol = {EqOperatorSymbol}\=|{EqOperatorSymbol}|\+\+|--|\?



BranchSymbol=for|while|do|continue|break|if|switch|else|case|default
TypeSymbol=byte|char|short|int|long|float|double|boolean



EOS = ";"

ANNOTATION=@{Identifier}

DefineSymbol=class|interface|enum

ReturnSymbol =return


IntLiteral = [0-9]+(i|I|l|L)?
DecLiteral = ([0-9]+\.[0-9]*|[0-9]*\.[0-9]+)(f|F|d|D)?
NumberLiteral = {IntLiteral}|{DecLiteral}


%state STRING
%state COMMENTS
%state JAVADOC
%state CHAR
%%


<YYINITIAL>{
  {BranchSymbol}	{push(token(BRANCH_TOKEN));return;}
  {TypeSymbol}		{push(token(TYPE_TOKEN));return;}
  {ReturnSymbol}	{push(token(RETURN_TOKEN));return;}
  {DefineSymbol}	{push(token(DEFINE_TOKEN));return;}
  {OperatorSymbol}	{push(token(OPERATOR_TOKEN));return;}
  {EOS}			{push(token(EOS_TOKEN));return;}
  {NumberLiteral}	{push(token(NUMBER_TOKEN));return;}
  {EndOfLineComment}	{push(token(COMMENT_TOKEN));return;}
  {LineTerminator}	{push(token(NEWLINE_TOKEN));return;}
  {WhiteSpace}		{push(token(SPACE_TOKEN));return;}
  \"			{longTokenInit();addLongToken();yybegin(STRING);}
  \'			{longTokenInit();addLongToken();yybegin(CHAR);}
  {JavaDoc}		{longTokenInit();addLongToken();yybegin(JAVADOC);}
  {TraditionalComment}	{longTokenInit();addLongToken();yybegin(COMMENTS);}
  {ANNOTATION}		{push(token(ANNOTATION_TOKEN));return;}
  \.			{push(token(ACCESS_TOKEN));return;}
  \{|\}			{push(token(BLOCK_TOKEN));return;}
  \(|\)|\[|\]		{push(token(PARENTHESIS_TOKEN));return;}
  {PlainSymbol}		{push(token(PLAIN_TOKEN));return;}
  {Identifier}		{push(token(IDENTIFIER));return;}
}

<STRING>{
  "\\\\"	{addLongToken();}
  "\\\""	{addLongToken();}
  [^\n\r\"\\]+	{addLongToken();}
  {LineTerminator}	{
    push(longToken(STRING_TOKEN));
    push(token(NEWLINE_TOKEN));
    longTokenInit_by_NewLine();
    return;
  }
  \"			{yybegin(YYINITIAL);addLongToken();push(longToken(STRING_TOKEN));return;}
  \\		{addLongToken();}

}

<CHAR>{
  \\\\|\\\'|[^\n\r\']+	{addLongToken();}
  {LineTerminator}	{
    push(longToken(CHARSTRING_TOKEN));
    push(token(NEWLINE_TOKEN));
    longTokenInit_by_NewLine();
    return;
  }
  \'			{yybegin(YYINITIAL);addLongToken();push(longToken(CHARSTRING_TOKEN));return;}
}


<COMMENTS>{
  {CommentEnd}		{yybegin(YYINITIAL);addLongToken();push(longToken(COMMENT_TOKEN));return;}
  {CommentContent}*	{addLongToken();}
    
  {LineTerminator}	{
    push(longToken(COMMENT_TOKEN));
    push(token(NEWLINE_TOKEN));
    longTokenInit_by_NewLine();
    return;
  }
  .			{addLongToken();}
}
<JAVADOC>{
  {CommentEnd}		{yybegin(YYINITIAL);addLongToken();push(longToken(JAVADOC_TOKEN));return;}
  {CommentContent}*	{addLongToken();}
  {LineTerminator}	{
    push(longToken(JAVADOC_TOKEN));
    push(token(NEWLINE_TOKEN));
    longTokenInit_by_NewLine();
    return;
  }
  .			{addLongToken();}
}



/* error fallback */
.|\n                             { System.err.println("token error:char("+yychar+") string: "+yytext());yybegin(YYINITIAL);push(token(UNKNOWN));return; }
