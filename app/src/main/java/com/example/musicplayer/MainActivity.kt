package com.example.musicplayer


import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.example.musicplayer.databinding.ActivityMainBinding
import com.example.musicplayer.mainFragments.Albums
import com.example.musicplayer.mainFragments.Artists
import com.example.musicplayer.mainFragments.Tracks
import com.google.android.material.tabs.TabLayout
import kotlin.system.exitProcess


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var playerQueue: PlayerQueue
    private val musicPlayer = MediaPlayer()
    private lateinit var resultLauncher: ActivityResultLauncher<Intent>

    private val MY_PERMISSIONS_REQUEST = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        playerQueue = PlayerQueue(this.filesDir.path)

        playerQueue.readSavedQueue()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), MY_PERMISSIONS_REQUEST)
                Toast.makeText(this, "Explanation", Toast.LENGTH_LONG).show()
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), MY_PERMISSIONS_REQUEST)
                Toast.makeText(this, "Exp no needed", Toast.LENGTH_LONG).show()
            }
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            this.finish()
            exitProcess(0)
        }

        val tableLayout = binding.mainTabL
        val pager = binding.pazer
        val viewPagerAdapter = PageAdapter(this, musicPlayer, playerQueue)
        pager.adapter = viewPagerAdapter

        tableLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                pager.currentItem = tab.position
                if (supportFragmentManager.findFragmentByTag("f" + pager.currentItem) != null) {

                    when (tab.position) {
                        1 -> {
                            if (playerQueue.getUpdateArtists()) {

                                val page =
                                    supportFragmentManager.findFragmentByTag("f" + pager.currentItem) as Artists
                                page.update()
                                playerQueue.setUpdateArtists(false)
                                playerQueue.saveUpdate()
                            }
                        }

                        2 -> {
                            if (playerQueue.getUpdateAlbums()) {

                                val page =
                                    supportFragmentManager.findFragmentByTag("f" + pager.currentItem) as Albums
                                page.update()
                                playerQueue.setUpdateAlbums(false)
                                playerQueue.saveUpdate()
                            }
                        }

                        3 -> {
                            if (playerQueue.getUpdateTracks()) {

                                val page =
                                    supportFragmentManager.findFragmentByTag("f" + pager.currentItem) as Tracks
                                page.update()
                                playerQueue.setUpdateTracks(false)
                                playerQueue.saveUpdate()
                            }
                        }
                    }
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {

            }
            override fun onTabReselected(tab: TabLayout.Tab) {

            }
        })

        pager.registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                tableLayout.getTabAt(position)?.select()
            }
        })

        resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                result -> playerQueue.readUpdate()
        }

    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.library) {
            val intent = Intent(this, ScanMedia::class.java)
            val bundle = Bundle()
            bundle.putSerializable("queue", playerQueue)
            intent.putExtras(bundle)
            resultLauncher.launch(intent)
        }
        return super.onOptionsItemSelected(item)
    }

//    override fun onStop() {
//        super.onStop()
//
//    }
}