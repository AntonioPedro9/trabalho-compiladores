package lumina.types;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.Objects;
import java.util.Set;
import lumina.ast.Node;

/** Mantém o contexto da verificação de tipos: pilha de funções ativas e variáveis inicializadas. */
public final class TypeCheckContext {

  /** Representa a função ou procedimento em análise. */
  public record FunctionFrame(String name, LuminaType returnType) {
    public FunctionFrame {
      Objects.requireNonNull(name, "O nome da função não pode ser nulo.");
      Objects.requireNonNull(returnType, "O tipo de retorno não pode ser nulo.");
    }

    public boolean isProcedure() {
      return returnType.isVoid();
    }
  }

  private final Deque<FunctionFrame> functionStack;
  private Set<Node> initializedDeclarations;

  public TypeCheckContext() {
    this.functionStack = new ArrayDeque<>();
    this.initializedDeclarations = newIdentityNodeSet();
  }

  public void enterFunction(String name, LuminaType returnType) {
    functionStack.push(new FunctionFrame(name, returnType));
  }

  public void exitFunction() {
    if (isInsideFunction()) {
      functionStack.pop();
    }
  }

  public boolean isInsideFunction() {
    return !functionStack.isEmpty();
  }

  public FunctionFrame currentFunction() {
    return functionStack.peek();
  }

  public void markInitialized(Node declarationNode) {
    if (declarationNode != null) {
      initializedDeclarations.add(declarationNode);
    }
  }

  public boolean isInitialized(Node declarationNode) {
    return declarationNode != null && initializedDeclarations.contains(declarationNode);
  }

  public Set<Node> snapshotInitialized() {
    Set<Node> snapshot = newIdentityNodeSet();
    snapshot.addAll(initializedDeclarations);
    return snapshot;
  }

  public void restoreInitialized(Set<Node> snapshot) {
    Set<Node> restored = newIdentityNodeSet();
    if (snapshot != null) {
      restored.addAll(snapshot);
    }
    this.initializedDeclarations = restored;
  }

  /**
   * Mescla de forma conservadora as variáveis inicializadas nos ramos de um {@code if},
   * considerando retorno antecipado ou a interseção entre os dois caminhos.
   */
  public void mergeBranchInitialization(
      Set<Node> afterThen, boolean thenReturns, Set<Node> afterElse, boolean elseReturns) {
    if (onlyElseBranchFallsThrough(thenReturns, elseReturns)) {
      restoreInitialized(afterElse);
      return;
    }
    if (onlyThenBranchFallsThrough(thenReturns, elseReturns)) {
      restoreInitialized(afterThen);
      return;
    }
    this.initializedDeclarations = intersectDeclarations(afterThen, afterElse);
  }

  private boolean onlyElseBranchFallsThrough(boolean thenReturns, boolean elseReturns) {
    return thenReturns && !elseReturns;
  }

  private boolean onlyThenBranchFallsThrough(boolean thenReturns, boolean elseReturns) {
    return !thenReturns && elseReturns;
  }

  private Set<Node> intersectDeclarations(Set<Node> firstBranch, Set<Node> secondBranch) {
    Set<Node> intersection = newIdentityNodeSet();
    for (Node declaration : firstBranch) {
      if (secondBranch.contains(declaration)) {
        intersection.add(declaration);
      }
    }
    return intersection;
  }

  private static Set<Node> newIdentityNodeSet() {
    return Collections.newSetFromMap(new IdentityHashMap<>());
  }
}
