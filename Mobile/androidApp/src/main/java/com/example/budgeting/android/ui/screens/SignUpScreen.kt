package com.example.budgeting.android.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.budgeting.android.ui.viewmodels.SignUpViewModel
import com.example.budgeting.android.ui.viewmodels.SignUpUiState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import com.example.budgeting.android.ui.viewmodels.SignUpViewModelFactory

enum class SignUpStep {
    ONBOARDING,
    FIRST_NAME,
    LAST_NAME,
    EMAIL,
    PHONE_NUMBER,
    PASSWORD
}

@Composable
fun SignUpScreen(
    onSignUpSuccess: () -> Unit,
    onBackToLogin: () -> Unit,
    signUpViewModel: SignUpViewModel = viewModel(
        factory = SignUpViewModelFactory(LocalContext.current)
    )
) {
    var currentStep by remember { mutableStateOf(SignUpStep.ONBOARDING) }
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
    ) {
        when (currentStep) {
            SignUpStep.ONBOARDING -> {
                OnboardingScreen(
                    onGetStarted = { currentStep = SignUpStep.FIRST_NAME },
                    onBackToLogin = onBackToLogin
                )
            }
            else -> {
                RegistrationFormScreen(
                    currentStep = currentStep,
                    firstName = firstName,
                    lastName = lastName,
                    email = email,
                    phoneNumber = phoneNumber,
                    password = password,
                    passwordVisible = passwordVisible,
                    onFirstNameChange = { firstName = it },
                    onLastNameChange = { lastName = it },
                    onEmailChange = { email = it },
                    onPhoneNumberChange = { phoneNumber = it },
                    onPasswordChange = { password = it },
                    onPasswordVisibleToggle = { passwordVisible = !passwordVisible },
                    onNext = {
                        val fieldName = when (currentStep) {
                            SignUpStep.FIRST_NAME -> "firstName"
                            SignUpStep.LAST_NAME -> "lastName"
                            SignUpStep.EMAIL -> "email"
                            SignUpStep.PHONE_NUMBER -> "phoneNumber"
                            SignUpStep.PASSWORD -> "password"
                            else -> ""
                        }
                        val currentValue = when (currentStep) {
                            SignUpStep.FIRST_NAME -> firstName
                            SignUpStep.LAST_NAME -> lastName
                            SignUpStep.EMAIL -> email
                            SignUpStep.PHONE_NUMBER -> phoneNumber
                            SignUpStep.PASSWORD -> password
                            else -> ""
                        }
                        
                        // Validate current field before proceeding
                        if (signUpViewModel.validateField(fieldName, currentValue)) {
                            currentStep = when (currentStep) {
                                SignUpStep.FIRST_NAME -> SignUpStep.LAST_NAME
                                SignUpStep.LAST_NAME -> SignUpStep.EMAIL
                                SignUpStep.EMAIL -> SignUpStep.PHONE_NUMBER
                                SignUpStep.PHONE_NUMBER -> SignUpStep.PASSWORD
                                SignUpStep.PASSWORD -> {
                                    // Submit registration
                                    signUpViewModel.signUp(firstName, lastName, email, password, phoneNumber)
                                    currentStep
                                }
                                else -> currentStep
                            }
                        }
                    },
                    onBack = {
                        currentStep = when (currentStep) {
                            SignUpStep.FIRST_NAME -> SignUpStep.ONBOARDING
                            SignUpStep.LAST_NAME -> SignUpStep.FIRST_NAME
                            SignUpStep.EMAIL -> SignUpStep.LAST_NAME
                            SignUpStep.PHONE_NUMBER -> SignUpStep.EMAIL
                            SignUpStep.PASSWORD -> SignUpStep.PHONE_NUMBER
                            else -> currentStep
                        }
                    },
                    uiState = uiState,
                    signUpViewModel = signUpViewModel,
                    screenHeight = screenHeight
                )
            }
        }

        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
fun OnboardingScreen(
    onGetStarted: () -> Unit,
    onBackToLogin: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 32.dp)
    ) {
        Spacer(modifier = Modifier.height(80.dp))


        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Track your money,\nbe smart",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
            lineHeight = 36.sp
        )

        Spacer(modifier = Modifier.height(48.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = "📸",
                    fontSize = 32.sp
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Scan receipts",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Just point and shoot. We'll grab the details for you.",
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 22.sp
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = "🤖",
                    fontSize = 32.sp
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Auto-categorize",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "AI figures out what you spent on. No typing needed.",
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 22.sp
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = "📊",
                    fontSize = 32.sp
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "See where it goes",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Know exactly what you're spending on groceries, bills, and everything else.",
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 22.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(56.dp))

        Button(
            onClick = onGetStarted,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Get started",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        TextButton(
            onClick = onBackToLogin,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Already have an account?",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Log in",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun RegistrationFormScreen(
    currentStep: SignUpStep,
    firstName: String,
    lastName: String,
    email: String,
    phoneNumber: String,
    password: String,
    passwordVisible: Boolean,
    onFirstNameChange: (String) -> Unit,
    onLastNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPhoneNumberChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onPasswordVisibleToggle: () -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit,
    uiState: SignUpUiState,
    signUpViewModel: SignUpViewModel,
    screenHeight: androidx.compose.ui.unit.Dp
) {
    val focusManager = LocalFocusManager.current
    
    LaunchedEffect(currentStep) {
        focusManager.clearFocus()
    }
    val fieldName = when (currentStep) {
        SignUpStep.FIRST_NAME -> "firstName"
        SignUpStep.LAST_NAME -> "lastName"
        SignUpStep.EMAIL -> "email"
        SignUpStep.PHONE_NUMBER -> "phoneNumber"
        SignUpStep.PASSWORD -> "password"
        else -> ""
    }

    val fieldError = uiState.fieldErrors[fieldName]
    val currentValue = when (currentStep) {
        SignUpStep.FIRST_NAME -> firstName
        SignUpStep.LAST_NAME -> lastName
        SignUpStep.EMAIL -> email
        SignUpStep.PHONE_NUMBER -> phoneNumber
        SignUpStep.PASSWORD -> password
        else -> ""
    }

    val onValueChange = when (currentStep) {
        SignUpStep.FIRST_NAME -> onFirstNameChange
        SignUpStep.LAST_NAME -> onLastNameChange
        SignUpStep.EMAIL -> onEmailChange
        SignUpStep.PHONE_NUMBER -> onPhoneNumberChange
        SignUpStep.PASSWORD -> onPasswordChange
        else -> { _ -> }
    }

    val label = when (currentStep) {
        SignUpStep.FIRST_NAME -> "First Name"
        SignUpStep.LAST_NAME -> "Last Name"
        SignUpStep.EMAIL -> "Email"
        SignUpStep.PHONE_NUMBER -> "Phone Number"
        SignUpStep.PASSWORD -> "Password"
        else -> ""
    }

    val keyboardType = when (currentStep) {
        SignUpStep.EMAIL -> KeyboardType.Email
        SignUpStep.PHONE_NUMBER -> KeyboardType.Phone
        SignUpStep.PASSWORD -> KeyboardType.Password
        else -> KeyboardType.Text
    }

    val stepNumber = when (currentStep) {
        SignUpStep.FIRST_NAME -> 1
        SignUpStep.LAST_NAME -> 2
        SignUpStep.EMAIL -> 3
        SignUpStep.PHONE_NUMBER -> 4
        SignUpStep.PASSWORD -> 5
        else -> 0
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Step $stepNumber of 5",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Spacer(modifier = Modifier.height(screenHeight * 0.05f))

            OutlinedTextField(
                value = currentValue,
                onValueChange = { 
                    onValueChange(it)
                    if (fieldError != null) {
                        signUpViewModel.clearFieldError(fieldName)
                    }
                },
                label = { Text(label) },
                isError = fieldError != null,
                supportingText = fieldError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                visualTransformation = if (currentStep == SignUpStep.PASSWORD && !passwordVisible) 
                    PasswordVisualTransformation() 
                else 
                    VisualTransformation.None,
                trailingIcon = if (currentStep == SignUpStep.PASSWORD) {
                    {
                        val image = if (passwordVisible)
                            Icons.Filled.Visibility
                        else Icons.Filled.VisibilityOff
                        IconButton(onClick = onPasswordVisibleToggle) {
                            Icon(imageVector = image, contentDescription = if (passwordVisible) "Hide password" else "Show password")
                        }
                    }
                } else null,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if (fieldError != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = if (fieldError != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                    errorBorderColor = MaterialTheme.colorScheme.error,
                    focusedTextColor = MaterialTheme.colorScheme.primary,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    errorLabelColor = MaterialTheme.colorScheme.error,
                    cursorColor = MaterialTheme.colorScheme.primary
                )
            )

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

            Spacer(modifier = Modifier.height(screenHeight * 0.05f))

            Button(
                onClick = onNext,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = currentValue.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(24.dp)
            ) {
                Text(
                    text = if (currentStep == SignUpStep.PASSWORD) "Sign Up" else "Next",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Back",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SignUpScreenPreview() {
    SignUpScreen(onSignUpSuccess = { }, onBackToLogin = { })
}
