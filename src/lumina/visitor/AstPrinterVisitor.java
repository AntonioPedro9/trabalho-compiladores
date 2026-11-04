package lumina.visitor;

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
 * Impressora textual da AST (Seção 3.2.1). Produz uma representação determinística da árvore, com
 * um nó por linha e indentação de 2 espaços por nível de profundidade.
 */
public final class AstPrinterVisitor implements Visitor<String> {

  private static final String INDENT_STEP = "  ";
  private int depth = 0;

  public String print(Node root) {
    this.depth = 0;
    return root.accept(this);
  }

  private String indent() {
    return INDENT_STEP.repeat(depth);
  }

  private String visitChild(Node child) {
    depth++;
    String rendered = child.accept(this);
    depth--;
    return rendered;
  }

  @Override
  public String visitProgram(ProgramNode node) {
    StringBuilder sb = new StringBuilder();
    sb.append(indent()).append("Program\n");
    for (DeclNode decl : node.getDeclarations()) {
      sb.append(visitChild(decl));
    }
    return sb.toString();
  }

  @Override
  public String visitVarDecl(VarDeclNode node) {
    StringBuilder sb = new StringBuilder();
    sb.append(indent())
        .append("VarDecl(")
        .append(node.getType().getTypeName())
        .append(" ")
        .append(node.getName())
        .append(")\n");
    if (node.hasInitializer()) {
      sb.append(visitChild(node.getInitializer()));
    }
    return sb.toString();
  }

  @Override
  public String visitFunctionDecl(FunctionDeclNode node) {
    StringBuilder sb = new StringBuilder();
    sb.append(indent())
        .append("FunctionDecl(")
        .append(node.getReturnType().getTypeName())
        .append(" ")
        .append(node.getName())
        .append(")\n");
    for (ParamNode param : node.getParams()) {
      sb.append(visitChild(param));
    }
    sb.append(visitChild(node.getBody()));
    return sb.toString();
  }

  @Override
  public String visitParam(ParamNode node) {
    return indent() + "Param(" + node.getType().getTypeName() + " " + node.getName() + ")\n";
  }

  @Override
  public String visitType(TypeNode node) {
    return indent() + "Type(" + node.getTypeName() + ")\n";
  }

  @Override
  public String visitBlock(BlockNode node) {
    StringBuilder sb = new StringBuilder();
    sb.append(indent()).append("Block\n");
    for (StmtNode stmt : node.getStatements()) {
      sb.append(visitChild(stmt));
    }
    return sb.toString();
  }

  @Override
  public String visitAssign(AssignNode node) {
    StringBuilder sb = new StringBuilder();
    sb.append(indent()).append("Assign(").append(node.getTargetName()).append(")\n");
    sb.append(visitChild(node.getValue()));
    return sb.toString();
  }

  @Override
  public String visitCallStmt(CallStmtNode node) {
    StringBuilder sb = new StringBuilder();
    sb.append(indent()).append("CallStmt(").append(node.getCallee()).append(")\n");
    for (ExprNode arg : node.getArguments()) {
      sb.append(visitChild(arg));
    }
    return sb.toString();
  }

  @Override
  public String visitIf(IfNode node) {
    StringBuilder sb = new StringBuilder();
    sb.append(indent()).append("If\n");
    sb.append(visitChild(node.getCondition()));
    sb.append(visitChild(node.getThenBranch()));
    if (node.hasElseBranch()) {
      sb.append(visitChild(node.getElseBranch()));
    }
    return sb.toString();
  }

  @Override
  public String visitWhile(WhileNode node) {
    StringBuilder sb = new StringBuilder();
    sb.append(indent()).append("While\n");
    sb.append(visitChild(node.getCondition()));
    sb.append(visitChild(node.getBody()));
    return sb.toString();
  }

  @Override
  public String visitReturn(ReturnNode node) {
    StringBuilder sb = new StringBuilder();
    sb.append(indent()).append("Return\n");
    if (node.hasExpression()) {
      sb.append(visitChild(node.getExpression()));
    }
    return sb.toString();
  }

  @Override
  public String visitBin(BinNode node) {
    StringBuilder sb = new StringBuilder();
    sb.append(indent()).append("Bin(").append(node.getOperatorSymbol()).append(")\n");
    sb.append(visitChild(node.getLeft()));
    sb.append(visitChild(node.getRight()));
    return sb.toString();
  }

  @Override
  public String visitUnary(UnaryNode node) {
    StringBuilder sb = new StringBuilder();
    sb.append(indent()).append("Unary(").append(node.getOperatorSymbol()).append(")\n");
    sb.append(visitChild(node.getOperand()));
    return sb.toString();
  }

  @Override
  public String visitCallExpr(CallExprNode node) {
    StringBuilder sb = new StringBuilder();
    sb.append(indent()).append("CallExpr(").append(node.getCallee()).append(")\n");
    for (ExprNode arg : node.getArguments()) {
      sb.append(visitChild(arg));
    }
    return sb.toString();
  }

  @Override
  public String visitId(IdNode node) {
    return indent() + "Id(" + node.getName() + ")\n";
  }

  @Override
  public String visitIntLit(IntLitNode node) {
    return indent() + "IntLit(" + node.getRawLexeme() + ")\n";
  }

  @Override
  public String visitDoubleLit(DoubleLitNode node) {
    return indent() + "DoubleLit(" + node.getRawLexeme() + ")\n";
  }

  @Override
  public String visitStringLit(StringLitNode node) {
    return indent() + "StringLit(" + node.getRawLexeme() + ")\n";
  }

  @Override
  public String visitCharLit(CharLitNode node) {
    return indent() + "CharLit(" + node.getRawLexeme() + ")\n";
  }

  @Override
  public String visitBoolLit(BoolLitNode node) {
    return indent() + "BoolLit(" + node.getValue() + ")\n";
  }
}
