package lumina.lexer.token;

import java.util.Map;
import java.util.Set;

/** Tabela imutável contendo a lista fechada de 13 palavras reservadas da linguagem Lumina. */
public final class KeywordTable {

  private static final Map<String, TokenType> KEYWORDS =
      Map.ofEntries(
          Map.entry("int", TokenType.INT),
          Map.entry("double", TokenType.DOUBLE),
          Map.entry("bool", TokenType.BOOL),
          Map.entry("char", TokenType.CHAR),
          Map.entry("string", TokenType.STRING),
          Map.entry("void", TokenType.VOID),
          Map.entry("if", TokenType.IF),
          Map.entry("else", TokenType.ELSE),
          Map.entry("while", TokenType.WHILE),
          Map.entry("return", TokenType.RETURN),
          Map.entry("fun", TokenType.FUN),
          Map.entry("true", TokenType.TRUE),
          Map.entry("false", TokenType.FALSE));

  /** Construtor privado para evitar instanciação da classe utilitária. */
  private KeywordTable() {}

  /**
   * Consulta o tipo de token correspondente a um determinado lexema.
   *
   * @param lexeme texto da unidade léxica consultada
   * @return tipo de token da palavra reservada ou IDENTIFIER se não encontrada
   */
  public static TokenType lookup(String lexeme) {
    return lexeme == null
        ? TokenType.IDENTIFIER
        : KEYWORDS.getOrDefault(lexeme, TokenType.IDENTIFIER);
  }

  /**
   * Verifica se um determinado lexema é uma palavra reservada da linguagem.
   *
   * @param lexeme texto da unidade léxica consultada
   * @return {@code true} se for palavra reservada, {@code false} caso contrário
   */
  public static boolean isKeyword(String lexeme) {
    return KEYWORDS.containsKey(lexeme);
  }

  /**
   * Retorna o conjunto com todas as palavras reservadas da linguagem Lumina.
   *
   * @return conjunto imutável de palavras reservadas
   */
  public static Set<String> getKeywords() {
    return KEYWORDS.keySet();
  }
}
