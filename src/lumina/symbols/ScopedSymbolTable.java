package lumina.symbols;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Implementação de {@link SymbolTable} baseada em Pilha de Mapas (um {@link Map} por nível de
 * escopo). Inicia com o escopo global aberto no nível {@code 0}.
 */
public final class ScopedSymbolTable implements SymbolTable {

  private static final int GLOBAL_LEVEL = 0;

  private final List<Map<String, Symbol>> scopes;

  public ScopedSymbolTable() {
    this.scopes = new ArrayList<>();
    this.scopes.add(new LinkedHashMap<>());
  }

  @Override
  public void openScope() {
    scopes.add(new LinkedHashMap<>());
  }

  /**
   * Fecha o escopo de bloco no topo da pilha.
   *
   * @throws IllegalStateException se chamado quando já estiver no nível global ({@code 0}),
   *     evitando qualquer {@code NullPointerException} ou estado inconsistente
   */
  @Override
  public void closeScope() {
    if (isAtGlobalScope()) {
      throw new IllegalStateException(
          "Não há escopo de bloco aberto para fechar (nível global 0 não pode ser fechado).");
    }
    scopes.remove(level());
  }

  @Override
  public boolean declare(Symbol s) {
    Objects.requireNonNull(s, "O símbolo a ser declarado não pode ser nulo.");
    if (isAlreadyDeclaredInCurrentScope(s.getName())) {
      return false;
    }
    currentScope().put(s.getName(), s);
    return true;
  }

  @Override
  public Symbol lookupLocal(String name) {
    if (name == null) {
      return null;
    }
    return currentScope().get(name);
  }

  @Override
  public Symbol lookup(String name) {
    if (name == null) {
      return null;
    }
    for (int scopeIndex = level(); scopeIndex >= GLOBAL_LEVEL; scopeIndex--) {
      Symbol found = scopes.get(scopeIndex).get(name);
      if (found != null) {
        return found;
      }
    }
    return null;
  }

  @Override
  public int level() {
    return scopes.size() - 1;
  }

  private boolean isAtGlobalScope() {
    return level() <= GLOBAL_LEVEL;
  }

  private boolean isAlreadyDeclaredInCurrentScope(String name) {
    return lookupLocal(name) != null;
  }

  private Map<String, Symbol> currentScope() {
    return scopes.get(level());
  }
}
