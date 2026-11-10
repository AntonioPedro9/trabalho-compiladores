# Checkpoint 4 — Projeto da Tabela de Símbolos e Passada de Identificação

**Universidade Federal de Uberlândia (UFU) — Faculdade de Computação (FACOM / BCC)**
**Disciplina:** Construção de Compiladores
**Alunos:** Antônio Pedro Rodrigues Santos e Guilherme Henrique Ventura
**Linguagem:** Lumina

---

## 1. Desenho do Ambiente e Resolução de Nomes no Papel (Seção 2.1)

### 1.1 Programa de Referência (`test/programas/cp4_sombreamento_valido.lum`)

O programa abaixo exercita todos os elementos exigidos: uma variável global (`taxa` na linha 1), uma função com parâmetro (`calcular(int base)` na linha 3), dois níveis de blocos aninhados dentro da função (`while` no nível 2 e `if` no nível 3), um nome sombreado (`taxa` redeclarado na linha 6 dentro do bloco do `while`, ocultando `taxa@1`) e um uso após o fechamento do bloco (`taxa` na linha 13, que volta a resolver para `taxa@1`).

```lumina
01: int taxa = 10;
02:
03: fun int calcular(int base) {
04:     int total = base + taxa;
05:     while (base > 0) {
06:         int taxa = 2;
07:         if (total > 5) {
08:             int bonus = taxa + base;
09:             total = total + bonus;
10:         }
11:         base = base - taxa;
12:     }
13:     return total + taxa;
14: }
```

### 1.2 Evolução do Ambiente Linha a Linha e Resolução dos Usos (`nome@linha`)

Notação utilizada para o ambiente (pilha de escopos do nível `0` até o nível atual):

- Cada nível `L` é representado por `Nível L: { nome@linha ( CATEGORIA ) }`.
- Cada uso de identificador é anotado com `nome@linhaDecl`, indicando o símbolo retornado por `lookup(nome)` naquele ponto.

