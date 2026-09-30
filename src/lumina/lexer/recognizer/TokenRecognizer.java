package lumina.lexer.recognizer;

import lumina.common.source.SourceBuffer;
import lumina.common.source.SourcePosition;
import lumina.lexer.error.LexicalErrorReporter;
import lumina.lexer.token.Token;

/** Contrato uniforme para os Autômatos Finitos Determinísticos (AFDs) reconhecedores de tokens. */
public interface TokenRecognizer {

  /**
   * Varre a entrada a partir do buffer e constrói o Token reconhecido, reportando anomalias ao
   * errorReporter quando necessário.
   *
   * @param buffer buffer de caracteres com controle de cursor e coordenadas
   * @param startPosition coordenada de início da unidade léxica
   * @param errorReporter acumulador de erros léxicos para recuperação resiliente
   * @return token válido ou token de erro recuperado
   */
  Token scan(SourceBuffer buffer, SourcePosition startPosition, LexicalErrorReporter errorReporter);
}
