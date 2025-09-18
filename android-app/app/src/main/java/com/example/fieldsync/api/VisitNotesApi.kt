package com.example.fieldsync.api

import retrofit2.Call
import retrofit2.http.*

data class Note(
    val id: Int? = null,
    val visit_id: Int,
    val content: String
)

interface VisitNotesApi {
    @GET("notes/{visit_id}")
    fun getNotes(@Path("visit_id") visitId: Int): Call<List<Note>>

    @POST("notes/{visit_id}")
    fun addNote(@Path("visit_id") visitId: Int, @Body note: Note): Call<Note>

    @PUT("notes/{visit_id}/{note_id}")
    fun updateNote(
        @Path("visit_id") visitId: Int,
        @Path("note_id") noteId: Int,
        @Body note: Note
    ): Call<Note>

    @DELETE("notes/{visit_id}/{note_id}")
    fun deleteNote(
        @Path("visit_id") visitId: Int,
        @Path("note_id") noteId: Int
    ): Call<Unit>
}
