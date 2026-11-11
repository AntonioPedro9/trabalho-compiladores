package lumina.symbols;

import java.util.Objects;
import lumina.ast.Node;
import lumina.lexer.token.TokenType;

/**
 * Representa uma entrada de declaração na Tabela de Símbolos da linguagem Lumina. Guarda o nome, a
 * categoria, o nó de declaração na AST e o nível de escopo, além de reservar os atributos {@code
 * type} (para a checagem de tipos das Aulas 12–15) e {@code offset} (para a geração de código TAM
 * das Aulas 16–19) sem exigir alteração estrutural nas fases seguintes.
 */
public final class Symbol {

  private final String name;
  private final SymbolCategory category;
  private final Node declarationNode;
  private final int level;

  /**
   * Campo reservado para o Checkpoint 5 (checagem de tipos: {@code i.decl.type}). Exposto também
   * via getter/setter.
   */
  public TokenType type;

  /** Campo reservado para o Checkpoint 6 (deslocamento no frame da máquina TAM). */
  private int offset;

  public Symbol(String name, SymbolCategory category, Node declarationNode, int level) {
    this(name, category, declarationNode, level, null);
  }

  public Symbol(
      String name, SymbolCategory category, Node declarationNode, int level, TokenType type) {
    this.name = Objects.requireNonNull(name, "O nome do símbolo não pode ser nulo.");
    this.category = Objects.requireNonNull(category, "A categoria do símbolo não pode ser nula.");
    this.declarationNode =
        Objects.requireNonNull(declarationNode, "O nó da declaração não pode ser nulo.");
    if (level < 0) {
      throw new IllegalArgumentException("O nível de escopo não pode ser negativo: " + level);
    }
    this.level = level;
    this.type = type;
    this.offset = -1;
  }

  public String getName() {
    return name;
  }

  public SymbolCategory getCategory() {
    return category;
  }

  public Node getDeclarationNode() {
    return declarationNode;
  }

  public int getDeclarationLine() {
    return declarationNode.getLine();
  }

  public int getLevel() {
    return level;
  }

  public TokenType getType() {
    return type;
  }

  public void setType(TokenType type) {
    this.type = type;
  }

  public int getOffset() {
    return offset;
  }

  public void setOffset(int offset) {
    this.offset = offset;
  }

  public String formatReference() {
    return name + "@" + getDeclarationLine();
  }

  @Override
  public String toString() {
    return String.format(
        "%s@%d (%s, nível %d)", name, getDeclarationLine(), category.name(), level);
  }
}
