package lumina.lexer.token;

import java.util.Objects;
import lumina.common.source.SourcePosition;

/**
 * Representa um token léxico imutável contendo seu tipo, o lexema original e a coordenada de
 * início.
 *
 * @param type tipo do token
 * @param lexeme representação textual original
 * @param position coordenada de início no código-fonte
 */
public record Token(TokenType type, String lexeme, SourcePosition position) {

  /**
   * Valida os atributos obrigatórios do token léxico.
   *
   * @param type tipo do token
   * @param lexeme representação textual original
   * @param position coordenada de início no código-fonte
   */
  public Token {
    Objects.requireNonNull(type, "O tipo do token não pode ser nulo.");
    Objects.requireNonNull(lexeme, "O lexema não pode ser nulo.");
    Objects.requireNonNull(position, "A posição não pode ser nula.");
  }

  /**
   * Cria um token a partir de coordenadas inteiras de linha e coluna.
   *
   * @param type tipo do token
   * @param lexeme representação textual original
   * @param line linha no código-fonte
   * @param column coluna no código-fonte
   */
  public Token(TokenType type, String lexeme, int line, int column) {
    this(type, lexeme, new SourcePosition(line, column));
  }

  /**
   * Cria um token especial de fim de arquivo (EOF).
   *
   * @param position coordenada de fim de arquivo
   * @return instância de Token do tipo EOF
   */
  public static Token eof(SourcePosition position) {
    return new Token(TokenType.EOF, "", position);
  }

  /**
   * Cria um token especial de erro léxico recuperado.
   *
   * @param lexeme trecho causador da anomalia
   * @param position coordenada do erro
   * @return instância de Token do tipo ERROR
   */
  public static Token error(String lexeme, SourcePosition position) {
    return new Token(TokenType.ERROR, lexeme, position);
  }

  /**
   * Retorna o tipo deste token.
   *
   * @return tipo de token
   */
  public TokenType getType() {
    return type;
  }

  /**
   * Retorna o lexema original deste token.
   *
   * @return cadeia de caracteres original
   */
  public String getLexeme() {
    return lexeme;
  }

  /**
   * Retorna a coordenada de início deste token no código-fonte.
   *
   * @return coordenada de início
   */
  public SourcePosition getPosition() {
    return position;
  }

  /**
   * Retorna a linha onde o token se inicia.
   *
   * @return linha baseada em 1
   */
  public int getLine() {
    return position.line();
  }

  /**
   * Retorna a coluna onde o token se inicia.
   *
   * @return coluna baseada em 1
   */
  public int getColumn() {
    return position.column();
  }

  /**
   * Verifica se o token pertence ao tipo esperado.
   *
   * @param expectedType tipo esperado para comparação
   * @return {@code true} se o tipo corresponder, {@code false} caso contrário
   */
  public boolean is(TokenType expectedType) {
    return this.type == expectedType;
  }

  /**
   * Retorna a representação textual formatada do token.
   *
   * @return string formatada com tipo, lexema e posição
   */
  @Override
  public String toString() {
    return String.format("Token(%s, \"%s\", %s)", type.name(), escapeForDisplay(lexeme), position);
  }

  /**
   * Escapa caracteres de controle para exibição legível.
   *
   * @param text texto original
   * @return texto com caracteres escapados
   */
  private static String escapeForDisplay(String text) {
    if (text == null) {
      return "";
    }
    return text.replace("\\", "\\\\")
        .replace("\n", "\\n")
        .replace("\r", "\\r")
        .replace("\t", "\\t")
        .replace("\"", "\\\"");
  }
}
