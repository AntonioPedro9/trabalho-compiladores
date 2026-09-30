package lumina.common.source;

/**
 * Coordenada imutável (linha e coluna) no código-fonte, indexada a partir de 1. Utilizada
 * transversalmente pelo compilador (tokens, nós da AST e diagnósticos de erro).
 *
 * @param line número da linha indexado a partir de 1
 * @param column número da coluna indexado a partir de 1
 */
public record SourcePosition(int line, int column) implements Comparable<SourcePosition> {

  /**
   * Valida as coordenadas de linha e coluna da posição no código-fonte.
   *
   * @param line número da linha
   * @param column número da coluna
   */
  public SourcePosition {
    if (line < 1 || column < 1) {
      throw new IllegalArgumentException("Linha e coluna devem ser maiores ou iguais a 1.");
    }
  }

  /**
   * Retorna o número da linha.
   *
   * @return linha baseada em 1
   */
  public int getLine() {
    return line;
  }

  /**
   * Retorna o número da coluna.
   *
   * @return coluna baseada em 1
   */
  public int getColumn() {
    return column;
  }

  /**
   * Compara esta coordenada com outra para fins de ordenação posicional.
   *
   * @param other outra posição a ser comparada
   * @return valor negativo se anterior, zero se igual, positivo se posterior
   */
  @Override
  public int compareTo(SourcePosition other) {
    if (other == null) {
      return 1;
    }
    int lineComparison = Integer.compare(this.line, other.line);
    if (lineComparison != 0) {
      return lineComparison;
    }
    return Integer.compare(this.column, other.column);
  }

  /**
   * Retorna a representação textual formatada da coordenada.
   *
   * @return string no formato [linha:coluna]
   */
  @Override
  public String toString() {
    return String.format("[%d:%d]", line, column);
  }
}
