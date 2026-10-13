package lumina.parser;

import java.util.Objects;
import java.util.Set;
import lumina.lexer.Scanner;
import lumina.lexer.token.Token;
import lumina.lexer.token.TokenType;
import lumina.parser.error.SyntaxErrorReporter;

/** Analisador sintático descendente recursivo para a linguagem Lumina (LL(1)). */
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
    parseProgram();
    return new ParseResult(!errorReporter.hasErrors(), errorReporter.getErrors());
  }

  // Declarações globais
  // Program ::= DeclList EOF
  public void parseProgram() {
    parseDeclList();
    match(TokenType.EOF);
  }

  // DeclList ::= Decl DeclList | vazio
  public void parseDeclList() {
    if (lookahead.is(TokenType.EOF)) {
      return;
    }
    if (isDeclarationStart(lookahead.getType())) {
      parseDecl();
      parseDeclList();
    } else if (isInFollow(FollowSets.DECL_LIST)) {
      // vazio
    } else {
      reportError("Declaração inválida no escopo global: token '" + lookahead.getLexeme() + "'.");
      advance();
      synchronizeToNextDeclaration();
      if (!lookahead.is(TokenType.EOF)) {
        parseDeclList();
      }
    }
  }

  // Decl ::= VarDecl | FunctionDecl
  public void parseDecl() {
    if (lookahead.is(TokenType.FUN)) {
      parseFunctionDecl();
    } else if (isTypeStart(lookahead.getType())) {
      parseVarDecl();
    } else {
      reportUnexpectedAndSync("declaração de função ('fun') ou variável (tipo)", FollowSets.DECL);
    }
  }

  // VarDecl ::= Type IDENTIFIER VarInitOpt ";"
  public void parseVarDecl() {
    parseType();
    match(TokenType.IDENTIFIER);
    parseVarInitOpt();
    match(TokenType.SEMICOLON);
  }

  // VarInitOpt ::= "=" Expr | vazio
  public void parseVarInitOpt() {
    if (lookahead.is(TokenType.EQUAL)) {
      match(TokenType.EQUAL);
      parseExpr();
    } else if (isInFollow(FollowSets.VAR_INIT_OPT)) {
      // vazio
    } else {
      reportUnexpectedAndSync("'=' ou ';' na declaração de variável", FollowSets.VAR_INIT_OPT);
    }
  }

  // FunctionDecl ::= "fun" ReturnType IDENTIFIER "(" ParamListOpt ")" Block
  public void parseFunctionDecl() {
    match(TokenType.FUN);
    parseReturnType();
    match(TokenType.IDENTIFIER);
    match(TokenType.LEFT_PAREN);
    parseParamListOpt();
    match(TokenType.RIGHT_PAREN);
    parseBlock();
  }

  // ReturnType ::= Type | "void"
  public void parseReturnType() {
    if (lookahead.is(TokenType.VOID)) {
      match(TokenType.VOID);
    } else if (isTypeStart(lookahead.getType())) {
      parseType();
    } else {
      reportUnexpectedAndSync("tipo de retorno ou 'void'", FollowSets.RETURN_TYPE);
    }
  }

  // Type ::= "int" | "double" | "bool" | "char" | "string"
  public void parseType() {
    if (lookahead.is(TokenType.INT)) {
      match(TokenType.INT);
    } else if (lookahead.is(TokenType.DOUBLE)) {
      match(TokenType.DOUBLE);
    } else if (lookahead.is(TokenType.BOOL)) {
      match(TokenType.BOOL);
    } else if (lookahead.is(TokenType.CHAR)) {
      match(TokenType.CHAR);
    } else if (lookahead.is(TokenType.STRING)) {
      match(TokenType.STRING);
    } else {
      reportUnexpectedAndSync("tipo primitivo (int, double, bool, char, string)", FollowSets.TYPE);
    }
  }

  // ParamListOpt ::= ParamList | vazio
  public void parseParamListOpt() {
    if (isTypeStart(lookahead.getType())) {
      parseParamList();
    } else if (isInFollow(FollowSets.PARAM_LIST_OPT)) {
      // vazio
    } else {
      reportUnexpectedAndSync("tipo do parâmetro ou ')'", FollowSets.PARAM_LIST_OPT);
    }
  }

  // ParamList ::= Param ParamListTail
  public void parseParamList() {
    parseParam();
    parseParamListTail();
  }

  // ParamListTail ::= "," Param ParamListTail | vazio
  public void parseParamListTail() {
    if (lookahead.is(TokenType.COMMA)) {
      match(TokenType.COMMA);
      parseParam();
      parseParamListTail();
    } else if (isInFollow(FollowSets.PARAM_LIST_TAIL)) {
      // vazio
    } else {
      reportUnexpectedAndSync("',' ou ')' na lista de parâmetros", FollowSets.PARAM_LIST_TAIL);
    }
  }

  // Param ::= Type IDENTIFIER
  public void parseParam() {
    parseType();
    match(TokenType.IDENTIFIER);
  }

  // Comandos e blocos
  // Block ::= "{" StmtList "}"
  public void parseBlock() {
    match(TokenType.LEFT_BRACE);
    parseStmtList();
    match(TokenType.RIGHT_BRACE);
  }

  // StmtList ::= Stmt StmtList | vazio
  public void parseStmtList() {
    if (lookahead.is(TokenType.EOF) || isInFollow(FollowSets.STMT_LIST)) {
      return;
    }
    if (isStatementStart(lookahead.getType())) {
      parseStmt();
      parseStmtList();
    } else {
      reportError("Comando inválido ou token inesperado '" + lookahead.getLexeme() + "'.");
      advance();
      synchronizeToNextStatementOrBlockEnd();
      if (!lookahead.is(TokenType.EOF)) {
        parseStmtList();
      }
    }
  }

  // Stmt ::= VarDecl | AssignOrCallStmt | IfStmt | WhileStmt | ReturnStmt
  public void parseStmt() {
    if (isTypeStart(lookahead.getType())) {
      parseVarDecl();
    } else if (lookahead.is(TokenType.IDENTIFIER)) {
      parseAssignOrCallStmt();
    } else if (lookahead.is(TokenType.IF)) {
      parseIfStmt();
    } else if (lookahead.is(TokenType.WHILE)) {
      parseWhileStmt();
    } else if (lookahead.is(TokenType.RETURN)) {
      parseReturnStmt();
    } else {
      reportUnexpectedAndSync("início de comando válido", FollowSets.STMT);
    }
  }

  // AssignOrCallStmt ::= IDENTIFIER AssignOrCallTail ";"
  public void parseAssignOrCallStmt() {
    match(TokenType.IDENTIFIER);
    parseAssignOrCallTail();
    match(TokenType.SEMICOLON);
  }

  // AssignOrCallTail ::= "=" Expr | "(" ArgListOpt ")"
  public void parseAssignOrCallTail() {
    if (lookahead.is(TokenType.EQUAL)) {
      match(TokenType.EQUAL);
      parseExpr();
    } else if (lookahead.is(TokenType.LEFT_PAREN)) {
      match(TokenType.LEFT_PAREN);
      parseArgListOpt();
      match(TokenType.RIGHT_PAREN);
    } else {
      reportUnexpectedAndSync("'=' ou '(' após identificador", FollowSets.ASSIGN_OR_CALL_TAIL);
    }
  }

  // IfStmt ::= "if" "(" Expr ")" Block ElseOpt
  public void parseIfStmt() {
    match(TokenType.IF);
    match(TokenType.LEFT_PAREN);
    parseExpr();
    match(TokenType.RIGHT_PAREN);
    parseBlock();
    parseElseOpt();
  }

  // ElseOpt ::= "else" Block | vazio
  public void parseElseOpt() {
    if (lookahead.is(TokenType.ELSE)) {
      match(TokenType.ELSE);
      parseBlock();
    } else if (isInFollow(FollowSets.ELSE_OPT)) {
      // vazio
    } else {
      reportUnexpectedAndSync("ramo 'else' ou próximo comando", FollowSets.ELSE_OPT);
    }
  }

  // WhileStmt ::= "while" "(" Expr ")" Block
  public void parseWhileStmt() {
    match(TokenType.WHILE);
    match(TokenType.LEFT_PAREN);
    parseExpr();
    match(TokenType.RIGHT_PAREN);
    parseBlock();
  }

  // ReturnStmt ::= "return" ExprOpt ";"
  public void parseReturnStmt() {
    match(TokenType.RETURN);
    parseExprOpt();
    match(TokenType.SEMICOLON);
  }

  // ExprOpt ::= Expr | vazio
  public void parseExprOpt() {
    if (isExpressionStart(lookahead.getType())) {
      parseExpr();
    } else if (isInFollow(FollowSets.EXPR_OPT)) {
      // vazio
    } else {
      reportUnexpectedAndSync("expressão de retorno ou ';'", FollowSets.EXPR_OPT);
    }
  }

  // Expressões
  // Expr ::= LogicalAnd ExprTail
  public void parseExpr() {
    parseLogicalAnd();
    parseExprTail();
  }

  // ExprTail ::= "||" LogicalAnd ExprTail | vazio
  public void parseExprTail() {
    if (lookahead.is(TokenType.PIPE_PIPE)) {
      match(TokenType.PIPE_PIPE);
      parseLogicalAnd();
      parseExprTail();
    } else if (isExpressionTailEnd(FollowSets.EXPR_TAIL)) {
      // vazio
    } else {
      reportUnexpectedAndSync("operador '||' ou delimitador", FollowSets.EXPR_TAIL);
    }
  }

  // LogicalAnd ::= Equality LogicalAndTail
  public void parseLogicalAnd() {
    parseEquality();
    parseLogicalAndTail();
  }

  // LogicalAndTail ::= "&&" Equality LogicalAndTail | vazio
  public void parseLogicalAndTail() {
    if (lookahead.is(TokenType.AMPERSAND_AMPERSAND)) {
      match(TokenType.AMPERSAND_AMPERSAND);
      parseEquality();
      parseLogicalAndTail();
    } else if (isExpressionTailEnd(FollowSets.LOGICAL_AND_TAIL)) {
      // vazio
    } else {
      reportUnexpectedAndSync("operador '&&' ou delimitador", FollowSets.LOGICAL_AND_TAIL);
    }
  }

  // Equality ::= Relational EqualityTail
  public void parseEquality() {
    parseRelational();
    parseEqualityTail();
  }

  // EqualityTail ::= EqOp Relational EqualityTail | vazio
  public void parseEqualityTail() {
    if (isEqualityOperator(lookahead.getType())) {
      parseEqOp();
      parseRelational();
      parseEqualityTail();
    } else if (isExpressionTailEnd(FollowSets.EQUALITY_TAIL)) {
      // vazio
    } else {
      reportUnexpectedAndSync("operador de igualdade ou delimitador", FollowSets.EQUALITY_TAIL);
    }
  }

  // EqOp ::= "==" | "!="
  public void parseEqOp() {
    if (lookahead.is(TokenType.EQUAL_EQUAL)) {
      match(TokenType.EQUAL_EQUAL);
    } else if (lookahead.is(TokenType.BANG_EQUAL)) {
      match(TokenType.BANG_EQUAL);
    } else {
      reportUnexpectedAndSync("operador de igualdade ('==' ou '!=')", FollowSets.RELATIONAL);
    }
  }

  // Relational ::= Additive RelationalTail
  public void parseRelational() {
    parseAdditive();
    parseRelationalTail();
  }

  // RelationalTail ::= RelOp Additive RelationalTail | vazio
  public void parseRelationalTail() {
    if (isRelationalOperator(lookahead.getType())) {
      parseRelOp();
      parseAdditive();
      parseRelationalTail();
    } else if (isExpressionTailEnd(FollowSets.RELATIONAL_TAIL)) {
      // vazio
    } else {
      reportUnexpectedAndSync("operador relacional ou delimitador", FollowSets.RELATIONAL_TAIL);
    }
  }

  // RelOp ::= "<" | "<=" | ">" | ">="
  public void parseRelOp() {
    if (lookahead.is(TokenType.LESS)) {
      match(TokenType.LESS);
    } else if (lookahead.is(TokenType.LESS_EQUAL)) {
      match(TokenType.LESS_EQUAL);
    } else if (lookahead.is(TokenType.GREATER)) {
      match(TokenType.GREATER);
    } else if (lookahead.is(TokenType.GREATER_EQUAL)) {
      match(TokenType.GREATER_EQUAL);
    } else {
      reportUnexpectedAndSync("operador relacional ('<', '<=', '>' ou '>=')", FollowSets.ADDITIVE);
    }
  }

  // Additive ::= Multiplicative AdditiveTail
  public void parseAdditive() {
    parseMultiplicative();
    parseAdditiveTail();
  }

  // AdditiveTail ::= AddOp Multiplicative AdditiveTail | vazio
  public void parseAdditiveTail() {
    if (isAdditiveOperator(lookahead.getType())) {
      parseAddOp();
      parseMultiplicative();
      parseAdditiveTail();
    } else if (isExpressionTailEnd(FollowSets.ADDITIVE_TAIL)) {
      // vazio
    } else {
      reportUnexpectedAndSync("operador aditivo ('+' ou '-')", FollowSets.ADDITIVE_TAIL);
    }
  }

  // AddOp ::= "+" | "-"
  public void parseAddOp() {
    if (lookahead.is(TokenType.PLUS)) {
      match(TokenType.PLUS);
    } else if (lookahead.is(TokenType.MINUS)) {
      match(TokenType.MINUS);
    } else {
      reportUnexpectedAndSync("operador aditivo ('+' ou '-')", FollowSets.MULTIPLICATIVE);
    }
  }

  // Multiplicative ::= Unary MultiplicativeTail
  public void parseMultiplicative() {
    parseUnary();
    parseMultiplicativeTail();
  }

  // MultiplicativeTail ::= MulOp Unary MultiplicativeTail | vazio
  public void parseMultiplicativeTail() {
    if (isMultiplicativeOperator(lookahead.getType())) {
      parseMulOp();
      parseUnary();
      parseMultiplicativeTail();
    } else if (isExpressionTailEnd(FollowSets.MULTIPLICATIVE_TAIL)) {
      // vazio
    } else {
      reportUnexpectedAndSync(
          "operador multiplicativo ('*', '/' ou '%')", FollowSets.MULTIPLICATIVE_TAIL);
    }
  }

  // MulOp ::= "*" | "/" | "%"
  public void parseMulOp() {
    if (lookahead.is(TokenType.STAR)) {
      match(TokenType.STAR);
    } else if (lookahead.is(TokenType.SLASH)) {
      match(TokenType.SLASH);
    } else if (lookahead.is(TokenType.PERCENT)) {
      match(TokenType.PERCENT);
    } else {
      reportUnexpectedAndSync("operador multiplicativo ('*', '/' ou '%')", FollowSets.UNARY);
    }
  }

  // Unary ::= "!" Unary | "-" Unary | "+" Unary | Primary
  public void parseUnary() {
    if (lookahead.is(TokenType.BANG)) {
      match(TokenType.BANG);
      parseUnary();
    } else if (lookahead.is(TokenType.MINUS)) {
      match(TokenType.MINUS);
      parseUnary();
    } else if (lookahead.is(TokenType.PLUS)) {
      match(TokenType.PLUS);
      parseUnary();
    } else if (isPrimaryStart(lookahead.getType())) {
      parsePrimary();
    } else {
      reportUnexpectedAndSync("operando ou operador unário ('!', '-' ou '+')", FollowSets.UNARY);
    }
  }

  // Primary ::= IDENTIFIER PrimaryTail | INT_LITERAL | DOUBLE_LITERAL | STRING_LITERAL |
  // CHAR_LITERAL | "true" | "false" | "(" Expr ")"
  public void parsePrimary() {
    if (lookahead.is(TokenType.IDENTIFIER)) {
      match(TokenType.IDENTIFIER);
      parsePrimaryTail();
    } else if (lookahead.is(TokenType.INT_LITERAL)) {
      match(TokenType.INT_LITERAL);
    } else if (lookahead.is(TokenType.DOUBLE_LITERAL)) {
      match(TokenType.DOUBLE_LITERAL);
    } else if (lookahead.is(TokenType.STRING_LITERAL)) {
      match(TokenType.STRING_LITERAL);
    } else if (lookahead.is(TokenType.CHAR_LITERAL)) {
      match(TokenType.CHAR_LITERAL);
    } else if (lookahead.is(TokenType.TRUE)) {
      match(TokenType.TRUE);
    } else if (lookahead.is(TokenType.FALSE)) {
      match(TokenType.FALSE);
    } else if (lookahead.is(TokenType.LEFT_PAREN)) {
      match(TokenType.LEFT_PAREN);
      parseExpr();
      match(TokenType.RIGHT_PAREN);
    } else {
      reportUnexpectedAndSync("literal, identificador ou '('", FollowSets.PRIMARY);
    }
  }

  // PrimaryTail ::= "(" ArgListOpt ")" | vazio
  public void parsePrimaryTail() {
    if (lookahead.is(TokenType.LEFT_PAREN)) {
      match(TokenType.LEFT_PAREN);
      parseArgListOpt();
      match(TokenType.RIGHT_PAREN);
    } else if (isExpressionTailEnd(FollowSets.PRIMARY_TAIL)) {
      // vazio
    } else {
      reportUnexpectedAndSync("'(' ou delimitador após identificador", FollowSets.PRIMARY_TAIL);
    }
  }

  // ArgListOpt ::= ArgList | vazio
  public void parseArgListOpt() {
    if (isExpressionStart(lookahead.getType())) {
      parseArgList();
    } else if (isInFollow(FollowSets.ARG_LIST_OPT)) {
      // vazio
    } else {
      reportUnexpectedAndSync("argumento ou ')'", FollowSets.ARG_LIST_OPT);
    }
  }

  // ArgList ::= Expr ArgListTail
  public void parseArgList() {
    parseExpr();
    parseArgListTail();
  }

  // ArgListTail ::= "," Expr ArgListTail | vazio
  public void parseArgListTail() {
    if (lookahead.is(TokenType.COMMA)) {
      match(TokenType.COMMA);
      parseExpr();
      parseArgListTail();
    } else if (isInFollow(FollowSets.ARG_LIST_TAIL)) {
      // vazio
    } else {
      reportUnexpectedAndSync("',' ou ')' na lista de argumentos", FollowSets.ARG_LIST_TAIL);
    }
  }

  // Consumo de tokens e recuperação em modo pânico
  private void match(TokenType expected) {
    if (lookahead.is(expected)) {
      advance();
      return;
    }
    reportError(
        String.format(
            "Esperava '%s' (%s), mas encontrou '%s'.",
            expected.getDescription(), expected.name(), lookahead.getLexeme()));
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

  private boolean isExpressionTailEnd(Set<TokenType> followSet) {
    return followSet.contains(lookahead.getType()) || isExpressionAnchor(lookahead.getType());
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
