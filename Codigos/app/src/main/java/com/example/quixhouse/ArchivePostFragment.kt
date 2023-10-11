package com.example.quixhouse

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.quixhouse.adapter.AdapterPost
import com.example.quixhouse.databinding.FragmentArchivePostBinding
import com.example.quixhouse.helper.FirebaseHelper
import com.example.quixhouse.model.Post
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class ArchivePostFragment : Fragment() {

    private var _binding: FragmentArchivePostBinding? = null
    private val binding get() = _binding!!

    private val postList = mutableListOf<Post>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentArchivePostBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getPosts()
    }

    private fun initAdapter() {
        val recyclerViewPosts = binding.recyclerViewPosts
        recyclerViewPosts.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        recyclerViewPosts.setHasFixedSize(true)
        // Configurar adpter
        val adapterPost = AdapterPost(requireContext(), postList)
        adapterPost.setOnItemClickListener(object : AdapterPost.OnItemClickListener {
            override fun onItemClick(position: Int) {
                // Lidar com o evento de clique do item aqui
                val intent = Intent(requireContext(), EditPostActivity::class.java)
                intent.putExtra("post_data", postList[position])
                requireContext().startActivity(intent)
            }
        })
        recyclerViewPosts.adapter = adapterPost
    }

    private fun getPosts() {
        FirebaseHelper
            .getDatabase()
            .child("posts")
            .child(FirebaseHelper.getIdUser() ?: "")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    postList.clear()
                    if (snapshot.exists()) {
                        for (snap in snapshot.children) {
                            val post = snap.getValue(Post::class.java) as Post
                            postList.add(post)
                        }
                        postList.reverse()
                        initAdapter()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), error.message, Toast.LENGTH_SHORT).show()
                }

            })
    }


}