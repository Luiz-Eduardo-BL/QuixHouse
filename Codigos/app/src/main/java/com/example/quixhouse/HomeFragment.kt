package com.example.quixhouse

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.quixhouse.adapter.AdapterPost
import com.example.quixhouse.databinding.FragmentHomeBinding
import com.example.quixhouse.helper.FirebaseHelper
import com.example.quixhouse.model.Post
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val postList = mutableListOf<Post>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).setSupportActionBar(binding.toolbar)
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
                val intent = Intent(requireContext(), PostActivity::class.java)
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
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    postList.clear()
                    if (snapshot.exists()) {
                        for (snap in snapshot.children) {
                            for (data in snap.children) {
                                val post = data.getValue(Post::class.java) as Post
                                postList.add(post)
                            }
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_home_search -> {
                Toast.makeText(requireContext(), "Search", Toast.LENGTH_SHORT).show()
                true
            }

            R.id.menu_home_perfil -> {
                Toast.makeText(requireContext(), "Perfil", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_home_to_perfilFragment)
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_home, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

}