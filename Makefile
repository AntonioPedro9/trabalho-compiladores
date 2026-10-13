JAVAC ?= javac --release 21
JAVA ?= java
BIN = bin
SRCS = $(shell find src test -name "*.java")
FILE ?= exemplos/exemplo_fatorial.lum
FORMATTER_JAR = .tools/google-java-format.jar

.PHONY: all build test run format clean

all: build

build:
	@echo "Iniciando build..."
	@mkdir -p $(BIN)
	@$(JAVAC) -d $(BIN) $(SRCS)
	@echo "Build concluído com sucesso!"

test: build
	@echo "Iniciando testes..."
	@$(JAVA) -cp $(BIN) lumina.TestRunner
	@echo "Testes concluídos com sucesso!"

run: build
	@echo "Iniciando execução..."
	@$(JAVA) -cp $(BIN) lumina.Main $(FILE) || true
	@echo "Execução concluída com sucesso!"

format:
	@echo "Formatando arquivos..."
	@$(JAVA) -jar $(FORMATTER_JAR) --replace $(SRCS)
	@echo "Formatação concluída com sucesso!"

clean:
	@rm -rf $(BIN)
	@echo "Limpeza concluída com sucesso!"
