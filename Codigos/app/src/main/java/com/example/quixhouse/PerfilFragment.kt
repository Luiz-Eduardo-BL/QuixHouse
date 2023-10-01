package com.example.quixhouse

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.quixhouse.databinding.FragmentPerfilBinding

private var _binding: FragmentPerfilBinding? = null
private val binding get() = _binding!!
class PerfilFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPerfilBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initClicks()
    }

    private fun initClicks() {
        binding.backButton.setOnClickListener {
            findNavController().navigate(R.id.action_perfilFragment_to_home)
        }
        binding.btnEditEmail.setOnClickListener {
            findNavController().navigate(R.id.action_perfilFragment_to_changeEmailFragment)
        }
        binding.btnEditName.setOnClickListener {
            findNavController().navigate(R.id.action_perfilFragment_to_changeNameFragment)
        }
        binding.btnEditPassword.setOnClickListener {
            findNavController().navigate(R.id.action_perfilFragment_to_changePasswordFragment)
        }
    }

}