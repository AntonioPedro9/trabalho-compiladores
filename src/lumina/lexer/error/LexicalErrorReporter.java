package lumina.lexer.error;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lumina.common.source.SourcePosition;

/** Acumulador e formatador de erros léxicos ocorridos durante a varredura do código-fonte. */
public final class LexicalErrorReporter {

  private final List<LexicalError> errors = new ArrayList<>();

  /**
   * Registra uma nova ocorrência de erro léxico.
   *
   * @param error instância do erro léxico a ser registrado
   */
  public void report(LexicalError error) {
    errors.add(error);
  }

  /**
   * Cria e registra um novo erro léxico com os atributos fornecidos.
   *
   * @param message descrição da anomalia encontrada
   * @param position coordenada no código-fonte onde o erro ocorreu
   * @param offendingLexeme trecho ou lexema que originou o erro
   */
  public void report(String message, SourcePosition position, String offendingLexeme) {
    report(new LexicalError(message, position, offendingLexeme));
  }

  /**
   * Verifica se foram detectados erros durante a análise léxica.
   *
   * @return {@code true} se houver erros registrados, {@code false} caso contrário
   */
  public boolean hasErrors() {
    return !errors.isEmpty();
  }

  /**
   * Retorna a quantidade total de erros registrados.
   *
   * @return número total de erros
   */
  public int getErrorCount() {
    return errors.size();
  }

  /**
   * Retorna a lista imutável de erros registrados.
   *
   * @return lista não modificável de erros léxicos
   */
  public List<LexicalError> getErrors() {
    return Collections.unmodifiableList(errors);
  }

  /** Limpa todos os erros registrados no coletor. */
  public void clear() {
    errors.clear();
  }

  /**
   * Imprime o relatório formatado de erros no fluxo de saída especificado.
   *
   * @param destination fluxo de saída para exibição do relatório
   */
  public void printReport(PrintStream destination) {
    if (!hasErrors()) {
      destination.println("Nenhum erro léxico detectado.");
      return;
    }
    destination.printf("Total de %d erro(s) léxico(s) detectado(s):%n", errors.size());
    errors.forEach(e -> destination.println("  " + e));
  }
}
