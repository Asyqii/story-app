package com.example.storyapp.view.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.storyapp.R
import com.example.storyapp.data.adapter.LoadingAdapter
import com.example.storyapp.data.adapter.StoryAdapter
import com.example.storyapp.databinding.ActivityMainBinding
import com.example.storyapp.view.ViewModelFactory
import com.example.storyapp.view.maps.MapsActivity
import com.example.storyapp.view.uploadstory.UploadStoryActivity
import com.example.storyapp.view.welcome.WelcomeActivity
import kotlinx.coroutines.launch
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.storyapp.data.database.DatabaseStory

class MainActivity : AppCompatActivity() {

    private val viewModel by viewModels<MainViewModel> {
        ViewModelFactory.getInstance(this)
    }

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //Splash Screen
        Thread.sleep(3000)
        installSplashScreen()

        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.addStory.setOnClickListener {
            val intent = Intent(this, UploadStoryActivity::class.java)
            startActivity(intent)
        }
        getSession()
    }

    private fun getSession() {
        try {
            val adapter = StoryAdapter()
            viewModel.getSession().observe(this) { user ->
                if (!user.isLogin) {
                    val intent = Intent(this, WelcomeActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()
                } else {
                    lifecycleScope.launch {
                        viewModel.getStory.observe(this@MainActivity) { result ->
                            adapter.submitData(lifecycle, result)
                            showLoading(false)
                        }
                    }
                }
                binding.rvStory.apply {
                    layoutManager = LinearLayoutManager(this@MainActivity)
                    this.adapter =
                        adapter.withLoadStateHeaderAndFooter(
                            header = LoadingAdapter { adapter.retry() },
                            footer = LoadingAdapter { adapter.retry() }
                        )
                }
            }
        } catch (e: Exception) {

            showToast("Error : ${e.message}")
            Log.e(TAG, "Error : ${e.message}")

        }

    }

    private fun showLoading(state: Boolean) {
        if (state) binding.progressBarMain.visibility = View.VISIBLE
        else binding.progressBarMain.visibility = View.GONE
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.item_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.maps -> {
                startActivity(Intent(this, MapsActivity::class.java))
            }

            R.id.logout -> {
                lifecycleScope.launch {
                    viewModel.logout()
                }
                startActivity(Intent(this, WelcomeActivity::class.java))
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    companion object {
        private const val TAG = "Main Activity"
    }

}