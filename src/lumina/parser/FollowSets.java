package lumina.parser;

import java.util.Set;
import lumina.lexer.token.TokenType;

/** Conjuntos FOLLOW formais dos 44 não-terminais da gramática LL(1) da linguagem Lumina. */
public final class FollowSets {

  private FollowSets() {}

  public static final Set<TokenType> PROGRAM = Set.of(TokenType.EOF);

  public static final Set<TokenType> DECL_LIST = Set.of(TokenType.EOF);

  public static final Set<TokenType> DECL =
      Set.of(
          TokenType.EOF,
          TokenType.FUN,
          TokenType.INT,
          TokenType.DOUBLE,
          TokenType.BOOL,
          TokenType.CHAR,
          TokenType.STRING);

  public static final Set<TokenType> VAR_DECL =
      Set.of(
          TokenType.EOF,
          TokenType.FUN,
          TokenType.INT,
          TokenType.DOUBLE,
          TokenType.BOOL,
          TokenType.CHAR,
          TokenType.STRING,
          TokenType.IDENTIFIER,
          TokenType.IF,
          TokenType.WHILE,
          TokenType.RETURN,
          TokenType.RIGHT_BRACE);

  public static final Set<TokenType> VAR_INIT_OPT = Set.of(TokenType.SEMICOLON);

  public static final Set<TokenType> FUNCTION_DECL = DECL;

  public static final Set<TokenType> RETURN_TYPE = Set.of(TokenType.IDENTIFIER);

  public static final Set<TokenType> TYPE = Set.of(TokenType.IDENTIFIER);

  public static final Set<TokenType> PARAM_LIST_OPT = Set.of(TokenType.RIGHT_PAREN);

  public static final Set<TokenType> PARAM_LIST = Set.of(TokenType.RIGHT_PAREN);

  public static final Set<TokenType> PARAM_LIST_TAIL = Set.of(TokenType.RIGHT_PAREN);

  public static final Set<TokenType> PARAM = Set.of(TokenType.COMMA, TokenType.RIGHT_PAREN);

  public static final Set<TokenType> BLOCK =
      Set.of(
          TokenType.EOF,
          TokenType.FUN,
          TokenType.INT,
          TokenType.DOUBLE,
          TokenType.BOOL,
          TokenType.CHAR,
          TokenType.STRING,
          TokenType.IDENTIFIER,
          TokenType.IF,
          TokenType.ELSE,
          TokenType.WHILE,
          TokenType.RETURN,
          TokenType.RIGHT_BRACE);

  public static final Set<TokenType> STMT_LIST = Set.of(TokenType.RIGHT_BRACE);

  public static final Set<TokenType> STMT =
      Set.of(
          TokenType.INT,
          TokenType.DOUBLE,
          TokenType.BOOL,
          TokenType.CHAR,
          TokenType.STRING,
          TokenType.IDENTIFIER,
          TokenType.IF,
          TokenType.WHILE,
          TokenType.RETURN,
          TokenType.RIGHT_BRACE);

  public static final Set<TokenType> ASSIGN_OR_CALL_STMT = STMT;

  public static final Set<TokenType> ASSIGN_OR_CALL_TAIL = Set.of(TokenType.SEMICOLON);

  public static final Set<TokenType> IF_STMT = STMT;

  public static final Set<TokenType> ELSE_OPT = STMT;

  public static final Set<TokenType> WHILE_STMT = STMT;

  public static final Set<TokenType> RETURN_STMT = STMT;

  public static final Set<TokenType> EXPR_OPT = Set.of(TokenType.SEMICOLON);

  public static final Set<TokenType> EXPR =
      Set.of(TokenType.SEMICOLON, TokenType.RIGHT_PAREN, TokenType.COMMA);

  public static final Set<TokenType> EXPR_TAIL = EXPR;

  public static final Set<TokenType> LOGICAL_AND =
      Set.of(TokenType.SEMICOLON, TokenType.RIGHT_PAREN, TokenType.COMMA, TokenType.PIPE_PIPE);