| Linha  | Construção / Evento            | Usos Resolvidos na Linha                                                                                                    | Evento de Escopo / Declaração                                                                                                              | Estado da Tabela de Símbolos (Ambiente Ativo após a Linha)                                                                                                             |
| :----: | :----------------------------- | :-------------------------------------------------------------------------------------------------------------------------- | :----------------------------------------------------------------------------------------------------------------------------------------- | :--------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **01** | `int taxa = 10;`               | _(nenhum)_                                                                                                                  | `declare(taxa@1, VARIABLE, nível 0)`                                                                                                       | **Nível 0:** `{ taxa@1 (VAR) }`                                                                                                                                        |
| **03** | `fun int calcular(int base) {` | _(nenhum)_                                                                                                                  | 1. `declare(calcular@3, FUNCTION, nível 0)`; 2. `openScope()` $\rightarrow$ entra no **Nível 1**; 3. `declare(base@3, PARAMETER, nível 1)` | **Nível 0:** `{ taxa@1 (VAR), calcular@3 (FUN) }`; **Nível 1:** `{ base@3 (PARAM) }`                                                                                   |
| **04** | `int total = base + taxa;`     | `base` $\rightarrow$ **`base@3`**; `taxa` $\rightarrow$ **`taxa@1`**                                                        | _(avalia `base + taxa` antes)_; `declare(total@4, VARIABLE, nível 1)`                                                                      | **Nível 0:** `{ taxa@1 (VAR), calcular@3 (FUN) }`; **Nível 1:** `{ base@3 (PARAM), total@4 (VAR) }`                                                                    |
| **05** | `while (base > 0) {`           | `base` $\rightarrow$ **`base@3`**                                                                                           | `openScope()` $\rightarrow$ entra no **Nível 2** (bloco do `while`)                                                                        | **Nível 0:** `{ taxa@1 (VAR), calcular@3 (FUN) }`; **Nível 1:** `{ base@3 (PARAM), total@4 (VAR) }`; **Nível 2:** `{ }`                                                |
| **06** | `int taxa = 2;`                | _(nenhum)_                                                                                                                  | `declare(taxa@6, VARIABLE, nível 2)` _(sombreia `taxa@1` do nível 0)_                                                                      | **Nível 0:** `{ taxa@1 (VAR), calcular@3 (FUN) }`; **Nível 1:** `{ base@3 (PARAM), total@4 (VAR) }`; **Nível 2:** `{ taxa@6 (VAR) }`                                   |
| **07** | `if (total > 5) {`             | `total` $\rightarrow$ **`total@4`**                                                                                         | `openScope()` $\rightarrow$ entra no **Nível 3** (bloco do `if`)                                                                           | **Nível 0:** `{ taxa@1 (VAR), calcular@3 (FUN) }`; **Nível 1:** `{ base@3 (PARAM), total@4 (VAR) }`; **Nível 2:** `{ taxa@6 (VAR) }`; **Nível 3:** `{ }`               |
| **08** | `int bonus = taxa + base;`     | `taxa` $\rightarrow$ **`taxa@6`** _(sombreado)_; `base` $\rightarrow$ **`base@3`**                                          | _(avalia `taxa + base` antes)_; `declare(bonus@8, VARIABLE, nível 3)`                                                                      | **Nível 0:** `{ taxa@1 (VAR), calcular@3 (FUN) }`; **Nível 1:** `{ base@3 (PARAM), total@4 (VAR) }`; **Nível 2:** `{ taxa@6 (VAR) }`; **Nível 3:** `{ bonus@8 (VAR) }` |
| **09** | `total = total + bonus;`       | alvo `total` $\rightarrow$ **`total@4`**; `total` $\rightarrow$ **`total@4`**; `bonus` $\rightarrow$ **`bonus@8`**          | _(apenas consultas `lookup`)_                                                                                                              | **Nível 0:** `{ taxa@1 (VAR), calcular@3 (FUN) }`; **Nível 1:** `{ base@3 (PARAM), total@4 (VAR) }`; **Nível 2:** `{ taxa@6 (VAR) }`; **Nível 3:** `{ bonus@8 (VAR) }` |
| **10** | `}` _(fim do `if`)_            | _(nenhum)_                                                                                                                  | `closeScope()` $\rightarrow$ descarta **Nível 3** (`bonus@8` deixa de existir)                                                             | **Nível 0:** `{ taxa@1 (VAR), calcular@3 (FUN) }`; **Nível 1:** `{ base@3 (PARAM), total@4 (VAR) }`; **Nível 2:** `{ taxa@6 (VAR) }`                                   |
| **11** | `base = base - taxa;`          | alvo `base` $\rightarrow$ **`base@3`**; `base` $\rightarrow$ **`base@3`**; `taxa` $\rightarrow$ **`taxa@6`** _(sombreado)_  | _(apenas consultas `lookup`)_                                                                                                              | **Nível 0:** `{ taxa@1 (VAR), calcular@3 (FUN) }`; **Nível 1:** `{ base@3 (PARAM), total@4 (VAR) }`; **Nível 2:** `{ taxa@6 (VAR) }`                                   |
| **12** | `}` _(fim do `while`)_         | _(nenhum)_                                                                                                                  | `closeScope()` $\rightarrow$ descarta **Nível 2** (`taxa@6` sai; **`taxa@1` é restaurado**)                                                | **Nível 0:** `{ taxa@1 (VAR), calcular@3 (FUN) }`; **Nível 1:** `{ base@3 (PARAM), total@4 (VAR) }`                                                                    |
| **13** | `return total + taxa;`         | `total` $\rightarrow$ **`total@4`**; `taxa` $\rightarrow$ **`taxa@1`** _(pós-bloco)_                                        | _(apenas consultas `lookup`)_                                                                                                              | **Nível 0:** `{ taxa@1 (VAR), calcular@3 (FUN) }`; **Nível 1:** `{ base@3 (PARAM), total@4 (VAR) }`                                                                    |
| **14** | `}` _(fim de `calcular`)_      | _(nenhum)_                                                                                                                  | `closeScope()` $\rightarrow$ descarta **Nível 1** (`base@3` e `total@4` saem)                                                              | **Nível 0:** `{ taxa@1 (VAR), calcular@3 (FUN) }`                                                                                                                      |

### 1.3 Saída Esperada da Árvore Decorada (`test/esperado/cp4_sombreamento_valido.txt`)

```text
Program
  VarDecl(int taxa)
    IntLit(10)
  FunctionDecl(int calcular)
    Param(int base)
    Block
      VarDecl(int total)
        Bin(+)
          Id(base@3)
          Id(taxa@1)
      While
        Bin(>)
          Id(base@3)
          IntLit(0)
        Block
          VarDecl(int taxa)
            IntLit(2)
          If
            Bin(>)
              Id(total@4)
              IntLit(5)
            Block
              VarDecl(int bonus)
                Bin(+)
                  Id(taxa@6)
                  Id(base@3)
              Assign(total@4)
                Bin(+)
                  Id(total@4)
                  Id(bonus@8)
          Assign(base@3)
            Bin(-)
              Id(base@3)
              Id(taxa@6)
      Return
        Bin(+)
          Id(total@4)
          Id(taxa@1)
```

