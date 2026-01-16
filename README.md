# 🌎 API Documentation

## 🚦 Routes

### Expense routes

🟩 **GET /expenses** get all the expenses<br>
🟩 **GET /expenses/{id}** get certain expense by id<br>
🟦 **POST /expenses** create new expense<br>
🟧 **PUT /expenses/{id}** update an expense<br>
🟥 **DELETE /expenses/{id}** delete an expense<br>

### Group logs
🟩 **GET /group_logs/{group_id}** get all the logs for a group you are part of<br>

### Expense payment routes
🟩 GET /expenses_payments/{expense_id}/payments get list of users who paid<br>
🟦 POST /expenses_payments/{expense_id}/pay/{user_id} mark payment (only expense creator)<br>
🟥 DELETE /expenses_payments/{expense_id}/pay/{user_id} unmark payment (only expense creator)<br>

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