# Checkpoint 2 — Gramática Final, FIRST/FOLLOW e Verificação LL(1)

**Universidade Federal de Uberlândia (UFU) — Faculdade de Computação (FACOM / BCC)**  
**Disciplina:** Construção de Compiladores  
**Alunos:** Antônio Pedro Rodrigues Santos e Guilherme Henrique Ventura  
**Linguagem:** Lumina

## 1. Gramática Final da Linguagem Lumina (Seção 2.1)

A gramática abaixo utiliza exatamente os terminais definidos na especificação léxica do Checkpoint 1 (`TokenType.java`) e cobre todo o escopo fixo da linguagem (Seção 7): tipos básicos (`int`, `double`, `bool`, `char`, `string`), seleção condicional (`if`/`else`), repetição (`while`), declarações de variáveis, procedimentos e funções (`fun` com tipo de retorno ou `void`, parâmetros, chamadas e `return` com e sem valor) e expressões aritméticas, relacionais e lógicas.

### 1.1 Transformações Aplicadas (Antes e Depois — Aulas 4 e 5)

| Construção | Antes (Com Conflito LL(1)) | Depois (Sem Recursão à Esquerda / Fatorada) |
| :--- | :--- | :--- |
| **Listas** (Recursão à esquerda) | `DeclList ::= DeclList Decl \| ε`<br>`StmtList ::= StmtList Stmt \| ε`<br>`ParamList ::= ParamList "," Param \| Param`<br>`ArgList ::= ArgList "," Expr \| Expr` | `DeclList ::= Decl DeclList \| ε`<br>`StmtList ::= Stmt StmtList \| ε`<br>`ParamList ::= Param ParamListTail`<br>`ParamListTail ::= "," Param ParamListTail \| ε`<br>`ArgList ::= Expr ArgListTail`<br>`ArgListTail ::= "," Expr ArgListTail \| ε` |
| **Expressões Lógicas e de Igualdade** (Recursão à esquerda direta) | `Expr ::= Expr "\|\|" LogicalAnd \| LogicalAnd`<br>`LogicalAnd ::= LogicalAnd "&&" Equality \| Equality`<br>`Equality ::= Equality EqOp Relational \| Relational` | `Expr ::= LogicalAnd ExprTail`<br>`ExprTail ::= "\|\|" LogicalAnd ExprTail \| ε`<br>`LogicalAnd ::= Equality LogicalAndTail`<br>`LogicalAndTail ::= "&&" Equality LogicalAndTail \| ε`<br>`Equality ::= Relational EqualityTail`<br>`EqualityTail ::= EqOp Relational EqualityTail \| ε` |
| **Expressões Relacionais e Aritméticas** (Recursão à esquerda direta) | `Relational ::= Relational RelOp Additive \| Additive`<br>`Additive ::= Additive AddOp Multiplicative \| Multiplicative`<br>`Multiplicative ::= Multiplicative MulOp Unary \| Unary` | `Relational ::= Additive RelationalTail`<br>`RelationalTail ::= RelOp Additive RelationalTail \| ε`<br>`Additive ::= Multiplicative AdditiveTail`<br>`AdditiveTail ::= AddOp Multiplicative AdditiveTail \| ε`<br>`Multiplicative ::= Unary MultiplicativeTail`<br>`MultiplicativeTail ::= MulOp Unary MultiplicativeTail \| ε` |
| **Declaração de Variável** (Prefixo comum `Type IDENTIFIER`) | `VarDecl ::= Type IDENTIFIER ";"`<br>`\| Type IDENTIFIER "=" Expr ";"` | `VarDecl ::= Type IDENTIFIER VarInitOpt ";"`<br>`VarInitOpt ::= "=" Expr \| ε` |
| **Atribuição vs. Chamada** (Prefixo comum `IDENTIFIER` em `Stmt`) | `AssignStmt ::= IDENTIFIER "=" Expr ";"`<br>`CallStmt ::= IDENTIFIER "(" ArgListOpt ")" ";"` | `AssignOrCallStmt ::= IDENTIFIER AssignOrCallTail ";"`<br>`AssignOrCallTail ::= "=" Expr \| "(" ArgListOpt ")"` |
| **Condicional `if`/`else`** (Prefixo comum `"if" "(" Expr ")" Block`) | `IfStmt ::= "if" "(" Expr ")" Block`<br>`\| "if" "(" Expr ")" Block "else" Block` | `IfStmt ::= "if" "(" Expr ")" Block ElseOpt`<br>`ElseOpt ::= "else" Block \| ε` |
| **Identificador vs. Chamada em Expressão** (Prefixo comum `IDENTIFIER` em `Primary`) | `Primary ::= IDENTIFIER`<br>`\| IDENTIFIER "(" ArgListOpt ")" \| ...` | `Primary ::= IDENTIFIER PrimaryTail \| ...`<br>`PrimaryTail ::= "(" ArgListOpt ")" \| ε` |

