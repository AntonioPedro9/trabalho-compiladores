package lumina.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
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
import lumina.ast.ParamNode;
import lumina.ast.ProgramNode;
import lumina.ast.ReturnNode;
import lumina.ast.StmtNode;
import lumina.ast.StringLitNode;
import lumina.ast.TypeNode;
import lumina.ast.UnaryNode;
import lumina.ast.VarDeclNode;
import lumina.ast.WhileNode;
import lumina.common.source.SourcePosition;
import lumina.lexer.Scanner;
import lumina.lexer.token.Token;
import lumina.lexer.token.TokenType;
import lumina.parser.error.SyntaxErrorReporter;

/**
 * Analisador sintático descendente recursivo LL(1) da linguagem Lumina. Constrói a Árvore de
 * Sintaxe Abstrata (AST) durante a descida recursiva, preservando as decisões por FIRST/FOLLOW e a
 * recuperação de erros em modo pânico do Checkpoint 2.
 */
public final class Parser {

  private final Scanner scanner;
  private final SyntaxErrorReporter errorReporter;
  private Token lookahead;

  public Parser(String source) {
    this(new Scanner(source), new SyntaxErrorReporter());
  }

  public Parser(Scanner scanner) {
    this(scanner, new SyntaxErrorReporter());
  }

  public Parser(Scanner scanner, SyntaxErrorReporter errorReporter) {
    this.scanner = Objects.requireNonNull(scanner, "O scanner não pode ser nulo.");
    this.errorReporter =
        Objects.requireNonNull(errorReporter, "O errorReporter não pode ser nulo.");
    this.lookahead = this.scanner.nextToken();
    skipLexicalErrorTokens();
  }

  public ParseResult parse() {
    ProgramNode root = parseProgram();
    if (errorReporter.hasErrors()) {
      return ParseResult.rejected(errorReporter.getErrors());
    }
    return ParseResult.accepted(root);
  }

  // Declarações globais
  // Program ::= DeclList EOF
  public ProgramNode parseProgram() {
    SourcePosition startPos = lookahead.getPosition();
    List<DeclNode> declarations = new ArrayList<>();
    parseDeclList(declarations);
    match(TokenType.EOF);
    return new ProgramNode(startPos, declarations);
  }

  // DeclList ::= Decl DeclList | vazio
  public void parseDeclList(List<DeclNode> declarations) {
    if (isDeclarationStart(lookahead.getType())) {
      DeclNode decl = parseDecl();
      if (decl != null) {
        declarations.add(decl);
      }
      parseDeclList(declarations);
    } else if (isInFollow(FollowSets.DECL_LIST)) {
      // vazio (lookahead em FOLLOW(DeclList) = { EOF })
    } else {
      reportError("Declaração inválida no escopo global: token '" + lookahead.getLexeme() + "'.");
      advance();
      synchronizeToNextDeclaration();
      if (!lookahead.is(TokenType.EOF)) {
        parseDeclList(declarations);
      }
    }
  }

  // Decl ::= VarDecl | FunctionDecl
  public DeclNode parseDecl() {
    if (lookahead.is(TokenType.FUN)) {
      return parseFunctionDecl();
    } else if (isTypeStart(lookahead.getType())) {
      return parseVarDecl();
    } else {
      reportUnexpectedAndSync("declaração de função ('fun') ou variável (tipo)", FollowSets.DECL);
      return null;
    }
  }

  // VarDecl ::= Type IDENTIFIER VarInitOpt ";"
  public VarDeclNode parseVarDecl() {
    SourcePosition startPos = lookahead.getPosition();
    TypeNode type = parseType();
    Token idToken = match(TokenType.IDENTIFIER);
    ExprNode init = parseVarInitOpt();
    match(TokenType.SEMICOLON);
    return new VarDeclNode(startPos, type, idToken.getLexeme(), init);
  }

