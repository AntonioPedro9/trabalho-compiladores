# Checkpoint 3 — Projeto da Árvore de Sintaxe Abstrata (AST) e Visitors

**Universidade Federal de Uberlândia (UFU) — Faculdade de Computação (FACOM / BCC)**  
**Disciplina:** Construção de Compiladores  
**Alunos:** Antônio Pedro Rodrigues Santos e Guilherme Henrique Ventura  
**Linguagem:** Lumina

## 1. Hierarquia de Nós da AST (Seção 2.1)

Seguindo os princípios da Aula 7 (padrão *Interpreter* combinado com *Visitor*), a hierarquia de nós da linguagem Lumina foi projetada com **um tipo de nó por categoria sintática relevante para as fases seguintes** (Tabela de Símbolos, Análise Semântica e Geração de Código TAM), e não um tipo de nó por não-terminal da gramática LL(1).

### 1.1 Classes Abstratas Base

| Classe Abstrata | Superclasse | Papel na Hierarquia e Preparação para Fases Futuras |
| :--- | :--- | :--- |
| `Node` | `Object` | Raiz de todos os nós da AST. Guarda `SourcePosition position` (linha e coluna do 1º token) e declara `public abstract <T> T accept(Visitor<T> v)`. |
| `StmtNode` | `Node` | Base abstrata para todos os comandos executáveis dentro de blocos (`BlockNode`, `VarDeclNode`, `AssignNode`, `CallStmtNode`, `IfNode`, `WhileNode`, `ReturnNode`). |
| `DeclNode` | `StmtNode` | Base abstrata para declarações (`VarDeclNode` e `FunctionDeclNode`). Estende `StmtNode` pois declarações de variáveis ocorrem tanto no escopo global quanto dentro de blocos. |
| `ExprNode` | `Node` | Base abstrata para todas as expressões. Nas aulas de Análise Semântica, receberá o atributo de tipo inferido/verificado (`LuminaType evaluatedType`) sem alterar a forma da árvore. |

### 1.2 Tabela Completa dos 20 Nós Concretos

| Nome do Nó | Superclasse | Atributos (com Tipo) | Produção(ões) da Gramática do CP2 que o Geram |
| :--- | :--- | :--- | :--- |
| `ProgramNode` | `Node` | `List<DeclNode> declarations` | `Program ::= DeclList EOF` |
| `VarDeclNode` | `DeclNode` | `TypeNode type`, `String name`, `ExprNode initializer` (opcional/`null`) | `VarDecl ::= Type IDENTIFIER VarInitOpt ";"` |
| `FunctionDeclNode` | `DeclNode` | `TypeNode returnType`, `String name`, `List<ParamNode> params`, `BlockNode body` | `FunctionDecl ::= "fun" ReturnType IDENTIFIER "(" ParamListOpt ")" Block` |
| `ParamNode` | `Node` | `TypeNode type`, `String name` | `Param ::= Type IDENTIFIER` |
| `TypeNode` | `Node` | `TokenType typeToken` (`INT`, `DOUBLE`, `BOOL`, `CHAR`, `STRING`, `VOID`) | `ReturnType ::= Type \| "void"`<br>`Type ::= "int" \| "double" \| "bool" \| "char" \| "string"` |
| `BlockNode` | `StmtNode` | `List<StmtNode> statements` | `Block ::= "{" StmtList "}"` |
| `AssignNode` | `StmtNode` | `String targetName`, `ExprNode value` | `AssignOrCallStmt ::= IDENTIFIER AssignOrCallTail ";"` (quando `AssignOrCallTail ::= "=" Expr`) |
| `CallStmtNode` | `StmtNode` | `String callee`, `List<ExprNode> arguments` | `AssignOrCallStmt ::= IDENTIFIER AssignOrCallTail ";"` (quando `AssignOrCallTail ::= "(" ArgListOpt ")"`) |
| `IfNode` | `StmtNode` | `ExprNode condition`, `BlockNode thenBranch`, `BlockNode elseBranch` (opcional/`null`) | `IfStmt ::= "if" "(" Expr ")" Block ElseOpt` |
| `WhileNode` | `StmtNode` | `ExprNode condition`, `BlockNode body` | `WhileStmt ::= "while" "(" Expr ")" Block` |
| `ReturnNode` | `StmtNode` | `ExprNode expression` (opcional/`null`) | `ReturnStmt ::= "return" ExprOpt ";"` |
| `BinNode` | `ExprNode` | `TokenType operator`, `ExprNode left`, `ExprNode right` | `ExprTail ::= "\|\|" LogicalAnd ExprTail`<br>`LogicalAndTail ::= "&&" Equality LogicalAndTail`<br>`EqualityTail ::= EqOp Relational EqualityTail`<br>`RelationalTail ::= RelOp Additive RelationalTail`<br>`AdditiveTail ::= AddOp Multiplicative AdditiveTail`<br>`MultiplicativeTail ::= MulOp Unary MultiplicativeTail` |
| `UnaryNode` | `ExprNode` | `TokenType operator` (`BANG`, `MINUS`, `PLUS`), `ExprNode operand` | `Unary ::= "!" Unary \| "-" Unary \| "+" Unary` |
| `CallExprNode` | `ExprNode` | `String callee`, `List<ExprNode> arguments` | `Primary ::= IDENTIFIER PrimaryTail` (quando `PrimaryTail ::= "(" ArgListOpt ")"`) |
| `IdNode` | `ExprNode` | `String name` *(futuro ponteiro `Symbol symbol` na Tabela de Símbolos)* | `Primary ::= IDENTIFIER PrimaryTail` (quando `PrimaryTail ::= ε`) |
| `IntLitNode` | `ExprNode` | `int value`, `String rawLexeme` | `Primary ::= INT_LITERAL` |
| `DoubleLitNode` | `ExprNode` | `double value`, `String rawLexeme` | `Primary ::= DOUBLE_LITERAL` |
| `StringLitNode` | `ExprNode` | `String rawLexeme` | `Primary ::= STRING_LITERAL` |
| `CharLitNode` | `ExprNode` | `String rawLexeme` | `Primary ::= CHAR_LITERAL` |
| `BoolLitNode` | `ExprNode` | `boolean value` | `Primary ::= "true" \| "false"` |

