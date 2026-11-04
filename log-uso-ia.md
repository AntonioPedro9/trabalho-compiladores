# Checkpoint 3

**Universidade Federal de Uberlândia (UFU) — Faculdade de Computação (FACOM / BCC)**  
**Disciplina:** Construção de Compiladores  
**Alunos:** Antônio Pedro Rodrigues Santos e Guilherme Henrique Ventura  
**Linguagem:** Lumina

## Onde Usamos IA e Quais Alterações Fizemos

- **Confecção da nova suíte de testes:** Utilizamos a IA para agilizar a escrita do código repetitivo dos métodos de teste (leitura dos arquivos em `test/esperado/`, asserções de comparação textual e estrutura dos testes de precedência, associatividade, parênteses redundantes, *round-trip* e regressão do CP2). Revisamos todos os casos de teste e validamos as entradas e saídas esperadas manualmente de acordo com as regras da nossa gramática e do nosso `AstPrinterVisitor`.
- **Formatação do documento .md:** A partir das decisões de projeto tomadas pela dupla (quais não-terminais viram nós, quais viram atributos e quais são descartados nos casos de precedência, recursão à esquerda e fatoração), utilizamos a IA para organizar o texto e formatar as tabelas Markdown e a representação visual em árvore do programa de exemplo. Todo o conteúdo técnico foi conferido para refletir exatamente as classes implementadas no código-fonte.
