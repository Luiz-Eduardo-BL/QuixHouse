package com.example.quixhouse.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
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
        holder.description.text = posts[position].decription
    }
}