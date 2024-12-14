package com.pam.test_firebase

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : AppCompatActivity() {

    // deklarasi variabel auth
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        //inisialisasi firebaseauth
        auth = FirebaseAuth.getInstance()

        // Cek status autentikasi (done)
        Handler(Looper.getMainLooper()).postDelayed({
            val currentUser = auth.currentUser
            if (currentUser != null) {
                // Jika sudah login, arahkan ke MainActivity
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            } else {
                // Jika belum login, arahkan ke LoginRegisterActivity
                startActivity(Intent(this, LoginRegisterActivity::class.java))
            }
            finish()
        }, 1000)
    }
}