  // VarInitOpt ::= "=" Expr | vazio
  public ExprNode parseVarInitOpt() {
    if (lookahead.is(TokenType.EQUAL)) {
      match(TokenType.EQUAL);
      return parseExpr();
    } else if (isInFollow(FollowSets.VAR_INIT_OPT)) {
      return null;
    } else {
      reportUnexpectedAndSync("'=' ou ';' na declaração de variável", FollowSets.VAR_INIT_OPT);
      return null;
    }
  }

  // FunctionDecl ::= "fun" ReturnType IDENTIFIER "(" ParamListOpt ")" Block
  public FunctionDeclNode parseFunctionDecl() {
    Token funToken = match(TokenType.FUN);
    TypeNode returnType = parseReturnType();
    Token nameToken = match(TokenType.IDENTIFIER);
    match(TokenType.LEFT_PAREN);
    List<ParamNode> params = parseParamListOpt();
    match(TokenType.RIGHT_PAREN);
    BlockNode body = parseBlock();
    return new FunctionDeclNode(
        funToken.getPosition(), returnType, nameToken.getLexeme(), params, body);
  }

  // ReturnType ::= Type | "void"
  public TypeNode parseReturnType() {
    if (lookahead.is(TokenType.VOID)) {
      Token voidToken = match(TokenType.VOID);
      return new TypeNode(voidToken.getPosition(), TokenType.VOID);
    } else if (isTypeStart(lookahead.getType())) {
      return parseType();
    } else {
      SourcePosition errPos = lookahead.getPosition();
      reportUnexpectedAndSync("tipo de retorno ou 'void'", FollowSets.RETURN_TYPE);
      return new TypeNode(errPos, TokenType.VOID);
    }
  }

  // Type ::= "int" | "double" | "bool" | "char" | "string"
  public TypeNode parseType() {
    SourcePosition pos = lookahead.getPosition();
    if (lookahead.is(TokenType.INT)) {
      match(TokenType.INT);
      return new TypeNode(pos, TokenType.INT);
    } else if (lookahead.is(TokenType.DOUBLE)) {
      match(TokenType.DOUBLE);
      return new TypeNode(pos, TokenType.DOUBLE);
    } else if (lookahead.is(TokenType.BOOL)) {
      match(TokenType.BOOL);
      return new TypeNode(pos, TokenType.BOOL);
    } else if (lookahead.is(TokenType.CHAR)) {
      match(TokenType.CHAR);
      return new TypeNode(pos, TokenType.CHAR);
    } else if (lookahead.is(TokenType.STRING)) {
      match(TokenType.STRING);
      return new TypeNode(pos, TokenType.STRING);
    } else {
      reportUnexpectedAndSync("tipo primitivo (int, double, bool, char, string)", FollowSets.TYPE);
      return new TypeNode(pos, TokenType.INT);
    }
  }

  // ParamListOpt ::= ParamList | vazio
  public List<ParamNode> parseParamListOpt() {
    if (isTypeStart(lookahead.getType())) {
      return parseParamList();
    } else if (isInFollow(FollowSets.PARAM_LIST_OPT) || lookahead.is(TokenType.LEFT_BRACE)) {
      return List.of();
    } else {
      reportUnexpectedAndSync("tipo do parâmetro ou ')'", FollowSets.PARAM_LIST_OPT);
      return List.of();
    }
  }

  // ParamList ::= Param ParamListTail
  public List<ParamNode> parseParamList() {
    List<ParamNode> params = new ArrayList<>();
    ParamNode first = parseParam();
    if (first != null) {
      params.add(first);
    }
    parseParamListTail(params);
    return params;
  }

  // ParamListTail ::= "," Param ParamListTail | vazio
  public void parseParamListTail(List<ParamNode> params) {
    if (lookahead.is(TokenType.COMMA)) {
      match(TokenType.COMMA);
      ParamNode next = parseParam();
      if (next != null) {
        params.add(next);
      }
      parseParamListTail(params);
    } else if (isInFollow(FollowSets.PARAM_LIST_TAIL)) {
      // vazio
    } else {
      reportUnexpectedAndSync("',' ou ')' na lista de parâmetros", FollowSets.PARAM_LIST_TAIL);
    }
  }

