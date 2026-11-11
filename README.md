# Lumina — Compilador (Checkpoints 1, 2, 3 e 4)

Projeto do compilador da linguagem **Lumina** (Analisador Léxico, Analisador Sintático LL(1), Árvore de Sintaxe Abstrata, Visitors, Tabela de Símbolos e Passada de Identificação).

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

### 2. Rodar todas as suítes de teste (Scanner CP1 + Parser CP2 + AST/Visitors CP3 + SymbolTable e Identificação CP4)

```bash
make test
# ou diretamente com java:
java -cp bin lumina.TestRunner
```

### 3. Executar o compilador sobre um arquivo `.lum` (exibe Tokens, AST, Código Reimpresso e AST Decorada/Identificação)

make run FILE=exemplos/exemplo_fatorial.lum
make run FILE=test/programas/cp4_sombreamento_valido.lum
make run FILE=test/programas/cp4_redeclaracao.lum
make run FILE=test/programas/cp4_nao_declarado.lum

```

```
