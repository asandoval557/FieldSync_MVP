package com.example.fieldsync.api

import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.http.*

data class Note(
    val id: Int? = null,

    @SerializedName("visit_id")
    val visitId: Int,

    @SerializedName("body")
    val body: String,

    @SerializedName("photo_url")
    val photoUrl: String? = null
)

interface VisitNotesApi {
    @GET("visits/{visit_id}/notes")
    fun getNotes(@Path("visit_id") visitId: Int): Call<List<Note>>

    @POST("visits/{visit_id}/notes")
    fun addNote(
        @Path("visit_id") visitId: Int,
        @Body note: Note
    ): Call<Note>

    @PUT("visits/{visit_id}/notes/{note_id}")
    fun updateNote(
        @Path("visit_id") visitId: Int,
        @Path("note_id") noteId: Int,
        @Body note: Note
    ): Call<Note>

    @DELETE("visits/{visit_id}/notes/{note_id}")
    fun deleteNote(
        @Path("visit_id") visitId: Int,
        @Path("note_id") noteId: Int
    ): Call<Unit>
}