### 1.3 Evolução para Tabela de Símbolos e Análise Semântica

- **Tabela de Símbolos:** `IdNode`, `AssignNode`, `CallExprNode` e `CallStmtNode` podem receber um campo `private Symbol symbol` (com `getSymbol()`/`setSymbol()`) resolvido por um `SymbolTableVisitor`, mantendo inalterados os construtores e os filhos de cada nó.
- **Análise Semântica:** `ExprNode` centraliza todas as expressões; basta acrescentar `private LuminaType evaluatedType` em `ExprNode` para que um `TypeCheckerVisitor` anote e valide os tipos de cada subexpressão sem modificar nenhuma classe de nó concreta nem os visitors anteriores.

## 2. Mapeamento da Gramática para a Árvore (Seção 2.2)

Na construção da AST, muitos não-terminais da gramática LL(1) e todos os terminais puramente delimitadores (`"("`, `")"`, `"{"`, `"}"`, `","`, `";"`, `EOF`) são descartados após guiarem o parser. Os três casos clássicos vistos na Aula 7 aparecem explicitamente identificados na tabela a seguir:

- **Caso (a) — Não-terminais criados para codificar precedência de operadores:** `Expr`, `LogicalAnd`, `Equality`, `Relational`, `Additive`, `Multiplicative`, `Unary` (no ramo `Primary`) e `Primary` (no ramo `"(" Expr ")"`).
- **Caso (b) — Não-terminais criados para eliminar recursão à esquerda:** `DeclList`, `ParamListTail`, `StmtList`, `ArgListTail` e as 6 caudas de expressões binárias (`ExprTail`, `LogicalAndTail`, `EqualityTail`, `RelationalTail`, `AdditiveTail`, `MultiplicativeTail`).
- **Caso (c) — Não-terminais criados pela fatoração à esquerda (e opcionais):** `VarInitOpt`, `ParamListOpt`, `AssignOrCallTail`, `ElseOpt`, `ExprOpt`, `PrimaryTail` e `ArgListOpt`.

