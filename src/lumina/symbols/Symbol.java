package lumina.symbols;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import lumina.ast.Node;
import lumina.lexer.token.TokenType;
import lumina.types.LuminaType;

/**
 * Representa uma entrada de declaração na Tabela de Símbolos, armazenando nome, categoria, nó de
 * origem, nível de escopo, tipo e assinatura de parâmetros.
 */
public final class Symbol {

  private final String name;
  private final SymbolCategory category;
  private final Node declarationNode;
  private final int level;
  private final List<LuminaType> parameterTypes;

  /** Tipo sintático associado ao símbolo declarado. */
  public TokenType type;

  /** Deslocamento reservado para alocação no frame da máquina TAM. */
  private int offset;

  public Symbol(String name, SymbolCategory category, Node declarationNode, int level) {
    this(name, category, declarationNode, level, null, List.of());
  }

  public Symbol(
      String name, SymbolCategory category, Node declarationNode, int level, TokenType type) {
    this(name, category, declarationNode, level, type, List.of());
  }

  public Symbol(
      String name,
      SymbolCategory category,
      Node declarationNode,
      int level,
      TokenType type,
      List<LuminaType> parameterTypes) {
    this.name = Objects.requireNonNull(name, "O nome do símbolo não pode ser nulo.");
    this.category = Objects.requireNonNull(category, "A categoria do símbolo não pode ser nula.");
    this.declarationNode =
        Objects.requireNonNull(declarationNode, "O nó da declaração não pode ser nulo.");
    Objects.requireNonNull(parameterTypes, "A lista de tipos de parâmetros não pode ser nula.");
    if (level < 0) {
      throw new IllegalArgumentException("O nível de escopo não pode ser negativo: " + level);
    }
    this.level = level;
    this.type = type;
    this.parameterTypes = List.copyOf(parameterTypes);
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

  public LuminaType getSemanticType() {
    return LuminaType.fromTokenType(type);
  }

  public List<LuminaType> getParameterTypes() {
    return Collections.unmodifiableList(parameterTypes);
  }

  public boolean isCallable() {
    return category == SymbolCategory.FUNCTION || category == SymbolCategory.PROCEDURE;
  }

  public boolean isProcedure() {
    return category == SymbolCategory.PROCEDURE;
  }

  public boolean isAssignableStorage() {
    return category == SymbolCategory.VARIABLE || category == SymbolCategory.PARAMETER;
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
