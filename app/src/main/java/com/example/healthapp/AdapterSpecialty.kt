package com.example.healthapp;

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.FirebaseDatabase
import com.example.healthapp.databinding.RowSpecialtyBinding

class AdapterSpecialty : RecyclerView.Adapter<AdapterSpecialty.HolderSpecialty>, Filterable {

    private val context: Context
    public var specialtyArrayList: ArrayList<ModelSpecialty>
    private var filterList: ArrayList<ModelSpecialty>

    private var filter: FilterSpecialty? = null

    private lateinit var binding: RowSpecialtyBinding

    // constructor
    constructor(context: Context, specialtyArrayList: ArrayList<ModelSpecialty>) {
        this.context = context
        this.specialtyArrayList = specialtyArrayList
        this.filterList = specialtyArrayList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderSpecialty {
        binding = RowSpecialtyBinding.inflate(LayoutInflater.from(context), parent, false )

        return HolderSpecialty(binding.root)
    }

    override fun getItemCount(): Int {
        return specialtyArrayList.size
    }

    override fun onBindViewHolder(holder: HolderSpecialty, position: Int) {
        // Get data, set data, handle click events

        // get data
        val model = specialtyArrayList[position]
        val id = model.id
        val specialty = model.specialty
        val uid = model.uid
        val timestamp = model.timestamp

        // set data
        holder.specialtyTv.text = specialty

        // handle click, delete specialty
        holder.deleteBtn.setOnClickListener {
            // confirm before delete
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Delete")
                .setMessage("Are you sure you want to delete this specialty")
                .setPositiveButton("Confirm") { a, d->
                    Toast.makeText(context, "Deleting...", Toast.LENGTH_SHORT).show()
                    deleteSpecialty(model, holder)
                }
                .setNegativeButton("Cancel") {a, d->
                    a.dismiss()
                }
                .show()
        }
    }

    private fun deleteSpecialty(model: ModelSpecialty, holder: HolderSpecialty) {
        // Get id of specialty to delete
        val id = model.id
        // Firebase DB > Specialties > specialtyID
        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.child(id)
            .removeValue()
            .addOnSuccessListener {
                Toast.makeText(context, "Deleted...", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e->
                Toast.makeText(context, "Unable to delete due to ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    inner class HolderSpecialty(itemView: View): RecyclerView.ViewHolder(itemView) {
        // init ui views
        var specialtyTv:TextView = binding.specialtyTv
        var deleteBtn:ImageButton = binding.deleteBtn
    }

    override fun getFilter(): Filter {
        if (filter == null) {
            filter = FilterSpecialty(filterList, this)
        }
        return filter as FilterSpecialty
    }
}