### 1.2 Gramática Completa em BNF (44 Não-Terminais)

```bnf
Program            ::= DeclList EOF
DeclList           ::= Decl DeclList | ε
Decl               ::= VarDecl | FunctionDecl
VarDecl            ::= Type IDENTIFIER VarInitOpt ";"
VarInitOpt         ::= "=" Expr | ε
FunctionDecl       ::= "fun" ReturnType IDENTIFIER "(" ParamListOpt ")" Block
ReturnType         ::= Type | "void"
Type               ::= "int" | "double" | "bool" | "char" | "string"
ParamListOpt       ::= ParamList | ε
ParamList          ::= Param ParamListTail
ParamListTail      ::= "," Param ParamListTail | ε
Param              ::= Type IDENTIFIER

Block              ::= "{" StmtList "}"
StmtList           ::= Stmt StmtList | ε
Stmt               ::= VarDecl | AssignOrCallStmt | IfStmt | WhileStmt | ReturnStmt
AssignOrCallStmt   ::= IDENTIFIER AssignOrCallTail ";"
AssignOrCallTail   ::= "=" Expr | "(" ArgListOpt ")"
IfStmt             ::= "if" "(" Expr ")" Block ElseOpt
ElseOpt            ::= "else" Block | ε
WhileStmt          ::= "while" "(" Expr ")" Block
ReturnStmt         ::= "return" ExprOpt ";"
ExprOpt            ::= Expr | ε

Expr               ::= LogicalAnd ExprTail
ExprTail           ::= "||" LogicalAnd ExprTail | ε
LogicalAnd         ::= Equality LogicalAndTail
LogicalAndTail     ::= "&&" Equality LogicalAndTail | ε
Equality           ::= Relational EqualityTail
EqualityTail       ::= EqOp Relational EqualityTail | ε
EqOp               ::= "==" | "!="
Relational         ::= Additive RelationalTail
RelationalTail     ::= RelOp Additive RelationalTail | ε
RelOp              ::= "<" | "<=" | ">" | ">="
Additive           ::= Multiplicative AdditiveTail
AdditiveTail       ::= AddOp Multiplicative AdditiveTail | ε
AddOp              ::= "+" | "-"
Multiplicative     ::= Unary MultiplicativeTail
MultiplicativeTail ::= MulOp Unary MultiplicativeTail | ε
MulOp              ::= "*" | "/" | "%"
Unary              ::= "!" Unary | "-" Unary | "+" Unary | Primary
Primary            ::= IDENTIFIER PrimaryTail | INT_LITERAL | DOUBLE_LITERAL
                     | STRING_LITERAL | CHAR_LITERAL | "true" | "false" | "(" Expr ")"
PrimaryTail        ::= "(" ArgListOpt ")" | ε
ArgListOpt         ::= ArgList | ε
ArgList            ::= Expr ArgListTail
ArgListTail        ::= "," Expr ArgListTail | ε
```

