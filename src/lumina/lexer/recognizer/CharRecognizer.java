package lumina.lexer.recognizer;

import java.util.Objects;
import lumina.common.source.SourceBuffer;
import lumina.common.source.SourcePosition;
import lumina.lexer.error.LexicalErrorReporter;
import lumina.lexer.token.Token;
import lumina.lexer.token.TokenType;

/** Reconhecedor léxico para Literais de Caractere (CHAR_LITERAL - AFD 4). */
public final class CharRecognizer implements TokenRecognizer {

  /**
   * Reconhece um literal de caractere na entrada tratando sequências de escape.
   *
   * @param buffer buffer de código-fonte
   * @param startPosition coordenada inicial do token
   * @param errorReporter coletor de erros léxicos
   * @return token CHAR_LITERAL reconhecido ou token de erro recuperado
   */
  @Override
  public Token scan(
      SourceBuffer buffer, SourcePosition startPosition, LexicalErrorReporter errorReporter) {
    Objects.requireNonNull(buffer, "O buffer não pode ser nulo.");
    Objects.requireNonNull(startPosition, "A posição inicial não pode ser nula.");
    Objects.requireNonNull(errorReporter, "O errorReporter não pode ser nulo.");

    int startCursor = buffer.getCursor();
    buffer.advance(); // Consome aspa simples de abertura

    if (buffer.isAtEnd() || buffer.peek() == '\n') {
      String unterminatedChar = buffer.extractSubstring(startCursor, buffer.getCursor());
      errorReporter.report("Literal de caractere não finalizado.", startPosition, unterminatedChar);
      return Token.error(unterminatedChar, startPosition);
    }

    if (buffer.peek() == '\\') {
      buffer.advance(); // Consome escape
      if (!buffer.isAtEnd()) {
        buffer.advance();
      }
    } else {
      buffer.advance(); // Consome caractere normal
    }

    if (buffer.peek() != '\'') {
      String malformedChar = buffer.extractSubstring(startCursor, buffer.getCursor());
      errorReporter.report(
          "Literal de caractere malformado: esperava-se fechamento com aspa simples.",
          startPosition,
          malformedChar);
      return Token.error(malformedChar, startPosition);
    }

    buffer.advance(); // Consome aspa simples de fechamento
    String charLexeme = buffer.extractSubstring(startCursor, buffer.getCursor());
    return new Token(TokenType.CHAR_LITERAL, charLexeme, startPosition);
  }
}
