class Test extends Tes
  constractor:(@a,b)->
    @b = b*10

  func:()->
    eventf=(e)=>
      console.log e
    eventf:eventf

#this is comment

###
 this is comment #

 this is comment
###

test = new Test(10,100)
if !test? then alert "ok"
else alert "#{"test.func=#{test.func()}"}
this is string
"

sstring = '#{意味なし}'
heredoc ='''
あ
　い
　　う
'''
for x in [0...10]
  alert x

