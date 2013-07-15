function Test(x,y,z){
    this.x=x;
    this.y=y+z;
    this.z=z;
    if(typeof z === 'undefined'){
        //undefined error/ not reg
    }
    if(y==this.z){
        document.write("abcd'efg'");
        document.write('<tag id="ome">');
    }
    return null;
}

/*
  long comment*
 */

Test.prototype={
    k:function(){alert(1);},
    b:12
};

var test = new Test(1,2,3);
var reg = /aiu\/eo/g;

