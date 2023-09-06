package com.example.quixhouse.auth

import android.os.Bundle
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.quixhouse.R
import com.example.quixhouse.databinding.FragmentCreateAccountBinding
import com.google.firebase.auth.FirebaseAuth


class CreateAccountFragment : Fragment() {

    private var _binding: FragmentCreateAccountBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCreateAccountBinding.inflate(inflater, container, false)
        applyUnderline(binding.linkLogin, binding.linkLogin.text.toString())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()
        initClicks()
    }

    private fun initClicks() {
        binding.buttonCadastro.setOnClickListener {
            val email = binding.editEmail.text.toString().trim()
            val password = binding.editPassword.text.toString().trim()
            registerUser(email, password)
        }
        binding.linkLogin.setOnClickListener {
            findNavController().navigate(R.id.action_createAccountFragment_to_loginFragment)
        }


    }



    private fun registerUser(email : String, password : String) {
        if (validateData(email, password)) {
            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
                if (it.isSuccessful) {
                    findNavController().navigate(R.id.action_createAccountFragment_to_feedActivity)
                    Toast.makeText(requireContext(),"Conta cadastrada com sucesso", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), it.exception.toString(), Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun validateData(email : String, password : String): Boolean {
        return if (email.isNotEmpty()) {
            if (password.isNotEmpty()) {
                true
            } else {
                Toast.makeText(requireContext(), "Preencha a senha", Toast.LENGTH_SHORT).show()
                false
            }
        } else {
            Toast.makeText(requireContext(), "Preencha o email", Toast.LENGTH_SHORT).show()
            false
        }
    }

    private fun applyUnderline(textView : TextView, texto : String) {
        // Crie uma SpannableString e aplique o sublinhado
        val content = SpannableString(texto)
        content.setSpan(UnderlineSpan(), 0, content.length, 0)

        // Defina a SpannableString como o texto do TextView
        textView.text = content
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }



}