| Não-Terminal (CP2) | Destino na AST (Nó, Atributo ou Descarte) | Categoria / Justificativa em Uma Frase |
| :--- | :--- | :--- |
| `Program` | Vira o nó raiz `ProgramNode`. | Representa a unidade completa de compilação do programa. |
| `DeclList` | Não vira nó; popula `List<DeclNode> declarations` de `ProgramNode`. | **Caso (b) Recursão à esquerda:** não-terminal auxiliar criado apenas para iterar sobre declarações globais sem recursão à esquerda. |
| `Decl` | Não vira nó próprio; repassa o `VarDeclNode` ou `FunctionDeclNode` filho. | Produção de encaminhamento entre categorias de declaração que compartilham a superclasse `DeclNode`. |
| `VarDecl` | Vira `VarDeclNode`. | Representa a declaração de variável global ou local na linguagem. |
| `VarInitOpt` | Vira o atributo `ExprNode initializer` (ou `null`) de `VarDeclNode`. | **Caso (c) Fatoração à esquerda:** criado para fatorar declarações de variáveis com e sem inicialização (`=`). |
| `FunctionDecl` | Vira `FunctionDeclNode`. | Representa a declaração completa de função ou procedimento. |
| `ReturnType` | Vira o atributo `TypeNode returnType` de `FunctionDeclNode`. | Unifica tipos primitivos e `void` em um único `TypeNode`. |
| `Type` | Vira `TypeNode`. | Representa os 5 tipos primitivos (`int`, `double`, `bool`, `char`, `string`). |
| `ParamListOpt` | Vira o atributo `List<ParamNode> params` de `FunctionDeclNode`. | **Caso (c) Fatoração à esquerda:** representa a presença opcional de parâmetros formais entre `"("` e `")"`. |
| `ParamList` | Não vira nó; constrói a lista `List<ParamNode>` para `FunctionDeclNode`. | **Caso (b) Recursão à esquerda:** cabeçalho da lista de parâmetros separada por vírgulas. |
| `ParamListTail` | Não vira nó; acumula elementos em `List<ParamNode>`. | **Caso (b) Recursão à esquerda:** cauda criada para eliminar a recursão à esquerda na lista de parâmetros. |
| `Param` | Vira `ParamNode`. | Representa cada parâmetro formal (`tipo` + `nome`) de uma função. |
| `Block` | Vira `BlockNode`. | Representa um escopo de comandos delimitado por chaves (`"{"` e `"}"` são descartados). |
| `StmtList` | Não vira nó; popula `List<StmtNode> statements` de `BlockNode`. | **Caso (b) Recursão à esquerda:** cauda recursiva à direita usada apenas para coletar a sequência de comandos do bloco. |
| `Stmt` | Não vira nó próprio; devolve o `StmtNode` específico do comando reconhecido. | Regra de despacho entre as cinco categorias de comando (`VarDecl`, `AssignOrCallStmt`, `IfStmt`, `WhileStmt`, `ReturnStmt`). |
| `AssignOrCallStmt` | Vira `AssignNode` ou `CallStmtNode` (definido por `AssignOrCallTail`). | Combina o identificador inicial com a cauda fatorada e descarta o ponto e vírgula final (`";"`). |
| `AssignOrCallTail` | Decide a criação de `AssignNode` (`"="`) ou `CallStmtNode` (`"("`). | **Caso (c) Fatoração à esquerda:** criado para resolver o prefixo comum `IDENTIFIER` entre atribuição e chamada de procedimento. |
| `IfStmt` | Vira `IfNode`. | Representa o comando condicional com sua condição e blocos `then`/`else`. |
| `ElseOpt` | Vira o atributo `BlockNode elseBranch` (ou `null`) de `IfNode`. | **Caso (c) Fatoração à esquerda:** criado para fatorar os comandos `if` com e sem cláusula `else`. |
| `WhileStmt` | Vira `WhileNode`. | Representa o laço de repetição condicional. |
| `ReturnStmt` | Vira `ReturnNode`. | Representa a instrução de retorno de função ou procedimento. |
| `ExprOpt` | Vira o atributo `ExprNode expression` (ou `null`) de `ReturnNode`. | **Caso (c) Fatoração à esquerda:** diferencia `return expr;` de `return;` vazio. |
| `Expr` | Não vira nó quando não há `\|\|`; repassa subárvore ou `BinNode(\|\|)`. | **Caso (a) Precedência:** nível 1 da hierarquia de expressões (disjunção lógica); a precedência já fica implícita na topologia da AST. |
| `ExprTail` | Vira `BinNode(PIPE_PIPE)` associativo à esquerda ou devolve o operando herdado. | **Caso (b) Recursão à esquerda:** recebe o operando esquerdo herdado e monta `BinNode("||", left, right)` quando não é vazio. |
| `LogicalAnd` | Não vira nó quando não há `&&`; repassa subárvore ou `BinNode(&&)`. | **Caso (a) Precedência:** nível 2 de precedência (conjunção lógica), descartado em cadeias unitárias. |
| `LogicalAndTail` | Vira `BinNode(AMPERSAND_AMPERSAND)` ou devolve o operando herdado. | **Caso (b) Recursão à esquerda:** elimina a recursão à esquerda do operador `&&` mantendo associatividade à esquerda. |
| `Equality` | Não vira nó quando não há `==`/`!=`; repassa subárvore. | **Caso (a) Precedência:** nível 3 de precedência (igualdade/diferença), desnecessário como nó isolado na AST. |
| `EqualityTail` | Vira `BinNode(== \| !=)` ou devolve o operando herdado. | **Caso (b) Recursão à esquerda:** acumula comparações de igualdade associadas à esquerda. |
| `EqOp` | Vira o atributo `TokenType operator` de `BinNode`. | Apenas seleciona o token do operador (`EQUAL_EQUAL` ou `BANG_EQUAL`) para o `BinNode`. |
| `Relational` | Não vira nó quando não há `<`, `<=`, `>`, `>=`; repassa subárvore. | **Caso (a) Precedência:** nível 4 de precedência (comparações relacionais), absorvido pela topologia da árvore. |
| `RelationalTail` | Vira `BinNode(< \| <= \| > \| >=)` ou devolve o operando herdado. | **Caso (b) Recursão à esquerda:** constrói nós binários relacionais com associatividade à esquerda. |
| `RelOp` | Vira o atributo `TokenType operator` de `BinNode`. | Fornece o tipo do token relacional ao `BinNode` correspondente. |
| `Additive` | Não vira nó quando não há `+` ou `-`; repassa subárvore. | **Caso (a) Precedência:** nível 5 de precedência (soma e subtração, equivalente ao `E` da Aula 7). |
| `AdditiveTail` | Vira `BinNode(+ \| -)` ou devolve o operando herdado. | **Caso (b) Recursão à esquerda:** equivalente ao `E'` da Aula 7; monta `(a - b) - c` associando à esquerda. |
| `AddOp` | Vira o atributo `TokenType operator` de `BinNode`. | Fornece o operador `+` (`PLUS`) ou `-` (`MINUS`) ao `BinNode`. |
| `Multiplicative` | Não vira nó quando não há `*`, `/` ou `%`; repassa subárvore. | **Caso (a) Precedência:** nível 6 de precedência (multiplicação, divisão e resto, equivalente ao `T` da Aula 7). |
| `MultiplicativeTail` | Vira `BinNode(* \| / \| %)` ou devolve o operando herdado. | **Caso (b) Recursão à esquerda:** equivalente ao `T'` da Aula 7; monta operações multiplicativas associadas à esquerda. |
| `MulOp` | Vira o atributo `TokenType operator` de `BinNode`. | Fornece o operador `*`, `/` ou `%` ao `BinNode`. |
| `Unary` | Vira `UnaryNode` (se precedido por `!`, `-`, `+`) ou repassa `Primary`. | **Caso (a) Precedência:** nível 7 de precedência; no ramo `Unary ::= Primary` não cria nó intermediário. |
| `Primary` | Vira `IntLitNode`, `DoubleLitNode`, `StringLitNode`, `CharLitNode`, `BoolLitNode`, `IdNode`/`CallExprNode`, ou repassa `Expr` entre `(` `)`. | **Caso (a) Precedência:** equivalente ao `F` da Aula 7; no ramo `"(" Expr ")"` descarta os parênteses e devolve diretamente a subárvore de `Expr`. |
| `PrimaryTail` | Decide entre `IdNode` (quando `ε`) e `CallExprNode` (quando `"(" ArgListOpt ")"`). | **Caso (c) Fatoração à esquerda:** criado para fatorar o prefixo `IDENTIFIER` entre variável simples e chamada de função em expressão. |
| `ArgListOpt` | Vira o atributo `List<ExprNode> arguments` de `CallStmtNode` ou `CallExprNode`. | **Caso (c) Fatoração à esquerda:** representa a lista opcional de argumentos reais entre parênteses. |
| `ArgList` | Não vira nó; constrói `List<ExprNode>` com os argumentos da chamada. | **Caso (b) Recursão à esquerda:** cabeçalho da lista de expressões de argumento separadas por vírgula. |
| `ArgListTail` | Não vira nó; adiciona os demais `ExprNode` à lista de argumentos. | **Caso (b) Recursão à esquerda:** cauda recursiva à direita que descarta as vírgulas separadoras e acumula argumentos. |

