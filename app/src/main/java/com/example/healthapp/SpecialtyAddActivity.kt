package com.example.healthapp

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.widget.ActivityChooserView
import com.example.healthapp.databinding.ActivityDashboardUserBinding
import com.example.healthapp.databinding.ActivitySpecialtyAddBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class SpecialtyAddActivity : AppCompatActivity() {

    //view binding
    private lateinit var binding: ActivitySpecialtyAddBinding

    //firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

    //progress dialog
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySpecialtyAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //init //firebase auth
        firebaseAuth = FirebaseAuth.getInstance()

        //configure progress dialog
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Por favor espere...")
        progressDialog.setCanceledOnTouchOutside(false)

        //handle click, go back
        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        //handle click, begin upload category
        binding.submitBtn.setOnClickListener {
            validateData()
        }
    }

    private var specialty = ""

    private fun validateData() {
        //validate data
        specialty = binding.specialtyEt.text.toString().trim()

        //validate date
        if(specialty.isEmpty()) {
            Toast.makeText(this, "Ingrese la especialidad...", Toast.LENGTH_SHORT).show()
        }
        else {
            addSpecialtyFirebase()
        }
    }

    private fun addSpecialtyFirebase() {
        //show progress
        progressDialog.show()

        //get timestamp
        val timestamp = System.currentTimeMillis()

        //setup data to add in firebase db
        val hashMap =  HashMap<String, Any>()
        hashMap["id"] = "$timestamp"
        hashMap["specialty"] = specialty
        hashMap["timestamp"] = timestamp
        hashMap["uid"] = "${firebaseAuth.uid}"

        //add to firebase db: Database Root > Specialties > specialtyId > specialty info
        val ref = FirebaseDatabase.getInstance().getReference("Specialties")
        ref.child("$timestamp")
            .setValue(hashMap)
            .addOnSuccessListener {
                //added successfully
                progressDialog.dismiss()
                Toast.makeText(this, "Se añadió exitosamente", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e->
                //failed to add
                Toast.makeText(this, "Falló al ingresar la especialidad debido a ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}