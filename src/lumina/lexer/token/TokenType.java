package lumina.lexer.token;

/** Representa os tipos de tokens reconhecidos pelo analisador léxico da linguagem Lumina. */
public enum TokenType {

  // Palavras reservadas
  INT("int"),
  DOUBLE("double"),
  BOOL("bool"),
  CHAR("char"),
  STRING("string"),
  VOID("void"),
  IF("if"),
  ELSE("else"),
  WHILE("while"),
  RETURN("return"),
  FUN("fun"),
  TRUE("true"),
  FALSE("false"),

  // Identificadores e literais
  IDENTIFIER("identificador"),
  INT_LITERAL("literal inteiro"),
  DOUBLE_LITERAL("literal double"),
  STRING_LITERAL("literal string"),
  CHAR_LITERAL("literal char"),

  // Operadores aritméticos
  PLUS("+"),
  MINUS("-"),
  STAR("*"),
  SLASH("/"),
  PERCENT("%"),

  // Operadores de atribuição e relacionais
  EQUAL("="),
  EQUAL_EQUAL("=="),
  BANG_EQUAL("!="),
  LESS("<"),
  LESS_EQUAL("<="),
  GREATER(">"),
  GREATER_EQUAL(">="),

  // Operadores lógicos
  AMPERSAND_AMPERSAND("&&"),
  PIPE_PIPE("||"),
  BANG("!"),

  // Delimitadores e pontuação
  LEFT_PAREN("("),
  RIGHT_PAREN(")"),
  LEFT_BRACE("{"),
  RIGHT_BRACE("}"),
  LEFT_BRACKET("["),
  RIGHT_BRACKET("]"),
  SEMICOLON(";"),
  COMMA(","),
  COLON(":"),

  // Tokens especiais de controle
  ERROR("erro lexico"),
  EOF("fim de arquivo");

  private final String description;

  /**
   * Construtor da constante de enum com sua descrição textual.
   *
   * @param description descrição legível do tipo de token
   */
  TokenType(String description) {
    this.description = description;
  }

  /**
   * Retorna a descrição legível deste tipo de token.
   *
   * @return descrição textual do token
   */
  public String getDescription() {
    return description;
  }

  /**
   * Verifica se o tipo de token é uma palavra reservada da linguagem.
   *
   * @return {@code true} se for palavra reservada, {@code false} caso contrário
   */
  public boolean isKeyword() {
    return this.ordinal() <= FALSE.ordinal();
  }

  /**
   * Verifica se o tipo de token é um literal (numérico, texto, caractere ou booleano).
   *
   * @return {@code true} se for literal, {@code false} caso contrário
   */
  public boolean isLiteral() {
    return this == INT_LITERAL
        || this == DOUBLE_LITERAL
        || this == STRING_LITERAL
        || this == CHAR_LITERAL
        || this == TRUE
        || this == FALSE;
  }

  /**
   * Verifica se o tipo de token é um operador.
   *
   * @return {@code true} se for operador, {@code false} caso contrário
   */
  public boolean isOperator() {
    return this.ordinal() >= PLUS.ordinal() && this.ordinal() <= BANG.ordinal();
  }

  /**
   * Verifica se o tipo de token é um delimitador de pontuação.
   *
   * @return {@code true} se for delimitador, {@code false} caso contrário
   */
  public boolean isDelimiter() {
    return this.ordinal() >= LEFT_PAREN.ordinal() && this.ordinal() <= COLON.ordinal();
  }
}