---

## 2. O que a Classe `Symbol` Guarda e Por Quê (Seção 2.2)

A classe `Symbol` (`lumina.symbols.Symbol`) centraliza todas as propriedades semânticas de uma declaração e é referenciada pelo campo `decl` dos nós de uso (`IdNode`, `AssignNode`, `CallExprNode` e `CallStmtNode`):

| Campo em `Symbol`                  | Tipo             | O que Guarda                                                                                                       | Fase que Consulta e Motivo                                                                                                                                                                                                                                                                                                                                                 |
| :--------------------------------- | :--------------- | :----------------------------------------------------------------------------------------------------------------- | :------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `name`                             | `String`         | O lexema exato do identificador declarado.                                                                         | **Passada de Identificação (CP4) e Relatórios de Erro:** chave de inserção e busca na `SymbolTable` (`declare`, `lookupLocal`, `lookup`) e formatação de erros e referências `nome@linha`.                                                                                                                                                                                 |
| `category`                         | `SymbolCategory` | Categoria da declaração: `VARIABLE`, `PARAMETER`, `FUNCTION` (retorno não-`void`) ou `PROCEDURE` (retorno `void`). | **Checagem de Tipos (CP5 / Aulas 12–15) e Geração de Código (CP6 / Aulas 16–19):** distingue se um identificador pode receber atribuição (`VARIABLE`/`PARAMETER`), ser chamado em expressão (`FUNCTION`) ou ser chamado como comando (`PROCEDURE`/`FUNCTION`).                                                                                                             |
| `declarationNode`                  | `Node`           | Ponteiro para o nó da AST que declarou o símbolo (`VarDeclNode`, `ParamNode` ou `FunctionDeclNode`).               | **Passada de Identificação (CP4) e Checagem de Tipos (CP5):** no CP4, fornece a linha exata da primeira declaração (`declarationNode.getLine()`) para a mensagem `"já declarado neste escopo (linha Y)"` e para a anotação `x@Y`; no CP5, permite inspecionar a lista de `ParamNode`s de um `FunctionDeclNode` para validar quantidade e tipos dos argumentos em chamadas. |
| `level`                            | `int`            | Profundidade léxica do escopo em que o símbolo foi declarado (`0` = global, `1` = função, `2+` = blocos internos). | **Passada de Identificação (CP4) e Geração de Código TAM (CP6 / Aulas 16–19):** registra em qual nível o nome vive e, na máquina abstrata TAM, determina o endereçamento base (`SB` para nível `0` vs. `LB` para variáveis locais e parâmetros no nível `1+`).                                                                                                             |
| `type` _(reservado Aulas 12–15)_   | `TokenType`      | Tipo estático associado ao símbolo (`INT`, `DOUBLE`, `BOOL`, `CHAR`, `STRING`, `VOID`).                            | **Checagem de Tipos (CP5):** reservado para que o verificador de tipos leia diretamente `node.getDecl().type` (ou `node.getDecl().getType()`) na AST decorada sem alterar a assinatura ou a estrutura de `Symbol` ou dos nós da AST.                                                                                                                                       |
| `offset` _(reservado Aulas 16–19)_ | `int`            | Deslocamento relativo (_displacement_) da variável ou parâmetro no quadro de ativação (_stack frame_) da TAM.      | **Alocação de Memória e Geração de Código TAM (CP6):** preenchido pelo gerador de código para emitir instruções `LOAD`/`STORE` com `offset[reg]` sem modificar a classe `Symbol`.                                                                                                                                                                                          |

> **Observação sobre extensões da linguagem:** A gramática da linguagem Lumina não possui constantes (`const`) nem passagem de parâmetro por referência (`var`/`ref`); todos os parâmetros são passados por valor (`PARAMETER`) e funções sem retorno (`fun void`) são categorizadas como `PROCEDURE`.

---

## 3. Escolha da Implementação da `SymbolTable` (Seção 3)