  // Param ::= Type IDENTIFIER
  public ParamNode parseParam() {
    SourcePosition startPos = lookahead.getPosition();
    TypeNode type = parseType();
    Token nameToken = match(TokenType.IDENTIFIER);
    return new ParamNode(startPos, type, nameToken.getLexeme());
  }

  // Comandos e blocos
  // Block ::= "{" StmtList "}"
  public BlockNode parseBlock() {
    Token openBrace = match(TokenType.LEFT_BRACE);
    List<StmtNode> statements = new ArrayList<>();
    parseStmtList(statements);
    match(TokenType.RIGHT_BRACE);
    return new BlockNode(openBrace.getPosition(), statements);
  }

  // StmtList ::= Stmt StmtList | vazio
  public void parseStmtList(List<StmtNode> statements) {
    if (isStatementStart(lookahead.getType())) {
      StmtNode stmt = parseStmt();
      if (stmt != null) {
        statements.add(stmt);
      }
      parseStmtList(statements);
    } else if (isInFollow(FollowSets.STMT_LIST)) {
      // vazio (lookahead em FOLLOW(StmtList) = { '}' })
    } else {
      if (lookahead.is(TokenType.EOF)) {
        return;
      }
      reportError("Comando inválido ou token inesperado '" + lookahead.getLexeme() + "'.");
      advance();
      synchronizeToNextStatementOrBlockEnd();
      if (!lookahead.is(TokenType.EOF)) {
        parseStmtList(statements);
      }
    }
  }

  // Stmt ::= VarDecl | AssignOrCallStmt | IfStmt | WhileStmt | ReturnStmt
  public StmtNode parseStmt() {
    if (isTypeStart(lookahead.getType())) {
      return parseVarDecl();
    } else if (lookahead.is(TokenType.IDENTIFIER)) {
      return parseAssignOrCallStmt();
    } else if (lookahead.is(TokenType.IF)) {
      return parseIfStmt();
    } else if (lookahead.is(TokenType.WHILE)) {
      return parseWhileStmt();
    } else if (lookahead.is(TokenType.RETURN)) {
      return parseReturnStmt();
    } else {
      reportUnexpectedAndSync("início de comando válido", FollowSets.STMT);
      return null;
    }
  }

  // AssignOrCallStmt ::= IDENTIFIER AssignOrCallTail ";"
  public StmtNode parseAssignOrCallStmt() {
    Token idToken = match(TokenType.IDENTIFIER);
    StmtNode stmt = parseAssignOrCallTail(idToken);
    match(TokenType.SEMICOLON);
    return stmt;
  }

  // AssignOrCallTail ::= "=" Expr | "(" ArgListOpt ")"
  public StmtNode parseAssignOrCallTail(Token idToken) {
    if (lookahead.is(TokenType.EQUAL)) {
      match(TokenType.EQUAL);
      ExprNode value = parseExpr();
      return new AssignNode(idToken.getPosition(), idToken.getLexeme(), value);
    } else if (lookahead.is(TokenType.LEFT_PAREN)) {
      match(TokenType.LEFT_PAREN);
      List<ExprNode> args = parseArgListOpt();
      match(TokenType.RIGHT_PAREN);
      return new CallStmtNode(idToken.getPosition(), idToken.getLexeme(), args);
    } else {
      reportUnexpectedAndSync("'=' ou '(' após identificador", FollowSets.ASSIGN_OR_CALL_TAIL);
      return new AssignNode(
          idToken.getPosition(), idToken.getLexeme(), new IdNode(idToken.getPosition(), "<erro>"));
    }
  }

