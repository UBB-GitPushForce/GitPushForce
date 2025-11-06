# GitPushForce

The main repository for the project.

## 🗞️ Linting

In order for our code to be _consistent_, there is a **linter** implemented in the project that runs on each push which modifies a `.py` file.

> We are using **Ruff**. A Rust based python **linter** that is fast. Below are the rules which we are using (find more details [here](https://docs.astral.sh/ruff/rules/#flake8-use-pathlib-pth)).

In the `ruff.toml` file you can find the line:

```
select = ["E101", "E115", "E225", "E226", "E228", "E301", "E303", "E4", "E7", "E9", "F", "N801", "N806", "I001"]
```

🖍️ These are the rules which we apply to our code:

- **E101**: Indentation contains mixed spaces and tabs
- **E115**: Expected an indented block (comment)
- **E225**: Missing whitespace around operator
- **E226**: Missing whitespace around arithmetic operator
- **E228**: Missing whitespace around modulo operator
- **E301**: Expected {BLANK_LINES_NESTED_LEVEL:?} blank line, found 0 (checks for blank lines between methods)
- **E303**: Too many blank lines ({actual_blank_lines}) (makes sure there is a consistent space of 1 line at one time)
- **E401**: Multiple imports on one line
- **E402**: Module level import not at top of cell
- **E701**: Multiple statements on one line (colon)
- **E702**: Multiple statements on one line (semicolon)
- **E703**: Statement ends with an unnecessary semicolon
- **E711**: Comparison to None should be cond is None
- **E712**: Avoid equality comparisons to True; use {cond}: for truth checks
- **E713**: Test for membership should be not in
- **E714**: Test for object identity should be is not
- **E721**: Use is and is not for type comparisons, or isinstance() for isinstance checks
- **E722**: Do not use bare except
- **E731**: Do not assign a lambda expression, use a def
- **E741**: Ambiguous variable name: {name}
- **E742**: Ambiguous class name: {name}
- **E743**: Ambiguous function name: {name}

> More info about this the /docs folder (`ruff.pdf`).
