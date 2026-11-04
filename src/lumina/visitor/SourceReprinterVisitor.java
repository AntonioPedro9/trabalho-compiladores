package lumina.visitor;

import java.util.List;
import lumina.ast.AssignNode;
import lumina.ast.BinNode;
import lumina.ast.BlockNode;
import lumina.ast.BoolLitNode;
import lumina.ast.CallExprNode;
import lumina.ast.CallStmtNode;
import lumina.ast.CharLitNode;
import lumina.ast.DeclNode;
import lumina.ast.DoubleLitNode;
import lumina.ast.ExprNode;
import lumina.ast.FunctionDeclNode;
import lumina.ast.IdNode;
import lumina.ast.IfNode;
import lumina.ast.IntLitNode;
import lumina.ast.Node;
import lumina.ast.ParamNode;
import lumina.ast.ProgramNode;
import lumina.ast.ReturnNode;
import lumina.ast.StmtNode;
import lumina.ast.StringLitNode;
import lumina.ast.TypeNode;
import lumina.ast.UnaryNode;
import lumina.ast.VarDeclNode;
import lumina.ast.WhileNode;

/**
 * Reimpressora de código-fonte canônico da linguagem Lumina (Seção 3.2.2). Reconstrói um programa
 * sintaticamente válido a partir da AST, parentizando expressões binárias e unárias de forma
 * canônica para preservar inequivocamente a precedência e a associatividade no teste de ida e volta
 * (round-trip).
 */
public final class SourceReprinterVisitor implements Visitor<String> {

  private static final String INDENT_STEP = "    ";
  private int indentLevel = 0;

  public String reprint(Node root) {
    this.indentLevel = 0;
    return root.accept(this);
  }

  private String currentIndent() {
    return INDENT_STEP.repeat(indentLevel);
  }

  @Override
  public String visitProgram(ProgramNode node) {
    StringBuilder sb = new StringBuilder();
    List<DeclNode> decls = node.getDeclarations();
    for (int i = 0; i < decls.size(); i++) {
      sb.append(decls.get(i).accept(this));
      if (i + 1 < decls.size()) {
        sb.append("\n");
      }
    }
    return sb.toString();
  }

  @Override
  public String visitVarDecl(VarDeclNode node) {
    StringBuilder sb = new StringBuilder();
    sb.append(currentIndent())
        .append(node.getType().accept(this))
        .append(" ")
        .append(node.getName());
    if (node.hasInitializer()) {
      sb.append(" = ").append(node.getInitializer().accept(this));
    }
    sb.append(";\n");
    return sb.toString();
  }

  @Override
  public String visitFunctionDecl(FunctionDeclNode node) {
    StringBuilder sb = new StringBuilder();
    sb.append(currentIndent())
        .append("fun ")
        .append(node.getReturnType().accept(this))
        .append(" ")
        .append(node.getName())
        .append("(");
    List<ParamNode> params = node.getParams();
    for (int i = 0; i < params.size(); i++) {
      if (i > 0) {
        sb.append(", ");
      }
      sb.append(params.get(i).accept(this));
    }
    sb.append(") ").append(node.getBody().accept(this)).append("\n");
    return sb.toString();
  }

  @Override
  public String visitParam(ParamNode node) {
    return node.getType().accept(this) + " " + node.getName();
  }

  @Override
  public String visitType(TypeNode node) {
    return node.getTypeName();
  }

  @Override
  public String visitBlock(BlockNode node) {
    StringBuilder sb = new StringBuilder();
    sb.append("{\n");
    indentLevel++;
    for (StmtNode stmt : node.getStatements()) {
      sb.append(stmt.accept(this));
    }
    indentLevel--;
    sb.append(currentIndent()).append("}");
    return sb.toString();
  }

  @Override
  public String visitAssign(AssignNode node) {
    return currentIndent() + node.getTargetName() + " = " + node.getValue().accept(this) + ";\n";
  }

  @Override
  public String visitCallStmt(CallStmtNode node) {
    StringBuilder sb = new StringBuilder();
    sb.append(currentIndent()).append(node.getCallee()).append("(");
    List<ExprNode> args = node.getArguments();
    for (int i = 0; i < args.size(); i++) {
      if (i > 0) {
        sb.append(", ");
      }
      sb.append(args.get(i).accept(this));
    }
    sb.append(");\n");
    return sb.toString();
  }

  @Override
  public String visitIf(IfNode node) {
    StringBuilder sb = new StringBuilder();
    sb.append(currentIndent())
        .append("if (")
        .append(node.getCondition().accept(this))
        .append(") ")
        .append(node.getThenBranch().accept(this));
    if (node.hasElseBranch()) {
      sb.append(" else ").append(node.getElseBranch().accept(this));
    }
    sb.append("\n");
    return sb.toString();
  }

  @Override
  public String visitWhile(WhileNode node) {
    return currentIndent()
        + "while ("
        + node.getCondition().accept(this)
        + ") "
        + node.getBody().accept(this)
        + "\n";
  }

  @Override
  public String visitReturn(ReturnNode node) {
    if (node.hasExpression()) {
      return currentIndent() + "return " + node.getExpression().accept(this) + ";\n";
    }
    return currentIndent() + "return;\n";
  }

  @Override
  public String visitBin(BinNode node) {
    return "("
        + node.getLeft().accept(this)
        + " "
        + node.getOperatorSymbol()
        + " "
        + node.getRight().accept(this)
        + ")";
  }

  @Override
  public String visitUnary(UnaryNode node) {
    return "(" + node.getOperatorSymbol() + node.getOperand().accept(this) + ")";
  }

  @Override
  public String visitCallExpr(CallExprNode node) {
    StringBuilder sb = new StringBuilder();
    sb.append(node.getCallee()).append("(");
    List<ExprNode> args = node.getArguments();
    for (int i = 0; i < args.size(); i++) {
      if (i > 0) {
        sb.append(", ");
      }
      sb.append(args.get(i).accept(this));
    }
    sb.append(")");
    return sb.toString();
  }

  @Override
  public String visitId(IdNode node) {
    return node.getName();
  }

  @Override
  public String visitIntLit(IntLitNode node) {
    return node.getRawLexeme();
  }

  @Override
  public String visitDoubleLit(DoubleLitNode node) {
    return node.getRawLexeme();
  }

  @Override
  public String visitStringLit(StringLitNode node) {
    return node.getRawLexeme();
  }

  @Override
  public String visitCharLit(CharLitNode node) {
    return node.getRawLexeme();
  }

  @Override
  public String visitBoolLit(BoolLitNode node) {
    return String.valueOf(node.getValue());
  }
}
