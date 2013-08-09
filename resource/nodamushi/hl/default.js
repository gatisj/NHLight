/*
  Template Type OL
  このテンプレートは

  <code class="nhlight"><pre>
  <ol>
  <li>ソースコード</li>………
  </ol>
  </pre></code>
  
  という形で整形した結果を出力します。（※改行等は含みません）
  Mozillaのサイトにあったソースコード表示部分のHTMLをかなり昔に参考にしたんだったと思います。参考にしたページがわからない～

  author：nodamushi

*/

var clname = "nhlight";
if(classname){
    clname+=" "+classname;
}
var code = createElement("code");
code.setClassName(clname);
if(id){
    code.setID(id);
}
var pre = createElement("pre");
code.appendChild(pre);

foreach(function(i,linenumber,DOM,lineclassname,subclassname,label){
    var li = createElement("li");
    var sp = createElement("span");
    sp.setClassName("linecontainer");
    var cl;
    if(lineclassname!=""){
        cl=lineclassname;
    }else{
        cl = getOddOrEvenLineName(linenumber);
    }
    if(subclassname!=""){
        cl+=" "+subclassname;
    }
    li.setClassName(cl);

    if(label!=""){
        li.setAttribute(labelAttrName,label);
    }
    sp.appendChild(DOM);
    li.appendChild(sp);
    pre.appendChild(li);
});


return code;
