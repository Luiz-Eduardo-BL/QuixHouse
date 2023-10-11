package com.example.quixhouse

import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.quixhouse.databinding.ActivityEditPostBinding
import com.example.quixhouse.model.Post

class EditPostActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditPostBinding
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditPostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val post = intent.getParcelableExtra("post_data", Post::class.java)

        if (post != null) {
            binding.descriptionPost.text = post.description
            Glide.with(this)
                .load(post.image)
                .apply(
                    RequestOptions()
                        .placeholder(R.drawable.ic_image_default) // Imagem de placeholder, se desejar
                        .error(R.drawable.ic_image_not_supported) // Imagem de erro, se desejar
                        .diskCacheStrategy(DiskCacheStrategy.ALL)) // Estratégia de armazenamento em cache
                .into(binding.imagePost)
        }
    }
}