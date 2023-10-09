package com.example.quixhouse

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.quixhouse.databinding.FragmentPerfilBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.selects.select
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.UUID

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

    override fun onStart() {
        super.onStart()
        val user = FirebaseAuth.getInstance().currentUser
        val database = FirebaseDatabase.getInstance().reference
        val uid = user?.uid
        val userName = database.child("users").child(uid.toString()).child("username")
        val email = database.child("users").child(uid.toString()).child("email")
        userName.get().addOnSuccessListener {
            binding.userName.text = it.value.toString()
        }
        email.get().addOnSuccessListener {
            binding.userEmail.text = it.value.toString()
        }
    }

    override fun onResume() {
        super.onResume()
        val user = FirebaseAuth.getInstance().currentUser
        val database = FirebaseDatabase.getInstance().reference
        val uid = user?.uid
        val profilePic = database.child("users").child(uid.toString()).child("profilePic")
        profilePic.get().addOnSuccessListener {
            if (it.value.toString() != "null") {
                val storage = FirebaseStorage.getInstance().reference
                val imageRef = storage.child("images/${it.value.toString()}")
                val localFile = File.createTempFile("tempImage", "jpg")
                imageRef.getFile(localFile).addOnSuccessListener {
                    val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
                    binding.profileImageView.setImageBitmap(bitmap)
                }
            } else {
                binding.profileImageView.setImageResource(R.drawable.baseline_person_24)
            }
        }
        binding.profileImageView.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }
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