## 3. Registro de Posição (`SourcePosition`) nos Nós (Seção 2.3)

Como o `Token` do Checkpoint 1 armazena um objeto imutável `SourcePosition(int line, int column)` (baseado em 1), a classe abstrata `Node` recebe obrigatoriamente `SourcePosition position` em seu construtor:

- Em declarações e comandos iniciados por palavra-chave (`FunctionDeclNode`, `IfNode`, `WhileNode`, `ReturnNode`), a posição salva é a do token da palavra-chave (`fun`, `if`, `while`, `return`).
- Em declarações de variáveis e parâmetros (`VarDeclNode`, `ParamNode`), a posição é a do primeiro token do tipo (`TypeNode`).
- Em `BlockNode`, a posição é a do token `"{"`.
- Em `AssignNode`, `CallStmtNode`, `CallExprNode` e `IdNode`, a posição é a do token `IDENTIFIER`.
- Em `BinNode`, a posição herdada é a do início do operando esquerdo (`left.getPosition()`), enquanto em `UnaryNode` é a do operador prefixo (`!`, `-` ou `+`).

## 4. Formato Determinístico da Impressora de Árvore (`AstPrinterVisitor` — Seção 3.2.1)

O visitor `AstPrinterVisitor` percorre a árvore em pré-ordem utilizando exclusivamente *double dispatch* (`node.accept(this)`), imprimindo **um nó por linha** com **2 espaços de indentação por nível de profundidade**:

