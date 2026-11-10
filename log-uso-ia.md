# Registro Histórico de Uso de IA

**Universidade Federal de Uberlândia (UFU) — Faculdade de Computação (FACOM / BCC)**  
**Disciplina:** Construção de Compiladores  
**Alunos:** Antônio Pedro Rodrigues Santos e Guilherme Henrique Ventura  
**Linguagem:** Lumina

---

## Checkpoint 3

### Onde Usamos IA e Quais Alterações Fizemos

- **Confecção da nova suíte de testes:** Utilizamos a IA para agilizar a escrita do código repetitivo dos métodos de teste (leitura dos arquivos em `test/esperado/`, asserções de comparação textual e estrutura dos testes de precedência, associatividade, parênteses redundantes, *round-trip* e regressão do CP2). Revisamos todos os casos de teste e validamos as entradas e saídas esperadas manualmente de acordo com as regras da nossa gramática e do nosso `AstPrinterVisitor`.
- **Formatação do documento .md:** A partir das decisões de projeto tomadas pela dupla (quais não-terminais viram nós, quais viram atributos e quais são descartados nos casos de precedência, recursão à esquerda e fatoração), utilizamos a IA para organizar o texto e formatar as tabelas Markdown e a representação visual em árvore do programa de exemplo. Todo o conteúdo técnico foi conferido para refletir exatamente as classes implementadas no código-fonte.

---

## Checkpoint 4

### Onde Usamos IA e Quais Alterações Fizemos

- **Alinhamento e documentação das 5 decisões de escopo (`escopo-design.md`):** A dupla definiu as regras semânticas de escopo da linguagem Lumina:
  1. **Sombreamento (*Shadowing*):** Permitido entre escopos de níveis diferentes (`level` interno maior que o externo), restaurando a declaração externa após o `closeScope()`; redeclarar no mesmo nível é sempre erro semântico (`declare` devolve `false` e preserva a 1ª declaração).
  2. **Declarar antes de usar:** Obrigatório em passada única na ordem do texto; em declarações com inicialização (`int x = expr;`), a expressão `expr` é avaliada antes de registrar `x` na tabela de símbolos.
  3. **Funções mutuamente recursivas:** Não permitidas (apenas recursão direta `f -> f`, garantida pelo registro do cabeçalho da função no escopo externo antes do `openScope()` do seu corpo).
  4. **Parâmetros e corpo da função:** Compartilham o mesmo escopo (nível 1, como em C e Java), tornando erro redeclarar um parâmetro no topo da função, enquanto blocos internos (`if`, `else`, `while`) abrem níveis $\ge 2$.
  5. **Espaços de nomes (*Namespaces*):** Espaço de nomes único para variáveis, parâmetros, funções e procedimentos (como em C).
  Utilizamos a IA para organizar o documento `escopo-design.md` e formatar a tabela de rastreamento linha a linha do ambiente e resolução de usos (`nome@linha`), removendo tags HTML inline (`<br>`) para conformidade com o linter Markdown (`MD033`).
- **Implementação da Tabela de Símbolos e Passada de Identificação (`lumina.symbols` e `IdentificationVisitor`):** Utilizamos a IA como par de programação para estruturar a implementação por Pilha de Mapas (`ScopedSymbolTable`), a classe `Symbol` (com os campos `type` e `offset` já reservados para os Checkpoints 5 e 6/TAM), a decoração com o campo `decl` nos nós de uso (`IdNode`, `AssignNode`, `CallExprNode`, `CallStmtNode`) e o `IdentificationVisitor` (`Visitor<Void>` sem `instanceof` nem `switch`). Revisamos o comportamento de borda (como `closeScope()` no nível global lançando `IllegalStateException` explicada em vez de `NullPointerException` e preservação da saída do `AstPrinterVisitor` quando `decl == null` para não quebrar a regressão do CP3).
- **Confecção das suítes de testes (`SymbolTableTest` e `IdentificationTest`) e casos em `test/programas/` e `test/esperado/`:** Utilizamos a IA para gerar o código repetitivo de asserções dos 33 testes unitários da `SymbolTable` isolada e dos 41 testes da passada de identificação (cobrindo os 3 programas obrigatórios comparados com `test/esperado/`, as 5 decisões de projeto, múltiplos erros em ordem, procedimento recursivo e invisibilidade pós-bloco). Todas as saídas esperadas em `test/esperado/` e números de linha foram conferidos manualmente.
