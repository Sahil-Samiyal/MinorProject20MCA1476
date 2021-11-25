package com.sahilSamiyal.musicPlayer

import android.content.Context
import android.content.Intent
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.sahilSamiyal.musicPlayer.databinding.VideoViewBinding


class VideoAdapter(private val context: Context, private var videolist: ArrayList<Video>,private val isFolder: Boolean =false) : RecyclerView.Adapter<VideoAdapter.MyHolder>()
{
    class MyHolder(binding: VideoViewBinding) :RecyclerView.ViewHolder(binding.root) {
        val  title =binding.videoName
        val  folder =binding.folderName
        val  duration = binding.duration
        val image = binding.videoImg
        val root = binding.root
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
       return MyHolder(VideoViewBinding.inflate(LayoutInflater.from(context),parent, false))
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {
    holder.title.text =videolist[position].title
        holder.folder.text= videolist[position].folderName
        holder.duration.text= DateUtils.formatElapsedTime(videolist[position].duration/1000)
        Glide.with(context)
            .asBitmap()
            .load(videolist[position].artUri)
            .apply(RequestOptions().placeholder(R.mipmap.ic_lancher).centerCrop())
            .into(holder.image)
        holder.root.setOnClickListener{
            when{
                isFolder -> {
                    VideoPlayerActivity.pipstatus =1
                    sendIntent(pos = position,ref = "FolderActivity")

                }
                else -> {
                    VideoPlayerActivity.pipstatus =2
                    sendIntent(pos = position,ref = "AllVideos")
                }
            }
        }
    }

    override fun getItemCount(): Int {
     return videolist.size
    }
    private fun sendIntent(pos : Int, ref : String){
        VideoPlayerActivity.position =pos
        val intent =Intent(context, VideoPlayerActivity::class.java)
        intent.putExtra("class",ref)
        ContextCompat.startActivity(context,intent,null)
    }

}