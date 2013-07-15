無事に見つかったときの処理<<[tabsize=8]>>

<<[name = backhilight]>>package<<[/]>> testcode;
import java.lang.*;

<<[linename = backhilight]>>public class Test{
    <<[block = bolds]>>public s<<[name=red]>>tat<<[/]>>ic void<<[/]>> main(String[] args){
	String	test = "あbc\"de";	//Stringとtestの間はタブ文字です
	char	ch = '\n';		//charとchの間もタブ文字です
	int	i = 10 + (int)20.9d;	//intとiの間もタブ文字です。
	Te<<[name = red]>>st.class.get<<[/]>>Name();
	Test test = new Test();
	int k = 100<<10;
    }

    /**
       here is
       JavaDoc*
     */
    public Test(){
	//singlecomment//
	/*
	  long comment*
	*/
    }
}
