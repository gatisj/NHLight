package nodamushi.hl.analysis;

import java.util.*;

import nodamushi.hl.Element;
import nodamushi.hl.EscapeMap;
import nodamushi.hl.Node;
import nodamushi.hl.Pair;


//Elementに変換する
class TokenToElement {

	private List<Line> lines;
	private List<Flag> flags;
	private int tabSize;
	private boolean tabLengthFixed = false;
	private Map<Integer, String> classNameMap; 
	
	
	private final String SPACE,DQUOTE,LT, GT,AMP;
	
	

	public TokenToElement(Collection<Line> lines,int tabSize,boolean tabLengthFixed
			,Collection<Flag> flags,String userClassNameDefine,EscapeMap escape) {
		this.lines = new ArrayList<>(Objects.requireNonNull(lines));
		if(tabSize<1)tabSize=1;
		this.tabSize = tabSize;
		this.tabLengthFixed = tabLengthFixed;
		this.flags  = new ArrayList<>(flags);
		this.classNameMap=new HashMap<Integer, String>();
		setTokenClassNames(TokenTypePreDefine.DEFAULT_TOKEN_MAP);
		setTokenClassNames(userClassNameDefine);
		
		if(escape==null)escape = new EscapeMap();
		SPACE = escape.space();
		DQUOTE=escape.doublequote();
		LT=escape.lessthan();
		GT=escape.greaterthan();
		AMP=escape.and();
	}


	/**
	 * 対応する数値のトークンが変換されるspanのクラス名を定義します。<br>
	 * *や空文字を設定すると、そのトークンはspanで囲われません。
	 * @param number
	 * @param className
	 */
	public void setTokenClassName(int number,String className){
		if(className==null)return;
		classNameMap.put(number, className);
	}

	/**
	 * 番号:クラス名[;番号:クラス名]*<br>
	 * という構文で書かれた文字列から各トークンの番号に対するspanのクラス名を設定します。
	 * @param config
	 */
	public void setTokenClassNames(String config){
		if(config==null || config.isEmpty())return ;
		String[] sp = config.split(";");
		for(String s:sp){
			if(s.isEmpty())continue;
			String[] spp = s.split(":", 2);
			if(spp.length<2)continue;
			try{
				int num = Integer.parseInt(spp[0]);
				String name = spp[1];
				setTokenClassName(num, name);
			}catch(NumberFormatException e){}
		}
	}

	
	
	//convert to node で使う変数
	private static final String 
	block_span = "block-span",
	name_span = "name-span",
	document_fragment="document-fragment",
	SPAN="span",
	LINE="line",
	SUB_CLASS="sub-class";
	
	private Flag nextflag;
	private int nextFlagPosition,tokenx,linex;
	private int eindex = 0;
	private List<Element> lis;
	private Element li,currentTokenAppendNode;
	private Node parent;
	//tag name, class name
	private ArrayDeque<Pair<String,String>> spanstack=new ArrayDeque<>();
	private Token currentToken;
	
	private void clear(){
		nextflag =null;
		li=currentTokenAppendNode= null;
		nextFlagPosition=-1;
		tokenx=-1;
		eindex=0;
		lis=null;
		parent =null;
		spanstack.clear();
		currentToken = null;
		linex=0;
	}



	private void nextFlag(){
		nextflag = getNextFlag();
		nextFlagPosition=getNextFlagPosition();
	}

	private Flag getNextFlag(){
		if(flags.size()>eindex)return flags.get(eindex++);
		return null;
	}
	private int getNextFlagPosition(){
		return nextflag==null?-1:nextflag.position;
	}

	
//	/**
//	 * 行番号を表すのに&lt;ol>と&lt;li>を使った表現に変換します
//	 * @return
//	 */
//	public Element convertTypeOl(){
//	   List<Element> lis = _convertToNode();
//	   int i=startNumber;
//	   if(i!=1){
//	       ol.setAttribute("start", Integer.toString(i));
//	   }
//	   for(Element li:lis){
//	       Element e = new Element("li");
//	       String clname = li.getAttribute("class");
//	       if(clname.isEmpty()){
//	           clname = (i&1) == 1? oddLineClassName:evenLineClassName;
//	       }
//	       String subclass = li.getAttribute(SUB_CLASS);
//	       if(!subclass.isEmpty()){
//	           clname+=" "+subclass;
//	       }
//	       e.setClassName(clname);
//	       li.removeAttribute(SUB_CLASS);
//	       e.appendChild(li);
//	       li.setNodeName("span");
//	       li.setClassName("codecontainer");
//	       ol.appendChild(e);
//	       i++;
//	   }
//	   
//	   Element pre = new Element("pre"),code=new Element("code");
//	   code.setClassName(codeClassName);
//	   pre.appendChild(ol);
//	   code.appendChild(pre);
//	   
//	   return code;
//	}
	
	
	
