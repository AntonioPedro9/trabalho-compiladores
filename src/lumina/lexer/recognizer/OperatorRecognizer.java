package lumina.lexer.recognizer;

import java.util.Objects;
import lumina.common.source.SourceBuffer;
import lumina.common.source.SourcePosition;
import lumina.lexer.error.LexicalErrorReporter;
import lumina.lexer.token.Token;
import lumina.lexer.token.TokenType;

/**
 * Reconhecedor léxico para operadores e delimitadores (AFD 2). Aplica casamento mais longo (maximal
 * munch) para desambiguar operadores compostos e reporta caracteres inválidos fora do alfabeto com
 * recuperação resiliente.
 */
public final class OperatorRecognizer implements TokenRecognizer {

  /**
   * Reconhece operadores simples e compostos e delimitadores pontuais na entrada.
   *
   * @param buffer buffer de código-fonte
   * @param startPosition coordenada inicial do token
   * @param errorReporter coletor de erros léxicos
   * @return token do operador ou delimitador reconhecido ou token de erro
   */
  @Override
  public Token scan(
      SourceBuffer buffer, SourcePosition startPosition, LexicalErrorReporter errorReporter) {
    Objects.requireNonNull(buffer, "O buffer não pode ser nulo.");
    Objects.requireNonNull(startPosition, "A posição inicial não pode ser nula.");
    Objects.requireNonNull(errorReporter, "O errorReporter não pode ser nulo.");

    char firstChar = buffer.advance();

    return switch (firstChar) {
      case '=' -> matchNext(buffer, '=', TokenType.EQUAL_EQUAL, TokenType.EQUAL, startPosition);
      case '!' -> matchNext(buffer, '=', TokenType.BANG_EQUAL, TokenType.BANG, startPosition);
      case '<' -> matchNext(buffer, '=', TokenType.LESS_EQUAL, TokenType.LESS, startPosition);
      case '>' -> matchNext(buffer, '=', TokenType.GREATER_EQUAL, TokenType.GREATER, startPosition);

      case '&' ->
          matchCompound(
              buffer, '&', TokenType.AMPERSAND_AMPERSAND, "&", startPosition, errorReporter);
      case '|' ->
          matchCompound(buffer, '|', TokenType.PIPE_PIPE, "|", startPosition, errorReporter);

      case '+' -> new Token(TokenType.PLUS, "+", startPosition);
      case '-' -> new Token(TokenType.MINUS, "-", startPosition);
      case '*' -> new Token(TokenType.STAR, "*", startPosition);
      case '/' -> new Token(TokenType.SLASH, "/", startPosition);
      case '%' -> new Token(TokenType.PERCENT, "%", startPosition);

      case '(' -> new Token(TokenType.LEFT_PAREN, "(", startPosition);
      case ')' -> new Token(TokenType.RIGHT_PAREN, ")", startPosition);
      case '{' -> new Token(TokenType.LEFT_BRACE, "{", startPosition);
      case '}' -> new Token(TokenType.RIGHT_BRACE, "}", startPosition);
      case '[' -> new Token(TokenType.LEFT_BRACKET, "[", startPosition);
      case ']' -> new Token(TokenType.RIGHT_BRACKET, "]", startPosition);
      case ';' -> new Token(TokenType.SEMICOLON, ";", startPosition);
      case ',' -> new Token(TokenType.COMMA, ",", startPosition);
      case ':' -> new Token(TokenType.COLON, ":", startPosition);

      default -> handleInvalidCharacter(firstChar, startPosition, errorReporter);
    };
  }

  /**
   * Verifica se o próximo caractere compõe um operador composto ou simples.
   *
   * @param buffer buffer de código-fonte
   * @param expectedChar caractere esperado para formar operador composto
   * @param compoundType tipo do token caso forme operador composto
   * @param singleType tipo do token caso seja operador simples
   * @param startPosition coordenada inicial do token
   * @return token reconhecido
   */
  private Token matchNext(
      SourceBuffer buffer,
      char expectedChar,
      TokenType compoundType,
      TokenType singleType,
      SourcePosition startPosition) {
    if (buffer.peek() == expectedChar) {
      buffer.advance();
      String compoundLexeme = singleType.getDescription() + expectedChar;
      return new Token(compoundType, compoundLexeme, startPosition);
    }
    return new Token(singleType, singleType.getDescription(), startPosition);
  }

  /**
   * Trata operadores compostos obrigatórios como '&&' e '||'.
   *
   * @param buffer buffer de código-fonte
   * @param expectedChar segundo caractere esperado do operador composto
   * @param compoundType tipo do token em caso de correspondência
   * @param singleChar representação textual do primeiro caractere
   * @param startPosition coordenada inicial do token
   * @param errorReporter coletor de erros léxicos
   * @return token do operador composto ou token de erro
   */
  private Token matchCompound(
      SourceBuffer buffer,
      char expectedChar,
      TokenType compoundType,
      String singleChar,
      SourcePosition startPosition,
      LexicalErrorReporter errorReporter) {
    if (buffer.peek() == expectedChar) {
      buffer.advance();
      return new Token(compoundType, singleChar + expectedChar, startPosition);
    }
    errorReporter.report(
        "Caractere '"
            + singleChar
            + "' inesperado: esperado operador composto '"
            + singleChar
            + expectedChar
            + "'.",
        startPosition,
        singleChar);
    return Token.error(singleChar, startPosition);
  }

  /**
   * Reporta erro para caracteres fora do alfabeto da linguagem e gera token de erro.
   *
   * @param invalidChar caractere inválido encontrado
   * @param startPosition coordenada inicial do caractere
   * @param errorReporter coletor de erros léxicos
   * @return token de erro correspondente ao caractere inválido
   */
  private Token handleInvalidCharacter(
      char invalidChar, SourcePosition startPosition, LexicalErrorReporter errorReporter) {
    String invalidLexeme = String.valueOf(invalidChar);
    errorReporter.report(
        String.format(
            "Caractere inválido fora do alfabeto da linguagem: '%s' (ASCII %d).",
            invalidChar, (int) invalidChar),
        startPosition,
        invalidLexeme);
    return Token.error(invalidLexeme, startPosition);
  }
}
