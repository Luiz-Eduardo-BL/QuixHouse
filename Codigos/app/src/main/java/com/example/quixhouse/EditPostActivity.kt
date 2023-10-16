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
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.quixhouse.databinding.ActivityEditPostBinding
import com.example.quixhouse.helper.FirebaseHelper
import com.example.quixhouse.model.Post
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.util.UUID

class EditPostActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditPostBinding
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
                        this.contentResolver,
                        data.data
                    )
                } else {
                    val source = ImageDecoder.createSource(
                        this.contentResolver,
                        data.data!!
                    )
                    ImageDecoder.decodeBitmap(source)
                }
                binding.imagePost.setImageBitmap(bitmap)
                flagImage = true
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditPostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        intent.getParcelableExtra("post_data", Post::class.java)!!.also { post = it }

        post.let {
            binding.descriptionPost.setText(it.description)
            Glide.with(this)
                .load(it.image)
                .apply(
                    RequestOptions()
                        .placeholder(R.drawable.ic_image_default) // Imagem de placeholder, se desejar
                        .error(R.drawable.ic_image_not_supported) // Imagem de erro, se desejar
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                ) // Estratégia de armazenamento em cache
                .into(binding.imagePost)
        }
        initCLicks()
    }

    private fun initCLicks() {
        binding.btnSave.setOnClickListener {
            post.description = binding.descriptionPost.text.toString()
            validateData()
        }
        binding.imagePost.setOnClickListener {
            verificaPermissaoGaleria()
        }
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
        val builder = AlertDialog.Builder(this)
            .setTitle("Atenção")
            .setMessage("Precisamos do acesso à galeria do dispositivo, deseja permitir agora?")
            .setNegativeButton("Não") { _, _ ->
                dialog.dismiss()
            }
            .setPositiveButton("Sim") { _, _ ->
                val intent = Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.fromParts("package", this.packageName, null)
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
            this,
            permissao
        ) == PackageManager.PERMISSION_GRANTED

    private fun validateData() {
        val description = binding.descriptionPost.text.toString().trim()
        if (description.isNotEmpty()) {
            if (flagImage) {
                val user = FirebaseAuth.getInstance().currentUser
                val uid = user?.uid
                val imageId = UUID.randomUUID().toString()
                val storageRef = FirebaseStorage.getInstance().reference.child("images/posts/$uid/$imageId")
                val baos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos)
                val data = baos.toByteArray()
                val uploadTask = storageRef.putBytes(data)
                uploadTask.addOnSuccessListener { _ ->
                    Toast.makeText(
                        this,
                        "Image uploaded to Firebase Storage",
                        Toast.LENGTH_SHORT
                    ).show()
                    storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                        post.description = description
                        post.image = downloadUri.toString()
                        updatePost()
                    }.addOnFailureListener { exception ->
                        Toast.makeText(
                            this,
                            "Failed to get download URL: ${exception.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }.addOnFailureListener { exception ->
                    Toast.makeText(
                        this,
                        "Failed to upload image: ${exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            else {
                post.description = description
                updatePost()
            }
        } else {
            Toast.makeText(this, "Escreva uma descrição válida", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun updatePost() {
        FirebaseHelper
            .getDatabase()
            .child("posts")
            .child(FirebaseHelper.getIdUser() ?: "")
            .child(post.id)
            .setValue(post)
            .addOnCompleteListener { it ->
                if (it.isSuccessful) {
                    Toast.makeText(
                        this,
                        R.string.text_post_update_sucess,
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        this,
                        "Error",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }.addOnFailureListener {
                Toast.makeText(
                    this,
                    it.message,
                    Toast.LENGTH_SHORT
                ).show()
            }

        finish()
    }


}