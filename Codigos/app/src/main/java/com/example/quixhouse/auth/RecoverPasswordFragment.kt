package com.example.quixhouse.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.quixhouse.databinding.FragmentRecoverPasswordBinding
import com.google.firebase.auth.FirebaseAuth


class RecoverPasswordFragment : Fragment() {

    private var _binding: FragmentRecoverPasswordBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRecoverPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()
        initClicks()
    }

    private fun initClicks() {
        binding.buttonSend.setOnClickListener {
            val email = binding.editEmail.text.toString().trim()
            recoverAccount(email)
        }

    }

    private fun recoverAccount(email : String) {
        if (validateData(email)) {
            auth.sendPasswordResetEmail(email).addOnCompleteListener {
                if (it.isSuccessful) {
                    Toast.makeText(requireContext(),"Enviado link com sucesso", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), it.exception.toString(), Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun validateData(email : String): Boolean {
        return if (email.isNotEmpty()) {
            true
        } else {
            Toast.makeText(requireContext(), "Preencha o email", Toast.LENGTH_SHORT).show()
            false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}