  public static final Set<TokenType> LOGICAL_AND_TAIL = LOGICAL_AND;

  public static final Set<TokenType> EQUALITY =
      Set.of(
          TokenType.SEMICOLON,
          TokenType.RIGHT_PAREN,
          TokenType.COMMA,
          TokenType.PIPE_PIPE,
          TokenType.AMPERSAND_AMPERSAND);

  public static final Set<TokenType> EQUALITY_TAIL = EQUALITY;

  public static final Set<TokenType> EQ_OP =
      Set.of(
          TokenType.IDENTIFIER,
          TokenType.INT_LITERAL,
          TokenType.DOUBLE_LITERAL,
          TokenType.STRING_LITERAL,
          TokenType.CHAR_LITERAL,
          TokenType.TRUE,
          TokenType.FALSE,
          TokenType.LEFT_PAREN,
          TokenType.BANG,
          TokenType.MINUS,
          TokenType.PLUS);

  public static final Set<TokenType> RELATIONAL =
      Set.of(
          TokenType.SEMICOLON,
          TokenType.RIGHT_PAREN,
          TokenType.COMMA,
          TokenType.PIPE_PIPE,
          TokenType.AMPERSAND_AMPERSAND,
          TokenType.EQUAL_EQUAL,
          TokenType.BANG_EQUAL);

  public static final Set<TokenType> RELATIONAL_TAIL = RELATIONAL;

  public static final Set<TokenType> REL_OP = EQ_OP;

  public static final Set<TokenType> ADDITIVE =
      Set.of(
          TokenType.SEMICOLON,
          TokenType.RIGHT_PAREN,
          TokenType.COMMA,
          TokenType.PIPE_PIPE,
          TokenType.AMPERSAND_AMPERSAND,
          TokenType.EQUAL_EQUAL,
          TokenType.BANG_EQUAL,
          TokenType.LESS,
          TokenType.LESS_EQUAL,
          TokenType.GREATER,
          TokenType.GREATER_EQUAL);

  public static final Set<TokenType> ADDITIVE_TAIL = ADDITIVE;

  public static final Set<TokenType> ADD_OP = EQ_OP;

  public static final Set<TokenType> MULTIPLICATIVE =
      Set.of(
          TokenType.SEMICOLON,
          TokenType.RIGHT_PAREN,
          TokenType.COMMA,
          TokenType.PIPE_PIPE,
          TokenType.AMPERSAND_AMPERSAND,
          TokenType.EQUAL_EQUAL,
          TokenType.BANG_EQUAL,
          TokenType.LESS,
          TokenType.LESS_EQUAL,
          TokenType.GREATER,
          TokenType.GREATER_EQUAL,
          TokenType.PLUS,
          TokenType.MINUS);

  public static final Set<TokenType> MULTIPLICATIVE_TAIL = MULTIPLICATIVE;

  public static final Set<TokenType> MUL_OP = EQ_OP;

  public static final Set<TokenType> UNARY =
      Set.of(
          TokenType.SEMICOLON,
          TokenType.RIGHT_PAREN,
          TokenType.COMMA,
          TokenType.PIPE_PIPE,
          TokenType.AMPERSAND_AMPERSAND,
          TokenType.EQUAL_EQUAL,
          TokenType.BANG_EQUAL,
          TokenType.LESS,
          TokenType.LESS_EQUAL,
          TokenType.GREATER,
          TokenType.GREATER_EQUAL,
          TokenType.PLUS,
          TokenType.MINUS,
          TokenType.STAR,
          TokenType.SLASH,
          TokenType.PERCENT);

  public static final Set<TokenType> PRIMARY = UNARY;

  public static final Set<TokenType> PRIMARY_TAIL = PRIMARY;

  public static final Set<TokenType> ARG_LIST_OPT = Set.of(TokenType.RIGHT_PAREN);

  public static final Set<TokenType> ARG_LIST = Set.of(TokenType.RIGHT_PAREN);

  public static final Set<TokenType> ARG_LIST_TAIL = Set.of(TokenType.RIGHT_PAREN);
}