- `ProgramNode`: `Program` (seguido das declarações filhas no nível +1)
- `VarDeclNode`: `VarDecl(<tipo> <nome>)` (seguido do inicializador no nível +1, se houver)
- `FunctionDeclNode`: `FunctionDecl(<tipoRetorno> <nome>)` (seguido de cada `Param` e do `Block` no nível +1)
- `ParamNode`: `Param(<tipo> <nome>)`
- `TypeNode`: `Type(<tipo>)`
- `BlockNode`: `Block` (seguido dos comandos filhos no nível +1)
- `AssignNode`: `Assign(<identificador>)` (seguido da expressão atribuída no nível +1)
- `CallStmtNode`: `CallStmt(<nomeFuncao>)` (seguido dos argumentos no nível +1)
- `IfNode`: `If` (seguido da condição, do `Block` *then* e, se existir, do `Block` *else* no nível +1)
- `WhileNode`: `While` (seguido da condição e do `Block` do corpo no nível +1)
- `ReturnNode`: `Return` (seguido da expressão retornada no nível +1, se existir)
- `BinNode`: `Bin(<simboloOperador>)` (seguido do filho esquerdo e do filho direito no nível +1)
- `UnaryNode`: `Unary(<simboloOperador>)` (seguido do operando no nível +1)
- `CallExprNode`: `CallExpr(<nomeFuncao>)` (seguido dos argumentos no nível +1)
- `IdNode`: `Id(<nome>)`
- Literais: `IntLit(<lexema>)`, `DoubleLit(<lexema>)`, `StringLit(<lexema>)`, `CharLit(<lexema>)`, `BoolLit(<true|false>)`

> **Nota sobre o segundo Visitor (`SourceReprinterVisitor`):** A adição do reimpressor de código-fonte canônico não exigiu alterar nenhuma classe de nó da hierarquia, pois todos os lexemas, operadores e subárvores já estavam preservados nos 20 nós concretos desenhados na Parte 1.

## 5. Árvore Desenhada para Programa Curto e Pedaços Descartados da Árvore de Derivação (Seção 2.4)

### 5.1 Programa Curto de Exemplo (com função, seleção `if/else` e repetição `while`)

```lumina
fun int contarPassos(int n) {
    int passos = 0;
    while (n > 1) {
        if (n == 2) {
            return passos;
        } else {
            n = n - 1;
        }
        passos = passos + 1;
    }
    return passos;
}
```

### 5.2 Desenho da AST (29 Nós — mínimo exigido: 15 nós)

