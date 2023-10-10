package com.example.quixhouse

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase


class ChangeNameFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_change_name, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val buttonChange = view.findViewById<Button>(R.id.buttonSend)
        val editUserName = view.findViewById<EditText>(R.id.editUserName)

        val user = FirebaseAuth.getInstance().currentUser
        val database = FirebaseDatabase.getInstance().reference
        val uid = user?.uid
        val userName = database.child("users").child(uid.toString()).child("username")
        userName.get().addOnSuccessListener {
            editUserName.hint = it.value.toString()
        }.addOnFailureListener{
            println("Failed to read value.")
        }

        buttonChange.setOnClickListener {
            changeName(view)
        }
    }


    // Função para alterar o nome do usuário no realtime database.
    fun changeName(view: View) {
        val user = FirebaseAuth.getInstance().currentUser
        val database = FirebaseDatabase.getInstance().reference
        val uid = user?.uid
        val userName = database.child("users").child(uid.toString()).child("username")
        val editUserName = view.findViewById<EditText>(R.id.editUserName)

        userName.setValue(editUserName.text.toString()).addOnSuccessListener {
            Toast.makeText(context, "Nome atualizado", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_changeNameFragment_to_perfilFragment)

        }.addOnFailureListener{
            Toast.makeText(context, "Erro, nome não atualizado!", Toast.LENGTH_SHORT).show()
            println("Erro, nome não atualizado!")
        }
    }

}
