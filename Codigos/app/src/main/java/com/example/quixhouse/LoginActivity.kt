package com.example.quixhouse

import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.quixhouse.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Aplicando sublinhado nos textViews
        applyUnderline(binding.linkCreateAccount, binding.linkCreateAccount.text.toString())
        applyUnderline(binding.linkRecoverPassword, binding.linkRecoverPassword.text.toString())

        binding.linkCreateAccount.setOnClickListener {
            startActivity(Intent(this, CreateAccountActivity::class.java))
        }

        firebaseAuth = FirebaseAuth.getInstance()

        binding.buttonLogin.setOnClickListener {
            val email = binding.editEmail.text.toString()
            val password = binding.editPassword.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
                    if (it.isSuccessful) {
                        Toast.makeText(this, "Login realizado com sucesso", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, FeedActivity::class.java))
                    } else {
                        Toast.makeText(this, it.exception.toString(), Toast.LENGTH_LONG).show()
                    }
                }
            } else {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
            }
        }

    }

    fun applyUnderline(textView : TextView, texto : String) {
        // Crie uma SpannableString e aplique o sublinhado
        val content = SpannableString(texto)
        content.setSpan(UnderlineSpan(), 0, content.length, 0)

        // Defina a SpannableString como o texto do TextView
        textView.text = content
    }


}