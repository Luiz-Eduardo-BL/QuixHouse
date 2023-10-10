package com.example.quixhouse

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
import com.example.quixhouse.model.Post

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

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
        initAdapter()
        initClicks()
    }

    private fun initAdapter() {
        val recyclerViewPosts = binding.recyclerViewPosts
        recyclerViewPosts.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        recyclerViewPosts.setHasFixedSize(true)
        // Configurar adpter
        val listPosts: MutableList<Post> = mutableListOf()
        val adapterPost = AdapterPost(requireContext(), listPosts)
        adapterPost.setOnItemClickListener(object : AdapterPost.OnItemClickListener {
            override fun onItemClick(position: Int) {
                // Lidar com o evento de clique do item aqui
                findNavController().navigate(R.id.action_home_to_postFragment)
            }
        })
        recyclerViewPosts.adapter = adapterPost

        listPosts.add(Post("", "","Apenas Teste"))
    }

    private fun initClicks() {

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
            R.id.menu_home_add_post -> {
                Toast.makeText(requireContext(), "Settings", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_home_to_addPostActivity)
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