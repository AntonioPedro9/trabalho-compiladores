package lumina.lexer.recognizer;

import java.util.Objects;
import lumina.common.source.SourceBuffer;
import lumina.common.source.SourcePosition;
import lumina.lexer.error.LexicalErrorReporter;
import lumina.lexer.token.Token;
import lumina.lexer.token.TokenType;

/**
 * Reconhecedor léxico para Literais Numéricos (AFD 5). Distingue inteiros (INT_LITERAL) de decimais
 * (DOUBLE_LITERAL) e trata malformações (ex.: '42.').
 */
public final class NumberRecognizer implements TokenRecognizer {

  /**
   * Reconhece literais numéricos inteiros e decimais de ponto flutuante.
   *
   * @param buffer buffer de código-fonte
   * @param startPosition coordenada inicial do token
   * @param errorReporter coletor de erros léxicos
   * @return token numérico reconhecido ou token de erro
   */
  @Override
  public Token scan(
      SourceBuffer buffer, SourcePosition startPosition, LexicalErrorReporter errorReporter) {
    Objects.requireNonNull(buffer, "O buffer não pode ser nulo.");
    Objects.requireNonNull(startPosition, "A posição inicial não pode ser nula.");
    Objects.requireNonNull(errorReporter, "O errorReporter não pode ser nulo.");

    int startCursor = buffer.getCursor();
    buffer.advance(); // Estado S0 -> S1: consome primeiro dígito
    consumeDigits(buffer);

    if (buffer.peek() != '.') {
      return new Token(
          TokenType.INT_LITERAL,
          buffer.extractSubstring(startCursor, buffer.getCursor()),
          startPosition);
    }

    if (!CharClassifier.isDigit(buffer.peek(1))) {
      buffer.advance(); // Consome o ponto malformado
      String malformed = buffer.extractSubstring(startCursor, buffer.getCursor());
      errorReporter.report(
          "Literal decimal malformado: esperava-se dígitos após o ponto.",
          startPosition,
          malformed);
      return Token.error(malformed, startPosition);
    }

    // Estado S2 -> S3: consome '.' e dígitos fracionários
    buffer.advance();
    buffer.advance();
    consumeDigits(buffer);

    return new Token(
        TokenType.DOUBLE_LITERAL,
        buffer.extractSubstring(startCursor, buffer.getCursor()),
        startPosition);
  }

  /**
   * Consome sequências contíguas de dígitos numéricos no buffer.
   *
   * @param buffer buffer de código-fonte
   */
  private void consumeDigits(SourceBuffer buffer) {
    while (!buffer.isAtEnd() && CharClassifier.isDigit(buffer.peek())) {
      buffer.advance();
    }
  }
}
