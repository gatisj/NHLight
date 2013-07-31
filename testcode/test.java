<<[tabsize=8]>>

package testcode;
import java.lang.*;

public class Test{
	public static void main(String[] args){
		String	test = "あ丈bc\"ｱ";	//タブ文字による位置揃えのデモ
		char	ch = '\n';		//丈はサロゲートペアだけど、
		int	i = 10 + (int)20.9d;	//ちゃんとそろっています。たぶんWindowsXPの人は分からなくてｺﾞﾒﾝﾈ
		Test.class.getName();
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
	@Deprecated
	public void method(){
		final int number = 1000;
		if(number == 0)return;
		for(int i=0;i<number;i++){
			break;
		}
	}
}
