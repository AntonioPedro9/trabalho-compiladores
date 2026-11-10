package lumina.symbols;

/**
 * Categorias semânticas dos símbolos declarados na linguagem Lumina. Distinguem variáveis,
 * parâmetros formais, funções com retorno de valor e procedimentos ({@code void}).
 */
public enum SymbolCategory {
  VARIABLE("variável"),
  PARAMETER("parâmetro"),
  FUNCTION("função"),
  PROCEDURE("procedimento");

  private final String description;

  SymbolCategory(String description) {
    this.description = description;
  }

  public String getDescription() {
    return description;
  }
}
