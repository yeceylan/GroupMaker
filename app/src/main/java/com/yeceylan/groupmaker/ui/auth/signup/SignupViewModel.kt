package com.yeceylan.groupmaker.ui.auth.signup

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.yeceylan.groupmaker.core.Resource
import com.yeceylan.groupmaker.domain.model.user.User
import com.yeceylan.groupmaker.domain.use_cases.user.AddUserUseCase
import com.yeceylan.groupmaker.domain.use_cases.auth.RegisterUseCase
import com.yeceylan.groupmaker.domain.use_cases.auth.SignInWithGoogleUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val registerUseCase: RegisterUseCase,
    private val addUserUseCase: AddUserUseCase,
    private val signInWithGoogleUseCase: SignInWithGoogleUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignUpUIState())
    val uiState = _uiState.asStateFlow()

    fun signUpWithEmailAndPassword(email: String, password: String, name: String) =
        registerUseCase(email, password).onEach {
            when (it) {
                is Resource.Loading -> {
                    _uiState.update { state ->
                        state.copy(isLoading = true)
                    }
                }

                is Resource.Success -> {
                    val user = it.data
                    val userId = user?.user?.uid
                    val photoUrl = "https://firebasestorage.googleapis.com/v0/b/groupmaker-b7bd3.appspot.com/o/users%2Fplaceholder.jpg?alt=media&token=79670420-0237-4ce2-a470-b2d614bf7baf"
                    val newUser = User(id = userId.toString(), email = email, userName = name, photoUrl = photoUrl)
                    addUserToFirestore(newUser)
                }

                is Resource.Error -> {
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            isHaveError = true,
                            errorMessage = it.errorMessage.orEmpty(),
                        )
                    }
                }
            }
        }.launchIn(viewModelScope)

    private fun addUserToFirestore(user: User) {
        viewModelScope.launch {
            try {
                addUserUseCase(user)
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        isHaveError = false,
                        isSuccessSignUpWithEmailAndPassword = true,
                    )
                }
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        isHaveError = true,
                        errorMessage = e.message.orEmpty(),
                    )
                }
            }
        }
    }

    fun signUpWithGoogle(account: GoogleSignInAccount) {
        signInWithGoogleUseCase(account).onEach {
            when (it) {
                is Resource.Loading -> {
                    _uiState.update { state ->
                        state.copy(isLoading = true)
                    }
                }

                is Resource.Success -> {
                    val user = it.data
                    val userId = user?.user?.uid
                    val photoUrl = user?.user?.photoUrl.toString()
                    val newUser = User(id = userId.toString(), email = user?.user?.email ?: "", userName = user?.user?.displayName ?: "", photoUrl = photoUrl)
                    addUserToFirestore(newUser)
                }

                is Resource.Error -> {
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            isHaveError = true,
                            errorMessage = it.errorMessage.orEmpty(),
                        )
                    }
                }
            }
        }.launchIn(viewModelScope)
    }

    fun isEmailValid(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun isPasswordValid(password: String): Boolean {
        val regex = "^[a-zA-Z0-9.*^$#@!%&_=+]*\$".toRegex()
        return password.matches(regex) && password.length >= 6
    }

    fun isPasswordsMatch(password: String, confirmPassword: String): Boolean {
        return password == confirmPassword
    }

    fun resetUIState() {
        _uiState.update {
            SignUpUIState()
        }
    }

    data class SignUpUIState(
        val isLoading: Boolean = false,
        val isHaveError: Boolean = false,
        val errorMessage: String = "",
        val isSuccessGoogleSignup: Boolean = false,
        val isSuccessSignUpWithEmailAndPassword: Boolean = false,
    )
}