  // IfStmt ::= "if" "(" Expr ")" Block ElseOpt
  public IfNode parseIfStmt() {
    Token ifToken = match(TokenType.IF);
    match(TokenType.LEFT_PAREN);
    ExprNode condition = parseExpr();
    match(TokenType.RIGHT_PAREN);
    BlockNode thenBranch = parseBlock();
    BlockNode elseBranch = parseElseOpt();
    return new IfNode(ifToken.getPosition(), condition, thenBranch, elseBranch);
  }

  // ElseOpt ::= "else" Block | vazio
  public BlockNode parseElseOpt() {
    if (lookahead.is(TokenType.ELSE)) {
      match(TokenType.ELSE);
      return parseBlock();
    } else if (isInFollow(FollowSets.ELSE_OPT)) {
      return null;
    } else {
      reportUnexpectedAndSync("ramo 'else' ou próximo comando", FollowSets.ELSE_OPT);
      return null;
    }
  }

  // WhileStmt ::= "while" "(" Expr ")" Block
  public WhileNode parseWhileStmt() {
    Token whileToken = match(TokenType.WHILE);
    match(TokenType.LEFT_PAREN);
    ExprNode condition = parseExpr();
    match(TokenType.RIGHT_PAREN);
    BlockNode body = parseBlock();
    return new WhileNode(whileToken.getPosition(), condition, body);
  }

  // ReturnStmt ::= "return" ExprOpt ";"
  public ReturnNode parseReturnStmt() {
    Token returnToken = match(TokenType.RETURN);
    ExprNode expr = parseExprOpt();
    match(TokenType.SEMICOLON);
    return new ReturnNode(returnToken.getPosition(), expr);
  }

  // ExprOpt ::= Expr | vazio
  public ExprNode parseExprOpt() {
    if (isExpressionStart(lookahead.getType())) {
      return parseExpr();
    } else if (isInFollow(FollowSets.EXPR_OPT)) {
      return null;
    } else {
      reportUnexpectedAndSync("expressão de retorno ou ';'", FollowSets.EXPR_OPT);
      return null;
    }
  }

  // Expressões (com passagem do operando esquerdo herdado para associatividade à esquerda)
  // Expr ::= LogicalAnd ExprTail
  public ExprNode parseExpr() {
    ExprNode left = parseLogicalAnd();
    return parseExprTail(left);
  }

  // ExprTail ::= "||" LogicalAnd ExprTail | vazio
  public ExprNode parseExprTail(ExprNode left) {
    if (lookahead.is(TokenType.PIPE_PIPE)) {
      Token op = match(TokenType.PIPE_PIPE);
      ExprNode right = parseLogicalAnd();
      ExprNode combined = new BinNode(left.getPosition(), op.getType(), left, right);
      return parseExprTail(combined);
    } else if (isInFollow(FollowSets.EXPR_TAIL) || isExpressionAnchor(lookahead.getType())) {
      return left;
    } else {
      reportUnexpectedAndSync("operador '||' ou delimitador", FollowSets.EXPR_TAIL);
      return left;
    }
  }

  // LogicalAnd ::= Equality LogicalAndTail
  public ExprNode parseLogicalAnd() {
    ExprNode left = parseEquality();
    return parseLogicalAndTail(left);
  }

  // LogicalAndTail ::= "&&" Equality LogicalAndTail | vazio
  public ExprNode parseLogicalAndTail(ExprNode left) {
    if (lookahead.is(TokenType.AMPERSAND_AMPERSAND)) {
      Token op = match(TokenType.AMPERSAND_AMPERSAND);
      ExprNode right = parseEquality();
      ExprNode combined = new BinNode(left.getPosition(), op.getType(), left, right);
      return parseLogicalAndTail(combined);
    } else if (isInFollow(FollowSets.LOGICAL_AND_TAIL) || isExpressionAnchor(lookahead.getType())) {
      return left;
    } else {
      reportUnexpectedAndSync("operador '&&' ou delimitador", FollowSets.LOGICAL_AND_TAIL);
      return left;
    }
  }

  // Equality ::= Relational EqualityTail
  public ExprNode parseEquality() {
    ExprNode left = parseRelational();
    return parseEqualityTail(left);
  }

