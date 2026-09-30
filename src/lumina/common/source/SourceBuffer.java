package lumina.common.source;

import java.util.Objects;

/**
 * Gerencia o cursor de leitura sobre o código fonte, rastreando coordenadas de linha e coluna.
 * Oferece operações de inspeção (peek) e avanço controlado (advance).
 */
public final class SourceBuffer {

  public static final char END_OF_SOURCE = '\0';

  private final String source;
  private final int length;

  private int cursor;
  private int line;
  private int column;

  /**
   * Constrói um SourceBuffer para o código-fonte especificado.
   *
   * @param source conteúdo textual do código-fonte
   */
  public SourceBuffer(String source) {
    this.source = Objects.requireNonNull(source, "O código-fonte não pode ser nulo.");
    this.length = source.length();
    this.cursor = 0;
    this.line = 1;
    this.column = 1;
  }

  /**
   * Retorna o caractere atual sob o cursor sem consumi-lo.
   *
   * @return caractere atual ou '\0' se o fim da entrada for alcançado
   */
  public char peek() {
    return peek(0);
  }

  /**
   * Retorna o caractere localizado a um determinado deslocamento à frente do cursor.
   *
   * @param offset deslocamento a partir da posição atual do cursor
   * @return caractere na posição deslocada ou '\0' se estiver além do fim da entrada
   */
  public char peek(int offset) {
    int targetIndex = cursor + offset;
    if (targetIndex >= length || targetIndex < 0) {
      return END_OF_SOURCE;
    }
    return source.charAt(targetIndex);
  }

  /**
   * Consome e retorna o caractere atual e atualiza linha e coluna.
   *
   * @return caractere consumido ou '\0' se estiver no fim do arquivo
   */
  public char advance() {
    if (isAtEnd()) {
      return END_OF_SOURCE;
    }

    char currentChar = source.charAt(cursor);
    cursor++;

    updateCoordinates(currentChar);
    return currentChar;
  }

  /**
   * Verifica se o cursor alcançou ou ultrapassou o fim do código fonte.
   *
   * @return {@code true} se o cursor alcançou o fim, {@code false} caso contrário
   */
  public boolean isAtEnd() {
    return cursor >= length;
  }

  /**
   * Retorna a coordenada atual no código fonte.
   *
   * @return coordenada atual de linha e coluna
   */
  public SourcePosition getCurrentPosition() {
    return new SourcePosition(line, column);
  }

  /**
   * Retorna o índice numérico atual do cursor (0 a length).
   *
   * @return índice do cursor
   */
  public int getCursor() {
    return cursor;
  }

  /**
   * Retorna a linha atual (1-based).
   *
   * @return número da linha baseada em 1
   */
  public int getLine() {
    return line;
  }

  /**
   * Retorna a coluna atual (1-based).
   *
   * @return número da coluna baseada em 1
   */
  public int getColumn() {
    return column;
  }

  /**
   * Extrai uma fatia do código fonte entre dois índices de cursor.
   *
   * @param startCursor índice inicial inclusivo
   * @param endCursor índice final exclusivo
   * @return substring delimitada pelos índices fornecidos
   */
  public String extractSubstring(int startCursor, int endCursor) {
    if (startCursor < 0 || endCursor > length || startCursor > endCursor) {
      throw new IllegalArgumentException(
          String.format(
              "Índices inválidos para extração: start=%d, end=%d, length=%d",
              startCursor, endCursor, length));
    }
    return source.substring(startCursor, endCursor);
  }

  /**
   * Atualiza as coordenadas de linha e coluna com base no caractere consumido.
   *
   * @param consumedChar caractere recém-consumido pelo cursor
   */
  private void updateCoordinates(char consumedChar) {
    if (consumedChar == '\n') {
      line++;
      column = 1;
      return;
    }

    // No Windows a quebra de linha é "\r\n", tratamos para não incrementar a linha duas vezes
    if (consumedChar == '\r') {
      return;
    }

    column++;
  }
}
