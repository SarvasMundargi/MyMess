package com.example.mymess

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.mymess.databinding.ActivityLoginBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private val PICK_IMAGE_REQUEST = 1
    private var imageUri: Uri? = null
    private var latitude: String="0.0"
    private var longitude: String="0.0"
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var binding: ActivityLoginBinding
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Request location permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE)
        } else {
            getCurrentLocation()
        }

        val action = intent.getStringExtra("action")

        if (action == "login") {
            binding.loginEmail.visibility = View.VISIBLE
            binding.loginPassword.visibility = View.VISIBLE
            binding.tvNewhere.visibility = View.INVISIBLE
            binding.buttonRegister.visibility = View.INVISIBLE
            binding.registerEmail.visibility = View.GONE
            binding.registerName.visibility = View.GONE
            binding.registerPassword.visibility = View.GONE
            binding.cardView.visibility = View.GONE
            binding.tvForgotPassword.visibility = View.VISIBLE

            // Handling Forgot Password
            binding.tvForgotPassword.setOnClickListener {
                startActivity(Intent(this, ForgotPasswordActivity::class.java))
            }

            // Handling login
            binding.buttonLogin.setOnClickListener {
                val email = binding.loginEmail.text.toString()
                val password = binding.loginPassword.text.toString()

                if (email.isBlank() || password.isBlank()) {
                    Toast.makeText(this, "Please fill all the Credentials", Toast.LENGTH_SHORT).show()
                } else {
                    binding.loadingAnimation.visibility = View.VISIBLE
                    binding.loadingImg.visibility = View.VISIBLE
                    binding.buttonLogin.visibility = View.GONE

                    auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this) {
                        binding.loadingAnimation.visibility = View.GONE
                        binding.loadingImg.visibility = View.GONE
                        if (it.isSuccessful) {
                            Toast.makeText(this, "Logged In😁", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        } else {
                            Toast.makeText(this, "Not proper credentials", Toast.LENGTH_SHORT).show()
                            binding.buttonLogin.visibility = View.VISIBLE
                            binding.loadingImg.visibility = View.GONE
                        }
                    }
                }
            }

        } else if (action == "register") {
            binding.buttonLogin.isEnabled = false
            binding.buttonLogin.alpha = 0.5f

            binding.buttonRegister.setOnClickListener {
                val email = binding.registerEmail.text.toString()
                val password = binding.registerPassword.text.toString()
                val name = binding.registerName.text.toString()

                if (email.isBlank() || password.isBlank() || name.isBlank()) {
                    Toast.makeText(this, "Please fill all the Credentials", Toast.LENGTH_SHORT).show()
                } else {
                    binding.loadingAnimation.visibility = View.VISIBLE
                    binding.loadingImg.visibility = View.VISIBLE
                    binding.buttonRegister.visibility = View.GONE
                    auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
                        binding.loadingAnimation.visibility = View.GONE
                        binding.loadingImg.visibility = View.GONE
                        if (it.isSuccessful) {
                            val user = auth.currentUser
                            // Adding the user to the database
                            user?.let {
                                addUserData(name, email, auth.currentUser!!.uid)
                                Toast.makeText(this, "Logged In", Toast.LENGTH_SHORT).show()
                                val intent = Intent(this, MainActivity::class.java)
                                intent.putExtra("messname", name)
                                intent.putExtra("messid", auth.currentUser!!.uid)
                                startActivity(intent)
                                finish()
                            }
                        } else {
                            Toast.makeText(this, "Error Connecting User", Toast.LENGTH_SHORT).show()
                            binding.buttonRegister.visibility = View.VISIBLE
                            binding.loadingImg.visibility = View.GONE
                        }
                    }
                }
            }
        }
    }

    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                latitude = it.latitude.toString()
                longitude = it.longitude.toString()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                getCurrentLocation()
            } else {
                // Permission denied
            }
        }
    }

    private fun addUserData(
        name: String,
        email: String,
        uid: String,
    ) {
        database= FirebaseDatabase.getInstance().getReference()
        database.child("MessOwners").child(uid).child("MessName").setValue(name)
        database.child("MessOwners").child(uid).child("Latitude").setValue(latitude)
        database.child("MessOwners").child(uid).child("longitude").setValue(longitude)
    }
}