  // EqualityTail ::= EqOp Relational EqualityTail | vazio
  public ExprNode parseEqualityTail(ExprNode left) {
    if (isEqualityOperator(lookahead.getType())) {
      TokenType op = parseEqOp();
      ExprNode right = parseRelational();
      ExprNode combined = new BinNode(left.getPosition(), op, left, right);
      return parseEqualityTail(combined);
    } else if (isInFollow(FollowSets.EQUALITY_TAIL) || isExpressionAnchor(lookahead.getType())) {
      return left;
    } else {
      reportUnexpectedAndSync("operador de igualdade ou delimitador", FollowSets.EQUALITY_TAIL);
      return left;
    }
  }

  // EqOp ::= "==" | "!="
  public TokenType parseEqOp() {
    if (lookahead.is(TokenType.EQUAL_EQUAL)) {
      return match(TokenType.EQUAL_EQUAL).getType();
    } else if (lookahead.is(TokenType.BANG_EQUAL)) {
      return match(TokenType.BANG_EQUAL).getType();
    } else {
      reportUnexpectedAndSync("operador de igualdade ('==' ou '!=')", FollowSets.EQ_OP);
      return TokenType.EQUAL_EQUAL;
    }
  }

  // Relational ::= Additive RelationalTail
  public ExprNode parseRelational() {
    ExprNode left = parseAdditive();
    return parseRelationalTail(left);
  }

  // RelationalTail ::= RelOp Additive RelationalTail | vazio
  public ExprNode parseRelationalTail(ExprNode left) {
    if (isRelationalOperator(lookahead.getType())) {
      TokenType op = parseRelOp();
      ExprNode right = parseAdditive();
      ExprNode combined = new BinNode(left.getPosition(), op, left, right);
      return parseRelationalTail(combined);
    } else if (isInFollow(FollowSets.RELATIONAL_TAIL) || isExpressionAnchor(lookahead.getType())) {
      return left;
    } else {
      reportUnexpectedAndSync("operador relacional ou delimitador", FollowSets.RELATIONAL_TAIL);
      return left;
    }
  }

  // RelOp ::= "<" | "<=" | ">" | ">="
  public TokenType parseRelOp() {
    if (lookahead.is(TokenType.LESS)) {
      return match(TokenType.LESS).getType();
    } else if (lookahead.is(TokenType.LESS_EQUAL)) {
      return match(TokenType.LESS_EQUAL).getType();
    } else if (lookahead.is(TokenType.GREATER)) {
      return match(TokenType.GREATER).getType();
    } else if (lookahead.is(TokenType.GREATER_EQUAL)) {
      return match(TokenType.GREATER_EQUAL).getType();
    } else {
      reportUnexpectedAndSync("operador relacional ('<', '<=', '>' ou '>=')", FollowSets.REL_OP);
      return TokenType.LESS;
    }
  }

  // Additive ::= Multiplicative AdditiveTail
  public ExprNode parseAdditive() {
    ExprNode left = parseMultiplicative();
    return parseAdditiveTail(left);
  }

  // AdditiveTail ::= AddOp Multiplicative AdditiveTail | vazio
  public ExprNode parseAdditiveTail(ExprNode left) {
    if (isAdditiveOperator(lookahead.getType())) {
      TokenType op = parseAddOp();
      ExprNode right = parseMultiplicative();
      ExprNode combined = new BinNode(left.getPosition(), op, left, right);
      return parseAdditiveTail(combined);
    } else if (isInFollow(FollowSets.ADDITIVE_TAIL) || isExpressionAnchor(lookahead.getType())) {
      return left;
    } else {
      reportUnexpectedAndSync("operador aditivo ('+' ou '-')", FollowSets.ADDITIVE_TAIL);
      return left;
    }
  }

