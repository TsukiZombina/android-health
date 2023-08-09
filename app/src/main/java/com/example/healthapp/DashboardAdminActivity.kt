package com.example.healthapp

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import com.example.healthapp.databinding.ActivityDashboardAdminBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class DashboardAdminActivity : AppCompatActivity() {

    //view binding
    private lateinit var binding: ActivityDashboardAdminBinding

    //firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

    // Arraylist to hold specialties
    private lateinit var specialtyArrayList: ArrayList<ModelSpecialty>

    // adapter
    private lateinit var adapterSpecialty: AdapterSpecialty
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //init firebase Auth
        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()
        loadSpecialties()

        // search
        binding.searchEt.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                try {
                    adapterSpecialty.filter.filter(s)
                }
                catch (e: Exception) {

                }
            }

            override fun afterTextChanged(p0: Editable?) {
            }
        })

        //handle click, logout
        binding.logoutBtn.setOnClickListener {
            firebaseAuth.signOut()
            checkUser()
        }

        //handle click, start add specialty
        binding.addSpecialtyBtn.setOnClickListener {
            startActivity(Intent(this, SpecialtyAddActivity::class.java))
        }
    }

    private fun loadSpecialties() {
        // init arraylist
        specialtyArrayList = ArrayList()

        // get all specialties from firebase database... Firebase DB > Specialties
        val ref = FirebaseDatabase.getInstance().getReference("Specialties")
        ref.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // clear list before starting adding data into it
                specialtyArrayList.clear()

                for (ds in snapshot.children) {
                    // get data as model
                    val model = ds.getValue(ModelSpecialty::class.java)

                    // add to array list
                    specialtyArrayList.add(model!!)
                }

                // Setup adapter
                adapterSpecialty = AdapterSpecialty(this@DashboardAdminActivity, specialtyArrayList)

                // Set adapter to recyclerview
                binding.specialtiesRv.adapter = adapterSpecialty
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }
    private fun checkUser() {
        //get current user
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser == null) {
            //not logged in, go to main screen
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
        else {
            //logged in, get and show user info
            val email = firebaseUser.email
            //set to textview of toolbar
            binding.subTitleTv.text = email
        }
    }
}