Escolhemos a implementação por **Pilha de Mapas** (`ScopedSymbolTable`, utilizando `List<Map<String, Symbol>>` onde o índice `i` da lista corresponde exatamente ao nível `i`, iniciando com o escopo global aberto no nível `0`).
Essa escolha modela diretamente o bloco ativo no topo da pilha: `openScope()` empilha um novo `LinkedHashMap` em $O(1)$, `closeScope()` remove o mapa do topo em $O(1)$ descartando de uma só vez todos os nomes locais, `lookupLocal(name)` consulta apenas o mapa do topo em $O(1)$ e `lookup(name)` percorre do índice `level()` até `0` em $O(\text{profundidade})$, que em programas reais é pequeno ($\le 5$).
**Comportamento de `closeScope()` sem escopo de bloco aberto:** se `closeScope()` for invocado quando a tabela já está no nível global (`level() == 0`), a implementação lança uma `IllegalStateException` explícita e documentada (`"Não há escopo de bloco aberto para fechar (nível global 0 não pode ser fechado)."`), nunca lançando `NullPointerException`.

---

## 4. As Cinco Decisões de Projeto de Escopo (Seção 2.3)

### Decisão 1 — Sombreamento (_Shadowing_) e Redeclaração

- **Escolha:** Permitir sombreamento (_shadowing_) quando a nova declaração ocorre em um escopo mais interno (`level` maior que o da declaração anterior), ocultando temporariamente o símbolo externo até o `closeScope()` do bloco interno. Proibir qualquer redeclaração no mesmo escopo (`lookupLocal` não nulo): `declare` devolve `false`, a passada reporta erro semântico, mantém a primeira declaração e descarta a segunda.
- **Alternativa descartada:** Proibir sombreamento em qualquer nível (tratando como erro declarar uma variável local com o mesmo nome de uma variável global ou de um bloco externo).
- **Motivo:** Segue a regra padrão de linguagens estruturadas em blocos da família C/Algol vistas na Aula 11, garantindo modularidade (adicionar uma variável global não quebra variáveis auxiliares internas de funções ou blocos `while`/`if`) ao mesmo tempo em que captura erros reais de duplicação acidental dentro do mesmo bloco.
- **Programa de teste (`test/programas/cp4_sombreamento_valido.lum` e `test/programas/cp4_redeclaracao.lum`):**

  ```lumina
  int valor = 100;
  fun void testar() {
      int valor = 50;
      int valor = 60;
      if (valor > 0) {
          int valor = 10;
      }
  }
  ```

  - Na linha 3, `valor@3` (nível 1) sombreia validamente `valor@1` (nível 0).
  - Na linha 4, `int valor = 60;` no mesmo nível 1 gera `linha 4: 'valor' já declarado neste escopo (linha 3)` e preserva `valor@3`.
  - Na linha 6, `valor@6` (nível 2, dentro do `if`) sombreia validamente `valor@3`.

---

### Decisão 2 — Declarar Antes de Usar (_Declare-Before-Use_)

- **Escolha:** Obrigatório declarar todo identificador textual e cronologicamente antes do seu primeiro uso (análise em passada única na ordem do texto). Em declarações de variáveis com inicializador (`int x = expr;`), a expressão `expr` é visitada **antes** de inserir `x` na tabela de símbolos.
- **Alternativa descartada:** Permitir o uso de variáveis ou funções antes de sua declaração no mesmo bloco/arquivo por meio de uma passada prévia de coleta de declarações (_hoisting_).
- **Motivo:** Elimina ambiguidades de leitura de variáveis antes da sua inicialização, simplifica a análise contextual para uma única travessia `Visitor<Void>` sobre a AST e impede que `int a = a;` referencie a própria variável ainda não declarada (resolvendo para um `a` externo se existir, ou reportando `'a' não declarado`).
- **Programa de teste (`test/programas/cp4_decisao2_declarar_antes_de_usar.lum`):**

  ```lumina
  fun void testarOrdem() {
      int a = b + 1;
      int b = 10;
      int c = c;
  }
  ```

  - Reporta `linha 2: 'b' não declarado` (pois `b` só é declarado na linha 3) e `linha 4: 'c' não declarado` (pois `c` ainda não está declarado enquanto seu inicializador é avaliado).

---

### Decisão 3 — Funções Mutuamente Recursivas

