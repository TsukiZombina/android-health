package com.example.healthapp

import android.widget.Filter

class FilterSpecialty: Filter {
    // Arraylist in which we want to search
    private var filterList: ArrayList<ModelSpecialty>

    // adapter in which filter needs to be implemented
    private var adapterSpecialty: AdapterSpecialty

    constructor(filterList: ArrayList<ModelSpecialty>, adapterSpecialty: AdapterSpecialty) : super() {
        this.filterList = filterList
        this.adapterSpecialty = adapterSpecialty
    }

    override fun performFiltering(constraint: CharSequence?): FilterResults {
        var constraint = constraint
        val results = FilterResults()

        // value should not be null and not empty
        if (constraint != null && constraint.isNotEmpty()) {
            // change to uppercase, or lower case to avoid case sensitivity
            constraint = constraint.toString().uppercase()
            val filteredModels: ArrayList<ModelSpecialty> = ArrayList()

            for (i in 0 until filterList.size) {
                // validate
                if (filterList[i].specialty.uppercase().contains(constraint)) {
                    // add to filtered list
                    filteredModels.add(filterList[i])
                }
            }

            results.count = filteredModels.size
            results.values = filteredModels
        }
        else {
            // search value is either null or empty
            results.count = filterList.size
            results.values = filterList
        }

        return results
    }

    override fun publishResults(constraint: CharSequence?, results: FilterResults) {
        // apply filter changes
        adapterSpecialty.specialtyArrayList = results.values as ArrayList<ModelSpecialty>
    }
}