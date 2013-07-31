# jflexファイルのコンパイルを行うMakefile
# 環境がWindows用になってるので、パスのセパレータを変更してください。


SEP:= $(empty)\$(empty)
SPACE:= $(empty) $(empty)
ANALYSISDIR:= .. src nodamushi hl analysis
HTMLDIR:= .. src nodamushi hl html
FLEXOUTDIR:= $(ANALYSISDIR) parser flex
FLEX := jflex

AD:=$(subst $(SPACE),$(SEP),$(ANALYSISDIR))
FOD:=$(subst $(SPACE),$(SEP),$(FLEXOUTDIR))
HTML:=$(subst $(SPACE),$(SEP),$(HTMLDIR))
IN=$(filter-out template.jflex nhlight.jflex htmltemplate.jflex,$(wildcard *.jflex))

.SUFFIXES:	.jflex .java
.jflex.java:
	$(FLEX) --nobak -d $(FOD)  $<
all:flex nhlight htmltemplate

flex:$(IN:.jflex=.java)

nhlight:nhlight.jflex
	$(FLEX) --nobak -d $(AD)  $<

htmltemplate:
	$(FLEX) --nobak -d $(HTML) htmltemplate.jflex

htmltemplate.jflex:htmltemplate
