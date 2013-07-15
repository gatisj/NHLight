# jflexファイルのコンパイルを行うMakefile
# 環境がWindows用になってるので、パスのセパレータを変更してください。


SEP:= $(empty)\$(empty)
SPACE:= $(empty) $(empty)
ANALYSISDIR:= .. src nodamushi hl analysis
FLEXOUTDIR:= $(ANALYSISDIR) parser flex
FLEX := jflex

AD:=$(subst $(SPACE),$(SEP),$(ANALYSISDIR))
FOD:=$(subst $(SPACE),$(SEP),$(FLEXOUTDIR))

IN=$(filter-out template.jflex nhlight.jflex,$(wildcard *.jflex))

.SUFFIXES:	.jflex .java
.jflex.java:
	$(FLEX) --nobak -d $(FOD)  $<
all:flex nhlight

flex:$(IN:.jflex=.java)

nhlight:nhlight.jflex
	$(FLEX) --nobak -d $(AD)  $<



