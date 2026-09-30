# Lumina

Projeto do compilador/analisador léxico para a linguagem Lumina.

---

## 📋 Pré-requisitos

- **Java JDK 21+** (`javac` e `java` disponíveis no terminal)
- **Make**

---

## 🚀 Como usar o Makefile

O projeto conta com comandos automatizados via `make` para facilitar compilação, execução, testes e formatação de código:

### 1. Compilar o projeto
Compila todos os arquivos Java das pastas `src/` e `test/` gerando os binários em `bin/`:
```bash
make build
# ou simplesmente:
make
```

### 2. Executar o analisador léxico
Executa o analisador sobre o arquivo padrão (`exemplo_fatorial.lum`):
```bash
make run
```

Para analisar outro arquivo `.lum`, passe o parâmetro `FILE`:
```bash
make run FILE=exemplo_com_erros.lum
```

### 3. Rodar os testes
Compila e executa a suíte de testes unitários:
```bash
make test
```

### 4. Formatar o código
Aplica as regras do **Google Java Format** em todo o código-fonte:
```bash
make format
```

### 5. Limpar arquivos compilados
Remove o diretório `bin/`:
```bash
make clean
```
