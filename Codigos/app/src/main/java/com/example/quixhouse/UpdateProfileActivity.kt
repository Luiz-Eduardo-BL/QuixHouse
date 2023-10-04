package com.example.quixhouse

import android.app.Dialog
import android.net.Uri
import android.os.Bundle
import android.view.Window
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.quixhouse.databinding.ActivityUpdateProfileBinding
import com.example.quixhouse.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.lang.ref.PhantomReference

class UpdateProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUpdateProfileBinding
    private lateinit var auth : FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var storageReference: StorageReference
    private lateinit var imageUri : Uri
    private lateinit var dialog : Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpdateProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        val uid = auth.currentUser?.uid
        databaseReference = Firebase.database.reference.child("Users").child(uid!!)
        binding.saveBtn.setOnClickListener {

            showProgressBar()
            val firstName = binding.etFirstName.text.toString()
            val lastName = binding.etLastName.text.toString()
            val bio = binding.etBio.text.toString()

            val user = User(firstName, lastName, bio)
            if (uid != null) {
                databaseReference.child(uid).setValue(user).addOnCompleteListener {
                    if (it.isSuccessful){
                        uploadProfilePic()
                    }else{
                        hideProgressBar()
                        Toast.makeText(this, "Error: Update falhou!!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun uploadProfilePic() {

        imageUri = Uri.parse("android.resource://$packageName/${R.drawable.profile}")
        storageReference = FirebaseStorage.getInstance().getReference("Users/" + auth.currentUser?.uid)
        storageReference.putFile(imageUri).addOnSuccessListener {
            hideProgressBar()
            Toast.makeText(this, "Perfil atualizado com sucesso!!", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            hideProgressBar()
            Toast.makeText(this, "Error: Atualização de perfil falhou!!", Toast.LENGTH_SHORT).show()
        }

    }

    private fun showProgressBar() {
        dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_wait)
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
    }

    private fun hideProgressBar() {
        dialog.dismiss()
    }
}