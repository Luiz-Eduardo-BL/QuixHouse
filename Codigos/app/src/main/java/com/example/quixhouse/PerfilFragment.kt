package com.example.quixhouse

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.Manifest
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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
import android.content.DialogInterface
import android.app.AlertDialog
import android.widget.Button

var _binding: FragmentPerfilBinding? = null
private val binding get() = _binding!!

class PerfilFragment : Fragment() {

    private var imageUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPerfilBinding.inflate(inflater, container, false)
        return binding.root
        binding.profileImageView.scaleType = ImageView.ScaleType.CENTER_CROP
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkProfilePhoto()
        initClicks()
        setupImageClickListener()

    }

    private fun setupImageClickListener() {
        binding.profileImageView.setOnClickListener {
            checkPermissionAndChooseImage()
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            chooseImageGallery()
        } else {
            Toast.makeText(context, "Permissão necessária para acessar a câmera/galeria", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkPermissionAndChooseImage() {
        when {
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> {
                chooseImageGallery()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    fun getPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.CAMERA), 101)
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 102)
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 103)
        }
    }

    fun uploadImage() {
        val user = FirebaseAuth.getInstance().currentUser
        val uid = user?.uid
        val imageId = UUID.randomUUID().toString()
        val storageRef = FirebaseStorage.getInstance().reference.child("images/$uid/$imageId")
        val uploadTask =
            imageUri?.let { storageRef.putFile(it) }

        val urlTask = uploadTask?.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            storageRef.downloadUrl
        }?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUri = task.result
                val databaseRef = FirebaseDatabase.getInstance().getReference("users/$uid/profilePhoto")
                databaseRef.setValue(downloadUri.toString())
            } else {
                Toast.makeText(context, "Erro ao fazer upload da imagem", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun chooseImageGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, 0)
        checkProfilePhoto()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 0 && resultCode == RESULT_OK && data != null) {
            imageUri = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(activity?.contentResolver, imageUri)
            binding.profileImageView.setImageBitmap(bitmap)
            uploadImage()
        }
        super.onActivityResult(requestCode, resultCode, data)
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

    private fun checkProfilePhoto() {
        val user = FirebaseAuth.getInstance().currentUser
        val uid = user?.uid
        val profilePhotoRef = FirebaseDatabase.getInstance().getReference("users/$uid/profilePhoto")
        profilePhotoRef.get().addOnSuccessListener {
            if (it.value == null) {
                binding.profileImageView.setImageResource(R.drawable.baseline_person_24)
            } else {
                val imageUrl = it.value.toString()
                val imageRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl)
                val ONE_MEGABYTE: Long = 1024 * 1024
                imageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener { bytes ->
                    val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    activity?.runOnUiThread {
                        binding.profileImageView.setImageBitmap(bmp)
                    }
                }.addOnFailureListener { exception ->
                    Toast.makeText(context, "Falha ao obter a foto do perfil", Toast.LENGTH_SHORT).show()
                }
            }
        }.addOnFailureListener { exception ->
            Toast.makeText(context, "Falha ao obter a foto do perfil", Toast.LENGTH_SHORT).show()
        }
    }


    private fun showDeleteConfirmationDialog() {
        val user = FirebaseAuth.getInstance().currentUser
        val uid = user?.uid
        val database = FirebaseDatabase.getInstance().reference
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Deletar conta")
        builder.setMessage("Tem certeza que deseja deletar sua conta?")
        builder.setPositiveButton("Sim") { dialogInterface: DialogInterface, i: Int ->
            user?.delete()?.addOnSuccessListener {
                database.child("users").child(uid.toString()).removeValue()
                Toast.makeText(context, "Conta deletada com sucesso", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_createAccountFragment_to_loginFragment)
            }?.addOnFailureListener {
                Toast.makeText(context, "Erro ao deletar conta", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Não") { dialogInterface: DialogInterface, i: Int ->
            Toast.makeText(context, "Conta não deletada", Toast.LENGTH_SHORT).show()
        }
        builder.show() 
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
        binding.btnDeletePerfil.setOnClickListener {
            showDeleteConfirmationDialog()
        }
    }
}
