
.PHONY: all checkUsage sourceTarget winTarget siteTarget exeFiles

SOURCE_TARGET := opensess-source-$(RELEASE).zip
WIN_TARGET    := opensess-win-$(RELEASE).zip
SITE_TARGET   := opensess-website-$(RELEASE).tar.gz

SOURCES := "$(wildcard openSess/*.java)"
CLASSES := "$(wildcard openSess/*.class)"
HELPFILES := "$(wildcard help/*)"
SITEFILES := "$(wildcard website/*)"

all: checkUsage sourceTarget winTarget siteTarget
	@echo ""
	@echo "Finished preparing release $(RELEASE)."

sourceTarget: 
	@echo "Creating $(SOURCE_TARGET)..."
	@rm -f $(SOURCE_TARGET)
	@zip $(SOURCE_TARGET) README-SOURCE LICENSE RELEASE BUILD openSess/*.java help/*

winTarget: exeFiles
	@echo "Creating $(WIN_TARGET)..."
	@exeJ -cfg opensess.cfg
	@mv -f using_javaw.exe OpenSess.exe
	@mv -f using_java.exe OpenSessConsole.exe
	@rm -f $(WIN_TARGET)
	@zip $(WIN_TARGET) README-WIN LICENSE RELEASE INSTALL openSess/*.class help/* OpenSess.exe OpenSessConsole.exe

siteTarget: 
	@echo "Creating $(SITE_TARGET)..."
	@rm -f $(SITE_TARGET)
	@cd website ; tar cvf ../$(SITE_TARGET:.gz=) *
	@gzip $(SITE_TARGET:.gz=)

checkUsage:
ifndef RELEASE
	@echo ""
	@echo "***** Variable RELEASE is not set."
	@echo ""
	@echo "Usage: make RELEASE=\"<releaseId>\""
	@echo ""
	@false
endif