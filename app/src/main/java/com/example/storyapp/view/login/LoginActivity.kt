package com.example.storyapp.view.login

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.storyapp.R
import com.example.storyapp.data.model.UserModel
import com.example.storyapp.data.response.LoginResponse
import com.example.storyapp.databinding.ActivityLoginBinding
import com.example.storyapp.view.ViewModelFactory
import com.google.gson.Gson
import kotlinx.coroutines.launch
import retrofit2.HttpException
import com.example.storyapp.data.Result
import com.example.storyapp.view.main.MainActivity


class LoginActivity : AppCompatActivity() {
    private val viewModel by viewModels<LoginViewModel> {
        ViewModelFactory.getInstance(this)
    }

    private var _binding: ActivityLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        showLoading(false)
        initializeUI()
        actionLogin()
    }

    private fun initializeUI() {
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        supportActionBar?.hide()
    }

    private fun actionLogin() {
        handleLoginClick()
    }

    private fun handleLoginClick() {
        binding.loginButton.setOnClickListener {
            try {
                val email = binding.edtEmail.text.toString()
                val password = binding.edtPw.text.toString()
                when {
                    email.isEmpty() -> binding.edtEmail.error = getString(R.string.fill_email)
                    password.isEmpty() -> binding.edtPw.error = getString(R.string.fill_password)
                    else -> {
                        lifecycleScope.launch {
                            viewModel.login(email, password).observe(this@LoginActivity) { result ->
                                if (result != null) {
                                    when (result) {
                                        is Result.Loading -> {
                                            showLoading(true)
                                        }

                                        is Result.Success -> {
                                            showLoading(false)
                                            showToast("Login berhasil!")
                                            lifecycleScope.launch {
                                                save(
                                                    UserModel(
                                                        result.data.loginResult?.token.toString(),
                                                        result.data.loginResult?.name.toString(),
                                                        result.data.loginResult?.userId.toString(),
                                                        true
                                                    )
                                                )
                                            }
                                        }

                                        is Result.Error -> {
                                            showLoading(false)
                                            showToast(result.error)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Log.d("LoginActivity", "Login button clicked with email: $email")

            } catch (e: HttpException) {
                showLoading(false)
                val errorBody = e.response()?.errorBody()?.string()
                val errorResponse = Gson().fromJson(errorBody, LoginResponse::class.java)
                showToast(errorResponse.message)
            }

        }
    }

    private fun save(session: UserModel) {
        lifecycleScope.launch {
            viewModel.saveSession(session)
            val intent = Intent(this@LoginActivity, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            ViewModelFactory.clearInstance()
            startActivity(intent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun showToast(message: String?) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBarLogin.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.loginButton.isEnabled = !isLoading
    }


}