```text
[1] ProgramNode (1:1)
 └── [2] FunctionDeclNode("int", "contarPassos") (1:1)
      ├── [3] TypeNode(int) (1:5)
      ├── [4] ParamNode("n") (1:22)
      │    └── [5] TypeNode(int) (1:22)
      └── [6] BlockNode (1:29)
           ├── [7] VarDeclNode("passos") (2:5)
           │    ├── [8] TypeNode(int) (2:5)
           │    └── [9] IntLitNode(0) (2:18)
           ├── [10] WhileNode (3:5)
           │    ├── [11] BinNode(">") (3:12)
           │    │    ├── [12] IdNode("n") (3:12)
           │    │    └── [13] IntLitNode(1) (3:16)
           │    └── [14] BlockNode (3:19)
           │         ├── [15] IfNode (4:9)
           │         │    ├── [16] BinNode("==") (4:13)
           │         │    │    ├── [17] IdNode("n") (4:13)
           │         │    │    └── [18] IntLitNode(2) (4:18)
           │         │    ├── [19] BlockNode [then] (4:21)
           │         │    │    └── [20] ReturnNode (5:13)
           │         │    │         └── [21] IdNode("passos") (5:20)
           │         │    └── [22] BlockNode [else] (6:16)
           │         │         └── [23] AssignNode("n") (7:13)
           │         │              └── [24] BinNode("-") (7:17)
           │         │                   ├── [25] IdNode("n") (7:17)
           │         │                   └── [26] IntLitNode(1) (7:21)
           │         └── [27] AssignNode("passos") (9:9)
           │              └── [28] BinNode("+") (9:18)
           │                   ├── [29] IdNode("passos") (9:18)
           │                   └── [30] IntLitNode(1) (9:27)
           └── [31] ReturnNode (11:5)
                └── [32] IdNode("passos") (11:12)
```

### 5.3 Três Pedaços da Árvore de Derivação que a AST Descartou

1. **Cadeia vertical de não-terminais de precedência e caudas vazias (`ε`) para um literal simples:**
   - Na árvore de derivação, o literal `0` em `int passos = 0;` passa por 14 passos de derivação:
     `Expr → LogicalAnd ExprTail(ε) → Equality LogicalAndTail(ε) → Relational EqualityTail(ε) → Additive RelationalTail(ε) → Multiplicative AdditiveTail(ε) → Unary MultiplicativeTail(ε) → Primary → INT_LITERAL(0)`.
   - Na AST, toda essa cadeia vertical de 7 níveis de precedência e 6 caudas anuláveis (`*Tail → ε`) foi colapsada em **um único nó folha**: `[9] IntLitNode(0)`.
2. **Não-terminais de fatoração à esquerda (`AssignOrCallTail`, `PrimaryTail` e `ElseOpt`):**
   - Na árvore de derivação da linha `n = n - 1;`, o comando deriva `Stmt → AssignOrCallStmt → IDENTIFIER("n") AssignOrCallTail ";"`, onde `AssignOrCallTail → "=" Expr`, e o operando esquerdo de `n - 1` deriva `Primary → IDENTIFIER("n") PrimaryTail`, com `PrimaryTail → ε`.
   - Na AST, tanto `AssignOrCallTail` quanto `PrimaryTail` desapareceram completamente: o comando virou diretamente `[23] AssignNode("n")` e o operando virou `[25] IdNode("n")`. Da mesma forma, `ElseOpt → "else" Block` não gerou um nó `ElseOptNode`, tornando-se apenas a referência `elseBranch` dentro de `[15] IfNode`.
3. **Não-terminais recursivos de listas (`DeclList`, `StmtList`, `ParamListOpt`, `ParamListTail`) e pontuação sintática:**
   - Na árvore de derivação, o bloco do `while` (`{ if (...) { ... } passos = passos + 1; }`) exige a cadeia aninhada `Block → "{" StmtList "}"`, onde `StmtList → Stmt StmtList → Stmt StmtList(ε)`, além de terminais delimitadores `"("`, `")"`, `"{"`, `"}"` e `";"`.
   - Na AST, todas as produções intermediárias de `StmtList` e todos os tokens de pontuação (`fun`, `(`, `)`, `{`, `}`, `;`, `=`) foram descartados, restando apenas a lista plana `List<StmtNode>` dentro de `[14] BlockNode`.
