package com.sahilSamiyal.musicPlayer

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.sahilSamiyal.musicPlayer.databinding.FragmentVideoBinding

class VideoFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_video, container, false)
        val binding = FragmentVideoBinding.bind(view)
        binding.videoRV.setHasFixedSize(true)
        binding.videoRV.setItemViewCacheSize(10)
        binding.videoRV.layoutManager =LinearLayoutManager(requireContext())
        binding.videoRV.adapter = VideoAdapter(requireContext(),VideoActivity2.videoList)
        binding.totalVideos.text="Total Videos: ${VideoActivity2.folderList.size}"
        return view
    }
}