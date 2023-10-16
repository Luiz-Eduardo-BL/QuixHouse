package com.example.quixhouse

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase


class ChangeEmailFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_change_email, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val user = FirebaseAuth.getInstance().currentUser
        val database = FirebaseDatabase.getInstance().reference
        val uid = user?.uid
        val userEmail = database.child("users").child(uid.toString()).child("email")
        userEmail.get().addOnSuccessListener {
            view.findViewById<EditText>(R.id.editNewEmail).hint = it.value.toString()
        }.addOnFailureListener{
            println("Failed to read value.")
        }
        if (user != null) {
            view.findViewById<Button>(R.id.buttonSendEmail).setOnClickListener {
                changeEmail(view)
            }
        }
    }

    fun changeEmail(view: View) {
        val user = FirebaseAuth.getInstance().currentUser
        val email = view.findViewById<EditText>(R.id.editNewEmail).text.toString()
        if (user != null) {
            user.updateEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(
                            context,
                            "Email alterado com sucesso",
                            Toast.LENGTH_SHORT
                        ).show()
                        // Atualizar o email no realtime database
                        val database = FirebaseDatabase.getInstance()
                        val myRef = database.getReference("users")
                        myRef.child(user.uid).child("email").setValue(email)
                        findNavController().navigate(R.id.perfilFragment)
                    } else {
                        Toast.makeText(
                            context,
                            "Email invalido",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
   }

}
