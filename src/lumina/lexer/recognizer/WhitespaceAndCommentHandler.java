package lumina.lexer.recognizer;

import java.util.Objects;
import lumina.common.source.SourceBuffer;
import lumina.common.source.SourcePosition;
import lumina.lexer.error.LexicalErrorReporter;

/**
 * Responsável por descartar espaços em branco e comentários contíguos no fluxo de entrada. Trata
 * comentários de linha (//) e comentários de bloco multilinhas, registrando erro léxico caso o fim
 * de arquivo seja atingido sem fechamento do bloco.
 */
public final class WhitespaceAndCommentHandler {

  /**
   * Avança o cursor sobre espaços em branco e comentários até o próximo caractere significativo.
   *
   * @param buffer buffer de código-fonte
   * @param errorReporter coletor de erros léxicos
   */
  public void skip(SourceBuffer buffer, LexicalErrorReporter errorReporter) {
    Objects.requireNonNull(buffer, "O buffer não pode ser nulo.");
    Objects.requireNonNull(errorReporter, "O errorReporter não pode ser nulo.");

    while (!buffer.isAtEnd()) {
      char currentChar = buffer.peek();

      if (CharClassifier.isWhitespace(currentChar)) {
        buffer.advance();
        continue;
      }

      if (isSingleLineCommentStart(buffer, currentChar)) {
        skipSingleLineComment(buffer);
        continue;
      }

      if (isBlockCommentStart(buffer, currentChar)) {
        skipBlockComment(buffer, errorReporter);
        continue;
      }

      // Nenhum espaço ou comentário restante
      break;
    }
  }

  /**
   * Verifica se os próximos caracteres iniciam um comentário de linha simples ('//').
   *
   * @param buffer buffer de código-fonte
   * @param currentChar caractere atual sob inspeção
   * @return {@code true} se for início de comentário de linha, {@code false} caso contrário
   */
  private boolean isSingleLineCommentStart(SourceBuffer buffer, char currentChar) {
    return currentChar == '/' && buffer.peek(1) == '/';
  }

  /**
   * Verifica se os próximos caracteres iniciam um comentário de bloco ('/*').
   *
   * @param buffer buffer de código-fonte
   * @param currentChar caractere atual sob inspeção
   * @return {@code true} se for início de comentário de bloco, {@code false} caso contrário
   */
  private boolean isBlockCommentStart(SourceBuffer buffer, char currentChar) {
    return currentChar == '/' && buffer.peek(1) == '*';
  }

  /**
   * Consome caracteres até o fim da linha atual para descartar comentário de linha.
   *
   * @param buffer buffer de código-fonte
   */
  private void skipSingleLineComment(SourceBuffer buffer) {
    // Consome os dois caracteres '//'
    buffer.advance();
    buffer.advance();

    while (!buffer.isAtEnd() && buffer.peek() != '\n') {
      buffer.advance();
    }
  }

  /**
   * Consome caracteres até o delimitador de fechamento de comentário de bloco.
   *
   * @param buffer buffer de código-fonte
   * @param errorReporter coletor de erros léxicos caso atinja o fim de arquivo antes do fechamento
   */
  private void skipBlockComment(SourceBuffer buffer, LexicalErrorReporter errorReporter) {
    SourcePosition commentStartPosition = buffer.getCurrentPosition();

    // Consome os dois caracteres '/*'
    buffer.advance();
    buffer.advance();

    while (!buffer.isAtEnd()) {
      if (isBlockCommentEnd(buffer)) {
        buffer.advance(); // Consome '*'
        buffer.advance(); // Consome '/'
        return;
      }
      buffer.advance();
    }

    // Fim de arquivo alcançado antes do fechamento
    errorReporter.report(
        "Comentário de bloco não finalizado: atingiu o fim de arquivo (EOF) antes de '*/'.",
        commentStartPosition,
        "/*");
  }

  /**
   * Verifica se os caracteres atuais fecham um comentário de bloco.
   *
   * @param buffer buffer de código-fonte
   * @return {@code true} se for delimitador de fechamento, {@code false} caso contrário
   */
  private boolean isBlockCommentEnd(SourceBuffer buffer) {
    return buffer.peek() == '*' && buffer.peek(1) == '/';
  }
}
