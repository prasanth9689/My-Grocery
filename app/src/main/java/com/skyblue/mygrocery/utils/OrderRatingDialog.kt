package com.skyblue.mygrocery.utils

import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.skyblue.mygrocery.databinding.DialogRateOrderBinding

class OrderRatingDialog(
    private val orderId: String,
    private val onRatingSubmitted: (Float, String) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: DialogRateOrderBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DialogRateOrderBinding.inflate(inflater, container, false)

        binding.ratingBar.setOnRatingBarChangeListener { _, _, _ ->
            binding.ratingBar.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSubmitRating.setOnClickListener {
            val rating = binding.ratingBar.rating
            val feedback = binding.etFeedback.text.toString()

            if (rating > 0) {
                onRatingSubmitted(rating, feedback)
                dismiss()
            } else {
                Toast.makeText(context, "Please select a star rating", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}