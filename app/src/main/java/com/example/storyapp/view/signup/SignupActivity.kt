package com.example.storyapp.view.signup

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.storyapp.R
import com.example.storyapp.data.response.RegisterResponse
import com.example.storyapp.databinding.ActivitySignupBinding
import com.example.storyapp.view.ViewModelFactory
import com.example.storyapp.view.login.LoginActivity
import com.google.gson.Gson
import kotlinx.coroutines.launch
import retrofit2.HttpException
import com.example.storyapp.data.Result


class SignupActivity : AppCompatActivity() {

    private var _binding: ActivitySignupBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModels<SignupViewModel> {
        ViewModelFactory.getInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        showLoading(false)

        setupActions()
    }

    private fun setupActions() {
        handleSignupClick()
    }


    private fun checkError(name: String, email: String, password: String): Boolean {
        var isValid = true

        when {
            name.isEmpty() -> {
                binding.edtLayoutName.error = "Name cannot be empty"
                isValid = false
            }
            email.isEmpty() -> {
                binding.edtLayoutEmail.error = "Please fill in your email"
                isValid = false
            }
            password.isEmpty() -> {
                binding.edtLayoutPw.error = "Please fill in your password"
                isValid = false
            }
        }

        return isValid

    }

    private fun handleSignupClick() {
        binding.signupButton.setOnClickListener {
            showLoading(true)
            val name = binding.edtName.text.toString()
            val email = binding.edtEmail.text.toString()
            val password = binding.edtPw.text.toString()

            if (!checkError(name, email, password)) {
                showLoading(false)
                return@setOnClickListener
            }

            showLoading(true)

            lifecycleScope.launch {
                try {
                    viewModel.register(name, email, password)
                        .observe(this@SignupActivity) { result ->
                            if (result != null) {
                                when (result) {
                                    is Result.Loading -> {
                                        showLoading(true)
                                    }

                                    is Result.Success -> {
                                        Log.d(TAG, "Sign Up Success")
                                        showLoading(false)
                                        showToast("Sign Up Success !")
                                        val intent = Intent(
                                            this@SignupActivity,
                                            LoginActivity::class.java
                                        )
                                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or
                                                Intent.FLAG_ACTIVITY_NEW_TASK

                                        startActivity(intent)
                                        finish()
                                    }

                                    is Result.Error -> {
                                        showLoading(false)
                                        showToast(result.error)
                                    }
                                }
                            }
                        }

                } catch (e: HttpException) {
                    showLoading(false)
                    val errorBody = e.response()?.errorBody()?.string()
                    val errorResponse = Gson().fromJson(errorBody, RegisterResponse::class.java)
                    showToast(errorResponse.message)
                }
            }


        }
    }


    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBarSignup.visibility =
            if (isLoading) android.view.View.VISIBLE else android.view.View.GONE
        binding.signupButton.isEnabled = !isLoading
    }

    companion object {
        private const val TAG = "Sign Up"
    }

}