  // AddOp ::= "+" | "-"
  public TokenType parseAddOp() {
    if (lookahead.is(TokenType.PLUS)) {
      return match(TokenType.PLUS).getType();
    } else if (lookahead.is(TokenType.MINUS)) {
      return match(TokenType.MINUS).getType();
    } else {
      reportUnexpectedAndSync("operador aditivo ('+' ou '-')", FollowSets.ADD_OP);
      return TokenType.PLUS;
    }
  }

  // Multiplicative ::= Unary MultiplicativeTail
  public ExprNode parseMultiplicative() {
    ExprNode left = parseUnary();
    return parseMultiplicativeTail(left);
  }

  // MultiplicativeTail ::= MulOp Unary MultiplicativeTail | vazio
  public ExprNode parseMultiplicativeTail(ExprNode left) {
    if (isMultiplicativeOperator(lookahead.getType())) {
      TokenType op = parseMulOp();
      ExprNode right = parseUnary();
      ExprNode combined = new BinNode(left.getPosition(), op, left, right);
      return parseMultiplicativeTail(combined);
    } else if (isInFollow(FollowSets.MULTIPLICATIVE_TAIL)
        || isExpressionAnchor(lookahead.getType())) {
      return left;
    } else {
      reportUnexpectedAndSync(
          "operador multiplicativo ('*', '/' ou '%')", FollowSets.MULTIPLICATIVE_TAIL);
      return left;
    }
  }

  // MulOp ::= "*" | "/" | "%"
  public TokenType parseMulOp() {
    if (lookahead.is(TokenType.STAR)) {
      return match(TokenType.STAR).getType();
    } else if (lookahead.is(TokenType.SLASH)) {
      return match(TokenType.SLASH).getType();
    } else if (lookahead.is(TokenType.PERCENT)) {
      return match(TokenType.PERCENT).getType();
    } else {
      reportUnexpectedAndSync("operador multiplicativo ('*', '/' ou '%')", FollowSets.MUL_OP);
      return TokenType.STAR;
    }
  }

  // Unary ::= "!" Unary | "-" Unary | "+" Unary | Primary
  public ExprNode parseUnary() {
    if (lookahead.is(TokenType.BANG)) {
      Token op = match(TokenType.BANG);
      ExprNode operand = parseUnary();
      return new UnaryNode(op.getPosition(), TokenType.BANG, operand);
    } else if (lookahead.is(TokenType.MINUS)) {
      Token op = match(TokenType.MINUS);
      ExprNode operand = parseUnary();
      return new UnaryNode(op.getPosition(), TokenType.MINUS, operand);
    } else if (lookahead.is(TokenType.PLUS)) {
      Token op = match(TokenType.PLUS);
      ExprNode operand = parseUnary();
      return new UnaryNode(op.getPosition(), TokenType.PLUS, operand);
    } else if (isPrimaryStart(lookahead.getType())) {
      return parsePrimary();
    } else {
      SourcePosition errPos = lookahead.getPosition();
      reportUnexpectedAndSync("operando ou operador unário ('!', '-' ou '+')", FollowSets.UNARY);
      return new IdNode(errPos, "<erro>");
    }
  }

