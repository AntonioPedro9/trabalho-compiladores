package lumina.symbols;

/**
 * Interface da Tabela de Símbolos (Aula 11 — Parte 2). Define o contrato de gerenciamento de
 * escopos léxicos aninhados, declaração e busca de símbolos sem acoplamento à estrutura de dados
 * concreta.
 */
public interface SymbolTable {

  /** Entra em um bloco, abrindo um novo nível de escopo no topo. */
  void openScope();

  /** Sai do bloco atual, descartando os símbolos declarados no escopo do topo. */
  void closeScope();

  /**
   * Registra um símbolo no escopo atual. Não substitui um símbolo já existente no mesmo escopo:
   * devolve {@code false} e preserva a primeira declaração.
   */
  boolean declare(Symbol s);

  /** Busca um símbolo apenas no escopo do topo; devolve {@code null} se não encontrar. */
  Symbol lookupLocal(String name);

  /**
   * Busca um símbolo do escopo mais interno ao mais externo; devolve {@code null} se não encontrar.
   */
  Symbol lookup(String name);

  /** Retorna a profundidade léxica atual ({@code 0} = escopo global). */
  int level();
}
