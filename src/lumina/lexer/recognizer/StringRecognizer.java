package lumina.lexer.recognizer;

import java.util.Objects;
import lumina.common.source.SourceBuffer;
import lumina.common.source.SourcePosition;
import lumina.lexer.error.LexicalErrorReporter;
import lumina.lexer.token.Token;
import lumina.lexer.token.TokenType;

/**
 * Reconhecedor léxico para Literais de Cadeia de Caracteres (AFD 3). Trata sequências de escape e
 * detecta anomalias de quebra física de linha ou EOF prematuro.
 */
public final class StringRecognizer implements TokenRecognizer {

  /**
   * Reconhece literais de cadeia de caracteres tratando sequências de escape e quebras inválidas.
   *
   * @param buffer buffer de código-fonte
   * @param startPosition coordenada inicial do token
   * @param errorReporter coletor de erros léxicos
   * @return token de literal string reconhecido ou token de erro recuperado
   */
  @Override
  public Token scan(
      SourceBuffer buffer, SourcePosition startPosition, LexicalErrorReporter errorReporter) {
    Objects.requireNonNull(buffer, "O buffer não pode ser nulo.");
    Objects.requireNonNull(startPosition, "A posição inicial não pode ser nula.");
    Objects.requireNonNull(errorReporter, "O errorReporter não pode ser nulo.");

    int startCursor = buffer.getCursor();
    buffer.advance(); // Estado S0 -> S1: consome aspas de abertura

    while (true) {
      if (buffer.isAtEnd()) {
        return fail(
            buffer,
            startPosition,
            errorReporter,
            startCursor,
            "String não finalizada: atingiu o fim de arquivo (EOF) antes do fechamento de aspas.");
      }

      char nextChar = buffer.peek();

      if (nextChar == '\n' || nextChar == '\r') {
        return fail(
            buffer,
            startPosition,
            errorReporter,
            startCursor,
            "String não finalizada: quebra de linha não permitida dentro de string literal.");
      }

      if (nextChar == '\\') {
        buffer.advance(); // Estado S2 (Escape no AFD): consome '\'
        if (!buffer.isAtEnd()) {
          buffer.advance(); // Consome caractere escapado e retorna para S1
        }
        continue;
      }

      if (nextChar == '"') {
        buffer.advance(); // Estado S3 (Aceitação no AFD): fecha aspas
        String lexeme = buffer.extractSubstring(startCursor, buffer.getCursor());
        return new Token(TokenType.STRING_LITERAL, lexeme, startPosition);
      }

      buffer.advance(); // Permanece em S1 consumindo caractere normal
    }
  }

  /**
   * Registra anomalia de string não finalizada e retorna o token de erro correspondente.
   *
   * @param buffer buffer de código-fonte
   * @param pos coordenada inicial da string
   * @param reporter coletor de erros léxicos
   * @param start cursor inicial da string no buffer
   * @param msg mensagem descritiva do erro
   * @return token de erro construído
   */
  private Token fail(
      SourceBuffer buffer,
      SourcePosition pos,
      LexicalErrorReporter reporter,
      int start,
      String msg) {
    String lexeme = buffer.extractSubstring(start, buffer.getCursor());
    reporter.report(msg, pos, lexeme);
    return Token.error(lexeme, pos);
  }
}
