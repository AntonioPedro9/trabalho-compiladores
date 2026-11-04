# Lumina — Compilador (Checkpoints 1, 2 e 3)

Projeto do compilador da linguagem **Lumina** (Analisador Léxico, Analisador Sintático LL(1), Árvore de Sintaxe Abstrata e Visitors).

**Alunos:** Antônio Pedro Rodrigues Santos e Guilherme Henrique Ventura

## Pré-requisitos

- **Java JDK 21+** (`javac` e `java` disponíveis no terminal)
- **Make** (opcional)

## Como compilar e executar

### 1. Compilar todo o projeto
```bash
make build
# ou diretamente com javac:
mkdir -p bin && javac --release 21 -d bin $(find src test -name "*.java")
```

### 2. Rodar todas as suítes de teste (Scanner CP1 + Parser CP2 + AST/Visitors CP3)
```bash
make test
# ou diretamente com java:
java -cp bin lumina.TestRunner
```

### 3. Executar o compilador sobre um arquivo `.lum` (exibe Tokens, AST e Código Reimpresso)
```bash
make run FILE=exemplos/exemplo_fatorial.lum
```
