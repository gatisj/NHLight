package nodamushi.hl.analysis;

import java.util.*;

import nodamushi.hl.*;



public class TokenToHTML {

	private List<Line> lines;
	private List<Event> events;
	private int tabSize;
	private int startNumber=1;
	private boolean tabLengthFixed = false;
	private Map<Integer, String> classNameMap; 
	private boolean escapeSpace=true;
	private String codeClassName = "codetohtml";
	private String oddLineClassName="oddline";
	private String evenLineClassName="evenline";
	
	
	private String SPACE="&nbsp;";
	private String QUOTE="&quot;";
	private String LT="&lt;";
	private String GT="&gt;";
	private String AMP="&amp;";
	
	

	public TokenToHTML(Collection<Line> lines,int tabSize,boolean tabLengthFixed,int startNumber
			,Collection<Event> events,String userClassNameDefine) {
		this.lines = new ArrayList<>(Objects.requireNonNull(lines));
		if(tabSize<1)tabSize=1;
		this.tabSize = tabSize;
		this.tabLengthFixed = tabLengthFixed;
		this.startNumber = startNumber;
		this.events  = new ArrayList<>(events);
		this.classNameMap=new HashMap<Integer, String>();
		setTokenClassNames(TokenTypePreDefine.DEFAULT_TOKEN_MAP);
		setTokenClassNames(userClassNameDefine);
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
	OL="ol",
	LI="li",
	SUB_CLASS="sub-class";
	
	private Event nextevent;
	private int nextEventPosition,tokenx,linex;
	private int eindex = 0;
	private List<Element> lis;
	private Element li,currentTokenAppendNode;
	private Node parent;
	//tag name, class name
	private ArrayDeque<Pair<String,String>> spanstack=new ArrayDeque<>();
	private Token currentToken;
	
	private void clear(){
		nextevent =null;
		li=currentTokenAppendNode= null;
		nextEventPosition=-1;
		tokenx=-1;
		eindex=0;
		lis=null;
		parent =null;
		spanstack.clear();
		currentToken = null;
		linex=0;
	}



	private void nextEvent(){
		nextevent = getNextEvent();
		nextEventPosition=getNextEventPosition();
	}

	private Event getNextEvent(){
		if(events.size()>eindex)return events.get(eindex++);
		return null;
	}
	private int getNextEventPosition(){
		return nextevent==null?-1:nextevent.position;
	}

	
	/**
	 * 行番号を表すのに&lt;ol>と&lt;li>を使った表現に変換します
	 * @return
	 */
	public Element convertTypeOl(){
	   List<Element> lis = _convertToNode();
	   Element ol = new Element(OL);
	   int i=startNumber;
	   if(i!=1){
	       ol.setAttribute("start", Integer.toString(i));
	   }
	   for(Element li:lis){
	       Element e = new Element("li");
	       String clname = li.getAttribute("class");
	       if(clname.isEmpty()){
	           clname = (i&1) == 1? oddLineClassName:evenLineClassName;
	       }
	       String subclass = li.getAttribute(SUB_CLASS);
	       if(!subclass.isEmpty()){
	           clname+=" "+subclass;
	       }
	       e.setClassName(clname);
	       li.removeAttribute(SUB_CLASS);
	       e.appendChild(li);
	       li.setNodeName("span");
	       li.setClassName("codecontainer");
	       ol.appendChild(e);
	       i++;
	   }
	   
	   Element pre = new Element("pre"),code=new Element("code");
	   code.setClassName(codeClassName);
	   pre.appendChild(ol);
	   code.appendChild(pre);
	   
	   return code;
	}
	
	
	
	//中間表現に変更
	private List<Element> _convertToNode(){
		nextEvent();

		lis = new ArrayList<>();//<li>のリスト。
		li =null;//現在編集中の<li>
		parent=null;

		for(Line l:lines){
			linex=0;
			li = new Element(LI);
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

				checkEvent(start, endx);


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
	            case name_span:
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
		else
			return 2;
	}
	
	private int stringLength(String str){
		int x = 0;
		for(int i=0,e=str.length();i<e;i++){
			x+=charLength(str.charAt(i));
		}
		return x;
	}
	
	private String escape(char c){
		switch(c){
		case '&':
			return AMP;
		case '<':
			return LT;
		case '>':
			return GT;
		case '"':
			return QUOTE;
		case ' ':
			return SPACE;
		default:
			return Character.toString(c);
		}
	}
	
	private String escape(String str){
		StringBuilder sb = new StringBuilder();
		for(int i=0,e=str.length();i<e;i++){
			char c = str.charAt(i);
			sb.append(escape(c));
		}
		return sb.toString();
	}
	
	private Node toText(String str){
		if(str.contains("\t")){
			int lx = linex;
			if(!tabLengthFixed){
				StringBuilder sb = new StringBuilder();
				for(int i=0,e=str.length();i<e;i++){
					char c = str.charAt(i);
					if(c=='\t'){
						int pos = (lx/tabSize + 1)*tabSize;
						int length = pos-lx;
						for(int k=0;k<length;k++)sb.append(SPACE);//可変長
						lx = pos;
					}else{
						sb.append(escape(c));
						lx+=charLength(c);
					}
				}
				Node n = Node.createTextNode(sb.toString());
				linex = lx;
				return n;
			}else{
				StringBuilder sb = new StringBuilder();
				for(int i=0,e=str.length();i<e;i++){
					char c = str.charAt(i);
					if(c=='\t'){
						for(int k=0;k<tabSize;k++)sb.append(SPACE);//tabの長さはtabSizeで固定
						lx +=tabSize;
					}else{
						sb.append(escape(c));
						lx+=charLength(c);
					}
				}
				Node n = Node.createTextNode(sb.toString());
				linex = lx;
				return n;
			}
		}else{
			Node n = Node.createTextNode(escape(str));
			linex +=stringLength(str);
			return n;
		}
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


	private void checkEvent(int from,int end){
		while(nextevent!=null){

			if(nextEventPosition <from){
				nextEvent();
				continue;
			}

			if(nextEventPosition >end)break;

			int type = nextevent.type;
			if(nextEventPosition==end){//close系だけ処理
				switch(type){
				case Event.CLOSE_NAME_SPAN:
					closeNameSpan(from);
					nextEvent();
					break;
//				case Event.CLOSE_BLOCK_SPAN:
//					closeBlockSpan();
//					nextEvent();
//					break;
				default:
				    return;
				}
			}else{
				switch(type){
				case Event.LINENAME:
					linenameEvent();
					break;
				case Event.ADDLINENAME:
					addLineNameEvent();
					break;
				case Event.NAME_SPAN:
					createNameSpan(from);
					break;
				case Event.BLOCK_SPAN:
					createBlockSpan();
					break;
				case Event.CLOSE_NAME_SPAN:
					closeNameSpan(from);
					break;
				case Event.CLOSE_BLOCK_SPAN:
					closeBlockSpan();
					break;
				}
				nextEvent();
			}
		}
	}

	private void createBlockSpan(){
		String name = nextevent.name;
		Element e = new Element(block_span);
		e.setClassName(name);
		parent.appendChild(e);
		parent = e;
	}

	private String getCurrentTokenString(int from){
		String str = currentToken.getString();
		String value = str.substring(tokenx, nextEventPosition-from);
		tokenx = nextEventPosition-from;
		return value;
	}

	private void createNameSpan(int from){
		spanstack.add(new Pair<String, String>(name_span, nextevent.name));
		String value = getCurrentTokenString(from);
		Node text = toText(value);
		currentTokenAppendNode.appendChild(text);
		Element elem = new Element(name_span);
		elem.setClassName(nextevent.name);
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

	private void linenameEvent(){
		String name = nextevent.name;
		li.setClassName(name);
	}

	private void addLineNameEvent(){
		String name = nextevent.name;
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