  // Primary ::= IDENTIFIER PrimaryTail | INT_LITERAL | DOUBLE_LITERAL | STRING_LITERAL |
  // CHAR_LITERAL | "true" | "false" | "(" Expr ")"
  public ExprNode parsePrimary() {
    if (lookahead.is(TokenType.IDENTIFIER)) {
      Token idToken = match(TokenType.IDENTIFIER);
      return parsePrimaryTail(idToken);
    } else if (lookahead.is(TokenType.INT_LITERAL)) {
      Token tok = match(TokenType.INT_LITERAL);
      int val = 0;
      try {
        val = Integer.parseInt(tok.getLexeme());
      } catch (NumberFormatException ignored) {
        // preservado pelo lexema bruto
      }
      return new IntLitNode(tok.getPosition(), val, tok.getLexeme());
    } else if (lookahead.is(TokenType.DOUBLE_LITERAL)) {
      Token tok = match(TokenType.DOUBLE_LITERAL);
      double val = 0.0;
      try {
        val = Double.parseDouble(tok.getLexeme());
      } catch (NumberFormatException ignored) {
        // preservado pelo lexema bruto
      }
      return new DoubleLitNode(tok.getPosition(), val, tok.getLexeme());
    } else if (lookahead.is(TokenType.STRING_LITERAL)) {
      Token tok = match(TokenType.STRING_LITERAL);
      return new StringLitNode(tok.getPosition(), tok.getLexeme());
    } else if (lookahead.is(TokenType.CHAR_LITERAL)) {
      Token tok = match(TokenType.CHAR_LITERAL);
      return new CharLitNode(tok.getPosition(), tok.getLexeme());
    } else if (lookahead.is(TokenType.TRUE)) {
      Token tok = match(TokenType.TRUE);
      return new BoolLitNode(tok.getPosition(), true);
    } else if (lookahead.is(TokenType.FALSE)) {
      Token tok = match(TokenType.FALSE);
      return new BoolLitNode(tok.getPosition(), false);
    } else if (lookahead.is(TokenType.LEFT_PAREN)) {
      match(TokenType.LEFT_PAREN);
      ExprNode inner = parseExpr();
      match(TokenType.RIGHT_PAREN);
      return inner;
    } else {
      SourcePosition errPos = lookahead.getPosition();
      reportUnexpectedAndSync("literal, identificador ou '('", FollowSets.PRIMARY);
      return new IdNode(errPos, "<erro>");
    }
  }

  // PrimaryTail ::= "(" ArgListOpt ")" | vazio
  public ExprNode parsePrimaryTail(Token idToken) {
    if (lookahead.is(TokenType.LEFT_PAREN)) {
      match(TokenType.LEFT_PAREN);
      List<ExprNode> args = parseArgListOpt();
      match(TokenType.RIGHT_PAREN);
      return new CallExprNode(idToken.getPosition(), idToken.getLexeme(), args);
    } else if (isInFollow(FollowSets.PRIMARY_TAIL) || isExpressionAnchor(lookahead.getType())) {
      return new IdNode(idToken.getPosition(), idToken.getLexeme());
    } else {
      reportUnexpectedAndSync("'(' ou delimitador após identificador", FollowSets.PRIMARY_TAIL);
      return new IdNode(idToken.getPosition(), idToken.getLexeme());
    }
  }

  // ArgListOpt ::= ArgList | vazio
  public List<ExprNode> parseArgListOpt() {
    if (isExpressionStart(lookahead.getType())) {
      return parseArgList();
    } else if (isInFollow(FollowSets.ARG_LIST_OPT)) {
      return List.of();
    } else {
      reportUnexpectedAndSync("argumento ou ')'", FollowSets.ARG_LIST_OPT);
      return List.of();
    }
  }

  // ArgList ::= Expr ArgListTail
  public List<ExprNode> parseArgList() {
    List<ExprNode> args = new ArrayList<>();
    args.add(parseExpr());
    parseArgListTail(args);
    return args;
  }

  // ArgListTail ::= "," Expr ArgListTail | vazio
  public void parseArgListTail(List<ExprNode> args) {
    if (lookahead.is(TokenType.COMMA)) {
      match(TokenType.COMMA);
      args.add(parseExpr());
      parseArgListTail(args);
    } else if (isInFollow(FollowSets.ARG_LIST_TAIL)) {
      // vazio
    } else {
      reportUnexpectedAndSync("',' ou ')' na lista de argumentos", FollowSets.ARG_LIST_TAIL);
    }
  }

  // Consumo de tokens e recuperação em modo pânico
  private Token match(TokenType expected) {
    Token current = lookahead;
    if (lookahead.is(expected)) {
      advance();
      return current;
    }
    reportError(
        String.format(
            "Esperava '%s' (%s), mas encontrou '%s'.",
            expected.getDescription(), expected.name(), lookahead.getLexeme()));
    return new Token(expected, expected.getDescription(), current.getPosition());
  }

