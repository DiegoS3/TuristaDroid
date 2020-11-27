package android.com.diego.turistadroid.login

import android.com.diego.turistadroid.R
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class LogInActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Ocultamos la barra de action
        this.supportActionBar?.hide()
        setContentView(R.layout.activity_log_in)
    }
}