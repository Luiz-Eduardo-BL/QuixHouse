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
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.quixhouse.databinding.ActivityAddPostBinding
import com.example.quixhouse.helper.FirebaseHelper
import com.example.quixhouse.model.Post
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.util.UUID

class AddPostActivity : AppCompatActivity() {

    private lateinit var imagePost: ImageView
    private var imageUrl: String? = null
    private lateinit var post: Post

//    private var bitmap: Bitmap? = null
    private var _bitmap: Bitmap? = null
    private val bitmap  get() = _bitmap!!

    private lateinit var binding: ActivityAddPostBinding
    private lateinit var dialog: AlertDialog

    companion object {
        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        private const val PERMISSAO_GALERIA = Manifest.permission.READ_MEDIA_IMAGES
    }

    private val requestGaleria =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { permissao ->
            if (permissao) {
                resultGaleria.launch(
                    Intent(
                        Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    )
                )
            } else {
                showDialogPermission()
            }
        }

    private val resultGaleria =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            _bitmap = if (Build.VERSION.SDK_INT < 28) {
                MediaStore.Images.Media.getBitmap(
                    baseContext.contentResolver,
                    result.data?.data
                )
            } else {
                val source = ImageDecoder.createSource(
                    this.contentResolver,
                    result.data?.data!!
                )
                ImageDecoder.decodeBitmap(source)
            }

            binding.imagePost.setImageBitmap(bitmap)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddPostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnAddImage.setOnClickListener {verificaPermissaoGaleria() }
        binding.btnSave.setOnClickListener {validateData() }
    }

    private fun verificaPermissaoGaleria() {
        val permissaoGaleriaAceita = verificaPermissao(PERMISSAO_GALERIA)
        when {
            permissaoGaleriaAceita -> {
                resultGaleria.launch(
                    Intent(
                        Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    )
                )
            }
            shouldShowRequestPermissionRationale(PERMISSAO_GALERIA) -> showDialogPermission()
            else -> requestGaleria.launch(PERMISSAO_GALERIA)
        }
    }

    private fun showDialogPermission() {
        val builder = AlertDialog.Builder(this)
            .setTitle("Atenção")
            .setMessage("Precisamos do acesso a galeria do dispositivo, deseja permitir agora?")
            .setNegativeButton("Não") { _, _ ->
                dialog.dismiss()
            }
            .setPositiveButton("Sim") { _, _ ->
                val intent = Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.fromParts("package", packageName, null)
                )
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                dialog.dismiss()
            }
        dialog = builder.create()
        dialog.show()
    }

    private fun verificaPermissao(permissao: String) =
        ContextCompat.checkSelfPermission(this, permissao) == PackageManager.PERMISSION_GRANTED

    private fun validateData() {
        val user = FirebaseAuth.getInstance().currentUser
        val uid = user?.uid
        val imageId = UUID.randomUUID().toString()
        val storageRef = FirebaseStorage.getInstance().reference.child("images/posts/$uid/$imageId")


        // Converter o bitmap em um ByteArray
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos)
        val data = baos.toByteArray()

        // Fazer o upload do ByteArray para o Firebase Storage
        val uploadTask = storageRef.putBytes(data)

        uploadTask.addOnSuccessListener { _ ->
            // Sucesso no upload
            Toast.makeText(this, "Image uploaded to Firebase Storage", Toast.LENGTH_SHORT).show()
            // Obter a URL de download da imagem
            storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                // Agora você pode usar a URL de download da imagem, por exemplo, para exibir a imagem em seu aplicativo
                imageUrl = downloadUri.toString()
                findViewById<TextView>(R.id.debug).text = imageUrl
                val description = findViewById<EditText>(R.id.descriptionPost).text.toString()

                if (description.isNotEmpty()) {
                    post = Post()
                    post.decription = description
                    post.image = imageUrl?:""
                    savePost()
                } else {
                    Toast.makeText(this, "Escreva uma descrição Valida", Toast.LENGTH_SHORT)
                        .show()
                }
            }.addOnFailureListener { exception ->
                // Tratamento de erros ao obter a URL de download
                Toast.makeText(
                    this,
                    "Failed to get download URL: ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }.addOnFailureListener { exception ->
            // Tratamento de erros no upload
            Toast.makeText(this, "Failed to upload image: ${exception.message}", Toast.LENGTH_SHORT)
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
                    Toast.makeText(
                        this,
                        "Salvo com sucesso",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(this, it.exception.toString(), Toast.LENGTH_SHORT)
                        .show()
                }
            }.addOnFailureListener {
                Toast.makeText(this, "ERROR", Toast.LENGTH_SHORT)
                    .show()
            }
    }


}