  private void advance() {
    lookahead = scanner.nextToken();
    skipLexicalErrorTokens();
  }

  private void skipLexicalErrorTokens() {
    while (lookahead.is(TokenType.ERROR)) {
      reportError("Erro léxico ignorado: '" + lookahead.getLexeme() + "'.");
      lookahead = scanner.nextToken();
    }
  }

  private void reportUnexpectedAndSync(String expectedDescription, Set<TokenType> syncSet) {
    reportError(
        "Esperava " + expectedDescription + ", mas encontrou '" + lookahead.getLexeme() + "'.");
    synchronize(syncSet);
  }

  private void synchronize(Set<TokenType> syncSet) {
    while (!lookahead.is(TokenType.EOF) && !syncSet.contains(lookahead.getType())) {
      advance();
    }
  }

  private void synchronizeToNextDeclaration() {
    while (!lookahead.is(TokenType.EOF)) {
      if (isDeclarationStart(lookahead.getType())) {
        return;
      }
      advance();
    }
  }

  private void synchronizeToNextStatementOrBlockEnd() {
    while (!lookahead.is(TokenType.EOF)) {
      if (lookahead.is(TokenType.SEMICOLON)) {
        advance();
        return;
      }
      if (lookahead.is(TokenType.RIGHT_BRACE) || isStatementStart(lookahead.getType())) {
        return;
      }
      advance();
    }
  }

  private void reportError(String message) {
    errorReporter.report(message, lookahead);
  }

  // Métodos auxiliares de verificação
  private boolean isInFollow(Set<TokenType> followSet) {
    return followSet.contains(lookahead.getType());
  }

  private boolean isDeclarationStart(TokenType type) {
    return type == TokenType.FUN || isTypeStart(type);
  }

  private boolean isTypeStart(TokenType type) {
    return type == TokenType.INT
        || type == TokenType.DOUBLE
        || type == TokenType.BOOL
        || type == TokenType.CHAR
        || type == TokenType.STRING;
  }

  private boolean isStatementStart(TokenType type) {
    return isTypeStart(type)
        || type == TokenType.IDENTIFIER
        || type == TokenType.IF
        || type == TokenType.WHILE
        || type == TokenType.RETURN;
  }

  private boolean isExpressionStart(TokenType type) {
    return isPrimaryStart(type)
        || type == TokenType.BANG
        || type == TokenType.MINUS
        || type == TokenType.PLUS;
  }

  private boolean isPrimaryStart(TokenType type) {
    return type == TokenType.IDENTIFIER
        || type == TokenType.INT_LITERAL
        || type == TokenType.DOUBLE_LITERAL
        || type == TokenType.STRING_LITERAL
        || type == TokenType.CHAR_LITERAL
        || type == TokenType.TRUE
        || type == TokenType.FALSE
        || type == TokenType.LEFT_PAREN;
  }

  private boolean isEqualityOperator(TokenType type) {
    return type == TokenType.EQUAL_EQUAL || type == TokenType.BANG_EQUAL;
  }

  private boolean isRelationalOperator(TokenType type) {
    return type == TokenType.LESS
        || type == TokenType.LESS_EQUAL
        || type == TokenType.GREATER
        || type == TokenType.GREATER_EQUAL;
  }

  private boolean isAdditiveOperator(TokenType type) {
    return type == TokenType.PLUS || type == TokenType.MINUS;
  }

  private boolean isMultiplicativeOperator(TokenType type) {
    return type == TokenType.STAR || type == TokenType.SLASH || type == TokenType.PERCENT;
  }

  private boolean isExpressionAnchor(TokenType type) {
    return isStatementStart(type)
        || type == TokenType.RIGHT_BRACE
        || type == TokenType.LEFT_BRACE
        || type == TokenType.EOF;
  }

  public SyntaxErrorReporter getErrorReporter() {
    return errorReporter;
  }
}
