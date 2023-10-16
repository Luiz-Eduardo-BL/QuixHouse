package com.example.quixhouse

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.quixhouse.databinding.FragmentAddPostBinding
import com.example.quixhouse.helper.FirebaseHelper
import com.example.quixhouse.model.Post
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.util.UUID

class AddPostFragment : Fragment() {

    private var _binding: FragmentAddPostBinding? = null
    private val binding get() = _binding!!
    private lateinit var dialog: AlertDialog
    private var _bitmap: Bitmap? = null
    private val bitmap get() = _bitmap!!
    private lateinit var post: Post
    private var flagImage: Boolean = false
    companion object {
        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        private const val PERMISSAO_GALERIA = Manifest.permission.READ_MEDIA_IMAGES
    }

    private val requestGaleriaLauncher: ActivityResultLauncher<String> = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openGallery()
        } else {
            showDialogPermission()
        }
    }

    private val resultGaleriaLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == AppCompatActivity.RESULT_OK) {
            val data: Intent? = result.data
            if (data != null) {
                _bitmap = if (Build.VERSION.SDK_INT < 28) {
                    MediaStore.Images.Media.getBitmap(
                        requireContext().contentResolver,
                        data.data
                    )
                } else {
                    val source = ImageDecoder.createSource(
                        requireContext().contentResolver,
                        data.data!!
                    )
                    ImageDecoder.decodeBitmap(source)
                }
                binding.imagePost.setImageBitmap(bitmap)
                flagImage = true
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddPostBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnAddImage.setOnClickListener { verificaPermissaoGaleria() }
        binding.btnSave.setOnClickListener { validateData() }
    }


    private fun verificaPermissaoGaleria() {
        val permissaoGaleriaAceita = verificaPermissao(PERMISSAO_GALERIA)
        if (permissaoGaleriaAceita) {
            openGallery()
        } else {
            requestGaleriaLauncher.launch(PERMISSAO_GALERIA)
        }
    }

    private fun openGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        resultGaleriaLauncher.launch(galleryIntent)
    }

    private fun showDialogPermission() {
        val builder = AlertDialog.Builder(requireContext())
            .setTitle("Atenção")
            .setMessage("Precisamos do acesso à galeria do dispositivo, deseja permitir agora?")
            .setNegativeButton("Não") { _, _ ->
                dialog.dismiss()
            }
            .setPositiveButton("Sim") { _, _ ->
                val intent = Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.fromParts("package", requireContext().packageName, null)
                )
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                dialog.dismiss()
            }
        dialog = builder.create()
        dialog.show()
    }

    private fun verificaPermissao(permissao: String) =
        ContextCompat.checkSelfPermission(
            requireContext(),
            permissao
        ) == PackageManager.PERMISSION_GRANTED

    private fun validateData() {
        val user = FirebaseAuth.getInstance().currentUser
        val uid = user?.uid
        val imageId = UUID.randomUUID().toString()
        val storageRef = FirebaseStorage.getInstance().reference.child("images/posts/$uid/$imageId")

        val description = binding.descriptionPost.text.toString().trim()
        if (description.isNotEmpty() && flagImage) {
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos)
            val data = baos.toByteArray()
            val uploadTask = storageRef.putBytes(data)
            uploadTask.addOnSuccessListener { _ ->
                Toast.makeText(
                    requireContext(),
                    "Image uploaded to Firebase Storage",
                    Toast.LENGTH_SHORT
                ).show()
                storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    post = Post()
                    post.description = description
                    post.image = downloadUri.toString()
                    savePost()

                }.addOnFailureListener { exception ->
                    Toast.makeText(
                        requireContext(),
                        "Failed to get download URL: ${exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }.addOnFailureListener { exception ->
                Toast.makeText(
                    requireContext(),
                    "Failed to upload image: ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            Toast.makeText(requireContext(), "Escreva uma descrição válida", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun savePost() {
        FirebaseHelper
            .getDatabase()
            .child("posts")
            .child(FirebaseHelper.getIdUser() ?: "")
            .child(post.id)
            .setValue(post)
            .addOnCompleteListener { it ->
                if (it.isSuccessful) {
                    Toast.makeText(requireContext(), "Salvo com sucesso", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), it.exception.toString(), Toast.LENGTH_SHORT)
                        .show()
                }
            }.addOnFailureListener {
                Toast.makeText(requireContext(), "ERROR", Toast.LENGTH_SHORT).show()
            }
    }
}
