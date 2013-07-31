package nodamushi.hl.analysis;
//説明が難しいなー。だいたい、フラグって何？元々はEventって名前だったし。
//ま～、なんも思いつかなかったんだけど

class Flag{
    public static final int
    LINENAME=1,
    ADDLINENAME=2,
    
    NAME_SPAN = 3,
    BLOCK_SPAN=4,
    CLOSE_NAME_SPAN=5,
    CLOSE_BLOCK_SPAN=6,
    
    LINE_NUMBER_FLAG=-1;//Lamuriyanとの連携で行番号をLamuriyan処理系に返したい場合様のフラグ
    
    
    
    
    public final int position;//char[]のポジション
    public final int type;//何をするか
    /**spanやliのクラス名やフラグ名*/
    public final String name;
    public final Flag closeTarget;//typeが5,6の時、閉じる対象
    
    
    public Flag(int position,int type,String name){
        this(position,type,name,null);
    }
    
    public Flag(int position,int type,String name,Flag closeTarget){
        this.position = position;
        this.type = type;
        this.name = name;
        this.closeTarget =closeTarget;
    }
    
    
}