	/**
	 * Elementの中間表現に変換
	 * @return
	 */
	public List<Element> convertToNode(){
		nextFlag();

		lis = new ArrayList<>();//<line>のリスト。
		li =null;//現在編集中の<line>
		parent=null;

		for(Line l:lines){
			linex=0;
			li = new Element(LINE);
			lis.add(li);
			parent = li;
			for(Token t:l.getTokens()){
				currentToken = t;
				int start = t.startPosition();
				int endx = start+t.length();
				tokenx = 0;
				Element element = createElement(t);
				currentTokenAppendNode= element;
				for(Pair<String,String> p:spanstack){
					Element elem = new Element(p.getA());
					currentTokenAppendNode.appendChild(elem);
					currentTokenAppendNode=elem;
				}

				checkFlag(start, endx);


				String v = currentToken.getString();
				String value = v.substring(tokenx);
				Node text = toText(value);
				currentTokenAppendNode.appendChild(text);

				//全部処理が終わったら
				if(element.getNodeName().equals(document_fragment)){
					List<Node> nodes=element.getChildNodes();
					for(Node n:nodes){
						parent.appendChild(n);
					}
				}else
					parent.appendChild(element);
			}//end for
		}//end for
		
		ArrayList<Element> result = new ArrayList<>();
		boolean start=false;
		for(Element l:lis){
		    if(start)
		        result.add(l);
		    else{//最初の空行の連続を削除。
		        String cont = l.getTextContent();
		        if(!cont.isEmpty()){
		            start = true;
		            result.add(l);
		        }
		    }
		}
		//最後の空行の連続を削除
		while(result.size()>0){
		    Element e = result.get(result.size()-1);
		    if(e.getTextContent().isEmpty()){
		        result.remove(result.size()-1);
		    }else break;
		}
		
		clear();
		
		
		//name-span,block-spanを普通のspanに名称変更
		for(Element l:result){
		    convertTagName(l.getChildNodes());
		}
		
		
		
		return result;
	}
	
	
	private void convertTagName(List<Node> nodes){
	    for(Node n:nodes){
	        switch(n.getNodeName()){
	            case name_span://オブジェクト使い回してるんだからif(name_span==にした方が処理が早いか？
	            case block_span:
	                n.setNodeName("span");
	                break;
	        }
	        convertTagName(n.getChildNodes());
	    }
	}

	//全角は2文字　半角は1文字として扱う。
	//参考： http://www.alqmst.co.jp/tech/040601.html
	private int charLength(char c){
		if( ( c<='\u007e' )|| // 英数字
				( c=='\u00a5' )|| // \記号
				( c=='\u203e' )|| // ~記号
				( c>='\uff61' && c<='\uff9f' ) // 半角カナ
				)
			return 1;
		else if(!Character.isDefined(c))//TODO 文字でないものは?として扱いたいんだけど、isDefinedじゃ全然判別できてない？
		    return 1;
		else
			return 2;
	}
	
//	private int stringLength(String str){
//		int x = 0;
//		for(int i=0,e=str.length();i<e;i++){
//		    char c = str.charAt(i);
//		    if(Character.isHighSurrogate(c)){//サロゲートペア
//		        if(i+1<e){
//		            char cc = str.charAt(i+1);
//		            if(!Character.isLowSurrogate(cc)){
//		                x+=1;//'?'扱い
//		            }else{
////		                int p = Character.toCodePoint(c, cc);
////		                if(Character.isDefine(p)){
//		                i++;
//		                x+=2;
//		                    
////		                }
//	                }
//		        }else x+=1;//'?'扱い
//		    }else
//		        x+=charLength(c);
//		}
//		return x;
//	}
	
	//文字をエスケープして書き出し
	private void print(char c,StringBuilder sb){
		switch(c){
		case '&':
			sb.append(AMP);
			break;
		case '<':
			sb.append(LT);
			break;
		case '>':
			sb.append(GT);
			break;
		case '"':
			sb.append(DQUOTE);
			break;
		case ' ':
			sb.append(SPACE);
			break;
		default:
		    if(Character.isDefined(c)){//TODO 文字でないものは?として扱いたいんだけど、isDefinedじゃ全然判別できてない？
		        sb.append(c);
		    }else{
		        sb.append("?");
		    }
			break;
		}
	}
	
	
	
