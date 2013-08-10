# .javaを.classにコンパイルするMakefile
# juniversalchardet.jarへのパスの設定と、
# Windows用になっているので、パスのセパレータを変更してください。


JUNIVERSALCHARDET:=C:\javaclasspath\juniversalchardet-1.0.3.jar
#VPATHの区切り文字はWindowsでは;だけど、UNIX系では:だとからしい
DSEP:=;

#runでテストするファイル名
TESTFILE:=test.tex

ALLPACKAGES:=\
nodamushi/hl \
nodamushi/hl/analysis \
nodamushi/hl/html \
nodamushi/hl/analysis/parser \
nodamushi/hl/analysis/parser/flex

JAVADOCPACKAGES:=\
nodamushi/hl \
nodamushi/hl/analysis \
nodamushi/hl/analysis/parser \
nodamushi/hl/html

SPACE:=$(empty) $(empty)


CP:=".;$(JUNIVERSALCHARDET)"
BIN:=bin
SRC:=src
DOCDIR:=javadoc
RESOURCE:=resource

SEARCHDIR:=$(SRC) $(foreach package,$(ALLPACKAGES),$(SRC)/$(package))
VPATH:=$(subst $(SPACE),$(DSEP),$(SEARCHDIR))

JAVAFILES:=$(foreach dir,$(SEARCHDIR),$(wildcard $(dir)/*.java))
JAVAC:=javac -cp $(CP) -encoding "utf-8" -sourcepath "$(SRC)" -d "$(BIN)" 
JARNAME:=nhlight.jar
JAR:=jar 
JAROPTION:=cvf $(JARNAME)
DOCTITLE:=NHLight
JAVADOC:=javadoc -doctitle "$(DOCTITLE)" -nodeprecated -charset utf-8 -docencoding utf-8 -encoding utf-8  -protected  -d $(DOCDIR) -sourcepath $(SRC) $(foreach pack,$(JAVADOCPACKAGES),$(subst /,.,$(pack)))

.SUFFIXES:	.java .class
.java.class:
	mkdir -p $(BIN)
	$(JAVAC) "$<"

all:FORCE
	rm -rf $(BIN)
	mkdir -p $(BIN)
	$(JAVAC) $(JAVAFILES)
	cp -r $(RESOURCE)/nodamushi $(BIN)

jar:all
	$(JAR) $(JAROPTION) -C $(BIN) nodamushi

#juniversalchardet.jarもjar内部に突っ込み、単体で動かせるjarを作る。
jarall:all
	$(JAR) -xf $(JUNIVERSALCHARDET) org
	$(JAR) $(JAROPTION) org -C $(BIN) nodamushi
	rm -rf org

clean:FORCE
	rm -rf $(BIN)
	rm -rf $(DOCDIR)
	rm -f nhlight.jar

javadoc:FORCE
	rm -rf $(DOCDIR)
	mkdir -p $(DOCDIR)
	$(JAVADOC)

zip:jar javadoc
	mkdir -p nhlight
	mv -f  $(JARNAME) $(DOCDIR)  nhlight
	cp -f foruser/* nhlight
	cp -fr css/* nhlight
	zip -r nhlight.zip nhlight
	rm -rf nhlight

run:FORCE
	java -cp "$(JUNIVERSALCHARDET)$(DSEP)$(BIN)" nodamushi.hl.NHLight -copy "testcode/$(TESTFILE)"

FORCE:
