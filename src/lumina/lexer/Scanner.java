package lumina.lexer;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lumina.common.source.SourceBuffer;
import lumina.common.source.SourcePosition;
import lumina.lexer.error.LexicalError;
import lumina.lexer.error.LexicalErrorReporter;
import lumina.lexer.recognizer.CharClassifier;
import lumina.lexer.recognizer.CharRecognizer;
import lumina.lexer.recognizer.IdentifierRecognizer;
import lumina.lexer.recognizer.NumberRecognizer;
import lumina.lexer.recognizer.OperatorRecognizer;
import lumina.lexer.recognizer.StringRecognizer;
import lumina.lexer.recognizer.WhitespaceAndCommentHandler;
import lumina.lexer.token.Token;
import lumina.lexer.token.TokenType;

/**
 * Analisador Léxico da linguagem Lumina. Atua como orquestrador da análise léxica, delegando as
 * tarefas aos reconhecedores baseados em Autômatos Finitos Determinísticos (AFDs).
 */
public final class Scanner {

  private final SourceBuffer buffer;
  private final LexicalErrorReporter errorReporter;
  private final WhitespaceAndCommentHandler whitespaceHandler;
  private final IdentifierRecognizer identifierRecognizer;
  private final NumberRecognizer numberRecognizer;
  private final StringRecognizer stringRecognizer;
  private final CharRecognizer charRecognizer;
  private final OperatorRecognizer operatorRecognizer;

  /**
   * Constrói o analisador léxico para o código-fonte fornecido com coletor padrão de erros.
   *
   * @param source código-fonte a ser analisado
   */
  public Scanner(String source) {
    this(source, new LexicalErrorReporter());
  }

  /**
   * Constrói o analisador léxico com código-fonte e coletor de erros específico.
   *
   * @param source código-fonte a ser analisado
   * @param errorReporter coletor de erros léxicos
   */
  public Scanner(String source, LexicalErrorReporter errorReporter) {
    this.buffer =
        new SourceBuffer(Objects.requireNonNull(source, "O código-fonte não pode ser nulo."));
    this.errorReporter =
        Objects.requireNonNull(errorReporter, "O errorReporter não pode ser nulo.");

    this.whitespaceHandler = new WhitespaceAndCommentHandler();
    this.identifierRecognizer = new IdentifierRecognizer();
    this.numberRecognizer = new NumberRecognizer();
    this.stringRecognizer = new StringRecognizer();
    this.charRecognizer = new CharRecognizer();
    this.operatorRecognizer = new OperatorRecognizer();
  }

  /**
   * Extrai e retorna o próximo token da entrada aplicando maximal munch. Retorna um token EOF ao
   * alcançar o fim do arquivo. Retorna tokens do tipo ERROR em caso de anomalias léxicas
   * recuperáveis.
   *
   * @return próximo token reconhecido ou token EOF
   */
  public Token nextToken() {
    whitespaceHandler.skip(buffer, errorReporter);

    if (buffer.isAtEnd()) {
      return Token.eof(buffer.getCurrentPosition());
    }

    SourcePosition startPosition = buffer.getCurrentPosition();
    char currentChar = buffer.peek();

    if (isIdentifier(currentChar)) {
      return identifierRecognizer.scan(buffer, startPosition, errorReporter);
    }

    if (isNumber(currentChar)) {
      return numberRecognizer.scan(buffer, startPosition, errorReporter);
    }

    if (isString(currentChar)) {
      return stringRecognizer.scan(buffer, startPosition, errorReporter);
    }

    if (isChar(currentChar)) {
      return charRecognizer.scan(buffer, startPosition, errorReporter);
    }

    return operatorRecognizer.scan(buffer, startPosition, errorReporter);
  }

  /**
   * Varre todo o código fonte restante e retorna a lista de tokens incluindo o token EOF.
   *
   * @return lista completa de tokens identificados na entrada
   */
  public List<Token> scanAllTokens() {
    List<Token> tokenList = new ArrayList<>();
    Token currentToken;

    do {
      currentToken = nextToken();
      tokenList.add(currentToken);
    } while (currentToken.getType() != TokenType.EOF);

    return tokenList;
  }

  /**
   * Indica se ainda existem caracteres significativos não processados no buffer.
   *
   * @return {@code true} se houver mais tokens a processar, {@code false} caso contrário
   */
  public boolean hasMoreTokens() {
    whitespaceHandler.skip(buffer, errorReporter);
    return !buffer.isAtEnd();
  }

  /**
   * Indica se ainda existem caracteres a serem processados.
   *
   * @return {@code true} se houver caracteres pendentes, {@code false} caso contrário
   */
  public boolean hasNext() {
    return hasMoreTokens();
  }

  /**
   * Inspeciona o caractere atual sob o cursor sem consumi-lo.
   *
   * @return caractere atual ou fim de arquivo
   */
  public char peek() {
    return buffer.peek();
  }

  /**
   * Consome e avança o cursor no caractere atual.
   *
   * @return caractere consumido
   */
  public char advance() {
    return buffer.advance();
  }

  /**
   * Retorna a lista imutável de erros léxicos acumulados até o momento.
   *
   * @return lista imutável de erros léxicos
   */
  public List<LexicalError> getErrors() {
    return errorReporter.getErrors();
  }

  /**
   * Retorna o coletor de erros associado a este scanner.
   *
   * @return instância de LexicalErrorReporter
   */
  public LexicalErrorReporter getErrorReporter() {
    return errorReporter;
  }

  /**
   * Verifica se o caractere pode iniciar um identificador.
   *
   * @param c caractere a ser verificado
   * @return {@code true} se for início de identificador, {@code false} caso contrário
   */
  private boolean isIdentifier(char c) {
    return CharClassifier.isIdentifierStart(c);
  }

  /**
   * Verifica se o caractere é um dígito numérico.
   *
   * @param c caractere a ser verificado
   * @return {@code true} se for dígito, {@code false} caso contrário
   */
  private boolean isNumber(char c) {
    return CharClassifier.isDigit(c);
  }

  /**
   * Verifica se o caractere indica o início de uma cadeia de caracteres (string).
   *
   * @param c caractere a ser verificado
   * @return {@code true} se for aspas duplas, {@code false} caso contrário
   */
  private boolean isString(char c) {
    return c == '"';
  }

  /**
   * Verifica se o caractere indica o início de um literal de caractere.
   *
   * @param c caractere a ser verificado
   * @return {@code true} se for aspas simples, {@code false} caso contrário
   */
  private boolean isChar(char c) {
    return c == '\'';
  }
}
