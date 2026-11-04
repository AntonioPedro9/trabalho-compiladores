package lumina.ast;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import lumina.common.source.SourcePosition;
import lumina.visitor.Visitor;

/** Nó de bloco de comandos delimitado por chaves (Block ::= "{" StmtList "}"). */
public final class BlockNode extends StmtNode {

  private final List<StmtNode> statements;

  public BlockNode(SourcePosition position, List<StmtNode> statements) {
    super(position);
    Objects.requireNonNull(statements, "A lista de comandos não pode ser nula.");
    this.statements = Collections.unmodifiableList(statements);
  }

  public List<StmtNode> getStatements() {
    return statements;
  }

  @Override
  public <T> T accept(Visitor<T> v) {
    return v.visitBlock(this);
  }
}