	private Node toText(String str){
	    int lx = linex;
	    
	    StringBuilder sb = new StringBuilder();
	    for(int i=0,e=str.length();i<e;i++){
	        char c = str.charAt(i);
	        if(c=='\t'){
	            int loop=tabLengthFixed?
	                    tabSize  : (lx/tabSize + 1)*tabSize-lx;
	            for(int k=0;k<loop;k++)sb.append(SPACE);
	            lx +=loop;
	        }else{
	            if(Character.isHighSurrogate(c)){//サロゲートペア
	                if(i+1<e){
	                    char cc = str.charAt(i+1);
	                    if(Character.isLowSurrogate(cc)){
	                        i++;//次をスキップ
	                        lx+=2;//サロゲートペアは二文字幅と扱う。
	                        int p = Character.toCodePoint(c, cc);
	                        if(Character.isDefined(p)){//TODO 文字でないものは?として扱いたいんだけど、isDefinedじゃ全然判別できてない？
	                            sb.append(c).append(cc);
	                        }else{
	                            //文字として登録されていない場合。
	                            //ここは?を1文字にすべきか2文字にすべきか
	                            sb.append("??");
	                        }
	                        continue;
	                    }
	                }
	                //i+1==eか、下位がサロゲートペアじゃない場合
	                lx+=1;
	                sb.append('?');
	                
	            }else{//char１つで1文字
	                print(c,sb);
	                lx+=charLength(c);
	            }//end if surrogate
	        }//end if c=='\t'
	    }//end for
	    Node n = Node.createTextNode(sb.toString());
	    linex = lx;
	    return n;
			
	}
	
	
	private Element createElement(Token token){
		Element e;
		String classname = classNameMap.get(token.getType());
		if(classname!=null){
		    try{
		        int maxloop = 100;//無限ループ防止用
		        while(maxloop>0){
		            int n = Integer.parseInt(classname);
		            classname = classNameMap.get(n);
		            maxloop--;
		        }
		        if(maxloop==0){
		            System.err.println("クラス名の検索を中断しました。トークン番号："+token.getType());
		        }
		    }catch(NumberFormatException nfe){
		    }
		}
		if(classname==null)
		    classname = "tokentype"+token.getType();
		if(_isMakeSpan(classname)){
			e = new Element(SPAN);
			e.setClassName(classname);

		}else{
			e = new Element(document_fragment);
		}
		return e;
	}


	private void checkFlag(int from,int end){
		while(nextflag!=null){

			if(nextFlagPosition <from){
				nextFlag();
				continue;
			}

			if(nextFlagPosition >end)break;

			int type = nextflag.type;
			if(nextFlagPosition==end){//close系だけ処理
				switch(type){
				case Flag.CLOSE_NAME_SPAN:
					closeNameSpan(from);
					nextFlag();
					break;
//				case Flag.CLOSE_BLOCK_SPAN:
//					closeBlockSpan();
//					nextFlag();
//					break;
				default:
				    return;
				}
			}else{
				switch(type){
				case Flag.LINENAME:
					linenameFlag();
					break;
				case Flag.ADDLINENAME:
					addLineNameFlag();
					break;
				case Flag.NAME_SPAN:
					createNameSpan(from);
					break;
				case Flag.BLOCK_SPAN:
					createBlockSpan();
					break;
				case Flag.CLOSE_NAME_SPAN:
					closeNameSpan(from);
					break;
				case Flag.CLOSE_BLOCK_SPAN:
					closeBlockSpan();
					break;
				case Flag.LINE_NUMBER_FLAG:
				    lineNubmerFlag();
				    break;
				}
				nextFlag();
			}
		}
	}

	private void createBlockSpan(){
		String name = nextflag.name;
		Element e = new Element(block_span);
		e.setClassName(name);
		parent.appendChild(e);
		parent = e;
	}

	private String getCurrentTokenString(int from){
		String str = currentToken.getString();
		String value = str.substring(tokenx, nextFlagPosition-from);
		tokenx = nextFlagPosition-from;
		return value;
	}

	private void createNameSpan(int from){
		spanstack.add(new Pair<String, String>(name_span, nextflag.name));
		String value = getCurrentTokenString(from);
		Node text = toText(value);
		currentTokenAppendNode.appendChild(text);
		Element elem = new Element(name_span);
		elem.setClassName(nextflag.name);
		currentTokenAppendNode.appendChild(elem);
		currentTokenAppendNode = elem;
	}


	private void closeNameSpan(int from){
		spanstack.pollLast();
		Element closeTarget = currentTokenAppendNode;
		currentTokenAppendNode = (Element)closeTarget.getParentNode();
		String value = getCurrentTokenString(from);
		if(!value.isEmpty()){
			Node text =toText(value);
			closeTarget.appendChild(text);
		}
		String v = closeTarget.getTextContent();
		if(v.isEmpty()){//空なら削除
			currentTokenAppendNode.removeChild(closeTarget);
		}
	}

	private void closeBlockSpan(){
		String parentname = parent.getNodeName();
		if(block_span.equals(parentname)){
			parent = parent.getParentNode();
		}
	}

	private void linenameFlag(){
		String name = nextflag.name;
		li.setClassName(name);
	}
	
	
	private void lineNubmerFlag(){
	    String name = nextflag.name;
	    li.setAttribute("linenumberflag", name);
	}

	private void addLineNameFlag(){
		String name = nextflag.name;
		String old = li.getAttribute(SUB_CLASS);
		String n;
		if(old.isEmpty())n = name;
		else n = old+" "+name;
		li.setAttribute(SUB_CLASS, n);
	}


	private boolean _isMakeSpan(String className){
		return !(className.isEmpty() || "*".equals(className) );
	}



}