## 2. Tabela de Conjuntos FIRST e FOLLOW (Seção 2.2)

| Não-Terminal | FIRST | FOLLOW |
| :--- | :--- | :--- |
| `Program` | { `"int"`, `"double"`, `"bool"`, `"char"`, `"string"`, `"fun"`, `EOF` } | { `EOF` } |
| `DeclList` | { `"int"`, `"double"`, `"bool"`, `"char"`, `"string"`, `"fun"`, `ε` } | { `EOF` } |
| `Decl` | { `"int"`, `"double"`, `"bool"`, `"char"`, `"string"`, `"fun"` } | { `"int"`, `"double"`, `"bool"`, `"char"`, `"string"`, `"fun"`, `EOF` } |
| `VarDecl` | { `"int"`, `"double"`, `"bool"`, `"char"`, `"string"` } | { `"int"`, `"double"`, `"bool"`, `"char"`, `"string"`, `"fun"`, `"if"`, `"while"`, `"return"`, `IDENTIFIER`, `"}"`, `EOF` } |
| `VarInitOpt` | { `"="`, `ε` } | { `";"` } |
| `FunctionDecl` | { `"fun"` } | { `"int"`, `"double"`, `"bool"`, `"char"`, `"string"`, `"fun"`, `EOF` } |
| `ReturnType` | { `"int"`, `"double"`, `"bool"`, `"char"`, `"string"`, `"void"` } | { `IDENTIFIER` } |
| `Type` | { `"int"`, `"double"`, `"bool"`, `"char"`, `"string"` } | { `IDENTIFIER` } |
| `ParamListOpt` | { `"int"`, `"double"`, `"bool"`, `"char"`, `"string"`, `ε` } | { `")"` } |
| `ParamList` | { `"int"`, `"double"`, `"bool"`, `"char"`, `"string"` } | { `")"` } |
| `ParamListTail` | { `","`, `ε` } | { `")"` } |
| `Param` | { `"int"`, `"double"`, `"bool"`, `"char"`, `"string"` } | { `")"`, `","` } |
| `Block` | { `"{"` } | { `"int"`, `"double"`, `"bool"`, `"char"`, `"string"`, `"fun"`, `"if"`, `"else"`, `"while"`, `"return"`, `IDENTIFIER`, `"}"`, `EOF` } |
| `StmtList` | { `"int"`, `"double"`, `"bool"`, `"char"`, `"string"`, `"if"`, `"while"`, `"return"`, `IDENTIFIER`, `ε` } | { `"}"` } |
| `Stmt` | { `"int"`, `"double"`, `"bool"`, `"char"`, `"string"`, `"if"`, `"while"`, `"return"`, `IDENTIFIER` } | { `"int"`, `"double"`, `"bool"`, `"char"`, `"string"`, `"if"`, `"while"`, `"return"`, `IDENTIFIER`, `"}"` } |
| `AssignOrCallStmt` | { `IDENTIFIER` } | { `"int"`, `"double"`, `"bool"`, `"char"`, `"string"`, `"if"`, `"while"`, `"return"`, `IDENTIFIER`, `"}"` } |
| `AssignOrCallTail` | { `"("`, `"="` } | { `";"` } |
| `IfStmt` | { `"if"` } | { `"int"`, `"double"`, `"bool"`, `"char"`, `"string"`, `"if"`, `"while"`, `"return"`, `IDENTIFIER`, `"}"` } |
| `ElseOpt` | { `"else"`, `ε` } | { `"int"`, `"double"`, `"bool"`, `"char"`, `"string"`, `"if"`, `"while"`, `"return"`, `IDENTIFIER`, `"}"` } |
| `WhileStmt` | { `"while"` } | { `"int"`, `"double"`, `"bool"`, `"char"`, `"string"`, `"if"`, `"while"`, `"return"`, `IDENTIFIER`, `"}"` } |
| `ReturnStmt` | { `"return"` } | { `"int"`, `"double"`, `"bool"`, `"char"`, `"string"`, `"if"`, `"while"`, `"return"`, `IDENTIFIER`, `"}"` } |
| `ExprOpt` | { `"true"`, `"false"`, `IDENTIFIER`, `INT_LITERAL`, `DOUBLE_LITERAL`, `STRING_LITERAL`, `CHAR_LITERAL`, `"("`, `"+"`, `"-"`, `"!"`, `ε` } | { `";"` } |
| `Expr` | { `"true"`, `"false"`, `IDENTIFIER`, `INT_LITERAL`, `DOUBLE_LITERAL`, `STRING_LITERAL`, `CHAR_LITERAL`, `"("`, `"+"`, `"-"`, `"!"` } | { `")"`, `";"`, `","` } |
| `ExprTail` | { `"\|\|"`, `ε` } | { `")"`, `";"`, `","` } |
| `LogicalAnd` | { `"true"`, `"false"`, `IDENTIFIER`, `INT_LITERAL`, `DOUBLE_LITERAL`, `STRING_LITERAL`, `CHAR_LITERAL`, `"("`, `"+"`, `"-"`, `"!"` } | { `")"`, `";"`, `","`, `"\|\|"` } |
| `LogicalAndTail` | { `"&&"`, `ε` } | { `")"`, `";"`, `","`, `"\|\|"` } |
| `Equality` | { `"true"`, `"false"`, `IDENTIFIER`, `INT_LITERAL`, `DOUBLE_LITERAL`, `STRING_LITERAL`, `CHAR_LITERAL`, `"("`, `"+"`, `"-"`, `"!"` } | { `")"`, `";"`, `","`, `"\|\|"`, `"&&"` } |
| `EqualityTail` | { `"=="`, `"!="`, `ε` } | { `")"`, `";"`, `","`, `"\|\|"`, `"&&"` } |
| `EqOp` | { `"=="`, `"!="` } | { `"true"`, `"false"`, `IDENTIFIER`, `INT_LITERAL`, `DOUBLE_LITERAL`, `STRING_LITERAL`, `CHAR_LITERAL`, `"("`, `"+"`, `"-"`, `"!"` } |
| `Relational` | { `"true"`, `"false"`, `IDENTIFIER`, `INT_LITERAL`, `DOUBLE_LITERAL`, `STRING_LITERAL`, `CHAR_LITERAL`, `"("`, `"+"`, `"-"`, `"!"` } | { `")"`, `";"`, `","`, `"\|\|"`, `"&&"`, `"=="`, `"!="` } |
| `RelationalTail` | { `"<"`, `"<="`, `">"`, `">="`, `ε` } | { `")"`, `";"`, `","`, `"\|\|"`, `"&&"`, `"=="`, `"!="` } |
| `RelOp` | { `"<"`, `"<="`, `">"`, `">="` } | { `"true"`, `"false"`, `IDENTIFIER`, `INT_LITERAL`, `DOUBLE_LITERAL`, `STRING_LITERAL`, `CHAR_LITERAL`, `"("`, `"+"`, `"-"`, `"!"` } |
| `Additive` | { `"true"`, `"false"`, `IDENTIFIER`, `INT_LITERAL`, `DOUBLE_LITERAL`, `STRING_LITERAL`, `CHAR_LITERAL`, `"("`, `"+"`, `"-"`, `"!"` } | { `")"`, `";"`, `","`, `"\|\|"`, `"&&"`, `"=="`, `"!="`, `"<"`, `"<="`, `">"`, `">="` } |
| `AdditiveTail` | { `"+"`, `"-"`, `ε` } | { `")"`, `";"`, `","`, `"\|\|"`, `"&&"`, `"=="`, `"!="`, `"<"`, `"<="`, `">"`, `">="` } |
| `AddOp` | { `"+"`, `"-"` } | { `"true"`, `"false"`, `IDENTIFIER`, `INT_LITERAL`, `DOUBLE_LITERAL`, `STRING_LITERAL`, `CHAR_LITERAL`, `"("`, `"+"`, `"-"`, `"!"` } |
| `Multiplicative` | { `"true"`, `"false"`, `IDENTIFIER`, `INT_LITERAL`, `DOUBLE_LITERAL`, `STRING_LITERAL`, `CHAR_LITERAL`, `"("`, `"+"`, `"-"`, `"!"` } | { `")"`, `";"`, `","`, `"\|\|"`, `"&&"`, `"=="`, `"!="`, `"<"`, `"<="`, `">"`, `">="`, `"+"`, `"-"` } |
| `MultiplicativeTail` | { `"*"`, `"/"`, `"%"`, `ε` } | { `")"`, `";"`, `","`, `"\|\|"`, `"&&"`, `"=="`, `"!="`, `"<"`, `"<="`, `">"`, `">="`, `"+"`, `"-"` } |
| `MulOp` | { `"*"`, `"/"`, `"%"` } | { `"true"`, `"false"`, `IDENTIFIER`, `INT_LITERAL`, `DOUBLE_LITERAL`, `STRING_LITERAL`, `CHAR_LITERAL`, `"("`, `"+"`, `"-"`, `"!"` } |
| `Unary` | { `"true"`, `"false"`, `IDENTIFIER`, `INT_LITERAL`, `DOUBLE_LITERAL`, `STRING_LITERAL`, `CHAR_LITERAL`, `"("`, `"+"`, `"-"`, `"!"` } | { `")"`, `";"`, `","`, `"\|\|"`, `"&&"`, `"=="`, `"!="`, `"<"`, `"<="`, `">"`, `">="`, `"+"`, `"-"`, `"*"`, `"/"`, `"%"` } |
| `Primary` | { `"true"`, `"false"`, `IDENTIFIER`, `INT_LITERAL`, `DOUBLE_LITERAL`, `STRING_LITERAL`, `CHAR_LITERAL`, `"("` } | { `")"`, `";"`, `","`, `"\|\|"`, `"&&"`, `"=="`, `"!="`, `"<"`, `"<="`, `">"`, `">="`, `"+"`, `"-"`, `"*"`, `"/"`, `"%"` } |
| `PrimaryTail` | { `"("`, `ε` } | { `")"`, `";"`, `","`, `"\|\|"`, `"&&"`, `"=="`, `"!="`, `"<"`, `"<="`, `">"`, `">="`, `"+"`, `"-"`, `"*"`, `"/"`, `"%"` } |
| `ArgListOpt` | { `"true"`, `"false"`, `IDENTIFIER`, `INT_LITERAL`, `DOUBLE_LITERAL`, `STRING_LITERAL`, `CHAR_LITERAL`, `"("`, `"+"`, `"-"`, `"!"`, `ε` } | { `")"` } |
| `ArgList` | { `"true"`, `"false"`, `IDENTIFIER`, `INT_LITERAL`, `DOUBLE_LITERAL`, `STRING_LITERAL`, `CHAR_LITERAL`, `"("`, `"+"`, `"-"`, `"!"` } | { `")"` } |
| `ArgListTail` | { `","`, `ε` } | { `")"` } |

