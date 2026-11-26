# Lumina - Compilador (Checkpoints 1 a 5)

Projeto do compilador da linguagem **Lumina** (Analisador Léxico, Analisador Sintático LL(1), Árvore de Sintaxe Abstrata, Visitors, Tabela de Símbolos, Passada de Identificação e Verificador de Tipos).

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

### 2. Rodar as suítes de teste (CP1 a CP5)

```bash
make test
# ou diretamente com java:
java -cp bin lumina.TestRunner
```

### 3. Executar o compilador sobre um programa

```bash
make run FILE=exemplos/exemplo_fatorial.lum
make run FILE=test/programas/cp4_sombreamento_valido.lum
```

## Representação dos Tipos e Localização das Regras (CP5)

- **Representação dos tipos (`src/lumina/types/`):**
  - `LuminaType`: enumeração com os tipos primitivos (`INT`, `DOUBLE`, `BOOL`, `CHAR`, `STRING`), `VOID` para procedimentos e `ERROR` (`<erro>`) para evitar erros em cascata. A compatibilidade de atribuição e promoção implícita (`int` para `double`) fica em `LuminaType.isAssignableFrom`. Na máquina TAM, `int`, `bool` e `char` ocupam 1 palavra; `double` usa ponto fixo escalado por 100 em 1 palavra (com conversão emitida pelo gerador quando um filho `INT` aparece em contexto `DOUBLE`); e `string` ocupa 1 palavra na pilha contendo o endereço do bloco `[comprimento, caracteres...]` no heap.
  - `ExprNode` (`src/lumina/ast/ExprNode.java`): guarda o `LuminaType` calculado em cada expressão (`getType()` / `setType()`).
  - `Symbol` (`src/lumina/symbols/Symbol.java`): guarda o tipo declarado (`getSemanticType()`) e a lista de tipos dos parâmetros (`getParameterTypes()`) preenchida pelo `IdentificationVisitor`.
- **Onde ficam as regras:**
  - `OperatorTypeTable`: define os pares aceitos e o tipo resultante para cada operador aritmético, relacional, de igualdade, lógico e unário.
  - `TypeCheckerVisitor` (`src/lumina/visitor/TypeCheckerVisitor.java`): aplica as regras de tipagem sobre a AST via `Visitor<LuminaType>` em passada única (literais, identificadores, expressões, declarações, atribuições, `if`, `while`, `return`, chamadas e verificação de `return` em todos os caminhos de execução).
  - `TypeCheckContext`, `TypeError` e `TypeWarning`: controlam a pilha de funções ativas, o conjunto conservador de variáveis inicializadas e os registros imutáveis de erro e aviso (`OK` quando sem diagnósticos).
