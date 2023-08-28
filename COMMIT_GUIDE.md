# Guia de Padrão de Commits

Este guia descreve o padrão de commits que estamos usando para manter um histórico organizado e compreensível de alterações no projeto. Siga estas diretrizes ao fazer commits para contribuir com o projeto.

## Padrão de Commits

Nossos commits seguem o padrão de commits convencional (Conventional Commits) para manter um histórico claro e informativo das alterações.

## Estrutura do Commit

Cada commit é composto por três partes principais: Tipo, Mensagem e Detalhes.

Para fazer um commit seguindo esse padrão, utilize o seguinte comando:

```bash
git commit -m "<tipo>: <mensagem>"
- Detalhe 1
- Detalhe 2
```
Você poderá ver exemplos no final do documento.

## Tipos de Commit

Use um dos seguintes tipos para descrever a natureza da alteração:

- `feat`: Para adicionar uma nova funcionalidade.
- `fix`: Para correção de bugs.
- `chore`: Para tarefas de manutenção, melhorias, organização, etc.
- `refactor`: Para alterações de código que não adicionam funcionalidades ou corrigem bugs.
- `style`: Para alterações na formatação do código, espaçamento, etc.
- `docs`: Para alterações na documentação.
- `test`: Para alterações nos testes.
- `perf`: Para melhorias de desempenho.
- `build`: Para alterações no processo de build.

## Mensagem do Commit

A mensagem do commit deve ser concisa, descritiva e no imperativo. Ela deve explicar o que a alteração faz.

## Detalhes do Commit

Forneça detalhes adicionais, se necessário, usando uma lista com marcadores para fornecer mais contexto sobre as alterações.

## Exemplos de Commits


### Adição de Nova Funcionalidade

```makefile
Author: Seu Nome <seuemail@gmail.com>
Date: Data atual
Commit: feat: Adds search functionality

- Implements interactive search bar
- Adds search filters by category
```

### Correção de Bug

```makefile
Author: Seu Nome <seuemail@gmail.com>
Date: Data atual
Commit: fix: fixes layout problem in Login component

- Adjusts alignment of elements on the login page
- Fixes content overflow
```

### Tarefas de Manutenção

```makefile
Author: Seu Nome <seuemail@gmail.com>
Date: Data atual
Commit: chore: optimizes imports and code formatting

- Reorganizes imports for better readability
- Adjusts code formatting according to guidelines
```