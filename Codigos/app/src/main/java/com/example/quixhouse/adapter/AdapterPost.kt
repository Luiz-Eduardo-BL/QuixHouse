package com.example.quixhouse.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.quixhouse.PostActivity
import com.example.quixhouse.R
import com.example.quixhouse.model.Post

class AdapterPost(private val context: android.content.Context, private val posts: MutableList<Post>): RecyclerView.Adapter<AdapterPost.PostViewHolder>() {

    private var onItemClickListener: OnItemClickListener? = null

    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image = itemView.findViewById<ImageView>(R.id.imagePost)
        val description = itemView.findViewById<TextView>(R.id.descriptionPost)
        init {
            image.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClickListener?.onItemClick(position)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val itemList = LayoutInflater.from(context).inflate(R.layout.item_post, parent, false)
        return PostViewHolder(itemList)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.onItemClickListener = listener
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }


    override fun getItemCount(): Int = posts.size

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
//        holder.image.setImageResource(posts[position].image)
        val post = posts[position]
        holder.description.text = post.description
        Glide.with(context)
            .load(post.image)
            .apply(
                RequestOptions()
                .placeholder(R.drawable.ic_image_default) // Imagem de placeholder, se desejar
                .error(R.drawable.ic_image_not_supported) // Imagem de erro, se desejar
                .diskCacheStrategy(DiskCacheStrategy.ALL)) // Estratégia de armazenamento em cache
            .into(holder.image)

        holder.image.setOnClickListener {
            val intent = Intent(context, PostActivity::class.java)
            intent.putExtra("post_data", post)
            context.startActivity(intent)
        }

    }
}