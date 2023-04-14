package com.example.uberclone

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.Menu
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.bumptech.glide.Glide
import com.example.uberclone.Utils.UserUtils
import com.example.uberclone.databinding.ActivityDriverHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask.TaskSnapshot
import java.util.Objects

class DriverHomeActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityDriverHomeBinding

    private lateinit var img_avatar: ImageView
    private lateinit var waitingDialog: AlertDialog
    private lateinit var storageReference: StorageReference
    private  var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDriverHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarDriverHome.toolbar)


        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_driver_home)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        init()
    }

    private fun init(){

        storageReference = FirebaseStorage.getInstance().getReference()

        waitingDialog = AlertDialog.Builder(this)
            .setMessage("Ожидание...")
            .setCancelable(false).create()

        binding.navView.setNavigationItemSelectedListener { it ->
            if (it.itemId == R.id.nav_sign_out)
            {
                val builder = AlertDialog.Builder(this@DriverHomeActivity)


                builder.setTitle("Выход")
                    .setMessage("Вы действительно хотите выйти")
                    .setNegativeButton("Закрыть") { dialogInterface, _ -> dialogInterface.dismiss() }

                    .setPositiveButton("Выйти"){dialogInterface, _ ->

                        FirebaseAuth.getInstance().signOut()
                        val intent = Intent(this@DriverHomeActivity,SplashScreen::class.java)
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        startActivity(intent)
                        finish()

                    }.setCancelable(false)

                val dialog = builder.create()
                dialog.setOnShowListener {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        .setTextColor(ContextCompat.getColor(this@DriverHomeActivity,android.R.color.holo_red_dark))
                    dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                        .setTextColor(ContextCompat.getColor(this@DriverHomeActivity,android.R.color.holo_red_dark))
                }

                dialog.show()

            }
            true
        }

        val headerView = binding.navView.getHeaderView(0)
        val txt_name = headerView.findViewById<View>(R.id.txt_name) as TextView
        val txt_phone = headerView.findViewById<View>(R.id.txt_phone) as TextView

        img_avatar = headerView.findViewById(R.id.img_avatar) as ImageView
        txt_name.text = Common.buildWelcomeMessage()
        txt_phone.text = Common.currentUser!!.phoneNumber

        if (Common.currentUser != null && Common.currentUser!!.avatar != null && !TextUtils.isEmpty(Common.currentUser!!.avatar))
        {
            Glide.with(this)
                .load(Common.currentUser!!.avatar)
                .into(img_avatar)
        }

        img_avatar.setOnClickListener {
            val  intent = Intent()
            intent.setType("image/*")
            intent.setAction(Intent.ACTION_GET_CONTENT)
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK)
        {
            if (data != null && data.data != null)
            {
                imageUri = data.data
                img_avatar.setImageURI(imageUri)

                showDialogUpload()
            }
        }
    }

    private fun showDialogUpload() {
        val builder = AlertDialog.Builder(this@DriverHomeActivity)


        builder.setTitle("Поменять аватар")
            .setMessage("Вы действительно хотите поменять Аватар")
            .setNegativeButton("Закрыть") { dialogInterface, _ -> dialogInterface.dismiss() }

            .setPositiveButton("Поменять"){dialogInterface, _ ->

                if (imageUri != null)
                {
                    waitingDialog.show()
                    val avatarFolder = storageReference.child("avatars/"+FirebaseAuth.getInstance().currentUser!!.uid)

                    avatarFolder.putFile(imageUri!!)
                        .addOnFailureListener { e ->
                            Snackbar.make(binding.drawerLayout, e.message!!, Snackbar.LENGTH_SHORT).show()
                            waitingDialog.dismiss()
                        }.addOnCompleteListener { task ->
                            if (task.isSuccessful)
                            {
                                avatarFolder.downloadUrl.addOnSuccessListener { uri ->
                                    val update_data = HashMap<String, Any>()
                                    update_data.put("avatar", uri.toString())

                                    UserUtils.updateUser(binding.drawerLayout, update_data)
                                }
                            }
                            waitingDialog.dismiss()
                        }.addOnProgressListener { taskSnapshot ->
                            val progress = (100.0*taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount)
                            waitingDialog.setMessage(StringBuilder("uploading: ").append(progress).append("%"))
                        }
                }

            }.setCancelable(false)

        val dialog = builder.create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setTextColor(ContextCompat.getColor(this@DriverHomeActivity,android.R.color.holo_red_dark))
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                .setTextColor(ContextCompat.getColor(this@DriverHomeActivity,android.R.color.holo_red_dark))
        }

        dialog.show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.driver_home, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_driver_home)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    companion object{
        val PICK_IMAGE_REQUEST = 7272
    }
}