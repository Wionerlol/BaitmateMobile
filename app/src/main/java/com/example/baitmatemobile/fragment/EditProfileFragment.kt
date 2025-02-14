package com.example.baitmatemobile.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.baitmatemobile.R
import com.example.baitmatemobile.model.User
import com.example.baitmatemobile.network.RetrofitClient
import kotlinx.coroutines.launch


class EditProfileFragment : Fragment() {
    private lateinit var user: User

    companion object {
        private const val ARG_USER = "user"
        fun newInstance(user: User): EditProfileFragment {
            val fragment = EditProfileFragment()
            val args = Bundle()
            args.putParcelable(ARG_USER, user)
            fragment.arguments=args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.getParcelable<User>(ARG_USER)?.let {
            user = it
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_edit_profile, container, false)

        val usernameField = view.findViewById<EditText>(R.id.edit_username)
        val phoneNumberField = view.findViewById<EditText>(R.id.edit_phone_number)
        val emailField = view.findViewById<EditText>(R.id.edit_email)
        val genderField = view.findViewById<EditText>(R.id.edit_gender)
        val addressField = view.findViewById<EditText>(R.id.edit_address)
        val saveButton = view.findViewById<Button>(R.id.btn_save_profile)

        usernameField.setText(user.username)
        phoneNumberField.setText(user.phoneNumber)
        emailField.setText(user.email)
        genderField.setText(user.gender)
        addressField.setText(user.address)

        saveButton.setOnClickListener {
            val userId = user.id
            val updatedUser = user.copy(
                username = usernameField.text.toString(),
                phoneNumber = phoneNumberField.text.toString(),
                email = emailField.text.toString(),
                gender = genderField.text.toString(),
                address = addressField.text.toString()
            )
            if (userId != null) {
                saveProfile(userId, updatedUser)
            }
        }
        return view
    }

    private fun saveProfile(userId: Long, updatedUser: User) {
        lifecycleScope.launch {
            try{
                val response = RetrofitClient.instance.updateProfile(userId, updatedUser)
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                    Log.d("UpdateProfile", "Profile updated successfully")
                    requireActivity().supportFragmentManager.popBackStackImmediate()
                } else {
                    Toast.makeText(requireContext(), "Failed to update profile", Toast.LENGTH_SHORT).show()
                    Log.e("UpdateProfile", "Error: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Failed to update profile", Toast.LENGTH_SHORT).show()
                Log.e("UpdateProfile", "Exception: ${e.message}")
            }
        }
    }

}