# GitPushForce

The main repository for the project.

# 🌎 API Documentation

## 🚦 Routes

### Expense routes

🟩 **GET /expenses** get all the expenses<br>
🟩 **GET /expenses/{id}** get certain expense by id<br>
🟦 **POST /expenses** create new expense<br>
🟧 **PUT /expenses/{id}** update an expense<br>
🟥 **DELETE /expenses/{id}** delete an expense<br>

### Expense payment routes
🟩 GET /expenses/{expense_id}/payments get list of users who paid<br>
🟦 POST /expenses/{expense_id}/pay/{user_id} mark payment (only expense creator)<br>
🟥 DELETE /expenses/{expense_id}/pay/{user_id} unmark payment (only expense creator)<br>

### Authentication routes

🟦 **POST /users/auth/register** register an account<br>
🟦 **POST /users/auth/login** login (sets a cookie for web and also returns JWT token to be used in mobile app)<br>
🟦 **POST /users/auth/logout** clears the cookie<br>
🟦 **POST /users/join-group/{invitation_code}** join group with invitation code<br>

### Group routes

🟩 **GET /groups** get all groups<br>
🟩 **GET /groups/{group_id}** get certain group by id<br>
🟩 **GET /groups/{group_id}/users** get all users from a group<br>
🟩 **GET /groups/user/{user_id}** get all groups from an user<br>
🟩 **GET /groups/{group_id}/expenses** get all expenses from a group<br>
🟩 **GET /groups/{group_id}/users/nr** get nr of users from a group<br>
🟦 **POST /groups** create new group<br>
🟦 **POST /groups/{group_id}/users/{user_id}** add an user to a group<br>
🟧 **PUT /groups/{group_id}** update a group<br>
🟥 **DELETE /groups/{group_id}** delete a group<br>
🟥 **DELETE /groups/{group_id}/users/{user_id}** remove an user from a group<br>

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
- **E902**: IO error {message}

> More info about this the /docs folder (`ruff.pdf`).

---

---
