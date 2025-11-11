package com.example.budgeting.android.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.budgeting.android.ui.viewmodels.SignUpViewModel
import androidx.compose.ui.platform.LocalContext
import com.example.budgeting.android.ui.viewmodels.SignUpViewModelFactory

@Composable
fun SignUpScreen(
    onSignUpSuccess: () -> Unit,
    onBackToLogin: () -> Unit,
    signUpViewModel: SignUpViewModel = viewModel(
        factory = SignUpViewModelFactory(LocalContext.current)
    )
) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val uiState by signUpViewModel.uiState.collectAsState()

    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp


    LaunchedEffect(uiState.signUpSuccess) {
        if (uiState.signUpSuccess) {
            onSignUpSuccess()
            signUpViewModel.onSignUpHandled()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    )
    {

        if (uiState.isLoading) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            )
            {

                Text(
                    text = "Sign Up",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(screenHeight * 0.01f))


                Text(
                    text = "Create an account to get started",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(screenHeight * 0.04f))

                // first name
                val firstNameError = uiState.fieldErrors["firstName"]
                OutlinedTextField(
                    value = firstName,
                    onValueChange = { 
                        firstName = it
                        if (firstNameError != null) {
                            signUpViewModel.clearFieldError("firstName")
                        }
                    },
                    label = { Text("First Name") },
                    isError = firstNameError != null,
                    supportingText = firstNameError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (firstNameError != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = if (firstNameError != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                        errorBorderColor = MaterialTheme.colorScheme.error,
                        focusedTextColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        errorLabelColor = MaterialTheme.colorScheme.error,
                        cursorColor = MaterialTheme.colorScheme.primary
                    )
                )

                Spacer(modifier = Modifier.height(screenHeight * 0.02f))

                // last name
                val lastNameError = uiState.fieldErrors["lastName"]
                OutlinedTextField(
                    value = lastName,
                    onValueChange = { 
                        lastName = it
                        if (lastNameError != null) {
                            signUpViewModel.clearFieldError("lastName")
                        }
                    },
                    label = { Text("Last Name") },
                    isError = lastNameError != null,
                    supportingText = lastNameError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (lastNameError != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = if (lastNameError != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                        errorBorderColor = MaterialTheme.colorScheme.error,
                        focusedTextColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        errorLabelColor = MaterialTheme.colorScheme.error,
                        cursorColor = MaterialTheme.colorScheme.primary
                    )
                )

                Spacer(modifier = Modifier.height(screenHeight * 0.02f))

                // email
                val emailError = uiState.fieldErrors["email"]
                OutlinedTextField(
                    value = email,
                    onValueChange = { 
                        email = it
                        if (emailError != null) {
                            signUpViewModel.clearFieldError("email")
                        }
                    },
                    label = { Text("Email") },
                    isError = emailError != null,
                    supportingText = emailError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (emailError != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = if (emailError != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                        errorBorderColor = MaterialTheme.colorScheme.error,
                        focusedTextColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        errorLabelColor = MaterialTheme.colorScheme.error,
                        cursorColor = MaterialTheme.colorScheme.primary
                    )
                )

                Spacer(modifier = Modifier.height(screenHeight * 0.02f))

                // phone number
                val phoneNumberError = uiState.fieldErrors["phoneNumber"]
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { 
                        phoneNumber = it
                        if (phoneNumberError != null) {
                            signUpViewModel.clearFieldError("phoneNumber")
                        }
                    },
                    label = { Text("Phone Number") },
                    isError = phoneNumberError != null,
                    supportingText = phoneNumberError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (phoneNumberError != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = if (phoneNumberError != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                        errorBorderColor = MaterialTheme.colorScheme.error,
                        focusedTextColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        errorLabelColor = MaterialTheme.colorScheme.error,
                        cursorColor = MaterialTheme.colorScheme.primary
                    )
                )

                Spacer(modifier = Modifier.height(screenHeight * 0.02f))

                // password
                val passwordError = uiState.fieldErrors["password"]
                OutlinedTextField(
                    value = password,
                    onValueChange = { 
                        password = it
                        if (passwordError != null) {
                            signUpViewModel.clearFieldError("password")
                        }
                    },
                    label = { Text("Password") },
                    isError = passwordError != null,
                    supportingText = passwordError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        val image = if (passwordVisible)
                            Icons.Filled.Visibility
                        else Icons.Filled.VisibilityOff

                        val description = if (passwordVisible) "Hide password" else "Show password"

                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(imageVector = image, description)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (passwordError != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = if (passwordError != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                        errorBorderColor = MaterialTheme.colorScheme.error,
                        focusedTextColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        errorLabelColor = MaterialTheme.colorScheme.error,
                        cursorColor = MaterialTheme.colorScheme.primary
                    )
                )

                // Show server errors as simple text (without red background)
                uiState.error?.let { errorMsg ->
                    if (uiState.fieldErrors.isEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorMsg,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(screenHeight * 0.03f))

                Button(
                    onClick = { signUpViewModel.signUp(firstName, lastName, email, password, phoneNumber) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text(text = "Sign Up", fontSize = 16.sp, color = MaterialTheme.colorScheme.onPrimary)
                }

                Spacer(modifier = Modifier.height(screenHeight * 0.02f))

                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Already have an account?", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Log in",
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { onBackToLogin() }
                    )

                }
            }
        }
    }
}


// for easier design implementation
@Preview(showBackground = true)
@Composable
fun SignUpScreenPreview() {
    SignUpScreen(onSignUpSuccess = { }, onBackToLogin = { })
}