## 3. Verificação Explícita da Condição LL(1) (Seção 2.3)

Verificação das três cláusulas para todo não-terminal `A`:
- **(a)** Alternativas distintas possuem conjuntos `FIRST` disjuntos (`FIRST(αᵢ) ∩ FIRST(αⱼ) = ∅`);
- **(b)** No máximo uma alternativa de `A` deriva `ε`;
- **(c)** Quando `A` deriva `ε`, o `FIRST` das alternativas não-anuláveis é disjunto de `FOLLOW(A)` (`(FIRST(A) − {ε}) ∩ FOLLOW(A) = ∅`).

> **Nota sobre `ElseOpt`:** Como todo corpo de `if` e `else` é obrigatoriamente um `Block` delimitado por chaves (`"{" StmtList "}"`), o token `"else"` jamais pertence a `FOLLOW(ElseOpt)` (`FOLLOW(ElseOpt) = FOLLOW(IfStmt) = {"int", "double", "bool", "char", "string", IDENTIFIER, "if", "while", "return", "}"}`). Portanto, `FIRST("else" Block) ∩ FOLLOW(ElseOpt) = {"else"} ∩ FOLLOW(ElseOpt) = ∅`.

| Não-Terminal | Cláusula (a): FIRST Disjuntos entre Alternativas | Cláusula (b): ≤ 1 Anulável | Cláusula (c): (FIRST − {ε}) ∩ FOLLOW = ∅ |
| :--- | :--- | :--- | :--- |
| `Program` | Produção única (trivialmente disjunto). | Não (0 anuláveis) | N/A (não deriva `ε`) |
| `DeclList` | ∩ = ∅ (`Decl DeclList` → {`"int"`, `"double"`, `"bool"`, `"char"`, `"string"`, `"fun"`}; `ε` → {`ε`}) | Sim (1 alternativa `ε`) | (FIRST − {`ε`}) ∩ FOLLOW = ∅ ✓ |
| `Decl` | ∩ = ∅ (`VarDecl` → {`"int"`, `"double"`, `"bool"`, `"char"`, `"string"`}; `FunctionDecl` → {`"fun"`}) | Não (0 anuláveis) | N/A (não deriva `ε`) |
| `VarDecl` | Produção única (trivialmente disjunto). | Não (0 anuláveis) | N/A (não deriva `ε`) |
| `VarInitOpt` | ∩ = ∅ (`"=" Expr` → {`"="`}; `ε` → {`ε`}) | Sim (1 alternativa `ε`) | (FIRST − {`ε`}) ∩ FOLLOW = ∅ ✓ |
| `FunctionDecl` | Produção única (trivialmente disjunto). | Não (0 anuláveis) | N/A (não deriva `ε`) |
| `ReturnType` | ∩ = ∅ (`Type` → {`"int"`, `"double"`, `"bool"`, `"char"`, `"string"`}; `"void"` → {`"void"`}) | Não (0 anuláveis) | N/A (não deriva `ε`) |
| `Type` | ∩ = ∅ (`"int"` → {`"int"`}; `"double"` → {`"double"`}; `"bool"` → {`"bool"`}; `"char"` → {`"char"`}; `"string"` → {`"string"`}) | Não (0 anuláveis) | N/A (não deriva `ε`) |
| `ParamListOpt` | ∩ = ∅ (`ParamList` → {`"int"`, `"double"`, `"bool"`, `"char"`, `"string"`}; `ε` → {`ε`}) | Sim (1 alternativa `ε`) | (FIRST − {`ε`}) ∩ FOLLOW = ∅ ✓ |
| `ParamList` | Produção única (trivialmente disjunto). | Não (0 anuláveis) | N/A (não deriva `ε`) |
| `ParamListTail` | ∩ = ∅ (`"," Param ParamListTail` → {`","`}; `ε` → {`ε`}) | Sim (1 alternativa `ε`) | (FIRST − {`ε`}) ∩ FOLLOW = ∅ ✓ |
| `Param` | Produção única (trivialmente disjunto). | Não (0 anuláveis) | N/A (não deriva `ε`) |
| `Block` | Produção única (trivialmente disjunto). | Não (0 anuláveis) | N/A (não deriva `ε`) |
| `StmtList` | ∩ = ∅ (`Stmt StmtList` → {`"int"`, `"double"`, `"bool"`, `"char"`, `"string"`, `"if"`, `"while"`, `"return"`, `IDENTIFIER`}; `ε` → {`ε`}) | Sim (1 alternativa `ε`) | (FIRST − {`ε`}) ∩ FOLLOW = ∅ ✓ |
| `Stmt` | ∩ = ∅ (`VarDecl` → {`"int"`, `"double"`, `"bool"`, `"char"`, `"string"`}; `AssignOrCallStmt` → {`IDENTIFIER`}; `IfStmt` → {`"if"`}; `WhileStmt` → {`"while"`}; `ReturnStmt` → {`"return"`}) | Não (0 anuláveis) | N/A (não deriva `ε`) |
| `AssignOrCallStmt` | Produção única (trivialmente disjunto). | Não (0 anuláveis) | N/A (não deriva `ε`) |
| `AssignOrCallTail` | ∩ = ∅ (`"=" Expr` → {`"="`}; `"(" ArgListOpt ")"` → {`"("`}) | Não (0 anuláveis) | N/A (não deriva `ε`) |
| `IfStmt` | Produção única (trivialmente disjunto). | Não (0 anuláveis) | N/A (não deriva `ε`) |
| `ElseOpt` | ∩ = ∅ (`"else" Block` → {`"else"`}; `ε` → {`ε`}) | Sim (1 alternativa `ε`) | (FIRST − {`ε`}) ∩ FOLLOW = ∅ ✓ |
| `WhileStmt` | Produção única (trivialmente disjunto). | Não (0 anuláveis) | N/A (não deriva `ε`) |
| `ReturnStmt` | Produção única (trivialmente disjunto). | Não (0 anuláveis) | N/A (não deriva `ε`) |
| `ExprOpt` | ∩ = ∅ (`Expr` → {`"true"`, `"false"`, `IDENTIFIER`, `INT_LITERAL`, `DOUBLE_LITERAL`, `STRING_LITERAL`, `CHAR_LITERAL`, `"("`, `"+"`, `"-"`, `"!"`}; `ε` → {`ε`}) | Sim (1 alternativa `ε`) | (FIRST − {`ε`}) ∩ FOLLOW = ∅ ✓ |
| `Expr` | Produção única (trivialmente disjunto). | Não (0 anuláveis) | N/A (não deriva `ε`) |
| `ExprTail` | ∩ = ∅ (`"\|\|" LogicalAnd ExprTail` → {`"\|\|"`}; `ε` → {`ε`}) | Sim (1 alternativa `ε`) | (FIRST − {`ε`}) ∩ FOLLOW = ∅ ✓ |
| `LogicalAnd` | Produção única (trivialmente disjunto). | Não (0 anuláveis) | N/A (não deriva `ε`) |
| `LogicalAndTail` | ∩ = ∅ (`"&&" Equality LogicalAndTail` → {`"&&"`}; `ε` → {`ε`}) | Sim (1 alternativa `ε`) | (FIRST − {`ε`}) ∩ FOLLOW = ∅ ✓ |
| `Equality` | Produção única (trivialmente disjunto). | Não (0 anuláveis) | N/A (não deriva `ε`) |
| `EqualityTail` | ∩ = ∅ (`EqOp Relational EqualityTail` → {`"=="`, `"!="`}; `ε` → {`ε`}) | Sim (1 alternativa `ε`) | (FIRST − {`ε`}) ∩ FOLLOW = ∅ ✓ |
| `EqOp` | ∩ = ∅ (`"=="` → {`"=="`}; `"!="` → { `"!="`}) | Não (0 anuláveis) | N/A (não deriva `ε`) |
| `Relational` | Produção única (trivialmente disjunto). | Não (0 anuláveis) | N/A (não deriva `ε`) |
| `RelationalTail` | ∩ = ∅ (`RelOp Additive RelationalTail` → {`"<"`, `"<="`, `">"`, `">="`}; `ε` → {`ε`}) | Sim (1 alternativa `ε`) | (FIRST − {`ε`}) ∩ FOLLOW = ∅ ✓ |
| `RelOp` | ∩ = ∅ (`"<"` → {`"<"`}; `"<="` → {`"<="`}; `">"` → {`">"`}; `">="` → {`">="`}) | Não (0 anuláveis) | N/A (não deriva `ε`) |
| `Additive` | Produção única (trivialmente disjunto). | Não (0 anuláveis) | N/A (não deriva `ε`) |
| `AdditiveTail` | ∩ = ∅ (`AddOp Multiplicative AdditiveTail` → {`"+"`, `"-"`}; `ε` → {`ε`}) | Sim (1 alternativa `ε`) | (FIRST − {`ε`}) ∩ FOLLOW = ∅ ✓ |
| `AddOp` | ∩ = ∅ (`"+"` → {`"+"`}; `"-"` → {`"-"`}) | Não (0 anuláveis) | N/A (não deriva `ε`) |
| `Multiplicative` | Produção única (trivialmente disjunto). | Não (0 anuláveis) | N/A (não deriva `ε`) |
| `MultiplicativeTail` | ∩ = ∅ (`MulOp Unary MultiplicativeTail` → {`"*"`, `"/"`, `"%"`}; `ε` → {`ε`}) | Sim (1 alternativa `ε`) | (FIRST − {`ε`}) ∩ FOLLOW = ∅ ✓ |
| `MulOp` | ∩ = ∅ (`"*"` → {`"*"`}; `"/"` → {`"/"`}; `"%"` → {`"%"`}) | Não (0 anuláveis) | N/A (não deriva `ε`) |
| `Unary` | ∩ = ∅ (`"!" Unary` → {`"!"`}; `"-" Unary` → {`"-"`}; `"+" Unary` → {`"+"`}; `Primary` → {`"true"`, `"false"`, `IDENTIFIER`, `INT_LITERAL`, `DOUBLE_LITERAL`, `STRING_LITERAL`, `CHAR_LITERAL`, `"("`}) | Não (0 anuláveis) | N/A (não deriva `ε`) |
| `Primary` | ∩ = ∅ (`IDENTIFIER PrimaryTail` → {`IDENTIFIER`}; `INT_LITERAL` → {`INT_LITERAL`}; `DOUBLE_LITERAL` → {`DOUBLE_LITERAL`}; `STRING_LITERAL` → {`STRING_LITERAL`}; `CHAR_LITERAL` → {`CHAR_LITERAL`}; `"true"` → {`"true"`}; `"false"` → {`"false"`}; `"(" Expr ")"` → {`"("`}) | Não (0 anuláveis) | N/A (não deriva `ε`) |
| `PrimaryTail` | ∩ = ∅ (`"(" ArgListOpt ")"` → {`"("`}; `ε` → {`ε`}) | Sim (1 alternativa `ε`) | (FIRST − {`ε`}) ∩ FOLLOW = ∅ ✓ |
| `ArgListOpt` | ∩ = ∅ (`ArgList` → {`"true"`, `"false"`, `IDENTIFIER`, `INT_LITERAL`, `DOUBLE_LITERAL`, `STRING_LITERAL`, `CHAR_LITERAL`, `"("`, `"+"`, `"-"`, `"!"`}; `ε` → {`ε`}) | Sim (1 alternativa `ε`) | (FIRST − {`ε`}) ∩ FOLLOW = ∅ ✓ |
| `ArgList` | Produção única (trivialmente disjunto). | Não (0 anuláveis) | N/A (não deriva `ε`) |
| `ArgListTail` | ∩ = ∅ (`"," Expr ArgListTail` → {`","`}; `ε` → {`ε`}) | Sim (1 alternativa `ε`) | (FIRST − {`ε`}) ∩ FOLLOW = ∅ ✓ |
