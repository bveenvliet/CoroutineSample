package com.example.coroutinesample

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ProgressBar
import android.widget.Toast
import com.example.coroutinesample.databinding.ActivityMainBinding
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var job: CompletableJob

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inflate the layout for this activity using view binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // bind the button click
        binding.btnStartStopJob.setOnClickListener {
            if (!::job.isInitialized) {
                initJob()
            }
            binding.progressBar.startJobOrCancel(job)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun initJob() {
        binding.apply {
            btnStartStopJob.text = "Start job"
            tvStatus.text = ""
            job = Job()
            job.invokeOnCompletion { throwable ->
                throwable?.message.let { msg ->
                    val reason = msg ?: "Unknown cancellation error"
                    println("$job was cancelled. Reason: $reason")
                    showToast(reason)
                }
            }
            progressBar.max = 100
            progressBar.progress = 0
        }
    }

    @SuppressLint("SetTextI18n")
    private fun ProgressBar.startJobOrCancel(job: Job) {
        if (this.progress > 0) {
            // cancel job
            println("$job is already active. Cancelling...")
            resetJob()
        } else {
            // start job
            binding.apply {
                btnStartStopJob.text = "Cancel job"
                tvStatus.text = ""
            }
            CoroutineScope(Dispatchers.IO + job).launch {
                println("coroutine $this is activated with job $job")
                for (i in 0..100) {
                    delay(50)
                    this@startJobOrCancel.progress = i
                }
                updateJobCompleteUi()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun resetJob() {
        if (job.isActive || job.isCompleted) {
            job.cancel(CancellationException("Resetting job"))
        }
        initJob()
    }

    @SuppressLint("SetTextI18n")
    private fun updateJobCompleteUi() {
        // must do this to execute on the main UI thread
        GlobalScope.launch(Dispatchers.Main) {
            binding.tvStatus.text = "Job is complete"
        }
    }

    private fun showToast(msg: String) {
        // must do this to execute on the main UI thread
        GlobalScope.launch(Dispatchers.Main) {
            Toast.makeText(this@MainActivity, msg, Toast.LENGTH_SHORT).show()
        }
    }
}