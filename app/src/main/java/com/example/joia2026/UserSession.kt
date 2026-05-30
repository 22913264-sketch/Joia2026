package com.example.joia2026

import android.content.Context

object UserSession {
    private const val PREFS_NAME = "JoiaPrefs"
    private const val KEY_TOKEN = "token"
    private const val KEY_LOGGED_IN = "logged_in"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_USER_NAME = "user_name"
    private const val KEY_USER_EMAIL = "user_email"
    private const val KEY_USER_CPF = "user_cpf"
    private const val KEY_USER_PHONE = "user_phone"
    private const val KEY_USER_COURSE_ID = "user_course_id"
    private const val KEY_USER_COURSE_NAME = "user_course_name"
    private const val KEY_USER_ROLE = "user_role"
    private const val KEY_PROFILE_PHOTO_URI = "profile_photo_uri"

    fun saveToken(context: Context, token: String?) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_TOKEN, token)
            .apply()
    }

    fun saveRegisteredUser(
        context: Context,
        nome: String,
        email: String,
        cpf: String,
        telefone: String,
        id: String? = null,
        cursoId: String? = null,
        cursoNome: String? = null,
        role: String? = null
    ) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_USER_ID, id)
            .putString(KEY_USER_NAME, nome)
            .putString(KEY_USER_EMAIL, email)
            .putString(KEY_USER_CPF, cpf)
            .putString(KEY_USER_PHONE, telefone)
            .putString(KEY_USER_COURSE_ID, cursoId)
            .putString(KEY_USER_COURSE_NAME, cursoNome)
            .putString(KEY_USER_ROLE, role ?: getUserData(context).role ?: "VIEWER")
            .apply()
    }

    fun saveLoggedUser(context: Context, user: User?) {
        if (user == null) return

        val current = getUserData(context)
        val canUseSavedDocumentData = current.email == user.email
        val cursoId = user.cursoId ?: user.curso?.id ?: current.cursoId.takeIf { canUseSavedDocumentData }
        val cursoNome = user.curso?.nome ?: current.cursoNome.takeIf { canUseSavedDocumentData }
        val role = if (canUseSavedDocumentData && current.role == "ADMIN") "ADMIN" else user.role
        saveRegisteredUser(
            context = context,
            nome = user.nome,
            email = user.email,
            cpf = user.cpf ?: current.cpf.orEmpty().takeIf { canUseSavedDocumentData }.orEmpty(),
            telefone = user.telefone ?: current.telefone.orEmpty().takeIf { canUseSavedDocumentData }.orEmpty(),
            id = user.id,
            cursoId = cursoId,
            cursoNome = cursoNome,
            role = role
        )
    }

    fun markLoggedIn(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_LOGGED_IN, true)
            .apply()
    }

    fun getToken(context: Context): String? {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_TOKEN, null)
    }

    fun isLoggedIn(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_LOGGED_IN, false) && !prefs.getString(KEY_TOKEN, null).isNullOrBlank()
    }

    fun isAdmin(context: Context): Boolean {
        return getUserData(context).role == "ADMIN"
    }

    fun getUserData(context: Context): UserData {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return UserData(
            nome = prefs.getString(KEY_USER_NAME, null),
            email = prefs.getString(KEY_USER_EMAIL, null),
            cpf = prefs.getString(KEY_USER_CPF, null),
            telefone = prefs.getString(KEY_USER_PHONE, null),
            cursoId = prefs.getString(KEY_USER_COURSE_ID, null),
            cursoNome = prefs.getString(KEY_USER_COURSE_NAME, null),
            role = prefs.getString(KEY_USER_ROLE, null),
            fotoPerfilUri = prefs.getString(KEY_PROFILE_PHOTO_URI, null)
        )
    }

    fun updateProfile(
        context: Context,
        nome: String,
        cursoId: String?,
        cursoNome: String?,
        cpf: String? = null,
        telefone: String? = null
    ) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_USER_NAME, nome)
            .putString(KEY_USER_COURSE_ID, cursoId)
            .putString(KEY_USER_COURSE_NAME, cursoNome)
            .putString(KEY_USER_CPF, cpf ?: getUserData(context).cpf)
            .putString(KEY_USER_PHONE, telefone ?: getUserData(context).telefone)
            .apply()
    }

    fun saveProfilePhoto(context: Context, uri: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_PROFILE_PHOTO_URI, uri)
            .apply()
    }

    fun clear(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()
    }
}

data class UserData(
    val nome: String?,
    val email: String?,
    val cpf: String?,
    val telefone: String?,
    val cursoId: String?,
    val cursoNome: String?,
    val role: String?,
    val fotoPerfilUri: String?
)
