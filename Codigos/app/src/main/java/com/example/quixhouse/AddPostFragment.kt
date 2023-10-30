package com.example.quixhouse

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
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
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.util.UUID

class AddPostFragment : Fragment(R.layout.fragment_add_post) {

    // ViewBinding
    private lateinit var binding: FragmentAddPostBinding

    // Variavel que exibe dialogos
    private lateinit var dialog: AlertDialog

    // Variavel que manipula a imagem
//    private var _bitmap: Bitmap? = null
//    private val bitmap get() = _bitmap!!
    private var bitmap: Bitmap? = null
    private var flagImage: Boolean = false

    // Variavel post
    private var post: Post = Post()
    private var latitude: Double? = null
    private var longitude: Double? = null

    /*
        Variaveis para lidar com autocomplete e maps
     */
    private var startAutocompleteIntentListener = View.OnClickListener { view: View ->
        view.setOnClickListener(null)
        startAutocompleteIntent()
    }


    // [START maps_solutions_android_autocomplete_define]
    private val startAutocomplete = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        binding.autocompleteAddressPost.setOnClickListener(startAutocompleteIntentListener)
        if (result.resultCode == Activity.RESULT_OK) {
            val intent = result.data
            if (intent != null) {
                val place = Autocomplete.getPlaceFromIntent(intent)
                Log.d(TAG, "Place: " + place.addressComponents)
                fillInAddress(place)
            }
        } else if (result.resultCode == Activity.RESULT_CANCELED) {
            Log.i(TAG, "User canceled autocomplete")
        }
    }
    // [END maps_solutions_android_autocomplete_define]


    companion object {
        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        private const val PERMISSAO_GALERIA = Manifest.permission.READ_MEDIA_IMAGES
        private val TAG = AddPostFragment::class.java.simpleName
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
                bitmap = if (Build.VERSION.SDK_INT < 28) {
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
        if (!Places.isInitialized()) {
          Places.initialize(requireContext(), BuildConfig.MAPS_API_KEY);
        }
        binding = FragmentAddPostBinding.inflate(inflater, container, false)
        val view = binding.root

        binding.autocompleteAddressPost.setOnClickListener(startAutocompleteIntentListener)


        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initClicks()
    }

    fun initClicks() {
        binding.imagePost.setOnClickListener { verificaPermissaoGaleria() }
        binding.btnAdd.setOnClickListener { validateData() }
    }

    // [START maps_solutions_android_autocomplete_intent]
    private fun startAutocompleteIntent() {
        // Set the fields to specify which types of place data to
        // return after the user has made a selection.
        val fields = listOf(
            Place.Field.ADDRESS_COMPONENTS,
            Place.Field.LAT_LNG, Place.Field.VIEWPORT
        )

        // Build the autocomplete intent with field, country, and type filters applied
        val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
            .setCountries(listOf("BR"))
            //TODO: https://developers.google.com/maps/documentation/places/android-sdk/autocomplete
            .setTypesFilter(listOf(TypeFilter.ADDRESS.toString().lowercase()))
            .build(requireActivity())
        startAutocomplete.launch(intent)
    }
    // [END maps_solutions_android_autocomplete_intent]

    private fun fillInAddress(place: Place) {
        val components = place.addressComponents
        val address1 = StringBuilder()
        val postcode = StringBuilder()

        // Get each component of the address from the place details,
        // and then fill-in the corresponding field on the form.
        // Possible AddressComponent types are documented at https://goo.gle/32SJPM1
        if (components != null) {
            for (component in components.asList()) {
                when (component.types[0]) {
                    "street_number" -> {
                        address1.insert(0, component.name)
                    }

                    "route" -> {
                        address1.append(" ")
                        address1.append(component.shortName)
                    }

                    "postal_code" -> {
                        postcode.insert(0, component.name)
                    }

                    "postal_code_suffix" -> {
                        postcode.append("-").append(component.name)
                    }

                    "sublocality_level_1" -> binding.neighborhoodPost.setText(component.name)

                    "administrative_area_level_1" -> binding.stateAddrPost.setText(component.shortName)

                    "administrative_area_level_2" -> binding.cityAddrPost.setText(component.name)

                    "country" -> binding.countryAddrPost.setText(component.name)
                }
            }
        }
        binding.autocompleteAddressPost.setText(address1.toString())
        binding.zipCodePost.setText(postcode.toString())

        // Obter as coordenadas de latitude e longitude
        latitude = place.latLng?.latitude
        longitude = place.latLng?.longitude
        if (latitude != null && longitude != null) {
            Log.d(TAG, "Latitude: $latitude, Longitude: $longitude")
        }

        // After filling the form with address components from the Autocomplete
        // prediction, set cursor focus on the second address line to encourage
        // entry of sub-premise information such as apartment, unit, or floor number.
        binding.numAddrPost.requestFocus()

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

    private fun clearForm() {
        flagImage = false
        bitmap = null
        binding.imagePost.setImageResource(R.drawable.ic_add_image)
        binding.descriptionPost.text.clear()
        binding.autocompleteAddressPost.setText("")
        binding.numAddrPost.text.clear()
        binding.neighborhoodPost.text.clear()
        binding.cityAddrPost.text.clear()
        binding.stateAddrPost.text.clear()
        binding.countryAddrPost.text.clear()
        binding.zipCodePost.text.clear()

    }
    private fun getAddress(): Boolean {
        val autocompleteAddress = binding.autocompleteAddressPost.text.toString().trim()
        val numAddr = binding.numAddrPost.text.toString().trim()
        val neighborhood = binding.neighborhoodPost.text.toString().trim()
        val cityAddr = binding.cityAddrPost.text.toString().trim()
        val stateAddr = binding.stateAddrPost.text.toString().trim()
        val countryAddr = binding.countryAddrPost.text.toString().trim()
        val zipCode = binding.zipCodePost.text.toString().trim()
        if (autocompleteAddress.isNotEmpty() && numAddr.isNotEmpty() &&
            cityAddr.isNotEmpty() && stateAddr.isNotEmpty() && countryAddr.isNotEmpty() &&
            zipCode.isNotEmpty()
        ) {
            post.locationAddress.address = autocompleteAddress
            post.locationAddress.numberAp = numAddr
            post.locationAddress.neighborhood = neighborhood
            post.locationAddress.city = cityAddr
            post.locationAddress.state = stateAddr
            post.locationAddress.country = countryAddr
            post.locationAddress.zipCode = zipCode
            post.locationAddress.latitude = latitude!!
            post.locationAddress.longitude = longitude!!
            return true
        } else {
            return false
        }
    }

    private fun validateData() {
        val user = FirebaseAuth.getInstance().currentUser
        val uid = user?.uid
        val imageId = UUID.randomUUID().toString()
        val storageRef = FirebaseStorage.getInstance().reference.child("images/posts/$uid/$imageId")

        val description = binding.descriptionPost.text.toString().trim()
        if (description.isNotEmpty() && flagImage && getAddress()) {
            val baos = ByteArrayOutputStream()
            bitmap?.compress(Bitmap.CompressFormat.JPEG, 90, baos)
            val data = baos.toByteArray()
            val uploadTask = storageRef.putBytes(data)
            uploadTask.addOnSuccessListener { _ ->
                Toast.makeText(
                    requireContext(),
                    "Image uploaded to Firebase Storage",
                    Toast.LENGTH_SHORT
                ).show()
                storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
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
                    clearForm()
                } else {
                    Toast.makeText(requireContext(), it.exception.toString(), Toast.LENGTH_SHORT)
                        .show()
                }
            }.addOnFailureListener {
                Toast.makeText(requireContext(), "ERROR", Toast.LENGTH_SHORT).show()
            }
    }


}
