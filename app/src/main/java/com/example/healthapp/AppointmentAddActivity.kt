package com.example.healthapp

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import com.google.firebase.auth.FirebaseAuth
import com.example.healthapp.databinding.ActivityAppointmentAddBinding
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class AppointmentAddActivity : AppCompatActivity() {

    // setup view binding activity appointment add --> ActivityAppointmentAddBinding
    private lateinit var binding: ActivityAppointmentAddBinding

    // firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

    // progress dialog (show while registering appointment)
    private lateinit var progressDialog: ProgressDialog

    // Array list to hold appointment specialties
    private lateinit var specialtyArrayList: ArrayList<ModelSpecialty>

    // uri of picked pdf
    private var pdfUri: Uri? = null

    // TAG
    private val TAG = "PDF_ADD_TAG"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppointmentAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // init firebase auth
        firebaseAuth = FirebaseAuth.getInstance()
        loadPdfSpecialties()

        // setup progress dialog
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Por favor espere...")
        progressDialog.setCanceledOnTouchOutside(false)

        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        // handle click, show specialty pick dialog
        binding.specialtyTv.setOnClickListener {
            specialtyPickDialog()
        }

        binding.attachPdfBtn.setOnClickListener {
            pdfPickIntent()
        }

        // handle click, start uploading pdf / book
        binding.submitBtn.setOnClickListener {
            // STEP 1: validate data
            // STEP 2: Upload pdf to firebase store
            // STEP 3: Get url of uploaded pdf
            // STEP 4: Upload pdf info to firebase db

            validateData()
        }
    }

    private var title = ""
    private var doctor = ""
    private var specialty = ""

    private fun validateData() {
        Log.d(TAG, "validataData: validating data")

        title = binding.titleEt.text.toString().trim()
        doctor = binding.doctorEt.text.toString().trim()
        specialty = binding.specialtyTv.text.toString().trim()

        if (title.isEmpty()) {
            Toast.makeText(this, "Introduzca el motivo...", Toast.LENGTH_SHORT).show()
        }
        else if (doctor.isEmpty()) {
            Toast.makeText(this, "Introduzca el nombre del doctor...", Toast.LENGTH_SHORT).show()
        }
        else if (specialty.isEmpty()) {
            Toast.makeText(this, "Elija la especialidad", Toast.LENGTH_SHORT).show()
        }
        else if (pdfUri == null) {
            Toast.makeText(this, "Elija el archivo PDF", Toast.LENGTH_SHORT).show()
        }
        else {
            uploadPdfToStorage()
        }
    }

    private fun uploadPdfToStorage() {
        Log.d("TAG", "uploadPDFToStorage: uploading to storage")

        // Show progress dialog
        progressDialog.setMessage("Uploading PDF...")
        progressDialog.show()

        // Timestamp
        val timestamp = System.currentTimeMillis()

        // path of pdf in firebase storage
        val filePathAndName = "Appointments/$timestamp"

        // storage reference
        val storageReference = FirebaseStorage.getInstance().getReference(filePathAndName)

        storageReference.putFile(pdfUri!!)
            .addOnSuccessListener { taskSnapshot ->
                Log.d(TAG, "uploadPdfToStorage: PDF uploaded now getting url...")

                // STEP 3: Get url of uploaded pdf
                val uriTask: Task<Uri> = taskSnapshot.storage.downloadUrl

                while (!uriTask.isSuccessful) ;

                val uploadedPdfUrl = "${uriTask.result}"

                uploadPdfInfoToDb(uploadedPdfUrl, timestamp)
            }
            .addOnFailureListener { e->
                Log.d(TAG, "uploadPdfToStorage: failed to upload due to ${e.message}")
                progressDialog.dismiss()
                Toast.makeText(this, "No se pudo adjuntar el pdf: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun uploadPdfInfoToDb(uploadedPdfUrl: String, timestamp: Long) {
        Log.d(TAG, "uploadPdfInfoToDb: uploading to db")
        progressDialog.setMessage("Uploading pdf info...")

        // uid of current user
        val uid = firebaseAuth.uid

        // setup data to upload
        val hashMap: HashMap<String, Any> = HashMap()
        hashMap["uid"] = "$uid"
        hashMap["id"] = "$timestamp"
        hashMap["title"] = "$title"
        hashMap["doctor"] = "$doctor"
        hashMap["specialtyId"] = "$selectedSpecialtyId"
        hashMap["url"] = "$uploadedPdfUrl"
        hashMap["timestamp"] = timestamp
        hashMap["viewCount"] = 0
        hashMap["downloadsCount"] = 0

        val ref = FirebaseDatabase.getInstance().getReference("Appointments")
        ref.child("$timestamp")
            .setValue(hashMap)
            .addOnSuccessListener {
                Log.d(TAG, "uploadPdfInfoToDB: uploaded to DB")
                progressDialog.dismiss()
                Toast.makeText(this, "PDF subido con éxito...", Toast.LENGTH_SHORT).show()
                pdfUri = null
            }
            .addOnFailureListener { e->
                Log.d(TAG, "uploadPdfInfoToDB: failed to upload due to ${e.message}")
                progressDialog.dismiss()
                Toast.makeText(this, "No se pudo adjuntar el pdf: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadPdfSpecialties() {
        Log.d(TAG, "loadPdfSpecialties: Loading pdf specialties")

        // Init array list
        specialtyArrayList = ArrayList()

        // db reference to load specialties DF > Specialties
        val ref = FirebaseDatabase.getInstance().getReference("Specialties")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Clear list before adding data
                specialtyArrayList.clear()
                for (ds in snapshot.children) {
                    // get data
                    val model = ds.getValue(ModelSpecialty::class.java)

                    // add to arraylist
                    specialtyArrayList.add(model!!)
                    Log.d(TAG, "onDataChange: ${model.specialty}")
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private var selectedSpecialtyId = ""
    private var selectedSpecialtyTitle = ""

    private fun specialtyPickDialog() {
        Log.d(TAG, "specialtyPickDialog: Showing appointment specialty pick dialog")

        // get string array of specialties from arraylist
        val specialtiesArray = arrayOfNulls<String>(specialtyArrayList.size)
        for (i in specialtyArrayList.indices) {
            specialtiesArray[i] = specialtyArrayList[i].specialty
        }

        // alert dialog
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Elija una especialidad")
            .setItems(specialtiesArray) {dialog, which ->
                selectedSpecialtyTitle = specialtyArrayList[which].specialty
                selectedSpecialtyId = specialtyArrayList[which].id

                binding.specialtyTv.text = selectedSpecialtyTitle

                Log.d(TAG, "specialtyPickDialog: Selected Specialty ID: $selectedSpecialtyId" )
                Log.d(TAG, "specialtyPickDialog: Selected Specialty Title: $selectedSpecialtyTitle" )
            }
            .show()
    }

    private fun pdfPickIntent() {
        Log.d(TAG, "pdfPickIntent: starting pdf pick intent")

        val intent = Intent()
        intent.type = "application/pdf"
        intent.action = Intent.ACTION_GET_CONTENT
    }

    val specialtyActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback<ActivityResult> { result ->
            if (result.resultCode == RESULT_OK) {
                Log.d(TAG, "PDF picked")
                pdfUri = result.data!!.data
            }
            else {
                Log.d(TAG, "PDF pick cancelled")
                Toast.makeText(this, "Cancelado", Toast.LENGTH_SHORT).show()
            }
        }
    )
}