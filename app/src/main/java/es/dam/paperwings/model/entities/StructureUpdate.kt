package es.dam.paperwings.model.entities

import com.google.gson.annotations.SerializedName

data class StructureUpdate (
        @SerializedName("body")
        val data: String,
        @SerializedName("error")
        val error: Boolean,
        @SerializedName("status")
        val status: Int

)