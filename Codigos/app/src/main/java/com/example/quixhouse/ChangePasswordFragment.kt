package com.example.quixhouse

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth


class ChangePasswordFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_change_password, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        verifyAuthentication()
        view.findViewById<com.google.android.material.button.MaterialButton>(R.id.buttonSendPassword).setOnClickListener {
            changePassword(view)
        }
    }

    private fun verifyAuthentication() {
        if (FirebaseAuth.getInstance().currentUser == null) {
            val intent = Intent(activity, MainActivity::class.java)
            startActivity(intent)
            activity?.finish()
        }
    }

    private fun validateData(): Boolean {
        val newPassword = view?.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.editNewPassword)?.text.toString()
        val confirmNewPassword = view?.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.confirmNewPassword)?.text.toString()
        return if (newPassword.isNotEmpty() && confirmNewPassword.isNotEmpty()) {
            if (newPassword == confirmNewPassword) {
                true
            } else {
                Toast.makeText(context, "As senhas não são iguais", Toast.LENGTH_SHORT).show()
                false
            }
        } else {
            Toast.makeText(context, "Preencha os campos", Toast.LENGTH_SHORT).show()
            false
        }
    }

    fun changePassword(view: View) {
        if (validateData()) {
            val newPassword = view.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.editNewPassword).text.toString()
            FirebaseAuth.getInstance().currentUser?.updatePassword(newPassword)?.addOnCompleteListener {
                if (it.isSuccessful) {
                    Toast.makeText(context, "Senha alterada com sucesso", Toast.LENGTH_SHORT).show()
                    FirebaseAuth.getInstance().signOut()
                    val intent = Intent(activity, MainActivity::class.java)
                    startActivity(intent)
                    activity?.finish()

                } else {
                    Toast.makeText(context, "Erro ao alterar senha", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


}