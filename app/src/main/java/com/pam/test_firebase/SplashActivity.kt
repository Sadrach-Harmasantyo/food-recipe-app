package com.pam.test_firebase

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        auth = FirebaseAuth.getInstance()

        // Cek status autentikasi
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // Jika sudah login, arahkan ke HomeActivity
            val intent = Intent(this, MainActivity::class.java)
//            intent.putExtra("email", currentUser.email)
            startActivity(intent)
        } else {
            // Jika belum login, arahkan ke RegisterActivity
            startActivity(Intent(this, LoginRegisterActivity::class.java))
        }

        finish() // Mengakhiri SplashActivity agar tidak masuk ke back stack

//        // Delay selama 2 detik sebelum mengecek autentikasi
//        Handler(Looper.getMainLooper()).postDelayed({
//            val currentUser = auth.currentUser
//            if (currentUser != null) {
//                // Jika sudah login, arahkan ke HomeActivity
//                val intent = Intent(this, HomeActivity::class.java)
//                intent.putExtra("email", currentUser.email)
//                startActivity(intent)
//            } else {
//                // Jika belum login, arahkan ke RegisterActivity
//                startActivity(Intent(this, RegisterActivity::class.java))
//            }
//            finish() // Mengakhiri SplashActivity agar tidak masuk ke back stack
//        }, 2000) // 2000 ms = 2 detik
    }
}
