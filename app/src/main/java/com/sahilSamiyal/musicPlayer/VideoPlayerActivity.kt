package com.sahilSamiyal.musicPlayer

import android.annotation.SuppressLint
import android.app.AppOpsManager
import android.app.PictureInPictureParams
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.drawable.ColorDrawable
import android.media.audiofx.LoudnessEnhancer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.sahilSamiyal.musicPlayer.databinding.ActivityVideoPlayerBinding
import com.sahilSamiyal.musicPlayer.databinding.BoosterBinding
import com.sahilSamiyal.musicPlayer.databinding.MoreFeaturesBinding
import com.sahilSamiyal.musicPlayer.databinding.SpeedDialogBinding
import java.text.DecimalFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.system.exitProcess

class VideoPlayerActivity : AppCompatActivity() {

   private lateinit var binding: ActivityVideoPlayerBinding
   private lateinit var runnable: Runnable
   private var isSubtitle: Boolean =true
    companion object{
        private var timer: Timer? =null
        private lateinit var  player:SimpleExoPlayer
        lateinit var playerList: ArrayList<Video>
        var position: Int = -1
        private var repeat : Boolean = false
        private var isFullscreen : Boolean = false
        private var isLocked: Boolean = false
        private lateinit var trackSelector: DefaultTrackSelector
        private lateinit var loudnessEnhancer: LoudnessEnhancer
        private var speed:Float =1.0f
        var pipstatus: Int =0

    }
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        binding = ActivityVideoPlayerBinding.inflate(layoutInflater)
        setTheme(R.style.playerActivityTheme)
        setContentView(binding.root)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window ,binding.root).let { controller ->
            controller.hide(WindowInsetsCompat.Type.statusBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
       initializeLayout()
        initializeBinding()
    }
    private  fun initializeLayout(){
        when(intent.getStringExtra("class")){
            "AllVideos" -> {
                playerList = ArrayList()
                playerList.addAll(VideoActivity2.videoList)
                createPlayer()
            }
            "FolderActivity" -> {
                playerList = ArrayList()
                playerList.addAll(FoldersActivity.currentFolderVideo)
                createPlayer()
            }
        }
        if (repeat) binding.repeatBtn.setImageResource(R.drawable.exo_controls_repeat_all)
        else binding.repeatBtn.setImageResource(R.drawable.exo_controls_repeat_off)

    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n")
    private fun initializeBinding(){
        binding.backBtn.setOnClickListener{
            finish()
        }
        binding.playPause.setOnClickListener{

            if (player.isPlaying) pauseVideo()
            else playVideo()
        }
        binding.nextBtn.setOnClickListener{
            if (player.isPlaying) pauseVideo()
            else playVideo()
        }
        binding.nextBtn.setOnClickListener{nextPrevVideo()}
        binding.prevBtn.setOnClickListener{nextPrevVideo(isNext = false)}
        binding.repeatBtn.setOnClickListener{
            if (repeat){
                repeat =false
                player.repeatMode =Player.REPEAT_MODE_OFF
                binding.repeatBtn.setImageResource(R.drawable.exo_controls_repeat_off)
            }else{
                repeat =true
                player.repeatMode =Player.REPEAT_MODE_ONE
                binding.repeatBtn.setImageResource(R.drawable.exo_controls_repeat_all)
            }
        }
        binding.fullScreenBtn.setOnClickListener{
            if (isFullscreen){
                isFullscreen =false
                playInFullscreen(enable = false)
            }else{
                isFullscreen =true
                playInFullscreen(enable = true)
            }
        }
        binding.lockButton.setOnClickListener {
            if (!isLocked){
                // for hiding lock button
                isLocked = true
                binding.playerView.hideController()
                binding.playerView.useController =false
                binding.lockButton.setImageResource(R.drawable.close_lock_icon)
            }else{
                // for showing lock button
                isLocked = false
                binding.playerView.useController =true
                binding.playerView.showController()
                binding.lockButton.setImageResource(R.drawable.lock_icon)
            }
        }
        binding.moreFeatureBtn.setOnClickListener{
            pauseVideo()
            val customDialog =LayoutInflater.from(this).inflate(R.layout.more_features, binding.root, false)
            val bindingMF =MoreFeaturesBinding.bind(customDialog)
            val dialog = MaterialAlertDialogBuilder(this).setView(customDialog)
                .setOnCancelListener { playVideo() }
                .setBackground(ColorDrawable(0x80915F6D.toInt()))
                .create()
            dialog.show()

            bindingMF.Audiotrack.setOnClickListener {
                dialog.dismiss()
                playVideo()
                val  audioTrack = ArrayList<String>()
                for (i in 0 until player.currentTrackGroups.length){
                    if(player.currentTrackGroups.get(i).getFormat(0).selectionFlags == C.SELECTION_FLAG_DEFAULT){
                        audioTrack.add(Locale(player.currentTrackGroups.get(i).getFormat(0).language.toString()).displayLanguage)
                    }
                }
                val tempTracks =audioTrack.toArray(arrayOfNulls<CharSequence>(audioTrack.size))
                MaterialAlertDialogBuilder(this , R.style.alertDialog)
                    .setTitle("Select Language")
                    .setOnCancelListener { playVideo() }
                    .setBackground(ColorDrawable(0x80915F6D.toInt()))
                    .setItems(tempTracks){_,position ->
                        Toast.makeText(this, audioTrack[position] + "Selected", Toast.LENGTH_SHORT).show()
                        trackSelector.setParameters(trackSelector.buildUponParameters().setPreferredAudioLanguage(audioTrack[position]))

                    }
                    .create()
                    .show()
            }
            bindingMF.subtitleBtn.setOnClickListener {
                if (isSubtitle){
                    trackSelector.parameters = DefaultTrackSelector.ParametersBuilder(this).setRendererDisabled(
                        C.TRACK_TYPE_VIDEO, true
                    ).build()
                    Toast.makeText(this,"Subtitles Off", Toast.LENGTH_SHORT).show()
                    isSubtitle =false
                }else{
                    trackSelector.parameters = DefaultTrackSelector.ParametersBuilder(this).setRendererDisabled(
                        C.TRACK_TYPE_VIDEO, false
                    ).build()
                    Toast.makeText(this, "Subtitles On",Toast.LENGTH_SHORT).show()
                    isSubtitle=true
                }
                dialog.dismiss()
                playVideo()
            }
            bindingMF.audioBooster.setOnClickListener {
                dialog.dismiss()
                val customDialogB =LayoutInflater.from(this).inflate(R.layout.booster, binding.root, false)
                val bindingB =BoosterBinding.bind(customDialogB)
                val dialogB = MaterialAlertDialogBuilder(this).setView(customDialogB)
                    .setOnCancelListener { playVideo() }
                    .setPositiveButton("OK"){self, _ ->
                        loudnessEnhancer.setTargetGain(bindingB.verticalbar.progress * 100)
                        playVideo()
                        self.dismiss()
                    }
                    .setBackground(ColorDrawable(0x80915F6D.toInt()))
                    .create()
                dialogB.show()
                bindingB.verticalbar.progress = loudnessEnhancer.targetGain.toInt()/100
                bindingB.progressText.text ="Audio Boost\n\n${loudnessEnhancer.targetGain.toInt()/10} %"
                bindingB.verticalbar.setOnProgressChangeListener {
                    bindingB.progressText.text ="Audio Boost\n\n${it*10} %"
                }
            }
            bindingMF.speedBtn.setOnClickListener {
                dialog.dismiss()
                playVideo()
                val customDialogS =LayoutInflater.from(this).inflate(R.layout.speed_dialog, binding.root, false)
                val bindingS =SpeedDialogBinding.bind(customDialogS)
                val dialogS = MaterialAlertDialogBuilder(this).setView(customDialogS)
                    .setCancelable(false)
                    .setPositiveButton("OK"){self, _ ->
                        self.dismiss()
                    }
                    .setBackground(ColorDrawable(0x80915F6D.toInt()))
                    .create()
                dialogS.show()
                bindingS.speedText.text = "${DecimalFormat("#.##").format(speed)} X"
                bindingS.minusBtn.setOnClickListener {
                    changeSpeed(isIncrement = false)
                    bindingS.speedText.text = "${DecimalFormat("#.##").format(speed)} X"
                }
                bindingS.plusBtn.setOnClickListener {
                    changeSpeed(isIncrement = true)
                    bindingS.speedText.text = "${DecimalFormat("#.##").format(speed)} X"
                }
            }

            bindingMF.sleepTimer.setOnClickListener {
                dialog.dismiss()
                if (timer != null) Toast.makeText(this, "Timer Already Running!\nClose App to Reset Timer",Toast.LENGTH_SHORT).show()
                else{
                    var sleepTime =15
                    val customDialogS =LayoutInflater.from(this).inflate(R.layout.speed_dialog, binding.root, false)
                    val bindingS =SpeedDialogBinding.bind(customDialogS)
                    val dialogS = MaterialAlertDialogBuilder(this).setView(customDialogS)
                        .setCancelable(false)
                        .setPositiveButton("OK"){self, _ ->
                            timer = Timer()
                            val task = object : TimerTask(){
                                override fun run() {
                                    moveTaskToBack(true)
                                    exitProcess(1)
                                }
                            }
                            timer!!.schedule(task, sleepTime*60*1000.toLong())
                            self.dismiss()
                            playVideo()
                        }
                        .setBackground(ColorDrawable(0x80915F6D.toInt()))
                        .create()
                    dialogS.show()
                    bindingS.speedText.text = "$sleepTime Min"
                    bindingS.minusBtn.setOnClickListener {
                        if (sleepTime > 15) sleepTime -= 15
                        bindingS.speedText.text ="$sleepTime Min"
                    }
                    bindingS.plusBtn.setOnClickListener {
                        if (sleepTime < 120)sleepTime += 15
                        bindingS.speedText.text = "$sleepTime Min"
                    }
                }
            }
            bindingMF.pipModeBtn.setOnClickListener{
                val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
                val status = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    appOps.checkOpNoThrow(AppOpsManager.OPSTR_PICTURE_IN_PICTURE, android.os.Process.myUid(),packageName) == AppOpsManager.MODE_ALLOWED
                } else { false }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    if (status) {
                        this.enterPictureInPictureMode(PictureInPictureParams.Builder().build())
                        dialog.dismiss()
                        binding.playerView.hideController()
                        playVideo()
                        pipstatus =0

                    } else {
                        val intent = Intent(
                            "android.settings.Picture_In_Picture_SETTINGS",
                            Uri.parse("package:$packageName")
                        )
                        startActivity(intent)
                    }
                }else{Toast.makeText(this,"Feature NOt Supported", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
                    playVideo()
                }

            }

        }
    }

    private fun createPlayer(){
        try{ player.release()}catch (e: Exception){}
        speed =1.0f
        trackSelector = DefaultTrackSelector(this)
        binding.videoTitle.text = playerList[position].title
        binding.videoTitle.isSelected= true
        player = SimpleExoPlayer.Builder(this).setTrackSelector(trackSelector).build()
        binding.playerView.player = player
        val mediaItem =MediaItem.fromUri(playerList[position].artUri)
        player.setMediaItem(mediaItem)
        player.prepare()
        playVideo()
        player.addListener(object : Player.Listener{
            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                if (playbackState == Player.STATE_ENDED) nextPrevVideo()
            }
        })
        playInFullscreen(enable = isFullscreen)
        setVisibility()
        loudnessEnhancer = LoudnessEnhancer(player.audioSessionId)
        loudnessEnhancer.enabled =true
    }
    private fun playVideo(){
        binding.playPause.setImageResource(R.drawable.pause_icon)
        player.play()
    }
    private fun pauseVideo(){
        binding.playPause.setImageResource(R.drawable.play_icon)
        player.pause()

    }
    private fun nextPrevVideo(isNext: Boolean = true){
        if (isNext) setPosition()
        else setPosition(isIncrement = false)
        createPlayer()
    }
    private fun setPosition(isIncrement: Boolean =true){
     if (!repeat){
         if (isIncrement){
             if (playerList.size -1 == position)
                 position =0
             else ++position
         }else{
             if (position ==0)
                 position = playerList.size -1
             else --position
         }
     }
    }
    private fun playInFullscreen(enable : Boolean){
        if (enable){
            binding.playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
            player.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
            binding.fullScreenBtn.setImageResource(R.drawable.fullscreen_exit_icon)
        }else{
            binding.playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
            player.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT
            binding.fullScreenBtn.setImageResource(R.drawable.fullscreen_icon)
        }
    }
    private fun setVisibility(){
        runnable =Runnable{
            if (binding.playerView.isControllerVisible) changeVisibility(View.VISIBLE)
            else changeVisibility(View.INVISIBLE)
            Handler(Looper.getMainLooper()).postDelayed(runnable, 300)
        }
        Handler(Looper.getMainLooper()).postDelayed(runnable, 0)

    }
    private fun changeVisibility(visibility : Int){
        binding.topController.visibility  =visibility
        binding.bottomController.visibility  =visibility
        binding.playPause.visibility  =visibility

        if (isLocked) binding.lockButton.visibility = View.VISIBLE
        else  binding.lockButton.visibility = visibility
    }
    private fun changeSpeed(isIncrement: Boolean){
        if (isIncrement){
            if (speed <=2.9f){
                speed += 0.10f
            }
        }else{
            if (speed > 0.20f){
                speed -= 0.10f
            }
        }
        player.setPlaybackSpeed(speed)
    }

    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean, newConfig: Configuration?) {
        if (pipstatus !=0){
            finish()
            val intent =Intent(this, VideoPlayerActivity::class.java)
            when(pipstatus){
                 1 -> intent.putExtra("class", "FolderActivity")
                2 -> intent.putExtra("class", "AllVideos")
            }
           startActivity(intent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        player.release()
    }
}