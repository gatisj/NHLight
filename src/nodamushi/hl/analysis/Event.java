package nodamushi.hl.analysis;

class Event{
    public static final int
    LINENAME=1,
    ADDLINENAME=2,
    
    NAME_SPAN = 3,
    BLOCK_SPAN=4,
    CLOSE_NAME_SPAN=5,
    CLOSE_BLOCK_SPAN=6;
    
    
    
    
    public final int position;//char[]のポジション
    public final int type;//何をするか
    public final String name;//spanやliのクラス名
    public final Event closeTarget;//typeが5,6の時、閉じる対象
    
    
    public Event(int position,int type,String name){
        this(position,type,name,null);
    }
    
    public Event(int position,int type,String name,Event closeTarget){
        this.position = position;
        this.type = type;
        this.name = name;
        this.closeTarget =closeTarget;
    }
    
    
}
