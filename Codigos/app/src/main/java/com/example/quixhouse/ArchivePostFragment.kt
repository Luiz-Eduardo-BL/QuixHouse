package com.example.quixhouse

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.quixhouse.adapter.AdapterPost
import com.example.quixhouse.adapter.AdapterPost.ViewType
import com.example.quixhouse.databinding.FragmentArchivePostBinding
import com.example.quixhouse.helper.DataResult
import com.example.quixhouse.helper.FirebaseHelper
import com.example.quixhouse.model.Post
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class ArchivePostFragment : Fragment() {

    private var _binding: FragmentArchivePostBinding? = null
    private val binding get() = _binding!!
    private lateinit var dialog: AlertDialog

    //    private lateinit var adapterPost: AdapterPost
    private val postList = mutableListOf<Post>()
    private val dataResultLiveData = MutableLiveData<DataResult<List<Post>>>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentArchivePostBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getPosts()
        dataResultLiveData.observe(viewLifecycleOwner) { result ->
            when (result.status) {
                DataResult.Status.LOADING -> {

                }

                DataResult.Status.SUCCESS -> {
                    postList.clear()
                    postList.addAll(result.data ?: emptyList())
                    initAdapter()
                }

                DataResult.Status.ERROR -> {
                    Toast.makeText(requireContext(), result.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun initAdapter() {
        val recyclerViewPosts = binding.recyclerViewPosts
        recyclerViewPosts.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        recyclerViewPosts.setHasFixedSize(true)
        // Configurar adpter
        val adapterPost = AdapterPost(requireContext(), postList)
        adapterPost.visibility = true
        adapterPost.setOnItemClickListener(object : AdapterPost.OnItemClickListener {
            override fun onItemClick(position: Int, viewType: AdapterPost.ViewType) {
                // Lidar com o evento de clique do item aqui
                when (viewType) {
                    ViewType.DELETE -> {
                        val builder = AlertDialog.Builder(requireContext()).setTitle("Atenção")
                            .setMessage("Tem certeza que quer excluir o post?")
                            .setNegativeButton("Não") { _, _ ->
                                dialog.dismiss()
                            }.setPositiveButton("Sim") { _, _ ->
                                deletePost(postList[position])
                                adapterPost.notifyDataSetChanged()
                                dialog.dismiss()
                            }
                        dialog = builder.create()
                        dialog.show()
                    }

                    ViewType.EDIT -> {
                        val intent = Intent(requireContext(), EditPostActivity::class.java)
                        intent.putExtra("post_data", postList[position])
                        requireContext().startActivity(intent)
                        adapterPost.notifyDataSetChanged()
                    }

                    else -> {}
                }
            }
        })
        recyclerViewPosts.adapter = adapterPost
    }

    private fun getPosts() {
        dataResultLiveData.value = DataResult.loading()
        FirebaseHelper.getDatabase().child("posts").child(FirebaseHelper.getIdUser() ?: "")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val posts = mutableListOf<Post>()
                    if (snapshot.exists()) {
                        for (snap in snapshot.children) {
                            val post = snap.getValue(Post::class.java) as Post
                            posts.add(post)
                        }
                        posts.reverse()
                        dataResultLiveData.value = DataResult.success(posts)
                    } else {
                        dataResultLiveData.value = DataResult.success(emptyList())
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    dataResultLiveData.value = DataResult.error(error.message)
                }
            })
    }


    private fun deletePost(post: Post) {
        // Referência ao armazenamento Firebase
        val imageRef = FirebaseStorage.getInstance().getReferenceFromUrl(post.image)
        // Exclui a imagem no Firebase Storage
        imageRef.delete().addOnSuccessListener {
            FirebaseHelper.getDatabase().child("posts").child(FirebaseHelper.getIdUser() ?: "")
                .child(post.id).removeValue().addOnCompleteListener { postTask ->
                    if (postTask.isSuccessful) {
                        Toast.makeText(
                            requireContext(), R.string.text_post_update_sucess, Toast.LENGTH_SHORT
                        ).show()
                        postList.remove(post)
                    } else {
                        Toast.makeText(
                            requireContext(), R.string.error_generic, Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }.addOnFailureListener { exception ->
            // A exclusão da imagem falhou
            val errorMessage = exception.message ?: getString(R.string.error_generic)
            Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}