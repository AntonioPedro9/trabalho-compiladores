package lumina.lexer.recognizer;

import java.util.Objects;
import lumina.common.source.SourceBuffer;
import lumina.common.source.SourcePosition;
import lumina.lexer.error.LexicalErrorReporter;
import lumina.lexer.token.KeywordTable;
import lumina.lexer.token.Token;
import lumina.lexer.token.TokenType;

/**
 * Reconhecedor léxico para Identificadores e Palavras Reservadas (AFD 1). Aplica a regra de
 * casamento mais longo (maximal munch) e consulta a tabela de palavras reservadas para determinar o
 * tipo do token.
 */
public final class IdentifierRecognizer implements TokenRecognizer {

  /**
   * Reconhece identificadores e palavras reservadas aplicando a regra do maximal munch.
   *
   * @param buffer buffer de código-fonte
   * @param startPosition coordenada inicial do token
   * @param errorReporter coletor de erros léxicos
   * @return token correspondente à palavra reservada ou identificador
   */
  @Override
  public Token scan(
      SourceBuffer buffer, SourcePosition startPosition, LexicalErrorReporter errorReporter) {
    Objects.requireNonNull(buffer, "O buffer não pode ser nulo.");
    Objects.requireNonNull(startPosition, "A posição inicial não pode ser nula.");

    int startCursor = buffer.getCursor();

    // Estado S0 -> S1: consome o caractere inicial (letra ou '_')
    buffer.advance();

    // Estado S1: auto-laço consumindo letras, dígitos e '_' (maximal munch)
    while (!buffer.isAtEnd() && CharClassifier.isIdentifierPart(buffer.peek())) {
      buffer.advance();
    }

    String lexeme = buffer.extractSubstring(startCursor, buffer.getCursor());
    TokenType tokenType = KeywordTable.lookup(lexeme);

    return new Token(tokenType, lexeme, startPosition);
  }

  /**
   * Reconhece identificadores e palavras reservadas sem coletor explícito de erros.
   *
   * @param buffer buffer de código-fonte
   * @param startPosition coordenada inicial do token
   * @return token correspondente à palavra reservada ou identificador
   */
  public Token scan(SourceBuffer buffer, SourcePosition startPosition) {
    return scan(buffer, startPosition, null);
  }
}
