package com.sahilSamiyal.musicPlayer

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager

import android.graphics.Color
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.MenuItem
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.sahilSamiyal.musicPlayer.databinding.ActivityVideo2Binding
import java.io.File
import java.lang.Exception

class VideoActivity2 : AppCompatActivity() {

    private lateinit var binding: ActivityVideo2Binding
    private lateinit var toggle: ActionBarDrawerToggle

    companion object{
        lateinit var videoList: ArrayList<Video>
        lateinit var folderList: ArrayList<Folder>
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideo2Binding.inflate(layoutInflater)
        setTheme(R.style.coolPinkNav)
        setContentView(binding.root)

        //for nav drawer
        toggle = ActionBarDrawerToggle(this, binding.root, R.string.open, R.string.close)
        binding.root.addDrawerListener(toggle)
        toggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        if (requestRuntimePermission()) {
            folderList = ArrayList()
            videoList = getAllVideos()
            setFragment(VideoFragment())
        }
        binding.bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.videoView -> setFragment(VideoFragment())
                R.id.folderView -> setFragment(FoldersFragment())
            }
            return@setOnItemSelectedListener true
        }
        binding.navView.setNavigationItemSelectedListener{
            when(it.itemId)
            {
                R.id.navFeedback -> startActivity(Intent(this, FeedbackActivity::class.java))
                R.id.navSettings -> startActivity(Intent(this, SettingsActivity::class.java))
                R.id.navAbout -> startActivity(Intent(this, AboutActivity::class.java))
                R.id.navExit -> {
                    val builder = MaterialAlertDialogBuilder(this)
                    builder.setTitle("Exit")
                        .setMessage("Do you want to close app?")
                        .setPositiveButton("Yes"){ _, _ ->
                            exitApplication()
                        }
                        .setNegativeButton("No"){dialog, _ ->
                            dialog.dismiss()
                        }
                    val customDialog = builder.create()
                    customDialog.show()
                    customDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED)
                    customDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.RED)
                }
            }
            true
        }
    }

    private fun setFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragmentFL, fragment)
        transaction.disallowAddToBackStack()
        transaction.commit()
    }

    private fun requestRuntimePermission() :Boolean{
        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), 13)
            return false
        }
        return true
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == 13){
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
                folderList =ArrayList()
                videoList =getAllVideos()
                setFragment(VideoFragment())
               // initializeLayout()
            }
            else
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), 13)
        }
    }



    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item))
            return true
        return super.onOptionsItemSelected(item)
    }

    @SuppressLint("InlinedApi","Recycle", "Range")
    private fun getAllVideos(): ArrayList<Video> {
        val tempList =ArrayList<Video>()
        val tempFolderList =ArrayList<String>()
        val  projection = arrayOf(MediaStore.Video.Media.TITLE, MediaStore.Video.Media.SIZE , MediaStore.Video.Media._ID,MediaStore.Video.Media.BUCKET_DISPLAY_NAME
        ,MediaStore.Video.Media.DATA, MediaStore.Video.Media.DATE_ADDED,MediaStore.Video.Media.DURATION, MediaStore.Video.Media.BUCKET_ID)
        val cursor = this.contentResolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, projection,null,null,
            MediaStore.Video.Media.DATE_ADDED + " Desc")
        if(cursor != null)
            if(cursor.moveToNext())
                do {val titleC = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.TITLE))
                    val idC = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media._ID))
                    val folderC = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.BUCKET_DISPLAY_NAME))
                    val folderIdC = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.BUCKET_ID))
                    val sizeC =  cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.SIZE))
                    val pathC = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA))
                    val durationC = cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media.DURATION))
                try {
                    val file =File(pathC)
                    val artUriC = Uri.fromFile(file)
                    val video = Video(title = titleC, id = idC , folderName = folderC, duration = durationC,size = sizeC, path = pathC, artUri = artUriC)
                    if (file.exists()) tempList.add(video)
                    // for adding folders
                    if(!tempFolderList.contains(folderC)){
                        tempFolderList.add(folderC)
                        folderList.add(Folder(id = folderIdC,folderName = folderC))
                    }
                }catch (e:Exception){}
            }while (cursor.moveToNext())
            cursor?.close()
        return tempList
    }
    }