- **Escolha:** Funções mutuamente recursivas **não são permitidas**; apenas a **recursão direta** (`f` chamando a si mesma) e chamadas a funções declaradas anteriormente no arquivo são aceitas.
- **Alternativa descartada:** Realizar uma passada preliminar sobre os filhos de `ProgramNode` para registrar todas as assinaturas de funções antes de visitar os seus corpos, permitindo recursão mútua sem protótipos.
- **Motivo:** Mantém total coerência com a Decisão 2 (passada estritamente única de cima para baixo, como em C sem protótipos) e com o protocolo da Aula 11 (Seção 4: _"o nome é declarado no escopo externo antes do openScope (a recursão funciona)"_), que resolve naturalmente a recursão direta sem introduzir múltiplas passadas sobre a AST.
- **Programa de teste (`test/programas/cp4_decisao3_recursao_mutua.lum`):**

  ```lumina
  fun int par(int n) {
      if (n == 0) {
          return 1;
      }
      return impar(n - 1);
  }

  fun int impar(int n) {
      if (n == 0) {
          return 0;
      }
      return par(n - 1);
  }
  ```

  - Na linha 5, a chamada `impar(n - 1)` reporta `linha 5: 'impar' não declarado`, enquanto na linha 12 a chamada `par(n - 1)` resolve normalmente para `par@1`.

---

### Decisão 4 — Escopo dos Parâmetros e do Corpo da Função

- **Escolha:** Os parâmetros formais (`ParamNode`) e as declarações diretas do bloco principal (`BlockNode`) da função compartilham **o mesmo escopo (nível 1)**, como em C e Java. Portanto, declarar uma variável local no topo da função com o mesmo nome de um parâmetro (`fun void f(int a) { int a = 1; }`) é erro de redeclaração no nível 1. Blocos internos de comandos (`if`, `else`, `while`) abrem novos escopos (nível 2, 3, ...), onde o sombreamento do parâmetro é permitido.
- **Alternativa descartada:** Abrir um escopo nível 1 exclusivo para os parâmetros e um escopo nível 2 separado para o `BlockNode` do corpo da função (como em Pascal/Algol, onde uma variável local do corpo pode ocultar imediatamente um parâmetro formal).
- **Motivo:** Impede o erro clássico de programação em que um parâmetro recebido pela função é acidentalmente ocultado logo na primeira linha do corpo, inutilizando o argumento passado pelo chamador.
- **Programa de teste (`test/programas/cp4_decisao4_parametro_corpo.lum`):**

  ```lumina
  fun int dobrar(int a) {
      int a = 10;
      if (a > 0) {
          int a = 20;
          return a;
      }
      return a;
  }
  ```

  - Na linha 2, `int a = 10;` reporta `linha 2: 'a' já declarado neste escopo (linha 1)` e é descartada, mantendo `a@1`.
  - Na linha 4 (dentro do bloco do `if`, nível 2), `int a = 20;` sombreia validamente `a@1`, fazendo a linha 5 resolver para `a@4` e a linha 7 resolver para `a@1`.

---

### Decisão 5 — Espaços de Nomes (_Namespaces_)

- **Escolha:** Variáveis, parâmetros, funções e procedimentos compartilham **um único espaço de nomes** (_single namespace_), exatamente como em C.
- **Alternativa descartada:** Manter espaços de nomes separados para variáveis e para funções/procedimentos (onde uma variável `x` e uma função `x()` poderiam coexistir no mesmo escopo sem colisão).
- **Motivo:** A interface `SymbolTable` da Aula 11 define `lookup(String name)` e `lookupLocal(String name)` indexados exclusivamente pelo identificador textual (`String`), sem chave de categoria. Além disso, o espaço de nomes único prepara o compilador para detectar no Checkpoint 5 erros como tentar chamar uma variável (`x(1)`) ou atribuir valor a uma função (`f = 10`), pois ambos os usos consultam a mesma tabela e inspecionam `decl.getCategory()`.
- **Programa de teste (`test/programas/cp4_decisao5_espaco_nomes.lum`):**

  ```lumina
  int conflito = 1;

  fun void conflito() {
      return;
  }

  fun void testarSombreamentoDeFuncao() {
      int conflito = 2;
      conflito = conflito + 1;
  }
  ```

  - Na linha 3, `fun void conflito()` no nível global colide com `int conflito = 1;` (nível 0) e reporta `linha 3: 'conflito' já declarado neste escopo (linha 1)`.
  - Na linha 8 (nível 1), `int conflito = 2;` sombreia validamente o símbolo global `conflito@1`, e a linha 9 resolve ambos os usos para `conflito@8`.
