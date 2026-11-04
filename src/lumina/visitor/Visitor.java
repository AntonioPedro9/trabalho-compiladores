package lumina.visitor;

import lumina.ast.AssignNode;
import lumina.ast.BinNode;
import lumina.ast.BlockNode;
import lumina.ast.BoolLitNode;
import lumina.ast.CallExprNode;
import lumina.ast.CallStmtNode;
import lumina.ast.CharLitNode;
import lumina.ast.DoubleLitNode;
import lumina.ast.FunctionDeclNode;
import lumina.ast.IdNode;
import lumina.ast.IfNode;
import lumina.ast.IntLitNode;
import lumina.ast.ParamNode;
import lumina.ast.ProgramNode;
import lumina.ast.ReturnNode;
import lumina.ast.StringLitNode;
import lumina.ast.TypeNode;
import lumina.ast.UnaryNode;
import lumina.ast.VarDeclNode;
import lumina.ast.WhileNode;

/**
 * Interface Visitor parametrizada para travessia da AST da linguagem Lumina via double dispatch.
 * Contém exatamente um método por tipo concreto de nó, sem uso de {@code instanceof} ou {@code
 * switch}.
 *
 * @param <T> tipo de retorno produzido pela operação sobre cada nó
 */
public interface Visitor<T> {

  T visitProgram(ProgramNode node);

  T visitVarDecl(VarDeclNode node);

  T visitFunctionDecl(FunctionDeclNode node);

  T visitParam(ParamNode node);

  T visitType(TypeNode node);

  T visitBlock(BlockNode node);

  T visitAssign(AssignNode node);

  T visitCallStmt(CallStmtNode node);

  T visitIf(IfNode node);

  T visitWhile(WhileNode node);

  T visitReturn(ReturnNode node);

  T visitBin(BinNode node);

  T visitUnary(UnaryNode node);

  T visitCallExpr(CallExprNode node);

  T visitId(IdNode node);

  T visitIntLit(IntLitNode node);

  T visitDoubleLit(DoubleLitNode node);

  T visitStringLit(StringLitNode node);

  T visitCharLit(CharLitNode node);

  T visitBoolLit(BoolLitNode node);
}
